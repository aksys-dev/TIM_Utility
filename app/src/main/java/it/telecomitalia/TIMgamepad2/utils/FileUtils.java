package it.telecomitalia.TIMgamepad2.utils;

import android.os.Handler;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import it.telecomitalia.TIMgamepad2.GamePadV2UpgadeApplication;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;

import static it.telecomitalia.TIMgamepad2.model.Constant.GAMEPADE;

/**
 * Created by czy on 2018/6/27.
 */

public class FileUtils {

    private static String PATH;


    static {
        PATH = GamePadV2UpgadeApplication.getContext().getCacheDir() + "/firmware/";
    }

    /**
     * 把保存到本地的json信息（从服务器获取的），读取出来转换成json，并存到FirmwareConfig对象里面
     *
     * @param path
     * @return
     */
    public static FirmwareConfig getJsonFromLocal(String path) {
        FirmwareConfig mConfigs = new FirmwareConfig();
        try {
            InputStream inputStream = InputStreamHelper.getInputStream(path);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            String json = new String(buffer);

            JSONObject jsonObject = new JSONObject(json);


            mConfigs.setmVersion(jsonObject.optString("versionCode"));
            mConfigs.setmDownUrl(jsonObject.optString("url"));
            mConfigs.setId(jsonObject.optString("id"));
            LogUtil.i("Version Code: " + jsonObject.optString("versionCode") + "\nURL: " + jsonObject.optString("url"));
        } catch (Exception e) {
            // handle exception
        }

        return mConfigs;

    }

    /**
     * 从服务器获取json信息，并存到本地对应地址
     *
     * @param path
     * @param name
     */
    public static void getJsonFromeServer(String path, String name, Handler handler) {
        try {
            int count = 0;

            URL url = new URL(path);
            LogUtil.i("getJsonFromeServer-->path:" + path);
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.connect();
            conection.setConnectTimeout(4000);
            conection.setReadTimeout(4000);
            // download the file
            InputStream input = conection.getInputStream();
            File dir = new File(PATH);
            File configFile = new File(PATH + name);
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
            //服务器请求json并下载到本地完成，发送handler
            handler.sendEmptyMessage(1);

        } catch (ConnectTimeoutException e) {
//            sendErrorCode(Constant.TIMEOUTERROR,handler);
            LogUtil.i(e.toString());
        } catch (Exception e) {
//            sendErrorCode(Constant.TIMEOUTERROR,handler);
            LogUtil.i(e.toString());
        }

    }


    /**
     * 对比下载下来的xml文件的版本号和新下载的newremote.bin以及newdongle.bin文件中的版本号，
     * 如果版本号一致则将新bin文件的内容复制到旧的bin文件中（remote.bin和dongle.bin）
     *
     * @param dowondUrl
     * @param handler
     */

    public static void compareVersion(final String dowondUrl, final Handler handler) {
        new Thread(new Runnable() {
            public void run()

            {

                try {
                    Thread.sleep(100);
                    try {
                        //一、先下载固件
                        downLoadBin(dowondUrl, handler);

						/*
						二、
						 * 对比下载下来的xml文件的版本号和新下载的newremote.bin以及newdongle.bin文件中的版本号，
						 * 如果版本号一致则将新bin文件的内容复制到旧的bin文件中（remote.bin和dongle.bin）
						 */
//                        File file=new File(PATH+NEWGAMEPADE);
//                        if (!file.exists()) {
//                            return;
//                        }
//                        String newgamepadver="";
//
//                        FirmwareConfig mConfigs1=getJsonFromLocal(PATH + CONFIG_FILE_NAME_GAMEPAD1);
//                        newgamepadver=mConfigs1.getmVersion();
//
//
//                        byte[] gamepadbytes=new byte[size];
//                        gamepadbytes=getBytes(PATH+NEWGAMEPADE);
//
//                        final byte[] gamepaddata=new byte[16];
//                        for (int i =0; i < 16; i++)
//                        {
//                            gamepaddata[i]=gamepadbytes[8192+i];
//                        }
//                        String gamepadver=new String(gamepaddata);
//                        gamepadver=gamepadver.substring(gamepadver.length()-6, gamepadver.length());
//						LogUtil.i(TAG,"downloadgamepad:固件手柄版本号："+gamepadver);
//						LogUtil.i(TAG,"downloadgamepad:JSON手柄版本号："+newgamepadver);
//
//						if ((newgamepadver.equals(gamepadver)))
//                        {
//                            File gamepadfile=new File(PATH+GAMEPADE);
//                            if(gamepadfile.exists())
//                            {
//                                gamepadfile.delete();
//                            }
//
//                            CommerHelper.getFile(getBytes(PATH+NEWGAMEPADE), PATH, GAMEPADE);
//
//                            handler.sendEmptyMessage(2);
//                        }else
//                        {
//
//                            handler.sendEmptyMessage(3);
//                        }

                    } catch (Exception e) {
//                        sendErrorCode(e.toString(),handler);
                    }
                } catch (InterruptedException e) {
//                    sendErrorCode(e.toString(),handler);
                }
            }

        }).start();

    }

    /**
     * 下载固件
     *
     * @param downurl
     */
    public static String downLoadBin(String downurl, Handler handler) {
        try {
            int count = 0;
            URL url = new URL(downurl);

            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            conection.connect();
            conection.setConnectTimeout(4000);
            conection.setReadTimeout(4000);

//            if(padnum==1){
//                postResultStatus(Constant.DOWNLOAD_SUCCESS,Integer.valueOf(getConfigrations(PATH+ CONFIG_FILE_NAME_GAMEPAD1).getId()));//发送下载成功消息到后台
//            }else{
//                postResultStatus(Constant.DOWNLOAD_SUCCESS,Integer.valueOf(getConfigrations(PATH+ CONFIG_FILE_NAME_DONGLE).getId()));//发送下载成功消息到后台
//            }

            // 下载固件
            InputStream input = conection.getInputStream();
            File dir = new File(PATH);
            File file = null;
            file = new File(PATH + GAMEPADE);
            if (!dir.exists())
                dir.mkdir();
            if (file.exists())
                file.delete();
            // Output stream
            OutputStream output = new FileOutputStream(file);

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
            handler.sendEmptyMessage(2); // what = 2;
        } catch (Exception e) {

        }
        return PATH + GAMEPADE;
    }


    /**
     * 获得指定文件的byte数组
     *
     * @param filePath
     * @return
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            long size = file.length();
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) size);
            byte[] b = new byte[(int) size];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
