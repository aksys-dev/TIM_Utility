package it.telecomitalia.TIMgamepad2.model;

import android.text.TextUtils;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

/**
 * Created by cmx on 2018/8/14.
 */

public class FabricModel {

    public String mPreviousVersion;
    public String mUpgradeVersion;

    public String mGamepadHWVersion;
    public boolean connectStatus;
    public FirmwareConfig mFirmwareConfig;
    public boolean mInUpgrading = false;
    private boolean mNeedUpdate;
    public int mBattery;
    public static final int BATTERY_LOW = 0x01;
    public static final int BATTERY_MIDDLE = 0x01;
    public static final int BATTERY_HIGH = 0x01;
    private static final int LOW = 3200;
    private static final int MIDDLE = 3800;
    private static final int HIGH = 4200;

    public boolean needUpdate() {
        LogUtil.d("Test", "Compare old : " + mPreviousVersion + " new : " + mUpgradeVersion);
        if(!TextUtils.isEmpty(mUpgradeVersion)) {
            return mUpgradeVersion.compareTo(mPreviousVersion) > 0;
        }
        return false;
    }

    public boolean needUpdate(String newVersion) {
        if(!TextUtils.isEmpty(newVersion) && !TextUtils.isEmpty(mPreviousVersion)) {
            return newVersion.compareTo(mPreviousVersion) > 0;
        }
        return false;
    }

    public int getBatteryLevel() {
        int level = BATTERY_LOW;
        if(mBattery > MIDDLE && mBattery < HIGH) {
            level = BATTERY_MIDDLE;
        } else if(mBattery > HIGH) {
            level = BATTERY_HIGH;
        }
        return level;
    }
}
