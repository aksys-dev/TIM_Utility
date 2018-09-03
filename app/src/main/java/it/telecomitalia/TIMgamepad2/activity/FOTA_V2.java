package it.telecomitalia.TIMgamepad2.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

import static it.telecomitalia.TIMgamepad2.fota.DeviceModel.INIT_ADDRESS;

public class FOTA_V2 extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Button menuGamepad, menuIMU, menuAbout;
    FrameLayout gamepadView;
    ScrollView gamepadLists;
    ListView gamepadList2;
    LinearLayout imuoptionLists;
    LinearLayout aboutLists;

    ArrayList<GamepadVO> datas;

    SeekBar seekBarSensitivity;
    TextView textSeekBarValue;
    float sensitivityValue = 1.00F;
    Switch calibration;

    //	FrameLayout frame_newVersion;
    TextView currentVersion, updateVersion;
    TextView lastUpdateDay;

    TextView activityTitle;
    LinearLayout menuButtons;

    int version = 0, update = 1;

    private static BluetoothDeviceManager mDeviceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fota__v2);

        mDeviceManager = BluetoothDeviceManager.getDeviceManager();
        datas = new ArrayList<>();

        EventBus.getDefault().register(this);

        activityTitle = findViewById(R.id.ActivityTitle);
        menuButtons = findViewById(R.id.menuList);
        menuGamepad = findViewById(R.id.menu_gamepad);
        menuIMU = findViewById(R.id.menu_imu);
        menuAbout = findViewById(R.id.menu_about);

        gamepadLists = findViewById(R.id.gamepadLists);
        gamepadView = findViewById(R.id.gamepadView2);
        gamepadList2 = findViewById(R.id.gamepadListView);
        imuoptionLists = findViewById(R.id.imuoptionLists);
        aboutLists = findViewById(R.id.aboutLists);

        seekBarSensitivity = findViewById(R.id.seekBar);
        textSeekBarValue = findViewById(R.id.textSensitibity);
        calibration = findViewById(R.id.sw_calibration);
        calibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    calibration.setText(getString(R.string.on));
                } else {
                    calibration.setText(getString(R.string.off));
                }
            }
        });

//		frame_newVersion = findViewById( R.id.frame_newversion );
        currentVersion = findViewById(R.id.value_currentVersion);
        lastUpdateDay = findViewById(R.id.value_updateDay);
        updateVersion = findViewById(R.id.value_newVersion);

        currentVersion.setText(String.format("1.%s", String.valueOf(version)));
        updateVersion.setText(String.format("1.%s", String.valueOf(update)));

        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // IMU Sensitivity Source
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sensitivityValue = (float) ((progress + 1) / 100.0);
                textSeekBarValue.setText(String.valueOf(sensitivityValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void SetupGamepadList() {
        GamepadVO g;
        int list = 0;

        List<DeviceModel> models = mDeviceManager.getBondedDevices();

        for (DeviceModel model : models) {
            if (!model.getMACAddress().equals(INIT_ADDRESS)) {
                EventAddGamepad(model.getMACAddress());
                list++;
            }
        }

        if (datas.size() == 0) {
            // No Gamepad
            g = new GamepadVO();
            g.setGamepadName(String.format("Game Pad %d", list));
            datas.add(g);
        }

//        while (datas.size() > 0 && datas.size() >= list) {
//            // TODO Get GAMEPAD Data to List View
//            if (datas.get(list).getGamepadName() == getString(R.string.none_found)) {
//                datas.remove(list);
//                continue;
//            }
//            Toast.makeText(this, datas.get(list).getMACAddress(), Toast.LENGTH_SHORT).show();
//            EventAddGamepad(datas.get(list).getMACAddress());
//            list++;
//        }
//        if (datas.size() == 0) {
//            // No Gamepad
//            g = new GamepadVO();
//            g.setGamepadName(String.format("Gamepad %d", list));
//            datas.add(g);
//        }
    }

    public void RefreshGamepadList() {
        GamepadListAdapter adapter = new GamepadListAdapter(this, R.layout.gamepad_info, datas);
        gamepadList2.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        // User press Back Button
        if (menuButtons.getVisibility() == View.GONE) {
            gamepadLists.setVisibility(View.GONE);
            gamepadView.setVisibility(View.GONE);
            imuoptionLists.setVisibility(View.GONE);
            aboutLists.setVisibility(View.GONE);
            activityTitle.setText(R.string.title_gamepad_advanced);
            menuButtons.setVisibility(View.VISIBLE);
        } else finish();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onClickMenu(View view) {
        gamepadLists.setVisibility(View.GONE);
        gamepadView.setVisibility(View.GONE);
        imuoptionLists.setVisibility(View.GONE);
        aboutLists.setVisibility(View.GONE);
        switch (view.getId()) {
            case R.id.menu_gamepad:
                SetupGamepadList();
                RefreshGamepadList();
                gamepadList2.setOnItemClickListener(this);
                gamepadView.setVisibility(View.VISIBLE);
                activityTitle.setText(R.string.menu_gamepad);
                break;
            case R.id.menu_imu:
                imuoptionLists.setVisibility(View.VISIBLE);
                activityTitle.setText(R.string.menu_imu);
                break;
            case R.id.menu_about:
                aboutLists.setVisibility(View.VISIBLE);
                activityTitle.setText(R.string.menu_about);
                break;
        }
        menuButtons.setVisibility(View.GONE);
    }

    public void onClickVersionCheck(View view) {
        switch (view.getId()) {
//			case R.id.buttonCurrentVersion:
//				frame_newVersion.setVisibility( View.VISIBLE );
//				break;
            case R.id.buttonUpdateVersion:
//				frame_newVersion.setVisibility( View.GONE );
                currentVersion.setText(updateVersion.getText());
                version = update++;
                updateVersion.setText(new StringBuilder().append("1.").append(String.valueOf(update)).toString());
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Gamepad Selected.
        Toast.makeText(this, datas.get(position).getGamepadName(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof BluetoothDevice) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) object;
            Crashlytics.log(Log.INFO, "FOTA2", "Detected Bluetooth: " + bluetoothDevice.getAddress());
            EventAddGamepad(bluetoothDevice.getAddress());
        } else if (((String) object).contains("BlueTooth_Connected")) {
            //蓝牙已连接
            Toast.makeText(this, "BlueTooth_Connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void EventAddGamepad(String macAddress) {
        if (macAddress == getString(R.string.unknown)) return;
        GamepadVO g = new GamepadVO(macAddress);
        Toast.makeText(this, "Detected Bluetooth: " + macAddress, Toast.LENGTH_SHORT).show();
        g.setGamepadName(String.format("Gamepad %d", datas.size() + 1));
        datas.add(g);
    }
}