package xyz.nextalone.nnngram.helpers;

import android.net.Uri;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.BaseFragment;

import xyz.nextalone.nnngram.activity.BaseActivity;
import xyz.nextalone.nnngram.activity.ChatSettingActivity;
import xyz.nextalone.nnngram.activity.ExperimentSettingActivity;
import xyz.nextalone.nnngram.activity.GeneralSettingActivity;
import xyz.nextalone.nnngram.activity.MainSettingActivity;
import xyz.nextalone.nnngram.activity.PasscodeSettingActivity;

public class SettingsHelper {

    public static void processDeepLink(Uri uri, Callback callback, Runnable unknown) {
        if (uri == null) {
            unknown.run();
            return;
        }
        var segments = uri.getPathSegments();
        if (segments.isEmpty() || segments.size() > 2 || !"NnnSettings".equals(segments.get(0))) {
            unknown.run();
            return;
        }
        BaseActivity fragment;
        if (segments.size() == 1) {
            fragment = new MainSettingActivity();
        } else if (PasscodeHelper.getSettingsKey().equals(segments.get(1))) {
            fragment = new PasscodeSettingActivity();
        } else {
            switch (segments.get(1)) {
                case "chat":
                case "chats":
                case "c":
                    fragment = new ChatSettingActivity();
                    break;
                case "experimental":
                case "e":
                    fragment = new ExperimentSettingActivity(false, false);
                    break;
                case "general":
                case "g":
                    fragment = new GeneralSettingActivity();
                    break;
                default:
                    unknown.run();
                    return;
            }
        }
        callback.presentFragment(fragment);
        var row = uri.getQueryParameter("r");
        if (TextUtils.isEmpty(row)) {
            row = uri.getQueryParameter("row");
        }
        if (!TextUtils.isEmpty(row)) {
            var rowFinal = row;
            AndroidUtilities.runOnUIThread(() -> fragment.scrollToRow(rowFinal, unknown));
        }

    }

    public interface Callback {
        void presentFragment(BaseFragment fragment);
    }
}
