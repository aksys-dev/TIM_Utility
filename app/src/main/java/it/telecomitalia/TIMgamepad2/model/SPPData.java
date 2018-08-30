package it.telecomitalia.TIMgamepad2.model;

/**
 * Created by D on 2018/1/22 0022.
 */

public class SPPData {
    private int size;
    private byte[] data;

    public SPPData(int size, byte[] data) {
        this.size = size;
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }
}
