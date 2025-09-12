/*
 * Copyright 2019 TeamViewer (www.teamviewer.com).  All rights reserved.
 *
 * Please refer to the end user license agreement (EULA), the app developer agreement
 * and license information associated with this source code for terms and conditions
 * that govern your use of this software.
 */

package com.teamviewer.example.travel.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.teamviewer.example.travel.R;
import com.teamviewer.example.travel.ScreenSharingWrapper;

import java.io.File;

public class TravelActivity extends AppCompatActivity implements ScreenSharingWrapper.RunningStateListener {

    private static final String TAG = "TravelActivity";
    private static final int REQUEST_CODE_RECORD_AUDIO_PERMISSIONS = 1;
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 2;
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 3;

    private static final int REQUEST_CODE_PICK_FILE = 201;
    private static final int REQUEST_CODE_TAKE_PICTURE = 202;
    private Uri m_pendingPictureUri;

    private MenuItem m_menuItemHelp;
    private MenuItem m_menuItemShare;
    private AlertDialog m_sessionCodeDialog;
    private Snackbar m_snackbar = null;

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

        m_menuItemHelp = menu.findItem(R.id.help);
        m_menuItemShare = menu.findItem(R.id.share);

        updateMenuItemState();

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
        if (item.getItemId() == R.id.help) {
            m_sessionCodeDialog = new SessionCodeInputDialog(this).getInstance();
            m_sessionCodeDialog.show();
            return true;
        } else if (item.getItemId() == R.id.share) {
            showChooseFileDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChooseFileDialog() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);
        adapter.addAll(getResources().getStringArray(R.array.share_from));
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_file_title)
                .setSingleChoiceItems(adapter, 0, (dialog, which) -> {
                    if (which == 0) {
                        //If you don't use PilotSessionUI you don't need to check Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSIONS);
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        try {
                            startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
                        } catch (ActivityNotFoundException ignored) {
                        }
                    }
                    dialog.dismiss();
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File storageFile = getStorageFile("picture_" + System.currentTimeMillis() + ".jpg");
        if (storageFile != null) {
            Uri photoUri = FileProvider.getUriForFile(this,
                    "com.teamviewer.example.travel.fileprovider",
                    storageFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            m_pendingPictureUri = photoUri;

            try {
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }

    @Nullable
    private File getStorageFile(@NonNull String name) {
        File[] mediaDirs = getExternalMediaDirs();
        if (mediaDirs != null && mediaDirs.length > 0) {
            return new File(mediaDirs[0], name);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_FILE) {
                if (data != null) {
                    if (data.getData() != null) {
                        ScreenSharingWrapper.getInstance().shareFiles(data.getData());
                    } else if (data.getClipData() != null) {
                        final ClipData clipData = data.getClipData();
                        final int count = clipData.getItemCount();
                        final Uri[] files = new Uri[count];
                        for (int i = 0; i < count; ++i) {
                            files[i] = clipData.getItemAt(i).getUri();
                        }
                        ScreenSharingWrapper.getInstance().shareFiles(files);
                    }
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
                ScreenSharingWrapper.getInstance().shareFiles(m_pendingPictureUri);
                m_pendingPictureUri = null;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_RECORD_AUDIO_PERMISSIONS) {
            for (int i = 0; i < permissions.length; ++i) {
                if (Manifest.permission.RECORD_AUDIO.equals(permissions[i]) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    ScreenSharingWrapper.getInstance().onMicrophonePermissionGranted();
                }
            }
        }
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSIONS) {
            for (int i = 0; i < permissions.length; ++i) {
                if (Manifest.permission.CAMERA.equals(permissions[i]) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onRunningStateChange(@NonNull SessionState sessionState) {
        // saving the state is not necessary and
        // missed events between #onPause() and #onResume()
        // are intercepted by querying the session state in
        // #updateMenuItemState(MenuItem)
        invalidateOptionsMenu();
        updateSnackbar(sessionState != SessionState.NoSession);

        //Request record audio permission
        if (sessionState == SessionState.ScreenSharing &&
                ContextCompat.checkSelfPermission(TravelActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO_PERMISSIONS);
        }
        if (sessionState == SessionState.Pilot &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(TravelActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION_PERMISSION);
        }
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
            updateMenuItemState();
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

    private void updateMenuItemState() {
        if(m_menuItemHelp == null){
            return;
        }
        boolean isSessionRunning = !ScreenSharingWrapper.getInstance().isSessionRunning();
        m_menuItemHelp.setVisible(isSessionRunning);
        m_menuItemShare.setVisible(!isSessionRunning);
    }

    private void handleOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
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
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(TAG, "Failed to display overlay permission screen");
        }
    }
}
