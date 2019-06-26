package it.telecomitalia.TIMgamepad2.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Hashtable;
import java.util.Set;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.FabricController;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FabricModel;
import it.telecomitalia.TIMgamepad2.model.FotaEvent;
import it.telecomitalia.TIMgamepad2.utils.GamePadEvent;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_SERVICE;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_KEY;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_MAC;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUS_MSG_NEED_UPGRADE;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUT_MSG_GP_DEVICE_CONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUT_MSG_GP_DEVICE_DISCONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.UpgradeManager.UPGRADE_CONNECTION_ERROR;
import static it.telecomitalia.TIMgamepad2.fota.UpgradeManager.UPGRADE_FAILED;
import static it.telecomitalia.TIMgamepad2.fota.UpgradeManager.UPGRADE_TIMEOUT;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_STATUS_DONE;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_STAUS_DOWNLOADING;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_STAUS_FLASHING;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_UPGRADE_FAILURE;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_UPGRADE_SUCCESS;

public class UpgradeUIActivity extends Activity {

    private ProgressBar mUpdateProgressBar;
    private Handler mHandler;
    private BluetoothDeviceManager mGamePadDeviceManager;
    private UpgradeManager mUpgradeManager;
    private Button mDoneButton;
    private TextView mProgressText;

    private DeviceModel mTargetDevice;

    private String targetDeviceMAC = "none";

    private boolean aborted = false;

//    private IntentFilter makeFilter() {
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(GAMEPAD_DEVICE_CONNECTED);
//        intentFilter.addAction(BluetoothDeviceManager.GAMEPAD_DEVICE_DISCONNECTED);
//
//        return intentFilter;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LogUtil.d("onCreated called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade_ui);

//        EventBus.getDefault().register(this);
//        mContext = this;
        mHandler = new MainHandler();
        Intent i = getIntent();
        targetDeviceMAC = i.getStringExtra(INTENT_MAC);
        aborted = false;
//        PATH = mContext.getCacheDir() + "/firmware/";
        initUI();
        initializeBluetoothManager();
        mTargetDevice = mGamePadDeviceManager.getDeviceModelByAddress(targetDeviceMAC);
    }

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


    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mReceiver);
    }

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action != null) {
//                LogUtil.d("Action ======== " + action);
//                if (action.equals(GAMEPAD_DEVICE_DISCONNECTED)) {
//                    String mac = intent.getStringExtra(INTENT_MAC);
//                    if (mac != null) {
//                        if (mac.equals(targetDeviceMAC)) {
//                            LogUtil.d("Target gamepad" + mac + "(" + targetDeviceMAC + ") disconnected, So abort......");
//                            aborted = true;
//                            mProgressText.setTextColor(getResources().getColor(R.color.red));
//                            mProgressText.setText(R.string.upgrading_gamepad_disconnected);
//                            mDoneButton.setVisibility(View.VISIBLE);
//                        } else {
//                            LogUtil.d("Not the target device, go on: " + mac + "(" + targetDeviceMAC + ")");
//                        }
//                    } else {
//                        LogUtil.d("Do nothing......");
//                    }
////                    List<DeviceModel> gamepads = mGamePadDeviceManager.getBondedDevices();
////                    boolean found = false;
////                    for (DeviceModel gamepad : gamepads) {
////                        LogUtil.d("Action ======== 1");
////                        if (gamepad.getMACAddress().equals(targetDeviceMAC) && gamepad.online()) {
////                            LogUtil.d("Action ======== 2");
////                            found = true;
////                            break;
////                        }
////                    }
////                    if (!found) {
////                        LogUtil.d("The upgrading device has disconnected, abort!");
////                        mProgressText.setTextColor(getResources().getColor(R.color.red));
////                        mProgressText.setText(R.string.upgrading_gamepad_disconnected);
////                        mDoneButton.setVisibility(View.VISIBLE);
////                    }
////                    LogUtil.d("Action ======== 4");
//                }
//            }
//        }
//    };

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GamePadEvent event) {
        if (event.getMessage().equals(EVENTBUT_MSG_GP_DEVICE_CONNECTED)) {
//            Toast.makeText(this, "Upgrade UI received GamePad connected", Toast.LENGTH_LONG).show();
//            LogUtil.d("Upgrade UI received GamePad connected");
        } else if (event.getMessage().equals(EVENTBUT_MSG_GP_DEVICE_DISCONNECTED)) {
//            Toast.makeText(this, "GamePad disconnected", Toast.LENGTH_LONG).show();
//            LogUtil.d("Upgrade UI received GamePad disconnected");
            String mac = event.getMAC();
            if (mac != null) {
                if (mac.equals(targetDeviceMAC)) {
                    LogUtil.d(R.string.log_target_gamepad_disconnected, mac, targetDeviceMAC);
                    aborted = true;
                    mProgressText.setTextColor(getResources().getColor(R.color.red));
                    mProgressText.setText(R.string.upgrading_gamepad_disconnected);
                    mDoneButton.setVisibility(View.VISIBLE);
                } else {
                    LogUtil.d(R.string.log_not_the_target_device, mac, targetDeviceMAC);
                }
            } else {
                LogUtil.d("Do nothing......");
            }
        }
    }

    private void initUI() {
        mUpdateProgressBar = findViewById(R.id.upgrade_progressbar);
        mUpdateProgressBar.setMax(120);
        mUpdateProgressBar.setProgress(0);
        mProgressText = findViewById(R.id.upgrade_progress);
        mDoneButton = findViewById(R.id.upgrade_done);
        TextView mDetailTv = findViewById(R.id.upgrade_details);
        mDetailTv.setText(R.string.upgrade_ing);
        mProgressText.setText(R.string.upgrade_download);
        mUpdateProgressBar.setProgress(0);
        mDoneButton.setOnClickListener(listener);
        mDoneButton.setVisibility(View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void displayUpgradeStatus(FotaEvent event) {
        if (!aborted) {
            switch (event.getEventName()) {
                case FOTA_STAUS_DOWNLOADING:
                    mProgressText.setText(R.string.upgrade_download);
                    mUpdateProgressBar.setProgress(10);
                    startUpgradeProcess();
                    break;
                case FOTA_STAUS_FLASHING:
//                LogUtil.d("status " + event.getStatus());
                    mProgressText.setText(R.string.upgrade_flash);
                    mUpdateProgressBar.setProgress(20 + event.getStatus());
                    break;
                case FOTA_STATUS_DONE:
                    mProgressText.setText("");
                    mProgressText.setText(R.string.upgrade_done);
                    mUpdateProgressBar.setProgress(120);
                    switch (event.getStatus()) {
                        case FOTA_UPGRADE_SUCCESS:
                            mProgressText.setText(R.string.upgrade_success);
                            Toast.makeText(UpgradeUIActivity.this, R.string.toast_upgrade_success, Toast.LENGTH_LONG).show();
                            break;
                        case FOTA_UPGRADE_FAILURE:
                            mProgressText.setText(R.string.upgrade_failure);
                            Toast.makeText(UpgradeUIActivity.this, R.string.toast_upgrade_failed, Toast.LENGTH_LONG).show();
                            break;
                    }
                    backToGamepadList();
                    SystemClock.sleep(200);
                    finish();
                    break;
            }
        }
    }

    private void backToGamepadList() {
        Intent intents = new Intent(UpgradeUIActivity.this, FOTA_V2.class);
        intents.putExtra(INTENT_KEY, INTENT_FROM_SERVICE);
        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intents);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            backToGamepadList();
            SystemClock.sleep(200);
            finish();
        }
    };

    private void initializeBluetoothManager() {
        mGamePadDeviceManager = BluetoothDeviceManager.getInstance();
        mUpgradeManager = mGamePadDeviceManager.getUpgradeManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGamePadDeviceManager.queryDeviceFabricInfo();
//        registerReceiver(mReceiver, makeFilter());
    }

//    /**
//     * 按照顺序一个个升级,先升级第一个，如果后面还有，那么就开始升级第二个
//     */
//    private void startUpgradeByOrder(int index) {
//        LogUtil.d("Start upgrade");
//        Hashtable<String, DeviceModel> deviceMap = mGamePadDeviceManager.getConnectedDevices();
//        Set<String> keySet = deviceMap.keySet();
//
//        for (String key : keySet) {
//            DeviceModel model = deviceMap.get(key);
//            LogUtil.d("index : " + index + " newIndex : " + model.getIndicator());
//            if (index == model.getIndicator() && !mInUpgrading && model.getDevice() != null) {
//                mCurrentUpgradeModel = model;
//                FabricModel fabricModel = deviceMap.get(key).getFabricModel();
//                LogUtil.d("upgrade check ： " + fabricModel.needUpdate());
//                if (fabricModel.needUpdate()) {
//                    LogUtil.d("Need upgrade");
//                    mInUpgrading = true;
//                    mUpgradeManager.startUpgrade(mCurrentUpgradeModel, mHandler);
//                    notifyUpgradeStatus(mCurrentUpgradeModel.getFabricModel(), true);
//                } else {
//                    LogUtil.d("No Need upgrade, Next");
//                    startUpgradeByOrder(index + 1);
//                }
//                break;
//            }
//        }
//    }

    private void startUpgradeProcess() {
//        LogUtil.d("Start upgrade");
        Hashtable<String, DeviceModel> deviceMap = mGamePadDeviceManager.getConnectedDevices();
        Set<String> keySet = deviceMap.keySet();

        if (keySet.contains(mTargetDevice.getMACAddress())) {
            if (mTargetDevice.online()) {
                FabricModel fabricModel = mTargetDevice.getFabricModel();
//                LogUtil.d("upgrade check ： " + fabricModel.needUpdate());
                if (fabricModel.needUpdate()) {
//                    LogUtil.d("Need upgrade");
                    mUpgradeManager.startUpgrade(mTargetDevice, mHandler);
                    notifyUpgradeStatus(mTargetDevice.getFabricModel(), true);
                } else {
                    LogUtil.d(R.string.log_device_not_online_skip);
                }
            } else {
                LogUtil.d(R.string.log_upgrade_device_has_disconnected);
                mProgressText.setTextColor(getResources().getColor(R.color.red));
                mProgressText.setText(R.string.upgrading_gamepad_disconnected);
                mDoneButton.setVisibility(View.VISIBLE);
            }
        }
    }


    private void notifyUpgradeStatus(FabricModel model, boolean result) {
        String upgradeResult = result ? "Sucess" : "Fail";
        FabricController.getInstance().upgradeStatistics(model.mPreviousVersion, model.mUpgradeVersion, model.mGamepadHWVersion, upgradeResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static final int MSG_SET_PROGRESSBAR = 0x101;
    public static final int MSG_RECONNECT_SUCCESS = 0x200;

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case UPGRADE_FAILED:
                    aborted = true;
//                    LogUtil.d("The upgrading device failed, abort!");
                    mProgressText.setTextColor(getResources().getColor(R.color.red));
                    mProgressText.setText(R.string.upgrading_gamepad_failed);
                    mDoneButton.setVisibility(View.VISIBLE);
                    break;
                case UPGRADE_CONNECTION_ERROR:
                    aborted = true;
//                    LogUtil.d("The upgrading timeout, abort!");
                    mProgressText.setTextColor(getResources().getColor(R.color.red));
                    mProgressText.setText(R.string.upgrading_connection_timeout);
                    mDoneButton.setVisibility(View.VISIBLE);
                    break;
                case UPGRADE_TIMEOUT:
                    break;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof String) {
            String event = (String) object;
            if (event.equals(EVENTBUS_MSG_NEED_UPGRADE)) {
                //启动来自UpdateFotaMainService的升级请求。
//                LogUtil.d("EVENTBUS_MSG_NEED_UPGRADE");
            } else if (event.equals(BluetoothDeviceManager.EVENTBUS_MSG_QUERY_END)) {
//                LogUtil.d("DisplayUpdateInfo");
//                startUpgradeByOrder(0);
            }
//        } else if (object instanceof BluetoothDevice) {
//            BluetoothDevice device = (BluetoothDevice) object;
//            //检查是否是当前正在升级的蓝牙手柄，如果是，那么更新状态，并发送COMMIT命令。
//            if (device != null && device.getAddress().equals(mCurrentUpgradeModel.getDevice().getAddress())) {
//                mCurrentUpgradeModel = mGamePadDeviceManager.getDeviceModelByAddress(device.getAddress());
//                mHandler.sendEmptyMessage(MSG_RECONNECT_SUCCESS);
//                showUpgradeProcess(100);
//            }
        } else if (object instanceof FotaEvent) {
            displayUpgradeStatus((FotaEvent) object);

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_BUTTON_B:
                return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
