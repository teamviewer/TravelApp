/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.callbacks;

import com.teamviewer.example.travel.ScreenSharingWrapper;
import com.teamviewer.sdk.screensharing.SessionCallback;
import com.teamviewer.sdk.screensharing.TeamViewerSession;

public class SessionCallbackImpl implements SessionCallback {

    private final InputCallbackImpl m_inputCallback;

    public SessionCallbackImpl(InputCallbackImpl inputCallback)
    {
        m_inputCallback = inputCallback;
    }

    @Override
    public void onSessionStarted(TeamViewerSession teamViewerSession) {
        ScreenSharingWrapper.getInstance().sessionStarted();
    }

    @Override
    public void onSessionEnded() {
        ScreenSharingWrapper.getInstance().sessionEnded();
        m_inputCallback.terminate();
    }
}
