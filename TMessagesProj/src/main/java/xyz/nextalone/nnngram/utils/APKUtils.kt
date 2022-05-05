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

import xyz.nextalone.nnngram.utils.LogUtils.i
import org.telegram.messenger.ApplicationLoader
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.zip.ZipFile

object APKUtils {
    /**
     * @return 获取到的abi
     * @throws IllegalStateException 如果没找到lib目录就会抛出这错误 一般不太可能发生
     */
    @JvmStatic
    @get:Throws(Exception::class)
    val abi: String
        get() {
            val filePath = ApplicationLoader.applicationContext.applicationInfo.sourceDir
            val file = ZipFile(filePath)
            val entries = file.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name
                if (name.contains("lib")) {
                    i("getAbi: " + entry.name.split("/").toTypedArray()[1])
                    val target = entry.name.split("/").toTypedArray()[1]
                    return if (target.contains("arm64")) {
                        "arm64"
                    } else if (target.contains("armeabi")) {
                        "arm32"
                    } else if (target.contains("x86") && !target.contains("x86_64")) {
                        "x86"
                    } else {
                        "x86_64"
                    }
                }
            }
            throw IllegalStateException("Directory Not Found")
        }

    /**
     * 挂起当前线程
     *
     * @param millis 挂起的毫秒数
     * @return 被中断返回false，否则true
     */
    @JvmStatic
    fun sleep(millis: Long): Boolean {
        if (millis > 0) {
            try {
                Thread.sleep(millis)
            } catch (e: InterruptedException) {
                return false
            }
        }
        return true
    }
}
