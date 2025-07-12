#include <jni.h>
#include <android/bitmap.h>
#include <string>
#include <vector>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
}

/**
 * Extract frames from WebM video using FFmpeg and convert to Android Bitmaps
 */
extern "C" JNIEXPORT jobjectArray JNICALL
Java_xyz_nextalone_nnngram_utils_MessageUtils_extractFramesFromWebmNative(
        JNIEnv *env, jclass /*clazz*/, jstring inputPath, jint maxFrames) {

    const char *input = env->GetStringUTFChars(inputPath, 0);

    AVFormatContext *inputFormatContext = nullptr;
    AVCodecContext *decoderContext = nullptr;
    SwsContext *swsContext = nullptr;
    jobjectArray resultArray = nullptr;

    do {
        // Open input file
        if (avformat_open_input(&inputFormatContext, input, nullptr, nullptr) < 0) {
            break;
        }

        if (avformat_find_stream_info(inputFormatContext, nullptr) < 0) {
            break;
        }

        // Find video stream
        int videoStreamIndex = -1;
        for (unsigned int i = 0; i < inputFormatContext->nb_streams; i++) {
            if (inputFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                videoStreamIndex = i;
                break;
            }
        }

        if (videoStreamIndex == -1) {
            break;
        }

        AVStream *videoStream = inputFormatContext->streams[videoStreamIndex];

        // Find decoder
        const AVCodec *decoder = avcodec_find_decoder(videoStream->codecpar->codec_id);
        if (!decoder) {
            break;
        }

        decoderContext = avcodec_alloc_context3(decoder);
        if (!decoderContext) {
            break;
        }

        if (avcodec_parameters_to_context(decoderContext, videoStream->codecpar) < 0) {
            break;
        }

        if (avcodec_open2(decoderContext, decoder, nullptr) < 0) {
            break;
        }

        // Calculate output dimensions (maintain aspect ratio)
        int inputWidth = decoderContext->width;
        int inputHeight = decoderContext->height;
        int outputWidth = inputWidth;
        int outputHeight = inputHeight;

        const int maxDimension = 320;
        if (inputWidth > maxDimension || inputHeight > maxDimension) {
            float scaleX = (float) maxDimension / inputWidth;
            float scaleY = (float) maxDimension / inputHeight;
            float scale = scaleX < scaleY ? scaleX : scaleY;

            outputWidth = (int) (inputWidth * scale);
            outputHeight = (int) (inputHeight * scale);

            // Ensure even dimensions
            outputWidth = (outputWidth + 1) & ~1;
            outputHeight = (outputHeight + 1) & ~1;
        }

        // Get original frame rate
        AVRational frameRate = videoStream->avg_frame_rate;
        if (frameRate.num == 0 || frameRate.den == 0) {
            frameRate = videoStream->r_frame_rate;
        }

        double originalFps = (double) frameRate.num / frameRate.den;
        double videoDuration = (double) inputFormatContext->duration / AV_TIME_BASE;
        double totalFrames = originalFps * videoDuration;

        int frameSkip = 1;
        if (totalFrames > maxFrames * 1.5) {
            frameSkip = (int) (totalFrames / maxFrames);
        }

        swsContext = sws_getContext(
                inputWidth, inputHeight, decoderContext->pix_fmt,
                outputWidth, outputHeight, AV_PIX_FMT_RGBA,
                SWS_LANCZOS, nullptr, nullptr, nullptr);

        if (!swsContext) {
            break;
        }

        // Allocate frames
        AVFrame *inputFrame = av_frame_alloc();
        AVFrame *scaledFrame = av_frame_alloc();

        if (!inputFrame || !scaledFrame) {
            if (inputFrame) av_frame_free(&inputFrame);
            if (scaledFrame) av_frame_free(&scaledFrame);
            break;
        }

        // Setup scaled frame
        scaledFrame->format = AV_PIX_FMT_RGBA;
        scaledFrame->width = outputWidth;
        scaledFrame->height = outputHeight;
        if (av_frame_get_buffer(scaledFrame, 32) < 0) {
            av_frame_free(&inputFrame);
            av_frame_free(&scaledFrame);
            break;
        }

        // Prepare bitmap array
        std::vector<jobject> bitmaps;
        jclass bitmapClass = env->FindClass("android/graphics/Bitmap");
        jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
        jfieldID argb8888FieldID = env->GetStaticFieldID(bitmapConfigClass, "ARGB_8888",
                                                         "Landroid/graphics/Bitmap$Config;");
        jobject argb8888Config = env->GetStaticObjectField(bitmapConfigClass, argb8888FieldID);
        jmethodID createBitmapMethodID = env->GetStaticMethodID(bitmapClass, "createBitmap",
                                                                "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

        // Read and convert frames with smart sampling
        AVPacket *packet = av_packet_alloc();
        if (!packet) {
            av_frame_free(&inputFrame);
            av_frame_free(&scaledFrame);
            break;
        }

        int frameCount = 0;
        int frameIndex = 0;

        while (frameCount < maxFrames && av_read_frame(inputFormatContext, packet) >= 0) {
            if (packet->stream_index == videoStreamIndex) {
                if (avcodec_send_packet(decoderContext, packet) == 0) {
                    while (avcodec_receive_frame(decoderContext, inputFrame) == 0) {
                        // Skip frames to maintain reasonable frame rate
                        if (frameIndex % frameSkip != 0) {
                            frameIndex++;
                            continue;
                        }
                        frameIndex++;

                        // Scale frame with high quality
                        sws_scale(swsContext,
                                  inputFrame->data, inputFrame->linesize, 0, inputHeight,
                                  scaledFrame->data, scaledFrame->linesize);

                        // Create Android Bitmap
                        jobject bitmap = env->CallStaticObjectMethod(bitmapClass,
                                                                     createBitmapMethodID,
                                                                     outputWidth, outputHeight,
                                                                     argb8888Config);

                        if (bitmap) {
                            AndroidBitmapInfo bitmapInfo;
                            void *bitmapPixels;

                            if (AndroidBitmap_getInfo(env, bitmap, &bitmapInfo) ==
                                ANDROID_BITMAP_RESULT_SUCCESS &&
                                AndroidBitmap_lockPixels(env, bitmap, &bitmapPixels) ==
                                ANDROID_BITMAP_RESULT_SUCCESS) {

                                // Copy pixel data with R/B channel swap (RGBA to ARGB)
                                uint8_t *src = scaledFrame->data[0];
                                uint32_t *dst = (uint32_t *) bitmapPixels;

                                for (int y = 0; y < outputHeight; y++) {
                                    for (int x = 0; x < outputWidth; x++) {
                                        uint8_t r = src[y * scaledFrame->linesize[0] + x * 4 + 0];
                                        uint8_t g = src[y * scaledFrame->linesize[0] + x * 4 + 1];
                                        uint8_t b = src[y * scaledFrame->linesize[0] + x * 4 + 2];
                                        uint8_t a = src[y * scaledFrame->linesize[0] + x * 4 + 3];

                                        // Swap R and B channels for Android ARGB8888 format
                                        dst[y * outputWidth + x] =
                                                (a << 24) | (b << 16) | (g << 8) | r;
                                    }
                                }

                                AndroidBitmap_unlockPixels(env, bitmap);
                                bitmaps.push_back(env->NewGlobalRef(bitmap));
                            }
                        }

                        frameCount++;
                        if (frameCount >= maxFrames) break;
                    }
                }
            }
            av_packet_unref(packet);
        }

        av_packet_free(&packet);
        av_frame_free(&inputFrame);
        av_frame_free(&scaledFrame);

        // Create result array
        if (!bitmaps.empty()) {
            resultArray = env->NewObjectArray(bitmaps.size(), bitmapClass, nullptr);
            for (size_t i = 0; i < bitmaps.size(); i++) {
                env->SetObjectArrayElement(resultArray, i, bitmaps[i]);
                env->DeleteGlobalRef(bitmaps[i]);
            }
        }

    } while (false);

    // Cleanup
    if (swsContext) {
        sws_freeContext(swsContext);
    }

    if (decoderContext) {
        avcodec_free_context(&decoderContext);
    }

    if (inputFormatContext) {
        avformat_close_input(&inputFormatContext);
    }

    env->ReleaseStringUTFChars(inputPath, input);

    return resultArray;
}

/**
 * Get frame rate from WebM video file
 */
extern "C" JNIEXPORT jdouble JNICALL
Java_xyz_nextalone_nnngram_utils_MessageUtils_getWebmFrameRateNative(
        JNIEnv *env, jclass /*clazz*/, jstring inputPath) {

    const char *input = env->GetStringUTFChars(inputPath, 0);
    double frameRate = 25.0; // Default fallback

    AVFormatContext *inputFormatContext = nullptr;

    do {
        if (avformat_open_input(&inputFormatContext, input, nullptr, nullptr) < 0) {
            break;
        }

        if (avformat_find_stream_info(inputFormatContext, nullptr) < 0) {
            break;
        }

        // Find video stream
        for (unsigned int i = 0; i < inputFormatContext->nb_streams; i++) {
            if (inputFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                AVStream *videoStream = inputFormatContext->streams[i];
                AVRational fr = videoStream->avg_frame_rate;
                if (fr.num == 0 || fr.den == 0) {
                    fr = videoStream->r_frame_rate;
                }
                if (fr.num > 0 && fr.den > 0) {
                    frameRate = (double) fr.num / fr.den;
                }
                break;
            }
        }
    } while (false);

    if (inputFormatContext) {
        avformat_close_input(&inputFormatContext);
    }

    env->ReleaseStringUTFChars(inputPath, input);
    return frameRate;
}
