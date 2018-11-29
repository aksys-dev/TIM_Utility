package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothDevice;

import it.telecomitalia.TIMgamepad2.model.FabricModel;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class DeviceModel {

    private BluetoothDevice mDevice;
    private SPPConnection mSPPConnection;
    private FabricModel mFabricModel;
    private int mIndex;

    private static final int INIT_INDICATOR = -1;
    public static final String INIT_ADDRESS = "undefined";
    private static final boolean INIT_ENABLED = false;
    private static final String INIT_NAME = "undefined";
    private String mGamedName = "undefined";
    private byte mLedIndicator = -1;

    private String mMACAddress = "undefined";
    private boolean mIMUEnabled = false;
    private boolean sppReady = false;

    private int mBatteryVolt = -1;

    private String mDeviceFWVersion = null;

    DeviceModel() {
        mDevice = null;
        mSPPConnection = null;
        mFabricModel = null;
        mFabricModel = new FabricModel();
        mIndex = -1;
    }

    public synchronized void reset() {
        mLedIndicator = INIT_INDICATOR;
        mMACAddress = INIT_ADDRESS;
        mIMUEnabled = INIT_ENABLED;
        mGamedName = INIT_NAME;
        mDevice = null;
        mSPPConnection = null;
    }


    public byte getIndicator() {
        return mLedIndicator;
    }

    public synchronized boolean imuEnabled() {
        return mIMUEnabled;
    }

    public String getMACAddress() {
        return mMACAddress;
    }

    public String getGamePadName() {
        return mGamedName;
    }


    public BluetoothDevice getBlueToothDevice() {
        return mDevice;
    }

    public String getFWVersion() {
        return mDeviceFWVersion;
    }

    public void setFirmwareVersion(String version) {
        mDeviceFWVersion = version;
    }

    public boolean online() {
        return mSPPConnection != null;
    }

    public synchronized void setLedIndicator(byte indicator) {
        mLedIndicator = indicator;
    }

    public synchronized void setIMUEnable(boolean enable) {
        mIMUEnabled = enable;
    }

    public synchronized void setGamePadName(String name) {
        mGamedName = name;
    }


    public synchronized void setBlueToothDevice(BluetoothDevice device) {
        mDevice = device;
    }

    public synchronized void setAddress(String address) {
        mMACAddress = address;
    }

    public void setFabricModel(FabricModel mode) {
        mFabricModel = mode;
    }

    public FabricModel getFabricModel() {
        return mFabricModel;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public SPPConnection getSPPConnection() {
        return mSPPConnection;
    }

    public void setSPPConnection(SPPConnection sppConnection) {
        mSPPConnection = sppConnection;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setBatteryVolt(int volt) {
        mBatteryVolt = volt;
    }

    public int getBatterVolt() {
        return mBatteryVolt;
    }

    public void print() {
        LogUtil.d("[0]=" + mGamedName + "\n[1]=" + mLedIndicator + "\n[2]=" + mSPPConnection.toString() + "\n[3]=" + mDevice.toString() + "\n[4]=" + mMACAddress + "\n[5]=" + mIMUEnabled + "\n[6]=" + sppReady);
    }

    public synchronized void fill(String name, String address, byte indicator, boolean imuEnabled) {
        this.mLedIndicator = indicator;
        this.mIMUEnabled = imuEnabled;
        this.mMACAddress = address;
        this.mGamedName = name;
    }
}
