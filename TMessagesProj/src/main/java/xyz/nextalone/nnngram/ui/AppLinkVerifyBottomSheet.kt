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
package xyz.nextalone.nnngram.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.BottomSheet
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.StickerImageView
import xyz.nextalone.nnngram.config.ConfigManager
import xyz.nextalone.nnngram.utils.Defines

@RequiresApi(api = Build.VERSION_CODES.S)
class AppLinkVerifyBottomSheet(fragment: BaseFragment) : BottomSheet(fragment.parentActivity, false) {
    override fun canDismissWithSwipe(): Boolean = false
    override fun canDismissWithTouchOutside(): Boolean = false

    init {
        setCanceledOnTouchOutside(false);
        val context: Context = fragment.parentActivity;
        val frameLayout = FrameLayout(context);
        val closeView = ImageView(context); closeView.background = Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector))
        closeView.setColorFilter(Theme.getColor(Theme.key_sheet_other))
        closeView.setImageResource(R.drawable.ic_layer_close)
        closeView.setOnClickListener { view: View? -> dismiss() }
        val closeViewPadding = AndroidUtilities.dp(8f)
        closeView.setPadding(closeViewPadding, closeViewPadding, closeViewPadding, closeViewPadding)
        frameLayout.addView(
            closeView,
            LayoutHelper.createFrame(
                36,
                36f,
                Gravity.TOP or Gravity.END,
                6f,
                8f,
                6f,
                0f
            )
        )
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        frameLayout.addView(linearLayout)
        val imageView = StickerImageView(context, currentAccount)
        imageView.setStickerNum(3)
        imageView.imageReceiver.setAutoRepeat(1)
        linearLayout.addView(
            imageView,
            LayoutHelper.createLinear(
                144,
                144,
                Gravity.CENTER_HORIZONTAL,
                0,
                16,
                0,
                0
            )
        )
        val title = TextView(context)
        title.gravity = Gravity.START
        title.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        title.typeface = AndroidUtilities.getTypeface("fonts/rmedium.ttf")
        title.text = LocaleController.getString("AppLinkNotVerifiedTitle", R.string.AppLinkNotVerifiedTitle)
        linearLayout.addView(
            title,
            LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT.toFloat(),
                0,
                21f,
                30f,
                21f,
                0f
            )
        )
        val description = TextView(context)
        description.gravity = Gravity.START
        description.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
        description.setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
        description.text = AndroidUtilities.replaceTags(LocaleController.getString("AppLinkNotVerifiedMessage", R.string.AppLinkNotVerifiedMessage))
        linearLayout.addView(
            description,
            LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT.toFloat(),
                0,
                21f,
                15f,
                21f,
                16f
            )
        )
        val buttonTextView = TextView(context)
        buttonTextView.setPadding(AndroidUtilities.dp(34f), 0, AndroidUtilities.dp(34f), 0)
        buttonTextView.gravity = Gravity.CENTER
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        buttonTextView.typeface = AndroidUtilities.getTypeface("fonts/rmedium.ttf")
        buttonTextView.text = LocaleController.getString(
            "GoToSettings",
            R.string.GoToSettings
        )
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText))
        buttonTextView.background = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6f),
            Theme.getColor(Theme.key_featuredStickers_addButton),
            ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120))
        linearLayout.addView(
            buttonTextView,
            LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                48f,
                0,
                16f,
                15f,
                16f,
                8f
            )
        )
        buttonTextView.setOnClickListener { view: View? ->
            val intent = Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, Uri.parse("package:" + context.packageName))
            context.startActivity(intent)
        }
        val textView = TextView(context)
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        textView.text = LocaleController.getString(
            "DontAskAgain",
            R.string.DontAskAgain
        )
        textView.setTextColor(Theme.getColor(Theme.key_featuredStickers_addButton))
        linearLayout.addView(
            textView,
            LayoutHelper.createFrame(
                LayoutHelper.MATCH_PARENT,
                48f,
                0,
                16f,
                0f,
                16f,
                0f
            )
        )
        textView.setOnClickListener { view: View? ->
            dismiss()
            ConfigManager.putBoolean(Defines.verifyLinkTip, true)
        }
        val scrollView = ScrollView(context)
        scrollView.addView(frameLayout)
        setCustomView(scrollView)
    }

    companion object {
        @JvmStatic
        fun checkBottomSheet(fragment: BaseFragment) {
            if (ConfigManager.getBooleanOrFalse(Defines.verifyLinkTip)) return
            val context: Context = fragment.parentActivity
            val manager = context.getSystemService(DomainVerificationManager::class.java)
            var userState: DomainVerificationUserState? = null
            try {
                userState = manager.getDomainVerificationUserState(context.packageName)
            } catch (ignore: PackageManager.NameNotFoundException) {
            }
            if (userState == null) return
            var hasUnverified = false
            val hostToStateMap = userState.hostToStateMap
            for (key in hostToStateMap.keys) {
                val stateValue = hostToStateMap[key]
                if (stateValue == null || stateValue == DomainVerificationUserState.DOMAIN_STATE_VERIFIED || stateValue == DomainVerificationUserState.DOMAIN_STATE_SELECTED) {
                    continue
                }
                hasUnverified = true
                break
            }
            if (hasUnverified) fragment.showDialog(AppLinkVerifyBottomSheet(fragment)
            )
        }
    }
}
