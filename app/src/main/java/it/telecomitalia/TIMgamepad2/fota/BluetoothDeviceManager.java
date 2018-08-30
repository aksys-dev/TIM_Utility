package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import it.telecomitalia.TIMgamepad2.model.DeviceInfo;
import it.telecomitalia.TIMgamepad2.model.FabricModel;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

/**
 * Created by cmx on 2018/8/17.
 * 从Service中抽离蓝牙的一些操作，用于维护所有的蓝牙设备，包括获取，更新等
 */

public class BluetoothDeviceManager {
    private static final String TAG = "BluetoothDeviceManager";
    private static final String GAMEPAD_NAME_RELEASE = "TIMGamepad";
    public static final String EVENTBUS_MSG_QUERY_END = "QueryDeviceEnd";
    public static final String EVENTBUS_MSG_NEED_UPGRADE = "NeedUpgrade";
    public static final String EVENTBUS_MSG_STAUS_CHANGED = "StatusChanged";

    private static BluetoothDeviceManager mDeviceManager;
    private static volatile boolean mInitialized = false;

    private Hashtable<String, DeviceModel> mDeviceMap;
    private Hashtable<String, DeviceModel> mNeedUpgradeDeviceMap;
    private UpgradeManager mUpgradeManager;
    private int mTargetDeviceCount = 0;

    public static BluetoothDeviceManager getDeviceManager() {
        if (mDeviceManager == null) {
            mDeviceManager = new BluetoothDeviceManager();
        }
        return mDeviceManager;
    }

    private BluetoothDeviceManager() {
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public void initializeDevice(String path) {
        if (mInitialized) return;

        LogUtil.d(TAG, "DeviceManager Initialize");
        mDeviceMap = new Hashtable<>(4);
        mUpgradeManager = new UpgradeManager(path);
        mInitialized = true;
    }

    public void intializeDevice() {
        mDeviceMap.clear();
        int index = 0;
        for (BluetoothDevice device : getTargetDevices()) {
            DeviceModel model = createDeviceModel(device, index);
            if (model != null) {
                mDeviceMap.put(device.getAddress(), model);
                index++;
            }
        }
    }

    public void notifyAddDevice(BluetoothDevice device) {
        DeviceModel model = createDeviceModel(device, 0);
        if (model != null) {

            String firmwareVersion = model.mSPPConnection.getDeviceFirmwareVersion().substring(0, 7);
            FabricController.getInstance().gamepadConnection(1, "connected", "V2", firmwareVersion);
        }
    }

    public void updateDevice(BluetoothDevice device) {
        LogUtil.d(TAG, "update Device : " + device.getAddress() + " contains : " + mDeviceMap.containsKey(device.getAddress()) + " size : " + mDeviceMap.size());
        if (mDeviceMap.containsKey(device.getAddress())) {
            DeviceModel oldModel = mDeviceMap.get(device.getAddress());
            int index = oldModel.mIndex;
            DeviceModel model = createDeviceModel(device, index);
            if (model != null) {
                model.mFabricModel = oldModel.mFabricModel;
                mDeviceMap.put(device.getAddress(), model);
            }
        }
    }

    public void updateFabric(BluetoothDevice device) {
        if (mDeviceMap.containsKey(device.getAddress())) {
            DeviceModel oldModel = mDeviceMap.get(device.getAddress());
            FabricModel fabricModel = oldModel.getmFabricModel();
            oldModel.mFabricModel.mPreviousVersion = oldModel.mSPPConnection.getDeviceFirmwareVersion().substring(0, 7);
            LogUtil.d(TAG, "newFirmwareVersion : " + fabricModel.mPreviousVersion);
            LogUtil.d(TAG, "version : " + fabricModel.mUpgradeVersion);
            oldModel.mFabricModel = fabricModel;
            mDeviceMap.put(device.getAddress(), oldModel);
        }
    }

    public void notifyRemoveDevice(BluetoothDevice device) {
        if (mDeviceMap.containsKey(device.getAddress())) {
            DeviceModel dmodel = mDeviceMap.get(device.getAddress());
            FabricModel model = mDeviceMap.get(device.getAddress()).getmFabricModel();
            dmodel.getmSPPConnection().deinit();
            FabricController.getInstance().gamepadConnection(1, "disconnected", "V2", model.mPreviousVersion);
        }
    }

    //测试协议是否成功
    public void testProtocol() {
        LogUtil.d(TAG, "testProtocol");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!mDeviceMap.isEmpty()) {
                    DeviceModel model = mDeviceMap.get(mDeviceMap.keySet().iterator().next());
                    mTestConnection = model.getmSPPConnection();

                    test("CMD_MOTOR_ON", SPPConnection.CMD_MOTOR_ON);
                    /* test("CMD_ENABLE_IMU", SPPConnection.CMD_ENABLE_IMU);
                    test("CMD_DISABLE_IMU", SPPConnection.CMD_DISABLE_IMU);
                    test("CMD_QUERY_IMU", SPPConnection.CMD_QUERY_IMU);
                    test("CMD_SET_CHANNEL", SPPConnection.CMD_SET_CHANNEL);
                    test("CMD_QUERY_FW_VERSION", SPPConnection.CMD_QUERY_FW_VERSION);

                    test("CMD_START_FW_UPGRADE", SPPConnection.CMD_START_FW_UPGRADE);
                   test("CMD_SET_MODE_PC", SPPConnection.CMD_SET_MODEL_PC);
                     test("CMD_GET_MODEL_PC", SPPConnection.CMD_GET_MODEL_PC);
                    test("CMD_SET_MODEL_GAME", SPPConnection.CMD_SET_MODEL_GAME);
                    test("CMD_SET_MODEL_ANDROID", SPPConnection.CMD_SET_MODEL_ANDROID)
                    test("CMD_GET_MODEL_CTV_SUMSUNG", SPPConnection.CMD_GET_MODEL_CTV_SUMSUNG);
                    /*test("CMD_GET_MODEL_CTV_LG", SPPConnection.CMD_GET_MODEL_CTV_LG);
                    test("CMD_GET_MODEL_CTV", SPPConnection.CMD_GET_MODEL_CTV);
                    test("CMD_ENABLE_UPDATE_MODE", SPPConnection.CMD_ENABLE_UPDATE_MODE);
                    test("CMD_DISABLE_UPDATE_MODE", SPPConnection.CMD_DISABLE_UPDATE_MODE);
                    test("CMD_UPGRADE_FAILED", SPPConnection.CMD_UPGRADE_FAILED);*/
                }
            }
        }).start();
    }

    private SPPConnection mTestConnection;

    private void test(String CMD, byte cmd) {
        LogUtil.d(TAG, "=== TEST " + CMD);
        SPPConnection.ReceivedData data = mTestConnection.execCMD(cmd);
        LogUtil.d(TAG, "=== RESULT Send CMD : " + cmd + " Received CMD : " + data.mCmd + " Length: " + data.mLength + "   RETURN : " + data.mResult + "===");
    }

    private DeviceModel createDeviceModel(BluetoothDevice device, int index) {
        LogUtil.d(TAG, "create model for : " + device.getAddress());
        DeviceInfo info = new DeviceInfo(device, device.getAddress());
        SPPConnection mainConnection = new SPPConnection(info);
        mainConnection.init();
        mainConnection.enableSensor(true);
        return new DeviceModel(device, mainConnection, index);
    }

    public boolean isEmpty() {
        return mTargetDeviceCount == 0;
    }

    public UpgradeManager getUpgradeManager() {
        return mUpgradeManager;
    }

    public DeviceModel getDeviceModelByAddress(String address) {
        return mDeviceMap.get(address);
    }

    public Hashtable<String, DeviceModel> getConnectedDevices() {
        return mDeviceMap;
    }


    public ArrayList<DeviceModel> getNeedUpgradedDevice(FirmwareConfig config) {
        Set<String> keySet = mDeviceMap.keySet();
        ArrayList<DeviceModel> deviceList = new ArrayList<>();

        for (String key : keySet) {
            DeviceModel model = mDeviceMap.get(key);
            if (model.getmFabricModel().needUpdate(config.getmVersion())) {
                deviceList.add(model);
            }
        }
        return deviceList;
    }

    public void queryDeviceFabricInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "queryDeviceFabricInfo");
                Set<String> keySet = mDeviceMap.keySet();
                for (String key : keySet) {
                    DeviceModel model = mDeviceMap.get(key);
                    FabricModel fabricModel = new FabricModel();
                    FirmwareConfig firmwareConfig = mUpgradeManager.getNewVersion();

                    fabricModel.connectStatus = true;
                    String version = model.mSPPConnection.getDeviceFirmwareVersion();
                    fabricModel.mPreviousVersion = version != null && version.length() > 7 ? version.substring(0, 7) : "";
                    fabricModel.mUpgradeVersion = firmwareConfig.getmVersion();
                    fabricModel.mFirmwareConfig = firmwareConfig;
                    fabricModel.mGamepadHWVersion = "V2";
                    fabricModel.mBattery = model.getmSPPConnection().getDeviceBattery();
                    LogUtil.d(TAG, "battery : " + fabricModel.mBattery);
                    model.mFabricModel = fabricModel;
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
                if (device.getName().contains(GAMEPAD_NAME_RELEASE) && isBlutoothConnected(device)) {
                    LogUtil.d(TAG, "Target device attached : " + device.getName() + " isBlutoothConnected " + isBlutoothConnected(device));
                    gamepadList.add(device);
                    mTargetDeviceCount++;
                }
            }
        }

        //TODO:没有V1的手柄，所以为0
        FabricController.getInstance().gamepadPlaying(0, mTargetDeviceCount);
        //TODO:这里没有V1的手柄，所以只传递V2的手柄
        FabricController.getInstance().gamepadNumPerSTB(0, mTargetDeviceCount);
        return gamepadList;
    }

    /**
     * 重新连接蓝牙设备
     */
    public boolean connectBluetooth(final BluetoothDevice btDevice) {
        LogUtil.d(TAG, "start reconnect BlueTooth : " + btDevice.getAddress() + " name : " + btDevice.getName());
        try {
            return btDevice.createBond();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isBlutoothConnected(BluetoothDevice btDevice) {

        BluetoothAdapter _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到蓝牙状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            //打开权限
            method.setAccessible(true);
            int state = (int) method.invoke(_bluetoothAdapter, (Object[]) null);

            if (state == BluetoothAdapter.STATE_CONNECTED) {

                LogUtil.i("BluetoothAdapter.STATE_CONNECTED");

                Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                method.setAccessible(true);
                return (boolean) isConnectedMethod.invoke(btDevice, (Object[]) null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static class DeviceModel {
        private BluetoothDevice mDevice;
        private SPPConnection mSPPConnection;
        private FabricModel mFabricModel;
        private int mIndex;

        public DeviceModel(BluetoothDevice device, SPPConnection connection, int index) {
            mDevice = device;
            mSPPConnection = connection;
            mFabricModel = new FabricModel();
            mIndex = index;
        }

        public FabricModel getmFabricModel() {
            return mFabricModel;
        }

        public BluetoothDevice getDevice() {
            return mDevice;
        }

        public SPPConnection getmSPPConnection() {
            return mSPPConnection;
        }

        public int getIndex() {
            return mIndex;
        }
    }
}