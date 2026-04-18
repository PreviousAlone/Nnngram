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
import xyz.nextalone.gen.Config
import java.util.concurrent.ConcurrentHashMap

object UnreadDialogRetention {

    private val expireAtById = ConcurrentHashMap<Long, Long>()

    private var pendingExpireRunnable: Runnable? = null

    @JvmStatic
    fun isEnabled(): Boolean = Config.getUnreadDialogRetention() > 0

    @JvmStatic
    fun getRetentionMillis(): Long = Config.getUnreadDialogRetention() * 1000L

    @JvmStatic
    fun onDialogOpened(dialogId: Long) {
        val retentionMillis = getRetentionMillis()
        if (retentionMillis <= 0) {
            return
        }
        expireAtById[dialogId] = System.currentTimeMillis() + retentionMillis
        scheduleNextExpiration()
    }

    @JvmStatic
    fun shouldRetain(dialogId: Long): Boolean {
        if (!isEnabled()) {
            return false
        }
        val expireAt = expireAtById[dialogId] ?: return false
        if (System.currentTimeMillis() >= expireAt) {
            expireAtById.remove(dialogId)
            return false
        }
        return true
    }

    @JvmStatic
    fun clear() {
        expireAtById.clear()
        cancelPending()
    }

    private fun scheduleNextExpiration() {
        AndroidUtilities.runOnUIThread {
            cancelPending()
            val now = System.currentTimeMillis()
            var earliest = Long.MAX_VALUE
            for ((id, expireAt) in expireAtById) {
                if (expireAt <= now) {
                    expireAtById.remove(id)
                } else if (expireAt < earliest) {
                    earliest = expireAt
                }
            }
            if (earliest == Long.MAX_VALUE) {
                return@runOnUIThread
            }
            val delay = (earliest - now).coerceAtLeast(50L)
            val runnable = Runnable {
                pendingExpireRunnable = null
                notifyDialogsReload()
                scheduleNextExpiration()
            }
            pendingExpireRunnable = runnable
            AndroidUtilities.runOnUIThread(runnable, delay)
        }
    }

    private fun cancelPending() {
        pendingExpireRunnable?.let { AndroidUtilities.cancelRunOnUIThread(it) }
        pendingExpireRunnable = null
    }

    private fun notifyDialogsReload() {
        for (account in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            if (!UserConfig.getInstance(account).isClientActivated) {
                continue
            }
            NotificationCenter.getInstance(account)
                .postNotificationName(NotificationCenter.dialogsNeedReload)
        }
    }
}
