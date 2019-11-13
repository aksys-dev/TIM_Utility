package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import it.telecomitalia.TIMgamepad2.R;
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
                    LogUtil.d(R.string.log_target_device_found_counts, number);
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
//                    LogUtil.d("Target device found: "+device.getName()+"/"+device.getAddress());
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

    public static boolean isConnected(BluetoothDevice device) {

        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到蓝牙状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(mBtAdapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {

                Set<BluetoothDevice> devices = mBtAdapter.getBondedDevices();

                if (devices.contains(device)) {
//                    LogUtil.i("Target device found:" + device.getAddress());
                    Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                    method.setAccessible(true);
                    boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);

                    if (isConnected && device.getAddress().equals(device.getAddress())) {
//                        LogUtil.i("Target connected:" + device.getAddress());
                        return true;
                    } else {
                        LogUtil.d(R.string.log_device_found_not_connected);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
