/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.callbacks;

import android.content.Context;

import com.teamviewer.example.travel.ScreenSharingWrapper;
import com.teamviewer.sdk.pilotsessionui.PilotSessionUI;
import com.teamviewer.sdk.screensharing.PilotSession;
import com.teamviewer.sdk.screensharing.SessionCallback;
import com.teamviewer.sdk.screensharing.TeamViewerSession;

public class SessionCallbackImpl implements SessionCallback {

    private final Context m_context;
    private final InputCallbackImpl m_inputCallback;

    public SessionCallbackImpl(Context context, InputCallbackImpl inputCallback) {
        m_context = context;
        m_inputCallback = inputCallback;
    }

    @Override
    public void onSessionStarted(TeamViewerSession teamViewerSession) {
        if (teamViewerSession instanceof PilotSession) {
            PilotSessionUI.INSTANCE.setCurrentSession((PilotSession) teamViewerSession);
            m_context.startActivity(PilotSessionUI.INSTANCE.createIntentForPilotSessionActivity(m_context));
        }
        ScreenSharingWrapper.getInstance().sessionStarted();
    }

    @Override
    public void onSessionEnded() {
        PilotSessionUI.INSTANCE.setCurrentSession(null);
        ScreenSharingWrapper.getInstance().sessionEnded();
        m_inputCallback.terminate();
    }
}
