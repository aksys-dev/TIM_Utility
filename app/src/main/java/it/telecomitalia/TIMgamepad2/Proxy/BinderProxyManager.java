package it.telecomitalia.TIMgamepad2.Proxy;

import android.os.IBinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class BinderProxyManager {

    private static final java.lang.String DESCRIPTOR = "SensorData";
    private static final int ALERT = android.os.IBinder.FIRST_CALL_TRANSACTION;
    private static final int PUSH = ALERT + 1;
    private static final int ADD = ALERT + 2;

    private IBinder mServiceBinder;


    private BinderProxyManager() {
        mServiceBinder =
    }

    public static BinderProxyManager getInstance() {
        return BinderProxyManagerSingleton.INSTANCE.getInstance();
    }


    public void send(byte[] data) {
//        mController.sendData(data);
    }

    private enum BinderProxyManagerSingleton {
        INSTANCE;
        private BinderProxyManager mManager;

        BinderProxyManagerSingleton() {
            mManager = new BinderProxyManager();
        }

        public BinderProxyManager getInstance() {
            return mManager;
        }
    }

    private IBinder getDemoServiceManager() {
        try {
            Class localClass = Class.forName("android.os.ServiceManager");
            Method getService = localClass.getMethod("getService", new Class[]{String.class});
            if (getService != null) {
                LogUtil.d("getService method ready:" + getService);
                try {
                    Object result = getService.invoke(localClass, new Object[]{DESCRIPTOR});
                    if (result != null) {
                        // DO WHAT EVER YOU WANT AT THIS POINT, YOU WILL
                        // NEED TO CAST THE BINDER TO THE PROPER TYPE OF THE SERVICE YOU USE.
                        return (IBinder) result;
                    } else {
                        LogUtil.d("Can not get Binder: " + result);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                LogUtil.d("Can not get method");
                return null;
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;

    }
}
