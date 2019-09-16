package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import android.view.InputDevice;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.model.FabricModel;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.utils.GamePadEvent;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

/**
 * Created by cmx on 2018/8/17.
 * 从Service中抽离蓝牙的一些操作，用于维护所有的蓝牙设备，包括获取，更新等
 */

public class BluetoothDeviceManager {
    public static final String EVENTBUS_MSG_QUERY_END = "QueryDeviceEnd";
    public static final String EVENTBUS_MSG_NEED_UPGRADE = "NeedUpgrade";
    public static final String EVENTBUS_MSG_UPGRADE_SUCCESS = "UpgradeSuccess";
    public static final String EVENTBUS_MSG_STAUS_CHANGED = "status_changed";
    public static final String EVENTBUT_MSG_GP_DEVICE_CONNECTED = "TIM.V2.GAMEPAD_DEVICE_CONNECTED";
    public static final String EVENTBUT_MSG_GP_DEVICE_DISCONNECTED = "TIM.V2.GAMEPAD_DEVICE_DISCONNECTED";
    private static final String TAG = "BluetoothDeviceManager";
    private static final String GAMEPAD_NAME_RELEASE = "TIMGamepad";
    private static final int GP_SUPPORTED_MAX_NUM = 4;
    private static final int DEFAULT_IMU_DEVICE_INDEX = 0;
    private static BluetoothDeviceManager mDeviceManager;
    private static volatile boolean mInitialized = false;
    private Hashtable<String, DeviceModel> mGamePadMap = new Hashtable<>(4);
    private UpgradeManager mUpgradeManager;
    private int mTargetDeviceCount = 0;
    private List<DeviceModel> mDevices;
    
    private BluetoothAdapter _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothDeviceManager() {

    }

    public static BluetoothDeviceManager getInstance() {
        BluetoothDeviceManager temp = BluetoothDeviceManagerSingleton.INSTANCE.getInstance();
//        LogUtil.d("The instance is: " + temp);
        return temp;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public synchronized void initializeDevice(String path, Context context) {
        if (mInitialized) return;
        LogUtil.d(TAG, R.string.log_devicemanager_initialize);
        makeEmptyDeviceList();
        mUpgradeManager = new UpgradeManager(path);
        DeviceLocalCache.getInstance().restore(mDevices);
        mInitialized = true;
    }

    private void makeEmptyDeviceList() {
        mDevices = new ArrayList<>(GP_SUPPORTED_MAX_NUM);
        for (int i = 0; i < GP_SUPPORTED_MAX_NUM; i++) {
            mDevices.add(new DeviceModel());
        }
    }

    private boolean invalidGamePad(BluetoothDevice device) {
        return (device.getName() == null) || !(device.getName().contains(GAMEPAD_NAME_RELEASE));
    }

    private void enableSensor(DeviceModel gamepad) {
        int indicator = gamepad.getIndicator();
        if (indicator == DEFAULT_IMU_DEVICE_INDEX) {
            LogUtil.d(R.string.log_enable_imu_gamepad_address, gamepad.getMACAddress());
            gamepad.setIMUEnable(true);

        } else {
            LogUtil.d(R.string.log_not_the_default_imu_device, gamepad.getMACAddress());
            gamepad.setIMUEnable(false);
        }
    }

    private byte findFreeSeat(List<DeviceModel> gamepads) {
        byte index;
        for (index = 0; index < gamepads.size(); index++) {
            if (gamepads.get(index).getIndicator() == -1) { //Means this seat didn't occupied by other gamepad
//                LogUtil.d("Free seat found: index->" + index);
                return index;
            }
        }
        return -1;
    }

    private byte findIdleSeat(List<DeviceModel> gamepads) {
        byte index;
        for (index = 0; index < gamepads.size(); index++) {
            if (gamepads.get(index).getSPPConnection() == null) { //Means this seat didn't occupied by other gamepad at now
//                LogUtil.d("Idle seat found: index->" + index);
                return index;
            }
        }
        return -1;
    }


    private void processNewDevice(byte seat, BluetoothDevice device, GamePadListener listener) {
        DeviceModel gp = mDevices.get(seat);
        gp.setLedIndicator(seat);
        gp.setGamePadName(device.getName());
        gp.setAddress(device.getAddress());
        gp.setBlueToothDevice(device);
        gp.setInputID();
        if (gp.getDevice() != null && gp.getInputID() != -1) {
            gp.setSPPConnection(new SPPConnection(gp, listener));
            gp.getSPPConnection().start();
            enableSensor(gp);
            gp.setCalibrationData();
//        gp.print();
            DeviceLocalCache.getInstance().save(mDevices); //update the local cache
            FabricModel model = gp.getFabricModel();
            FabricController.getInstance().gamepadConnection(1, FabricController.STATUS_CONNECTED, FabricController.GP_V2_NAME, model.mPreviousVersion);
            gp.setFabricModel(model);
        }
    }


    private synchronized DeviceModel getTarget(String key) {
        if (mDevices != null) {
            for (DeviceModel model : mDevices) {
                if (model.getMACAddress().equals(key)) {
                    return model;
                }
            }
        }
        return null;
    }

    public void notifyConnectedDevice(BluetoothDevice device, GamePadListener listener) {
        LogUtil.l();
        if (invalidGamePad(device)) {
            LogUtil.i(R.string.log_not_valid_tim_gamepad);
            listener.onSetupStatusChanged(false, device);
            return;
        }

        DeviceModel gamepad = getTarget(device.getAddress());
        if (gamepad != null) {
            LogUtil.d(R.string.log_gamepad_existed, gamepad.getIndicator(), gamepad.getGamePadName(), gamepad.getMACAddress());
            gamepad.setBlueToothDevice(device);
            gamepad.setInputID();
        }
        
        if (gamepad != null && gamepad.getInputID() != -1) {
            enableSensor(gamepad);
            gamepad.setSPPConnection(new SPPConnection(gamepad, listener));
            gamepad.getSPPConnection().start();
            gamepad.setCalibrationData();

            FabricModel model = gamepad.getFabricModel();
            FabricController.getInstance().gamepadConnection(1, FabricController.STATUS_CONNECTED, FabricController.GP_V2_NAME, model.mPreviousVersion);
            gamepad.setFabricModel(model);
        } else {
            LogUtil.d(R.string.log_new_gamepad);
            byte seat = findFreeSeat(mDevices);
            if (seat != -1) {
                LogUtil.l();
                processNewDevice(seat, device, listener);
            } else {
                LogUtil.l();
                seat = findIdleSeat(mDevices);
                if (seat != -1) {
                    LogUtil.l();
                    processNewDevice(seat, device, listener);
                } else {
                    //LogUtil.e("Abnormal scenario!");
                }
            }
        }
    }

    public void updateFabric(BluetoothDevice device) {
        DeviceModel model = getTarget(device.getAddress());
        if (model != null) {
            FabricModel fabricModel = model.getFabricModel();
            model.getFabricModel().mPreviousVersion = model.getFWVersion();
            LogUtil.d(R.string.log_updatefabric_firmwareversion, fabricModel.mPreviousVersion, fabricModel.mUpgradeVersion);
            model.setFabricModel(fabricModel);
        }
    }

    public void notifyDisconnectedDevice(BluetoothDevice device) {
        if (invalidGamePad(device)) {
            LogUtil.i(R.string.log_not_valid_tim_gamepad);
            return;
        }

        DeviceModel gamepad = getTarget(device.getAddress());
        if (gamepad != null) {

            LogUtil.d(R.string.log_gamepad_existed, gamepad.getIndicator(), gamepad.getGamePadName(), gamepad.getMACAddress());
            if (gamepad.getSPPConnection() != null) {
                gamepad.getSPPConnection().stop();
            }
            gamepad.setSPPConnection(null);
            gamepad.setBlueToothDevice(null);
            DeviceLocalCache.getInstance().save(mDevices);
            FabricModel model = gamepad.getFabricModel();
            FabricController.getInstance().gamepadConnection(1, FabricController.STATUS_DISCONNECTED, FabricController.GP_V2_NAME, model.mPreviousVersion);
//            Intent intentBroadcast = new Intent();
//            intentBroadcast.putExtra(INTENT_MAC, gamepad.getMACAddress());
//            intentBroadcast.setAction(GAMEPAD_DEVICE_DISCONNECTED);
            EventBus.getDefault().post(new GamePadEvent(EVENTBUT_MSG_GP_DEVICE_DISCONNECTED, gamepad.getMACAddress()));
//            mContext.sendBroadcast(intentBroadcast);
            return;
        }

        //LogUtil.e("Abnormal scenario!!");
    }

    public void notifyUnpairedDevice(BluetoothDevice device) {
        if (invalidGamePad(device)) {
            LogUtil.i(R.string.log_not_valid_tim_gamepad);
            return;
        }

        DeviceModel gamepad = getTarget(device.getAddress());
        if (gamepad != null) {
            LogUtil.d(R.string.log_gamepad_existed, gamepad.getIndicator(), gamepad.getGamePadName(), gamepad.getMACAddress());
            if (gamepad.getSPPConnection() != null) {
                gamepad.getSPPConnection().stop();
            }
            gamepad.reset();
            DeviceLocalCache.getInstance().save(mDevices);
            FabricModel model = gamepad.getFabricModel();
            FabricController.getInstance().gamepadConnection(1, FabricController.STATUS_UNPAIRED, FabricController.GP_V2_NAME, model.mPreviousVersion);
//            Intent intentBroadcast = new Intent();
//            intentBroadcast.setAction(GAMEPAD_DEVICE_DISCONNECTED);
//            mContext.sendBroadcast(intentBroadcast);
            EventBus.getDefault().post(new GamePadEvent(EVENTBUT_MSG_GP_DEVICE_DISCONNECTED, gamepad.getMACAddress()));
            return;
        }
    }

    public boolean isEmpty() {
        return mTargetDeviceCount == 0;
    }
    
    public int getTargetDeviceCount() {
        return mTargetDeviceCount;
    }
    
    public int getTargetDeviceCount(String exceptDeviceAddress) {
        for (DeviceModel model : mDevices) {
            if (model.getMACAddress().equals(exceptDeviceAddress)) return mTargetDeviceCount - 1;
        }
        return mTargetDeviceCount;
    }

    public UpgradeManager getUpgradeManager() {
        return mUpgradeManager;
    }

    public DeviceModel getDeviceModelByAddress(String address) {
        return getTarget(address);
    }

    public Hashtable<String, DeviceModel> getConnectedDevices() {

        for (DeviceModel model : mDevices) {
            if (model.getBlueToothDevice() != null) {
                mGamePadMap.put(model.getMACAddress(), model);
            }
        }
        Log.d("Daniel", "mGamePadMap --->>" + mGamePadMap);
        return mGamePadMap;
    }

    public List<DeviceModel> getConnectedDevicesList() {
        Log.d("Daniel", "mDevices --->>" + mDevices);
        return mDevices;
    }

    public List<DeviceModel> getBondedDevices() {
        return mDevices;
    }


    public ArrayList<DeviceModel> getNeedUpgradedDevice(FirmwareConfig config) {
        ArrayList<DeviceModel> deviceList = new ArrayList<>();

        for (DeviceModel model : mDevices) {
            if (model.getFabricModel().needUpdate(config.getmVersion())) {
                deviceList.add(model);
            }
        }
        return deviceList;
    }


    public void queryDeviceFabricInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (DeviceModel model : mDevices) {
                    FabricModel fabricModel = new FabricModel();
                    FirmwareConfig firmwareConfig = mUpgradeManager.getNewVersion();
                    fabricModel.connectStatus = true;
                    fabricModel.mPreviousVersion = model.getFWVersion();
                    fabricModel.mUpgradeVersion = firmwareConfig.getmVersion();
                    fabricModel.mFirmwareConfig = firmwareConfig;
                    fabricModel.mGamepadHWVersion = "V2";
                    fabricModel.mBattery = model.getBatterVolt();
                    model.setFabricModel(fabricModel);
                }
                //notify FotaMainActivity fresh UI.
                EventBus.getDefault().post(EVENTBUS_MSG_QUERY_END);


            }
        }).start();
    }


    /**
     * 获取已经配对的设备
     *
     * @return
     */
    public ArrayList<BluetoothDevice> getTargetDevices() {
        ArrayList<BluetoothDevice> gamepadList = new ArrayList<BluetoothDevice>();
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if (device.getName().contains(GAMEPAD_NAME_RELEASE) && isConnected(device)) {
                    LogUtil.d(TAG, R.string.log_target_device_attached_isconnect, device.getName(), isConnected(device));
                    gamepadList.add(device);
                    mTargetDeviceCount++;
                }
            }
        }

        //没有V1的手柄，所以为0
        FabricController.getInstance().gamepadPlaying(0, mTargetDeviceCount);
        //这里没有V1的手柄，所以只传递V2的手柄
        FabricController.getInstance().gamepadNumPerSTB(0, mTargetDeviceCount);
        return gamepadList;
    }

    /**
     * 重新连接蓝牙设备
     */
    public boolean connectBluetooth(final BluetoothDevice btDevice) {
        LogUtil.d(TAG, R.string.log_start_reconnect_bluetooth, btDevice.getAddress(), btDevice.getName());
        try {
            return btDevice.createBond();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isConnected(BluetoothDevice device) {
        if (!_bluetoothAdapter.isEnabled()) return false;
        if (device == null) return false;
        String name = device.getName();
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            LogUtil.i(R.string.log_target_device_connected, device.getAddress());
            for (int i : InputDevice.getDeviceIds())
                if (name.equals(InputDevice.getDevice(i).getName())) {
                    return true;
                }
            return false;
        } else {
            LogUtil.d(R.string.log_device_found_not_connected);
            return false;
        }
    }

    public void generateException() {
        List<String> inits = new ArrayList<>(1);
        LogUtil.d("" + inits.get(2));
    }

    private enum BluetoothDeviceManagerSingleton {
        INSTANCE;
        private BluetoothDeviceManager mManager;

        BluetoothDeviceManagerSingleton() {
            mManager = new BluetoothDeviceManager();
        }

        public BluetoothDeviceManager getInstance() {
            return mManager;
        }
    }
}