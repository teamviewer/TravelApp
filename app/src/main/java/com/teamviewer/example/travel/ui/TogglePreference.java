/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;

/**
 * A {@link CheckBoxPreference} where the checkbox widget is hidden
 */
public class TogglePreference extends CheckBoxPreference {
    public TogglePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TogglePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TogglePreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // don't display the checkbox widget
        View checkboxView = holder.findViewById(android.R.id.widget_frame);
        checkboxView.setVisibility(View.GONE);
    }
}
