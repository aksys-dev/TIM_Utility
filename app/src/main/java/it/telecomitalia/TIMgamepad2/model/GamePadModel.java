package it.telecomitalia.TIMgamepad2.model;

/**
 * Created by czy on 2018/6/25.
 */

public class GamePadModel {

    /**
     * id
     */
    private String id;
    /**
     * verionCode
     */
    private String versionCode;
    /**
     * versionName
     */
    private String versionName;
    /**
     * fileSize
     */
    private String fileSize;
    /**
     * 固件下载url
     */
    private String url;
    /**
     * createdTime
     */
    private String createdTime;

    /**
     * projectId
     */
    private String projectId;

    /**
     * deviceId
     */
    private String deviceId;

    public String getId() {
        return id;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getUrl() {
        return url;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String toString() {
        return "GamePadModel{" +
                "id='" + id + '\'' +
                ", versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", url='" + url + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", projectId='" + projectId + '\'' +
                ", deviceId='" + deviceId + '\''
                ;
    }
}
