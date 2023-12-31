package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import it.telecomitalia.TIMgamepad2.utils.CommerHelper;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;


/**
 * Created by D on 2018/1/18 0018.
 */

public class BlueToothConnThread extends Thread {
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private static final int STREAM_UNINITIATED = -1;
    private static final int STREAM_READY = 0;
    private static final int STREAM_BUSY = 1;
    private static final int STREAM_FAILED = 2;
    private static final int STREAM_INITIATED = 3;
    private DeviceModel mDeviceInfo;
    private BluetoothSocket mSocket = null;
    private InputStream mIS = null;
    private OutputStream mOS = null;
    private boolean sppRunning = false;
    private int mStreamReady = STREAM_UNINITIATED; //-1=uninitialized,  0=ready , 1=busy, 2=failed
    private SPPDataListener mCb;
    private ConnectionReadyListener mListener;

    private Thread SPPDataThread = new Thread(new Runnable() {

        @Override
        public void run() {

            int bytes;
            byte[] recv = new byte[22];
            // Keep listening to the InputStream while connected
            while (sppRunning) {
                try {
                    // Read from the InputStream
                    if (mIS != null) {
                        bytes = mIS.read(recv);
                        mCb.onDataArrived(recv, bytes);
                    }
                } catch (IOException e) {
                    LogUtil.e("disconnected\n", e.toString());
                    break;
                }
            }
        }
    });

    BlueToothConnThread(DeviceModel info, SPPDataListener cb, ConnectionReadyListener listener) {
        mDeviceInfo = info;
        mListener = listener;
        setName("BTConnThread-" + info.getIndicator());
//        LogUtil.d("" + getName() + " ");
        mCb = cb;
    }

    @Override
    public void run() {
        mStreamReady = STREAM_BUSY;
//        LogUtil.d("" + getName() + " ");

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
/*
        BluetoothDevice device = btAdapter.getRemoteDevice(btdevaddr);

        UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // bluetooth serial port service
        //UUID SERIAL_UUID = device.getUuids()[0].getUuid(); //if you don't know the UUID of the bluetooth device service, you can get it like this from android cache

        BluetoothSocket socket = null;

        try {
            socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
        } catch (Exception e) {Log.e("","Error creating socket");}

        try {
            socket.connect();
            Log.e("","Connected");
        } catch (IOException e) {
            Log.e("",e.getMessage());
            try {
                Log.e("","trying fallback...");

                socket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                socket.connect();

                Log.e("","Connected");
            }
            catch (Exception e2) {
                Log.e("", "Couldn't establish Bluetooth connection!");
            }
        }
*/
        do {
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                int sdk = Build.VERSION.SDK_INT;
                if (sdk >= 10) {
                    mSocket = mDeviceInfo.getDevice().createInsecureRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                } else {
                    mSocket = mDeviceInfo.getDevice().createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                }
            } catch (IOException e) {
                LogUtil.e("Error when create socketed");
            }
            try {
                mSocket.connect();
                LogUtil.d("SPP socketed Connected");
                mStreamReady = STREAM_INITIATED;
            } catch (IOException e) {
                LogUtil.e("unable to connect() socket, trying fallback...\n" + e.getMessage());
                // Close the socket
//                try {
//                    if (mSocket != null) {
//                        mSocket.close();
//                        mSocket = null;
//                    }
//                    Thread.sleep(500);
//                } catch (IOException | InterruptedException e1) {
//                    LogUtil.e("unable to close() socket, ignore\n" + e1.getMessage());
//                }
//
//                mStreamReady = STREAM_FAILED;
//                continue;

                try {
                    mSocket = (BluetoothSocket) mDeviceInfo.getDevice().getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mDeviceInfo.getDevice(), 1);
                    mSocket.connect();

                    LogUtil.d("SPP socketed Connected");
                    mStreamReady = STREAM_INITIATED;
                } catch (NoSuchMethodException | IOException | InvocationTargetException | IllegalAccessException e1) {
                    LogUtil.e("Connecting failed: " + e1);

                    if (mSocket != null) {
                        try {
                            mSocket.close();
                        } catch (IOException e2) {
                            LogUtil.e(e2.toString());
                        }

                        mSocket = null;
                    }
                    SystemClock.sleep(1000);
                }
            }

        } while (mStreamReady != STREAM_INITIATED);

        // Get the BluetoothSocket input and output streams
        try {
//            LogUtil.d("Getting input and output streaming");
            mIS = mSocket.getInputStream();
            mOS = mSocket.getOutputStream();
        } catch (IOException e) {
            LogUtil.e("temp sockets not created\n" + e.toString());
            mStreamReady = STREAM_FAILED;
            mListener.onConnectionReady(false);
        }

        mStreamReady = STREAM_READY;
        mListener.onConnectionReady(true);
    }

    public void startSPPDataListener() {
        sppRunning = true;
        SPPDataThread.start();
    }

    public void stopSPPDataListener() {
        sppRunning = false;
    }

    public void cancel() {
        stopSPPDataListener();
        try {
            if (mIS != null) {
                LogUtil.d("Close SPP socket input stream");
                mIS.close();
            }
            if (mOS != null) {
                LogUtil.d("Close SPP socket output stream");
                mOS.close();
            }
            if (mSocket != null) {
                LogUtil.d("Close SPP socket");
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            LogUtil.e("close() of connect of Input/Output/socket failed\n" + e.getMessage());
        }
    }

    private void waitUntilSocketReady() {
        while (mStreamReady != STREAM_READY) {
            SystemClock.sleep(100);
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public synchronized boolean write(byte[] buffer) {
        LogUtil.d("Send spp data (" + buffer.length + ") to game pad");
        try {
            if (mOS != null) {
                mOS.write(buffer);
            } else {
                LogUtil.e("Can not find target device");
            }
        } catch (IOException e) {
            LogUtil.e("Exception during write\n" + e.getMessage());
            return false;
        }
        return true;
    }

    public synchronized void write(byte cmd) {
        try {
            LogUtil.d("Send spp data (" + CommerHelper.HexToString(cmd) + ") to game pad");
            if (mOS != null) {
                mOS.write(cmd);
            } else {
                LogUtil.e("Can not find target device");
            }
        } catch (IOException e) {
            LogUtil.e("Exception during write\n" + e.toString());
        }
    }

    public int read(byte[] recv) {
        waitUntilSocketReady();
        int size = 0;
        try {
            // Read from the InputStream
            if (mIS != null) {
                size = mIS.read(recv);
                LogUtil.i("Received " + size + " bytes");
            } else {
                //LogUtil.i("Input stream socket has been closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }
}
