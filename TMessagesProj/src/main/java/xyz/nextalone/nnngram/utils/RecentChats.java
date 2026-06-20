package xyz.nextalone.nnngram.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseArray;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.SerializedData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RecentChats {

    public static final String CHAT_TYPE = "recent";
    public static final long VIRTUAL_UID = Long.MIN_VALUE + 10;

    private static final int MAX_RECENT_DIALOGS = 50;
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("nekorecentdialogs", Context.MODE_PRIVATE);
    private static final SparseArray<LinkedList<Long>> recentDialogs = new SparseArray<>();

    private RecentChats() {
    }

    public static List<Long> getRecentDialogs(int currentAccount) {
        return new ArrayList<>(getRecentDialogsInternal(currentAccount));
    }

    public static boolean hasRecentDialogs(int currentAccount) {
        return !getRecentDialogsInternal(currentAccount).isEmpty();
    }

    public static boolean isRecentDialog(int currentAccount, long dialogId) {
        return getRecentDialogsInternal(currentAccount).contains(dialogId);
    }

    public static void addRecentDialog(int currentAccount, long dialogId) {
        LinkedList<Long> recentDialog = getRecentDialogsInternal(currentAccount);
        if (!recentDialog.isEmpty() && recentDialog.getFirst() == dialogId) {
            return;
        }
        recentDialog.remove(dialogId);
        recentDialog.addFirst(dialogId);

        while (recentDialog.size() > MAX_RECENT_DIALOGS) {
            recentDialog.removeLast();
        }

        LinkedList<Long> finalRecentDialog = new LinkedList<>(recentDialog);
        Utilities.globalQueue.postRunnable(() -> saveRecentDialogs(currentAccount, finalRecentDialog));
        if (hasRecentFolderEnabled(currentAccount)) {
            reloadDialogs(currentAccount);
        }
    }

    public static void removeRecentDialogs(int currentAccount, List<Long> dialogIds) {
        if (dialogIds == null || dialogIds.isEmpty()) {
            return;
        }
        LinkedList<Long> recentDialog = getRecentDialogsInternal(currentAccount);
        boolean changed = false;
        for (Long dialogId : dialogIds) {
            changed |= recentDialog.remove(dialogId);
        }
        if (!changed) {
            return;
        }
        LinkedList<Long> finalRecentDialog = new LinkedList<>(recentDialog);
        Utilities.globalQueue.postRunnable(() -> saveRecentDialogs(currentAccount, finalRecentDialog));
        reloadDialogs(currentAccount);
    }

    public static void clearRecentDialogs(int currentAccount) {
        getRecentDialogsInternal(currentAccount).clear();
        preferences.edit().putString(recentDialogsKey(currentAccount), "").apply();
        if (hasRecentFolderEnabled(currentAccount)) {
            reloadDialogs(currentAccount);
        }
    }

    public static boolean isRecentFolderEnabled(int currentAccount, int filterId) {
        if (filterId <= 0) {
            return false;
        }
        return getRecentFolderIds(currentAccount).contains(String.valueOf(filterId));
    }

    public static boolean hasRecentFolderEnabled(int currentAccount) {
        return !getRecentFolderIds(currentAccount).isEmpty();
    }

    public static void setRecentFolderEnabled(int currentAccount, int filterId, boolean enabled) {
        if (filterId <= 0) {
            return;
        }
        String id = String.valueOf(filterId);
        Set<String> values = new HashSet<>(getRecentFolderIds(currentAccount));
        boolean changed;
        if (enabled) {
            changed = values.add(id);
        } else {
            changed = values.remove(id);
        }
        if (!changed) {
            return;
        }

        SharedPreferences.Editor editor = preferences.edit();
        if (values.isEmpty()) {
            editor.remove(recentFoldersKey(currentAccount));
        } else {
            editor.putStringSet(recentFoldersKey(currentAccount), values);
        }
        editor.apply();
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated);
        reloadDialogs(currentAccount);
    }

    private static LinkedList<Long> getRecentDialogsInternal(int currentAccount) {
        LinkedList<Long> recentDialog = recentDialogs.get(currentAccount);
        if (recentDialog == null) {
            recentDialog = new LinkedList<>();
            String list = preferences.getString(recentDialogsKey(currentAccount), null);
            if (!TextUtils.isEmpty(list)) {
                byte[] bytes = Base64.decode(list, Base64.NO_WRAP | Base64.NO_PADDING);
                SerializedData data = new SerializedData(bytes);
                int count = data.readInt32(false);
                for (int a = 0; a < count; a++) {
                    recentDialog.add(data.readInt64(false));
                }
                data.cleanup();
            }
            recentDialogs.put(currentAccount, recentDialog);
        }
        return recentDialog;
    }

    private static Set<String> getRecentFolderIds(int currentAccount) {
        Set<String> values = preferences.getStringSet(recentFoldersKey(currentAccount), null);
        if (values == null) {
            return new HashSet<>();
        }
        return new HashSet<>(values);
    }

    private static void saveRecentDialogs(int currentAccount, LinkedList<Long> recentDialog) {
        SerializedData serializedData = new SerializedData();
        serializedData.writeInt32(recentDialog.size());
        for (Long dialog : recentDialog) {
            serializedData.writeInt64(dialog);
        }
        preferences.edit().putString(recentDialogsKey(currentAccount), Base64.encodeToString(serializedData.toByteArray(), Base64.NO_WRAP | Base64.NO_PADDING)).apply();
        serializedData.cleanup();
    }

    private static String recentDialogsKey(int currentAccount) {
        return "recents_" + currentAccount;
    }

    private static String recentFoldersKey(int currentAccount) {
        return "recent_folders_" + currentAccount;
    }

    private static void reloadDialogs(int currentAccount) {
        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
    }
}
