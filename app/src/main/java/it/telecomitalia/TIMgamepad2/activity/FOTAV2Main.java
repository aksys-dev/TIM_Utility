package it.telecomitalia.TIMgamepad2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import it.telecomitalia.TIMgamepad2.R;

import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_SERVICE;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_KEY;

public class FOTAV2Main extends AppCompatActivity {
    private static final boolean CALIBRATION_DEFAULT = false;

    ListView menulistView;
    private Context mContext;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fota_v2_main);
        mContext = this;

        String from = getIntent().getStringExtra(INTENT_KEY);
        if (from != null) {
            if (from.equals(INTENT_FROM_SERVICE)) {
                gotoGamepadsListView(true);
            }
        }
    }

    private void gotoGamepadsListView(boolean directly) {
        if (directly) {
            OpenGamepadMenu();
        }
    }

    public void onClickMenu(View view) {
        if (view.getId() == R.id.menu_gamepad) OpenGamepadMenu();
        if (view.getId() == R.id.menu_imu) OpenIMUMenu();
        if (view.getId() == R.id.menu_button_conf) OpenButtonConfigureation();
        if (view.getId() == R.id.menu_about) OpenAboutVersion();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void OpenGamepadMenu() {
        startActivity(new Intent(mContext, GamepadActivity.class));
    }

    void OpenIMUMenu() {
        startActivity(new Intent(mContext, SetupIMUActivity.class));
    }

    void OpenButtonConfigureation() {
        startActivity(new Intent(mContext, ButtonSettingActivity.class));
    }
    
    void OpenAboutVersion() {
        startActivity(new Intent(mContext, AboutActivity.class));
    }
}