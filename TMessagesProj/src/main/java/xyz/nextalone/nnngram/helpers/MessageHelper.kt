package xyz.nextalone.nnngram.helpers

import androidx.recyclerview.widget.GridLayoutManagerFixed
import org.telegram.messenger.MessageObject
import org.telegram.ui.Cells.ChatActionCell
import org.telegram.ui.Cells.ChatMessageCell
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.RecyclerListView

object MessageHelper {

    fun getFirstVisibleMessage(
        chatLayoutManager: GridLayoutManagerFixed,
        chatListView: RecyclerListView,
        chatAdapter: ChatActivity.ChatActivityAdapter,
        messages: MutableList<MessageObject>
    ): Int {
        var messageId =
            0
        val position: Int =
            chatLayoutManager.findFirstVisibleItemPosition()
        if (position != 0) {
            var holder =
                chatListView.findViewHolderForAdapterPosition(
                    position
                )
            if (holder != null) {
                var mid =
                    0
                if (holder.itemView is ChatMessageCell) {
                    mid =
                        (holder.itemView as ChatMessageCell).messageObject.id
                } else if (holder.itemView is ChatActionCell) {
                    mid =
                        (holder.itemView as ChatActionCell).messageObject.id
                }
                if (mid == 0) {
                    holder =
                        chatListView.findViewHolderForAdapterPosition(
                            position + 1
                        )
                }
                var ignore =
                    false
                var count =
                    0
                for (a in position - 1 downTo chatAdapter.messagesStartRow) {
                    val num: Int =
                        a - chatAdapter.messagesStartRow
                    if (num < 0 || num >= messages.size) {
                        continue
                    }
                    val messageObject: MessageObject =
                        messages.get(
                            num
                        )
                    if (messageObject.id == 0) {
                        continue
                    }
                    if ((!messageObject.isOut || messageObject.messageOwner.from_scheduled) && messageObject.isUnread) {
                        ignore =
                            true
                        messageId =
                            0
                    }
                    if (count > 2) {
                        break
                    }
                    count++
                }
                if (holder != null && !ignore) {
                    if (holder.itemView is ChatMessageCell) {
                        messageId =
                            (holder.itemView as ChatMessageCell).messageObject.id
                    } else if (holder.itemView is ChatActionCell) {
                        messageId =
                            (holder.itemView as ChatActionCell).messageObject.id
                    } else {
                        messageId =
                            0
                    }
                }
            }
        }
        return messageId
    }
}
