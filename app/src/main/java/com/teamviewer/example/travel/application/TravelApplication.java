package com.teamviewer.example.travel.application;

import android.app.Application;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;

import com.teamviewer.example.travel.Constants;

public class TravelApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannelCompat notificationChannel = new NotificationChannelCompat
                .Builder(Constants.NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Travel App Support Session")
                .setDescription("Ongoing Session")
                .build();
        NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel);
    }
}
