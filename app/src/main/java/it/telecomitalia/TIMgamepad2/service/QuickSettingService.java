package it.telecomitalia.TIMgamepad2.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.service.quicksettings.TileService;

import it.telecomitalia.TIMgamepad2.GamePadV2UpgadeApplication;
import it.telecomitalia.TIMgamepad2.activity.FOTAV2Main;

public class QuickSettingService extends TileService {
	@Override
	public void onClick() {
		super.onClick();
		Intent intent = new Intent(GamePadV2UpgadeApplication.getContext(), FOTAV2Main.class);
		startActivity(intent);
		Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		GamePadV2UpgadeApplication.getContext().sendBroadcast(closeIntent);
	}
}
