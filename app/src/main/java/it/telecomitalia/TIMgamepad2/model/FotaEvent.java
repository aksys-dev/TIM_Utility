package it.telecomitalia.TIMgamepad2.model;

import android.bluetooth.BluetoothDevice;

public class FotaEvent {
    private byte mEventName;
    private BluetoothDevice mDevice;
    private int status;

    public FotaEvent(byte mEventName, BluetoothDevice mDevice) {
        this.mEventName = mEventName;
        this.mDevice = mDevice;
    }

    public byte getEventName() {
        return mEventName;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setEventName(byte mEventName) {
        this.mEventName = mEventName;
    }

    public void setDevice(BluetoothDevice mDevice) {
        this.mDevice = mDevice;
    }
}
