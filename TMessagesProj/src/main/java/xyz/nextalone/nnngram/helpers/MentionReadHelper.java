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
package xyz.nextalone.nnngram.helpers;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.Vector;

import java.util.HashSet;

public class MentionReadHelper {

    public static boolean isEligible(MessageObject msg, TLRPC.Chat chat, TLRPC.ChatFull chatInfo) {
        if (msg == null || chat == null) {
            return false;
        }
        if (!msg.isOutOwner() || msg.isUnread()) {
            return false;
        }
        if (msg.getMentionedUserIds().isEmpty()) {
            return false;
        }
        int currentAccount = msg.currentAccount;
        int timeDiff = ConnectionsManager.getInstance(currentAccount).getCurrentTime() - msg.messageOwner.date;
        int expirePeriod = MessagesController.getInstance(currentAccount).chatReadMarkExpirePeriod;
        if (timeDiff >= expirePeriod) {
            return false;
        }
        if (!ChatObject.isMegagroup(chat) && ChatObject.isChannel(chat)) {
            return false;
        }
        if (chatInfo != null && chatInfo.participants_count > MessagesController.getInstance(currentAccount).chatReadMarkSizeThreshold) {
            return false;
        }
        return true;
    }

    public static void fetchReadParticipants(MessageObject msg, int currentAccount) {
        if (msg == null || msg.mentionReadParticipantsLoading) {
            return;
        }
        msg.mentionReadParticipantsLoading = true;

        TLRPC.TL_messages_getMessageReadParticipants req = new TLRPC.TL_messages_getMessageReadParticipants();
        req.msg_id = msg.getId();
        req.peer = MessagesController.getInstance(currentAccount).getInputPeer(msg.getDialogId());

        long dialogId = msg.getDialogId();
        int msgId = msg.getId();

        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            msg.mentionReadParticipantsLoading = false;
            msg.mentionReadParticipantsFetched = true;

            if (error != null) {
                return;
            }
            if (!(response instanceof Vector)) {
                return;
            }

            Vector vector = (Vector) response;
            HashSet<Long> readUsers = new HashSet<>();
            for (int i = 0, n = vector.objects.size(); i < n; i++) {
                Object object = vector.objects.get(i);
                if (object instanceof TLRPC.TL_readParticipantDate) {
                    readUsers.add(((TLRPC.TL_readParticipantDate) object).user_id);
                } else if (object instanceof Long) {
                    long peerId = (Long) object;
                    if (peerId > 0) {
                        readUsers.add(peerId);
                    }
                }
            }
            msg.mentionReadParticipants = readUsers;

            NotificationCenter.getInstance(currentAccount).postNotificationName(
                    NotificationCenter.mentionReadParticipantsLoaded, dialogId, msgId);
        }));
    }

    public static void fetchReadParticipantsFromCell(MessageObject msg, int currentAccount) {
        if (msg == null || msg.mentionReadParticipantsLoading || msg.mentionReadParticipantsFetched) {
            return;
        }
        fetchReadParticipants(msg, currentAccount);
    }
}
