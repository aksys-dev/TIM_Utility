package it.telecomitalia.TIMgamepad2.fota;


import android.os.SystemClock;

import it.telecomitalia.TIMgamepad2.Proxy.ProxyManager;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.model.Constant.TAG;

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
    public static final byte DATA_IMU_HEADER = (byte) 0x22;

    private BlueToothConnThread mConnectionThread;
    private DeviceModel mInfo;
    private boolean ready;
    private ProxyManager mProxyManager = ProxyManager.getInstance();

    private static final String UNKNOWN = "unknown";

    private String mFirmwareVersion = UNKNOWN;
    private int mBatteryVolt = -1;

    SPPConnection(DeviceModel info) {
        LogUtil.d(TAG, "SPPConnection : " + info.getDevice().getName());
        mConnectionThread = new BlueToothConnThread(info, this, this);
        mInfo = info;
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

//    public void enableSensor(boolean enable) {
//        int bytes;
//        byte[] recv = new byte[22];
//        if (enable) {
//            mConnectionThread.write(CMD_ENABLE_IMU);
//
//            bytes = mConnectionThread.read(recv);
//            String msg = "enable received: " + bytes + " byte:";
//            msg += " 0x" + Integer.toHexString(combineHighAndLowByte(recv[0], recv[1]));
//            LogUtil.d(msg);
//
//            mConnectionThread.startSPPDataListener();
//        } else {
//            mConnectionThread.write(CMD_DISABLE_IMU);
//
//            bytes = mConnectionThread.read(recv);
//            String msg = "Disable received: " + bytes + " byte:";
//            msg += " 0x" + Integer.toHexString(combineHighAndLowByte(recv[0], recv[1]));
//            LogUtil.d(msg);
//
//            mConnectionThread.stopSPPDataListener();
//        }
//    }

    public String getDeviceFirmwareVersion() {
        return mFirmwareVersion;
//        byte[] recv = new byte[57];
//        String result = "";
//
//        mConnectionThread.write(CMD_QUERY_FW_VERSION);
//        int size = mConnectionThread.read(recv);
//
//        ReceivedData data = new ReceivedData(recv, size);
//        if (data.mCmd == CMD_QUERY_FW_VERSION) {
//            result = data.mResult;
//            LogUtil.i(TAG, "Version: " + result);
//        }
//        return result;
    }

    public int getDeviceBattery() {
//        LogUtil.d(TAG, "query battery");
//        byte[] recv = new byte[57];
//        String result = "";
//
//        mConnectionThread.write(CMD_GET_BAT_V);
//        int size = mConnectionThread.read(recv);
//
//        LogUtil.d(TAG, "byte : " + recv + " cmd " + CMD_GET_BAT_V);
//        ReceivedData data = new ReceivedData(recv, size);
//        if (data.mCmd == CMD_GET_BAT_V) {
//            return combineHighAndLowByte(recv[2], recv[3]);
//        }
//        return -1;
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


    public String imuSwitch(byte imuData) {
        LogUtil.i(TAG, "sppSend: " + imuData);
        byte[] recv = new byte[57];
        mConnectionThread.write(imuData);
        mConnectionThread.read(recv);
        String result = new String(recv);

        LogUtil.i(TAG, "sppBack: " + result);
        return result;
    }

    public ReceivedData execCMD(byte cmd) {
        byte[] recv = new byte[100];
        mConnectionThread.write(cmd);
        int size = mConnectionThread.read(recv);
        ReceivedData data = new ReceivedData(recv, size);
        return data;
    }

    public void fotaOn(byte imuData) {
        LogUtil.i(TAG, "SPP Send: " + imuData);
        mConnectionThread.write(imuData);
    }

    public void fotaOn(String cmd) {
        mConnectionThread.write(cmd.getBytes());
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
//        SPPData sppdata = mConnectionThread.read();
//        int size = sppdata.getSize();
//        byte[] data = sppdata.getData();
//        LogUtil.d("received data: " + size + " bytes");
//        String text = null;
//        for (int i = 0; i < size; i++) {
//            text = text + " " + data[i];
//        }
//        LogUtil.d("" + text);
//
//        return data[1];
    }

    public void sendData(byte[] data) {
        mConnectionThread.write(data);
    }

    public int waitAck(byte[] reply) {
        LogUtil.d(TAG, "waitAck Called");
        return mConnectionThread.read(reply);
    }

    @Override
    public void onConnectionReady(boolean b) {
        ready = b;
        mConnectionThread.startSPPDataListener();
        setGamePadLedIndicator();
        SystemClock.sleep(500);
        queryFirmwareVersion();
        SystemClock.sleep(500);
        queryFirmwareBatteryLevel();

    }

    public boolean isReady() {
        return ready;
    }

    private void queryFirmwareVersion() {
        mConnectionThread.write(CMD_QUERY_FW_VERSION);
    }

    private void queryFirmwareBatteryLevel() {
        mConnectionThread.write(CMD_GET_BAT_V);
    }

    @Override
    public void onDataArrived(byte[] data, int size) {
        LogUtil.d("On " + size + " bytes data arrived");
        switch (data[0]) {
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
                LogUtil.d("CMD_QUERY_FW_VERSION");
                mFirmwareVersion = new String(data, 2, size);
                LogUtil.d("Firmware version on device: " + mFirmwareVersion);
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
                LogUtil.d("Moto OFF");
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
                LogUtil.d(TAG, "CMD_GET_BAT_V ");
                mBatteryVolt = combineHighAndLowByte(data[2], data[3]);
                LogUtil.d("BatteryVolt: " + mBatteryVolt);
                break;
            case DATA_IMU_HEADER:
                LogUtil.d("DATA_IMU_HEADER: " + data[0]);
                if (checkValid(data, size)) {
                    mProxyManager.send(new byte[]{0x09, data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15], data[16], data[17]});
                }
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
        return (size == 22 && data[1] == 0x55);
    }
}
