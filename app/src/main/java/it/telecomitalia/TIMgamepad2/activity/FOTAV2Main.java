package it.telecomitalia.TIMgamepad2.activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
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

import it.telecomitalia.TIMgamepad2.BuildConfig;
import it.telecomitalia.TIMgamepad2.GamepadListAdapter;
import it.telecomitalia.TIMgamepad2.GamepadVO;
import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;
import it.telecomitalia.TIMgamepad2.utils.SharedPreferenceUtils;

import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_SERVICE;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_USER;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_KEY;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_MAC;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.GAMEPAD_DEVICE_CONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.GAMEPAD_DEVICE_DISCONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.DeviceModel.INIT_ADDRESS;

public class FOTAV2Main extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private static final String CONFIG_FILE_NAME = "CONFIG";
    private static final String KEY_SENSITIVE = "sensitivity";
    private static final float SENSITIVE_DEFAULT = 1.0f;
    private static final String KEY_CALIBRATION = "calibration";
    private static final boolean CALIBRATION_DEFAULT = true;

    ConstraintLayout MainTitle, IMUSensorTitle;

    private Context mContext;

    ConstraintLayout menulist;
    ListView menulistView;

    FrameLayout gamepadView;
    ListView gamepadList2;
    LinearLayout imuoptionLists;
    ConstraintLayout aboutLists;

    ArrayList<GamepadVO> datas;

    SeekBar seekBarSensitivity;
    TextView textSeekBarValue;
    float sensitivityValue = 1.00F;
    boolean calibrationEnabled = true;
    Switch calibration;

    private UpgradeManager mUpgradeManager;
    private BluetoothDeviceManager mGamePadDeviceManager;

    //	FrameLayout frame_newVersion;
    TextView currentVersion, updateVersion;
    TextView lastUpdateDay;

    TextView activityTitle;

    private BluetoothDeviceManager mDeviceManager;

    private MagicKey[] mMagicKeys = new MagicKey[]{new MagicKey(102, 0), new MagicKey(103, 1), new MagicKey(21, 2), new MagicKey(19, 3), new MagicKey(20, 4), new MagicKey(22, 5),};

    private int mMagicIndex = 0;

    private class MagicKey {
        private int mKeyCode;
        private int mIndex;

        MagicKey(int keyCode, int index) {
            this.mKeyCode = keyCode;
            this.mIndex = index;
        }

        int getKeyCode() {
            return mKeyCode;
        }

        int getIndex() {
            return mIndex;
        }
    }

    private IntentFilter makeFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GAMEPAD_DEVICE_CONNECTED);
        intentFilter.addAction(BluetoothDeviceManager.GAMEPAD_DEVICE_DISCONNECTED);
        return intentFilter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        float restoreValue = (float) SharedPreferenceUtils.get(CONFIG_FILE_NAME, mContext, KEY_SENSITIVE, SENSITIVE_DEFAULT);
        calibrationEnabled = (boolean) SharedPreferenceUtils.get(CONFIG_FILE_NAME, mContext, KEY_CALIBRATION, CALIBRATION_DEFAULT);
        calibration.setChecked(calibrationEnabled);
        textSeekBarValue.setText(String.valueOf(restoreValue));
        seekBarSensitivity.setProgress((int) (restoreValue * 100));


//        LogUtil.i("calibrationEnabled-> " + calibrationEnabled);
//        LogUtil.i("sensitivity Value-> " + restoreValue);
//        LogUtil.i("progress-> " + seekBarSensitivity.getProgress());

        calibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    calibration.setText(getString(R.string.on));
                } else {
                    calibration.setText(getString(R.string.off));
                }
                SharedPreferenceUtils.put(CONFIG_FILE_NAME, mContext, KEY_CALIBRATION, isChecked);
            }
        });

        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // IMU Sensitivity Source
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    progress = 1;
                }

                sensitivityValue = (float) ((progress) / 100.0);
                textSeekBarValue.setText(String.valueOf(sensitivityValue));
                SharedPreferenceUtils.put(CONFIG_FILE_NAME, mContext, KEY_SENSITIVE, sensitivityValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        registerReceiver(mReceiver, makeFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(GAMEPAD_DEVICE_CONNECTED) || action.equals(GAMEPAD_DEVICE_DISCONNECTED)) {
                    gotoGamepadsListView(false);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fota_v2_main);
        mContext = this;
        MainTitle = findViewById(R.id.AppTitle);
        IMUSensorTitle = findViewById(R.id.IMU_Title);

        mDeviceManager = BluetoothDeviceManager.getInstance();
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


        currentVersion = findViewById(R.id.value_currentVersion);
        lastUpdateDay = findViewById(R.id.value_updateDay);

        try {
            android.content.pm.PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
//            java.util.Date updateDate = new java.util.Date(packageInfo.lastUpdateTime);
//            SimpleDateFormat dmySlash = new SimpleDateFormat("dd/MM/yyyy");

            currentVersion.setText(packageInfo.versionName);
//          lastUpdateDay.setText(dmySlash.format(updateDate));
            lastUpdateDay.setText(BuildConfig.BUILD_TIME);
        } catch (PackageManager.NameNotFoundException e) {
            currentVersion.setText(R.string.app_version);
            lastUpdateDay.setText(R.string.lastupdate);
        }


        mGamePadDeviceManager = BluetoothDeviceManager.getInstance();
        mUpgradeManager = mGamePadDeviceManager.getUpgradeManager();

        String from = getIntent().getStringExtra(INTENT_KEY);
        if (from != null) {
            if (from.equals(INTENT_FROM_SERVICE)) {
                gotoGamepadsListView(true);
            }
        }
    }

    private void gotoGamepadsListView(boolean directly) {
        if (directly) {
            menulist.setVisibility(View.GONE);
            gamepadView.setVisibility(View.GONE);
            imuoptionLists.setVisibility(View.GONE);
            aboutLists.setVisibility(View.GONE);

            SetupGamePadList();
            RefreshGamePadList();
            gamepadList2.setOnItemClickListener(mGamepadListListener);
            gamepadView.setVisibility(View.VISIBLE);
            activityTitle.setText(R.string.title_gamepad);
        } else {
            if (gamepadView.getVisibility() == View.VISIBLE) {
                menulist.setVisibility(View.GONE);
                gamepadView.setVisibility(View.GONE);
                imuoptionLists.setVisibility(View.GONE);
                aboutLists.setVisibility(View.GONE);
                SetupGamePadList();
                RefreshGamePadList();
                gamepadList2.setOnItemClickListener(mGamepadListListener);
                gamepadView.setVisibility(View.VISIBLE);
                activityTitle.setText(R.string.title_gamepad);
            } else {
                SetupGamePadList();
                RefreshGamePadList();
                gamepadList2.setOnItemClickListener(mGamepadListListener);
            }
        }
    }

    public void SetMenuData() {
        String[] menulists;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || BuildConfig.ANDROID_7_SUPPORT_IMU) {
            menulists = new String[]{getString(R.string.menu_gamepad), getString(R.string.menu_imu), getString(R.string.menu_about)};
        } else {
            menulists = new String[]{getString(R.string.menu_gamepad), getString(R.string.menu_about)};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menulists);
        menulistView.setAdapter(adapter);
    }


    public void SetupGamePadList() {
        GamepadVO g;
        int list = 0;
        datas.clear();
        List<DeviceModel> models = mDeviceManager.getBondedDevices();
        if (models == null || models.size() == 0) {
            LogUtil.w("No gamepad connected or service not running...");
            g = new GamepadVO();
            g.setGamepadName(String.format("Gamepad %d", list));
            datas.add(g);
        } else {
            for (DeviceModel model : models) {
                if (!model.getMACAddress().equals(INIT_ADDRESS)) {
                    EventAddGamepad(model);
                    list++;
                }
            }
            if (datas.size() == 0) {
                // No Game pad
                g = new GamepadVO();
                g.setGamepadName(String.format("Gamepad %d", list));
                datas.add(g);
            }
        }
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

            IMUSensorTitle.setVisibility(View.GONE);
            MainTitle.setVisibility(View.VISIBLE);

            activityTitle.setText(R.string.gamepad_v2);
            menulist.setVisibility(View.VISIBLE);
        } else finish();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
                    OpenGamepadMenu();
                    break;
                case 1:
                    if (BuildConfig.ANDROID_7_SUPPORT_IMU || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    void OpenGamepadMenu() {
        // Gamepad Menu
        SetupGamePadList();
        RefreshGamePadList();
        gamepadList2.setOnItemClickListener(mGamepadListListener);
        gamepadView.setVisibility(View.VISIBLE);
        activityTitle.setText(R.string.title_gamepad);
    }

    void OpenIMUMenu() {
        // IMU Setting
        imuoptionLists.setVisibility(View.VISIBLE);
        MainTitle.setVisibility(View.GONE);
        IMUSensorTitle.setVisibility(View.VISIBLE);
    }

    void OpenAboutVersion() {
        // About App
        aboutLists.setVisibility(View.VISIBLE);
        activityTitle.setText(R.string.menu_about);
    }

    private ListView.OnItemClickListener mGamepadListListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            showGamepadsInfo(position);
        }
    };

    private void showGamepadsInfo(int position) {
        List<DeviceModel> models = mDeviceManager.getBondedDevices();
        if (models != null && models.size() != 0 && mUpgradeManager != null) {
            if (gamepadView.getVisibility() == View.VISIBLE) {
                FirmwareConfig config = mUpgradeManager.getNewVersion();
                ArrayList<DeviceModel> deviceList = mGamePadDeviceManager.getNeedUpgradedDevice(config);
                boolean online = false;

                for (DeviceModel model : deviceList) {
                    LogUtil.d("Device online? " + model.online());
                    if (datas.get(position).getMACAddress().equals(model.getMACAddress()) && model.online()) {
                        online = true;
                        targetDeviceMac = model.getMACAddress();
                        break;
                    } else {
                        targetDeviceMac = "none";
                    }
                }
                if (!online) {
                    LogUtil.d("Device not online");
                    return;
                }
                LogUtil.d("Need upgrade");
                Intent dialogIntent = new Intent(FOTAV2Main.this, DialogActivity.class);
                dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                dialogIntent.putExtra(INTENT_KEY, INTENT_FROM_USER);
                dialogIntent.putExtra(INTENT_MAC, targetDeviceMac);
                startActivity(dialogIntent);
            }
        }
    }

    private String targetDeviceMac = "none";

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
        g.setGamepadName(String.format("Gamepad %d"));
        datas.add(g);
    }

    public void EventAddGamepad(DeviceModel model) {
        UpgradeManager mgr = mDeviceManager.getUpgradeManager();
        GamepadVO g = new GamepadVO(getString(R.string.gamepad_one) + (model.getIndicator() + 1), model.getMACAddress(), model.getBatterVolt(), model.getFWVersion(), model.online(), mgr.getNewVersion().getmVersion());
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
                        Toast.makeText(FOTAV2Main.this, "Magic key!!!", Toast.LENGTH_LONG).show();
                        Intent dialogIntent = new Intent(FOTAV2Main.this, BackDoor.class);
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