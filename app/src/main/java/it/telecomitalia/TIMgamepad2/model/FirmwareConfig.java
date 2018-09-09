package it.telecomitalia.TIMgamepad2.model;

import java.io.Serializable;

public class FirmwareConfig implements Serializable {
    private String mName;
    /**
     * 服务器上面版本号
     */
    private String mVersion;
    /**
     * 固件下载地址
     */
    private String mDownUrl;
    /**
     * 这个ID用来告诉后台下载成功、更新成功、更新失败
     */
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmVersion() {

        return mVersion;
//        return "181010";
    }

    public void setmVersion(String mVersion) {
        this.mVersion = mVersion;
    }

    public String getmDownUrl() {
        return mDownUrl;
    }

    public void setmDownUrl(String mDownUrl) {
        this.mDownUrl = mDownUrl;
    }

}
