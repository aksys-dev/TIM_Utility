package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothDevice;

public interface GamePadListener {
    void onSetupStatusChanged(boolean success, BluetoothDevice device);
//    void onUpgradeMode(boolean upgrade, BluetoothDevice device);
}
