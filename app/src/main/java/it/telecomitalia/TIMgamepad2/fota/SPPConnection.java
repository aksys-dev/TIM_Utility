package it.telecomitalia.TIMgamepad2.fota;


import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import it.telecomitalia.TIMgamepad2.BuildConfig;
import it.telecomitalia.TIMgamepad2.Proxy.BinderProxyManager;
import it.telecomitalia.TIMgamepad2.Proxy.ProxyManager;
import it.telecomitalia.TIMgamepad2.model.FotaEvent;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.BuildConfig.TEST_A7_ON_A8;
import static it.telecomitalia.TIMgamepad2.fota.UpgradeManager.UPGRADE_CONNECTION_ERROR;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_STATUS_DONE;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_STAUS_FLASHING;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_UPGRADE_FAILURE;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_UPGRADE_SUCCESS;


/**
 * Created by D on 2018/1/18 0018.
 */

public class SPPConnection implements ConnectionReadyListener, SPPDataListener {

    public static final byte CMD_ENABLE_IMU = (byte) 0x80;
    public static final byte CMD_DISABLE_IMU = (byte) 0x81;
    public static final byte CMD_QUERY_IMU = (byte) 0x82;
    public static final byte CMD_SET_CHANNEL = (byte) 0x83;
    public static final byte CMD_QUERY_FW_VERSION = (byte) 0x84;//获取版本号
    public static final byte CMD_START_FW_UPGRADE = (byte) 0x85;
    public static final byte CMD_SET_MODEL_PC = (byte) 0x86;
    public static final byte CMD_SET_MODEL_GAME = (byte) 0x87;
    public static final byte CMD_SET_MODEL_ANDROID = (byte) 0x88;
    public static final byte CMD_GET_MODEL_PC = (byte) 0x89;
    public static final byte CMD_GET_MODEL_CTV_SUMSUNG = (byte) 0x8a;
    public static final byte CMD_GET_MODEL_CTV_LG = (byte) 0x8b;
    public static final byte CMD_GET_MODEL_CTV = (byte) 0x8c;
    public static final byte CMD_ENABLE_UPDATE_MODE = (byte) 0x8d;
    public static final byte CMD_DISABLE_UPDATE_MODE = (byte) 0x8e;
    public static final byte CMD_UPGRADE_SUCCESS = (byte) 0x8f;
    public static final byte CMD_UPGRADE_FAILED = (byte) 0x90;
    public static final byte CMD_MOTOR_ON = (byte) 0x91;
    public static final byte CMD_MOTOR_OFF = (byte) 0x92;
    public static final byte CMD_GET_BAT_V = (byte) 0x93;
    public static final byte DATA_IMU_HEADER_PREFIX = (byte) 0x00;
    public static final byte DATA_IMU_HEADER = (byte) 0x55;
    public static final byte CMD_PARTITION_VERIFY_FAIL = (byte) 0x94;
    public static final byte CMD_PARTITION_VERIFY_SUCCESS = (byte) 0x95;
    public static final byte CMD_OTA_WRITTEN_BYTES = (byte) 0x96;
    public static final byte CMD_OTA_DATA_RECEIVED = (byte) 0x97;
    //    public static final byte CMD_OTA_INTENT_REBOOT = (byte) 0x98;
    public static final byte CMD_ERROR_HEADER = (byte) 0x98;
    private static final int CMD_HEADER = 0;
    private static final int CMD_PAYLOAD = 1;
    //    public static final byte CMD_OTA_INTENT_REBOOT = (byte) 0x98;
    private static final int IMU_FRAME_SIZE = 22;

    private static final byte CMD_VIBRATE_VALUE0 = (byte) 0x00;
    private static final byte CMD_VIBRATE_VALUE1 = (byte) 0x01;
    private static final byte CMD_VIBRATE_VALUE2 = (byte) 0x02;

    private static final int INDEX_CMD = 0;
    private static final int INDEX_STATUS = 1;
    private static final int INDEX_LENGTH = 2;
    private static final int INDEX_DATA_START = 3;
    private static final String UNKNOWN = "unknown";
    private final static float TOTAL_SIZE = 91374;
    private static final int interval = 200; //(4s)50Hz = 1 second
    private static final int IMUMonitorInterval = 2000;
    private static int interval_counter = 0;
    private boolean checked = false;
    private BlueToothConnThread mConnectionThread;
    private DeviceModel mInfo;
    private BinderProxyManager mBinderProxy = BinderProxyManager.getInstance();
    private ProxyManager mProxyManager;// = ProxyManager.getInstance();
    private String mFirmwareVersion = UNKNOWN;
    private int mBatteryVolt = -1;
    private GamePadListener mGamepadListener;
    private String mPath = "";
    private Handler mHandler;
    private int retries = 0;
    private float sentDataSize = 0;
//    private int mIMUTimeoutCounter = 0;
//    private volatile boolean mIMUTimeout = false;
//    private volatile boolean monitoring = true;
//    private IMUStatusCheckThread imuStatusCheckThread;//= new IMUStatusCheckThread();

    SPPConnection(DeviceModel info, GamePadListener listener) {
        mConnectionThread = new BlueToothConnThread(info, this, this);
        mInfo = info;
        mGamepadListener = listener;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && BuildConfig.ANDROID_7_SUPPORT_IMU) {
            mProxyManager = ProxyManager.getInstance();
        }

        if (TEST_A7_ON_A8) {
            mProxyManager = ProxyManager.getInstance();
        }
    }

    private static int combineHighAndLowByte(byte high, byte low) {
        return (high << 8) | (low & 0xFF);
    }

    public void start() {
        mConnectionThread.start();
    }

    public void stop() {
        mConnectionThread.cancel();
        mConnectionThread = null;
//        monitoring = false;
//        imuStatusCheckThread.interrupt();
    }

    private void recoverySPPConnection() {
        SystemClock.sleep(1000);
        if (mInfo != null && mInfo.getDevice() != null) {
            LogUtil.d("Do recovery");
            mConnectionThread.cancel();
            SystemClock.sleep(100);
            mConnectionThread = null;
//        LogUtil.d("mInfo=" + mInfo + "; Devices+" + mInfo.getDevice());

            mConnectionThread = new BlueToothConnThread(mInfo, this, this);
            mConnectionThread.start();
            checked = false;
        } else {
            LogUtil.w("Device unpaired from user, Ignore");
        }
    }

    public String getDeviceFirmwareVersion() {
        return mFirmwareVersion;
    }

    public int getDeviceBattery() {
        return mBatteryVolt;
    }

    private void setGamePadLedIndicator() {
        try {
            LogUtil.d("Channel:" + mInfo.getIndicator());
            byte[] cmd = new byte[2];
            cmd[CMD_HEADER] = CMD_SET_CHANNEL;
            cmd[CMD_PAYLOAD] = mInfo.getIndicator();
            mConnectionThread.write(cmd);
        } catch (NullPointerException e) {
            LogUtil.e(e.toString());
            e.printStackTrace();
        }
    }

    private void setGamePadIMU() {
        if (mInfo.imuEnabled()) {
            LogUtil.d("Enable the IMU actually");
            mConnectionThread.write(CMD_ENABLE_IMU);
        } else {
            LogUtil.d("Disable none predefined device's IMU");
            mConnectionThread.write(CMD_DISABLE_IMU);
        }
    }

    public void fotaOn(byte cmd) {
        LogUtil.i("SPP Send: " + cmd);
        mConnectionThread.write(cmd);
    }

    public void setWorkMode(byte workMode) {
        mConnectionThread.write(workMode);
    }

    private boolean sendData(byte[] data) {
        return mConnectionThread.write(data);
    }

    synchronized boolean startUpgradeProcess(byte[] data) {
        byte[] firmware = data.clone();
        return sendData(firmware);
    }

    public void vibrator(int left, int right) {

        byte[] output = new byte[3]; // Vibrate Stream
        output[0] = CMD_MOTOR_ON;
        if (left == 2) output[1] = CMD_VIBRATE_VALUE2;
        else if (left == 1) output[1] = CMD_VIBRATE_VALUE1;
        else if (left == 0) output[1] = CMD_VIBRATE_VALUE0;

        if (right == 2) output[2] = CMD_VIBRATE_VALUE2;
        else if (right == 1) output[2] = CMD_VIBRATE_VALUE1;
        else if (right == 0) output[2] = CMD_VIBRATE_VALUE0;
        mConnectionThread.write(output);

    }

    synchronized void startUpgrade(String path, final Handler handler, boolean internal) {
        if (internal) {
            retries++;
            if (retries >= 30) {
                mHandler.sendEmptyMessage(UPGRADE_CONNECTION_ERROR);
                retries = 0;
            }
        } else {
            retries = 0;
        }
        try {
            mHandler = handler;
            LogUtil.d("Start upgrade process");
            mPath = path;
            FileInputStream fileInputStream = new FileInputStream(new File(path));
            int len = fileInputStream.available();
            LogUtil.d("file length : " + len);
            byte[] buffer = new byte[len];
            int size = fileInputStream.read(buffer);
            LogUtil.i("File：" + len + " bytes. Will send：" + buffer.length + "(" + size + ") bytes. Transmitting, Please wait......");
            if (!startUpgradeProcess(buffer)) {
                if (handler != null)
                    handler.sendEmptyMessage(UPGRADE_CONNECTION_ERROR);
            }
            LogUtil.d("Data finished");
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int waitAck(byte[] reply) {
        LogUtil.d("waitAck Called");
        return mConnectionThread.read(reply);
    }

    @Override
    public void onConnectionReady(boolean connected) {
//        LogUtil.i("onConnectionReady(" + connected + ")");
        if (connected && mConnectionThread != null) {
            mConnectionThread.startSPPDataListener();
//            mIMUTimeout = false;
//            if (mInfo.getIndicator() == 0) {
//                monitoring = false;
//                if (imuStatusCheckThread != null) {
//                    SystemClock.sleep(IMUMonitorInterval + 200);
//                    imuStatusCheckThread.stop();
//                    imuStatusCheckThread = null;
//                }
//                monitoring = true;
//                imuStatusCheckThread = new IMUStatusCheckThread();
//                imuStatusCheckThread.start();
//            }
            mConnectionThread.write(CMD_UPGRADE_SUCCESS);
            SystemClock.sleep(200);
            setGamePadLedIndicator();
            SystemClock.sleep(200);
            queryFirmwareVersion();
            SystemClock.sleep(200);
            queryBatteryLevel();

        } else {
            mGamepadListener.onSetupStatusChanged(false, mInfo.getDevice());
//            monitoring = false;
        }
    }

    @Override
    public void onConnectionException(Exception e) {
        LogUtil.w("SPP connection exception: " + e.getMessage());
//        LogUtil.i("Recovery the SPP connection");
        recoverySPPConnection();
    }

    private void queryFirmwareVersion() {
        mConnectionThread.write(CMD_QUERY_FW_VERSION);
    }

    private void queryBatteryLevel() {
        mConnectionThread.write(CMD_GET_BAT_V);
    }

    @Override
    public void onDataArrived(byte[] data, int size) {
        //LogUtil.d("FrameHeader: [" + HexToString(data[INDEX_CMD]) + "]");
        switch (data[INDEX_CMD]) {
            case CMD_ENABLE_IMU:
                LogUtil.d("CMD_ENABLE_IMU");
                break;
            case CMD_DISABLE_IMU:
                LogUtil.d("CMD_DISABLE_IMU");
                break;
            case CMD_QUERY_IMU:
                LogUtil.d("CMD_QUERY_IMU");
                break;
            case CMD_SET_CHANNEL:
                LogUtil.d("CMD_SET_CHANNEL");
                break;
            case CMD_QUERY_FW_VERSION:
                mFirmwareVersion = new String(data, INDEX_DATA_START, 6);
                LogUtil.d("Firmware version on device: " + mFirmwareVersion);
                mInfo.setFirmwareVersion(mFirmwareVersion);
                mInfo.getFabricModel().mPreviousVersion = mFirmwareVersion;
                mInfo.setFabricModel(mInfo.getFabricModel());
                break;
            case CMD_START_FW_UPGRADE:
                LogUtil.d("CMD_START_FW_UPGRADE");
                break;
            case CMD_SET_MODEL_PC:
            case CMD_SET_MODEL_GAME:
            case CMD_SET_MODEL_ANDROID:
            case CMD_GET_MODEL_PC:
            case CMD_GET_MODEL_CTV_SUMSUNG:
            case CMD_GET_MODEL_CTV_LG:
            case CMD_GET_MODEL_CTV:
                break;
            case CMD_ENABLE_UPDATE_MODE:
                LogUtil.d("CMD_ENABLE_UPDATE_MODE");
                sentDataSize = 0;
                break;
            case CMD_DISABLE_UPDATE_MODE:
                LogUtil.d("CMD_DISABLE_UPDATE_MODE");
                break;
            case CMD_UPGRADE_SUCCESS:
                LogUtil.d("CMD_UPGRADE_SUCCESS");
                break;
            case CMD_UPGRADE_FAILED:
                LogUtil.d("CMD_UPGRADE_FAILED");
                break;
            case CMD_MOTOR_ON:
                LogUtil.d("CMD_MOTOR_ON");
                break;
            case CMD_MOTOR_OFF:
                LogUtil.d("CMD_MOTOR_OFF");
                break;
            case CMD_GET_BAT_V:
                mBatteryVolt = combineHighAndLowByte(data[INDEX_DATA_START], data[INDEX_DATA_START + 1]);
                LogUtil.d("BatteryVolt: " + mBatteryVolt);
                mInfo.setBatteryVolt(mBatteryVolt);
                //Enable IMU if android 7 or higher version
                if (!checked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || BuildConfig.ANDROID_7_SUPPORT_IMU) {
                        setGamePadIMU();
                    }
                    mGamepadListener.onSetupStatusChanged(true, mInfo.getDevice());
                    checked = true;
                }
                break;
            case DATA_IMU_HEADER_PREFIX:
                if (data[1] == DATA_IMU_HEADER && size == IMU_FRAME_SIZE) {
//                    mIMUTimeoutCounter++;
                    //Use binder instead of socket on android 8 or higher version
                    byte[] event = new byte[]{0x09, data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17]};
//                    if (interval_counter % interval == 0) {
//                        LogUtil.d("Frames from GP: " + HexToString(event));
//                        interval_counter = 1;
//                    }
//                    interval_counter++;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mBinderProxy.send(event);
                    } else {
                        if (BuildConfig.ANDROID_7_SUPPORT_IMU)
                            mProxyManager.send(event);
                    }
                    if (TEST_A7_ON_A8) {
                        mProxyManager.send(event);
                    }
                }
                break;
            case CMD_PARTITION_VERIFY_FAIL:
                LogUtil.d("CMD_PARTITION_VERIFY_FAIL");
                EventBus.getDefault().post(new FotaEvent(FOTA_STATUS_DONE, mInfo.getDevice(), FOTA_UPGRADE_FAILURE));
                break;
            case CMD_PARTITION_VERIFY_SUCCESS:
                LogUtil.d("CMD_PARTITION_VERIFY_SUCCESS");
                EventBus.getDefault().post(new FotaEvent(FOTA_STATUS_DONE, mInfo.getDevice(), FOTA_UPGRADE_SUCCESS));
                sentDataSize = 0;
                break;
            case CMD_OTA_WRITTEN_BYTES:
                sentDataSize += combineHighAndLowByte(data[2], data[3]);
                int percent = (int) ((sentDataSize / TOTAL_SIZE) * 100);
                EventBus.getDefault().post(new FotaEvent(FOTA_STAUS_FLASHING, mInfo.getDevice(), percent));
                if (percent % 20 == 0) {
                    LogUtil.d("CMD_OTA_WRITTEN_BYTES: percent = " + percent);
                }
                break;
            case CMD_OTA_DATA_RECEIVED:
                LogUtil.d("CMD_OTA_END_TAG");
                break;
            case CMD_ERROR_HEADER:
                LogUtil.d("CMD_ERROR_HEADER, Try to send the data again");
                SystemClock.sleep(800);
                startUpgrade(mPath, mHandler, true);
//              mHandler.sendEmptyMessage(UPGRADE_CONNECTION_ERROR);
                break;
            default:
                LogUtil.e("Unknown command!");
                break;
        }
    }

    public static class ReceivedData {
        public byte mCmd;
        public String mResult;
        private byte mLength;

        public ReceivedData(byte[] data, int size) {
            mCmd = data[0];
            mLength = data[1];
            mResult = new String(data, 2, size);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CMD : " + mCmd).append("\n");
            builder.append("RESULT : " + mResult);
            return builder.toString();
        }
    }

//    private class IMUStatusCheckThread extends Thread {
//        @Override
//        public void run() {
//            while (monitoring) {
//                SystemClock.sleep(IMUMonitorInterval);
//                if (!mIMUTimeout) {
////                    LogUtil.d("IMU fresh rate:" + mIMUTimeoutCounter / 2 + " Hz");
//                    if (mIMUTimeoutCounter < 5) {
//                        mIMUTimeoutCounter = 0;
//                        mIMUTimeout = true;
//                        LogUtil.w("Lack of IMU events:(" + mIMUTimeoutCounter + "), Restart monitor...");
//                        onConnectionException(new Exception("IMU Time Out"));
//                        break;
//                    }
//                } else {
//                    LogUtil.d("Waiting for the IMU data recovery");
//                }
//                mIMUTimeoutCounter = 0;
//            }
//            super.run();
//        }
//    }
}
