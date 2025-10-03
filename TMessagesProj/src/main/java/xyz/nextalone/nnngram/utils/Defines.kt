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

import org.telegram.messenger.BuildVars
import xyz.nextalone.nnngram.BooleanConfig
import xyz.nextalone.nnngram.IntConfig
import xyz.nextalone.nnngram.StringConfig

/**
 * ConfigManager用到的Key都塞这 统一管理比较方便些
 */
@Suppress("ConstPropertyName")
object Defines {
    // Function
    @BooleanConfig const val showBotAPIID = "showBotAPIID"
    @BooleanConfig const val ignoreBlockedUser = "ignoreBlockedUser"
    @BooleanConfig const val blockSponsorAds = "blockSponsorMessages"
    @BooleanConfig const val hideGroupSticker = "hideGroupSticker"
    @BooleanConfig const val allowScreenshotOnNoForwardChat = "allowScreenshotOnNoForwardChat"
    @BooleanConfig(true) const val disableSharePhoneWithContactByDefault = "disableSharePhoneWithContactByDefault"
    @BooleanConfig const val autoDisableBuiltInProxy = "autoDisableBuiltInProxy"
    @BooleanConfig const val labelChannelUser = "labelChannelUser"
    @BooleanConfig const val displaySpoilerMsgDirectly = "displaySpoilerMsgDirectly"
    @BooleanConfig const val disableGreetingSticker = "disableGreetingSticker"
    @BooleanConfig const val channelAlias = "aliasChannel" // ignore typo
    const val channelAliasPrefix = "aliasChannelName_" // ignore typo
    @BooleanConfig const val linkedUser = "linkedUser"
    const val linkedUserPrefix = "linkedUser_"
    @BooleanConfig const val overrideChannelAlias = "overrideChannelAlias"
    @BooleanConfig const val hidePhone = "hidePhone"
    @BooleanConfig const val openArchiveOnPull = "openArchiveOnPull"
    @BooleanConfig const val disableJumpToNextChannel = "disableJumpToNextChannel"
    @BooleanConfig const val verifyLinkTip = "verifyLinkTip"
    @BooleanConfig const val showExactNumber = "showExactNumber"
    @BooleanConfig const val disableTrendingSticker = "disableTrendingSticker"
    @BooleanConfig const val disablePremiumSticker = "disablePremiumSticker"
    @BooleanConfig const val disableInstantCamera = "disableInstantCamera"
    @BooleanConfig const val showHiddenSettings = "showHiddenSettings"
    @BooleanConfig const val confirmToSendMediaMessages = "confirmToSendMediaMessages"
    @BooleanConfig const val disableUndo = "disableUndo"
    @BooleanConfig const val skipOpenLinkConfirm = "skipOpenLinkConfirm"
    @IntConfig(20) const val maxRecentSticker = "maxRecentSticker"
    @BooleanConfig const val unreadBadgeOnBackButton = "unreadBadgeOnBackButton"
    @BooleanConfig const val disableSendTyping = "disableSendTyping"
    @BooleanConfig const val ignoreReactionMention = "ignoreReactionMention"
    const val stickerSize = "customStickerSize"
    @BooleanConfig const val keepCopyFormatting = "keepCopyFormatting"
    @BooleanConfig const val dateOfForwardedMsg = "dateOfForwardedMsg"
    @BooleanConfig const val enchantAudio = "enchantAudio"
    @BooleanConfig const val avatarAsDrawerBackground = "avatarAsDrawerBackground"
    @BooleanConfig const val avatarBackgroundBlur = "avatarBackgroundBlur"
    @BooleanConfig const val avatarBackgroundDarken = "avatarBackgroundDarken"
    @BooleanConfig const val hideTimeForSticker = "hideTimeForSticker"
    @BooleanConfig const val showMessageID = "showMessageID"
    @BooleanConfig const val hideQuickSendMediaBottom = "hideQuickSendMediaButtom"
    @BooleanConfig const val largeAvatarAsBackground = "largeAvatarAsBackground"
    @BooleanConfig const val useSystemEmoji = "useSystemEmoji"
    const val customQuickMessage = "customQuickCommand"
    @BooleanConfig const val customQuickMessageEnabled = "customQuickMessageEnabled"
    const val customQuickMessageDisplayName = "customQuickCommandDisplayName"
    const val customQuickMessageRow = 92
    @BooleanConfig const val customQuickMsgSAR = "customQuickMessageSendAsReply"
    @BooleanConfig const val scrollableChatPreview = "scrollableChatPreview"
    @BooleanConfig const val disableVibration = "disableVibration"
    const val aospEmojiFont = "NotoColorEmoji.ttf"
    @BooleanConfig const val hidePremiumStickerAnim = "hidePremiumStickerAnim"
    @BooleanConfig const val fastSpeedUpload = "fastSpeedUpload"
    @BooleanConfig const val showTabsOnForward = "showTabsOnForward"
    @BooleanConfig(true) const val disableStickersAutoReorder = "disableStickersAutoReorder"
    @IntConfig(512) const val modifyDownloadSpeed = "modifyDownloadSpeed"
    @BooleanConfig const val disablePreviewVideoSoundShortcut = "disablePreviewVideoSoundShortcut"
    @BooleanConfig const val quickToggleAnonymous = "quickToggleAnonymous"
    @BooleanConfig const val hideProxySponsorChannel = "hideProxySponsorChannel"
    @BooleanConfig const val hideAllTab = "hideAllTab"
    @BooleanConfig const val ignoreMutedCount = "ignoreMutedCount"
    @BooleanConfig const val alwaysSendWithoutSound = "alwaysSendWithoutSound"
    @BooleanConfig const val markdownDisabled = "markdownEnable"
    @BooleanConfig(true) const val markdownParseLinks = "markdownParseLinks"
    @BooleanConfig(true) const val newMarkdownParser = "newMarkdownParser"
    @BooleanConfig const val showRPCError = "showRPCError"
    @BooleanConfig const val enablePanguOnSending = "enablePanguOnSending"
    @BooleanConfig const val enablePanguOnReceiving = "enablePanguOnReceiving"
    @BooleanConfig const val showExactTime = "showExactTime"
    @BooleanConfig const val hideStories = "hideStories"
    @BooleanConfig const val storyStealthMode = "storyStealthMode"
    @BooleanConfig const val ignoreUserSpecifiedReplyColor = "ignoreUserSpecifiedReplyColor"
    @BooleanConfig const val hideTitle = "hideTitle"
    @BooleanConfig const val showHideTitle = "showHideTitle"
    @StringConfig("") const val messageFilter = "messageFilter1"
    @BooleanConfig const val sendLargePhoto = "sendLargePhoto"

    /**
     * 0 default 1 online 2 offline
     */
    @IntConfig(0) const val keepOnlineStatusAs = "keepOnlineStatusAs"

    // Custom API
    @IntConfig(disableCustomAPI)
    const val customAPI = "customAPI"
    @IntConfig(BuildVars.APP_ID)
    const val customAppId = "customAppId"
    @StringConfig(BuildVars.APP_HASH)
    const val customAppHash = "customAppHash"
    const val disableCustomAPI = 0
    const val useTelegramAPI = 1
    const val useCustomAPI = 2
    const val telegramID = 4
    const val telegramHash = "014b35b6184100b085b0d0572f9b5103"

    // Menu Display
    @BooleanConfig const val showDeleteDownloadFiles = "showDeleteDownloadFiles"
    @BooleanConfig(true) const val showNoQuoteForward = "showNoQuoteForward"
    @BooleanConfig const val showMessagesDetail = "showMessagesDetail"
    @BooleanConfig const val showSaveMessages = "showSaveMessages"
    @BooleanConfig(true) const val showViewHistory = "showViewHistory"
    @BooleanConfig(true) const val showRepeat = "showRepeat"
    @BooleanConfig const val showRepeatAsCopy = "showRepeatAsCopy"
    @BooleanConfig const val showReverse = "showReverse"
    @BooleanConfig const val showCopyPhoto = "showCopyPhoto"
    @BooleanConfig(true) const val showReport = "showReport"
    @BooleanConfig(true) const val showReactions = "showReactions"

    // custom double tap
    @IntConfig(doubleTabReaction)
    const val doubleTab = "doubleTab"
    const val doubleTabNone = 0
    const val doubleTabReaction = 1
    const val doubleTabReply = 2
    const val doubleTabSaveMessages = 3
    const val doubleTabRepeat = 4
    const val doubleTabRepeatAsCopy = 5
    const val doubleTabEdit = 6
    const val doubleTabTranslate = 7
    const val doubleTabReverse = 8

    // Auto Update
    const val lastCheckUpdateTime = "lastCheckUpdateTime"
    const val updateChannel = "updateChannel"
    const val stableChannel = 1
    const val disableAutoUpdate = 0
//    const val ciChannel = 2
    const val updateChannelSkip = "updateChannelSkip"

    // Tab Menu
    @BooleanConfig const val hasUpdateDialogFilterDatabase = "hasUpdateDialogFilterDatabaseFix"
    @IntConfig(tabMenuMix) const val tabMenu = "tabMenu"
    const val tabMenuText = 0
    const val tabMenuMix = 1
    const val tabMenuIcon = 2

    // Override device
    @IntConfig(devicePerformanceAuto) const val devicePerformance = "devicePerformance"
    const val devicePerformanceAuto = -1
    const val devicePerformanceLow = 0
    const val devicePerformanceMedium = 1
    const val devicePerformanceHigh = 2

    // Override Player Decoder Mode
    @IntConfig(playerDecoderSoftware) const val playerDecoder = "playerDecoder"
    const val playerDecoderSoftware = 0
    const val playerDecoderHardware = 1
    const val playerDecoderPerferHW = 2

    // WebSocket Proxy
    @BooleanConfig(true) const val wsEnableTLS = "wsEnableTLS"
    @StringConfig("") const val wsServerHost = "wsServerHost"
    const val wsBuiltInProxyBackend = "wsBuiltInProxyBackend"

    // Translate
    @BooleanConfig const val showOriginal = "showOriginal"
    const val translatorProvider = "translatorProvider"
    const val deepLFormality = "deepLFormality"
    @StringConfig("") const val deepLxApi = "deepLxApi"
    const val deepLxPreserveFormatting = "deepLxPreserveFormatting"
    const val translatorStatus = "translatorStatus"
    const val targetLanguage = "targetLanguage"
    const val editTextTargetLanguage = "editTextTargetLanguage"
    const val restrictedLanguages = "restrictedLanguagesFix"
    @BooleanConfig const val autoTranslate = "autoTranslate"

    // Misc
    @JvmField
    val officialID = longArrayOf(
        966253902,  // Developer
        1668888324,  // Channel
        1578562490,  // Developer Channel
        1645976613,  // Update Channel
        1714986438,  // CI Channel
        1477185964,  // Discussion Group
        1068402676,  // Kitsune
        784901712,  // NextAlone
        457896977   // Queally rw
    )

    @BooleanConfig const val doNotUnarchiveBySwipe = "doNotUnarchiveBySwipe"
    @BooleanConfig const val hideInputFieldBotButton = "hideInputFieldBotButton"
    @BooleanConfig const val hideMessageSeenTooltip = "hideMessageSeenTooltip"
    @BooleanConfig const val disableNotificationBubble = "disableNotificationBubble"
    @BooleanConfig const val showOnlineStatus = "showOnlineStatus"
    @BooleanConfig const val disablePhotoSideAction = "disablePhotoSideAction"
    @BooleanConfig const val mergeMessage = "mergeMessage"
    @BooleanConfig const val filterZalgo = "filterZalgo"
    @BooleanConfig const val ignoreFolderUnreadCount = "ignoreFolderUnreadCount"
    @BooleanConfig const val ignoreChatStrict = "ignoreChatStrict"
    @StringConfig("Nnngram") const val customTitle = "customTitle"
    @StringConfig("") const val textStyleSettings = "textStyleSettings"
    @BooleanConfig const val hideSendAsButton = "hideSendAsButton"
    @BooleanConfig const val hideFilterMuteAll = "hideFilterMuteAll"
    @BooleanConfig const val hideKeyboardWhenScrolling = "hideKeyboardWhenScrolling"
    @BooleanConfig const val searchInPlace =  "searchInPlace"
    @IntConfig(100) const val gifSize = "gifSize"
    @BooleanConfig const val hideDialogsFloatingButton = "hideDialogsFloatingButton"
    @BooleanConfig const val disableChannelMuteButton = "disableChannelMuteButton"
    @BooleanConfig const val disableAutoPip = "disableAutoPip"
    @BooleanConfig const val sendMp4DocumentAsVideo = "sendMp4DocumentAsVideo"
    @BooleanConfig const val disableGravityDetectionInVideo = "disableGravityDetectionInVideo"
    @BooleanConfig const val hideProxyEntryInTitle = "hideProxyEntryInTitle"
    @BooleanConfig const val showProxyEntryInDrawer = "showProxyEntryInDrawer"
    @BooleanConfig const val autoMuteAfterJoiningChannel = "autoMuteAfterJoinChannel"
    @BooleanConfig const val disableRepeatInChannel = "disableRepeatInChannel"
    @BooleanConfig const val searchHashtagInCurrentChat = "searchHashtagInCurrentChat"
    @BooleanConfig const val cancelLoadingVideoWhenClose = "cancelLoadingVideoWhenClose"
    @BooleanConfig const val hideSavedAndArchivedMessagesInList = "hideSavedAndArchivedMessagesInList"
    @BooleanConfig(true) const val showIdAndDc = "showIdAndDc"

    // Drawer List
    @BooleanConfig(true) const val showNewGroup = "showNewGroup"
    @BooleanConfig(true) const val showContacts = "showContacts"
    @BooleanConfig(true) const val showCalls = "showCalls"
    @BooleanConfig(true) const val showPeopleNearby = "showPeopleNearby"
    @BooleanConfig(true) const val showSavedMessages = "showSavedMessages"
    @BooleanConfig const val showArchivedChats = "showArchivedChats"
    @BooleanConfig(true) const val showChangeEmojiStatus = "showChangeEmojiStatus"
    @BooleanConfig(true) const val showProfileMyStories = "showProfileMyStories"

    // Text Style
    @BooleanConfig(true) const val showTextSpoiler = "showTextSpoiler"
    @BooleanConfig(true) const val showTextBold = "showTextBold"
    @BooleanConfig(true) const val showTextItalic = "showTextItalic"
    @BooleanConfig(true) const val showTextMono = "showTextMono"
    @BooleanConfig(true) const val showTextStrike = "showTextStrike"
    @BooleanConfig(true) const val showTextUnderline = "showTextUnderline"
    @BooleanConfig(true) const val showTextCreateMention = "showTextCreateMention"
    @BooleanConfig(true) const val showTextCreateLink = "showTextCreateLink"
    @BooleanConfig(true) const val showTextRegular = "showTextRegular"
    @BooleanConfig(true) const val showTextQuote = "showTextQuote"
    @BooleanConfig(true) const val showTextMonoCode = "showTextMonoCode"
    @BooleanConfig(true) const val showTextTranslate = "showTextTranslate"
    @BooleanConfig(false) const val enableXiaomiHyperAi = "enableXiaomiHyperAi"

    /**
     * 数组中元素未找到的下标，值为-1
     */
    private const val indexNotFound = -1

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    @JvmStatic
    fun contains(array: LongArray?, value: Long): Boolean {
        return indexOf(array, value) > indexNotFound
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回[.indexNotFound]
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回[.indexNotFound]
     */
    @JvmStatic
    fun indexOf(array: LongArray?, value: Long): Int {
        if (null != array) {
            for (i in array.indices) {
                if (value == array[i]) {
                    return i
                }
            }
        }
        return indexNotFound
    }
}
