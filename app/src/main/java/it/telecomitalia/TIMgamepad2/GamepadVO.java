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
    protected String latestFWVersion = "";

    //On gamepad side, Minimal battery is 3400
    private static final float MIN_BAT = 3000;
    private static final float MAX_BAT = 4150;

    public GamepadVO(String name, String mac, int batt, String firm, boolean online, String latestFWVersion) {
        this.GamepadName = name;
        this.MACAddress = mac;
//        LogUtil.d("battery is :" + batt);
        float validBattery = calibrateBattery(batt);
//        LogUtil.d("Calibrated battery: " + validBattery);
        this.Battery = ((validBattery - MIN_BAT) / (MAX_BAT - MIN_BAT)) * 100;
        this.Firmware = firm;
        this.NeedUpdate = false;
        this.mOnLine = online;
        this.latestFWVersion = latestFWVersion;
//        log();
    }

    private float calibrateBattery(int raw) {
        float coded = raw;
        if (raw > MAX_BAT) {
            coded = MAX_BAT;
        }

        if (raw < MIN_BAT) {
            coded = MIN_BAT;
        }

        return coded;
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

    public String getLatestFWVersion() {
        return latestFWVersion;
    }

    public boolean onLine() {
        return mOnLine;
    }
}
