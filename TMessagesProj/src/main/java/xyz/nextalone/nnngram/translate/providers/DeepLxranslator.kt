/*
 * Copyright (C) 2019-2023 qwq233 <qwq233@qwq2333.top>
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

package xyz.nextalone.nnngram.translate.providers

import android.text.TextUtils
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.json.JSONObject
import xyz.nextalone.gen.Config
import xyz.nextalone.nnngram.config.ConfigManager
import xyz.nextalone.nnngram.translate.BaseTranslator
import xyz.nextalone.nnngram.utils.Defines
import xyz.nextalone.nnngram.utils.Log
import java.io.IOException
import java.util.Locale
import java.util.UUID
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author NextAlone
 * @date 2024/11/04 01:14
 *
 */
object DeepLxTranslator : BaseTranslator() {

    private val targetLanguages = listOf(
        "bg", "cs", "da", "de", "el", "en-GB", "en-US", "en", "es", "et",
        "fi", "fr", "hu", "id", "it", "ja", "lt", "lv", "nl", "pl", "pt-BR",
        "pt-PT", "pt", "ro", "ru", "sk", "sl", "sv", "tr", "uk", "zh"
    )

    override fun getTargetLanguages(): List<String> = targetLanguages

    override fun convertLanguageCode(language: String, country: String?): String {
        val languageLowerCase: String = language.lowercase(Locale.getDefault())
        val code: String = if (!TextUtils.isEmpty(country)) {
            val countryUpperCase: String = country!!.uppercase(Locale.getDefault())
            if (targetLanguages.contains("$languageLowerCase-$countryUpperCase")) {
                "$languageLowerCase-$countryUpperCase"
            } else {
                languageLowerCase
            }
        } else {
            languageLowerCase
        }
        return code
    }

    override suspend fun translateText(text: String, from: String, to: String): RequestResult {
        Log.d("text: $text")
        Log.d("from: $from")
        Log.d("to: $to")
        if (from == to) {
            return RequestResult(from, text)
        }
        if (Config.deepLxApi.isEmpty()) {
            throw IOException("DeepLx API or token is empty")
        }

        client.post(Config.deepLxApi) {
            contentType(ContentType.Application.Json)
//            header("Referer", "https://www.deepl.com/")
//            header("User-Agent", "DeepL/1.8(52) Android 13 (Pixel 5;aarch64)")
//            header("Client-Id", uuid)
//            header("x-instance", uuid)
//            header("x-app-os-name", "Android")
//            header("x-app-os-version", "13")
//            header("x-app-version", "1.8")
//            header("x-app-build", "52")
//            header("x-app-device", "Pixel 5")
//            header("x-app-instance-id", uuid)
            setBody(getRequestBody(text, from, to))
        }.let {
            when (it.status) {
                HttpStatusCode.OK -> {
                    val jsonObject = JSONObject(it.bodyAsText())
                    if (jsonObject.has("error")) {
                        throw IOException(jsonObject.getString("message"))
                    }
                    return RequestResult(
                        jsonObject.getString("source_lang"),
                        jsonObject.getString("data")
                    )
                }

                else -> {
                    Log.w(it.bodyAsText())
                    return RequestResult(from, null, it.status)
                }
            }
        }
    }

    const val FORMALITY_DEFAULT = 0
    const val FORMALITY_MORE = 1
    const val FORMALITY_LESS = 2


    private fun getRequestBody(text: String, from: String, to: String): String {
        var iCounter = 1
        val iMatcher: Matcher = Pattern.compile("[i]").matcher(text)
        while (iMatcher.find()) {
            iCounter++
        }
        val params = JSONObject().apply {
            put("text", text)
            put("split_sentences", 1)
            put("source_lang", from)
            put("target_lang", to)
            put("preserve_formatting", true)
            put("formality", getFormalityString())
        }

        return params.toString()
    }

    private fun getFormalityString(): String? {
        return when (ConfigManager.getIntOrDefault(Defines.deepLFormality, -1)) {
            FORMALITY_DEFAULT -> "default"
            FORMALITY_MORE -> "more"
            FORMALITY_LESS -> "less"
            else -> "default"
        }
    }
}
