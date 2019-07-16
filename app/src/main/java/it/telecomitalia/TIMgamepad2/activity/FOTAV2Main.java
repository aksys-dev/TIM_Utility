package it.telecomitalia.TIMgamepad2.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import it.telecomitalia.TIMgamepad2.BuildConfig;
import it.telecomitalia.TIMgamepad2.CalibrationGamepadVO;
import it.telecomitalia.TIMgamepad2.CalibrationListAdapter;
import it.telecomitalia.TIMgamepad2.GamepadListAdapter;
import it.telecomitalia.TIMgamepad2.GamepadVO;
import it.telecomitalia.TIMgamepad2.Proxy.BinderProxyManager;
import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.utils.GamePadEvent;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;
import it.telecomitalia.TIMgamepad2.utils.SharedPreferenceUtils;

import static it.telecomitalia.TIMgamepad2.BuildConfig.CONFIG_FILE_NAME;
import static it.telecomitalia.TIMgamepad2.BuildConfig.KEY_SENSITIVE;
import static it.telecomitalia.TIMgamepad2.BuildConfig.TEST_A7_ON_A8;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_SERVICE;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_USER;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_KEY;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_MAC;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUT_MSG_GP_DEVICE_CONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUT_MSG_GP_DEVICE_DISCONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.DeviceModel.INIT_ADDRESS;

public class FOTAV2Main extends AppCompatActivity {
    private static final boolean CALIBRATION_DEFAULT = false;

    ConstraintLayout MainTitle, IMUSensorTitle;
    ConstraintLayout menulist;
    ListView menulistView;
    FrameLayout gamepadView;
    ListView gamepadList2;
    LinearLayout imuoptionLists;
    ConstraintLayout aboutLists;
    ArrayList<GamepadVO> gamepadDatas;
    SeekBar seekBarSensitivity;
    TextView textSeekBarValue;
//    float sensitivityValue = 1.00F;
//    boolean calibrationEnabled = true;
//    Switch calibration;
//    FrameLayout frame_newVersion;
    TextView currentVersion, updateVersion;
    TextView lastUpdateDay;
    TextView activityTitle;
    private Context mContext;
    private UpgradeManager mUpgradeManager;
    private BluetoothDeviceManager mGamePadDeviceManager;
    //private BluetoothDeviceManager mDeviceManager;

    private BinderProxyManager mBinderProxy = BinderProxyManager.getInstance();
//    private ProxyManager mProxyManager = ProxyManager.getInstance();

    private int mMagicIndex = 0;
    private String targetDeviceMac = "none";
//    private ListView.OnItemClickListener mGamepadListListener = new ListView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//            showGamepadsInfo(position);
//        }
//    };
//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action != null) {
//                if (action.equals(GAMEPAD_DEVICE_CONNECTED) || action.equals(GAMEPAD_DEVICE_DISCONNECTED)) {
//                    gotoGamepadsListView(false);
//                }
//            }
//        }
//    };
//
//    private IntentFilter makeFilter() {
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(GAMEPAD_DEVICE_CONNECTED);
//        intentFilter.addAction(BluetoothDeviceManager.GAMEPAD_DEVICE_DISCONNECTED);
//        return intentFilter;
//    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 事件响应方法
     * 接收消息
     *
     * @param event
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GamePadEvent event) {
        if (event.getMessage().equals(EVENTBUT_MSG_GP_DEVICE_CONNECTED)) {
            LogUtil.d("GamePad connected");
            gotoGamepadsListView(false);
        } else if (event.getMessage().equals(EVENTBUT_MSG_GP_DEVICE_DISCONNECTED)) {
            LogUtil.d("GamePad disconnected");
            gotoGamepadsListView(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        float restoreValue = (float) SharedPreferenceUtils.get(CONFIG_FILE_NAME, mContext, KEY_SENSITIVE, SENSITIVE_DEFAULT);
//        calibrationEnabled = (boolean) SharedPreferenceUtils.get(CONFIG_FILE_NAME, mContext, KEY_CALIBRATION, CALIBRATION_DEFAULT);
//        calibration.setChecked(calibrationEnabled);
//        if (calibrationEnabled) {
//            calibration.setText(getStringResource(R.string.on));
//        } else {
//            calibration.setText(getStringResource(R.string.off));
//        }
//        textSeekBarValue.setText(String.valueOf(restoreValue));
//        seekBarSensitivity.setProgress((int) (restoreValue * 100));

//        LogUtil.i("calibrationEnabled-> " + calibrationEnabled);
//        LogUtil.i("sensitivity Value-> " + restoreValue);
//        LogUtil.i("progress-> " + seekBarSensitivity.getProgress());

//        calibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    calibration.setText(getStringResource(R.string.on));
//                } else {
//                    calibration.setText(getStringResource(R.string.off));
//                }
//                SharedPreferenceUtils.put(CONFIG_FILE_NAME, mContext, KEY_CALIBRATION, isChecked);
//            }
//        });


//        registerReceiver(mReceiver, makeFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fota_v2_main);
        mContext = this;
//        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_XLARGE) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//        MainTitle = findViewById(R.id.AppTitle);
//        IMUSensorTitle = findViewById(R.id.IMU_Title);
    
        mGamePadDeviceManager = BluetoothDeviceManager.getInstance();
        gamepadDatas = new ArrayList<>();

//        EventBus.getDefault().register(this);

//        activityTitle = findViewById(R.id.ActivityTitle);

        // App Main Menu
        menulist = findViewById(R.id.Menus);
        menulistView = findViewById(R.id.menuListView);
        menulistView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
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
        } );
        SetMenuData();

        // Gamepad Option
        gamepadView = findViewById(R.id.gamepadView2);
        gamepadList2 = findViewById(R.id.gamepadListView);
        gamepadList2.setOnItemClickListener( new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                showGamepadsInfo(position);
            }
        } );

        // IMU Sensor
//        imuoptionLists = findViewById(R.id.imuoptionLists);
//        aboutLists = findViewById(R.id.aboutLists);

//        seekBarSensitivity = findViewById(R.id.seekBar);
//        textSeekBarValue = findViewById(R.id.textSensitibity);
////        calibrationGamepadList = findViewById( R.id.calibrationlist );
////        calibrationGamepadList.setOnItemClickListener( new AdapterView.OnItemClickListener() {
////            @Override
////            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                TextView textView = view.findViewById( R.id.gamepad_name );
////                TextView addressData = view.findViewById( R.id.gamepad_address );
////                String addr = addressData.getText().toString();
////                String name = textView.getText().toString();
////                LogUtil.i( "Select " + name );
////                if (!textView.getText().toString().equals( getString(R.string.no_gamepad) )
////                        && !addr.equals( getResources().getText( R.string.gamepad_offline ) )) {
////                    LogUtil.i( "Mac Address " + addr );
////                    gotoIMUCalibrationScene( name, addr );
////                }
////            }
////        } );
////
////
//
////        calibration = findViewById(R.id.sw_calibration);
        String from = getIntent().getStringExtra(INTENT_KEY);
        if (from != null) {
            if (from.equals(INTENT_FROM_SERVICE)) {
                gotoGamepadsListView(true);
            }
        }

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && BuildConfig.ANDROID_7_SUPPORT_IMU) {
//            mProxyManager = ProxyManager.getInstance();
//        }
//
//        if (TEST_A7_ON_A8) {
//            mProxyManager = ProxyManager.getInstance();
//        }
    }
    
    //// GAMEPAD SCENE

    private void gotoGamepadsListView(boolean directly) {
        if (directly) {
            menulist.setVisibility(View.GONE);
            gamepadView.setVisibility(View.GONE);
            imuoptionLists.setVisibility(View.GONE);
            aboutLists.setVisibility(View.GONE);

            SetupGamePadList();
            gamepadView.setVisibility(View.VISIBLE);
            activityTitle.setText(R.string.title_gamepad);
        } else {
            if (gamepadView.getVisibility() == View.VISIBLE) {
                menulist.setVisibility(View.GONE);
                gamepadView.setVisibility(View.GONE);
                imuoptionLists.setVisibility(View.GONE);
                aboutLists.setVisibility(View.GONE);
                SetupGamePadList();
                gamepadView.setVisibility(View.VISIBLE);
                activityTitle.setText(R.string.title_gamepad);
            } else {
                SetupGamePadList();
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
        int list = 1;
        gamepadDatas.clear();
        List<DeviceModel> models = mGamePadDeviceManager.getBondedDevices();
        if (models == null || models.size() == 0) {
            LogUtil.w("No gamepad connected or service not running...");
            g = new GamepadVO();
            g.setGamepadName(String.format("Gamepad %d", list));
            gamepadDatas.add(g);
        } else {
            for (DeviceModel model : models) {
                if (!model.getMACAddress().equals(INIT_ADDRESS)) {
                    EventAddGamepad(model);
                    list++;
                }
            }
            if ( gamepadDatas.size() == 0) {
                // No Game pad
                g = new GamepadVO();
                g.setGamepadName(String.format("Gamepad %d", list));
                gamepadDatas.add(g);
            }
        }
    
        GamepadListAdapter adapter = new GamepadListAdapter(this, R.layout.gamepad_info, gamepadDatas );
        gamepadList2.setAdapter(adapter);
    }
    
    //// IMU SENSOR SCENE


    @Override
    public void onBackPressed() {
        // User press Back Button
//        if (menulist.getVisibility() == View.GONE) {
//            gamepadView.setVisibility(View.GONE);
//            imuoptionLists.setVisibility(View.GONE);
//            aboutLists.setVisibility(View.GONE);
//
//            activityTitle.setText(R.string.gamepad_v2);
//            menulist.setVisibility(View.VISIBLE);
//        } else
        finish();
    }

    @Override
    public void onDestroy() {
//        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    void OpenGamepadMenu() {
        // Gamepad Menu
        SetupGamePadList();
        gamepadView.setVisibility(View.VISIBLE);
        activityTitle.setText(R.string.title_gamepad);
    }

    void OpenIMUMenu() {
        // IMU Setting
//        SetupIMUEnabledGamepadList();
//        imuoptionLists.setVisibility(View.VISIBLE);
//        MainTitle.setVisibility(View.GONE);
//        IMUSensorTitle.setVisibility(View.VISIBLE);
        startActivity(new Intent(mContext, SetupIMUActivity.class));
    }

    void OpenAboutVersion() {
        // About App
        aboutLists.setVisibility(View.VISIBLE);
        activityTitle.setText(R.string.menu_about);
    }

    private void showGamepadsInfo(int position) {
        List<DeviceModel> models = mGamePadDeviceManager.getBondedDevices();
        if (models != null && models.size() != 0 && mUpgradeManager != null) {
            if (gamepadView.getVisibility() == View.VISIBLE) {
                FirmwareConfig config = mUpgradeManager.getNewVersion();
                ArrayList<DeviceModel> deviceList = mGamePadDeviceManager.getNeedUpgradedDevice(config);
                boolean online = false;

                for (DeviceModel model : deviceList) {
                    LogUtil.d("Device online? " + model.online());
                    if ( gamepadDatas.get(position).getMACAddress().equals(model.getMACAddress()) && model.online()) {
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
                Intent dialogIntent = new Intent(mContext, DialogActivity.class);
                dialogIntent.putExtra(INTENT_KEY, INTENT_FROM_USER);
                dialogIntent.putExtra(INTENT_MAC, targetDeviceMac);
                startActivity(dialogIntent);
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof BluetoothDevice) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) object;
            Crashlytics.log(Log.INFO, "FOTA2", "Detected Bluetooth: " + bluetoothDevice.getAddress());
            EventAddGamepad(bluetoothDevice.getAddress());
        }
//        else if ((object instanceof String) && ((String)object).contains("BlueTooth_Connected")) {
//            //蓝牙已连接
//            Toast.makeText(this, "BlueTooth_Connected", Toast.LENGTH_SHORT).show();
//        }
    }

    public void EventAddGamepad(String macAddress) {
        if (macAddress == getString(R.string.unknown)) return;
        GamepadVO g = new GamepadVO(macAddress);
//        Toast.makeText(this, "Detected Bluetooth: " + macAddress, Toast.LENGTH_SHORT).show();
        int i = 0;
        for (DeviceModel device : mGamePadDeviceManager.getBondedDevices()) {
            i++;
            if (device.getMACAddress().equals( macAddress )) {
                break;
            }
        }
        g.setGamepadName(String.format("Gamepad %d", i ));
        gamepadDatas.add(g);
    }

    public void EventAddGamepad(DeviceModel model) {
        UpgradeManager mgr = mGamePadDeviceManager.getUpgradeManager();
        GamepadVO g = new GamepadVO(getString(R.string.gamepad_one) + (model.getIndicator() + 1), model.getMACAddress(), model.getBatterVolt(), model.getFWVersion(), model.online(), mgr.getNewVersion().getmVersion());
        gamepadDatas.add(g);
    }
    
}