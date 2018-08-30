package it.telecomitalia.TIMgamepad2.model;

import android.bluetooth.BluetoothDevice;

/**
 * Created by D on 2018/1/18 0018.
 */

public class DeviceInfo {
    private BluetoothDevice mDevice;
    private int  mChannel;
    private String macAddr;

    public DeviceInfo(BluetoothDevice mDevice, int channel, String MAC) {
        this.mDevice = mDevice;
        this.mChannel = channel;
        this.macAddr = MAC;
    }

    public DeviceInfo(BluetoothDevice mDevice, String MAC) {
        this.mDevice = mDevice;
        this.mChannel = -1;
        this.macAddr = MAC;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public int getChannel() {
        return mChannel;
    }

    public String getMAC() {
        return macAddr;
    }


}
