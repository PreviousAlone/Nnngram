/*
 * Copyright (C) 2019-2026 qwq233 <qwq233@qwq2333.top>
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
package xyz.nextalone.nnngram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class QuickSendMediaPopup extends FrameLayout {

    public interface Delegate {
        void onSend(QuickSendMediaEntry entry);

        /** Called when the user explicitly taps the × to dismiss. Not called on auto-dismiss. */
        default void onUserDismiss(QuickSendMediaEntry entry) {}
    }

    public static class QuickSendMediaEntry {
        public long id;
        public String path;
        public Uri uri;
        public long dateAdded;
        public boolean isVideo;
        public int orientation;
    }

    private static final long AUTO_DISMISS_MS = 10_000L;

    private final ImageView thumbView;
    private final ImageView closeButton;
    private final ImageView sendIcon;
    private QuickSendMediaEntry entry;
    private Delegate delegate;
    private boolean dismissed;
    private boolean sending;

    private final Runnable autoDismissRunnable = () -> dismiss(true);

    public QuickSendMediaPopup(Context context) {
        super(context);

        setWillNotDraw(false);
        setClipChildren(false);

        FrameLayout card = new FrameLayout(context);
        card.setBackground(Theme.createRoundRectDrawable(dp(14), Theme.getColor(Theme.key_dialogBackground)));
        card.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(14));
            }
        });
        card.setElevation(dp(6));
        addView(card, LayoutHelper.createFrame(60, 60, Gravity.CENTER, 8, 8, 8, 8));

        thumbView = new ImageView(context);
        thumbView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        thumbView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dp(10));
            }
        });
        thumbView.setClipToOutline(true);
        thumbView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        card.addView(thumbView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER, 4, 4, 4, 4));

        sendIcon = new ImageView(context);
        sendIcon.setImageResource(R.drawable.attach_send);
        sendIcon.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        sendIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sendIcon.setBackground(Theme.createCircleDrawable(dp(22), Theme.getColor(Theme.key_chats_actionBackground)));
        sendIcon.setContentDescription(LocaleController.getString("Send", R.string.Send));
        card.addView(sendIcon, LayoutHelper.createFrame(22, 22, Gravity.BOTTOM | Gravity.RIGHT, 0, 0, -4, -4));

        closeButton = new ImageView(context);
        closeButton.setImageResource(R.drawable.msg_close);
        closeButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.SRC_IN));
        closeButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        closeButton.setBackground(Theme.createCircleDrawable(dp(20), Theme.getColor(Theme.key_dialogBackground)));
        closeButton.setElevation(dp(4));
        closeButton.setContentDescription(LocaleController.getString("Close", R.string.Close));
        addView(closeButton, LayoutHelper.createFrame(20, 20, Gravity.TOP | Gravity.RIGHT, 0, 0, 0, 0));

        card.setOnClickListener(v -> {
            if (sending || entry == null || delegate == null) {
                return;
            }
            sending = true;
            AndroidUtilities.cancelRunOnUIThread(autoDismissRunnable);
            delegate.onSend(entry);
            dismiss(true);
        });
        closeButton.setOnClickListener(v -> {
            if (entry != null && delegate != null) {
                delegate.onUserDismiss(entry);
            }
            dismiss(true);
        });

        setAlpha(0f);
        setScaleX(0.6f);
        setScaleY(0.6f);
        setVisibility(GONE);
    }

    public void show(QuickSendMediaEntry entry, Delegate delegate) {
        this.entry = entry;
        this.delegate = delegate;
        dismissed = false;
        sending = false;
        setVisibility(VISIBLE);
        loadThumb(entry);
        animate().cancel();
        setAlpha(0f);
        setScaleX(0.6f);
        setScaleY(0.6f);
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(260)
            .setInterpolator(new OvershootInterpolator(1.6f))
            .start();

        AndroidUtilities.cancelRunOnUIThread(autoDismissRunnable);
        AndroidUtilities.runOnUIThread(autoDismissRunnable, AUTO_DISMISS_MS);
    }

    public void dismiss(boolean animated) {
        if (dismissed) {
            return;
        }
        dismissed = true;
        AndroidUtilities.cancelRunOnUIThread(autoDismissRunnable);
        animate().cancel();
        if (animated) {
            animate()
                .alpha(0f)
                .scaleX(0.6f)
                .scaleY(0.6f)
                .setDuration(180)
                .withEndAction(() -> setVisibility(GONE))
                .start();
        } else {
            setAlpha(0f);
            setVisibility(GONE);
        }
    }

    public QuickSendMediaEntry getEntry() {
        return entry;
    }

    public boolean isShowingFor(long id) {
        return !dismissed && getVisibility() == VISIBLE && entry != null && entry.id == id;
    }

    private void loadThumb(QuickSendMediaEntry e) {
        thumbView.setImageDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)));
        Utilities.searchQueue.postRunnable(() -> {
            Bitmap bitmap = decodeThumb(e);
            AndroidUtilities.runOnUIThread(() -> {
                if (dismissed || entry == null || entry.id != e.id) {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    return;
                }
                if (bitmap != null) {
                    thumbView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private Bitmap decodeThumb(QuickSendMediaEntry e) {
        try {
            int target = dp(120);
            Bitmap bitmap = null;
            if (e.path != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(e.path, opts);
                int w = opts.outWidth;
                int h = opts.outHeight;
                int sample = 1;
                while (w / sample > target * 2 && h / sample > target * 2) {
                    sample *= 2;
                }
                BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
                decodeOpts.inSampleSize = sample;
                bitmap = BitmapFactory.decodeFile(e.path, decodeOpts);
            }
            if (bitmap == null && e.uri != null) {
                try {
                    bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                        ApplicationLoader.applicationContext.getContentResolver(), e.uri);
                } catch (Throwable ignored) {
                }
            }
            if (bitmap != null && e.orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(e.orientation);
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (rotated != bitmap) {
                    bitmap.recycle();
                }
                bitmap = rotated;
            }
            return bitmap;
        } catch (Throwable t) {
            FileLog.e(t);
            return null;
        }
    }

    /**
     * Queries MediaStore for the newest image added after {@code sinceDateSec} (unix seconds).
     * Runs on a background thread and calls {@code callback} on the UI thread.
     */
    public static void queryLatestImage(long sinceDateSec, java.util.function.Consumer<QuickSendMediaEntry> callback) {
        Utilities.searchQueue.postRunnable(() -> {
            QuickSendMediaEntry result = null;
            Cursor cursor = null;
            try {
                String[] projection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.ORIENTATION
                };
                String selection = MediaStore.Images.Media.DATE_ADDED + " > ?";
                String[] args = new String[]{String.valueOf(sinceDateSec)};
                // LIMIT in sortOrder is unsupported on Android 11+; take first row from ordered cursor instead.
                cursor = ApplicationLoader.applicationContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    args,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
                );
                if (cursor != null && cursor.moveToFirst()) {
                    int idCol = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    int dataCol = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    int dateCol = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
                    int orientCol = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                    QuickSendMediaEntry e = new QuickSendMediaEntry();
                    e.id = idCol >= 0 ? cursor.getLong(idCol) : 0L;
                    e.path = dataCol >= 0 ? cursor.getString(dataCol) : null;
                    e.dateAdded = dateCol >= 0 ? cursor.getLong(dateCol) : 0L;
                    e.orientation = orientCol >= 0 ? cursor.getInt(orientCol) : 0;
                    e.uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, e.id);
                    e.isVideo = false;
                    // DATA column may be null on Android 10+ in some cases; fall back to EXIF via URI later.
                    if (e.orientation == 0 && e.path != null) {
                        try {
                            ExifInterface exif = new ExifInterface(e.path);
                            int o = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            switch (o) {
                                case ExifInterface.ORIENTATION_ROTATE_90: e.orientation = 90; break;
                                case ExifInterface.ORIENTATION_ROTATE_180: e.orientation = 180; break;
                                case ExifInterface.ORIENTATION_ROTATE_270: e.orientation = 270; break;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    if (e.path != null || e.uri != null) {
                        result = e;
                    }
                }
            } catch (Throwable t) {
                FileLog.e(t);
            } finally {
                if (cursor != null) {
                    try { cursor.close(); } catch (Throwable ignored) {}
                }
            }
            final QuickSendMediaEntry finalResult = result;
            AndroidUtilities.runOnUIThread(() -> callback.accept(finalResult));
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AndroidUtilities.cancelRunOnUIThread(autoDismissRunnable);
    }
}
