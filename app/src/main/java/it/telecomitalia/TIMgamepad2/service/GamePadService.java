package it.telecomitalia.TIMgamepad2.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import it.telecomitalia.TIMgamepad2.IGamePadService;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;

public class GamePadService extends Service {
    public static final int ERROR_NONE = 0;
    public static final int ERROR_UNKNOWN_MODE = 0xFFFF;

    public static final byte WORK_MODEL_PC = (byte) 0x86;
    public static final byte WORK_MODEL_GAME = (byte) 0x87;
    public static final byte WORK_MODEL_ANDROID = (byte) 0x88;

    private static final String TAG = "GamePadService";

    private ServiceBinder mGamePadBinder;

    private BluetoothDeviceManager mGamePadDeviceManager;

    @Override
    public void onCreate() {
        mGamePadDeviceManager = BluetoothDeviceManager.getDeviceManager();
        mGamePadBinder = new ServiceBinder();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(TAG, "onBind()");
        return mGamePadBinder;
    }

    private int setMode(byte work_mode) {
        switch (work_mode) {
            case WORK_MODEL_GAME:
            case WORK_MODEL_PC:
            case WORK_MODEL_ANDROID:
                List<DeviceModel> devices = mGamePadDeviceManager.getConnectedDevicesList();
                for (DeviceModel dev : devices) {
                    dev.getSPPConnection().setWorkMode(work_mode);
                }
                break;
        }
        return ERROR_NONE;
    }

    // Param: device indicator: 0 means Gamepad 1, 1 means gamepad 2...
    private String getVersionImpl(int device) {
        List<DeviceModel> devices = mGamePadDeviceManager.getConnectedDevicesList();
        for (DeviceModel dev : devices) {
            if (dev.getIndicator() == device) {
                return dev.getFWVersion();
            }
        }
        return null;
    }

    public class ServiceBinder extends IGamePadService.Stub {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String getVersion(int device) throws RemoteException {
            return getVersionImpl(device);
        }

        @Override
        public void setVibrationState(int id, int status) throws RemoteException {
            List<DeviceModel> devices = mGamePadDeviceManager.getConnectedDevicesList();
            for (DeviceModel dev : devices) {
                if (dev.getIndicator() == id) {
                    dev.getSPPConnection().setVibration(status);
                }
            }
        }

        @Override
        public int setGamePadMode(byte mode) throws RemoteException {
            return setMode(mode);
        }
    }
}
