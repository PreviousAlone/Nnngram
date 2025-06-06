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

package xyz.nextalone.nnngram.activity;

import static org.telegram.messenger.AndroidUtilities.getSystemProperty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;

import java.util.ArrayList;

import xyz.nextalone.gen.Config;
import xyz.nextalone.nnngram.config.ConfigManager;
import xyz.nextalone.nnngram.ui.PopupBuilder;
import xyz.nextalone.nnngram.utils.Defines;
import xyz.nextalone.nnngram.utils.Log;

@SuppressLint("NotifyDataSetChanged")
public class ExperimentSettingActivity extends BaseActivity {


    private int experimentRow;
    private int disableFilteringRow;
    private int blockSponsorAdsRow;
    private int hideProxySponsorChannelRow;
    private int disableSendTypingRow;
    private int storyStealthModeRow;
    private int keepOnlineStatusAsRow;
    private int aliasChannelRow;
    private int keepFormattingRow;
    private int enchantAudioRow;
    private int linkedUserRow;
    private int overrideChannelAliasRow;
    private int showRPCErrorRow;
    private int enableXiaomiHyperAiRow;

    private int specialRow;
    private int special2Row;

    private int panguRow;
    private int enablePanguOnSendingRow;
    private int enablePanguOnReceivingRow;
    private int pangu2Row;
    private int pangu3Row;

    private int premiumRow;
    private int hidePremiumStickerAnimRow;
    private int fastSpeedUploadRow;
    private int modifyDownloadSpeedRow;
    private int ignoreChatStrictRow;
    private int premium2Row;
    private int alwaysSendWithoutSoundRow;
    private int playerDecoderRow;

    private int experiment2Row;

    private boolean sensitiveEnabled;
    private final boolean sensitiveCanChange;

    public ExperimentSettingActivity(boolean sensitiveEnabled, boolean sensitiveCanChange) {
        this.sensitiveEnabled = sensitiveEnabled;
        this.sensitiveCanChange = sensitiveCanChange;
    }


    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == blockSponsorAdsRow) {
            Config.toggleBlockSponsorAds();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.blockSponsorAds);
            }
        } else if (position == hideProxySponsorChannelRow) {
            Config.toggleHideProxySponsorChannel();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hideProxySponsorChannel);
            }
        } else if (position == disableSendTypingRow) {
            Config.toggleDisableSendTyping();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.disableSendTyping);
            }
        } else if (position == disableFilteringRow) {
            sensitiveEnabled = !sensitiveEnabled;
            TLRPC.TL_account_setContentSettings req = new TLRPC.TL_account_setContentSettings();
            req.sensitive_enabled = sensitiveEnabled;
            AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.show();
            getConnectionsManager().sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                progressDialog.dismiss();
                if (error == null) {
                    if (response instanceof TLRPC.TL_boolTrue && view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(sensitiveEnabled);
                    }
                } else {
                    AndroidUtilities.runOnUIThread(() -> AlertsCreator.processError(currentAccount, error, this, req));
                }
            }));
        } else if (position == aliasChannelRow) {
            boolean currentStatus = Config.channelAlias;
            if (!currentStatus && !Config.labelChannelUser) {
                Config.setLabelChannelUser(true);
            }
            Config.toggleChannelAlias();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.channelAlias);
            }
        } else if (position == keepFormattingRow) {
            Config.toggleKeepCopyFormatting();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.keepCopyFormatting);
            }
        } else if (position == enchantAudioRow) {
            Config.toggleEnchantAudio();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.enchantAudio);
            }
        } else if (position == linkedUserRow) {
            Config.toggleLinkedUser();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.linkedUser);
            }
        } else if (position == overrideChannelAliasRow) {
            Config.toggleOverrideChannelAlias();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.overrideChannelAlias);
            }
        } else if (position == hidePremiumStickerAnimRow) {
            Config.toggleHidePremiumStickerAnim();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.hidePremiumStickerAnim);
            }
        } else if (position == fastSpeedUploadRow) {
            Config.toggleFastSpeedUpload();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.fastSpeedUpload);
            }
        } else if (position == modifyDownloadSpeedRow) {
            int[] speeds = new int[]{128, 256, 384, 512, 768, 1024};
            ArrayList<String> speedsStr = new ArrayList<>();
            for (int speed : speeds) {
                speedsStr.add(speed + " Kb/block");
            }
            PopupBuilder.show(speedsStr, LocaleController.getString("modifyDownloadSpeed", R.string.modifyDownloadSpeed),
                speedsStr.indexOf(Config.getModifyDownloadSpeed() + " Kb/block"), getParentActivity(), view, i -> {
                    Log.i("speeds[i]: " + speeds[i]);
                    Log.i("i: " + i);
                    Config.setModifyDownloadSpeed(speeds[i]);
                    listAdapter.notifyItemChanged(modifyDownloadSpeedRow, PARTIAL);
                });
        } else if (position == keepOnlineStatusAsRow) {
            ArrayList<String> str = new ArrayList<>();
            str.add(LocaleController.getString("Default", R.string.Default));
            str.add(LocaleController.getString("Online", R.string.Online));
            str.add(LocaleController.getString("Offline", R.string.Offline));
            PopupBuilder.show(str, LocaleController.getString("keepOnlineStatusAsRow", R.string.keepOnlineStatusAs),
                Config.getKeepOnlineStatusAs(), getParentActivity(), view, i -> {
                    Config.setKeepOnlineStatusAs(i);

                    TL_account.updateStatus req = new TL_account.updateStatus();
                    req.offline = i == 2;
                    ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, null);

                    listAdapter.notifyItemChanged(keepOnlineStatusAsRow, PARTIAL);
                });
        } else if (position == alwaysSendWithoutSoundRow) {
            Config.toggleAlwaysSendWithoutSound();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.alwaysSendWithoutSound);
            }
        } else if (position == showRPCErrorRow) {
            Config.toggleShowRPCError();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.showRPCError);
            }
        } else if (position == enablePanguOnSendingRow) {
            Config.toggleEnablePanguOnSending();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.enablePanguOnSending);
            }
        } else if (position == enablePanguOnReceivingRow) {
            Config.toggleEnablePanguOnReceiving();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.enablePanguOnReceiving);
            }
        } else if (position == storyStealthModeRow) {
            Config.toggleStoryStealthMode();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.storyStealthMode);
            }
        } else if (position == ignoreChatStrictRow) {
            Config.toggleIgnoreChatStrict();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.ignoreChatStrict);
            }
        } else if (position == enableXiaomiHyperAiRow) {
            Config.toggleEnableXiaomiHyperAi();
            if (view instanceof TextCheckCell) {
                ((TextCheckCell) view).setChecked(Config.enableXiaomiHyperAi);
            }
        } else if (position == playerDecoderRow) {
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<Integer> types = new ArrayList<>();
            arrayList.add(LocaleController.getString(R.string.PlayerDecoderSoftware));
            types.add(Defines.playerDecoderSoftware);
            arrayList.add(LocaleController.getString(R.string.PlayerDecoderHardware));
            types.add(Defines.playerDecoderHardware);
            arrayList.add(LocaleController.getString(R.string.PlayerDecoderPerferHW));
            types.add(Defines.playerDecoderPerferHW);
            PopupBuilder.show(arrayList, LocaleController.getString(R.string.PlayerDecoder), types.indexOf(Config.getPlayerDecoder()), getParentActivity(), view, i -> {
                Config.setPlayerDecoder(types.get(i));
                listAdapter.notifyItemChanged(playerDecoderRow, PARTIAL);
            });
        }

    }

    @Override
    protected boolean onItemLongClick(View view, int position, float x, float y) {
        // ignore
        return false;
    }

    @Override
    protected String getKey() {
        return "e";
    }

    @Override
    protected void updateRows() {
        super.updateRows();

        experimentRow = addRow();
        disableFilteringRow = sensitiveCanChange ? addRow("disableFiltering") : -1;
        aliasChannelRow = addRow("aliasChannel");
        keepFormattingRow = addRow("keepFormatting");
        enchantAudioRow = addRow("enchantAudio");
        linkedUserRow = addRow("linkedUser");
        alwaysSendWithoutSoundRow = addRow();
        playerDecoderRow = addRow();
        if (Config.linkedUser && Config.labelChannelUser) {
            overrideChannelAliasRow = addRow("overrideChannelAlias");
        } else {
            overrideChannelAliasRow = -1;
        }
        var user = UserConfig.getInstance(currentAccount).getCurrentUser();
        showRPCErrorRow = user != null && user.developer() ? addRow("showRPCError") : -1;
        if (getSystemProperty("ro.mi.os.version.name") != null) {
            enableXiaomiHyperAiRow = addRow("enableXiaomiHyperAi");
        }
        experiment2Row = addRow();

        if (Config.showHiddenSettings) {
            specialRow = addRow();
            blockSponsorAdsRow = addRow("blockSponsorAds");
            hideProxySponsorChannelRow = addRow("hideProxySponsorChannel");
            disableSendTypingRow = addRow("disableSendTyping");
            keepOnlineStatusAsRow = addRow("keepOnlineStatusAs");
            storyStealthModeRow = addRow("storyStealthMode");
            special2Row = addRow();
        }

        panguRow = addRow();
        enablePanguOnSendingRow = addRow("enablePanguOnSending");
        enablePanguOnReceivingRow = addRow("enablePanguOnReceiving");
        pangu3Row = addRow();
        pangu2Row = addRow();

        if (Config.showHiddenSettings) {
            premiumRow = addRow();
            hidePremiumStickerAnimRow = addRow("hidePremiumStickerAnim");
            fastSpeedUploadRow = addRow("fastSpeedUpload");
            modifyDownloadSpeedRow = addRow("modifyDownloadSpeed");
            premium2Row = addRow();
            ignoreChatStrictRow = addRow("ignoreChatStrict");
        }

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("Experiment", R.string.Experiment);
    }

    private class ListAdapter extends BaseListAdapter {

        public ListAdapter(Context context) {
            super(context);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, boolean payload) {
            switch (holder.getItemViewType()) {
                case TYPE_SHADOW: {
                    if (position == experiment2Row) {
                        holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }
                case TYPE_SETTINGS: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                    if (position == modifyDownloadSpeedRow) {
                        textCell.setTextAndValue(LocaleController.getString("modifyDownloadSpeed", R.string.modifyDownloadSpeed),
                            Config.getModifyDownloadSpeed() + " Kb/block", payload, false);
                    } else if (position == keepOnlineStatusAsRow) {
                        String value = switch (Config.getKeepOnlineStatusAs()) {
                            case 0 -> LocaleController.getString("Default", R.string.Default);
                            case 1 -> LocaleController.getString("Online", R.string.Online);
                            case 2 -> LocaleController.getString("Offline", R.string.Offline);
                            default -> throw new IllegalStateException("Unexpected value: " + ConfigManager.getIntOrDefault(Defines.keepOnlineStatusAs, 0));
                        };
                        textCell.setTextAndValue(LocaleController.getString("keepOnlineStatusAs", R.string.keepOnlineStatusAs),
                            value, payload, false);
                    }
                    break;
                }
                case TYPE_CHECK: {
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    textCell.setEnabled(true, null);
                    if (position == blockSponsorAdsRow) {
                        textCell.setTextAndCheck(LocaleController.getString("blockSponsorAds", R.string.blockSponsorAds), Config.blockSponsorAds,
                            true);
                    } else if (position == hideProxySponsorChannelRow) {
                        textCell.setTextAndCheck(LocaleController.getString("hideProxySponsorChannel", R.string.hideProxySponsorChannel),
                            Config.hideProxySponsorChannel, true);
                    } else if (position == disableSendTypingRow) {
                        textCell.setTextAndCheck(LocaleController.getString("disableSendTyping", R.string.disableSendTyping),
                            Config.disableSendTyping, true);
                    } else if (position == storyStealthModeRow) {
                        textCell.setTextAndCheck(LocaleController.getString("storyStealthMode", R.string.storyStealthMode),
                            Config.storyStealthMode, true);
                    } else if (position == disableFilteringRow) {
                        textCell.setTextAndValueAndCheck(LocaleController.getString("SensitiveDisableFiltering", R.string.SensitiveDisableFiltering),
                            LocaleController.getString("SensitiveAbout", R.string.SensitiveAbout), sensitiveEnabled, true, true);
                        textCell.setEnabled(sensitiveCanChange, null);
                    } else if (position == aliasChannelRow) {
                        textCell.setTextAndValueAndCheck(LocaleController.getString("channelAlias", R.string.channelAlias),
                            LocaleController.getString("channelAliasDetails", R.string.channelAliasDetails), Config.channelAlias, true, true);
                    } else if (position == keepFormattingRow) {
                        textCell.setTextAndCheck(LocaleController.getString("keepFormatting", R.string.keepFormatting), Config.keepCopyFormatting,
                            true);
                    } else if (position == enchantAudioRow) {
                        textCell.setTextAndValueAndCheck(LocaleController.getString("enchantAudioRow", R.string.enchantAudio),
                            LocaleController.getString("enchantAudioDetails", R.string.enchantAudioDetails), Config.enchantAudio, true, true);
                    } else if (position == linkedUserRow) {
                        textCell.setTextAndValueAndCheck(LocaleController.getString("linkUser", R.string.linkUser),
                            LocaleController.getString("linkUserDetails", R.string.linkUserDetails), Config.linkedUser, true, true);
                    } else if (position == overrideChannelAliasRow) {
                        textCell.setTextAndValueAndCheck(
                            Config.channelAlias ?
                                LocaleController.getString("overrideChannelAlias", R.string.overrideChannelAlias) :
                                LocaleController.getString("labelChannelInChat", R.string.labelChannelInChat),
                            LocaleController.getString("overrideChannelAliasDetails", R.string.overrideChannelAliasDetails),
                            Config.overrideChannelAlias, true, true);
                    } else if (position == hidePremiumStickerAnimRow) {
                        textCell.setTextAndCheck(LocaleController.getString("hidePremiumStickerAnim", R.string.hidePremiumStickerAnim),
                            Config.hidePremiumStickerAnim, true);
                    } else if (position == fastSpeedUploadRow) {
                        textCell.setTextAndCheck(LocaleController.getString("fastSpeedUpload", R.string.fastSpeedUpload), Config.fastSpeedUpload,
                            true);
                    } else if (position == alwaysSendWithoutSoundRow) {
                        textCell.setTextAndCheck(LocaleController.getString("alwaysSendWithoutSound", R.string.alwaysSendWithoutSound),
                            Config.alwaysSendWithoutSound, true);
                    } else if (position == showRPCErrorRow) {
                        textCell.setTextAndCheck(LocaleController.getString("showRPCError", R.string.showRPCError), Config.showRPCError, true);
                    } else if (position == enablePanguOnSendingRow) {
                        textCell.setTextAndCheck(LocaleController.getString("enablePanguOnSending", R.string.enablePanguOnSending), Config.enablePanguOnSending,
                            true);
                    } else if (position == enablePanguOnReceivingRow) {
                        textCell.setTextAndCheck(LocaleController.getString("enablePanguOnReceiving", R.string.enablePanguOnReceiving), Config.enablePanguOnReceiving, true);
                    } else if (position == ignoreChatStrictRow) {
                        textCell.setTextAndCheck("", Config.ignoreChatStrict, true);
                    } else if (position == enableXiaomiHyperAiRow) {
                        textCell.setTextAndCheck(LocaleController.getString(R.string.enableXiaomiHyperAi), Config.enableXiaomiHyperAi, true);
                    } else if (position == playerDecoderRow) {
                        String value = switch (Config.getPlayerDecoder()) {
                            case Defines.playerDecoderHardware -> LocaleController.getString(R.string.PlayerDecoderHardware);
                            case Defines.playerDecoderPerferHW -> LocaleController.getString(R.string.PlayerDecoderPerferHW);
                            default -> LocaleController.getString(R.string.PlayerDecoderSoftware);
                        };
                        textCell.setTextAndValue(LocaleController.getString(R.string.PlayerDecoder), value, payload, false);
                    }
                    break;
                }
                case TYPE_HEADER: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == experimentRow) {
                        headerCell.setText(LocaleController.getString("Experiment", R.string.Experiment));
                    } else if (position == premiumRow) {
                        headerCell.setText(LocaleController.getString("Premium", R.string.premium));
                    } else if (position == panguRow) {
                        headerCell.setText(LocaleController.getString("pangu", R.string.pangu));
                    } else if (position == specialRow) {
                        headerCell.setText(LocaleController.getString("Special", R.string.Special));
                    }
                    break;
                }
                case TYPE_NOTIFICATION_CHECK: {
                    NotificationsCheckCell textCell = (NotificationsCheckCell) holder.itemView;
                    break;
                }
                case TYPE_INFO_PRIVACY: {
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (position == pangu3Row) {
                        cell.getTextView().setMovementMethod(null);
                        cell.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                        cell.setText(LocaleController.getString("panguInfo", R.string.panguInfo));
                    }
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == experiment2Row || position == premium2Row || position == pangu2Row || position == special2Row) {
                return TYPE_SHADOW;
            } else if (position == modifyDownloadSpeedRow || position == keepOnlineStatusAsRow) {
                return TYPE_SETTINGS;
            } else if (position == experimentRow || position == premiumRow || position == panguRow || position == specialRow) {
                return TYPE_HEADER;
            } else if (position == pangu3Row) {
                return TYPE_INFO_PRIVACY;
            }
            return TYPE_CHECK;
        }
    }
}
