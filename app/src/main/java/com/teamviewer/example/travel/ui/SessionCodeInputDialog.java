/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.teamviewer.example.travel.R;
import com.teamviewer.example.travel.ScreenSharingWrapper;

@SuppressLint("InflateParams")
public class SessionCodeInputDialog extends AlertDialog.Builder {

    private AlertDialog m_alertDialog;

    AlertDialog getInstance()
    {
        return m_alertDialog;
    }

    SessionCodeInputDialog(Context context) {
        super(context);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.session_code_entry, null);
        EditText sessionCode = view.findViewById(R.id.sessionCode);
        setView(view);

        setPositiveButton(R.string.teamviewersdk_authentication_connect, null);
        setNegativeButton(R.string.teamviewersdk_authentication_cancel, (dialogInterface, i) ->
                {
                    m_alertDialog.dismiss();
                    ScreenSharingWrapper.getInstance().stopRunningSession();
                });

        setCancelable(false);

        m_alertDialog = create();
        m_alertDialog.setOnShowListener((dialogInterface) ->
        {
            Button connect = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
            connect.setOnClickListener(view1 ->
            {
                sessionCode.setEnabled(false);
                TextView bodyText = view.findViewById(R.id.inputTitle);
                bodyText.setText(R.string.teamviewersdk_enter_session_code_title_wait);
                connect.setEnabled(false);
                ScreenSharingWrapper.getInstance().startTeamViewerSession(context,
                        sessionCode.getText().toString());
            });
        });
    }
}
