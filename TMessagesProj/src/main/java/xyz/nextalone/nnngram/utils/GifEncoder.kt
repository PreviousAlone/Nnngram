/*
 * Copyright (C) 2019-2025 qwq233 <qwq233@qwq2333.top>
 * https://github.com/qwq233/Nullgram
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this software.
 *  If not, see
 * <https://www.gnu.org/licenses/>
 */

package xyz.nextalone.nnngram.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import java.io.IOException
import java.io.OutputStream
import kotlin.math.max

class GifEncoder {
    companion object {
        private const val TAG = "GifEncoder"
    }

    private var width = 0
    private var height = 0
    private var x = 0
    private var y = 0
    private var transparent = -1
    private var transIndex = 0
    private var repeat = -1
    private var delay = 0
    private var started = false
    private var out: OutputStream? = null
    private var image: Bitmap? = null
    private var pixels: IntArray? = null
    private var indexedPixels: ByteArray? = null
    private var colorDepth = 8
    private var colorTab: ByteArray? = null
    private var usedEntry = BooleanArray(256)
    private var palSize = 7
    private var dispose = -1
    private var closeStream = false
    private var firstFrame = true
    private var sizeSet = false
    private var sample = 10
    private var currentFramePlaceholder: Int = 0
    private var currentFrameHasTransparency = false
    private var globalQuantizer: NeuQuant? = null
    private var rgbBuffer: ByteArray? = null

    fun setDelay(ms: Int) {
        delay = maxOf(1, (ms + 5) / 10)
    }

    fun setRepeat(iter: Int) {
        repeat = if (iter >= 0) iter else -1
    }

    fun addFrame(im: Bitmap?): Boolean {
        if (im == null || !started) return false

        return try {
            if (!sizeSet) setSize(im.width, im.height)

            processFrame(im)
            analyzePixels()

            if (firstFrame) {
                writeLSD()
                writePalette()
                if (repeat >= 0) writeNetscapeExt()
            }

            writeGraphicCtrlExt()
            writeImageDesc()
            writePixels()
            firstFrame = false
            true
        } catch (_: IOException) {
            Log.e(TAG, "Error adding frame")
            false
        }
    }

    fun finish(): Boolean {
        if (!started) return false

        return try {
            started = false
            out?.write(0x3b)
            out?.flush()
            if (closeStream) out?.close()
            cleanup()
            true
        } catch (_: IOException) {
            Log.e(TAG, "Error finishing GIF")
            false
        }
    }

    private fun cleanup() {
        transIndex = 0
        out = null
        image?.recycle()
        image = null
        pixels = null
        indexedPixels = null
        colorTab = null
        rgbBuffer = null
        currentFramePlaceholder = 0
        currentFrameHasTransparency = false
        globalQuantizer = null
        closeStream = false
        firstFrame = true
    }

    fun setFrameRate(fps: Float) {
        if (fps != 0f) {
            delay = (100 / fps).toInt()
        }
    }

    fun setQuality(quality: Int) {
        var qual = quality
        if (qual < 1) qual = 1
        sample = qual
    }

    fun setSize(w: Int, h: Int) {
        width = if (w < 1) 320 else w
        height = if (h < 1) 240 else h
        sizeSet = true
    }

    fun setPosition(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun start(os: OutputStream?): Boolean {
        if (os == null) return false
        var ok = true
        closeStream = false
        out = os
        try {
            writeString("GIF89a")
        } catch (_: IOException) {
            ok = false
        }
        started = ok
        return ok
    }

    private fun analyzePixels() {
        val nPix = pixels!!.size
        indexedPixels = ByteArray(nPix)
        var hasTransparent = false
        var transparentPixelCount = 0

        for (i in 0 until nPix) {
            val pixel = pixels!![i]
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha < 128) {
                hasTransparent = true
                transparentPixelCount++
            }
        }

        Log.d(TAG, "Frame analysis: hasTransparent=$hasTransparent, transparentPixels=$transparentPixelCount, totalPixels=$nPix")
        currentFrameHasTransparency = hasTransparent

        val rgbSize = nPix * 3
        if (rgbBuffer == null || rgbBuffer!!.size != rgbSize) rgbBuffer = ByteArray(rgbSize)

        if (hasTransparent) {
            currentFramePlaceholder = findSafePlaceholderColorOptimized()
            Log.d(TAG, "Using placeholder color: 0x${Integer.toHexString(currentFramePlaceholder)}")
        } else {
            transIndex = 0
        }

        convertPixelsToRGB(hasTransparent)

        val quantizer = if (firstFrame) {
            val nq = NeuQuant(rgbBuffer!!, rgbSize, sample)
            globalQuantizer = nq
            colorTab = nq.process()

            for (i in colorTab!!.indices step 3) {
                val temp = colorTab!![i]
                colorTab!![i] = colorTab!![i + 2]
                colorTab!![i + 2] = temp
                usedEntry[i / 3] = false
            }
            Log.d(TAG, "Created global color table for first frame")
            nq
        } else {
            Log.d(TAG, "Reusing global color table")
            globalQuantizer!!
        }

        if (hasTransparent) {
            val r = (currentFramePlaceholder ushr 16) and 0xFF
            val g = (currentFramePlaceholder ushr 8) and 0xFF
            val b = currentFramePlaceholder and 0xFF

            transIndex = quantizer.map(b, g, r)
            Log.d(TAG, "Transparent index set to: $transIndex")

            if (firstFrame) updatePaletteForTransparency(r, g, b)
        }

        mapPixelsToIndices(quantizer, hasTransparent)

        colorDepth = 8
        palSize = 7

        if (transparent != -1 && !hasTransparent) {
            transIndex = findClosest(transparent)
            Log.d(TAG, "Legacy transparent color handling: transIndex=$transIndex")
        }
    }

    private fun convertPixelsToRGB(hasTransparent: Boolean) {
        val placeholderR = if (hasTransparent) (currentFramePlaceholder ushr 16) and 0xFF else 0
        val placeholderG = if (hasTransparent) (currentFramePlaceholder ushr 8) and 0xFF else 0
        val placeholderB = if (hasTransparent) currentFramePlaceholder and 0xFF else 0

        for (i in pixels!!.indices) {
            val pixel = pixels!![i]
            val alpha = (pixel ushr 24) and 0xFF
            val rgbIndex = i * 3

            if (hasTransparent && alpha < 128) {
                rgbBuffer!![rgbIndex] = placeholderB.toByte()
                rgbBuffer!![rgbIndex + 1] = placeholderG.toByte()
                rgbBuffer!![rgbIndex + 2] = placeholderR.toByte()
            } else {
                rgbBuffer!![rgbIndex] = (pixel and 0xFF).toByte()
                rgbBuffer!![rgbIndex + 1] = ((pixel ushr 8) and 0xFF).toByte()
                rgbBuffer!![rgbIndex + 2] = ((pixel ushr 16) and 0xFF).toByte()
            }
        }
    }

    private fun mapPixelsToIndices(quantizer: NeuQuant, hasTransparent: Boolean) {
        var transparentCount = 0

        for (i in pixels!!.indices) {
            val pixel = pixels!![i]
            val alpha = (pixel ushr 24) and 0xFF

            if (hasTransparent && alpha < 128) {
                indexedPixels!![i] = transIndex.toByte()
                transparentCount++
            } else {
                val rgbIndex = i * 3
                val index = quantizer.map(
                    rgbBuffer!![rgbIndex].toInt() and 0xFF,
                    rgbBuffer!![rgbIndex + 1].toInt() and 0xFF,
                    rgbBuffer!![rgbIndex + 2].toInt() and 0xFF
                )
                usedEntry[index] = true
                indexedPixels!![i] = index.toByte()
            }
        }

        Log.d(TAG, "Mapped $transparentCount transparent pixels to index $transIndex")
    }

    private fun updatePaletteForTransparency(r: Int, g: Int, b: Int) {
        val paletteIndex = transIndex * 3
        if (paletteIndex + 2 < colorTab!!.size) {
            colorTab!![paletteIndex] = r.toByte()
            colorTab!![paletteIndex + 1] = g.toByte()
            colorTab!![paletteIndex + 2] = b.toByte()
            Log.d(TAG, "Set palette entry $transIndex to transparent color [$r, $g, $b]")
        }
    }

    private fun findClosest(c: Int): Int {
        if (colorTab == null) return -1
        val r = (c shr 16) and 0xff
        val g = (c shr 8) and 0xff
        val b = c and 0xff
        var minpos = 0
        var dmin = 256 * 256 * 256
        val len = colorTab!!.size
        var i = 0
        while (i < len) {
            val dr = r - (colorTab!![i++].toInt() and 0xff)
            val dg = g - (colorTab!![i++].toInt() and 0xff)
            val db = b - (colorTab!![i].toInt() and 0xff)
            val d = dr * dr + dg * dg + db * db
            val index = i / 3
            if (usedEntry[index] && d < dmin) {
                dmin = d
                minpos = index
            }
            i++
        }
        return minpos
    }

    private fun processFrame(bitmap: Bitmap) {
        val w = bitmap.width
        val h = bitmap.height

        val processedBitmap = if (w != width || h != height) {
            val temp = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(temp)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            temp
        } else {
            bitmap
        }

        val pixelCount = width * height
        if (pixels == null || pixels!!.size != pixelCount) {
            pixels = IntArray(pixelCount)
        }

        processedBitmap.getPixels(pixels!!, 0, width, 0, 0, width, height)
        if (processedBitmap !== bitmap) processedBitmap.recycle()
    }

    private fun writeGraphicCtrlExt() {
        val out = this.out ?: return

        out.write(0x21)
        out.write(0xf9)
        out.write(4)

        val (transp, disp) = if (currentFrameHasTransparency) {
            Log.d(TAG, "Setting transparency flag: transIndex=$transIndex")
            1 to 2
        } else {
            Log.d(TAG, "No transparency flag set")
            0 to 1
        }

        val finalDisp = if (dispose >= 0) (dispose and 7) else disp
        out.write((finalDisp shl 2) or transp)
        writeShort(delay)
        out.write(if (transp == 1) transIndex else 0)
        out.write(0)

        Log.d(
            TAG,
            "Graphic Control Extension: transp=$transp, disp=$finalDisp, delay=$delay, transIndex=${if (transp == 1) transIndex else 0}"
        )
    }

    private fun writeImageDesc() {
        out!!.write(0x2c)
        writeShort(x)
        writeShort(y)
        writeShort(width)
        writeShort(height)
        out!!.write(0)
        Log.d("GifEncoder", "Written Image Descriptor: pos=($x,$y), size=($width,$height), using global color table")
    }

    private fun writeLSD() {
        writeShort(width)
        writeShort(height)
        out!!.write(0x80 or 0x70 or 0x00 or palSize)
        out!!.write(0)
        out!!.write(0)
        Log.d("GifEncoder", "Written Logic Screen Descriptor with background color 0")
    }

    private fun writeNetscapeExt() {
        out!!.write(0x21)
        out!!.write(0xff)
        out!!.write(11)
        writeString("NETSCAPE2.0")
        out!!.write(3)
        out!!.write(1)
        writeShort(repeat)
        out!!.write(0)
    }

    private fun writePalette() {
        out!!.write(colorTab!!, 0, colorTab!!.size)
        val n = 3 * 256 - colorTab!!.size
        for (i in 0 until n) {
            out!!.write(0)
        }
    }

    private fun writePixels() {
        val indexedPixels = this.indexedPixels ?: return
        Log.d(TAG, "Writing pixels: ${indexedPixels.size} indexed pixels, colorDepth=$colorDepth")
        val encoder = LZWEncoder(width, height, indexedPixels, colorDepth)
        encoder.encode(out!!)
        Log.d(TAG, "Finished writing pixels for frame")
    }

    private fun writeShort(value: Int) {
        out?.apply {
            write(value and 0xff)
            write((value ushr 8) and 0xff)
        }
    }

    private fun writeString(s: String) {
        out?.apply {
            s.forEach { char ->
                write(char.code)
            }
        }
    }

    private class NeuQuant(
        private val thepicture: ByteArray, private val lengthcount: Int, private var samplefac: Int
    ) {
        companion object {
            const val NETSIZE = 256
            const val PRIME1 = 499
            const val PRIME2 = 491
            const val PRIME3 = 487
            const val PRIME4 = 503
            const val MINPICTUREBYTES = 3 * PRIME4
            const val MAXNETPOS = NETSIZE - 1
            const val NETBIASSHIFT = 4
            const val NCYCLES = 100
            const val INTBIASSHIFT = 16
            const val INTBIAS = 1 shl INTBIASSHIFT
            const val GAMMASHIFT = 10
            const val BETASHIFT = 10
            const val BETA = INTBIAS shr BETASHIFT
            const val BETAGAMMA = INTBIAS shl (GAMMASHIFT - BETASHIFT)
            const val INITRAD = NETSIZE shr 3
            const val RADIUSBIASSHIFT = 6
            const val RADIUSBIAS = 1 shl RADIUSBIASSHIFT
            const val INITRADIUS = INITRAD * RADIUSBIAS
            const val RADIUSDEC = 30
            const val ALPHABIASSHIFT = 10
            const val INITALPHA = 1 shl ALPHABIASSHIFT
            const val RADBIASSHIFT = 8
            const val RADBIAS = 1 shl RADBIASSHIFT
            const val ALPHARADBSHIFT = ALPHABIASSHIFT + RADBIASSHIFT
            const val ALPHARADBIAS = 1 shl ALPHARADBSHIFT
        }

        private var alphadec = 0
        private val network = Array(NETSIZE) { IntArray(4) }
        private val netindex = IntArray(256)
        private val bias = IntArray(NETSIZE)
        private val freq = IntArray(NETSIZE)
        private val radpower = IntArray(INITRAD)

        init {
            for (i in 0 until NETSIZE) {
                val p = network[i]
                val value = (i shl (NETBIASSHIFT + 8)) / NETSIZE
                p[0] = value
                p[1] = value
                p[2] = value
                p[3] = i
                freq[i] = INTBIAS / NETSIZE
                bias[i] = 0
            }
        }

        fun colorMap(): ByteArray {
            val map = ByteArray(3 * NETSIZE)
            val index = IntArray(NETSIZE)
            for (i in 0 until NETSIZE) {
                index[network[i][3]] = i
            }
            var k = 0
            for (i in 0 until NETSIZE) {
                val j = index[i]
                map[k++] = network[j][0].toByte()
                map[k++] = network[j][1].toByte()
                map[k++] = network[j][2].toByte()
            }
            return map
        }

        fun inxbuild() {
            var previouscol = 0
            var startpos = 0
            for (i in 0 until NETSIZE) {
                val p = network[i]
                var smallpos = i
                var smallval = p[1]
                for (j in i + 1 until NETSIZE) {
                    val q = network[j]
                    if (q[1] < smallval) {
                        smallpos = j
                        smallval = q[1]
                    }
                }
                val q = network[smallpos]
                if (i != smallpos) {
                    var j = q[0]; q[0] = p[0]; p[0] = j
                    j = q[1]; q[1] = p[1]; p[1] = j
                    j = q[2]; q[2] = p[2]; p[2] = j
                    j = q[3]; q[3] = p[3]; p[3] = j
                }
                if (smallval != previouscol) {
                    netindex[previouscol] = (startpos + i) shr 1
                    for (j in previouscol + 1 until smallval) {
                        netindex[j] = i
                    }
                    previouscol = smallval
                    startpos = i
                }
            }
            netindex[previouscol] = (startpos + MAXNETPOS) shr 1
            for (j in previouscol + 1 until 256) {
                netindex[j] = MAXNETPOS
            }
        }

        fun learn() {
            if (lengthcount < MINPICTUREBYTES) samplefac = 1
            alphadec = 30 + (samplefac - 1) / 3
            val p = thepicture
            var pix = 0
            val lim = lengthcount
            val samplepixels = lengthcount / (3 * samplefac)
            var delta = samplepixels / NCYCLES
            var alpha = INITALPHA
            var radius = INITRADIUS

            var rad = radius shr RADIUSBIASSHIFT
            for (i in 0 until rad) {
                radpower[i] = alpha * ((rad * rad - i * i) * RADBIAS / (rad * rad))
            }

            val step = if (lengthcount < MINPICTUREBYTES) {
                3
            } else if (lengthcount % PRIME1 != 0) {
                3 * PRIME1
            } else if (lengthcount % PRIME2 != 0) {
                3 * PRIME2
            } else if (lengthcount % PRIME3 != 0) {
                3 * PRIME3
            } else {
                3 * PRIME4
            }

            var i = 0
            while (i < samplepixels) {
                val b = (p[pix].toInt() and 0xff) shl NETBIASSHIFT
                val g = (p[pix + 1].toInt() and 0xff) shl NETBIASSHIFT
                val r = (p[pix + 2].toInt() and 0xff) shl NETBIASSHIFT
                val j = contest(b, g, r)

                altersingle(alpha, j, b, g, r)
                if (rad != 0) alterneigh(rad, j, b, g, r)

                pix += step
                if (pix >= lim) pix -= lengthcount

                i++
                if (delta == 0) delta = 1
                if (i % delta == 0) {
                    alpha -= alpha / alphadec
                    radius -= radius / RADIUSDEC
                    rad = radius shr RADIUSBIASSHIFT
                    if (rad <= 1) rad = 0
                    for (j2 in 0 until rad) {
                        radpower[j2] = alpha * ((rad * rad - j2 * j2) * RADBIAS / (rad * rad))
                    }
                }
            }
        }

        fun map(b: Int, g: Int, r: Int): Int {
            var bestd = 1000
            var best = -1
            var i = netindex[g]
            var j = i - 1

            while (i < NETSIZE || j >= 0) {
                if (i < NETSIZE) {
                    val p = network[i]
                    var dist = p[1] - g
                    if (dist >= bestd) {
                        i = NETSIZE
                    } else {
                        i++
                        if (dist < 0) dist = -dist
                        var a = p[0] - b
                        if (a < 0) a = -a
                        dist += a
                        if (dist < bestd) {
                            a = p[2] - r
                            if (a < 0) a = -a
                            dist += a
                            if (dist < bestd) {
                                bestd = dist
                                best = p[3]
                            }
                        }
                    }
                }
                if (j >= 0) {
                    val p = network[j]
                    var dist = g - p[1]
                    if (dist >= bestd) {
                        j = -1
                    } else {
                        j--
                        if (dist < 0) dist = -dist
                        var a = p[0] - b
                        if (a < 0) a = -a
                        dist += a
                        if (dist < bestd) {
                            a = p[2] - r
                            if (a < 0) a = -a
                            dist += a
                            if (dist < bestd) {
                                bestd = dist
                                best = p[3]
                            }
                        }
                    }
                }
            }
            return best
        }

        fun process(): ByteArray {
            learn()
            unbiasnet()
            inxbuild()
            return colorMap()
        }

        fun unbiasnet() {
            for (i in 0 until NETSIZE) {
                network[i][0] = network[i][0] shr NETBIASSHIFT
                network[i][1] = network[i][1] shr NETBIASSHIFT
                network[i][2] = network[i][2] shr NETBIASSHIFT
                network[i][3] = i
            }
        }

        private fun alterneigh(rad: Int, i: Int, b: Int, g: Int, r: Int) {
            var lo = i - rad
            if (lo < -1) lo = -1
            var hi = i + rad
            if (hi > NETSIZE) hi = NETSIZE

            var j = i + 1
            var k = i - 1
            var m = 1
            while (j < hi || k > lo) {
                val a = radpower[m++]
                if (j < hi) {
                    val p = network[j++]
                    try {
                        p[0] -= a * (p[0] - b) / ALPHARADBIAS
                        p[1] -= a * (p[1] - g) / ALPHARADBIAS
                        p[2] -= a * (p[2] - r) / ALPHARADBIAS
                    } catch (_: Exception) {
                    }
                }
                if (k > lo) {
                    val p = network[k--]
                    try {
                        p[0] -= a * (p[0] - b) / ALPHARADBIAS
                        p[1] -= a * (p[1] - g) / ALPHARADBIAS
                        p[2] -= a * (p[2] - r) / ALPHARADBIAS
                    } catch (_: Exception) {
                    }
                }
            }
        }

        private fun altersingle(alpha: Int, i: Int, b: Int, g: Int, r: Int) {
            val n = network[i]
            n[0] -= alpha * (n[0] - b) / INITALPHA
            n[1] -= alpha * (n[1] - g) / INITALPHA
            n[2] -= alpha * (n[2] - r) / INITALPHA
        }

        private fun contest(b: Int, g: Int, r: Int): Int {
            var bestd = Int.MAX_VALUE
            var bestbiasd = bestd
            var bestpos = -1
            var bestbiaspos = bestpos

            for (i in 0 until NETSIZE) {
                val n = network[i]
                var dist = n[0] - b
                if (dist < 0) dist = -dist
                var a = n[1] - g
                if (a < 0) a = -a
                dist += a
                a = n[2] - r
                if (a < 0) a = -a
                dist += a
                if (dist < bestd) {
                    bestd = dist
                    bestpos = i
                }
                val biasdist = dist - (bias[i] shr (INTBIASSHIFT - NETBIASSHIFT))
                if (biasdist < bestbiasd) {
                    bestbiasd = biasdist
                    bestbiaspos = i
                }
                val betafreq = freq[i] shr BETASHIFT
                freq[i] -= betafreq
                bias[i] += betafreq shl GAMMASHIFT
            }
            freq[bestpos] += BETA
            bias[bestpos] -= BETAGAMMA
            return bestbiaspos
        }
    }

    private class LZWEncoder(
        private val imgW: Int, private val imgH: Int, private val pixAry: ByteArray, private val initCodeSize: Int
    ) {
        companion object {
            const val EOF = -1
            const val BITS = 12
            const val HSIZE = 5003
        }

        private var nBits = 0
        private val maxbits = BITS
        private var maxcode = 0
        private val maxmaxcode = 1 shl BITS
        private val htab = IntArray(HSIZE)
        private val codetab = IntArray(HSIZE)
        private val hsize = HSIZE
        private var freeEnt = 0
        private var clearFlg = false
        private var gInitBits = 0
        private var clearCode = 0
        private var eofCode = 0
        private var curAccum = 0
        private var curBits = 0

        private val masks = intArrayOf(
            0x0000, 0x0001, 0x0003, 0x0007, 0x000F, 0x001F, 0x003F, 0x007F, 0x00FF,
            0x01FF, 0x03FF, 0x07FF, 0x0FFF, 0x1FFF, 0x3FFF, 0x7FFF, 0xFFFF
        )

        private var aCount = 0
        private val accum = ByteArray(256)
        private var remaining = 0
        private var curPixel = 0

        fun encode(os: OutputStream) {
            val codeSize = max(2, initCodeSize)
            os.write(codeSize)
            remaining = imgW * imgH
            curPixel = 0
            compress(codeSize + 1, os)
            os.write(0)
        }

        private fun compress(initBits: Int, outs: OutputStream) {
            gInitBits = initBits
            clearFlg = false
            nBits = gInitBits
            maxcode = maxcode(nBits)
            clearCode = 1 shl (initBits - 1)
            eofCode = clearCode + 1
            freeEnt = clearCode + 2
            aCount = 0
            var ent = nextPixel()
            var hshift = 0
            var fcode = hsize
            while (fcode < 65536) {
                ++hshift
                fcode *= 2
            }
            hshift = 8 - hshift
            val hsizeReg = hsize
            clHash(hsizeReg)
            output(clearCode, outs)

            outerLoop@ while (true) {
                val c = nextPixel()
                if (c == EOF) break
                fcode = (c shl maxbits) + ent
                var i = (c shl hshift) xor ent
                if (htab[i] == fcode) {
                    ent = codetab[i]
                    continue
                } else if (htab[i] >= 0) {
                    var disp = hsizeReg - i
                    if (i == 0) disp = 1
                    do {
                        i -= disp
                        if (i < 0) i += hsizeReg
                        if (htab[i] == fcode) {
                            ent = codetab[i]
                            continue@outerLoop
                        }
                    } while (htab[i] >= 0)
                }
                output(ent, outs)
                ent = c
                if (freeEnt < maxmaxcode) {
                    codetab[i] = freeEnt++
                    htab[i] = fcode
                } else {
                    clBlock(outs)
                }
            }
            output(ent, outs)
            output(eofCode, outs)
        }

        private fun output(code: Int, outs: OutputStream) {
            curAccum = curAccum and masks[curBits]
            curAccum = if (curBits > 0) curAccum or (code shl curBits) else code
            curBits += nBits
            while (curBits >= 8) {
                charOut((curAccum and 0xff).toByte(), outs)
                curAccum = curAccum shr 8
                curBits -= 8
            }
            if (freeEnt > maxcode || clearFlg) {
                if (clearFlg) {
                    maxcode = maxcode(gInitBits.also { nBits = it })
                    clearFlg = false
                } else {
                    ++nBits
                    maxcode = if (nBits == maxbits) maxmaxcode else maxcode(nBits)
                }
            }
            if (code == eofCode) {
                while (curBits > 0) {
                    charOut((curAccum and 0xff).toByte(), outs)
                    curAccum = curAccum shr 8
                    curBits -= 8
                }
                flushChar(outs)
            }
        }

        private fun charOut(c: Byte, outs: OutputStream) {
            accum[aCount++] = c
            if (aCount >= 254) flushChar(outs)
        }

        private fun flushChar(outs: OutputStream) {
            if (aCount > 0) {
                outs.write(aCount)
                outs.write(accum, 0, aCount)
                aCount = 0
            }
        }

        private fun clBlock(outs: OutputStream) {
            clHash(hsize)
            freeEnt = clearCode + 2
            clearFlg = true
            output(clearCode, outs)
        }

        private fun clHash(hsize: Int) {
            for (i in 0 until hsize) {
                htab[i] = -1
            }
        }

        private fun nextPixel(): Int {
            if (remaining == 0) return EOF
            --remaining
            val pix = pixAry[curPixel++]
            return pix.toInt() and 0xff
        }

        private fun maxcode(nBits: Int): Int = (1 shl nBits) - 1
    }

    private fun findSafePlaceholderColorOptimized(): Int {
        if (pixels == null) return 0xFF00FF

        val existingColors = HashSet<Int>(pixels!!.size / 4)

        for (pixel in pixels!!) {
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha >= 128) {
                val rgb = pixel and 0xFFFFFF
                existingColors.add(rgb)
            }
        }

        val priorityColors = intArrayOf(
            0xFF00FF, 0x00FFFF, 0xFFFF00, 0x010101, 0xFEFEFE, 0xFF0000, 0x00FF00, 0x0000FF
        )

        for (color in priorityColors) {
            if (!existingColors.contains(color)) {
                Log.d(TAG, "Using priority placeholder color: 0x${Integer.toHexString(color)}")
                return color
            }
        }

        for (r in 1..255) {
            for (g in 0..255) {
                for (b in 0..255) {
                    val color = (r shl 16) or (g shl 8) or b
                    if (!existingColors.contains(color)) {
                        Log.d(TAG, "Found safe placeholder color: 0x${Integer.toHexString(color)}")
                        return color
                    }
                }
            }
        }

        Log.w(TAG, "Using fallback placeholder color: 0xFF00FF")
        return 0xFF00FF
    }
}
