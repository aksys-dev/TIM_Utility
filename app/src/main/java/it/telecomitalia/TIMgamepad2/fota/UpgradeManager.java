package it.telecomitalia.TIMgamepad2.fota;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import it.telecomitalia.TIMgamepad2.model.Constant;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.model.FotaEvent;
import it.telecomitalia.TIMgamepad2.service.UpdateFotaMainService;
import it.telecomitalia.TIMgamepad2.utils.FileUtils;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.model.Constant.CONFIG_FILE_NAME_GAMEPAD1;
import static it.telecomitalia.TIMgamepad2.model.FotaEvent.FOTA_STAUS_FLASHING;
import static it.telecomitalia.TIMgamepad2.utils.FileUtils.getJsonFromLocal;

/**
 * Created by cmx on 2018/8/17.
 * 用于处理升级，包括轮询，升级一个蓝牙设备，下载等升级相关的接口
 */

public class UpgradeManager {
    private String mFWPath;
    private Timer mTimer;

    public static final int EVENT_UGPRADE_FILE_HEADER_ERROR = 0x01;

    UpgradeManager(String path) {
        mFWPath = path;
        mTimer = new Timer();
    }

    public void startServerCycle(final Handler handler) {
        if (mTimer == null) {
            mTimer = new Timer();
        } else {
            mTimer.schedule(new TimerTask() {
                public void run() {
//                    LogUtil.i("Start upgrade manager");
                    FirmwareConfig config = getNewVersion();
                    Message msg = handler.obtainMessage();
                    Bundle data = new Bundle();
                    data.putSerializable(UpdateFotaMainService.KEY_MSG_FIRMWARE, config);
                    msg.setData(data);
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }, 2000, 5 * 60 * 1000);
        }
    }

    public void startUpgrade(final DeviceModel model, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("Start Upgrade : " + model.getDevice().getAddress() + " Index : " + model.getIndex());
                if (model.getFabricModel() != null && model.getFabricModel().mFirmwareConfig != null) {
                    String url = model.getFabricModel().mFirmwareConfig.getmDownUrl();
                    if (url != null) {
                        String path = FileUtils.downLoadBin(url, handler);
                        LogUtil.d("download success " + path);
                        SPPConnection mainConnection = model.getSPPConnection();
                        if (mainConnection != null) {
                            mainConnection.fotaOn(SPPConnection.CMD_ENABLE_UPDATE_MODE);
                            EventBus.getDefault().post(new FotaEvent(FOTA_STAUS_FLASHING, model.getDevice(), 0));
                            mainConnection.startUpgrade(path, handler, false);
                        } else {
                            handler.sendEmptyMessage(UPGRADE_FAILED);
                        }
                    } else {
                        handler.sendEmptyMessage(UPGRADE_FAILED);
                    }
                } else {
                    handler.sendEmptyMessage(UPGRADE_FAILED);
                }
            }
        }).start();
    }

    public static final int UPGRADE_FAILED = 3;
    public static final int UPGRADE_CONNECTION_ERROR = 4;
    public static final int UPGRADE_TIMEOUT = 5;


    public void startUpgradeLocal(final DeviceModel model, final String localPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d("Start Local Upgrade: " + model.getDevice().getAddress() + ";Index: " + model.getIndex() + "; Path: " + localPath);
                SPPConnection mainConnection = model.getSPPConnection();
                mainConnection.fotaOn(SPPConnection.CMD_ENABLE_UPDATE_MODE);
                EventBus.getDefault().post(new FotaEvent(FOTA_STAUS_FLASHING, model.getDevice(), 0));

                try {
                    FileInputStream fileInputStream = new FileInputStream(new File(localPath));
                    int len = fileInputStream.available();
                    LogUtil.d("file length : " + len);
                    byte[] buffer = new byte[len];
                    int size = fileInputStream.read(buffer);
                    LogUtil.i("File：" + len + " bytes. Will send：" + size + " bytes. Transmitting, Please wait......");
                    mainConnection.startUpgradeProcess(buffer);
                    LogUtil.d("Data send finished");
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void upgrade(DeviceModel device) {
        String newversion = "";
        String fmVersion = "";

        boolean needupdate = false;
        //把本地从服务器下载的json文件读取出来，获取里面的版本号
        File file = new File(mFWPath + CONFIG_FILE_NAME_GAMEPAD1);
        if (file.exists()) {
            FirmwareConfig mConfigs = getJsonFromLocal(mFWPath + CONFIG_FILE_NAME_GAMEPAD1);
            newversion = mConfigs.getmVersion();
        } else {
            newversion = "";
        }

        if (TextUtils.isEmpty(device.getFabricModel().mPreviousVersion)) {
            fmVersion = device.getSPPConnection().getDeviceFirmwareVersion();
        }

        if (!TextUtils.isEmpty(newversion)) {
            if (newversion.compareTo(fmVersion) > 0) {
                needupdate = true;
            }
        }
    }

    public FirmwareConfig getNewVersion() {
        //把本地从服务器下载的json文件读取出来，获取里面的版本号
//        getJsonFromeServer(Constant.REMOTE_CONFIG_URL_GAMEPAD1_NEW, CONFIG_FILE_NAME_GAMEPAD1);
        getJsonFromeServer(Constant.REMOTE_CONFIG_URL_KOREA_NEW, CONFIG_FILE_NAME_GAMEPAD1);
        File file = new File(mFWPath + CONFIG_FILE_NAME_GAMEPAD1);
        FirmwareConfig config = new FirmwareConfig();

        if (file.exists()) {
            config = getJsonFromLocal(mFWPath + CONFIG_FILE_NAME_GAMEPAD1);
        }
//        LogUtil.d("Version : " + config.getmVersion());
        return config;
    }

    /**
     * 从服务器获取json信息，并存到本地对应地址
     *
     * @param path
     * @param name
     */
    public void getJsonFromeServer(final String path, final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = 0;

                    URL url = new URL(path);
//                    LogUtil.i("URL: " + path);
                    HttpURLConnection conection = (HttpURLConnection) url.openConnection();
                    conection.connect();
                    conection.setConnectTimeout(4000);
                    conection.setReadTimeout(4000);
                    // download the file
                    InputStream input = conection.getInputStream();
                    File dir = new File(mFWPath);
                    File configFile = new File(mFWPath + name);
                    if (!dir.exists())
                        dir.mkdir();
                    if (configFile.exists())
                        configFile.delete();

                    // Output stream
                    OutputStream output = new FileOutputStream(configFile);

                    byte data[] = new byte[256];
                    do {
                        count = input.read(data);
                        if (count == -1) {
                            break;
                        }
                        output.write(data, 0, count);
                    } while (true);
                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                } catch (Exception e) {
                    LogUtil.i(e.toString());
                }
            }
        }).start();

    }
}
