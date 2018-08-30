package it.telecomitalia.TIMgamepad2.fota;


import it.telecomitalia.TIMgamepad2.model.DeviceInfo;
import it.telecomitalia.TIMgamepad2.model.SPPData;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.model.Constant.TAG;

/**
 * Created by D on 2018/1/18 0018.
 */

public class SPPConnection implements ConnectionReadyListener {
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

    /*public static final String IMU_OPEN="UPDATE ENABLE";
    public static final String IMU_CLOSE="UPDATE DISABLE";
    public static final String SET_MODEL_PC="set_mode0";
    public static final String SET_MODEL_GAME="set_mode1";
    public static final String SET_MODEL_ANDROID="set_mode2";
    public static final String SET_MODEL_CTV="set_mode3";
    public static final String GET_MODEL_PC="get_mode";
    public static final String GET_MODEL_CTV_SUMSUNG="set_cTV_mode1";
    public static final String GET_MODEL_CTV_LG="set_cTV_mode2";
    public static final String GET_MODEL_CTV="get_cTV_mode";
    public static final String UPDATE_ENABLE="FOTA_ON";//升级时，首先需要进入升级模式
    public static final String UPDATE_DISABLE="FOTA_OFF";//退出升级模式
    public static final String GVERSION="GVERSION";//升级模式时，获取版本号
    public static final String COMMIT_OK="COMMITOK";//升级成功重连之后发送这个*/


    private BlueToothConnThread mConnectionThread;
    private DeviceInfo mInfo;
    private boolean ready;

    public SPPConnection(DeviceInfo info, IMURawDataListener listener) {
        mConnectionThread = new BlueToothConnThread(info, listener, this);
        mInfo = info;
    }

    public SPPConnection(DeviceInfo info) {
        LogUtil.d(TAG, "SPPConnection : " + info.getDevice().getName());
        mConnectionThread = new BlueToothConnThread(info, this);
        mInfo = info;
    }

    public SPPConnection(DeviceInfo info, IMUDataListener listener) {
//        mConnectionThread = new BlueToothConnThread(info, listener, this);
//        mInfo = info;
    }

    private static int combineHighAndLowByte(byte high, byte low) {
        return (high << 8) | (low & 0xFF);
    }

    public DeviceInfo getDeviceInfo() {
        return mInfo;
    }

    public void init() {
        mConnectionThread.start();
    }

    public void deinit() {
        mConnectionThread.cancel();
    }

    public void enableSensor(boolean enable) {
        int bytes;
        byte[] recv = new byte[22];
        if (enable) {
            mConnectionThread.write(CMD_ENABLE_IMU);

            bytes = mConnectionThread.read(recv);
            String msg = "enable received: " + bytes + " byte:";
            msg += " 0x" + Integer.toHexString(combineHighAndLowByte(recv[0], recv[1]));
            LogUtil.d(msg);

            mConnectionThread.startReceiveIMUDate();
        } else {
            mConnectionThread.write(CMD_DISABLE_IMU);

            bytes = mConnectionThread.read(recv);
            String msg = "Disable received: " + bytes + " byte:";
            msg += " 0x" + Integer.toHexString(combineHighAndLowByte(recv[0], recv[1]));
            LogUtil.d(msg);

            mConnectionThread.stopReceiveIMUDate();
        }
    }

    public String getDeviceFirmwareVersion() {
        byte[] recv = new byte[57];
        String result = "";

        mConnectionThread.write(CMD_QUERY_FW_VERSION);
        int size = mConnectionThread.read(recv);

        ReceivedData data = new ReceivedData(recv, size);
        if (data.mCmd == CMD_QUERY_FW_VERSION) {
            result = data.mResult;
            LogUtil.i(TAG, "Version: " + result);
        }
        return result;
    }

    public int getDeviceBattery() {
        LogUtil.d(TAG, "query battery");
        byte[] recv = new byte[57];
        String result = "";

        mConnectionThread.write(CMD_GET_BAT_V);
        int size = mConnectionThread.read(recv);

        LogUtil.d(TAG, "byte : " + recv + " cmd " + CMD_GET_BAT_V);
        ReceivedData data = new ReceivedData(recv, size);
        if (data.mCmd == CMD_GET_BAT_V) {
            return combineHighAndLowByte(recv[2], recv[3]);
        }
        return -1;
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

    public int setDeviceChannel(byte channel) {
        byte[] cmd = new byte[2];
        cmd[0] = CMD_SET_CHANNEL;
        cmd[1] = channel;
        mConnectionThread.write(cmd);
        SPPData sppdata = mConnectionThread.read();
        int size = sppdata.getSize();
        byte[] data = sppdata.getData();
        LogUtil.d("received data: " + size + " bytes");
        String text = null;
        for (int i = 0; i < size; i++) {
            text = text + " " + data[i];
        }
        LogUtil.d("" + text);

        return data[1];

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
    }

    public boolean getReady() {
        return ready;
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
}
