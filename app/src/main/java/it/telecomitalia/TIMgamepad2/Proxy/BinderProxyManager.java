package it.telecomitalia.TIMgamepad2.Proxy;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class BinderProxyManager {

    private static final java.lang.String DESCRIPTOR = "SensorData";
    private static final int INJECT = android.os.IBinder.FIRST_CALL_TRANSACTION;

    private IBinder mServiceBinder;


    private BinderProxyManager() {
        mServiceBinder = getSensorDataService();
    }

    public static BinderProxyManager getInstance() {
        return BinderProxyManagerSingleton.INSTANCE.getInstance();
    }


    public void send(byte[] data) {
        if (mServiceBinder != null) {
            inject(mServiceBinder, data);
        } else {
            LogUtil.e("Proxy service not ready, Please try it later...");
        }
    }

    private IBinder getSensorDataService() {
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

    private void inject(IBinder b, byte[] data) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeByteArray(data);

            b.transact(INJECT, _data, _reply, IBinder.FLAG_ONEWAY);
            _reply.readException();
            _reply.readInt();


        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
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
}
