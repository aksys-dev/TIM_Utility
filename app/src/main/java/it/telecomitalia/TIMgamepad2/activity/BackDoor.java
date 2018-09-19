package it.telecomitalia.TIMgamepad2.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.SPPConnection;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

import static it.telecomitalia.TIMgamepad2.activity.UpgradeUIActivity.MSG_RECONNECT_SUCCESS;
import static it.telecomitalia.TIMgamepad2.activity.UpgradeUIActivity.MSG_SET_PROGRESSBAR;

public class BackDoor extends Activity implements View.OnClickListener {

    private static final int EX_FILE_PICKER_RESULT = 0;

    ExFilePicker exFilePicker;
    private SPPConnection mainConnection;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径
    private TextView mVersionText, mFilePath, mUpdateResult, mStatusText;

    BluetoothDeviceManager manager = BluetoothDeviceManager.getDeviceManager();

    private Button mButton;

    private List<DeviceModel> mDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backdoor);
        findViewById(R.id.bUpdate).setVisibility(View.VISIBLE);


        mVersionText = findViewById(R.id.version);
        mFilePath = findViewById(R.id.file);
        mUpdateResult = findViewById(R.id.update);
//        mStatusText = findViewById(R.id.status);
        mButton = findViewById(R.id.button1);

        mButton.setOnClickListener(this);
        mButton.setVisibility(View.GONE);

        LogUtil.d("Now start BT connection status receiver");

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDevices = manager.getConnectedDevicesList();
        upgradeManager = manager.getUpgradeManager();
        if (mDevices == null) {
            LogUtil.e("No device connected!");
        } else {
            if (mDevices.size() == 0) {
                LogUtil.e("No device connected!");
            } else {
                mainConnection = mDevices.get(0).getSPPConnection();
                findViewById(R.id.bVersion).setOnClickListener(this);
                findViewById(R.id.bFile).setOnClickListener(this);
                findViewById(R.id.bUpdate).setOnClickListener(this);
                findViewById(R.id.exit).setOnClickListener(this);
            }
        }
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_RECONNECT_SUCCESS:
                    LogUtil.d("Send COMMIT Command");
                    mUpdateResult.setText("进度：升级完成！");
                    break;
                case MSG_SET_PROGRESSBAR:
                    mUpdateResult.setText("进度：开始传送数据(" + msg.arg1 + ")，等到设备回应");
                    break;
            }
        }
    }

    private MainHandler mTHisHandler = new MainHandler();

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.exit) {
            finish();
            return;
        }

        switch (v.getId()) {
            case R.id.bVersion:
                mVersionText.setText("版本：" + mainConnection.getDeviceFirmwareVersion());
                break;
            case R.id.bFile:
                exFilePicker = new ExFilePicker();
                exFilePicker.setCanChooseOnlyOneItem(true);
                exFilePicker.setShowOnlyExtensions("dat");
                exFilePicker.setQuitButtonEnabled(true);
                if (TextUtils.isEmpty(startDirectory)) {
                    exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                } else {
                    exFilePicker.setStartDirectory(startDirectory);
                }
                exFilePicker.setChoiceType(ExFilePicker.ChoiceType.FILES);
                exFilePicker.start(this, EX_FILE_PICKER_RESULT);
                break;
//            case R.id.bSend:
//                if (!TextUtils.isEmpty(startDirectory)) {
//                    startSendFirmwareToDevice(startDirectory);
//                } else {
//                    Toast.makeText(this, "请先选择需要升级的文件", Toast.LENGTH_LONG).show();
//                }
//                break;
            case R.id.bUpdate:
//                if (canUpgrade) {
//                    mUpdateResult.setText("开始升级设备，请稍后......");
//                    mainConnection.startFirmwareUpgrade();
//                    byte[] reply = new byte[8];
//                    mainConnection.waitAck(reply);
//                    mUpdateResult.setText("升级结果：" + reply[0]);
//                } else {
//                    Toast.makeText(this, "请先将升级文件发送到设备", Toast.LENGTH_LONG).show();
//                }
                upgradeManager.startUpgradeLocal(mDevices.get(0), startDirectory);
                break;
            case R.id.exit:
                finish();
                break;
            case R.id.button1:
                manager.generateException();
                break;

        }
    }

    private UpgradeManager upgradeManager;

//    private void startSendFirmwareToDevice(String path) {
//        try {
//            FileInputStream fileInputStream = new FileInputStream(new File(path));
//            int len = fileInputStream.available();
//            byte[] buffer = new byte[len];
//            int size = fileInputStream.read(buffer);
//            mSendProgress.setText("文件：" + len + " bytes. 将发送：" + size + " bytes. 正在传输数据，请稍后......");
//            fileInputStream.close();
//            mainConnection.sendData(buffer);
//            byte[] reply = new byte[8];
//            int read = mainConnection.waitAck(reply);
//            mSendProgress.append(" ...... 实际发送：");
//            canUpgrade = true;
//
//        } catch (FileNotFoundException e) {
//            Toast.makeText(this, "无法打开升级文件: " + e.getMessage(), Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        } catch (IOException e1) {
//            Toast.makeText(this, "读取文件出错: " + e1.getMessage(), Toast.LENGTH_LONG).show();
//            e1.printStackTrace();
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String path = result.getPath();
                List<String> names = result.getNames();
                for (int i = 0; i < names.size(); i++) {
                    File f = new File(path, names.get(i));
                    try {
                        Uri uri = Uri.fromFile(f); //这里获取了真实可用的文件资源
                        mFilePath.setText("升级文件：" + uri.getPath());
                        startDirectory = uri.getPath();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}

