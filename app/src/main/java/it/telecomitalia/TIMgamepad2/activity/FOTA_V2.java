package it.telecomitalia.TIMgamepad2.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.service.UpdateFotaMainService;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class FOTA_V2 extends Activity {

    private ProgressBar progress;
    private TextView tv;

    private static final String RESTART_SERVICE_BROADCAST = "it.telecomitalia.TIMgamepad2.RESTART_SERVICE";

    @SuppressLint("HandlerLeak")
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
//                LogUtil.d("Reset finished, Now start utility UI");
                progress.setVisibility(View.GONE);
                tv.setVisibility(View.GONE);
                Intent i = new Intent(FOTA_V2.this, FOTAV2Main.class);
                startActivity(i);
                FOTA_V2.this.finish();
            } else if (msg.what == 0) {
//                LogUtil.d("Try to restart service...");
                Intent i = new Intent();
                i.setAction(RESTART_SERVICE_BROADCAST);
                i.setPackage(getPackageName());
                sendBroadcast(i);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fota_v2);
        progress = findViewById(R.id.progressBar3);
        tv = findViewById(R.id.restarting_text);
        if (!isMyServiceRunning(UpdateFotaMainService.class)) {
            progress.setVisibility(View.VISIBLE);
            tv.setVisibility(View.VISIBLE);
            LogUtil.d("Service not running, Launch it...");
            final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            final BluetoothAdapter btAdapter = btManager.getAdapter();

            handler.sendEmptyMessage(0);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        while (!btAdapter.isEnabled()) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(1);
                }
            }).start();
        } else {
//            LogUtil.d("Service running, Ignore...");
            Intent i = new Intent(FOTA_V2.this, FOTAV2Main.class);
            startActivity(i);
            finish();
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(100)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
