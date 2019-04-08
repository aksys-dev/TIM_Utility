package it.telecomitalia.TIMgamepad2.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.fota.BluetoothDeviceManager;
import it.telecomitalia.TIMgamepad2.fota.DeviceModel;
import it.telecomitalia.TIMgamepad2.fota.SensorCalibrationEvent;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;
import it.telecomitalia.TIMgamepad2.utils.SharedPreferenceUtils;

public class SensorCalibrationActivity extends AppCompatActivity {
	BluetoothDeviceManager deviceManager;
	DeviceModel device;
	
	TextView calibrationMessage, normalMessage;
	TextView monitorIMU, monitorSavedIMU;
	ProgressBar StatusProgress;
	Handler handler;
	boolean isHardMode;
	boolean isWorking;
	
	final int USER_WAIT_TIME = 5000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_sensor_calibration );
		deviceManager= BluetoothDeviceManager.getInstance();
		
		calibrationMessage = findViewById( R.id.status_calibration );
		normalMessage = findViewById( R.id.message );
		StatusProgress = findViewById( R.id.status_progress );
		monitorIMU = findViewById( R.id.view_imudata );
		monitorIMU.setVisibility( View.GONE );
		monitorSavedIMU = findViewById( R.id.view_savedimu );
		monitorSavedIMU.setVisibility( View.GONE );
		isHardMode = false;
		isWorking = true;
		
		LogUtil.d( "Calibration Activity" );
		handler = new Handler();
		
		if (getIntent().hasExtra( "gpaddr" )) {
			SetGamepadEvent(
					getIntent().getStringExtra( "gpname" ),
					getIntent().getStringExtra( "gpaddr" )
			);
		}
		else {
			Toast.makeText( this, "Wrong ", Toast.LENGTH_SHORT ).show();
		}
	}
	
	@Override
	public void onBackPressed() {
		if ( ! isWorking ) finish();
	}
	
	public void SetGamepadEvent(String name, String address) {
		LogUtil.d( name + " - " + address );
		device = deviceManager.getDeviceModelByAddress( address );
		if (device == null) {
			LogUtil.w( "Not Found Device!" );
			super.onBackPressed();
		}
		
		calibrationMessage.setText( "Calibration " + name );
		normalMessage.setText( R.string.gamepad_calibration_first );
		
		device.getSPPConnection().setCalibrationEventListener( calibrationEvent );
		handler.postDelayed( SetCalibrationSoftly, USER_WAIT_TIME );
	}
	
	Runnable SetCalibrationSoftly = new Runnable() {
		@Override
		public void run() {
			normalMessage.setText( R.string.gamepad_calibration_wait );
			device.getSPPConnection().setCalibrationClear();
			device.getSPPConnection().startCalibration();
		}
	};
	
	Runnable EndCalibrationSoftly = new Runnable() {
		@Override
		public void run() {
			isWorking = false;
			calibrationMessage.setText( "It's Done!" );
			StatusProgress.setIndeterminate( false );
			StatusProgress.setMax( 100 );
			StatusProgress.setProgress( 100 );
			normalMessage.setText( R.string.gamepad_calibration_end );
			monitorIMU.setVisibility( View.GONE );
			monitorSavedIMU.setVisibility( View.GONE );
		}
	};
	
	Runnable CheckProgress = new Runnable() {
		@Override
		public void run() {
			StatusProgress.setIndeterminate( false );
			StatusProgress.setMax( progressMax );
			StatusProgress.setProgress( progressNow );
		}
	};
	
	int sx,sy,sz, zx,zy,zz, progressNow, progressMax;
	
	Runnable ViewCalibrationValue = new Runnable() {
		@Override
		public void run() {
			monitorIMU.setText( String.format( "X: %+05d\nY: %+05d\nZ: %+05d", sx, sy, sz ) );
			monitorSavedIMU.setText( String.format( "(%+05d)\n(%+05d)\n(%+05d)", zx, zy, zz ) );
		}
	};
	
	SensorCalibrationEvent calibrationEvent = new SensorCalibrationEvent() {
		@Override
		public void getGyroscopeValue(final int x, final int y, final int z) {
			sx = x; sy = y; sz = z;
			handler.post( ViewCalibrationValue );
		}
		
		@Override
		public void getSavedGyroZero(final int x, final int y, final int z) {
			zx = x; zy = y; zz = z;
			handler.post( ViewCalibrationValue );
			handler.post( EndCalibrationSoftly );
			SaveCalibrationData( x,y,z);
		}
		
		@Override
		public void progressCalibration(int progress, int max) {
			progressNow = progress; progressMax = max;
			handler.post( CheckProgress );
		}
		
		@Override
		public void startCalibration() {
			/// working in gamepad automately.
			handler.removeCallbacks( SetCalibrationSoftly );
			normalMessage.setText( R.string.gamepad_calibration_wait );
		}
		
		@Override
		public void endCalibration() {
			/// its over.
			isWorking = false;
			normalMessage.setText( R.string.gamepad_calibration_end );
			int[] gyrovalues = device.getSPPConnection().getResultSensorCalibration();
			String textvalue = "";
			for (int val : gyrovalues) {
				if (textvalue != "") textvalue += ",";
				textvalue += String.valueOf( val );
			}
		}
	};
	
	void SaveCalibrationData(final int x, final int y, final int z) {
		String data = x+","+y+","+z;
		SharedPreferenceUtils.put( device.getMACAddress(), this, "calibration", data );
	}
	
	void SaveCalibrationHardmode() {
		SharedPreferenceUtils.put( device.getMACAddress(), this, "calibration", "HARDWARE" );
	}
	
	public static int[] getCalibrationData(String xyz) {
		if (xyz.indexOf( "," ) >= 0) {
			String[] xyzvalue = xyz.split( "," );
			int[] value = new int[ 3 ];
			value[ 0 ] = Integer.valueOf( xyzvalue[ 0 ] );
			value[ 1 ] = Integer.valueOf( xyzvalue[ 1 ] );
			value[ 2 ] = Integer.valueOf( xyzvalue[ 2 ] );
			return value;
		}
		return new int[] {0,0,0};
	}
}
