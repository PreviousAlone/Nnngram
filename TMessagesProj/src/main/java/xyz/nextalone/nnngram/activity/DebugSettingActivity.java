/*
 * Copyright (C) 2019-2025 qwq233 <qwq233@qwq2333.top>
 * https://github.com/qwq233/Nullgram
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */

package xyz.nextalone.nnngram.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.ProfileActivity;

@SuppressLint("NotifyDataSetChanged")
public class DebugSettingActivity extends BaseActivity {

    private int debugHeaderRow;
    private int logsEnabledRow;
    private int debugShadowRow;
    private int logsHeaderRow;
    private int sendLogsRow;
    private int sendLastLogsRow;
    private int clearLogsRow;
    private int logsShadowRow;

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString(R.string.DebugMenu);
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == logsEnabledRow) {
            BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED;
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            preferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).apply();
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("app start time = " + ApplicationLoader.startTime);
                try {
                    FileLog.d("buildVersion = " + ApplicationLoader.applicationContext.getPackageManager()
                            .getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0).versionCode);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
            updateRows();
        } else if (position == sendLogsRow) {
            ProfileActivity.sendLogs(getParentActivity(), false);
        } else if (position == sendLastLogsRow) {
            ProfileActivity.sendLogs(getParentActivity(), true);
        } else if (position == clearLogsRow) {
            FileLog.cleanupLogs();
        }
    }

    @Override
    protected boolean onItemLongClick(View view, int position, float x, float y) {
        return false;
    }

    @Override
    protected String getKey() {
        return "debug";
    }

    @Override
    protected void updateRows() {
        super.updateRows();

        debugHeaderRow = addRow();
        logsEnabledRow = addRow("DebugMenuEnableLogs");
        debugShadowRow = addRow();

        if (BuildVars.LOGS_ENABLED) {
            logsHeaderRow = addRow();
            sendLogsRow = addRow("DebugSendLogs");
            sendLastLogsRow = addRow("DebugSendLastLogs");
            clearLogsRow = addRow("DebugClearLogs");
            logsShadowRow = addRow();
        } else {
            logsHeaderRow = -1;
            sendLogsRow = -1;
            sendLastLogsRow = -1;
            clearLogsRow = -1;
            logsShadowRow = -1;
        }

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getBaseGuid() {
        return 13000;
    }

    @Override
    public int getDrawable() {
        return R.drawable.msg_info;
    }

    private class ListAdapter extends BaseListAdapter {

        ListAdapter(Context context) {
            super(context);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, boolean payload) {
            switch (holder.getItemViewType()) {
                case TYPE_SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext,
                            position == logsShadowRow ? R.drawable.greydivider_bottom : R.drawable.greydivider,
                            Theme.key_windowBackgroundGrayShadow));
                    break;
                case TYPE_HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(LocaleController.getString(position == debugHeaderRow ? R.string.DebugMenu : R.string.SettingsDebug));
                    break;
                case TYPE_CHECK:
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    checkCell.setTextAndCheck(LocaleController.getString(R.string.DebugMenuEnableLogs), BuildVars.LOGS_ENABLED, false);
                    break;
                case TYPE_SETTINGS:
                    TextSettingsCell settingsCell = (TextSettingsCell) holder.itemView;
                    if (position == sendLogsRow) {
                        settingsCell.setText(LocaleController.getString(R.string.DebugSendLogs), true);
                    } else if (position == sendLastLogsRow) {
                        settingsCell.setText(LocaleController.getString(R.string.DebugSendLastLogs), true);
                    } else if (position == clearLogsRow) {
                        settingsCell.setText(LocaleController.getString(R.string.DebugClearLogs), false);
                    }
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == debugHeaderRow || position == logsHeaderRow) {
                return TYPE_HEADER;
            } else if (position == logsEnabledRow) {
                return TYPE_CHECK;
            } else if (position == debugShadowRow || position == logsShadowRow) {
                return TYPE_SHADOW;
            }
            return TYPE_SETTINGS;
        }
    }
}
