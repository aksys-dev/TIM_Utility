package it.telecomitalia.TIMgamepad2.Proxy;


import android.net.LocalServerSocket;
import android.net.LocalSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
//import java.net.InetAddress;
//import java.net.Socket;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

public class ProxyTransmitter extends Thread {

    private final String SOCKET_ADDRESS = "aks_v_imu_data_socket";
    private LocalSocket soc = null;
    private LocalServerSocket lss = null;
    private DataOutputStream mOutput = null;
    private DataInputStream mInput = null;

//    private InetAddress mTargetAddress;
    private boolean mInitiated = false;
    private final ProxyStatusListener mListener;

    ProxyTransmitter(ProxyStatusListener listener) {
        mListener = listener;
        this.start();
    }

    @Override
    public void run() {
//        final int PORT = 9529;
//        try {
//            mTargetAddress = InetAddress.getLocalHost();
//            LogUtil.e("Local IP address is " + mTargetAddress.getHostAddress());
//        } catch (IOException e) {
//            LogUtil.e("IOException: " + e.toString());
//        }
        while (true) {
            if (lss == null && soc == null) {
                try {
//                    LogUtil.d("socket", "new socket");
                    lss = new LocalServerSocket(SOCKET_ADDRESS);
                    soc = new LocalSocket();
                    soc.connect(lss.getLocalSocketAddress());
//                    soc.setTcpNoDelay(true);
                    mInput = new DataInputStream(soc.getInputStream());
                    mOutput = new DataOutputStream(soc.getOutputStream());
                } catch (IOException e) {
//                    LogUtil.e("IOException: " + e.toString());
                    e.printStackTrace();
                    mListener.onProxyConnectionReady(false);
                    mInitiated = false;
                    try {
//                        LogUtil.d("Retrying1......");
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                        LogUtil.i(R.string.log_interrupt_exception, e.toString());
                        e1.printStackTrace();
                    }
                    continue;
                }
            }
            break;
        }
        mListener.onProxyConnectionReady(true);
        mInitiated = true;
    }

    public boolean send(byte[] data) {
        if (mInitiated) {
            try {
                mOutput.write(data);
                mOutput.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            LogUtil.w(R.string.logw_server_not_ready);
            return false;
        }
    }

    public int receive(byte[] message) {
        int size;
        if (mInitiated) {
            try {
                size = mInput.read(message);
//                LogUtil.d("Received data: " + message[0]);
                return size;
            } catch (IOException e) {
                LogUtil.e(R.string.loge_ioexception, e.toString());
                e.printStackTrace();
            }
            return 0;
        } else {
            LogUtil.w(R.string.logw_server_not_ready);
            return 0;
        }
    }
}
