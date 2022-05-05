/*
 * Copyright (C) 2019-2022 qwq233 <qwq233@qwq2333.top>
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

import android.app.Application
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import org.telegram.messenger.BuildVars

object AppcenterUtils {
    private val appCenterToken = BuildVars.APPCENTER_HASH

    @JvmStatic
    fun start(app: Application?) {
        AppCenter.start(app, appCenterToken, Crashes::class.java, Analytics::class.java)
    }

    @JvmStatic
    fun trackEvent(event: String?) {
        Analytics.trackEvent(event)
    }

    @JvmStatic
    fun trackEvent(event: String?, map: HashMap<String?, String?>?) {
        Analytics.trackEvent(event, map)
    }

    @JvmStatic
    fun trackCrashes(thr: Throwable?) {
        Crashes.trackError(thr!!)
    }
}
