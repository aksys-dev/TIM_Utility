package it.telecomitalia.TIMgamepad2.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdatePowerOnReceiver extends BroadcastReceiver {
	
	public void onReceive(Context context, Intent intent)
	{
		Intent intent2=new Intent(context, UpdateFotaMainService.class);
		context.startService(intent2);
	}
}
