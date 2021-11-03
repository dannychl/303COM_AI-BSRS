package com.example.ai_bsrs.notification_module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import androidx.core.app.NotificationCompat;

public class AlarmBootUpReceiver extends BroadcastReceiver {

    private String id = "";
    @Override
    public void onReceive(Context context, Intent intent) {

        id = intent.getExtras().getString("notificationAlarmId");
        NotificationHelper notificationHelper = new NotificationHelper(context, id);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(1, nb.build());
    }
}
