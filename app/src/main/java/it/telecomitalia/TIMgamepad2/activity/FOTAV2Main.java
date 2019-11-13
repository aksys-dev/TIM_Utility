package it.telecomitalia.TIMgamepad2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import it.telecomitalia.TIMgamepad2.R;

import static it.telecomitalia.TIMgamepad2.GamePadV2UpgadeApplication.IS_DEBUG;

public class FOTAV2Main extends AppCompatActivity {
    private static final boolean CALIBRATION_DEFAULT = false;
    
    ConstraintLayout menulist;
    ListView menulistView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fota_v2_main);
        mContext = this;
    
        // App Main Menu
        menulist = findViewById(R.id.Menus);
        menulistView = findViewById(R.id.menuListView);
        menulistView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (menulist.getVisibility() == View.VISIBLE) {
                    switch (position) {
                        case 0:
                            OpenGamepadMenu();
                            break;
                        case 1:
                            if (IS_DEBUG || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                OpenIMUMenu();
                            } else {
                                OpenAboutVersion();
                            }
                            break;
                        case 2:
                            OpenAboutVersion();
                            break;
                    }
                }
            }
        } );
        SetMenuData();
    }
    
    //// GAMEPAD SCENE
    public void SetMenuData() {
        String[] menulists;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || IS_DEBUG) {
            menulists = new String[]{getString(R.string.menu_gamepad), getString(R.string.menu_imu), getString(R.string.menu_about)};
        } else {
            menulists = new String[]{getString(R.string.menu_gamepad), getString(R.string.menu_about)};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, menulists);
        menulistView.setAdapter(adapter);
    }
    
    void OpenGamepadMenu() {
        // Gamepad Menu
        startActivity(new Intent(mContext, GamepadActivity.class));
    }

    void OpenIMUMenu() {
        // IMU Setting
        startActivity(new Intent(mContext, SetupIMUActivity.class));
    }

    void OpenAboutVersion() {
        // About App
        startActivity(new Intent(mContext, AboutActivity.class));
    }
}