package it.telecomitalia.TIMgamepad2.fota;


import android.os.SystemClock;

import org.greenrobot.eventbus.EventBus;

import it.telecomitalia.TIMgamepad2.Proxy.ProxyManager;
import it.telecomitalia.TIMgamepad2.model.FotaEvent;
import it.telecomitalia.TIMgamepad2.utils.CommerHelper;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;


/**
 * Created by D on 2018/1/18 0018.
 */

public class SPPConnection implements ConnectionReadyListener, SPPDataListener {

    private static final int CMD_HEADER = 0;
    private static final int CMD_PAYLOAD = 1;

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
    public static final byte CMD_OTA_DATA_RECEVIED = (byte) 0x97;
    public static final byte CMD_OTA_INTENT_REBOOT = (byte) 0x98;

    private static final int IMU_FRAME_SIZE = 22;

    private static final int INDEX_CMD = 0;
    private static final int INDEX_STATUS = 1;
    private static final int INDEX_LENGTH = 2;
    private static final int INDEX_DATA_START = 3;

    private BlueToothConnThread mConnectionThread;
    private DeviceModel mInfo;
    private boolean ready;
    private ProxyManager mProxyManager = ProxyManager.getInstance();

    private static final String UNKNOWN = "unknown";

    private String mFirmwareVersion = UNKNOWN;
    private int mBatteryVolt = -1;

    private GamePadListener mGamepadListener;

    SPPConnection(DeviceModel info, GamePadListener listener) {
        LogUtil.d("SPPConnection : " + info.getDevice().getName());
        mConnectionThread = new BlueToothConnThread(info, this, this);
        mInfo = info;
        mGamepadListener = listener;
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
    }


    public String getDeviceFirmwareVersion() {
        return mFirmwareVersion;
    }

    public int getDeviceBattery() {
        return mBatteryVolt;
    }

    private void setGamePadLedIndicator() {
        //TODO: send the led indicator number to device
        try {
            LogUtil.d("Header:" + CMD_SET_CHANNEL + "; Channel:" + mInfo.getIndicator());
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

    public ReceivedData getResultWithoutRequest() {
        byte[] reply = new byte[64];
        int size = waitAck(reply);
        return new ReceivedData(reply, size);
    }

    public void startFirmwareUpgrade() {
        mConnectionThread.write(CMD_START_FW_UPGRADE);
    }


    public void getIMUstate() {
        mConnectionThread.write(CMD_QUERY_IMU);
    }

    public void setDeviceChannel(byte channel) {
        byte[] cmd = new byte[2];
        cmd[0] = CMD_SET_CHANNEL;
        cmd[1] = channel;
        mConnectionThread.write(cmd);
    }

    public void sendData(byte[] data) {
        mConnectionThread.write(data);
    }

    public int waitAck(byte[] reply) {
        LogUtil.d("waitAck Called");
        return mConnectionThread.read(reply);
    }

    @Override
    public void onConnectionReady(boolean b) {
        ready = b;
        mConnectionThread.startSPPDataListener();
        mConnectionThread.write(CMD_UPGRADE_SUCCESS);
        SystemClock.sleep(200);
        setGamePadLedIndicator();
        SystemClock.sleep(200);
        setGamePadLedIndicator();
        SystemClock.sleep(200);
        queryFirmwareVersion();
        SystemClock.sleep(200);
        queryBatteryLevel();
        mGamepadListener.onSetupSuccessfully(true, mInfo.getDevice());
    }

    public boolean isReady() {
        return ready;
    }

    private void queryFirmwareVersion() {
        mConnectionThread.write(CMD_QUERY_FW_VERSION);
    }

    private void queryBatteryLevel() {
        mConnectionThread.write(CMD_GET_BAT_V);
    }

    private void rebootDeviceForFota() {
        mConnectionThread.write(CMD_OTA_INTENT_REBOOT);
    }

    @Override
    public void onDataArrived(byte[] data, int size) {
        if (size != 22) {
            LogUtil.d("On " + size + " bytes data arrived");
            LogUtil.d(CommerHelper.HexToString(data, size));
        }
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
//                mGamepadListener.onUpgradeMode(true, mInfo.getDevice());
                break;
            case CMD_DISABLE_UPDATE_MODE:
                LogUtil.d("CMD_DISABLE_UPDATE_MODE");
//                mGamepadListener.onUpgradeMode(false, mInfo.getDevice());
                break;
            case CMD_UPGRADE_SUCCESS:
                LogUtil.d("CMD_UPGRADE_SUCCESS");
                EventBus.getDefault().post(new FotaEvent(CMD_UPGRADE_SUCCESS, mInfo.getDevice()));
                break;
            case CMD_UPGRADE_FAILED:
                LogUtil.d("CMD_UPGRADE_FAILED");
                EventBus.getDefault().post(new FotaEvent(CMD_UPGRADE_FAILED, mInfo.getDevice()));
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
                setGamePadIMU();
                break;
            case DATA_IMU_HEADER_PREFIX:
                if (data[1] == DATA_IMU_HEADER && size == IMU_FRAME_SIZE) {
                    mProxyManager.send(new byte[]{0x09, data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17]});
                }
                break;
            case CMD_PARTITION_VERIFY_FAIL:
                LogUtil.d("CMD_PARTITION_VERIFY_FAIL");
                break;
            case CMD_PARTITION_VERIFY_SUCCESS:
                LogUtil.d("CMD_PARTITION_VERIFY_SUCCESS");
                break;
            case CMD_OTA_WRITTEN_BYTES:
                LogUtil.d("CMD_OTA_WRITTEN_BYTES: ");
                break;
            case CMD_OTA_DATA_RECEVIED:
                LogUtil.d("CMD_OTA_END_TAG");
                break;
            default:
                LogUtil.e("Unknown command!");
                break;
        }
    }

    public static class ReceivedData {
        public byte mCmd;
        public byte mLength;
        public String mResult;

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

    private boolean checkValid(byte[] data, int size) {
        return (size == 22);
    }
}
