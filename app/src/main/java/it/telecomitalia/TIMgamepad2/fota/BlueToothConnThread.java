package it.telecomitalia.TIMgamepad2.fota;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import it.telecomitalia.TIMgamepad2.Proxy.ProxyManager;
import it.telecomitalia.TIMgamepad2.model.DeviceInfo;
import it.telecomitalia.TIMgamepad2.model.SPPData;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.model.Constant.TAG;

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
    private DeviceInfo mDeviceInfo;
    private BluetoothSocket mSocket = null;
    private InputStream mIS = null;
    private OutputStream mOS = null;
    private boolean mIMUEnabled = false;
    private int mStreamReady = STREAM_UNINITIATED; //-1=uninitialized,  0=ready , 1=busy, 2=failed
    private IMURawDataListener mCb;
    private ConnectionReadyListener mListener;
    private boolean firstRun = true;

    private ProxyManager mProxy = ProxyManager.getInstance();
    private Thread IMUDataThread = new Thread(new Runnable() {

        @Override
        public void run() {

            int bytes;
            byte[] recv = new byte[22];
            // Keep listening to the InputStream while connected
            while (mIMUEnabled) {
                try {
                    if (firstRun) {
                        LogUtil.d("IMU:" + getName() + " ");
                        firstRun = false;
                    }
                    // Read from the InputStream
                    if (mIS != null) {
                        bytes = mIS.read(recv);
//                        LogUtil.d("Received "+bytes+" bytes");
                        if (checkValid(recv, bytes)) {
//                            mCb.onRawDataArrived(recv, bytes);
                            mProxy.send(new byte[]{0x09, recv[6], recv[7], recv[8], recv[9], recv[10], recv[11], recv[12], recv[13], recv[14], recv[15], recv[16], recv[17]});
                        } else {
                            LogUtil.e("Wrong data received!");
                        }
                    } else {
                        LogUtil.e("Input stream socket has been closed");
                    }
                } catch (IOException e) {
                    LogUtil.e("disconnected\n", e.toString());
                    break;
                }
            }
        }
    });


    BlueToothConnThread(DeviceInfo info, IMURawDataListener cb, ConnectionReadyListener listener) {
        mDeviceInfo = info;
        mListener = listener;
        setName("BTConnThread-" + info.getChannel());
        LogUtil.d("" + getName() + " ");
        mCb = cb;
    }

    BlueToothConnThread(DeviceInfo info, ConnectionReadyListener listener) {
        mDeviceInfo = info;
        mListener = listener;
        setName("BTConnThread-" + info.getChannel());
        LogUtil.d("" + getName() + " ");
    }

    @Override
    public void run() {
        mStreamReady = STREAM_BUSY;
        LogUtil.d("" + getName() + " ");

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
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
                mSocket.connect();
            } catch (IOException e) {
                LogUtil.e("unable to connect() socket, tries again\n" + e.getMessage());
                // Close the socket
                try {
                    if (mSocket != null) {
                        mSocket.close();
                        mSocket = null;
                    }
                    Thread.sleep(500);
                } catch (IOException | InterruptedException e1) {
                    LogUtil.e("unable to close() socket, ignore\n" + e1.getMessage());
                }

                mStreamReady = STREAM_FAILED;
                continue;
            }
            mStreamReady = STREAM_INITIATED;
        } while (mStreamReady != STREAM_INITIATED);

        // Get the BluetoothSocket input and output streams
        try {
            LogUtil.d("Getting input and output streaming");
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

    public void startReceiveIMUDate() {
        mIMUEnabled = true;
        IMUDataThread.start();
    }

    public void stopReceiveIMUDate() {
        mIMUEnabled = false;
    }

    public void cancel() {
        mIMUEnabled = false;
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
            SystemClock.sleep(500);
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        waitUntilSocketReady();
//        LogUtil.d("Now start really operation");
        try {
            if (mOS != null) {
                mOS.write(buffer);
                LogUtil.i(TAG, "数据量");

            } else {
                LogUtil.e("Can not find target device");
            }
        } catch (IOException e) {
            LogUtil.e("Exception during write\n" + e.getMessage());
        }
    }

    public void write(byte cmd) {
        waitUntilSocketReady();
        try {
            LogUtil.d("Send spp data (" + Integer.toHexString(cmd) + ") to game pad");
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
                LogUtil.i(TAG, "Received " + size + " bytes");
            } else {
                LogUtil.i(TAG, "Input stream socket has been closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    public SPPData read() {
        waitUntilSocketReady();
        int size;
        byte[] recv = new byte[22];
        try {
            // Read from the InputStream
            if (mIS != null) {
                size = mIS.read(recv);
                LogUtil.d("Received " + size + " bytes");
                if (checkValid(recv, size)) {
                    return new SPPData(size, recv);
                } else {
                    LogUtil.e("Wrong data received!");
                }
            } else {
                LogUtil.e("Input stream socket has been closed");
            }
        } catch (IOException e) {
            LogUtil.e("disconnected\n", e.toString());
        }
        return null;
    }

    private boolean checkValid(byte[] data, int size) {
        return (size == 22 && data[1] == 0x55);
    }
}
