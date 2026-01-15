/*
 * Copyright (C) 2019-2025 qwq233 <qwq233@qwq2333.top>
 * https://github.com/qwq233/Nullgram
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.io.File
import org.gradle.api.JavaVersion

object Version {
    fun findBuildToolsVersion(): String {
        val defaultBuildToolsVersion = "35.0.1"
        return File(System.getenv("ANDROID_HOME"), "build-tools").listFiles()?.filter { it.isDirectory }?.maxOfOrNull { it.name }?.also { println("Using build tools version $it") }
            ?: defaultBuildToolsVersion
    }

    @JvmStatic
    val java = JavaVersion.VERSION_17

    @JvmStatic
    val officialVersionName = "12.3.1"

    @JvmStatic
    val isStable = false
}
