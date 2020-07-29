/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.callbacks;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.teamviewer.example.travel.ui.HandPointer;
import com.teamviewer.sdk.screensharing.InputCallback;

public class InputCallbackImpl implements InputCallback {
    private static final String TAG = "InputCallbackImpl";
    private final Context m_context;
    private HandPointer m_handPointer;
    private boolean m_isButtonClicked;
    private boolean m_applicationOverlayAllowed;

    public InputCallbackImpl(Context context) {
        m_context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && android.provider.Settings.canDrawOverlays(m_context)) {
            m_applicationOverlayAllowed = true;
        } else {
            m_applicationOverlayAllowed = false;
            Log.d(TAG, "Permission to display over application was not granted. Hand pointer will not be drawn.");
        }
    }

    @Override
    public void onInput(int x, int y, int flags) {
        if (m_applicationOverlayAllowed) {
            if (m_handPointer == null) {
                m_handPointer = new HandPointer(m_context);
            }
            if ((flags & InputCallback.FLAG_MOUSE_BUTTON_LEFT) == InputCallback.FLAG_MOUSE_BUTTON_LEFT) {
                m_isButtonClicked = true;
                m_handPointer.buttonClicked(x, y);
            } else if ((flags & InputCallback.FLAG_MOUSE) == InputCallback.FLAG_MOUSE) {
                if (m_isButtonClicked) {
                    m_isButtonClicked = false;
                    m_handPointer.buttonReleased(x, y);
                } else {
                    m_handPointer.show(x, y);
                }
            }
        }
    }

    public void terminate() {
        if (m_handPointer != null) {
            m_handPointer.removePointer();
            m_handPointer = null;
        }
    }
}
