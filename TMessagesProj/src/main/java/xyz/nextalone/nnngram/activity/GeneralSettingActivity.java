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

package xyz.nextalone.nnngram.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Locale;

import kotlin.Unit;
import xyz.nextalone.gen.Config;
import xyz.nextalone.nnngram.config.ConfigManager;
import xyz.nextalone.nnngram.helpers.TranslateHelper;
import xyz.nextalone.nnngram.helpers.TranslateHelper.ProviderType;
import xyz.nextalone.nnngram.translate.providers.DeepLTranslator;
import xyz.nextalone.nnngram.ui.DrawerProfilePreviewCell;
import xyz.nextalone.nnngram.ui.PopupBuilder;
import xyz.nextalone.nnngram.utils.Defines;

@SuppressLint("NotifyDataSetChanged")
public class GeneralSettingActivity extends BaseActivity {

    private DrawerProfilePreviewCell profilePreviewCell;

    private int generalRow;

    private int drawerRow;
    private int avatarAsDrawerBackgroundRow;
    private int avatarBackgroundBlurRow;
    private int avatarBackgroundDarkenRow;
    private int largeAvatarAsBackgroundRow;
    private int hidePhoneRow;
    private int drawerListRow;
    private int hideDialogsFloatingButtonRow;
    private int drawer2Row;

    private int translatorRow;
    private int showOriginalRow;
    private int deepLFormalityRow;
    private int deepLxApiRow;
    private int translatorTypeRow;
    private int translationProviderRow;
    private int translationTargetRow;
    private int doNotTranslateRow;
    private int autoTranslateRow;
    private int translator2Row;

    private int customTitleRow;
    private int showBotAPIRow;
    private int showExactNumberRow;
    private int showExactTimeRow;
    private int disableInstantCameraRow;
    private int disableUndoRow;
    private int skipOpenLinkConfirmRow;
    private int tabsTitleTypeRow;
    private int openArchiveOnPullRow;
    private int hideAllTabRow;
    private int ignoreMutedCountRow;
    private int disableSharePhoneWithContactByDefaultRow;
    private int ignoreUserSpecifiedReplyColorRow;
    private int ignoreFolderUnreadCountRow;
    private int hideProxyEntryInTitleRow;
    private int showProxyEntryInDrawerRow;
    private int hideFilterMuteAllRow;

    private int devicesRow;
    private int useSystemEmojiRow;
    private int disableVibrationRow;
    private int autoDisableBuiltInProxyRow;
    private int overrideDevicePerformanceRow;
    private int overrideDevicePerformanceDescRow;
    private int devices2Row;

    private int storiesRow;
    private int hideStoriesRow;
    private int stories2Row;

    private int general2Row;


    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("General", R.string.General);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == customTitleRow) {
            setCustomTitle(view, position);
            listAdapter.notifyItemChanged(position, PARTIAL);
        }
        else if (position == showBotAPIRow) {
            Config.toggleShowBotAPIID();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.showBotAPIID);
            }
        } else if (position == hidePhoneRow) {
            Config.toggleHidePhone();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hidePhone);
            }
            parentLayout.rebuildAllFragmentViews(false, false);
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == drawerListRow) {
            showDrawerListAlert();
        } else if (position == avatarAsDrawerBackgroundRow) {
            Config.toggleAvatarAsDrawerBackground();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.avatarAsDrawerBackground);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            TransitionManager.beginDelayedTransition(profilePreviewCell);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (Config.avatarAsDrawerBackground) {
                updateRows();
                listAdapter.notifyItemRangeInserted(avatarBackgroundBlurRow, 2);
            } else {
                listAdapter.notifyItemRangeRemoved(avatarBackgroundBlurRow, 2);
                updateRows();
            }
        } else if (position == avatarBackgroundBlurRow) {
            Config.toggleAvatarBackgroundBlur();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.avatarBackgroundBlur);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == avatarBackgroundDarkenRow) {
            Config.toggleAvatarBackgroundDarken();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.avatarBackgroundDarken);
            }
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
        } else if (position == largeAvatarAsBackgroundRow) {
            Config.toggleLargeAvatarAsBackground();
            getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
            TransitionManager.beginDelayedTransition(profilePreviewCell);
            listAdapter.notifyItemChanged(drawerRow, PARTIAL);
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.largeAvatarAsBackground);
            }
        } else if (position == showExactNumberRow) {
            Config.toggleShowExactNumber();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.showExactNumber);
            }
        } else if (position == showExactTimeRow) {
            Config.toggleShowExactTime();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.showExactTime);
            }
        } else if (position == disableInstantCameraRow) {
            Config.toggleDisableInstantCamera();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.disableInstantCamera);
            }
        } else if (position == disableUndoRow) {
            Config.toggleDisableUndo();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.disableUndo);
            }
        } else if (position == skipOpenLinkConfirmRow) {
            Config.toggleSkipOpenLinkConfirm();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.skipOpenLinkConfirm);
            }
        } else if (position == useSystemEmojiRow) {
            Config.toggleUseSystemEmoji();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.useSystemEmoji);
            }
        } else if (position == disableVibrationRow) {
            Config.toggleDisableVibration();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.disableVibration);
            }
        } else if (position == tabsTitleTypeRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("TabTitleTypeText", R.string.TabTitleTypeText));
            types.add(Defines.tabMenuText);
            arrayList.add(LocaleController.getString("TabTitleTypeIcon", R.string.TabTitleTypeIcon));
            types.add(Defines.tabMenuIcon);
            arrayList.add(LocaleController.getString("TabTitleTypeMix", R.string.TabTitleTypeMix));
            types.add(Defines.tabMenuMix);
            PopupBuilder.show(arrayList, LocaleController.getString("TabTitleType", R.string.TabTitleType), types.indexOf(Config.getTabMenu()), getParentActivity(), view, i -> {
                Config.setTabMenu(types.get(i));
                listAdapter.notifyItemChanged(tabsTitleTypeRow, PARTIAL);
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            });
        } else if (position == overrideDevicePerformanceRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("DevicePerformanceAuto", R.string.DevicePerformanceAuto));
            types.add(Defines.devicePerformanceAuto);
            arrayList.add(LocaleController.getString("DevicePerformanceLow", R.string.DevicePerformanceLow));
            types.add(Defines.devicePerformanceLow);
            arrayList.add(LocaleController.getString("DevicePerformanceMedium", R.string.DevicePerformanceMedium));
            types.add(Defines.devicePerformanceMedium);
            arrayList.add(LocaleController.getString("DevicePerformanceHigh", R.string.DevicePerformanceHigh));
            types.add(Defines.devicePerformanceHigh);
            PopupBuilder.show(arrayList, LocaleController.getString("OverrideDevicePerformance", R.string.OverrideDevicePerformance), types.indexOf(Config.getDevicePerformance()), getParentActivity(), view, i -> {
                Config.setDevicePerformance(types.get(i));
                listAdapter.notifyItemChanged(overrideDevicePerformanceRow, PARTIAL);
            });
        } else if (position == openArchiveOnPullRow) {
            Config.toggleOpenArchiveOnPull();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.openArchiveOnPull);
            }
        } else if (position == hideAllTabRow) {
            Config.toggleHideAllTab();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hideAllTab);
            }
        } else if (position == ignoreMutedCountRow) {
            Config.toggleIgnoreMutedCount();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.ignoreMutedCount);
            }
        } else if (position == disableSharePhoneWithContactByDefaultRow) {
            Config.toggleDisableSharePhoneWithContactByDefault();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.disableSharePhoneWithContactByDefault);
            }
        } else if (position == ignoreUserSpecifiedReplyColorRow) {
            Config.toggleIgnoreUserSpecifiedReplyColor();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.ignoreUserSpecifiedReplyColor);
            }
        } else if (position == ignoreFolderUnreadCountRow) {
            Config.toggleIgnoreFolderUnreadCount();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.ignoreFolderUnreadCount);
            }
        } else if (position == autoDisableBuiltInProxyRow) {
            Config.toggleAutoDisableBuiltInProxy();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.autoDisableBuiltInProxy);
            }
        } else if (position == translationProviderRow) {
            final var oldProvider = TranslateHelper.getCurrentProviderType();
            TranslateHelper.showTranslationProviderSelector(getParentActivity(), view, null, param -> {
                if (param) {
                    listAdapter.notifyItemChanged(translationProviderRow, PARTIAL);
                } else {
                    listAdapter.notifyItemChanged(translationProviderRow, PARTIAL);
                    listAdapter.notifyItemChanged(translationTargetRow, PARTIAL);
                }
                if (!oldProvider.equals(TranslateHelper.getCurrentProviderType())) {
                    boolean wasDeepLTranslator = oldProvider.equals(ProviderType.DeepLTranslator);
                    boolean isDeepLTranslator = TranslateHelper.getCurrentProviderType().equals(ProviderType.DeepLTranslator);
                    boolean wasDeepLxTranslator = oldProvider.equals(ProviderType.DeepLxTranslator);
                    boolean isDeepLxTranslator = TranslateHelper.getCurrentProviderType().equals(ProviderType.DeepLxTranslator);

                    if (wasDeepLTranslator && !isDeepLxTranslator) {
                        listAdapter.notifyItemRemoved(deepLFormalityRow);
                    } else if (wasDeepLxTranslator) {
                        listAdapter.notifyItemRemoved(deepLxApiRow);
                        if (!isDeepLxTranslator) {
                            listAdapter.notifyItemRemoved(deepLFormalityRow);
                        }
                    }

                    updateRows();

                    if (isDeepLTranslator) {
                        listAdapter.notifyItemInserted(deepLFormalityRow);
                    } else if (isDeepLxTranslator) {
                        listAdapter.notifyItemInserted(deepLxApiRow);
                        listAdapter.notifyItemInserted(deepLFormalityRow);
                    }
                }
                return Unit.INSTANCE;
            });
        } else if (position == translationTargetRow) {
            TranslateHelper.showTranslationTargetSelector(this, view, () -> {
                listAdapter.notifyItemChanged(translationTargetRow, PARTIAL);
                if (getRestrictedLanguages().size() == 1) {
                    listAdapter.notifyItemChanged(doNotTranslateRow, PARTIAL);
                }
                return Unit.INSTANCE;
            });
        } else if (position == deepLFormalityRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString("DeepLFormalityDefault", R.string.DeepLFormalityDefault));
            types.add(DeepLTranslator.FORMALITY_DEFAULT);
            arrayList.add(LocaleController.getString("DeepLFormalityMore", R.string.DeepLFormalityMore));
            types.add(DeepLTranslator.FORMALITY_MORE);
            arrayList.add(LocaleController.getString("DeepLFormalityLess", R.string.DeepLFormalityLess));
            types.add(DeepLTranslator.FORMALITY_LESS);
            PopupBuilder.show(arrayList, LocaleController.getString("DeepLFormality", R.string.DeepLFormality),
                types.indexOf(ConfigManager.getIntOrDefault(Defines.deepLFormality, 0)), getParentActivity(), view, i -> {
                    ConfigManager.putInt(Defines.deepLFormality, types.get(i));
                    listAdapter.notifyItemChanged(deepLFormalityRow, PARTIAL);
                });
        } else if (position == deepLxApiRow) {
            EditTextBoldCursor editText = new EditTextBoldCursor(getParentActivity());
            editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            editText.setHint("DeepLx API Url"); // todo: string resource
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editText.setText(Config.getDeepLxApi());
            FrameLayout frameLayout = new FrameLayout(getParentActivity());
            frameLayout.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 16, 0, 16, 0));
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle("DeepLx API Url"); // todo: string resource
            builder.setView(frameLayout);
            builder.setPositiveButton(LocaleController.getString("Save", R.string.Save), (dialogInterface, i) -> {
                Config.setDeepLxApi(editText.getText().toString());
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            builder.show();
        } else if (position == translatorTypeRow) {
            var oldType = TranslateHelper.getCurrentStatus();
            TranslateHelper.showTranslatorTypeSelector(getParentActivity(), view, () -> {
                var newType = TranslateHelper.getCurrentStatus();
                listAdapter.notifyItemChanged(translatorTypeRow, PARTIAL);
                if (oldType != newType) {
                    int count = 3;
                    if (oldType == TranslateHelper.Status.InMessage || newType == TranslateHelper.Status.InMessage) {
                        count++;
                    }
                    if (oldType == TranslateHelper.Status.External) {
                        updateRows();
                        listAdapter.notifyItemRangeInserted(translationProviderRow, count);
                    } else if (newType == TranslateHelper.Status.External) {
                        listAdapter.notifyItemRangeRemoved(translationProviderRow, count);
                        updateRows();
                    } else if (oldType == TranslateHelper.Status.InMessage) {
                        listAdapter.notifyItemRemoved(showOriginalRow);
                        updateRows();
                    } else if (newType == TranslateHelper.Status.InMessage) {
                        updateRows();
                        listAdapter.notifyItemInserted(showOriginalRow);
                    }
                }
                return Unit.INSTANCE;
            });
        } else if (position == doNotTranslateRow) {
            if (!LanguageDetector.hasSupport()) {
                BulletinFactory.of(this).createErrorBulletinSubtitle(LocaleController.getString("BrokenMLKit", R.string.BrokenMLKit), LocaleController.getString("BrokenMLKitDetail", R.string.BrokenMLKitDetail), null).show();
                return;
            }
            presentFragment(new LanguageSelectActivity(LanguageSelectActivity.TYPE_RESTRICTED, true));
        } else if (position == autoTranslateRow) {
            if (!LanguageDetector.hasSupport()) {
                BulletinFactory.of(this).createErrorBulletinSubtitle(LocaleController.getString("BrokenMLKit", R.string.BrokenMLKit), LocaleController.getString("BrokenMLKitDetail", R.string.BrokenMLKitDetail), null).show();
                return;
            }
            TranslateHelper.toggleAutoTranslate();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(TranslateHelper.getAutoTranslate());
            }
        } else if (position == showOriginalRow) {
            TranslateHelper.toggleShowOriginal();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(TranslateHelper.getShowOriginal());
            }
        } else if (position == hideStoriesRow) {
            Config.toggleHideStories();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hideStories);
            }
        } else if (position == hideFilterMuteAllRow) {
            Config.toggleHideFilterMuteAll();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hideFilterMuteAll);
            }
        } else if (position == hideDialogsFloatingButtonRow) {
            Config.toggleHideDialogsFloatingButton();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hideDialogsFloatingButton);
            }
        } else if (position == hideProxyEntryInTitleRow) {
            Config.toggleHideProxyEntryInTitle();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hideProxyEntryInTitle);
            }
        } else if (position == showProxyEntryInDrawerRow) {
            Config.toggleShowProxyEntryInDrawer();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.showProxyEntryInDrawer);
            }
        }
    }

    @Override
    protected boolean onItemLongClick(View view, int position, float x, float y) {
        return false;
    }

    @Override
    protected String getKey() {
        return "g";
    }

    protected void updateRows() {
        super.updateRows();

        drawerRow = addRow();
        avatarAsDrawerBackgroundRow = addRow("avatarAsDrawerBackground");
        if (Config.avatarAsDrawerBackground) {
            avatarBackgroundBlurRow = addRow("avatarBackgroundBlur");
            avatarBackgroundDarkenRow = addRow("avatarBackgroundDarken");
            largeAvatarAsBackgroundRow = addRow("largeAvatarAsBackground");
        } else {
            avatarBackgroundBlurRow = -1;
            avatarBackgroundDarkenRow = -1;
            largeAvatarAsBackgroundRow = -1;
        }
        hidePhoneRow = addRow("hidePhone");
        drawerListRow = addRow("drawerList");
        hideDialogsFloatingButtonRow = addRow("hideDialogsFloatingButton");
        drawer2Row = addRow();

        translatorRow = addRow();
        translatorTypeRow = addRow("translatorType");
        if (TranslateHelper.getCurrentStatus() != TranslateHelper.Status.External) {
            showOriginalRow = TranslateHelper.getCurrentStatus() == TranslateHelper.Status.InMessage ? addRow("showOriginal") : -1;
            translationProviderRow = addRow("translationProvider");
            deepLxApiRow = TranslateHelper.getCurrentProviderType().equals(ProviderType.DeepLxTranslator) ? addRow("deepLxApi") : -1;
            deepLFormalityRow = (TranslateHelper.getCurrentProviderType().equals(ProviderType.DeepLTranslator) || TranslateHelper.getCurrentProviderType().equals(ProviderType.DeepLxTranslator)) ? addRow("deepLFormality") : -1;
            translationTargetRow = addRow("translationTarget");
            doNotTranslateRow = addRow("doNotTranslate");
            autoTranslateRow = addRow("autoTranslate");
        } else {
            showOriginalRow = -1;
            translationProviderRow = -1;
            translationTargetRow = -1;
            deepLFormalityRow = -1;
            doNotTranslateRow = -1;
            autoTranslateRow = -1;
        }
        translator2Row = addRow();


        generalRow = addRow();
        customTitleRow = addRow("customTitle");
        showBotAPIRow = addRow("showBotAPI");
        showExactNumberRow = addRow("showExactNumber");
        showExactTimeRow = addRow("showExactTime");
        disableInstantCameraRow = addRow("disableInstantCamera");
        disableUndoRow = addRow("disableUndo");
        skipOpenLinkConfirmRow = addRow("skipOpenLinkConfirm");
        openArchiveOnPullRow = addRow("openArchiveOnPull");
        hideAllTabRow = addRow("hideAllTab");
        ignoreMutedCountRow = addRow("ignoreMutedCount");
        disableSharePhoneWithContactByDefaultRow = addRow("disableSharePhoneWithContactByDefault");
        ignoreUserSpecifiedReplyColorRow = addRow("ignoreUserSpecifiedReplyColor");
        tabsTitleTypeRow = addRow("tabsTitleType");
        ignoreFolderUnreadCountRow = addRow("ignoreFolderUnreadCount");
        hideFilterMuteAllRow = addRow("hideFilterMuteAll");
        hideProxyEntryInTitleRow = addRow();
        showProxyEntryInDrawerRow = addRow("showProxyEntryInDrawer");
        general2Row = addRow();

        devicesRow = addRow();
        useSystemEmojiRow = addRow("useSystemEmoji");
        autoDisableBuiltInProxyRow = addRow("autoDisableBuiltInProxy");
        disableVibrationRow = addRow("disableVibration");
        overrideDevicePerformanceRow = addRow("overrideDevicePerformance");
        overrideDevicePerformanceDescRow = addRow();
        devices2Row = addRow();

        storiesRow = addRow();
        hideStoriesRow = addRow("hideStories");
        stories2Row = addRow();

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends BaseListAdapter {
        private final DrawerLayoutContainer mDrawerLayoutContainer;

        public ListAdapter(Context context) {
            super(context);
            mDrawerLayoutContainer = new DrawerLayoutContainer(mContext);

        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, boolean payload) {
            switch (holder.getItemViewType()) {
                case 1: {
                    if (position == general2Row) {
                        holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
                case 2: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == tabsTitleTypeRow) {
                        String value = switch (Config.getTabMenu()) {
                            case Defines.tabMenuText -> LocaleController.getString("TabTitleTypeText", R.string.TabTitleTypeText);
                            case Defines.tabMenuIcon -> LocaleController.getString("TabTitleTypeIcon", R.string.TabTitleTypeIcon);
                            case Defines.tabMenuMix -> LocaleController.getString("TabTitleTypeMix", R.string.TabTitleTypeMix);
                            default -> LocaleController.getString("TabTitleTypeMix", R.string.TabTitleTypeMix);
                        };
                        textCell.setTextAndValue(LocaleController.getString("TabTitleType", R.string.TabTitleType), value, payload, false);
                    } else if (position == overrideDevicePerformanceRow) {
                        String value = switch (Config.getDevicePerformance()) {
                            case Defines.devicePerformanceLow -> LocaleController.getString("DevicePerformanceLow", R.string.DevicePerformanceLow);
                            case Defines.devicePerformanceMedium -> LocaleController.getString("DevicePerformanceMedium", R.string.DevicePerformanceMedium);
                            case Defines.devicePerformanceHigh -> LocaleController.getString("DevicePerformanceHigh", R.string.DevicePerformanceHigh);
                            default -> LocaleController.getString("DevicePerformanceAuto", R.string.DevicePerformanceAuto);
                        };
                        textCell.setTextAndValue(LocaleController.getString("OverrideDevicePerformance", R.string.OverrideDevicePerformance), value, payload, false);
                    } else if (position == translationProviderRow) {
                        Pair<ArrayList<String>, ArrayList<TranslateHelper.ProviderType>> providers = TranslateHelper.getProviders();
                        ArrayList<String> names = providers.first;
                        ArrayList<TranslateHelper.ProviderType> types = providers.second;
                        if (names == null || types == null) {
                            return;
                        }
                        int index = types.indexOf(TranslateHelper.getCurrentProviderType());
                        if (index < 0) {
                            textCell.setTextAndValue(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), "", payload, true);
                        } else {
                            String value = names.get(index);
                            textCell.setTextAndValue(LocaleController.getString("TranslationProviderShort", R.string.TranslationProviderShort), value, payload, true);
                        }
                    } else if (position == translationTargetRow) {
                        String language = TranslateHelper.getCurrentTargetLanguage();
                        CharSequence value;
                        if (language.equals("app")) {
                            value = LocaleController.getString("TranslationTargetApp", R.string.TranslationTargetApp);
                        } else {
                            Locale locale = Locale.forLanguageTag(language);
                            if (!TextUtils.isEmpty(locale.getScript())) {
                                value = HtmlCompat.fromHtml(locale.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY);
                            } else {
                                value = locale.getDisplayName();
                            }
                        }
                        textCell.setTextAndValue(LocaleController.getString("TranslationTarget", R.string.TranslationTarget), value, payload, true);
                    } else if (position == deepLFormalityRow) {
                        String value;
                        switch (ConfigManager.getIntOrDefault(Defines.deepLFormality, 0)) {
                            case DeepLTranslator.FORMALITY_DEFAULT:
                            default:
                                value = LocaleController.getString("DeepLFormalityDefault", R.string.DeepLFormalityDefault);
                                break;
                            case DeepLTranslator.FORMALITY_MORE:
                                value = LocaleController.getString("DeepLFormalityMore", R.string.DeepLFormalityMore);
                                break;
                            case DeepLTranslator.FORMALITY_LESS:
                                value = LocaleController.getString("DeepLFormalityLess", R.string.DeepLFormalityLess);
                                break;
                        }
                        textCell.setTextAndValue(LocaleController.getString("DeepLFormality", R.string.DeepLFormality), value, payload, true);
                    } else if (position == translatorTypeRow) {
                        String value;
                        switch (TranslateHelper.getCurrentStatus()) {
                            case Popup:
                                value = LocaleController.getString("TranslatorTypePopup", R.string.TranslatorTypePopup);
                                break;
                            case External:
                                value = LocaleController.getString("TranslatorTypeExternal", R.string.TranslatorTypeExternal);
                                break;
                            case InMessage:
                            default:
                                value = LocaleController.getString("TranslatorTypeInMessage", R.string.TranslatorTypeInMessage);
                                break;
                        }
                        textCell.setTextAndValue(LocaleController.getString("TranslatorType", R.string.TranslatorType), value, payload, position + 1 != translator2Row);
                    } else if (position == doNotTranslateRow) {
                        ArrayList<String> langCodes = getRestrictedLanguages();
                        CharSequence value;
                        if (langCodes.size() == 1) {
                            Locale locale = Locale.forLanguageTag(langCodes.get(0));
                            if (!TextUtils.isEmpty(locale.getScript())) {
                                value = HtmlCompat.fromHtml(locale.getDisplayScript(), HtmlCompat.FROM_HTML_MODE_LEGACY);
                            } else {
                                value = locale.getDisplayName();
                            }
                        } else {
                            value = LocaleController.formatPluralString("Languages", langCodes.size());
                        }
                        textCell.setTextAndValue(LocaleController.getString("DoNotTranslate", R.string.DoNotTranslate), value, payload, true);
                    } else if (position == customTitleRow) {
                        textCell.setTextAndValue(LocaleController.getString("customTitle", R.string.customTitle), Config.getCustomTitle(), payload, true);
                    } else if (position == drawerListRow) {
                        textCell.setText(LocaleController.getString("drawerList", R.string.drawerList), false);
                    } else if (position == deepLxApiRow) {
                        String value = Config.getDeepLxApi();
                        if (value.length() > 25) {
                            value = value.substring(0, 25) + "...";
                        }
                        textCell.setTextAndValue(LocaleController.getString(R.string.DeepLxApi), value, payload, true);
                    }
                    break;
                }
                case 3: {
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    textCell.setEnabled(true, null);
                    if (position == showBotAPIRow) {
                        textCell.setTextAndCheck(LocaleController.getString("showBotAPIID", R.string.showBotAPIID), Config.showBotAPIID, true);
                    } else if (position == hidePhoneRow) {
                        textCell.setTextAndCheck(LocaleController.getString("hidePhone", R.string.hidePhone), Config.hidePhone, true);
                    } else if (position == showExactNumberRow) {
                        textCell.setTextAndCheck(LocaleController.getString("showExactNumber", R.string.showExactNumber), Config.showExactNumber, true);
                    } else if (position == showExactTimeRow) {
                        textCell.setTextAndCheck(LocaleController.getString("showExactTime", R.string.showExactTime), Config.showExactTime, true);
                    } else if (position == disableInstantCameraRow) {
                        textCell.setTextAndCheck(LocaleController.getString("disableInstantCamera", R.string.disableInstantCamera), Config.disableInstantCamera, true);
                    } else if (position == disableUndoRow) {
                        textCell.setTextAndCheck(LocaleController.getString("disableUndo", R.string.disableUndo), Config.disableUndo, true);
                    } else if (position == skipOpenLinkConfirmRow) {
                        textCell.setTextAndCheck(LocaleController.getString("skipOpenLinkConfirm", R.string.skipOpenLinkConfirm), Config.skipOpenLinkConfirm, true);
                    } else if (position == avatarAsDrawerBackgroundRow) {
                        textCell.setTextAndCheck(LocaleController.getString("AvatarAsBackground", R.string.AvatarAsBackground), Config.avatarAsDrawerBackground, true);
                    } else if (position == avatarBackgroundBlurRow) {
                        textCell.setTextAndCheck(LocaleController.getString("BlurAvatarBackground", R.string.BlurAvatarBackground), Config.avatarBackgroundBlur, true);
                    } else if (position == avatarBackgroundDarkenRow) {
                        textCell.setTextAndCheck(LocaleController.getString("DarkenAvatarBackground", R.string.DarkenAvatarBackground), Config.avatarBackgroundDarken, true);
                    } else if (position == largeAvatarAsBackgroundRow) {
                        textCell.setTextAndCheck(LocaleController.getString("LargeAvatarAsBackground", R.string.largeAvatarAsBackground), Config.largeAvatarAsBackground, true);
                    } else if (position == useSystemEmojiRow) {
                        textCell.setTextAndCheck(LocaleController.getString("UseSystemEmoji", R.string.useSystemEmoji), Config.useSystemEmoji, true);
                    } else if (position == disableVibrationRow) {
                        textCell.setTextAndCheck(LocaleController.getString("DisableVibration", R.string.disableVibration), Config.disableVibration, true);
                    } else if (position == openArchiveOnPullRow) {
                        textCell.setTextAndCheck(LocaleController.getString("openArchiveOnPull", R.string.openArchiveOnPull), Config.openArchiveOnPull, true);
                    } else if (position == hideAllTabRow) {
                        textCell.setTextAndCheck(LocaleController.getString("hideAllTab", R.string.hideAllTab), Config.hideAllTab, true);
                    } else if (position == ignoreMutedCountRow) {
                        textCell.setTextAndCheck(LocaleController.getString("ignoreMutedCount", R.string.ignoreMutedCount),
                            Config.ignoreMutedCount, true);
                    } else if (position == disableSharePhoneWithContactByDefaultRow) {
                        textCell.setTextAndCheck(LocaleController.getString("disableSharePhoneWithContactByDefault", R.string.disableSharePhoneWithContactByDefault),
                            Config.disableSharePhoneWithContactByDefault, true);
                    } else if (position == ignoreUserSpecifiedReplyColorRow) {
                        textCell.setTextAndCheck(LocaleController.getString("ignoreUserSpecifiedReplyColor", R.string.ignoreUserSpecifiedReplyColor),
                            Config.ignoreUserSpecifiedReplyColor, true);
                    } else if (position == autoDisableBuiltInProxyRow) {
                        textCell.setTextAndValueAndCheck(LocaleController.getString("autoDisableBuiltInProxy", R.string.autoDisableBuiltInProxy),
                            LocaleController.getString("autoDisableBuiltInProxyDesc", R.string.autoDisableBuiltInProxyDesc),
                            Config.autoDisableBuiltInProxy, true, true);
                    } else if (position == autoTranslateRow) {
                        textCell.setEnabled(LanguageDetector.hasSupport(), null);
                        textCell.setTextAndValueAndCheck(LocaleController.getString("AutoTranslate", R.string.AutoTranslate),
                            LocaleController.getString("AutoTranslateAbout",
                                R.string.AutoTranslateAbout), TranslateHelper.getAutoTranslate(), true, false);
                    } else if (position == showOriginalRow) {
                        textCell.setTextAndCheck(LocaleController.getString("TranslatorShowOriginal", R.string.TranslatorShowOriginal), TranslateHelper.getShowOriginal(), true);
                    } else if (position == hideStoriesRow) {
                        textCell.setTextAndCheck(LocaleController.getString("HideStories", R.string.HideStories), Config.hideStories, true);
                    } else if (position == ignoreFolderUnreadCountRow) {
                        textCell.setTextAndCheck(LocaleController.getString("ignoreFolderUnreadCount", R.string.ignoreFolderUnreadCount), Config.ignoreFolderUnreadCount, true);
                    } else if (position == hideFilterMuteAllRow) {
                        textCell.setTextAndCheck(LocaleController.getString("hideFilterMuteAll", R.string.hideFilterMuteAll), Config.hideFilterMuteAll, true);
                    } else if (position == hideDialogsFloatingButtonRow) {
                        textCell.setTextAndCheck(LocaleController.getString("hideDialogsFloatingButton", R.string.hideDialogsFloatingButton), Config.hideDialogsFloatingButton, true);
                    } else if (position == hideProxyEntryInTitleRow) {
                        textCell.setTextAndCheck(LocaleController.getString(R.string.hideProxyEntryInTitle), Config.hideProxyEntryInTitle, true);
                    } else if (position == showProxyEntryInDrawerRow) {
                        textCell.setTextAndCheck(LocaleController.getString(R.string.showProxyEntryInDrawer), Config.showProxyEntryInDrawer, true);
                    }
                    break;
                }
                case 4: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == generalRow) {
                        headerCell.setText(LocaleController.getString("General", R.string.General));
                    } else if (position == translatorRow) {
                        headerCell.setText(LocaleController.getString("Translator", R.string.Translator));
                    } else if (position == devicesRow) {
                        headerCell.setText(LocaleController.getString("Devices", R.string.Devices));
                    } else if (position == storiesRow) {
                        headerCell.setText(LocaleController.getString("Stories", R.string.Stories));
                    }
                    break;
                }
                case 5: {
                    NotificationsCheckCell textCell = (NotificationsCheckCell) holder.itemView;
                    break;
                }
                case 7: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == overrideDevicePerformanceDescRow) {
                        cell.setText(LocaleController.getString("OverrideDevicePerformanceDesc", R.string.OverrideDevicePerformanceDesc));
                    }
                    break;
                }
                case 8: {
                    DrawerProfilePreviewCell cell = (DrawerProfilePreviewCell) holder.itemView;
                    cell.setUser(getUserConfig().getCurrentUser(), false);
                    break;
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 2 || type == 3 || type == 6 || type == 5;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 1:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 2:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new NotificationsCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 7:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 8:
                    profilePreviewCell = new DrawerProfilePreviewCell(mContext);
                    profilePreviewCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    profilePreviewCell.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
                    return new RecyclerListView.Holder(profilePreviewCell);
            }
            //noinspection ConstantConditions
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == general2Row || position == drawer2Row || position == translator2Row || position == devices2Row || position == stories2Row) {
                return 1;
            } else if (position == tabsTitleTypeRow || position == translationProviderRow || position == deepLFormalityRow || position == translationTargetRow ||
                position == translatorTypeRow || position == doNotTranslateRow || position == overrideDevicePerformanceRow || position == customTitleRow ||
                position == drawerListRow || position == deepLxApiRow) {
                return 2;
            } else if (position == generalRow || position == translatorRow || position == devicesRow || position == storiesRow) {
                return 4;
            } else if (position == overrideDevicePerformanceDescRow) {
                return 7;
            } else if (position == drawerRow) {
                return 8;
            } else if ((position > generalRow && position < general2Row) ||
                    (position > devicesRow && position < devices2Row) ||
                    (position > drawerRow && position < drawer2Row) ||
                    (position > translatorRow && position < translator2Row) ||
                    (position > storiesRow && position < stories2Row)) {
                return 3;
            }
            return -1;
        }
    }

    private ArrayList<String> getRestrictedLanguages() {
        String currentLang = TranslateHelper.stripLanguageCode(TranslateHelper.getCurrentProvider().getCurrentTargetLanguage());
        ArrayList<String> langCodes = new ArrayList<>(TranslateHelper.getRestrictedLanguages());
        if (!langCodes.contains(currentLang)) {
            langCodes.add(currentLang);
        }
        return langCodes;
    }

    private void setCustomTitle(View view, int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("setCustomTitle", R.string.setCustomTitle));

        final EditTextBoldCursor editText = new EditTextBoldCursor(getParentActivity()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64), MeasureSpec.EXACTLY));
            }
        };
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        editText.setHintText(LocaleController.getString("AppName", R.string.AppName));
        editText.setText(Config.getCustomTitle());
        editText.setHeaderHintColor(getThemedColor(Theme.key_windowBackgroundWhiteBlueHeader));
        editText.setSingleLine(true);
        editText.setFocusable(true);
        editText.setTransformHintToHeader(true);
        editText.setLineColors(getThemedColor(Theme.key_windowBackgroundWhiteInputField), getThemedColor(Theme.key_windowBackgroundWhiteInputFieldActivated), getThemedColor(Theme.key_text_RedRegular));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setBackgroundDrawable(null);
        editText.requestFocus();
        editText.setPadding(0, 0, 0, 0);
        builder.setView(editText);

        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> {
            if (editText.getText().toString().trim().isEmpty()) {
                Config.setCustomTitle(LocaleController.getString("AppName", R.string.AppName));
            } else {
                Config.setCustomTitle(editText.getText().toString().trim());
            }
            listAdapter.notifyItemChanged(pos, PARTIAL);
        });
        builder.setNeutralButton(LocaleController.getString("Default", R.string.Default), (dialogInterface, i) -> {
            Config.setCustomTitle(LocaleController.getString("AppName", R.string.AppName));
            listAdapter.notifyItemChanged(pos, PARTIAL);
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        builder.show().setOnShowListener(dialog -> {
            editText.requestFocus();
            AndroidUtilities.showKeyboard(editText);
        });
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) editText.getLayoutParams();
        if (layoutParams != null) {
            if (layoutParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) layoutParams).gravity = Gravity.CENTER_HORIZONTAL;
            }
            layoutParams.rightMargin = layoutParams.leftMargin = AndroidUtilities.dp(24);
            layoutParams.height = AndroidUtilities.dp(36);
            editText.setLayoutParams(layoutParams);
        }
        editText.setSelection(0, editText.getText().length());
    }

    private void showDrawerListAlert() {
        if (getParentActivity() == null) {
            return;
        }
        Context context = getParentActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("drawerList", R.string.drawerList));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearLayoutInviteContainer = new LinearLayout(context);
        linearLayoutInviteContainer.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayoutInviteContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        boolean isPremium = UserConfig.getInstance(UserConfig.selectedAccount).isPremium();
        int count = isPremium ? 8 : 6;
        for (int a = 0; a < count; a++) {
            if (a == 3) {
                continue;
            }
            TextCheckCell textCell = new TextCheckCell(context);
            switch (a) {
                case 0 -> textCell.setTextAndCheck(LocaleController.getString("NewGroup", R.string.NewGroup), Config.showNewGroup,  false);
                case 1 -> textCell.setTextAndCheck(LocaleController.getString("Contacts", R.string.Contacts), Config.showContacts,  false);
                case 2 -> textCell.setTextAndCheck(LocaleController.getString("Calls", R.string.Calls), Config.showCalls,  false);
//                case 3 -> textCell.setTextAndCheck(LocaleController.getString("PeopleNearby", R.string.PeopleNearby), Config.showPeopleNearby,  false);
                case 4 -> textCell.setTextAndCheck(LocaleController.getString("SavedMessages", R.string.SavedMessages), Config.showSavedMessages,  false);
                case 5 -> textCell.setTextAndCheck(LocaleController.getString("ArchivedChats", R.string.ArchivedChats), Config.showArchivedChats, false);
                case 6 -> textCell.setTextAndCheck(LocaleController.getString("ChangeEmojiStatus", R.string.ChangeEmojiStatus), Config.showChangeEmojiStatus,  false);
                case 7 -> textCell.setTextAndCheck(LocaleController.getString("MyProfile", R.string.MyProfile), Config.showProfileMyStories,  false);
            }
            textCell.setTag(a);
            textCell.setBackground(Theme.getSelectorDrawable(false));
            linearLayoutInviteContainer.addView(textCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            textCell.setOnClickListener(v2 -> {
                Integer tag = (Integer) v2.getTag();
                switch (tag) {
                    case 0 -> {
                        Config.toggleShowNewGroup();
                        textCell.setChecked(Config.showNewGroup);
                    }
                    case 1 -> {
                        Config.toggleShowContacts();
                        textCell.setChecked(Config.showContacts);
                    }
                    case 2 -> {
                        Config.toggleShowCalls();
                        textCell.setChecked(Config.showCalls);
                    }
                    case 3 -> {
                        Config.toggleShowPeopleNearby();
                        textCell.setChecked(Config.showPeopleNearby);
                    }
                    case 4 -> {
                        Config.toggleShowSavedMessages();
                        textCell.setChecked(Config.showSavedMessages);
                    }
                    case 5 -> {
                        Config.toggleShowArchivedChats();
                        textCell.setChecked(Config.showArchivedChats);
                    }
                    case 6 -> {
                        Config.toggleShowChangeEmojiStatus();
                        textCell.setChecked(Config.showChangeEmojiStatus);
                    }
                    case 7 -> {
                        Config.toggleShowProfileMyStories();
                        textCell.setChecked(Config.showProfileMyStories);
                    }
                }
            });
        }
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        builder.setView(linearLayout);
        showDialog(builder.create());
    }
}
