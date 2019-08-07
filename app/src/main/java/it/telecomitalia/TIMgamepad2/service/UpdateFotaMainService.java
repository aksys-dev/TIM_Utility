package it.telecomitalia.TIMgamepad2.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.activity.DialogActivity;
import it.telecomitalia.TIMgamepad2.activity.FOTA_V2;
import it.telecomitalia.TIMgamepad2.activity.GamepadActivity;
import it.telecomitalia.TIMgamepad2.activity.UpgradeUIActivity;
import it.telecomitalia.TIMgamepad2.fota.AttachDevice;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.GamePadListener;
import it.telecomitalia.TIMgamepad2.fota.SPPConnection;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FabricModel;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.model.FotaEvent;
import it.telecomitalia.TIMgamepad2.model.UpdateModel;
import it.telecomitalia.TIMgamepad2.utils.CommerHelper;
import it.telecomitalia.TIMgamepad2.utils.FileUtils;
import it.telecomitalia.TIMgamepad2.utils.GamePadEvent;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;
import it.telecomitalia.TIMgamepad2.utils.SharedPreferenceUtils;

import static it.telecomitalia.TIMgamepad2.BuildConfig.CONFIG_FILE_NAME;
import static it.telecomitalia.TIMgamepad2.BuildConfig.KEY_SENSITIVE;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_SERVICE;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_FROM_USER;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_KEY;
import static it.telecomitalia.TIMgamepad2.activity.DialogActivity.INTENT_MAC;
import static it.telecomitalia.TIMgamepad2.fota.AttachDevice.getConnectedTargetDevice;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUT_MSG_GP_DEVICE_CONNECTED;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_STAUS_DOWNLOADING;


/**
 * Created by czy on 2018/7/24.
 */

public class UpdateFotaMainService extends Service implements GamePadListener {
    public static final String DIALOG_CANCEL_BROADCAST = "dialog_cancel_broadcast";
    public static final String DIALOG_OK_BROADCAST = "dialog_ok_broadcast";
    public static final int MSG_QUERY_FIRMWARE_VERSION = 0x100;
    public static final String KEY_MSG_FIRMWARE = "FIRMWARE_CONFIG";
    private static final String GAMEPAD_NAME_RELEASE = "TIMGamepad";
    private static final int FOREGROUND_ID = 9527;
    private static final String FOREGROUND_NOTIFICATION_CHANNEL_ID = "TIMGamepad_V2";
    private static final String ACTION_HID_STATUS_CHANGED = "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED";
    private static String PATH;
    private static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取本地蓝牙设备
    private static boolean driverReady = false;
    int waiting_counter = 0;
    private Context mContext;
    private Timer mTimer;
    private SPPConnection mainConnection;
    private String firmwareVersion = "";//获取的硬件版本号
    private String gamepadDownLoadUrl;
    private boolean isEnterFotaMainActivity = false;
    private boolean isshowndialog = false;//升级对话框是否弹出
    private boolean isConnect = false;//蓝牙是否连接
    private SharedPreferences sp;
    private FabricModel mFabricModel;
    private BluetoothDeviceManager mGamepadDeviceManager;
    private UpgradeManager mUpgradeManager;
    private boolean isUpgradeMode = false;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //  Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    //服务器下载完json文件后返回到这里
                    FirmwareConfig config = (FirmwareConfig) msg.getData().getSerializable(UpdateFotaMainService.KEY_MSG_FIRMWARE);
                    ArrayList<DeviceModel> deviceList = mGamepadDeviceManager.getNeedUpgradedDevice(config);

                    //有设备需要升级
                    if (!deviceList.isEmpty()) {
                        try {
                            FileUtils.compareVersion(config.getmDownUrl(), handler);
                        } catch (NullPointerException e) {
                            LogUtil.e(e.getMessage());
                            e.printStackTrace();
                        }
//                        Intent recommendationIntent = new Intent(mContext, UpdateRecommendationsService.class);
//                        mContext.startService(recommendationIntent);
                    }
                    break;
                case 2:
                    //固件下载完回到这里,弹出对话框提示用户升级
                    LogUtil.i("Firmware download finished");
                    if (!isTopActivityGamepad()) {
                        if (!isshowndialog) {
//                            Intent dialogIntent = new Intent(UpdateFotaMainService.this, DialogActivity.class);
//                            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            dialogIntent.putExtra(INTENT_KEY, INTENT_FROM_SERVICE);
//                            startActivity(dialogIntent);
//                            isshowndialog = true;

                            Intent intent = new Intent(getApplicationContext(), GamepadActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String channelID = getString(R.string.app_name);
                                String channelName = getString(R.string.app_name);
                                String channelDesc = getString(R.string.dailog_tips);

                                NotificationChannel ch = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);

                                if (notificationManager != null) {
                                    notificationManager.createNotificationChannel(ch);
                                } else {
                                    LogUtil.w(getString(R.string.logw_create_notification));
                                }

                                ch.enableLights(true);
                                ch.setLightColor(Color.YELLOW);
                                ch.enableVibration(true);
                                ch.setVibrationPattern(new long[]{100, 200, 300});
                                ch.setShowBadge(false);

                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID);
                                builder.setContentTitle(getString(R.string.firmware));
                                builder.setContentText(getString(R.string.dailog_tips));
                                builder.addAction(new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_share, getString(R.string.ok), pendingIntent).build());
                                builder.setSmallIcon(R.drawable.ic_launcher_foreground);
                                notificationManager.notify(112, builder.build());
                            }
                            else {
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

                                builder.setContentTitle(getString(R.string.firmware));
                                builder.setContentText(getString(R.string.dailog_tips));
//                                builder.addAction(new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.cancel), null).build());
                                builder.addAction(new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_share, getString(R.string.ok), pendingIntent).build());
                                builder.setSmallIcon(R.drawable.ic_launcher_foreground);
                                notificationManager.notify(112, builder.build());
                            }
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
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//            LogUtil.d("Daniel ACTION:" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //发现远程设备
//                LogUtil.i("ACTION_FOUND: " + device.getName());
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //指明一个与远程设备建立的低级别（ACL）连接。
//                LogUtil.d("ACL_CONNECTED device: :" + device.getAddress());
//                if (AttachDevice.isConnected(device)) {
//                } else {
//                    LogUtil.w("Ignoring the disconnected devices");
//                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //指明一个为远程设备提出的低级别（ACL）的断开连接请求，并即将断开连接。
//                LogUtil.d("ACTION_ACL_DISCONNECT_REQUESTED: " + device.getName());
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //指明一个来自于远程设备的低级别（ACL）连接的断开
//                LogUtil.d(device.getName() + "***********************************************BlueTooth disConnected");
//                if (mUpgradeDevice != null && mUpgradeDevice.getAddress().equals(device.getAddress()) && !isUpgradeMode) {
                //mGamepadDeviceManager.notifyDisconnectedDevice(device);
//                }
            } else if (action.equals(BluetoothAdapter.STATE_OFF)) {
                //LogUtil.i("STATE_OFF");
            } else if (action.equals(BluetoothAdapter.STATE_ON)) {
                //LogUtil.i("STATE_ON");
            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                //蓝牙扫描状态(SCAN_MODE)发生改变
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                //指明一个远程设备的连接状态的改变。比如，当一个设备已经被匹配。
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        mGamepadDeviceManager.notifyUnpairedDevice(device);
                        break;
                    case BluetoothDevice.BOND_BONDING:
//                        LogUtil.d("BOND_BONDING: " + device.getName());
                        break;
                    case BluetoothDevice.BOND_BONDED:
//                        LogUtil.d("BOND_BONDED: " + device.getName() + " / " + device.getAddress());
//                        new ConnectedPostThread(device).start();
                        break;
                }
            } else if (action.equals(DIALOG_CANCEL_BROADCAST)) {
                isshowndialog = false;
            } else if (action.equals(DIALOG_OK_BROADCAST)) {
                isshowndialog = true;
                String from = intent.getStringExtra(INTENT_KEY);
                if (from.equals(INTENT_FROM_USER)) {
                    String macAddress = intent.getStringExtra(INTENT_MAC);
                    LogUtil.d(String.format(getString(R.string.log_device_intent_to_be_upgraded), macAddress));
                    Intent intents = new Intent(UpdateFotaMainService.this, UpgradeUIActivity.class);
                    intents.putExtra(INTENT_MAC, macAddress);
                    intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intents);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SystemClock.sleep(1000);
                            EventBus.getDefault().post(BluetoothDeviceManager.EVENTBUS_MSG_NEED_UPGRADE);
                            SystemClock.sleep(1000);
                            EventBus.getDefault().post(new FotaEvent(FOTA_STAUS_DOWNLOADING, null, 0));
                        }
                    }).start();
                }
            } else if (action.equals(ACTION_HID_STATUS_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
//                LogUtil.i("HID state=" + state + ",device=" + device);
                if (state == BluetoothProfile.STATE_CONNECTED) {
                    LogUtil.d(String.format(getString(R.string.log_hid_device_connected), device.getAddress()));
                    new ConnectedPostThread(device).start();
                } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                    mGamepadDeviceManager.notifyDisconnectedDevice(device);
                    LogUtil.d(String.format(getString(R.string.log_hid_device_disconnected), device.getAddress()));
                }
            }
        }
    };

    NotificationManager notificationManager;
    @Override
    public void onCreate() {
        LogUtil.d("Creating utility service");
        EventBus.getDefault().register(this);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        super.onCreate();
    }

    private void processConnectedDevice() {

        Set<BluetoothDevice> mConnectedDevice = getConnectedTargetDevice();
        if (mConnectedDevice != null && mConnectedDevice.size() != 0) {
            for (BluetoothDevice device : mConnectedDevice) {
                if (AttachDevice.isConnected(device)) {
                    LogUtil.d("Handle Connected Device: " + device.getAddress());
                    new ConnectedPostThread(device).start();
                }
            }
        }
    }

    private void restartBTAdapter(Context context) {
        final BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (btAdapter.isEnabled()) {
            LogUtil.i("Restart Adapter(" + 4 + ")");
            btAdapter.disable();
            while (btAdapter.getState() != BluetoothAdapter.STATE_OFF) {
                SystemClock.sleep(200);
            }
            SystemClock.sleep(4000);
            btAdapter.enable();
        } else {
            btAdapter.enable();
        }
        while (btAdapter.getState() != BluetoothAdapter.STATE_ON) {
            SystemClock.sleep(100);
        }
        LogUtil.i("Adapter (Re)started");
    }

    private Notification buildForegroundNotification(Context context) {
        String CHANNEL_ID = FOREGROUND_NOTIFICATION_CHANNEL_ID;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            mChannel.setDescription(getString(R.string.desc_v2_notification));
            mChannel.enableLights(false);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(false);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            } else {
                LogUtil.w(getString(R.string.logw_create_notification));
            }
        }

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID);
//        Notification.Builder b = new Notification.Builder(context);

        b.setOngoing(true)
                .setContentTitle(context.getString(R.string.inno_gamepad_update))
                .setContentText(context.getString(R.string.gamepad_service_running));

        return (b.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//      return super.onStartCommand(intent, flags, startId);
        mContext = this;
//        restartBTAdapter(mContext);
        //Only use socket on android 7

        LogUtil.d(String.format(getString(R.string.log_running_api), Build.VERSION.SDK_INT));
        float sensitivityValue = (float) SharedPreferenceUtils.get(CONFIG_FILE_NAME, mContext, KEY_SENSITIVE, 1.0f);
        LogUtil.d(String.format(getString(R.string.log_sensor_sensitivity), String.valueOf(sensitivityValue)));
        sp = UpdateFotaMainService.this.getSharedPreferences(CommerHelper.SPNAME, Activity.MODE_PRIVATE);
        PATH = mContext.getCacheDir() + "/firmware/";

//        LogUtil.d("Utility service started");
        registerBTListener();
        mGamepadDeviceManager = BluetoothDeviceManager.getInstance();

        if (!mGamepadDeviceManager.isInitialized()) {
            mGamepadDeviceManager.initializeDevice(PATH, UpdateFotaMainService.this);

        }

        mUpgradeManager = mGamepadDeviceManager.getUpgradeManager();
        mUpgradeManager.startServerCycle(handler);
        processConnectedDevice();
        startForeground(FOREGROUND_ID, buildForegroundNotification(this));
        return START_STICKY;
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final String s) {
        if (s.equals("enter")) {
            isEnterFotaMainActivity = true;
            if (isConnect) {
                if (mainConnection != null) {
                    EventBus.getDefault().postSticky(mainConnection);
                }
            }
        } else if (s.equals("outer")) {
            LogUtil.i(getString(R.string.log_exited_FotaMainActivity));
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
        intentFilter.addAction(ACTION_HID_STATUS_CHANGED);
//        for (int i = 0; i < intentFilter.countActions(); i++) {
//            LogUtil.d("Action ==> " + intentFilter.getAction(i));
//        }
        return intentFilter;
    }

    private boolean isTopActivityGamepad() {
//        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
//        return cn.getPackageName().equals("it.telecomitalia.TIMgamepad2");
        return false;
    }

    @Override
    public void onSetupStatusChanged(boolean success, BluetoothDevice device) {
        if (success) {
            LogUtil.i(String.format(getString(R.string.log_setup_successfully), device.getName()));
            EventBus.getDefault().post(getString(R.string.event_bluetooth_connected));
            EventBus.getDefault().post(device);
//            Intent intentBroadcast = new Intent();
//            intentBroadcast.setAction(GAMEPAD_DEVICE_CONNECTED);
//            sendBroadcast(intentBroadcast);
            EventBus.getDefault().post(new GamePadEvent(EVENTBUT_MSG_GP_DEVICE_CONNECTED, device.getAddress()));
        } else {
            LogUtil.w(String.format(getString(R.string.log_setup_failed), device.getName()));
//            gotoGamepadList();
        }
    }

    private void gotoGamepadList() {
        Intent intents = new Intent(UpdateFotaMainService.this, FOTA_V2.class);
        intents.putExtra(INTENT_KEY, INTENT_FROM_SERVICE);
        intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intents);
    }

    private class ConnectedPostThread extends Thread {
        private BluetoothDevice mDevice;
        private int counter = 0;

        ConnectedPostThread(BluetoothDevice device) {
            mDevice = device;
        }

        @Override
        public void run() {
            while (!mGamepadDeviceManager.isConnected(mDevice)) {
                LogUtil.d(String.format(getString(R.string.log_wait_for_connection_ready), counter, mDevice.getAddress()));
                if (counter++ >= 10) {
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
//                LogUtil.d("setupDevice on Receiver");
                mGamepadDeviceManager.notifyConnectedDevice(mDevice, UpdateFotaMainService.this);

            }
        }
    }
}
