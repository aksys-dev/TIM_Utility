package it.telecomitalia.TIMgamepad2.Proxy;

public class ProxyManager {
    private ProxyController mController;

    private ProxyManager() {
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);
        mController = new ProxyController();
    }

    public static ProxyManager getInstance() {
        return InputAssistantManagerSingleton.INSTANCE.getInstance();
    }

    public boolean ready() {
        return mController.commonRequest(CommonEventCode.REQ_CHECK_ACTIVATION) == CommonEventCode.RES_ACTIVATED;
    }

    public void send(byte[] data) {
        mController.sendData(data);
    }

    private enum InputAssistantManagerSingleton {
        INSTANCE;
        private ProxyManager mManager;

        InputAssistantManagerSingleton() {
            mManager = new ProxyManager();
        }

        public ProxyManager getInstance() {
            return mManager;
        }
    }

}
