package it.telecomitalia.TIMgamepad2.fota;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import it.telecomitalia.TIMgamepad2.activity.FotaMainActivity;
import it.telecomitalia.TIMgamepad2.model.Constant;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.service.UpdateFotaMainService;
import it.telecomitalia.TIMgamepad2.utils.FileUtils;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.model.Constant.CONFIG_FILE_NAME_GAMEPAD1;
import static it.telecomitalia.TIMgamepad2.model.Constant.TAG;
import static it.telecomitalia.TIMgamepad2.utils.FileUtils.getJsonFromLocal;

/**
 * Created by cmx on 2018/8/17.
 * 用于处理升级，包括轮询，升级一个蓝牙设备，下载等升级相关的接口
 */

public class UpgradeManager {
    private String mFWPath;
    private Timer mTimer;

    public UpgradeManager(String path) {
        mFWPath = path;
        mTimer = new Timer();
    }

    public void startServerCycle(final Handler handler) {
        if (mTimer == null) {
            mTimer = new Timer();
        } else {
            mTimer.schedule(new TimerTask() {

                public void run() {
                    LogUtil.i(TAG, "进入来了");
                    FirmwareConfig config = getNewVersion();
                    Message msg = handler.obtainMessage();
                    Bundle data = new Bundle();
                    data.putSerializable(UpdateFotaMainService.KEY_MSG_FIRMWARE, config);
                    msg.setData(data);
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }, 2000, 300000);
        }
    }

    public void startUpgrade(final DeviceModel model, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "Start Upgrade : " + model.getDevice().getAddress() + " Index : " + model.getIndex());
                String path = FileUtils.downLoadBin(model.getFabricModel().mFirmwareConfig.getmDownUrl(), handler);

                LogUtil.d(TAG, "download success " + path);
                SPPConnection mainConnection = model.getSPPConnection();
                mainConnection.fotaOn(SPPConnection.CMD_ENABLE_UPDATE_MODE);
                Message msg = handler.obtainMessage();
                msg.what = FotaMainActivity.MSG_SET_PROGRESSBAR;
                msg.arg1 = 30;
                handler.sendMessage(msg);
                try {

                    FileInputStream fileInputStream = new FileInputStream(new File(path));
                    int len = fileInputStream.available();
                    LogUtil.d(TAG, "file length : " + len);
                    byte[] buffer = new byte[len];
                    int size = fileInputStream.read(buffer);
                    LogUtil.i(TAG, "文件：" + len + " bytes. 将发送：" + size + " bytes. 正在传输数据，请稍后......");
                    fileInputStream.close();
                    mainConnection.sendData(buffer);
                    byte[] reply = new byte[64];

                    while ((mainConnection.waitAck(reply)) > 0) {
                        String content = new String(reply);

                        if (content.contains("Reboot in 1 second")) {
                            LogUtil.i(TAG, "升级完成...");
                            Message msg2 = handler.obtainMessage();
                            msg2.arg1 = 90;
                            msg2.what = FotaMainActivity.MSG_SET_PROGRESSBAR;
                            handler.sendMessage(msg2);
                            break;
                        }
                        Array.setByte(reply, 0, (byte) 0);
                    }
                    LogUtil.d(TAG, "update end");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
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

        LogUtil.d(TAG, "file exits " + file.exists());
        if (file.exists()) {
            config = getJsonFromLocal(mFWPath + CONFIG_FILE_NAME_GAMEPAD1);
        }
        LogUtil.d(TAG, "new version : " + config.getmVersion());
        return config;
    }

    /**
     * 从服务器获取json信息，并存到本地对应地址
     *
     * @param path
     * @param name
     */
    public void getJsonFromeServer(String path, String name) {
        try {
            int count = 0;

            URL url = new URL(path);
            LogUtil.i(Constant.TAG, "getJsonFromeServer-->path:" + path);
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

        } catch (ConnectTimeoutException e) {
            LogUtil.i(Constant.TAG, e.toString());
        } catch (Exception e) {
            LogUtil.i(Constant.TAG, e.toString());
        }
    }
}
