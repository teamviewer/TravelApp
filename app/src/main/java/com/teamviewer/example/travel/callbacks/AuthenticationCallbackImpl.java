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
import com.teamviewer.sdk.screensharing.AuthenticationCallback;
import com.teamviewer.sdk.screensharing.AuthenticationData;

public class AuthenticationCallbackImpl implements AuthenticationCallback {

    private final Context m_context;

    private AlertDialog m_alertDialog;

    public AuthenticationCallbackImpl(Context context)
    {
        m_context = context;
    }

    @Override
    public void onAuthentication(AuthenticationData data) {

        new AlertDialog.Builder(m_context)
                .setTitle(R.string.teamviewersdk_authentication_title)
                .setMessage(m_context.getString(R.string.teamviewersdk_authentication_message, data.getPartnerName()))
                .setPositiveButton(R.string.teamviewersdk_authentication_positive, (dialog, which) ->
                        data.getCallback().onAuthenticationResult(true))
                .setNegativeButton(R.string.teamviewersdk_authentication_negative, (dialog, which) ->
                        data.getCallback().onAuthenticationResult(false))
                .setCancelable(false)
                .show();
    }

    @Override
    public void onAuthenticationCanceled() {
    }
}
