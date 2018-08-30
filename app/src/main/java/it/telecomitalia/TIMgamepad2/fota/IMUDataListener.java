package it.telecomitalia.TIMgamepad2.fota;


import it.telecomitalia.TIMgamepad2.model.IMUData;

/**
 * Created by D on 2018/1/18 0018.
 */

public interface IMUDataListener {
    void OnIMUDataUpdate(IMUData data);
}
