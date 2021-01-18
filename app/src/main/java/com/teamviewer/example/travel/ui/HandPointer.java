/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.teamviewer.example.travel.R;

public class HandPointer {
    private boolean m_addedToView;
    private final ImageView m_image;
    private final WindowManager.LayoutParams m_layoutParams;
    private final Context m_context;
    private double m_xShift;
    private double m_yShift;

    public HandPointer(Context context) {
        m_context = context;
        m_image = new ImageView(m_context);
        m_image.setImageResource(R.drawable.tv_show_marker);

        m_layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSPARENT);

        m_layoutParams.gravity = Gravity.START | Gravity.TOP;
    }

    public void show(int x, int y) {
        m_layoutParams.x = (int) (x - m_xShift);
        m_layoutParams.y = (int) (y - m_yShift);

        WindowManager windowManager = (WindowManager) m_context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager != null && !m_addedToView) {
            if (!m_addedToView) {
                windowManager.addView(m_image, m_layoutParams);
                m_addedToView = true;
            }
        } else if (m_addedToView) {
            updatePosition();
        }
    }

    private void updatePosition() {
        WindowManager windowManager = (WindowManager) m_context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager != null) {
            windowManager.updateViewLayout(m_image, m_layoutParams);
        }
    }

    public void buttonClicked(int x, int y) {
        float scale = 0.75f;
        ObjectAnimator xScale = ObjectAnimator.ofFloat(m_image, "scaleX", scale);
        ObjectAnimator yScale = ObjectAnimator.ofFloat(m_image, "scaleY", scale);
        xScale.setDuration(1);
        yScale.setDuration(1);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(xScale).with(yScale);
        animatorSet.start();

        updateShift(scale);
        show(x, y);
    }

    public void buttonReleased(int x, int y) {
        float scale = 1.0f;
        ObjectAnimator xScale = ObjectAnimator.ofFloat(m_image, "scaleX", scale);
        ObjectAnimator yScale = ObjectAnimator.ofFloat(m_image, "scaleY", scale);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(xScale).with(yScale);
        animatorSet.start();

        updateShift(scale);
        show(x, y);
    }

    public void removePointer() {
        WindowManager windowManager = (WindowManager) m_context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.removeViewImmediate(m_image);
        }
    }

    public void updateShift(float scale) {
        float shiftingFactor = (1 - scale) / 2;
        m_xShift = m_image.getWidth() * shiftingFactor;
        m_yShift = m_image.getHeight() * shiftingFactor;
    }
}
