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
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import xyz.nextalone.gen.Config
import java.util.concurrent.atomic.AtomicLong

/**
 * Keeps "recently conversed" chats visible inside dialog filters that
 * exclude read messages. A dialog is considered recent if its
 * `last_message_date` is within the configured retention window
 * (measured against the server-synced time, not device wall clock).
 *
 * Scheduling is event-driven: each `isRecent` hit reports its earliest
 * expiration; a single pending UI-thread runnable fires exactly at that
 * point so expired dialogs drop even without any other activity. The
 * scheduler quiesces when no dialogs are retained and pauses work while
 * the main interface is backgrounded.
 */
object UnreadDialogRetention {

    private val earliestExpireAt = AtomicLong(Long.MAX_VALUE)

    @Volatile private var reconcileQueued = false
    @Volatile private var pendingExpireAt = Long.MAX_VALUE

    private var pendingRunnable: Runnable? = null

    @JvmStatic
    fun isEnabled(): Boolean = Config.unreadDialogRetention > 0

    @JvmStatic
    fun getRetentionMillis(): Long = Config.unreadDialogRetention * 1000L

    @JvmStatic
    fun isRecent(account: Int, dialog: TLRPC.Dialog?): Boolean {
        if (dialog == null) return false
        val retention = getRetentionMillis()
        if (retention <= 0) return false
        val lastSec = dialog.last_message_date
        if (lastSec <= 0) return false
        val nowSec = ConnectionsManager.getInstance(account).currentTime
        val ageSec = (nowSec - lastSec).toLong()
        if (ageSec < 0) return true // clock skew: treat future timestamps as just now
        val ageMillis = ageSec * 1000L
        if (ageMillis > retention) return false
        val expireAt = System.currentTimeMillis() + (retention - ageMillis)
        atomicMin(earliestExpireAt, expireAt)
        requestReconcile()
        return true
    }

    /**
     * Re-evaluate scheduling after a setting change: cancel any pending
     * work and fire one reload so the new duration takes effect.
     */
    @JvmStatic
    fun onSettingChanged() {
        AndroidUtilities.runOnUIThread {
            cancelPending()
            earliestExpireAt.set(Long.MAX_VALUE)
            pendingExpireAt = Long.MAX_VALUE
            reloadAllAccounts()
        }
    }

    private fun requestReconcile() {
        if (reconcileQueued) return
        reconcileQueued = true
        AndroidUtilities.runOnUIThread {
            reconcileQueued = false
            reconcile()
        }
    }

    private fun reconcile() {
        val candidate = earliestExpireAt.getAndSet(Long.MAX_VALUE)
        if (candidate == Long.MAX_VALUE) return
        if (!isEnabled()) return
        if (ApplicationLoader.mainInterfacePaused) return
        if (candidate >= pendingExpireAt) return
        cancelPending()
        pendingExpireAt = candidate
        val delay = (candidate - System.currentTimeMillis()).coerceAtLeast(50L)
        val runnable = Runnable {
            pendingRunnable = null
            pendingExpireAt = Long.MAX_VALUE
            if (!isEnabled()) return@Runnable
            if (ApplicationLoader.mainInterfacePaused) return@Runnable
            reloadAllAccounts()
        }
        pendingRunnable = runnable
        AndroidUtilities.runOnUIThread(runnable, delay)
    }

    private fun cancelPending() {
        pendingRunnable?.let { AndroidUtilities.cancelRunOnUIThread(it) }
        pendingRunnable = null
        pendingExpireAt = Long.MAX_VALUE
    }

    private fun reloadAllAccounts() {
        for (account in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            if (!UserConfig.getInstance(account).isClientActivated) continue
            NotificationCenter.getInstance(account)
                .postNotificationName(NotificationCenter.dialogsNeedReload)
        }
    }

    private fun atomicMin(ref: AtomicLong, candidate: Long) {
        while (true) {
            val cur = ref.get()
            if (candidate >= cur) return
            if (ref.compareAndSet(cur, candidate)) return
        }
    }
}
