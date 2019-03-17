package it.telecomitalia.TIMgamepad2.utils;

public class GamePadEvent {
    private String mMessage;
    private String mMAC;
    public GamePadEvent(String msg, String mac) {
        mMessage = msg;
        mMAC = mac;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getMAC() {
        return mMAC;
    }

}
