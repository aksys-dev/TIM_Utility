package it.telecomitalia.TIMgamepad2;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CalibrationListAdapter extends ArrayAdapter<CalibrationGamepadVO> {
	private Context context;
	private int resId;
	ArrayList<CalibrationGamepadVO> gamepads;
	
	public CalibrationListAdapter(Context context, int resId, ArrayList<CalibrationGamepadVO> gamepads) {
		super(context, resId);
		this.context = context;
		this.resId= resId;
		this.gamepads = gamepads;
	}
	
	@Override
	public int getCount() {
		return gamepads.size();
	}
	
	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (inflater != null)
				convertView = inflater.inflate(resId, null);
		}
		
		TextView gamepadNo = convertView.findViewById(R.id.gamepad_name); // Gamepad Number
		TextView gamepadAddr = convertView.findViewById(R.id.gamepad_address); // Gamepad Address
		ImageView gamepadIcon = convertView.findViewById(R.id.gamepad_ico);
		
		gamepadAddr.setVisibility( View.GONE );
		
		final CalibrationGamepadVO vo = gamepads.get( position );
		gamepadNo.setText( vo.GamepadName );
		gamepadAddr.setText( vo.getMACAddress() );
		gamepadIcon.setImageResource( R.drawable.gamepad_big );
		if (!vo.online && vo.getGamepadName() != getContext().getString( R.string.no_gamepad )) {
			gamepadAddr.setVisibility( View.VISIBLE );
			gamepadAddr.setText( R.string.gamepad_offline );
		}
		
		return convertView;
	}
}
