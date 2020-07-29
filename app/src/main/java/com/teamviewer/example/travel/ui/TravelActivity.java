/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.teamviewer.example.travel.R;
import com.teamviewer.example.travel.ScreenSharingWrapper;

public class TravelActivity extends AppCompatActivity implements ScreenSharingWrapper.RunningStateListener {

    private static final String TAG = "TravelActivity";

    private MenuItem m_menuItem;
    private AlertDialog m_sessionCodeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupCustomActionBar();
        setupSimplePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.travel_activity_actions, menu);

        m_menuItem = menu.findItem(R.id.help);
        updateMenuItemState(m_menuItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ensure the correct reference is listening
        ScreenSharingWrapper.getInstance().setRunningStateListener(this);
        updateSnackbar(ScreenSharingWrapper.getInstance().isSessionRunning());

        handleOverlayPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // don't keep the reference when activity is destroyed
        ScreenSharingWrapper.getInstance().setRunningStateListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                m_sessionCodeDialog = new SessionCodeInputDialog(this).getInstance();
                m_sessionCodeDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Snackbar m_snackbar = null;

    @Override
    public void onRunningStateChange(boolean isRunning) {
        // saving the state is not necessary and
        // missed events between #onPause() and #onResume()
        // are intercepted by querying the session state in
        // #updateMenuItemState(MenuItem)
        invalidateOptionsMenu();
        updateSnackbar(isRunning);
    }

    private void updateSnackbar(boolean isRunning) {
        if (isRunning) {
            if (m_sessionCodeDialog != null) {
                m_sessionCodeDialog.dismiss();
            }
            if (m_snackbar == null) {
                m_snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.teamviewersdk_session_title, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.teamviewersdk_session_stop, v -> ScreenSharingWrapper.getInstance().stopRunningSession())
                        .setActionTextColor(ContextCompat.getColor(this, R.color.app_color));
            }
            m_snackbar.show();
        } else {
            if (m_snackbar != null) {
                m_snackbar.dismiss();
            }
        }
        if (m_menuItem != null) {
            updateMenuItemState(m_menuItem);
        }
    }

    private void setupCustomActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.action_bar_title);
        }
    }

    private void setupSimplePreferences() {
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new TravelFragment())
                .commit();
    }

    private void updateMenuItemState(MenuItem menuItem) {
        boolean buttonEnabled = !ScreenSharingWrapper.getInstance().isSessionRunning();
        menuItem.setEnabled(buttonEnabled);
    }

    private void handleOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog();
        }
    }

    private void showOverlayPermissionDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Special app access required");
        alertBuilder.setMessage("Travel app needs permission to display over other app");
        alertBuilder.setNegativeButton("Cancel", (dialog, i) -> {
            dialog.dismiss();
        });
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton("Settings", (dialog, i) -> openSettingsIntent());
        alertBuilder.show();
    }

    private void openSettingsIntent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Log.e(TAG, "Failed to display overlay permission screen");
            }
        }
    }
}
