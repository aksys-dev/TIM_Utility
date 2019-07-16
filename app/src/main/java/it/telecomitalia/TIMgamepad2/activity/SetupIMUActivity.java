package it.telecomitalia.TIMgamepad2.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import it.telecomitalia.TIMgamepad2.BuildConfig;
import it.telecomitalia.TIMgamepad2.CalibrationGamepadVO;
import it.telecomitalia.TIMgamepad2.CalibrationListAdapter;
import it.telecomitalia.TIMgamepad2.Proxy.BinderProxyManager;
import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;
import it.telecomitalia.TIMgamepad2.utils.SharedPreferenceUtils;

import static it.telecomitalia.TIMgamepad2.BuildConfig.CONFIG_FILE_NAME;
import static it.telecomitalia.TIMgamepad2.BuildConfig.KEY_SENSITIVE;
import static it.telecomitalia.TIMgamepad2.BuildConfig.TEST_A7_ON_A8;

public class SetupIMUActivity extends AppCompatActivity {
    private BluetoothDeviceManager mGamePadDeviceManager;
    Context context;
    private BinderProxyManager mBinderProxy = BinderProxyManager.getInstance();

    /// Views
    SeekBar seekBarSensitivity;
    TextView textSeekBarValue;
    ListView calibrationGamepadList;

    /// Default Values
    private static final float SENSITIVE_DEFAULT = 1.0f;
    float sensitivityValue = 1.00F;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_imu);

        context = this;
        mGamePadDeviceManager = BluetoothDeviceManager.getInstance();

        calibrationGamepadList = findViewById( R.id.calibrationlist );
        calibrationGamepadList.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = view.findViewById( R.id.gamepad_name );
                TextView addressData = view.findViewById( R.id.gamepad_address );
                String addr = addressData.getText().toString();
                String name = textView.getText().toString();
                LogUtil.i( "Select " + name );
                if (!textView.getText().toString().equals( getString(R.string.no_gamepad) )
                        && !addr.equals( getResources().getText( R.string.gamepad_offline ) )) {
                    LogUtil.i( "Mac Address " + addr );
                    gotoIMUCalibrationScene( name, addr );
                }
            }
        } );

        seekBarSensitivity = findViewById(R.id.seekBar);
        textSeekBarValue = findViewById(R.id.textSensitibity);

        float restoreValue = (float) SharedPreferenceUtils.get(CONFIG_FILE_NAME, context, KEY_SENSITIVE, SENSITIVE_DEFAULT);

        textSeekBarValue.setText(String.valueOf(restoreValue));
        seekBarSensitivity.setProgress((int) (restoreValue * 100));

        seekBarSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // IMU Sensitivity Source
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    progress = 1;
                }

                sensitivityValue = (float) ((progress) / 100.0);
                textSeekBarValue.setText(String.valueOf(sensitivityValue));
                SharedPreferenceUtils.put(CONFIG_FILE_NAME, context, KEY_SENSITIVE, sensitivityValue);

                // TODO: Check is Proxy need
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    mBinderProxy.setSensitivity(sensitivityValue);
//                } else {
//                    if (BuildConfig.ANDROID_7_SUPPORT_IMU)
//                        mProxyManager.setSensitivity((byte) (sensitivityValue * 100));
//                }
//
//                if (TEST_A7_ON_A8) {
//                    mProxyManager.setSensitivity((byte) (sensitivityValue * 100));
//                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SetupIMUEnabledGamepadList();
    }

    private void gotoIMUCalibrationScene(final String name, final String addr) {
        AlertDialog.Builder b = new AlertDialog.Builder( this );
        b.setTitle( getString( R.string.Calibration ) + " " + name );
        b.setMessage( R.string.calibration_ready );
        b.setPositiveButton( R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent( context, SensorCalibrationActivity.class );
                intent.putExtra( "gpname", name );
                intent.putExtra( "gpaddr", addr );
                startActivity( intent );
            }
        } );
        b.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        } );
        b.show();
    }

    public void SetupIMUEnabledGamepadList() {
        CalibrationGamepadVO c;
        ArrayList<CalibrationGamepadVO> enabledGamepads = new ArrayList<>();
        int list = 1;
        enabledGamepads.clear();
        List<DeviceModel> models = mGamePadDeviceManager.getBondedDevices();
        if ( models == null || models.size() == 0 ) {
            LogUtil.w( "No Gamepad enabled IMU" );
            c = new CalibrationGamepadVO(
                    getString( R.string.no_gamepad ),
                    null, false
            );
            enabledGamepads.add( c );
        } else {
            for ( DeviceModel model : models ) {
                if ( model.imuEnabled() ) {
                    c = new CalibrationGamepadVO( String.format( "Gamepad %d", list )
                            , model.getMACAddress(), model.online() );
                    enabledGamepads.add( c );
                    list++;
                }
            }
            if ( enabledGamepads.size() == 0 ) {
                // No IMU Enabled Gamepad.
                LogUtil.w( "No Gamepad enabled IMU" );
                c = new CalibrationGamepadVO(
                        getString( R.string.no_gamepad ),
                        null, false
                );
                enabledGamepads.add( c );
            }
        }

        CalibrationListAdapter adapter = new CalibrationListAdapter(this, R.layout.gamepad_calibration_list, enabledGamepads );
        calibrationGamepadList.setAdapter( adapter );
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void getEvent(final Object object) {
        if (object instanceof BluetoothDevice) {
            BluetoothDevice bluetoothDevice = (BluetoothDevice) object;
            Crashlytics.log(Log.INFO, "FOTA2", "Detected Bluetooth: " + bluetoothDevice.getAddress());
            SetupIMUEnabledGamepadList();
        }
//        else if ((object instanceof String) && ((String)object).contains("BlueTooth_Connected")) {
//            //蓝牙已连接
//            Toast.makeText(this, "BlueTooth_Connected", Toast.LENGTH_SHORT).show();
//        }
    }
}
