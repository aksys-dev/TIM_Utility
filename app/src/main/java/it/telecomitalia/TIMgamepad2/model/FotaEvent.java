package it.telecomitalia.TIMgamepad2.model;

import android.bluetooth.BluetoothDevice;

public class FotaEvent {
    public static final int FOTA_STAUS_DOWNLOADING = 1;
    public static final int FOTA_STAUS_FLASHING = 2;
    public static final int FOTA_STATUS_DONE = 3;

    public static final int FOTA_UPGRADE_SUCCESS = 0;
    public static final int FOTA_UPGRADE_FAILURE = -1;

    private int mEventName;
    private BluetoothDevice mDevice;
    private int status;

    public FotaEvent(int mEventName, BluetoothDevice device, int status) {
        this.mEventName = mEventName;
        this.mDevice = device;
        this.status = status;
    }

    public int getEventName() {
        return mEventName;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }


    public void setDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }

    public int getStatus() {
        return this.status;
    }
}
