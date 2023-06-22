/*
 * Copyright (C) 2019-2024 qwq233 <qwq233@qwq2333.top>
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

import java.io.File
import java.util.Properties
import org.gradle.api.JavaVersion

object Version {
    fun findBuildToolsVersion(): String {
        val defaultBuildToolsVersion = "33.0.0"
        return File(System.getenv("ANDROID_HOME"), "build-tools").listFiles()?.filter { it.isDirectory }?.maxOfOrNull { it.name }?.also { println("Using build tools version $it") }
            ?: defaultBuildToolsVersion
    }

    fun findNdkVersion(): String {
        val defaultNdkVersion = "23.2.8568313"
        val ndkBundle = File(System.getenv("ANDROID_HOME"), "ndk-bundle")
        if (ndkBundle.exists()) {
            val sourceProperties = File(ndkBundle, "source.properties")
            if (sourceProperties.exists()) {
                val properties = Properties()
                sourceProperties.inputStream().use {
                    properties.load(it)
                }
                return properties.getProperty("Pkg.Revision").also { if (it != null) println("Using ndk version $it") }
                    ?: defaultNdkVersion
            }
        }
        return File(System.getenv("ANDROID_HOME"), "ndk").listFiles()?.filter { it.isDirectory }?.maxOfOrNull { it.name }.also { if (it != null) println("Using ndk version $it") }
            ?: defaultNdkVersion
    }

    @JvmStatic
    val java = JavaVersion.VERSION_17

    @JvmStatic
    val officialVersionName = "11.2.3"

    @JvmStatic
    val isStable = false
}
