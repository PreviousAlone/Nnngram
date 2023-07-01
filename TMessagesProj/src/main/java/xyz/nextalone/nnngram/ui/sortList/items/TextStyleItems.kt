package xyz.nextalone.nnngram.ui.sortList.items

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import xyz.nextalone.nnngram.utils.Defines

object TextStyleItems : SortListItems() {
    override val itemDefines = arrayOf(
        Defines.showTextQuote,
        Defines.showTextSpoiler,
        Defines.showTextBold,
        Defines.showTextItalic,
        Defines.showTextMonoCode,
        Defines.showTextMono,
        Defines.showTextStrike,
        Defines.showTextUnderline,
        Defines.showTextCreateMention,
        Defines.showTextCreateLink,
        Defines.showTextRegular,
    )
    override var itemNames = arrayOf(
        LocaleController.getString("Quote", R.string.Quote),
        LocaleController.getString("Spoiler", R.string.Spoiler),
        LocaleController.getString("Bold", R.string.Bold),
        LocaleController.getString("Italic", R.string.Italic),
        LocaleController.getString("MonoCode", R.string.MonoCode),
        LocaleController.getString("Mono", R.string.Mono),
        LocaleController.getString("Strike", R.string.Strike),
        LocaleController.getString("Underline", R.string.Underline),
        LocaleController.getString("CreateMention", R.string.CreateMention),
        LocaleController.getString("CreateLink", R.string.CreateLink),
        LocaleController.getString("Regular", R.string.Regular),
    )

    val itemIds = arrayOf(
        R.id.menu_quote, R.id.menu_spoiler, R.id.menu_bold, R.id.menu_italic, R.id.menu_code, R.id.menu_mono, R.id.menu_strike, R.id.menu_underline, R.id.menu_mention, R.id.menu_link, R.id.menu_regular,
    )

    override var define = Defines.textStyleSettings
    override val itemDefaultConfig: Boolean = true
}
