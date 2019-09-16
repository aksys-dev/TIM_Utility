package it.telecomitalia.TIMgamepad2.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class UpdatePowerOnReceiver extends BroadcastReceiver {

//    public static final String CHANNEL_ID = "it.telecomitalia.TIMgamepad2";

    public void onReceive(Context context, Intent intent) {
        LogUtil.d("Received intent: " + intent.getAction());
        Intent intent2 = new Intent(context, UpdateFotaMainService.class);
        intent2.setAction(intent.getAction());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(intent2);
        } else {
            context.startForegroundService(intent2);
        }
    }
}
