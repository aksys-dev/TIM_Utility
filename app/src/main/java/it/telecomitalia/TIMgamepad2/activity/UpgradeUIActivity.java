package it.telecomitalia.TIMgamepad2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.fota.SPPConnection.CMD_UPGRADE_FAILED;
import static it.telecomitalia.TIMgamepad2.fota.SPPConnection.CMD_UPGRADE_SUCCESS;

public class UpgradeUIActivity extends Activity {
    private Context mContext;
    private static String PATH;

    private ProgressBar mUpdateProgressBar;
    private Handler mHandler;
    private BluetoothDeviceManager mGamePadDeviceManager;
    private UpgradeManager mUpgradeManager;
    private Button mDoneButton;
    private TextView mDetailTv;
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LogUtil.d("onCreated called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade_ui);

        EventBus.getDefault().register(this);
        mContext = this;
        mHandler = new MainHandler();
        PATH = mContext.getCacheDir() + "/firmware/";
        initUI();
        initializeBluetoothManager();
    }

    private void initUI() {
        mUpdateProgressBar = findViewById(R.id.upgrade_progressbar);
        mResultText = findViewById(R.id.upgrade_result);
        mDoneButton = findViewById(R.id.upgrade_done);
        mDetailTv = findViewById(R.id.upgrade_details);
        mDetailTv.setText(R.string.upgrade_ing);
        mDoneButton.setVisibility(View.INVISIBLE);
        mDoneButton.setOnClickListener(listener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private boolean mInUpgrading = false;

    private void displayUpgradeStatus(boolean upgrade) {
//        mUpdateProgressBar.setVisibility(upgrade ? View.VISIBLE : View.GONE);
        mDetailTv.setText(getString(R.string.upgrade_ing));
//        mUpdateProgressBar.setVisibility(View.GONE);
        mResultText.setText(R.string.upgrade_success);
        mDoneButton.setVisibility(View.INVISIBLE);
    }


    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            mHandler.sendEmptyMessage(MSG_RECONNECT_SUCCESS);
            finish();
        }
    };

    private void initializeBluetoothManager() {
        LogUtil.d("initializeBluetoothManager");
        mGamePadDeviceManager = BluetoothDeviceManager.getDeviceManager();
//        mGamePadDeviceManager.initializeDevice(PATH);
        mUpgradeManager = mGamePadDeviceManager.getUpgradeManager();
    }

    @Override
    protected void onResume() {
        LogUtil.d("onResume called");
        mGamePadDeviceManager.queryDeviceFabricInfo();
        super.onResume();
    }

    /**
     * 按照顺序一个个升级,先升级第一个，如果后面还有，那么就开始升级第二个
     */
    private void startUpgradeByOrder(int index) {
        LogUtil.d("Start upgrade");
        Hashtable<String, DeviceModel> deviceMap = mGamePadDeviceManager.getConnectedDevices();
        Set<String> keySet = deviceMap.keySet();

        for (String key : keySet) {
            DeviceModel model = deviceMap.get(key);
            LogUtil.d("index : " + index + " newIndex : " + model.getIndicator());
            if (index == model.getIndicator() && !mInUpgrading && model.getDevice() != null) {
                mCurrentUpgradeModel = model;
                FabricModel fabricModel = deviceMap.get(key).getFabricModel();
                LogUtil.d("upgrade check ： " + fabricModel.needUpdate());
                if (fabricModel.needUpdate()) {
                    LogUtil.d("Need upgrade");
                    mInUpgrading = true;
                    mUpgradeManager.startUpgrade(mCurrentUpgradeModel, mHandler);
                    notifyUpgradeStatus(mCurrentUpgradeModel.getFabricModel(), true);
                    displayUpgradeStatus(true);
                } else {
                    LogUtil.d("No Need upgrade, Next");
                    startUpgradeByOrder(index + 1);
                }
                break;
            }
        }

//        List<DeviceModel> models = mGamePadDeviceManager.getConnectedDevices();
//        for (DeviceModel model : models) {
//            LogUtil.d(TAG, "Device: " + model.getMACAddress());
//            if (!mInUpgrading) {
//                mCurrentUpgradeModel = model;
//                FabricModel fabricModel = model.getFabricModel();
//                LogUtil.d(TAG, "upgrade check ： " + fabricModel.needUpdate());
//                if (fabricModel.needUpdate()) {
//                    mInUpgrading = true;
//                    mUpgradeManager.startUpgrade(mCurrentUpgradeModel, mHandler);
//                    notifyUpgradeStatus(mCurrentUpgradeModel.getFabricModel(), true);
//                    displayUpgradeStatus(true);
//                } else {
//                    startUpgradeByOrder();
//                }
//                break;
//            }
//        }
    }

    /**
     * 下载完数据，显示30%；
     * 传送升级包到手柄，显示90%
     * 发送COMMIT命令到手柄端，获取返回SUCCESS，显示100%
     */
    private void showUpgradeProcess(int percent) {
        mUpdateProgressBar.setProgress(percent);
        if (percent == 100) {
            Toast.makeText(UpgradeUIActivity.this, "Upgrade successfully", Toast.LENGTH_LONG).show();
            finish();
        } else if (percent == 0) {
//            mUpdateProgressBar.setVisibility(View.GONE);
            mResultText.setText(R.string.upgrade_failure);
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
    private DeviceModel mCurrentUpgradeModel;

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_RECONNECT_SUCCESS:
                    LogUtil.d("Send COMMIT Command");
                    notifyUpgradeStatus(mCurrentUpgradeModel.getFabricModel(), true);
                    mGamePadDeviceManager.updateFabric(mCurrentUpgradeModel.getDevice());
                    mCurrentUpgradeModel = mGamePadDeviceManager.getDeviceModelByAddress(mCurrentUpgradeModel.getDevice().getAddress());
                    displayUpgradeStatus(false);
                    //已经升级完了，可以做其他的操作了，比如更新UI，执行下一次升级等
                    mInUpgrading = false;
                    //开始下一个蓝牙设备升级
                    int nextIndex = mCurrentUpgradeModel.getIndicator() + 1;
                    startUpgradeByOrder(nextIndex);
                    break;
                case MSG_SET_PROGRESSBAR:
                    showUpgradeProcess(msg.arg1);
                    break;
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof String) {
            String event = (String) object;
            if (event.equals(BluetoothDeviceManager.EVENTBUS_MSG_NEED_UPGRADE)) {
                //启动来自UpdateFotaMainService的升级请求。
                startUpgradeByOrder(0);
            } else if (event.equals(BluetoothDeviceManager.EVENTBUS_MSG_QUERY_END)) {
                LogUtil.d("DisplayUpdateInfo");
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
            FotaEvent event = (FotaEvent) object;
            byte eventName = event.getEventName();
            BluetoothDevice device = event.getDevice();
            switch (eventName) {
                case CMD_UPGRADE_SUCCESS:
                    LogUtil.d("CMD_UPGRADE_SUCCESS");
                    mCurrentUpgradeModel = mGamePadDeviceManager.getDeviceModelByAddress(device.getAddress());
                    showUpgradeProcess(100);
                    break;
                case CMD_UPGRADE_FAILED:
                    LogUtil.d("CMD_UPGRADE_FAILED");
                    mCurrentUpgradeModel = mGamePadDeviceManager.getDeviceModelByAddress(device.getAddress());
                    showUpgradeProcess(0);
                    break;

            }

        }
    }

}
