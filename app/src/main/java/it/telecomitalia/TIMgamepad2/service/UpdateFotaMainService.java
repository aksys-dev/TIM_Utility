package it.telecomitalia.TIMgamepad2.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Timer;

import it.telecomitalia.TIMgamepad2.Proxy.ProxyManager;
import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.activity.DialogActivity;
import it.telecomitalia.TIMgamepad2.activity.UpgradeUIActivity;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.GamePadListener;
import it.telecomitalia.TIMgamepad2.fota.SPPConnection;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FabricModel;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.model.UpdateModel;
import it.telecomitalia.TIMgamepad2.utils.CommerHelper;
import it.telecomitalia.TIMgamepad2.utils.FileUtils;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;


/**
 * Created by czy on 2018/7/24.
 */

public class UpdateFotaMainService extends Service implements GamePadListener {
    private static String PATH;
    private Context mContext;
    private Timer mTimer;
    private boolean isUpdating = false;
    private static final String GAMEPAD_NAME_RELEASE = "TIMGamepad";
    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取本地蓝牙设备
    private SPPConnection mainConnection;
    public static final String DIALOG_CANCEL_BROADCAST = "dialog_cancel_broadcast";
    public static final String DIALOG_OK_BROADCAST = "dialog_ok_broadcast";
    private String firmwareVersion = "";//获取的硬件版本号
    private String gamepadDownLoadUrl;
    private boolean isEnterFotaMainActivity = false;
    private boolean isshowndialog = false;//升级对话框是否弹出
    private boolean isConnect = false;//蓝牙是否连接
    private SharedPreferences sp;
    private FabricModel mFabricModel;
    private BluetoothDeviceManager mGamepadDeviceManager;
    private UpgradeManager mUpgradeManager;

    private static boolean driverReady = false;

    public static final int MSG_QUERY_FIRMWARE_VERSION = 0x100;
    public static final String KEY_MSG_FIRMWARE = "FIRMWARE_CONFIG";

    private boolean isUpgradeMode = false;

    private ProxyManager mProxy = ProxyManager.getInstance();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //服务器下载完json文件后返回到这里
                    FirmwareConfig config = (FirmwareConfig) msg.getData().getSerializable(UpdateFotaMainService.KEY_MSG_FIRMWARE);
                    ArrayList<DeviceModel> deviceList = mGamepadDeviceManager.getNeedUpgradedDevice(config);

                    LogUtil.d("isTopActivityGamepad : " + isTopActivityGamepad());
                    //有设备需要升级
                    LogUtil.d("deviceList empty : " + deviceList.isEmpty());
                    if (!deviceList.isEmpty()) {
                        try {
                            FileUtils.compareVersion(config.getmDownUrl(), handler);
                        } catch (NullPointerException e) {
                            LogUtil.e(e.getMessage());
                            e.printStackTrace();
                        }
                        Intent recommendationIntent = new Intent(mContext, UpdateRecommendationsService.class);
                        mContext.startService(recommendationIntent);
                    }

                    /*if (TextUtils.isEmpty(firmwareVersion)){
                        firmwareVersion = mainConnection.getDeviceFirmwareVersion().substring(8,14);
                    }

                    if(isConnect==true){
                        LogUtil.i(TAG,"需要升级，开始下载固件---->");
                        FileUtils.compareVersion(gamepadDownLoadUrl,handler);

                        Intent recommendationIntent = new Intent(mContext, UpdateRecommendationsService.class);
                        mContext.startService(recommendationIntent);
                    }*/
                    break;
                case 2:
                    //固件下载完回到这里,弹出对话框提示用户升级
                    LogUtil.i("固件下载完成---->");
                    if (!isTopActivityGamepad()) {
                        if (!isshowndialog) {
                            Intent dialogIntent = new Intent(UpdateFotaMainService.this, DialogActivity.class);
                            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(dialogIntent);
                            isshowndialog = true;
                        }
                    } else {
                        UpdateModel updateModel = new UpdateModel(mainConnection, true);
                        EventBus.getDefault().post(updateModel);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        mContext = this;
//        restartBTAdapter(mContext);
        checkDriver();
        sp = UpdateFotaMainService.this.getSharedPreferences(CommerHelper.SPNAME, Activity.MODE_PRIVATE);
        PATH = mContext.getCacheDir() + "/firmware/";
        registerBTListener();
    }

    private void restartBTAdapter(Context context) {
        final BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (btAdapter.isEnabled()) {
            LogUtil.i("Restarting Adapter");
            btAdapter.disable();
            while (btAdapter.getState() != BluetoothAdapter.STATE_OFF) {
                SystemClock.sleep(100);
            }
            btAdapter.enable();
        } else {
            btAdapter.enable();
        }
        while (btAdapter.getState() != BluetoothAdapter.STATE_ON) {
            SystemClock.sleep(100);
        }
        LogUtil.i("Adapter (Re)started");
    }

    private void checkDriver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mProxy.ready()) {
//                    LogUtil.d("Wait for driver ready...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                driverReady = true;
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerBTListener();
        mGamepadDeviceManager = BluetoothDeviceManager.getDeviceManager();

        if (!mGamepadDeviceManager.isInitialized()) {
            mGamepadDeviceManager.initializeDevice(PATH);

        }
        mUpgradeManager = mGamepadDeviceManager.getUpgradeManager();
        mUpgradeManager.startServerCycle(handler);
//      return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final String s) {
        if (s.equals("enter")) {
            LogUtil.i("进入了FotaMainActivity");
            isEnterFotaMainActivity = true;
            if (isConnect) {
                if (mainConnection != null) {
                    EventBus.getDefault().postSticky(mainConnection);
                }
            }
        } else if (s.equals("outer")) {
            LogUtil.i("退出了FotaMainActivity");
            isEnterFotaMainActivity = false;
        }
    }

    private void registerBTListener() {
        if (mBluetoothAdapter != null) {
            //Step 1: Enable BlueTooth if off
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            //Step 2: add bt status filter and register receiver
            registerReceiver(mReceiver, makeFilter());
        } else {
            Toast.makeText(this, getString(R.string.not_support_bt), Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        EventBus.getDefault().unregister(this);
    }

    private IntentFilter makeFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(DIALOG_CANCEL_BROADCAST);
        intentFilter.addAction(DIALOG_OK_BROADCAST);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        for (int i = 0; i < intentFilter.countActions(); i++) {
            LogUtil.d("Action ==> " + intentFilter.getAction(i));
        }
        return intentFilter;
    }

    private boolean isTopActivityGamepad() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        return cn.getPackageName().equals("it.telecomitalia.TIMgamepad2");
    }

    @Override
    public void onSetupSuccessfully(boolean success, BluetoothDevice device) {
        if (success) {
            LogUtil.i("Device (" + device.getName() + ") setup successfully");
            EventBus.getDefault().post("BlueTooth_Connected");
            EventBus.getDefault().post(device);
        } else {
            LogUtil.w("Device (" + device.getName() + " setup failed");
        }
    }

//    private BluetoothDevice mUpgradeDevice;
//
//    @Override
//    public void onUpgradeMode(boolean upgrade, BluetoothDevice device) {
//        if (upgrade) {
//            isUpgradeMode = true;
//            mUpgradeDevice = device;
//        } else {
//            isUpgradeMode = false;
//            mUpgradeDevice = device;
//        }
//    }

    private class ConnectedPostThread extends Thread {
        private BluetoothDevice mDevice;
        private int counter = 0;

        ConnectedPostThread(BluetoothDevice device) {
            mDevice = device;
        }

        @Override
        public void run() {
            while (!mGamepadDeviceManager.isConnected(mDevice)) {
                LogUtil.d("Waiting for the connection ready");
                if (counter++ >= 20) {
                    LogUtil.e("Device(" + mDevice.getAddress() + ") connect timeout, abort.");
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LogUtil.e(e.getMessage());
                    e.printStackTrace();
                }
            }

            if (mDevice.getName() != null && mDevice.getName().contains(GAMEPAD_NAME_RELEASE)) {
                LogUtil.d("setupDevice on Receiver");
                //添加一个设备并更新UI
                mGamepadDeviceManager.notifyConnectedDevice(mDevice, UpdateFotaMainService.this);

            }
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //发现远程设备
                LogUtil.i("ACTION_FOUND: " + device.getName());
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //指明一个与远程设备建立的低级别（ACL）连接。
                LogUtil.d("===============================================BlueTooth Connected");
                new ConnectedPostThread(device).start();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //指明一个为远程设备提出的低级别（ACL）的断开连接请求，并即将断开连接。
                LogUtil.d("ACTION_ACL_DISCONNECT_REQUESTED: " + device.getName());

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //指明一个来自于远程设备的低级别（ACL）连接的断开
                LogUtil.d(device.getName() + "***********************************************BlueTooth disConnected");
//                if (mUpgradeDevice != null && mUpgradeDevice.getAddress().equals(device.getAddress()) && !isUpgradeMode) {
                mGamepadDeviceManager.notifyDisconnectedDevice(device);
//                }
            } else if (action.equals(BluetoothAdapter.STATE_OFF)) {
                LogUtil.i("STATE_OFF");

            } else if (action.equals(BluetoothAdapter.STATE_ON)) {
                LogUtil.i("STATE_ON");

            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                //蓝牙扫描状态(SCAN_MODE)发生改变

            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                //指明一个远程设备的连接状态的改变。比如，当一个设备已经被匹配。
                LogUtil.i("ACTION_BOND_STATE_CHANGED");
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        mGamepadDeviceManager.notifyUnpairedDevice(device);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        LogUtil.d("BOND_BONDING: " + device.getName());
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        LogUtil.d("BOND_BONDED: " + device.getName() + " / " + device.getAddress());
                        break;
                }
            } else if (action.equals(DIALOG_CANCEL_BROADCAST)) {
                isshowndialog = false;
            } else if (action.equals(DIALOG_OK_BROADCAST)) {
                isshowndialog = true;
                Intent intents = new Intent(UpdateFotaMainService.this, UpgradeUIActivity.class);
                intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intents);
                /*UpdateModel updateModel = new UpdateModel(mainConnection,true);
                EventBus.getDefault().post(updateModel);*/
                LogUtil.d("EVENTBUS_MSG_NEED_UPGRADE");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SystemClock.sleep(1000);
                        EventBus.getDefault().post(BluetoothDeviceManager.EVENTBUS_MSG_NEED_UPGRADE);
                    }
                }).start();

            }
        }
    };

     /*
    private void setupDevice() {

       String name = device.getName();
        if (isBlutoothConnected(device)) {
            isConnect=true;
            DeviceInfo info = new DeviceInfo(device, device.getAddress());

            LogUtil.d("Target device: " + name + " found!");

            if (mainConnection == null) {
                mainConnection = new SPPConnection(info);
                mainConnection.init();
            }

            if (mTimer == null) {
                mTimer = new Timer();
            } else {
                mTimer.schedule(new TimerTask() {

                    public void run() {
                        LogUtil.i("进入来了");
                        downLoadJson();
                    }
                }, 2000, 300000);
            }
            EventBus.getDefault().postSticky(mainConnection);
            EventBus.getDefault().postSticky(device);


            if(sp.getBoolean(CommerHelper.IS_COMMIT, false)){
                //设备升级完之后重连之后需要发送这个命令才可以把新固件写入

                mainConnection.fotaOn(SPPConnection.CMD_UPGRADE_SUCCESS);

                byte[] reply = new byte[64];
                mainConnection.waitAck(reply);
                String content = new String(reply);

                sp.edit().putBoolean(CommerHelper.IS_COMMIT,false).commit();
                LogUtil.i(TAG,"进入serivce  commit发送--------"+content);

            }
           }
        }*/

    /**
     * 从服务器获取json信息，并存到本地对应地址
     */
/*    private void downLoadJson() {
        LogUtil.i(TAG,"UpdateFotaMainService进入到了downLoadJson");
        new Thread(new Runnable() {
            public void run()

            {
                try {
                    Thread.sleep(100);
                    try {

                        getJsonFromeServer(Constant.REMOTE_CONFIG_URL_GAMEPAD1_NEW, CONFIG_FILE_NAME_GAMEPAD1, handler);

                    } catch (Exception e) {
//                        sendErrorCode(e.toString(),handler);
                    }
                } catch (InterruptedException e) {
//                    sendErrorCode(e.toString(),handler);
                }
            }
        }).start();
    }*/

    /**
     * 判断是否需要升级
     */
   /* private boolean isToUpgrade() {
        String newversion = "";
        boolean needupdate = false;
        //把本地从服务器下载的json文件读取出来，获取里面的版本号
        File file = new File(PATH + CONFIG_FILE_NAME_GAMEPAD1);
        if (file.exists()) {
            FirmwareConfig mConfigs = getJsonFromLocal(PATH + CONFIG_FILE_NAME_GAMEPAD1);
            newversion = mConfigs.getmVersion();
            gamepadDownLoadUrl=mConfigs.getmDownUrl();
        } else {
            newversion = "";
        }

        if (TextUtils.isEmpty(firmwareVersion)){
            firmwareVersion = mainConnection.getDeviceFirmwareVersion().substring(8,14);
        }

        if(!TextUtils.isEmpty(newversion)){
            if (newversion.compareTo(firmwareVersion)>0){
                needupdate=true;
            }
        }
        LogUtil.i(TAG,"硬件的版本号:"+firmwareVersion+"---->服务器版本号"+newversion);
        EventBus.getDefault().post("FirmwareVersion"+firmwareVersion);
        EventBus.getDefault().post("ServerVersion"+newversion);

        mFabricModel.mPreviousVersion = firmwareVersion;
        mFabricModel.mUpgradeVersion = newversion;
        return needupdate;
    }*/
}
