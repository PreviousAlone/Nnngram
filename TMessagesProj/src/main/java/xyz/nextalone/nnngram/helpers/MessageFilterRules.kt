/*
 * Copyright (C) 2019-2026 qwq233 <qwq233@qwq2333.top>
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
package xyz.nextalone.nnngram.helpers

import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONObject
import org.telegram.messenger.MessagesController
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import xyz.nextalone.gen.Config
import xyz.nextalone.nnngram.utils.Log
import java.util.UUID
import java.util.regex.Pattern

/**
 * 消息过滤规则的数据模型 + 持久化 + 匹配引擎.
 *
 * 存储: 以 JSON 数组字符串形式存在 Config.filterRules (Defines.filterRules key).
 * 规则被 Pattern.compile 后缓存, 规则变更时 invalidate 并发通知刷新开启的聊天.
 */
object MessageFilterRules {

    /** 命中时消息动作. ord 决定优先级 (大优先): HIDE > COLLAPSE > SPOILER > NONE. */
    const val ACTION_NONE = 0
    const val ACTION_SPOILER = 1
    const val ACTION_COLLAPSE = 2
    const val ACTION_HIDE = 3

    /** 匹配维度. */
    const val MATCH_REGEX = 0        // 对 messageText/caption 做正则 find
    const val MATCH_CONTAINS = 1     // 朴素包含 (大小写敏感 flag 控制)
    const val MATCH_SENDER_ID = 2    // 发件人 peer id (long, 与 forward origin 一致检查)
    const val MATCH_VIA_BOT = 3      // 通过 inline bot 发送. matchValue 空/"*" = 任意, 数字 = 特定 bot id

    data class Rule(
        val id: String,
        val matchType: Int,
        val matchValue: String,
        val action: Int,
        val enabled: Boolean = true,
        val caseInsensitive: Boolean = false,
    ) {
        fun toJson(): JSONObject = JSONObject().apply {
            put("id", id)
            put("matchType", matchType)
            put("matchValue", matchValue)
            put("action", action)
            put("enabled", enabled)
            put("caseInsensitive", caseInsensitive)
        }

        companion object {
            fun fromJson(o: JSONObject): Rule = Rule(
                id = o.optString("id", UUID.randomUUID().toString()),
                matchType = o.optInt("matchType", MATCH_REGEX),
                matchValue = o.optString("matchValue", ""),
                action = o.optInt("action", ACTION_HIDE),
                enabled = o.optBoolean("enabled", true),
                caseInsensitive = o.optBoolean("caseInsensitive", false),
            )
        }
    }

    private data class Compiled(val rule: Rule, val pattern: Pattern?)

    @Volatile private var cache: List<Compiled>? = null

    private fun compiled(): List<Compiled> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val list = loadRaw().mapNotNull { r ->
                if (!r.enabled || r.matchValue.isEmpty()) return@mapNotNull null
                val p: Pattern? = when (r.matchType) {
                    MATCH_REGEX -> runCatching {
                        val flags = if (r.caseInsensitive) Pattern.CASE_INSENSITIVE else 0
                        Pattern.compile(r.matchValue, flags)
                    }.getOrNull()
                    else -> null
                }
                // regex 编译失败的规则直接丢, 其它匹配类型 pattern 始终为 null
                if (r.matchType == MATCH_REGEX && p == null) null else Compiled(r, p)
            }
            cache = list
            return list
        }
    }

    /** 从 SharedPreferences 读出原始规则列表. */
    @JvmStatic
    fun loadRaw(): List<Rule> {
        val json = Config.filterRules
        if (json.isEmpty() || json == "[]") return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            val out = ArrayList<Rule>(arr.length())
            for (i in 0 until arr.length()) out.add(Rule.fromJson(arr.getJSONObject(i)))
            out
        }.onFailure { Log.e("MessageFilterRules load failed", it) }.getOrDefault(emptyList())
    }

    /** 写回规则并通知 UI 刷新. */
    @JvmStatic
    fun save(rules: List<Rule>) {
        val arr = JSONArray()
        rules.forEach { arr.put(it.toJson()) }
        Config.filterRules = arr.toString()
        invalidate()
    }

    @JvmStatic
    fun invalidate() {
        cache = null
        // 清所有账号的 dialog 预览 fallback 缓存, 否则 DialogCell 会继续显示旧规则下的替换消息.
        for (a in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            runCatching { MessagesController.getInstance(a).dialogMessageFromUnblocked.clear() }
        }
        NotificationCenter.getGlobalInstance().postNotificationName(
            NotificationCenter.messageFilterRulesChanged
        )
    }

    /**
     * 给定消息字段, 返回应执行的动作.
     * 多规则命中时取优先级最高 (HIDE > COLLAPSE > SPOILER).
     * @param viaBotId 通过 inline bot 发送的 bot user id, 0 表示未解析出数值 id.
     * @param viaBotName bot username (无 @ 前缀), null/空表示无. 某些 via bot 消息只有 name 没 id.
     */
    @JvmStatic
    fun match(messageText: CharSequence?, caption: CharSequence?, senderId: Long, viaBotId: Long, viaBotName: String?): Int {
        val rules = compiled()
        if (rules.isEmpty()) return ACTION_NONE
        val hasViaBot = viaBotId != 0L || !viaBotName.isNullOrEmpty()
        var best = ACTION_NONE
        for (c in rules) {
            val hit = when (c.rule.matchType) {
                MATCH_REGEX -> {
                    val p = c.pattern ?: continue
                    (messageText != null && p.matcher(messageText).find()) ||
                        (caption != null && p.matcher(caption).find())
                }
                MATCH_CONTAINS -> {
                    val needle = c.rule.matchValue
                    val ci = c.rule.caseInsensitive
                    containsText(messageText, needle, ci) || containsText(caption, needle, ci)
                }
                MATCH_SENDER_ID -> {
                    val target = c.rule.matchValue.toLongOrNull() ?: continue
                    senderId == target
                }
                MATCH_VIA_BOT -> {
                    if (!hasViaBot) false
                    else {
                        val raw = c.rule.matchValue.trim()
                        if (raw.isEmpty() || raw == "*") true
                        else raw.toLongOrNull() == viaBotId
                    }
                }
                else -> false
            }
            if (hit && c.rule.action > best) best = c.rule.action
            if (best == ACTION_HIDE) break // 短路: 已是最高优先级
        }
        return best
    }

    private fun containsText(src: CharSequence?, needle: String, ci: Boolean): Boolean {
        if (src == null || needle.isEmpty()) return false
        if (!ci) return TextUtils.indexOf(src, needle) >= 0
        val s = src.toString().lowercase()
        return s.contains(needle.lowercase())
    }
}
