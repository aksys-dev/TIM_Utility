package it.telecomitalia.TIMgamepad2.fota;

/**
 * Created by D on 2018/1/19 0019.
 */

public interface IMURawDataListener {
    void onRawDataArrived(byte[] data, int size);
}
