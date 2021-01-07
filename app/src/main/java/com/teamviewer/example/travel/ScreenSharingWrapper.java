/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.teamviewer.example.travel.callbacks.AccessControlCallbackImpl;
import com.teamviewer.example.travel.callbacks.AuthenticationCallbackImpl;
import com.teamviewer.example.travel.callbacks.InputCallbackImpl;
import com.teamviewer.example.travel.callbacks.SessionCallbackImpl;
import com.teamviewer.sdk.screensharing.AccessControlRule;
import com.teamviewer.sdk.screensharing.AccessType;
import com.teamviewer.sdk.screensharing.DefaultLogger;
import com.teamviewer.sdk.screensharing.Settings;
import com.teamviewer.sdk.screensharing.TeamViewerSdk;

/**
 * Encapsulates the TeamViewer Screen Sharing SDK.
 */
public final class ScreenSharingWrapper {

    private static final ScreenSharingWrapper sInstance = new ScreenSharingWrapper();
    /**
     * A valid SDK Token which was created in the
     * <a href="https://login.teamviewer.com/nav/api/create/mobile">TeamViewer Management Console</a>.
     * //TODO Add SDK token here in the form "{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}"
     */
    private static final String SDK_TOKEN = ;

    private Settings m_settings;
    private RunningStateListener mRunningStateListener;
    private TeamViewerSdk m_teamViewerSdk;
    private boolean mIsSessionRunning;

    public static ScreenSharingWrapper getInstance() {
        return sInstance;
    }

    private ScreenSharingWrapper() {
        // created by the factory method
    }

    /**
     * @param listener A listener object, or {@code null} to unregister.
     */
    public void setRunningStateListener(RunningStateListener listener) {
        mRunningStateListener = listener;
    }

    /**
     * The TeamViewer session will be started asynchronously.
     *
     * @param context The application's context
     */
    public void startTeamViewerSession(Context context, String sessionCode) {
        setupSettings();
        InputCallbackImpl inputCallback = new InputCallbackImpl(context);

        m_teamViewerSdk = new TeamViewerSdk.Builder(context)
                .withToken(SDK_TOKEN)
                .withLogger(new DefaultLogger())
                .withErrorCallback(errorCode -> {
                    Log.e("TeamViewerSdk", "Error: " + errorCode);
                    Toast.makeText(context, errorCode.toString(), Toast.LENGTH_SHORT).show();
                })
                .withSessionCallback(new SessionCallbackImpl(context, inputCallback))
                .withAuthenticationCallback(new AuthenticationCallbackImpl(context))
                .withSettings(m_settings)
                .withAccessControlCallback(new AccessControlCallbackImpl(context))
                .withInputCallback(inputCallback)
                .build();
        m_teamViewerSdk.connectToSessionCode(sessionCode);
    }

    private void setupSettings()
    {
        m_settings = new Settings();
        m_settings.accessControlRules.put(
                AccessType.RemoteControl, AccessControlRule.AfterConfirmation);
    }

    public boolean isSessionRunning() {
        return mIsSessionRunning;
    }

    public void stopRunningSession()
    {
        if (m_teamViewerSdk != null)
        {
            m_teamViewerSdk.shutdown();
            m_teamViewerSdk = null;
            m_settings = null;
        }
    }

    public void sessionStarted() {
        mIsSessionRunning = true;
        if (mRunningStateListener != null) {
            mRunningStateListener.onRunningStateChange(true);
        }
    }

    public void sessionEnded() {
        mIsSessionRunning = false;
        if (mRunningStateListener != null) {
            mRunningStateListener.onRunningStateChange(false);
        }
    }

    /**
     * Callback to be invoked when the TeamViewer session is started or has
     * ended.
     */
    public interface RunningStateListener {
        /**
         * Will be called on running state changes of the TeamViewer session.
         *
         * @param isRunning New session state.
         */
        void onRunningStateChange(boolean isRunning);
    }
}
