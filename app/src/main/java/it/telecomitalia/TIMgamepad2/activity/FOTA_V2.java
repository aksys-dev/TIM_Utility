package it.telecomitalia.TIMgamepad2.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import it.telecomitalia.TIMgamepad2.GamepadListAdapter;
import it.telecomitalia.TIMgamepad2.GamepadVO;
import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.fota.DeviceModel.INIT_ADDRESS;

public class FOTA_V2 extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ConstraintLayout menulist;
    ListView menulistView;

    FrameLayout gamepadView;
    ListView gamepadList2;
    LinearLayout imuoptionLists;
    LinearLayout aboutLists;

    ArrayList<GamepadVO> datas;

    SeekBar seekBarSensitivity;
    TextView textSeekBarValue;
    float sensitivityValue = 1.00F;
    Switch calibration;

    private UpgradeManager mUpgradeManager;
    private BluetoothDeviceManager mGamePadDeviceManager;

    //	FrameLayout frame_newVersion;
    TextView currentVersion, updateVersion;
    TextView lastUpdateDay;

    TextView activityTitle;
    LinearLayout menuButtons;

    private static BluetoothDeviceManager mDeviceManager;

    private MagicKey[] mMagicKeys = new MagicKey[]{new MagicKey(102, 0), new MagicKey(103, 1), new MagicKey(21, 2), new MagicKey(19, 3), new MagicKey(20, 4), new MagicKey(22, 5),};

    private int mMagicIndex = 0;

    class MagicKey {
        private int mKeyCode;
        private int mIndex;

        MagicKey(int keyCode, int index) {
            this.mKeyCode = keyCode;
            this.mIndex = index;
        }

        public int getKeyCode() {
            return mKeyCode;
        }

        public int getIndex() {
            return mIndex;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fota__v2);

        mDeviceManager = BluetoothDeviceManager.getDeviceManager();
        datas = new ArrayList<>();

        EventBus.getDefault().register(this);

        activityTitle = findViewById(R.id.ActivityTitle);

        // App Main Menu
        menulist = findViewById(R.id.Menus);
        menulistView = findViewById(R.id.menuListView);
        menulistView.setOnItemClickListener(this);
        SetMenuData();

        // Gamepad Option
        gamepadView = findViewById(R.id.gamepadView2);
        gamepadList2 = findViewById(R.id.gamepadListView);

        // IMU Sensor
        imuoptionLists = findViewById(R.id.imuoptionLists);
        aboutLists = findViewById(R.id.aboutLists);

        seekBarSensitivity = findViewById(R.id.seekBar);
        textSeekBarValue = findViewById(R.id.textSensitibity);
        calibration = findViewById(R.id.sw_calibration);
        calibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    calibration.setText(getString(R.string.on));
                } else {
                    calibration.setText(getString(R.string.off));
                }
            }
        });

        currentVersion = findViewById(R.id.value_currentVersion);
        lastUpdateDay = findViewById(R.id.value_updateDay);
        updateVersion = findViewById(R.id.value_newVersion);

        try {
            currentVersion.setText(this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            currentVersion.setText("1.0");
        }
        updateVersion.setText("1.0");

        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // IMU Sensitivity Source
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensitivityValue = (float) ((progress + 1) / 100.0);
                textSeekBarValue.setText(String.valueOf(sensitivityValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mGamePadDeviceManager = BluetoothDeviceManager.getDeviceManager();
        mUpgradeManager = mGamePadDeviceManager.getUpgradeManager();
    }

    public void SetMenuData() {
        String[] menulists = {getString(R.string.menu_gamepad), getString(R.string.menu_imu), getString(R.string.menu_about)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menulists);
        menulistView.setAdapter(adapter);
    }

    public void SetupGamePadList() {
        GamepadVO g;
        int list = 0;
        datas.clear();
        List<DeviceModel> models = mDeviceManager.getBondedDevices();

        for (DeviceModel model : models) {
            if (!model.getMACAddress().equals(INIT_ADDRESS)) {
                EventAddGamepad(model);
                list++;
            }
        }

        if (datas.size() == 0) {
            // No Gamepad
            g = new GamepadVO();
            g.setGamepadName(String.format("Game Pad %d", list));
            datas.add(g);
        }

//        while (datas.size() > 0 && datas.size() >= list) {
//            // TODO Get GAMEPAD Data to List View
//            if (datas.get(list).getGamepadName() == getString(R.string.none_found)) {
//                datas.remove(list);
//                continue;
//            }
//            Toast.makeText(this, datas.get(list).getMACAddress(), Toast.LENGTH_SHORT).show();
//            EventAddGamepad(datas.get(list).getMACAddress());
//            list++;
//        }
//        if (datas.size() == 0) {
//            // No Gamepad
//            g = new GamepadVO();
//            g.setGamepadName(String.format("Gamepad %d", list));
//            datas.add(g);
//        }
    }

    private GamepadListAdapter adapter;

    public void RefreshGamePadList() {
        adapter = new GamepadListAdapter(this, R.layout.gamepad_info, datas);
        gamepadList2.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // User press Back Button
        if (menulist.getVisibility() == View.GONE) {
            gamepadView.setVisibility(View.GONE);
            imuoptionLists.setVisibility(View.GONE);
            aboutLists.setVisibility(View.GONE);

            activityTitle.setText(R.string.title_gamepad_advanced);
            menulist.setVisibility(View.VISIBLE);
        } else finish();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

//    Deprecated Source
//    public void onClickMenu(View view) {
//        gamepadLists.setVisibility(View.GONE);
//        gamepadView.setVisibility(View.GONE);
//        imuoptionLists.setVisibility(View.GONE);
//        aboutLists.setVisibility(View.GONE);
//        switch (view.getId()) {
//            case R.id.menu_gamepad:
//                SetupGamepadList();
//                RefreshGamepadList();
//                gamepadList2.setOnItemClickListener(this);
//                gamepadView.setVisibility(View.VISIBLE);
//                activityTitle.setText(R.string.menu_gamepad);
//                break;
//            case R.id.menu_imu:
//                imuoptionLists.setVisibility(View.VISIBLE);
//                activityTitle.setText(R.string.menu_imu);
//                break;
//            case R.id.menu_about:
//                aboutLists.setVisibility(View.VISIBLE);
//                activityTitle.setText(R.string.menu_about);
//                break;
//        }
//        menuButtons.setVisibility(View.GONE);
//    }

    public void onClickVersionCheck(View view) {
        // Deprecated Source
//        switch ( view.getId() ) {
//            case R.id.buttonVersionCheck:
//                // Check new Application Version
//                if (version != update) {
//                    // Now Version != New Version
//                    findViewById( R.id.buttonUpdateVersion ).setVisibility( View.VISIBLE );
//                    findViewById( R.id.tr_newUpdate ).setVisibility( View.VISIBLE );
//                    findViewById( R.id.tr_updateMessage ).setVisibility( View.VISIBLE );
//                } else {
//                    // Now Version == New Version
//                    findViewById( R.id.buttonUpdateVersion ).setVisibility( View.GONE );
//                    findViewById( R.id.tr_newUpdate ).setVisibility( View.GONE );
//                    findViewById( R.id.tr_updateMessage ).setVisibility( View.GONE );
//                }
//                break;
//            case R.id.buttonUpdateVersion:
//                // Application Version Update Code Here
//                findViewById( R.id.buttonUpdateVersion ).setVisibility( View.GONE );
//                findViewById( R.id.tr_newUpdate ).setVisibility( View.GONE );
//                findViewById( R.id.tr_updateMessage ).setVisibility( View.GONE );
//                currentVersion.setText( updateVersion.getText() );
//                // If you can check Update Server, Delete this . Please.
//                version = update++;
//                updateVersion.setText( new StringBuilder().append( "1." ).append( String.valueOf( update ) ).toString() ); // New Version Name
//                break;
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (menulist.getVisibility() == View.VISIBLE) {
            // Menu Item Click
            menulist.setVisibility(View.GONE);
            gamepadView.setVisibility(View.GONE);
            imuoptionLists.setVisibility(View.GONE);
            aboutLists.setVisibility(View.GONE);

            switch (position) {
                case 0:
                    // Gamepad Menu
                    SetupGamePadList();
                    RefreshGamePadList();
                    gamepadList2.setOnItemClickListener(this);
                    gamepadView.setVisibility(View.VISIBLE);
                    activityTitle.setText(R.string.menu_gamepad);
                    break;
                case 1:
                    // IMU Setting
                    imuoptionLists.setVisibility(View.VISIBLE);
                    activityTitle.setText(R.string.menu_imu);
                    break;
                case 2:
                    // About App
                    aboutLists.setVisibility(View.VISIBLE);
                    activityTitle.setText(R.string.menu_about);
                    break;
            }
        }

        if (gamepadView.getVisibility() == View.VISIBLE) {
            FirmwareConfig config = mUpgradeManager.getNewVersion();
            ArrayList<DeviceModel> deviceList = mGamePadDeviceManager.getNeedUpgradedDevice(config);
            boolean found = false;

            for (DeviceModel model : deviceList) {
                if (datas.get(position).getMACAddress().equals(model.getMACAddress())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                LogUtil.d("Need upgrade ");
                Intent dialogIntent = new Intent(FOTA_V2.this, DialogActivity.class);
                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
            } else {
                Toast.makeText(FOTA_V2.this, datas.get(position).getGamepadName() + getString(R.string.no_need_update), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof BluetoothDevice) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) object;
            Crashlytics.log(Log.INFO, "FOTA2", "Detected Bluetooth: " + bluetoothDevice.getAddress());
            EventAddGamepad(bluetoothDevice.getAddress());
        } else if (((String) object).contains("BlueTooth_Connected")) {
            //蓝牙已连接
//            Toast.makeText(this, "BlueTooth_Connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void EventAddGamepad(String macAddress) {
        if (macAddress == getString(R.string.unknown)) return;
        GamepadVO g = new GamepadVO(macAddress);
//        Toast.makeText(this, "Detected Bluetooth: " + macAddress, Toast.LENGTH_SHORT).show();
        g.setGamepadName(String.format("Game Pad %d"));
        datas.add(g);
    }

    public void EventAddGamepad(DeviceModel model) {
        GamepadVO g = new GamepadVO(getString(R.string.gamepad_one) + (model.getIndicator() + 1), model.getMACAddress(), model.getBatterVolt(), model.getFWVersion(), model.online());
        datas.add(g);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getAction()) {

            case KeyEvent.ACTION_UP:

                if (mMagicIndex == mMagicKeys[mMagicIndex].getIndex() && event.getKeyCode() == mMagicKeys[mMagicIndex].getKeyCode()) {
                    mMagicIndex++;
                    LogUtil.d("Keycode " + event.getKeyCode() + "; Index =" + mMagicIndex);
                    if (mMagicIndex == 6) {
                        Toast.makeText(FOTA_V2.this, "Magic key!!!", Toast.LENGTH_LONG).show();
                        Intent dialogIntent = new Intent(FOTA_V2.this, BackDoor.class);
                        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dialogIntent);
                        mMagicIndex = 0;
                    }
                } else {
                    mMagicIndex = 0;
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }
}