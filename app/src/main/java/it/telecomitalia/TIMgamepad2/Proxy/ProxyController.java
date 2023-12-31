package it.telecomitalia.TIMgamepad2.Proxy;

import android.os.SystemClock;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;


public class ProxyController implements ProxyStatusListener {

    private boolean serviceReady = false;
    private ProxyTransmitter mTransmitter;

    public ProxyController() {
        mTransmitter = new ProxyTransmitter(ProxyController.this);
    }

    @Override
    public void onProxyConnectionReady(boolean ready) {
        if (ready) {
            LogUtil.e("Core service ready.");
        } else {
//            LogUtil.e("Waiting for the core service ready.");
        }
        serviceReady = ready;
    }

    public CommonEventCode commonRequest(CommonEventCode reqCode) {
//        LogUtil.e("Service ready ? " + serviceReady);
        int retries = 10;
        byte[] msg = new byte[32];
        int size;
        while (retries-- > 0) {
            if (serviceReady) {
                mTransmitter.send(new byte[]{reqCode.value(reqCode)});
                size = mTransmitter.receive(msg);
                if (size != 0) {
                    return reqCode.valueOf(msg[0]);
                }
            }
            SystemClock.sleep(100);
        }
        return CommonEventCode.CODE_ERROR;
    }

    public void sendData(byte[] data) {
        mTransmitter.send(data);
    }
}
