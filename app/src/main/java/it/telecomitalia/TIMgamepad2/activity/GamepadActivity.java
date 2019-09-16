package it.telecomitalia.TIMgamepad2.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import it.telecomitalia.TIMgamepad2.GamepadListAdapter;
import it.telecomitalia.TIMgamepad2.GamepadVO;
import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.UpgradeManager;
import it.telecomitalia.TIMgamepad2.model.FirmwareConfig;
import it.telecomitalia.TIMgamepad2.service.UpdateFotaMainService;
import it.telecomitalia.TIMgamepad2.utils.GamePadEvent;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;

import static it.telecomitalia.TIMgamepad2.DialogCode.INTENT_FROM_USER;
import static it.telecomitalia.TIMgamepad2.DialogCode.INTENT_KEY;
import static it.telecomitalia.TIMgamepad2.DialogCode.INTENT_MAC;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUT_MSG_GP_DEVICE_CONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager.EVENTBUT_MSG_GP_DEVICE_DISCONNECTED;
import static it.telecomitalia.TIMgamepad2.fota.DeviceModel.INIT_ADDRESS;

public class GamepadActivity extends AppCompatActivity {

    /// View
    ListView gamepadList2;
    FrameLayout gamepadView;

    /// Managers
    private BluetoothDeviceManager mGamePadDeviceManager;
    private UpgradeManager mUpgradeManager;

    /// Others
    ArrayList<GamepadVO> gamepadDatas;
    private String targetDeviceMac = "none";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamepad);

        mGamePadDeviceManager = BluetoothDeviceManager.getInstance();
        mUpgradeManager = mGamePadDeviceManager.getUpgradeManager();

        gamepadList2 = findViewById(R.id.gamepadListView);
        gamepadList2.setOnItemClickListener( new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                showGamepadsInfo(position);
            }
        } );

        gamepadDatas = new ArrayList<>();

        SetupGamePadList();
    }

    public void SetupGamePadList() {
        GamepadVO g;
        int list = 1;
        gamepadDatas.clear();
        List<DeviceModel> models = mGamePadDeviceManager.getBondedDevices();
        if (models == null || models.size() == 0) {
            LogUtil.w("No gamepad connected or service not running...");
            g = new GamepadVO();
            g.setGamepadName(String.format("Gamepad %d", list));
            gamepadDatas.add(g);
        } else {
            for (DeviceModel model : models) {
                if (!model.getMACAddress().equals(INIT_ADDRESS)) {
                    EventAddGamepad(model);
                    list++;
                }
            }
            if ( gamepadDatas.size() == 0) {
                // No Game pad
                g = new GamepadVO();
                g.setGamepadName(String.format("Gamepad %d", list));
                gamepadDatas.add(g);
            }
        }

        GamepadListAdapter adapter = new GamepadListAdapter(this, R.layout.gamepad_info, gamepadDatas );
        gamepadList2.setAdapter(adapter);
    }

    public void EventAddGamepad(DeviceModel model) {
        UpgradeManager mgr = mGamePadDeviceManager.getUpgradeManager();
        GamepadVO g = new GamepadVO(getString(R.string.gamepad_one) + (model.getIndicator() + 1), model.getMACAddress(), model.getBatterVolt(), model.getFWVersion(), model.online(), mgr.getNewVersion().getmVersion());
        gamepadDatas.add(g);
    }

    public void EventAddGamepad(String macAddress) {
        if (macAddress == getString(R.string.unknown)) return;
        GamepadVO g = new GamepadVO(macAddress);
//        Toast.makeText(this, "Detected Bluetooth: " + macAddress, Toast.LENGTH_SHORT).show();
        int i = 0;
        for (DeviceModel device : mGamePadDeviceManager.getBondedDevices()) {
            i++;
            if (device.getMACAddress().equals( macAddress )) {
                break;
            }
        }
        g.setGamepadName(String.format("Gamepad %d", i ));
        gamepadDatas.add(g);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof BluetoothDevice) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) object;
            Crashlytics.log(Log.INFO, "FOTA2", "Detected Bluetooth: " + bluetoothDevice.getAddress());
            EventAddGamepad(bluetoothDevice.getAddress());
        }
//        else if ((object instanceof String) && ((String)object).contains("BlueTooth_Connected")) {
//            //蓝牙已连接
//            Toast.makeText(this, "BlueTooth_Connected", Toast.LENGTH_SHORT).show();
//        }
    }


    private void showGamepadsInfo(int position) {
        List<DeviceModel> models = mGamePadDeviceManager.getBondedDevices();
        if (models != null && models.size() != 0 && mUpgradeManager != null) {
            FirmwareConfig config = mUpgradeManager.getNewVersion();
            ArrayList<DeviceModel> deviceList = mGamePadDeviceManager.getNeedUpgradedDevice(config);
            boolean online = false;

            for (DeviceModel model : deviceList) {
                LogUtil.d("Device online? " + model.online());
                if (gamepadDatas.get(position).getMACAddress().equals(model.getMACAddress()) && model.online()) {
                    online = true;
                    targetDeviceMac = model.getMACAddress();
                    break;
                } else {
                    targetDeviceMac = "none";
                }
            }
            if (!online) {
                LogUtil.d("Device not online");
                return;
            }
            LogUtil.d("Need upgrade");
            // TODO: Change to Alert Message.
//            Intent dialogIntent = new Intent(this, DialogCode.class);
//            dialogIntent.putExtra(INTENT_KEY, INTENT_FROM_USER);
//            dialogIntent.putExtra(INTENT_MAC, targetDeviceMac);
//            startActivity(dialogIntent);
    
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dailog_tips);
//        builder.setTitle(R.string.firmware);
//        builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intentBroadcast = new Intent();
                    intentBroadcast.setAction(UpdateFotaMainService.DIALOG_OK_BROADCAST);
                    intentBroadcast.putExtra(INTENT_KEY, INTENT_FROM_USER);
                    intentBroadcast.putExtra(INTENT_MAC, targetDeviceMac);
                    sendBroadcast(intentBroadcast);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(UpdateFotaMainService.DIALOG_CANCEL_BROADCAST);
                    sendBroadcast(intent);
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 事件响应方法
     * 接收消息
     *
     * @param event
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(GamePadEvent event) {
        if (event.getMessage().equals(EVENTBUT_MSG_GP_DEVICE_CONNECTED)) {
            LogUtil.d("GamePad connected");
            SetupGamePadList();
        } else if (event.getMessage().equals(EVENTBUT_MSG_GP_DEVICE_DISCONNECTED)) {
            LogUtil.d("GamePad disconnected");
            SetupGamePadList();
        }
    }
}
