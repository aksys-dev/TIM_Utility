package it.telecomitalia.TIMgamepad2.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by D on 2018/1/18 0018.
 */

public class IMUData implements Parcelable {

    //必须提供一个名为CREATOR的static final属性 该属性需要实现android.os.Parcelable.Creator<T>接口
    public static final Creator<IMUData> CREATOR = new Creator<IMUData>() {

        @Override
        public IMUData createFromParcel(Parcel source) {
            return new IMUData(source);
        }

        @Override
        public IMUData[] newArray(int size) {
            return new IMUData[size];
        }
    };
    private int ACC_X;
    private int ACC_Y;
    private int ACC_Z;
    private int GYRO_X;
    private int GYRO_Y;
    private int GYRO_Z;

    private int mChannel;

    public IMUData() {
        mChannel = 0;
        ACC_X = 0;
        ACC_Y = 0;
        ACC_Z = 0;
        GYRO_X = 0;
        GYRO_X = 0;
        GYRO_Y = 0;
    }

    private IMUData(Parcel source) {
        readFromParcel(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mChannel);
        parcel.writeInt(ACC_X);
        parcel.writeInt(ACC_Y);
        parcel.writeInt(ACC_Z);
        parcel.writeInt(GYRO_X);
        parcel.writeInt(GYRO_Y);
        parcel.writeInt(GYRO_Z);

    }

    //注意读取变量和写入变量的顺序应该一致 不然得不到正确的结果
    public void readFromParcel(Parcel source) {
        mChannel = source.readInt();
        ACC_X = source.readInt();
        ACC_Y = source.readInt();
        ACC_Z = source.readInt();
        GYRO_X = source.readInt();
        GYRO_Y = source.readInt();
        GYRO_Z = source.readInt();
    }

    public int getChannel() {
        return mChannel;
    }

    public void setChannel(int channel) {
        mChannel = channel;
    }

    public int get_AX() {
        return ACC_X;
    }

    public void set_AX(int n) {
        this.ACC_X = n;
    }

    public int get_AY() {
        return ACC_Y;
    }

    public void set_AY(int n) {
        this.ACC_Y = n;
    }

    public int get_AZ() {
        return ACC_Z;
    }

    public void set_AZ(int n) {
        this.ACC_Z = n;
    }

    public int get_GX() {
        return GYRO_X;
    }

    public void set_GX(int n) {
        this.GYRO_X = n;
    }

    public int get_GY() {
        return GYRO_Y;
    }

    public void set_GY(int n) {
        this.GYRO_Y = n;
    }

    public int get_GZ() {
        return GYRO_Z;
    }

    public void set_GZ(int n) {
        this.GYRO_Z = n;
    }

}
