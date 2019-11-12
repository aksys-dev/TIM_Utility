package it.telecomitalia.TIMgamepad2.Proxy;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class BinderProxyManager {

    private static final java.lang.String DESCRIPTOR = "SensorData";
    private static final int SET_SENSITIVITY     = android.os.IBinder.FIRST_CALL_TRANSACTION;
    private static final int ENABLE_IMU          = android.os.IBinder.FIRST_CALL_TRANSACTION + 1;
    private static final int DISABLE_IMU         = android.os.IBinder.FIRST_CALL_TRANSACTION + 2;
    private static final int INJECT_IMU_DATA     = android.os.IBinder.FIRST_CALL_TRANSACTION + 3;

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
        }
    }

    public void setSensitivity(float value) {
        if (mServiceBinder != null) {
            setSensitivityValue(mServiceBinder, value);
        }
    }
    
    public void enableIMU() {
        if (mServiceBinder != null) {
            nativeEnableIMU(mServiceBinder);
        }
    }
    public void disableIMU() {
        if (mServiceBinder != null) {
            nativeDisableIMU(mServiceBinder);
        }
    }

    private IBinder getSensorDataService() {
        try {
            Class localClass = Class.forName("android.os.ServiceManager");
            Method getService = localClass.getMethod("getService", new Class[]{String.class});
            if (getService != null) {
                try {
                    Object result = getService.invoke(localClass, new Object[]{DESCRIPTOR});
                    if (result != null) {
                        // DO WHAT EVER YOU WANT AT THIS POINT, YOU WILL
                        // NEED TO CAST THE BINDER TO THE PROPER TYPE OF THE SERVICE YOU USE.
                        return (IBinder) result;
                    } else {
                        LogUtil.e("Can not get Binder: " + result);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                LogUtil.e("Can not get method for binder");
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

            b.transact(INJECT_IMU_DATA, _data, _reply, IBinder.FLAG_ONEWAY);
            _reply.readException();
            _reply.readInt();


        } catch (RemoteException e) {
            //  Auto-generated catch block
            e.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }

    private void setSensitivityValue(IBinder b, float senstivity) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            _data.writeFloat(senstivity);

            b.transact(SET_SENSITIVITY, _data, _reply, IBinder.FLAG_ONEWAY);
//            _reply.readException();

        } catch (RemoteException e) {
            //  Auto-generated catch block
            e.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }
    
    private void nativeEnableIMU(IBinder b) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(ENABLE_IMU, _data, _reply, IBinder.FLAG_ONEWAY);
            _reply.readException();
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }
    }
    private void nativeDisableIMU(IBinder b) {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken(DESCRIPTOR);
            b.transact(DISABLE_IMU, _data, _reply, IBinder.FLAG_ONEWAY);
            _reply.readException();
        } catch (RemoteException e) {
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
