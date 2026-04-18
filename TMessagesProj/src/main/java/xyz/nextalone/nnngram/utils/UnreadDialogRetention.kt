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

    private val expireAtByAccount = ConcurrentHashMap<Int, ConcurrentHashMap<Long, Long>>()

    private var pendingExpireRunnable: Runnable? = null

    @JvmStatic
    fun isEnabled(): Boolean = Config.unreadDialogRetention > 0

    @JvmStatic
    fun getRetentionMillis(): Long = Config.unreadDialogRetention * 1000L

    @JvmStatic
    fun onDialogOpened(account: Int, dialogId: Long) {
        val retentionMillis = getRetentionMillis()
        if (retentionMillis <= 0) {
            return
        }
        mapFor(account)[dialogId] = System.currentTimeMillis() + retentionMillis
        scheduleNextExpiration()
    }

    @JvmStatic
    fun shouldRetain(account: Int, dialogId: Long): Boolean {
        if (!isEnabled()) {
            return false
        }
        val map = expireAtByAccount[account] ?: return false
        val expireAt = map[dialogId] ?: return false
        if (System.currentTimeMillis() >= expireAt) {
            map.remove(dialogId)
            return false
        }
        return true
    }

    @JvmStatic
    fun clearAndReload() {
        val accounts = expireAtByAccount.keys.toList()
        expireAtByAccount.clear()
        cancelPending()
        for (account in accounts) {
            notifyDialogsReload(account)
        }
    }

    private fun mapFor(account: Int): ConcurrentHashMap<Long, Long> =
        expireAtByAccount.computeIfAbsent(account) { ConcurrentHashMap() }

    private fun scheduleNextExpiration() {
        AndroidUtilities.runOnUIThread {
            cancelPending()
            val now = System.currentTimeMillis()
            var earliest = Long.MAX_VALUE
            for ((_, map) in expireAtByAccount) {
                val it = map.entries.iterator()
                while (it.hasNext()) {
                    val entry = it.next()
                    if (entry.value <= now) {
                        it.remove()
                    } else if (entry.value < earliest) {
                        earliest = entry.value
                    }
                }
            }
            if (earliest == Long.MAX_VALUE) {
                return@runOnUIThread
            }
            val delay = (earliest - now).coerceAtLeast(50L)
            val runnable = Runnable {
                pendingExpireRunnable = null
                fireExpiredAndReload()
                scheduleNextExpiration()
            }
            pendingExpireRunnable = runnable
            AndroidUtilities.runOnUIThread(runnable, delay)
        }
    }

    private fun fireExpiredAndReload() {
        val now = System.currentTimeMillis()
        val accountsToReload = mutableSetOf<Int>()
        for ((account, map) in expireAtByAccount) {
            val it = map.entries.iterator()
            var changed = false
            while (it.hasNext()) {
                if (it.next().value <= now) {
                    it.remove()
                    changed = true
                }
            }
            if (changed) {
                accountsToReload.add(account)
            }
        }
        for (account in accountsToReload) {
            notifyDialogsReload(account)
        }
    }

    private fun cancelPending() {
        pendingExpireRunnable?.let { AndroidUtilities.cancelRunOnUIThread(it) }
        pendingExpireRunnable = null
    }

    private fun notifyDialogsReload(account: Int) {
        if (account < 0 || account >= UserConfig.MAX_ACCOUNT_COUNT) {
            return
        }
        if (!UserConfig.getInstance(account).isClientActivated) {
            return
        }
        NotificationCenter.getInstance(account)
            .postNotificationName(NotificationCenter.dialogsNeedReload)
    }
}
