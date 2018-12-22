package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;


import java.util.HashSet;
import java.util.Set;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

/**
 * Created by D on 2017/9/19 0019.
 */

public class AttachDevice {

    public static final String GAMEPAD_NAME_RELEASE = "TIMGamepad";
    public static final String DEVICE_NOT_FOUND = "TARGET_DEVICE_NOT_CONNECTED";
    // Debugging
    private static final String TAG = "AttachDevice";
    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    // Member fields
    private static BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    public static Intent getTargetDevice() {

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        Intent intent = new Intent();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if (device.getName().contains(GAMEPAD_NAME_RELEASE)) {
                    LogUtil.d( "Target device attached : " + device.getName());
                    intent.putExtra(EXTRA_DEVICE_ADDRESS, device.getAddress());
                    return intent;
                }
            }
        }
        intent.putExtra(EXTRA_DEVICE_ADDRESS, DEVICE_NOT_FOUND);
        return intent;
    }


    public static int getConnectedTargetDeviceNumber() {
        int number = 0;
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices and find all all the connected device that we specified
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if (device.getName().contains(GAMEPAD_NAME_RELEASE)) {
                    number +=1;
                    LogUtil.d("Target device found, Total="+number);
                }
            }
        }
        return number;
    }

    public static Set<BluetoothDevice> getConnectedTargetDevice() {
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        Set<BluetoothDevice> targetDevices = new HashSet<>();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices and find all all the connected device that we specified
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if (device.getName().contains(GAMEPAD_NAME_RELEASE)) {
                    LogUtil.d("Target device found: "+device.getName()+"/"+device.getAddress());
                    targetDevices.add(device);
                }
            }
        }
        if (targetDevices.size() != 0) {
            return targetDevices;
        } else {
            return null;
        }
    }
}
