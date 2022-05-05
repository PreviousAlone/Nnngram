package xyz.nextalone.nnngram.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.MotionEvent
import android.widget.LinearLayout
import org.telegram.messenger.*
import org.telegram.tgnet.TLRPC.*
import org.telegram.ui.ActionBar.ActionBarLayout
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.ChatMessageCell
import org.telegram.ui.Cells.ChatMessageCell.ChatMessageCellDelegate
import org.telegram.ui.Components.BackgroundGradientDrawable
import org.telegram.ui.Components.BackgroundGradientDrawable.Disposable
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.MotionBackgroundDrawable

@SuppressLint("ViewConstructor")
class StickerSizePreviewMessagesCell(context: Context?, private val parentLayout: ActionBarLayout) : LinearLayout(context) {
    private var backgroundGradientDisposable: Disposable? = null
    private var oldBackgroundGradientDisposable: Disposable? = null
    private var currentBackgroundDrawable: Drawable? = null
    private var oldBackgroundDrawable: Drawable? = null
    private val cells = arrayOfNulls<ChatMessageCell>(2)
    private val messageObjects = arrayOfNulls<MessageObject>(2)
    private val shadowDrawable: Drawable

    init {
        setWillNotDraw(false)
        orientation = VERTICAL
        setPadding(0, AndroidUtilities.dp(11f), 0, AndroidUtilities.dp(11f))
        shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow)
        val date = (System.currentTimeMillis() / 1000).toInt() - 60 * 60
        var message = TL_message()
        message.date = date + 10
        message.dialog_id = 1
        message.flags = 257
        message.from_id = TL_peerUser()
        message.from_id.user_id = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()
        message.id = 1
        message.media = TL_messageMediaDocument()
        message.media.flags = 1
        message.media.document = TL_document()
        message.media.document.mime_type = "image/webp"
        message.media.document.file_reference = ByteArray(0)
        message.media.document.access_hash = 0
        message.media.document.date = date
        val attributeSticker = TL_documentAttributeSticker()
        attributeSticker.alt = "🐈‍⬛"
        message.media.document.attributes.add(attributeSticker)
        val attributeImageSize = TL_documentAttributeImageSize()
        attributeImageSize.h = 512
        attributeImageSize.w = 512
        message.media.document.attributes.add(attributeImageSize)
        message.message = ""
        message.out = true
        message.peer_id = TL_peerUser()
        message.peer_id.user_id = 0
        messageObjects[0] = MessageObject(UserConfig.selectedAccount, message, true, false)
        messageObjects[0]!!.useCustomPhoto = true
        message = TL_message()
        message.message = LocaleController.getString("StickerSizeDialogMessageReplyTo", R.string.StickerSizeDialogMessageReplyTo)
        message.date = date + 1270
        message.dialog_id = -1
        message.flags = 259
        message.id = 2
        message.media = TL_messageMediaEmpty()
        message.out = false
        message.peer_id = TL_peerUser()
        message.peer_id.user_id = 1
        messageObjects[0]!!.customReplyName = "James Clef"
        messageObjects[0]!!.replyMessageObject = MessageObject(UserConfig.selectedAccount, message, true, false)
        message = TL_message()
        message.message = LocaleController.getString("StickerSizeDialogMessage", R.string.StickerSizeDialogMessage)
        message.date = date + 1270
        message.dialog_id = -1
        message.flags = 259
        message.id = 2
        message.media = TL_messageMediaEmpty()
        message.out = false
        message.peer_id = TL_peerUser()
        message.peer_id.user_id = 1
        messageObjects[1] = MessageObject(UserConfig.selectedAccount, message, true, false)
        val currentUser = MessagesController.getInstance(UserConfig.selectedAccount).getUser(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId())
        messageObjects[1]!!.customReplyName = ContactsController.formatName(currentUser.first_name, currentUser.last_name)
        messageObjects[1]!!.replyMessageObject = messageObjects[0]
        for (a in cells.indices) {
            cells[a] = ChatMessageCell(context)
            cells[a]!!.delegate = object : ChatMessageCellDelegate {}
            cells[a]!!.isChat = false
            cells[a]!!.setFullyDraw(true)
            cells[a]!!.setMessageObject(messageObjects[a], null, false, false)
            addView(cells[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT))
        }
    }

    override fun invalidate() {
        super.invalidate()
        for (a in cells.indices) {
            cells[a]!!.setMessageObject(messageObjects[a], null, false, false)
            cells[a]!!.invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val newDrawable = Theme.getCachedWallpaperNonBlocking()
        if (newDrawable !== currentBackgroundDrawable && newDrawable != null) {
            if (Theme.isAnimatingColor()) {
                oldBackgroundDrawable = currentBackgroundDrawable
                oldBackgroundGradientDisposable = backgroundGradientDisposable
            } else if (backgroundGradientDisposable != null) {
                backgroundGradientDisposable!!.dispose()
                backgroundGradientDisposable = null
            }
            currentBackgroundDrawable = newDrawable
        }
        val themeAnimationValue = parentLayout.themeAnimationValue
        for (a in 0..1) {
            val drawable = (if (a == 0) oldBackgroundDrawable else currentBackgroundDrawable)
                ?: continue
            if (a == 1 && oldBackgroundDrawable != null) {
                drawable.alpha = (255 * themeAnimationValue).toInt()
            } else {
                drawable.alpha = 255
            }
            if (drawable is ColorDrawable || drawable is GradientDrawable || drawable is MotionBackgroundDrawable) {
                drawable.setBounds(0, 0, measuredWidth, measuredHeight)
                if (drawable is BackgroundGradientDrawable) {
                    backgroundGradientDisposable = drawable.drawExactBoundsSize(canvas, this)
                } else {
                    drawable.draw(canvas)
                }
            } else if (drawable is BitmapDrawable) {
                if (drawable.tileModeX == Shader.TileMode.REPEAT) {
                    canvas.save()
                    val scale = 2.0f / AndroidUtilities.density
                    canvas.scale(scale, scale)
                    drawable.setBounds(0, 0, Math.ceil((measuredWidth / scale).toDouble()).toInt(), Math.ceil((measuredHeight / scale).toDouble()).toInt())
                } else {
                    val viewHeight = measuredHeight
                    val scaleX = measuredWidth.toFloat() / drawable.getIntrinsicWidth().toFloat()
                    val scaleY = viewHeight.toFloat() / drawable.getIntrinsicHeight().toFloat()
                    val scale = Math.max(scaleX, scaleY)
                    val width = Math.ceil((drawable.getIntrinsicWidth() * scale).toDouble()).toInt()
                    val height = Math.ceil((drawable.getIntrinsicHeight() * scale).toDouble()).toInt()
                    val x = (measuredWidth - width) / 2
                    val y = (viewHeight - height) / 2
                    canvas.save()
                    canvas.clipRect(0, 0, width, measuredHeight)
                    drawable.setBounds(x, y, x + width, y + height)
                }
                drawable.draw(canvas)
                canvas.restore()
            }
            if (a == 0 && oldBackgroundDrawable != null && themeAnimationValue >= 1.0f) {
                if (oldBackgroundGradientDisposable != null) {
                    oldBackgroundGradientDisposable!!.dispose()
                    oldBackgroundGradientDisposable = null
                }
                oldBackgroundDrawable = null
                invalidate()
            }
        }
        shadowDrawable.setBounds(0, 0, measuredWidth, measuredHeight)
        shadowDrawable.draw(canvas)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (backgroundGradientDisposable != null) {
            backgroundGradientDisposable!!.dispose()
            backgroundGradientDisposable = null
        }
        if (oldBackgroundGradientDisposable != null) {
            oldBackgroundGradientDisposable!!.dispose()
            oldBackgroundGradientDisposable = null
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return false
    }

    override fun dispatchSetPressed(pressed: Boolean) {}

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }
}
