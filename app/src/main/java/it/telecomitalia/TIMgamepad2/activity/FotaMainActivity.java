package it.telecomitalia.TIMgamepad2.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.FabricController;
import it.telecomitalia.TIMgamepad2.fota.SPPConnection;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FabricModel;
import it.telecomitalia.TIMgamepad2.service.UpdateFotaMainService;
import it.telecomitalia.TIMgamepad2.utils.CommerHelper;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.model.Constant.GAMEPADE;
import static it.telecomitalia.TIMgamepad2.model.Constant.TAG;

/**
 * Created by czy on 2018/7/24.
 */

public class FotaMainActivity extends Activity implements View.OnClickListener {
    private Button open_imu_btn, close_imu_btn, upgrade_describe_btn, bt_update, btn_commit;
    private TextView device_name_dis, device_mac_dis, bluetooth_status, tv_currentgamepadver1, tv_gamepadversion1;
    private SPPConnection mainConnection;
    private TextView mVersionText, upgrade_describe_txt;
    private LinearLayout mBluetoothItemContainer;
    private Context mContext;
    private static String PATH;
    private boolean isUpdating = false;
    private boolean needUpdate = false;

    private Spinner spinner;
    private List<String> data_list;
    private ArrayAdapter<String> arr_adapter;
    private boolean isSpinnerFirst = true;

    private SharedPreferences sp;

    private ProgressDialog progressDialog;
    private ProgressBar mUpdateProgressBar;
    private Handler mHandler;
    private BluetoothDeviceManager mGamepadDeviceManager;
    private UpgradeManager mUpgradeManager;
    private static boolean mAppInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LogUtil.d(TAG, "onCreated called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usbupdate1);
//        Fabric.with(this, new Crashlytics());
        initView();

        EventBus.getDefault().register(this);
        mContext = this;
        mHandler = new MainHandler();
        PATH = mContext.getCacheDir() + "/firmware/";

        progressDialog = new ProgressDialog(mContext);

        Intent intent2 = new Intent(FotaMainActivity.this, UpdateFotaMainService.class);
        FotaMainActivity.this.startService(intent2);
        EventBus.getDefault().post("enter");

        sp = FotaMainActivity.this.getSharedPreferences(CommerHelper.SPNAME, Activity.MODE_PRIVATE);
        displayDefaultBluetoothInfo();
        intializeBluetoothManager();
    }

    @Override
    protected void onResume() {
        LogUtil.d(TAG, "onResume called");
        mGamepadDeviceManager.intializeDevice();
        mGamepadDeviceManager.queryDeviceFabricInfo();

        //Get the BLuetoothDevice object
        if (mGamepadDeviceManager.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_target_device), Toast.LENGTH_LONG).show();
        } else {
//            mUpgradeManager.startServerCycle(handler);
//            EventBus.getDefault().post(mGamepadDeviceManager.getConnectedDevices());
        }
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void displayDefaultBluetoothInfo() {
        mBluetoothItemContainer.removeAllViews();
        for (int index = 0; index < 4; index++) {
            displayBluetoothStatusByPosition(index, new FabricModel());
        }
    }

    private boolean mInUpgrading = false;

    private void displayBluetoothStatusByPosition(int position, FabricModel model) {
        View bluetoothItemView = getLayoutInflater().inflate(R.layout.update_bt_item, null);
        TextView nameTxtView = bluetoothItemView.findViewById(R.id.gamepad_name);
        TextView curVersionTxtView = bluetoothItemView.findViewById(R.id.tv_currentgamepadver1);
        TextView newVersionTextView = bluetoothItemView.findViewById(R.id.tv_gamepadversion1);
        ImageView batteryImgView = bluetoothItemView.findViewById(R.id.iv_battery1);
        String curVersionTxt = getString(R.string.gampad_unconnected);
        String updateVersionTxt = "";
        int batteryLevel = model.getBatteryLevel();

        //has new firmware and need to upgrade.
        if (model.connectStatus && model.needUpdate()) {
            curVersionTxt = new StringBuilder("Versione Corrente: ").append(model.mPreviousVersion).toString();
            updateVersionTxt = new StringBuilder("Version update : ").append(model.mUpgradeVersion).toString();
            showBattery(batteryImgView, batteryLevel, View.VISIBLE);
        } else if (model.connectStatus) {
            //do not need upgrade, but connected.
            curVersionTxt = "BlueTooth State:" + "BlueTooth Connected";
            updateVersionTxt = new StringBuilder("Versione Corrente: ").append(model.mPreviousVersion).toString();
            showBattery(batteryImgView, batteryLevel, View.VISIBLE);
        } else {
            showBattery(batteryImgView, batteryLevel, View.GONE);
        }
        nameTxtView.setText(new StringBuilder("Gamepad ").append(position + 1).toString());

        curVersionTxtView.setText(curVersionTxt);
        newVersionTextView.setText(updateVersionTxt);
        mBluetoothItemContainer.addView(bluetoothItemView, position);
    }

    private void showBattery(ImageView battery, int mode, int visibility) {
        if (mode == FabricModel.BATTERY_LOW) {
            battery.setBackgroundResource(R.drawable.lowbattery);
        } else if (mode == FabricModel.BATTERY_MIDDLE) {
            battery.setBackgroundResource(R.drawable.mbattery);
        } else if (mode == FabricModel.BATTERY_HIGH) {
            battery.setBackgroundResource(R.drawable.battery);
        }
        battery.setVisibility(visibility);
    }

    private void displayUpgradeStatus(int position, boolean upgrade) {
        int visiblity = upgrade ? View.GONE : View.VISIBLE;
        View bluetoothItemView = mBluetoothItemContainer.getChildAt(position);
        TextView curVersionTxtView = bluetoothItemView.findViewById(R.id.tv_currentgamepadver1);
        TextView newVersionTextView = bluetoothItemView.findViewById(R.id.tv_gamepadversion1);
        curVersionTxtView.setVisibility(visiblity);
        newVersionTextView.setVisibility(visiblity);
        mUpdateProgressBar = bluetoothItemView.findViewById(R.id.progressbar);
        mUpdateProgressBar.setVisibility(upgrade ? View.VISIBLE : View.GONE);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof String) {
            String event = (String) object;

            //BluetoothDeviceManager后台扫描连接的设备，获取到之后，更新UI
            //BluetoothDeviceManager发现新设备或者连接的设备断开后，更新UI
            if (event.equals(BluetoothDeviceManager.EVENTBUS_MSG_QUERY_END) ||
                    event.equals(BluetoothDeviceManager.EVENTBUS_MSG_STAUS_CHANGED)) {
                LogUtil.d(TAG, "DisplayUpdateInfo");
                Hashtable<String, BluetoothDeviceManager.DeviceModel> deviceMap = mGamepadDeviceManager.getConnectedDevices();
                Set<String> keySet = deviceMap.keySet();
                int index = 0;

                //只有在没有升级的情况下，才去更新UI
                if (!mInUpgrading) {
                    for (String key : keySet) {
                        mBluetoothItemContainer.removeViewAt(index);
                        displayBluetoothStatusByPosition(index++, deviceMap.get(key).getmFabricModel());
                    }
                }
//                mGamepadDeviceManager.testProtocol();
            } else if (event.equals(BluetoothDeviceManager.EVENTBUS_MSG_NEED_UPGRADE)) {
                //启动来自UpdateFotaMainService的升级请求。
                startUpgradeByOrder(0);
            }
        } else if (object instanceof BluetoothDevice) {
            BluetoothDevice device = (BluetoothDevice) object;

            //检查是否是当前正在升级的蓝牙手柄，如果是，那么更新状态，并发送COMMIT命令。
            if (device != null && device.getAddress().equals(mCurrentUpgradeModel.getDevice().getAddress())) {

                //手动进入配对模式，系统会自动连接蓝牙手柄，不需要手动去连接
//                boolean result = mGamepadDeviceManager.connectBluetooth(device);
//                LogUtil.d(TAG, "connect result : " + result);

                mGamepadDeviceManager.updateDevice(mCurrentUpgradeModel.getDevice());
                mCurrentUpgradeModel = mGamepadDeviceManager.getDeviceModelByAddress(device.getAddress());
                mHandler.sendEmptyMessage(MSG_RECONNECT_SUCESS);
                showUpgradeProcess(100);
            }
        }

       /* if (object instanceof String) {
            LogUtil.i(TAG,"FotaMainActivity getEventBus");
            if (((String) object).contains("FirmwareVersion")) {
//                tv_currentgamepadver1.setText((String) object);
            }else if(((String) object).contains("ServerVersion")){
//                tv_gamepadversion1.setText((String) object);
            }else if (((String) object).contains("BlueTooth_Connected")) {
                //蓝牙已连接
//                bluetooth_status.setText("BlueTooth State:"+"BlueTooth_Connected");
                Toast.makeText(FotaMainActivity.this,"BlueTooth_Connected",Toast.LENGTH_SHORT).show();
            } else if (((String) object).contains("BlueTooth_disConnected")) {
                Toast.makeText(FotaMainActivity.this,"BlueTooth_disConnected",Toast.LENGTH_SHORT).show();
                //蓝牙已断开
//                bluetooth_status.setText("BlueTooth State:"+"BlueTooth_disConnected");
//                bt_update.setEnabled(false);
//                bt_update.setText("");
                mainConnection = null;
            }
        } else if (object instanceof UpdateModel) {
            UpdateModel updateModel = (UpdateModel) object;
            mainConnection = updateModel.getMainConnection();
            needUpdate = updateModel.isUpdate();
            bt_update.setEnabled(true);
            bt_update.setText("Aggiorna gli accessori");
        } else if (object instanceof BluetoothDevice) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) object;
//            device_mac_dis.setText("MacAddress:"+bluetoothDevice.getAddress());
        }else if(object instanceof SPPConnection){
            mainConnection=(SPPConnection)object;
        }*/
    }


    @Override
    public void onClick(View v) {
        LogUtil.d(TAG, "OnClick " + v.getId());
        switch (v.getId()) {
            case R.id.upgrade_describe_btn:
                //进行升级
                if (!TextUtils.isEmpty(PATH + GAMEPADE)) {
//                    startSendFirmwareToDevice(PATH + GAMEPADE);
                } else {
                    Toast.makeText(mContext, getString(R.string.select_upgrade_file), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.open_imu_btn:

                Toast.makeText(mContext, getString(R.string.enable_imu) + mainConnection.imuSwitch(SPPConnection.CMD_ENABLE_UPDATE_MODE), Toast.LENGTH_SHORT).show();
                break;

            case R.id.close_imu_btn:
                Toast.makeText(mContext, getString(R.string.disable_imu) + mainConnection.imuSwitch(SPPConnection.CMD_DISABLE_UPDATE_MODE), Toast.LENGTH_SHORT).show();
                break;

            case R.id.bt_update:
                //进行升级
                /*if (!TextUtils.isEmpty(PATH + GAMEPADE)) {
                    startSendFirmwareToDevice(PATH + GAMEPADE);
                } else {
                    Toast.makeText(mContext, "请先选择需要升级的文件", Toast.LENGTH_SHORT).show();
                }*/
                startUpgradeByOrder(0);

                break;

            case R.id.btn_commit:

                mainConnection.fotaOn(SPPConnection.CMD_UPGRADE_SUCCESS);

                byte[] reply = new byte[64];
                mainConnection.waitAck(reply);
                String content = new String(reply);

                sp.edit().putBoolean(CommerHelper.IS_COMMIT, false).commit();
                LogUtil.i(TAG, "进入serivce  commit发送--------" + content);
                break;
        }
    }

    private void intializeBluetoothManager() {
        LogUtil.d(TAG, "initializeBluetoothManager");
        mGamepadDeviceManager = BluetoothDeviceManager.getDeviceManager();
        mGamepadDeviceManager.initializeDevice(PATH);
        mUpgradeManager = mGamepadDeviceManager.getUpgradeManager();
    }

    /**
     * 按照顺序一个个升级,先升级第一个，如果后面还有，那么就开始升级第二个
     */
    private void startUpgradeByOrder(int index) {
        LogUtil.d(TAG, "Start upgrade : " + index);
        Hashtable<String, BluetoothDeviceManager.DeviceModel> deviceMap = mGamepadDeviceManager.getConnectedDevices();
        Set<String> keySet = deviceMap.keySet();

        for (String key : keySet) {
            BluetoothDeviceManager.DeviceModel model = deviceMap.get(key);
            LogUtil.d(TAG, "index : " + index + " newIndex : " + model.getIndex());
            if (index == model.getIndex() && !mInUpgrading) {
                mCurrentUpgradeModel = model;
                FabricModel fabricModel = deviceMap.get(key).getmFabricModel();
                LogUtil.d(TAG, "upgrade check ： " + fabricModel.needUpdate());
                if (fabricModel.needUpdate()) {
                    mInUpgrading = true;
                    mUpgradeManager.startUpgrade(mCurrentUpgradeModel, mHandler);
                    notifyUpgradeStatus(mCurrentUpgradeModel.getmFabricModel(), true);
                    displayUpgradeStatus(index, true);
                } else {
                    startUpgradeByOrder(index + 1);
                }
                break;
            }
        }
    }

    /**
     * 下载完数据，显示30%；
     * 传送升级包到手柄，显示90%
     * 发送COMMIT命令到手柄端，获取返回SUCCESS，显示100%
     */
    private void showUpgradeProcess(int percent) {
        mUpdateProgressBar.setProgress(percent);
    }

    private void notifyUpgradeStatus(FabricModel model, boolean result) {
        String upgradeResult = result ? "Sucess" : "Fail";
        FabricController.getInstance().upgradeStatistics(model.mPreviousVersion, model.mUpgradeVersion, model.mGamepadHWVersion, upgradeResult);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static final int MSG_SET_PROGRESSBAR = 0x101;
    public static final int MSG_RECONNECT_SUCESS = 0x200;
    private BluetoothDeviceManager.DeviceModel mCurrentUpgradeModel;

    private class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_RECONNECT_SUCESS:
                    LogUtil.d(TAG, "Send COMMIT Command");
                    mCurrentUpgradeModel.getmSPPConnection().fotaOn(SPPConnection.CMD_UPGRADE_SUCCESS);

                    byte[] reply = new byte[64];
                    int size = mCurrentUpgradeModel.getmSPPConnection().waitAck(reply);
                    SPPConnection.ReceivedData data = new SPPConnection.ReceivedData(reply, size);
                    LogUtil.d(TAG, "Command : result : " + data.toString());
                    if (data.mCmd == SPPConnection.CMD_UPGRADE_SUCCESS && data.mResult.contains("SUCCESS")) {
                        notifyUpgradeStatus(mCurrentUpgradeModel.getmFabricModel(), true);
                        mGamepadDeviceManager.updateFabric(mCurrentUpgradeModel.getDevice());
                        mCurrentUpgradeModel = mGamepadDeviceManager.getDeviceModelByAddress(mCurrentUpgradeModel.getDevice().getAddress());
                        displayUpgradeStatus(mCurrentUpgradeModel.getIndex(), false);
                        mBluetoothItemContainer.removeViewAt(mCurrentUpgradeModel.getIndex());
                        displayBluetoothStatusByPosition(mCurrentUpgradeModel.getIndex(), mCurrentUpgradeModel.getmFabricModel());
                    }

                    //已经升级完了，可以做其他的操作了，比如更新UI，执行下一次升级等
                    mInUpgrading = false;
                    //开始下一个蓝牙设备升级
                    int nextIndex = mCurrentUpgradeModel.getIndex() + 1;
                    startUpgradeByOrder(nextIndex);
                    break;
                case MSG_SET_PROGRESSBAR:
                    showUpgradeProcess(msg.arg1);
                    break;
            }
        }
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        Intent intent2 = new Intent(FotaMainActivity.this, UpdateFotaMainService.class);
        FotaMainActivity.this.startService(intent2);
        EventBus.getDefault().postSticky("enter");
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().postSticky("outer");
    }*/

    /*private class UpgradeAsyncTask extends AsyncTask<BluetoothDeviceManager.DeviceModel, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(BluetoothDeviceManager.DeviceModel... params) {
            LogUtil.d(TAG, "DoInBackGround");
            BluetoothDeviceManager.DeviceModel model = params[0];
            String path = FileUtils.downLoadBin(model.getmFabricModel().mFirmwareConfig.getmDownUrl(), mHandler);

            LogUtil.d(TAG, "download success " + path);
            SPPConnection mainConnection = model.getmSPPConnection();
            mainConnection.fotaOn(SPPConnection.CMD_ENABLE_UPDATE_MODE);
            showUpgradeProcess(30);
            try {

                FileInputStream fileInputStream = new FileInputStream(new File(path));
                int len = fileInputStream.available();
                LogUtil.d(TAG, "file length : " + len);
                byte[] buffer = new byte[len];
                int size = fileInputStream.read(buffer);
                LogUtil.i(TAG,"文件：" + len + " bytes. 将发送：" + size + " bytes. 正在传输数据，请稍后......");
                fileInputStream.close();
                mainConnection.sendData(buffer);
                byte[] reply = new byte[64];

                while ((mainConnection.waitAck(reply)) > 0){
                    String content = new String(reply);

                    if(content.contains("Reboot in 1 second")){
                        LogUtil.i(TAG,"升级完成...");
                        showUpgradeProcess(90);
                        mHandler.sendEmptyMessage(MSG_UPGRADE_FINISH);
                        return 100;
                    }
                    Array.setByte(reply, 0, (byte)0);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            LogUtil.d(TAG, "onPostExecute");
            super.onPostExecute(result);
        }
    }*/


    private void initView() {
        mBluetoothItemContainer = (LinearLayout) findViewById(R.id.bluetooth_list);
//        tv_currentgamepadver1=(TextView)findViewById(R.id.tv_currentgamepadver1);
//        tv_gamepadversion1=(TextView)findViewById(R.id.tv_gamepadversion1);
        bt_update = (Button) findViewById(R.id.bt_update);
        bt_update.setOnClickListener(this);
//        upgrade_describe_btn = (Button) findViewById(R.id.upgrade_describe_btn);
//        open_imu_btn = (Button) findViewById(R.id.open_imu_btn);
//        close_imu_btn = (Button) findViewById(R.id.open_imu_btn);
//        mVersionText = (TextView) findViewById(R.id.version);
        upgrade_describe_txt = findViewById(R.id.upgrade_describe_txt);
//        device_name_dis = (TextView) findViewById(R.id.device_name_dis);
//        device_mac_dis = (TextView) findViewById(R.id.device_mac_dis);
        bluetooth_status = (TextView) findViewById(R.id.bluetooth_status);
        btn_commit = (Button) findViewById(R.id.btn_commit);
        btn_commit.setOnClickListener(this);
//
//        spinner = (Spinner) findViewById(R.id.spinner);
//        //数据
//        data_list = new ArrayList<String>();
//        data_list.add("None");
//        data_list.add("switch_pc");
//        data_list.add("switch_game");
//        data_list.add("switch_android");
//        data_list.add("switch_ctv");
//        data_list.add("get_mode");
//        data_list.add("set_sumsung");
//        data_list.add("set_ctv_lg");
//        data_list.add("get_ctv");
//        //适配器
//        arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
//        //设置样式
//        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        //加载适配器
//        spinner.setAdapter(arr_adapter);
//
//
//        upgrade_describe_btn.setOnClickListener(this);
//        open_imu_btn.setOnClickListener(this);
//        close_imu_btn.setOnClickListener(this);
//
//        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
//            public void onItemSelected(AdapterView<?> arg0, View arg1,
//                                       int arg2, long arg3) {
//                if (isSpinnerFirst) {
//                    isSpinnerFirst = false;
//                } else {
//                    if (mainConnection == null) {
//                        return;
//                    }
//                    Toast.makeText(FotaMainActivity.this, "您选择的是：" + data_list.get(arg2), Toast.LENGTH_SHORT).show();
//                    switch (arg2) {
//                        case 1:
//                            mainConnection.imuSwitch(SET_MODEL_PC);
//                            break;
//                        case 2:
//                            mainConnection.imuSwitch(SET_MODEL_GAME);
//                            break;
//                        case 3:
//                            mainConnection.imuSwitch(SET_MODEL_ANDROID);
//                            break;
//                        case 4:
//                            mainConnection.imuSwitch(SET_MODEL_CTV);
//                            break;
//                        case 5:
//                            mainConnection.imuSwitch(GET_MODEL_PC);
//                            break;
//                        case 6:
//                            mainConnection.imuSwitch(GET_MODEL_CTV_SUMSUNG);
//                            break;
//                        case 7:
//                            mainConnection.imuSwitch(GET_MODEL_CTV_LG);
//                            break;
//                        case 8:
//                            mainConnection.imuSwitch(GET_MODEL_CTV);
//                            break;
//                        default:
//                            break;
//                    }
//
//                }
//
//            }
//
//            public void onNothingSelected(AdapterView<?> arg0) {
//                // TODO Auto-generated method stub
//                Toast.makeText(FotaMainActivity.this, "没有选中" +
//                        "", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    /**
     * 升级固件
     * @param path
     */
    /*private void startSendFirmwareToDevice(String path) {
        mainConnection.fotaOn(SPPConnection.CMD_ENABLE_UPDATE_MODE);
        try {

            FileInputStream fileInputStream = new FileInputStream(new File(path));
            int len = fileInputStream.available();
            byte[] buffer = new byte[len];
            int size = fileInputStream.read(buffer);
            LogUtil.i(TAG,"文件：" + len + " bytes. 将发送：" + size + " bytes. 正在传输数据，请稍后......");
            fileInputStream.close();
            mainConnection.sendData(buffer);
            byte[] reply = new byte[64];

            while (mainConnection.waitAck(reply)!=-1){
                Toast.makeText(this, "Begin Upgrade...", Toast.LENGTH_LONG).show();
                isUpdating=true;
                String content = new String(reply);
                LogUtil.i(TAG,"back"+content);
                if(content.contains("Reboot in 1 second")){
                    isUpdating=false;
                    LogUtil.i(TAG,"升级完成...");
                    notifyUpgradeStatus(true);
                    sp.edit().putBoolean(CommerHelper.IS_COMMIT,true).commit();
                    Toast.makeText(this, "Over Upgrade...", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    notifyUpgradeStatus(false);
                }
            }

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "FileNotFound:" + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e1) {
            Toast.makeText(this, "读取文件出错: " + e1.getMessage(), Toast.LENGTH_LONG).show();
            e1.printStackTrace();
        }
    }*/
}
