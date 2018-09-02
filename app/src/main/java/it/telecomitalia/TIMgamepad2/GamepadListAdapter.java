package it.telecomitalia.TIMgamepad2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GamepadListAdapter extends ArrayAdapter<GamepadVO> {
	Context context;
	int resId;
	ArrayList<GamepadVO> gamepads;

	public GamepadListAdapter(Context context, int resId, ArrayList<GamepadVO> gamepads) {
		super(context, resId);
		this.context = context;
		this.resId = resId;
		this.gamepads = gamepads;
	}

	@Override
	public int getCount() {
		return gamepads.size();
	}

	public String getGamepadName(int position) {return gamepads.get( position ).GamepadName; }

	@Override
	@NonNull
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			convertView = inflater.inflate( resId, null );
		}

		TableLayout statusTable = (TableLayout)convertView.findViewById( R.id.statusTable );
		TextView gamepadNo = (TextView)convertView.findViewById( R.id.textView2 ); // Gamepad Number
		TextView macAddress = (TextView)convertView.findViewById( R.id.table_mac );
		ImageView batteryImg = (ImageView)convertView.findViewById( R.id.image_battery );
		TextView batteryValue = (TextView)convertView.findViewById( R.id.table_battery );
		final TextView firmware = (TextView)convertView.findViewById( R.id.table_firmware );
		TextView newFirmware = (TextView)convertView.findViewById( R.id.NewFirmware );

		final GamepadVO vo = gamepads.get( position );

		if (vo.MACAddress == vo.unc && position == 0) {
			gamepadNo.setText( R.string.none_found ); // No Gamepad Message.
			vo.setGamepadName( gamepadNo.getText().toString() );
			statusTable.setVisibility( View.GONE );
			newFirmware.setVisibility( View.GONE );
		} else {
			statusTable.setVisibility( View.VISIBLE );
			gamepadNo.setText( vo.GamepadName );
			macAddress.setText( vo.MACAddress );
			if ( vo.Battery > 80 ) batteryImg.setImageResource( R.drawable.battery_max_big );
			else if ( vo.Battery > 20 )
				batteryImg.setImageResource( R.drawable.battery_normal_big );
			else if ( vo.Battery > 0 ) batteryImg.setImageResource( R.drawable.battery_low_big );

			if ( vo.Battery == - 1 ) {
				batteryImg.setVisibility( View.GONE );
				batteryValue.setText( R.string.unknown );
			} else {
				batteryImg.setVisibility( View.VISIBLE );
				batteryValue.setText( String.format( "%d%%", vo.Battery ) );
			}
			firmware.setText( vo.Firmware );
			if ( vo.NeedUpdate ) newFirmware.setVisibility( View.VISIBLE );
			else newFirmware.setVisibility( View.GONE );
		}

		return convertView;
	}
}
