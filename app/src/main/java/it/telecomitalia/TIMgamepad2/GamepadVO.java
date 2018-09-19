package it.telecomitalia.TIMgamepad2;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class GamepadVO {
    protected final String unc = "Unknown";
    protected String GamepadName;
    protected String MACAddress;
    protected float Battery;
    protected String Firmware;
    boolean NeedUpdate;
    boolean mOnLine = false;

    private static final float MIN_BAT = 3000;
    private static final float MAX_BAT = 4250;

    public GamepadVO(String name, String mac, int batt, String firm, boolean online) {
        GamepadName = name;
        MACAddress = mac;

        Battery = ((batt - MIN_BAT) / (MAX_BAT - MIN_BAT)) * 100;
        Firmware = firm;
        NeedUpdate = false;
        mOnLine = online;
        log();
    }

    private void log() {
        LogUtil.d("GamepadName:" + GamepadName + "; MACAddress" + MACAddress + "; Battery" + Battery + "; Firmware" + Firmware + "; NeedUpdate" + NeedUpdate + "; mOnLine" + mOnLine);
    }

    public GamepadVO(String mac) {
        GamepadName = unc;
        MACAddress = mac;
        Battery = -1;
        Firmware = unc;
        NeedUpdate = false;
    }

    public GamepadVO() {
        GamepadName = unc;
        MACAddress = unc;
        Battery = -1;
        Firmware = unc;
        NeedUpdate = false;
    }

    public void setGamepadName(String name) {
        GamepadName = name;
    }

    public void setMACAddress(String mac) {
        MACAddress = mac;
    }

    public void setBattery(int value) {
        Battery = value;
    }

    public void setFirmware(String version) {
        Firmware = version;
    }

    public String getGamepadName() {
        return GamepadName;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public float getBattery() {
        return Battery;
    }

    public String getFirmware() {
        return Firmware;
    }
}
