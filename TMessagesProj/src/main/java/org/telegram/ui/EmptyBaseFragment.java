/*
 * Copyright (C) 2019-2024 qwq233 <qwq233@qwq2333.top>
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

package org.telegram.ui;

import android.content.Context;
import android.view.View;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.telegram.messenger.AndroidUtilities;

public class EmptyBaseFragment extends BaseFragment {


    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }
    @Override
    public View createView(Context context) {
        ViewCompat.setOnApplyWindowInsetsListener(fragmentView, (v, insets) -> {
            final int bottomInset = Math.max(insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom, AndroidUtilities.navigationBarHeight);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomInset);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(fragmentView);
        return fragmentView = new SizeNotifierFrameLayout(context);
    }

}
