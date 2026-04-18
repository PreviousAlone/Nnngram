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

import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import xyz.nextalone.gen.Config

/**
 * Keeps "recently conversed" chats visible inside dialog filters that
 * exclude read messages. A dialog is considered recent if its
 * `last_message_date` is within the configured retention window.
 *
 * When enabled, a periodic UI-thread runnable fires `dialogsNeedReload`
 * so that dialogs drop out of the list once their window elapses, even
 * in the absence of any other activity.
 */
object UnreadDialogRetention {

    private var pendingRunnable: Runnable? = null
    @Volatile private var schedulerArmed = false

    @JvmStatic
    fun isEnabled(): Boolean = Config.unreadDialogRetention > 0

    @JvmStatic
    fun getRetentionMillis(): Long = Config.unreadDialogRetention * 1000L

    @JvmStatic
    fun isRecent(dialog: TLRPC.Dialog?): Boolean {
        if (dialog == null) return false
        val retention = getRetentionMillis()
        if (retention <= 0) return false
        val lastSec = dialog.last_message_date
        if (lastSec <= 0) return false
        val ageMillis = System.currentTimeMillis() - lastSec * 1000L
        if (ageMillis !in 0..retention) return false
        ensureSchedulerArmed()
        return true
    }

    private fun ensureSchedulerArmed() {
        if (schedulerArmed) return
        AndroidUtilities.runOnUIThread {
            if (!schedulerArmed && isEnabled()) {
                schedulerArmed = true
                schedule()
            }
        }
    }

    /**
     * Re-schedule the periodic reload based on the current setting, and
     * fire a reload immediately so that the new setting takes effect.
     */
    @JvmStatic
    fun onSettingChanged() {
        cancelPending()
        schedulerArmed = false
        reloadAllAccounts()
        if (isEnabled()) {
            schedulerArmed = true
            schedule()
        }
    }

    private fun schedule() {
        cancelPending()
        if (!isEnabled()) {
            schedulerArmed = false
            return
        }
        val delay = pollIntervalMillis()
        val runnable = Runnable {
            pendingRunnable = null
            if (!isEnabled()) {
                schedulerArmed = false
                return@Runnable
            }
            reloadAllAccounts()
            schedule()
        }
        pendingRunnable = runnable
        AndroidUtilities.runOnUIThread(runnable, delay)
    }

    /**
     * Poll interval: refresh often enough that expiring dialogs disappear
     * in reasonable time, but sparsely for longer retention windows.
     * retention / 10, clamped to [15s, 60s].
     */
    private fun pollIntervalMillis(): Long {
        val retention = getRetentionMillis()
        val scaled = retention / 10
        return scaled.coerceIn(15_000L, 60_000L)
    }

    private fun cancelPending() {
        pendingRunnable?.let { AndroidUtilities.cancelRunOnUIThread(it) }
        pendingRunnable = null
    }

    private fun reloadAllAccounts() {
        for (account in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            if (!UserConfig.getInstance(account).isClientActivated) continue
            NotificationCenter.getInstance(account)
                .postNotificationName(NotificationCenter.dialogsNeedReload)
        }
    }
}
