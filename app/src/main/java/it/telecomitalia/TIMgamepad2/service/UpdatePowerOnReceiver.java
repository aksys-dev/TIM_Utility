package it.telecomitalia.TIMgamepad2.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import static android.content.Context.NOTIFICATION_SERVICE;

public class UpdatePowerOnReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "it.telecomitalia.TIMgamepad2";

    private void initNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "gamepad", NotificationManager.IMPORTANCE_HIGH));
        }
    }

    public void onReceive(Context context, Intent intent) {
        initNotificationChannel(context);
        Intent intent2 = new Intent(context, UpdateFotaMainService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
//            context.startForegroundService(intent2);
//        } else {
        context.startService(intent2);
//        }
    }
}
