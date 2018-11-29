package it.telecomitalia.TIMgamepad2.fota;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import it.telecomitalia.TIMgamepad2.utils.Applications;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;
import it.telecomitalia.TIMgamepad2.utils.SharedPreferenceUtils;

/**
 * Created by D on 2018/2/5 0005.
 */

@SuppressWarnings("unused")
public class DeviceLocalCache {
    private static final String CACHED_KEY = "GAMEPADS";
    private static final String DEFAULT_KEY = "empty";

    private static final String FILE_NAME = "GamePads";

    private Context mContext;

    private DeviceLocalCache() {
        mContext = Applications.context();
    }

    public static DeviceLocalCache getInstance() {
        return DeviceLocalCacheSingleton.INSTANCE.getInstance();
    }

    private enum DeviceLocalCacheSingleton {
        INSTANCE;
        private DeviceLocalCache mLocalDevices;

        DeviceLocalCacheSingleton() {
            mLocalDevices = new DeviceLocalCache();
        }

        public DeviceLocalCache getInstance() {
            return mLocalDevices;
        }
    }

    public synchronized void save(List<DeviceModel> list) {
        ArrayList<CachedDevice> devices = new ArrayList<>(list.size());
        for (DeviceModel gamepad : list) {
            CachedDevice device = new CachedDevice(gamepad.getGamePadName(), gamepad.getMACAddress(), gamepad.getIndicator(), gamepad.imuEnabled());
            devices.add(device);
        }
        LogUtil.d("Create " + devices.size() + " device record");
        Gson gson = new Gson();
        String jsonStr = gson.toJson(devices);
        SharedPreferenceUtils.clear(FILE_NAME, mContext);
        SharedPreferenceUtils.put(FILE_NAME, mContext, CACHED_KEY, jsonStr);
        LogUtil.d("Device cached:" + jsonStr);
    }

    public synchronized void clear() {
        SharedPreferenceUtils.clear(FILE_NAME, mContext);
    }

    public synchronized void restore(List<DeviceModel> list) {
        String str = (String) SharedPreferenceUtils.get(FILE_NAME, mContext, CACHED_KEY, DEFAULT_KEY);
        if (str.equals(DEFAULT_KEY)) {
            LogUtil.d("No existed device info, Now init: " + str);
            for (DeviceModel gamepad : list) {
                gamepad.reset();
            }
            save(list);
        } else {
            Gson gson = new Gson();
            ArrayList<CachedDevice> devices;
            devices = gson.fromJson(str, new TypeToken<List<CachedDevice>>() {
            }.getType());
            LogUtil.d("Do restore: " + gson.toJson(devices));

            for (int i = 0; i < devices.size(); i++) {
                CachedDevice device = devices.get(i);
                list.get(i).fill(device.getName(), device.getAddress(), device.getLedIndicator(), device.getIMUEnabled());
            }
        }
    }

    private class CachedDevice {
        private String Name;
        private String Address;
        private byte LedIndicator;
        private boolean IMUEnabled;

        private CachedDevice(String name, String address, byte indicator, boolean enable) {
            this.Name = name;
            this.Address = address;
            this.LedIndicator = indicator;
            this.IMUEnabled = enable;
        }

        private String getName() {
            return this.Name;
        }

        private String getAddress() {
            return Address;
        }

        private byte getLedIndicator() {
            return LedIndicator;
        }

        private boolean getIMUEnabled() {
            return IMUEnabled;
        }

        private String toStr() {
            return Name + "==" + Address + "==" + LedIndicator + "==" + IMUEnabled;
        }

    }

}
