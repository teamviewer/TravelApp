/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.callbacks;

import android.app.AlertDialog;
import android.content.Context;

import com.teamviewer.example.travel.R;
import com.teamviewer.sdk.screensharing.AccessControlData;

public class AccessControlCallbackImpl implements com.teamviewer.sdk.screensharing.AccessControlCallback
{
    private Context m_context;

    public AccessControlCallbackImpl(Context context)
    {
        m_context = context;
    }

    @Override
    public void onAccessControlRequest(AccessControlData accessControlData)
    {
        new AlertDialog.Builder(m_context)
                .setTitle("Supporter requests control for your device.")
                .setMessage("Allow device control?")
                .setPositiveButton(R.string.teamviewersdk_authentication_positive, ((dialogInterface, i) ->
                        accessControlData.getCallback().onAccessControlResult(true)
                        ))
                .setNegativeButton(R.string.teamviewersdk_authentication_cancel, ((dialogInterface, i) ->
                        accessControlData.getCallback().onAccessControlResult(false)))
                .setCancelable(false)
                .show();
    }
}
