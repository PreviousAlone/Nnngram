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

package xyz.nextalone.nnngram.helpers

import androidx.core.util.Pair
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import xyz.nextalone.nnngram.config.ConfigManager
import xyz.nextalone.nnngram.utils.Defines

object FolderIconHelper {
    val folderIcons = linkedMapOf<String, Int>()

    init {
        folderIcons["\uD83D\uDC31"] = R.drawable.filter_cat // 🐱
        folderIcons["\uD83D\uDCD5"] = R.drawable.filter_book // 📕
        folderIcons["\uD83D\uDCB0"] = R.drawable.filter_money // 💰
        folderIcons["\uD83C\uDFAE"] = R.drawable.filter_game // 🎮
        folderIcons["\uD83D\uDCA1"] = R.drawable.filter_light // 💡
        folderIcons["\uD83D\uDC4C"] = R.drawable.filter_like // 👌
        folderIcons["\uD83C\uDFB5"] = R.drawable.filter_note // 🎵
        folderIcons["\uD83C\uDFA8"] = R.drawable.filter_palette // 🎨
        folderIcons["\u2708"] = R.drawable.filter_travel // ✈
        folderIcons["\u26BD"] = R.drawable.filter_sport // ⚽
        folderIcons["\u2B50"] = R.drawable.filter_favorite // ⭐
        folderIcons["\uD83C\uDF93"] = R.drawable.filter_study // 🎓
        folderIcons["\uD83D\uDEEB"] = R.drawable.filter_airplane // 🛫
        folderIcons["\uD83D\uDC64"] = R.drawable.filter_private // 👤
        folderIcons["\uD83D\uDC65"] = R.drawable.filter_group // 👥
        folderIcons["\uD83D\uDCAC"] = R.drawable.filter_all // 💬
        folderIcons["\u2705"] = R.drawable.filter_unread // ✅
        folderIcons["\uD83E\uDD16"] = R.drawable.filter_bots // 🤖
        folderIcons["\uD83D\uDC51"] = R.drawable.filter_crown // 👑
        folderIcons["\uD83C\uDF39"] = R.drawable.filter_flower // 🌹
        folderIcons["\uD83C\uDFE0"] = R.drawable.filter_home // 🏠
        folderIcons["\u2764"] = R.drawable.filter_love // ❤
        folderIcons["\uD83C\uDFAD"] = R.drawable.filter_mask // 🎭
        folderIcons["\uD83C\uDF78"] = R.drawable.filter_party // 🍸
        folderIcons["\uD83D\uDCC8"] = R.drawable.filter_trade // 📈
        folderIcons["\uD83D\uDCBC"] = R.drawable.filter_work // 💼
        folderIcons["\uD83D\uDD14"] = R.drawable.filter_unmuted // 🔔
        folderIcons["\uD83D\uDCE2"] = R.drawable.filter_channels // 📢
        folderIcons["\uD83D\uDCC1"] = R.drawable.filter_custom // 📁
        folderIcons["\uD83D\uDCCB"] = R.drawable.filter_setup // 📋
    }

    @JvmStatic
    fun getEmoticonFromFlags(newFilterFlags: Int): Pair<String, String> {
        var flags = newFilterFlags and MessagesController.DIALOG_FILTER_FLAG_ALL_CHATS
        var newName = ""
        var newEmoticon = ""
        if (flags and MessagesController.DIALOG_FILTER_FLAG_ALL_CHATS == MessagesController.DIALOG_FILTER_FLAG_ALL_CHATS) {
            if (newFilterFlags and MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_READ != 0) {
                newName = LocaleController.getString("FilterNameUnread", R.string.FilterNameUnread)
                newEmoticon = "\u2705"
            } else if (newFilterFlags and MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_MUTED != 0) {
                newName = LocaleController.getString("FilterNameNonMuted", R.string.FilterNameNonMuted)
                newEmoticon = "\uD83D\uDD14"
            }
        } else if (flags and MessagesController.DIALOG_FILTER_FLAG_CONTACTS != 0) {
            flags = flags and MessagesController.DIALOG_FILTER_FLAG_CONTACTS.inv()
            if (flags == 0) {
                newName = LocaleController.getString("FilterContacts", R.string.FilterContacts)
                newEmoticon = "\uD83D\uDC64"
            }
        } else if (flags and MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS != 0) {
            flags = flags and MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS.inv()
            if (flags == 0) {
                newName = LocaleController.getString("FilterNonContacts", R.string.FilterNonContacts)
                newEmoticon = "\uD83D\uDC64"
            }
        } else if (flags and MessagesController.DIALOG_FILTER_FLAG_GROUPS != 0) {
            flags = flags and MessagesController.DIALOG_FILTER_FLAG_GROUPS.inv()
            if (flags == 0) {
                newName = LocaleController.getString("FilterGroups", R.string.FilterGroups)
                newEmoticon = "\uD83D\uDC65"
            }
        } else if (flags and MessagesController.DIALOG_FILTER_FLAG_BOTS != 0) {
            flags = flags and MessagesController.DIALOG_FILTER_FLAG_BOTS.inv()
            if (flags == 0) {
                newName = LocaleController.getString("FilterBots", R.string.FilterBots)
                newEmoticon = "\uD83E\uDD16"
            }
        } else if (flags and MessagesController.DIALOG_FILTER_FLAG_CHANNELS != 0) {
            flags = flags and MessagesController.DIALOG_FILTER_FLAG_CHANNELS.inv()
            if (flags == 0) {
                newName = LocaleController.getString("FilterChannels", R.string.FilterChannels)
                newEmoticon = "\uD83D\uDCE2"
            }
        }
        return Pair.create(newName, newEmoticon)
    }

    @JvmStatic
    fun getIconWidth(): Int {
        return AndroidUtilities.dp(28f)
    }

    @JvmStatic
    fun getPadding(): Int = if (ConfigManager.getIntOrDefault(Defines.tabMenu, Defines.tabMenuMix) == Defines.tabMenuMix) {
        AndroidUtilities.dp(6f)
    } else {
        0
    }


    @JvmStatic
    fun getTotalIconWidth(): Int = if (ConfigManager.getIntOrDefault(Defines.tabMenu, Defines.tabMenuMix) != Defines.tabMenuText) {
        getIconWidth() + getPadding()
    } else {
        0
    }

    @JvmStatic
    fun getPaddingTab(): Float = if (ConfigManager.getIntOrDefault(Defines.tabMenu, Defines.tabMenuMix) != Defines.tabMenuIcon) {
        24f
    } else {
        16f
    }


    @JvmStatic
    fun getTabIcon(emoji: String?): Int {
        if (emoji != null) {
            val folderIcon: Int? = folderIcons[emoji]
            if (folderIcon != null) {
                return folderIcon
            }
        }
        return R.drawable.filter_custom
    }
}
