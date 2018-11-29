package it.telecomitalia.TIMgamepad2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class GamepadListAdapter extends ArrayAdapter<GamepadVO> {
    private Context context;
    private int resId;
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

    @Override
    @NonNull
    public View getView(int position, View convertView, @Nullable ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null)
                convertView = inflater.inflate(resId, null);
        }

        TableLayout statusTable = convertView.findViewById(R.id.statusTable);
        TextView gamepadNo = convertView.findViewById(R.id.textView2); // Gamepad Number
        TextView macAddress = convertView.findViewById(R.id.table_mac);
        ImageView batteryImg = convertView.findViewById(R.id.image_battery);
        TextView batteryValue = convertView.findViewById(R.id.table_battery);
        final TextView firmware = convertView.findViewById(R.id.table_firmware);
        final TextView latestFirmware = convertView.findViewById(R.id.latest_table_firmware);
        TextView online = convertView.findViewById(R.id.table_online);
        TextView newFirmware = convertView.findViewById(R.id.NewFirmware);
        TableRow firmware_row = convertView.findViewById(R.id.firmware_row);
        TableRow last_firmware_row = convertView.findViewById(R.id.latest_firmware_row);
        TableRow battery_row = convertView.findViewById(R.id.battery_row);


        final GamepadVO vo = gamepads.get(position);

        if (vo.MACAddress.equals(vo.unc) && position == 0) {
            gamepadNo.setText(R.string.none_found); // No Gamepad Message.
            vo.setGamepadName(gamepadNo.getText().toString());
            statusTable.setVisibility(View.GONE);
            newFirmware.setVisibility(View.GONE);
        } else {
            statusTable.setVisibility(View.VISIBLE);
            gamepadNo.setText(vo.GamepadName);
            macAddress.setText(vo.MACAddress);
            if (vo.mOnLine) {
                if (vo.Battery > 80) batteryImg.setImageResource(R.drawable.battery_max_big);
                else if (vo.Battery > 30)
                    batteryImg.setImageResource(R.drawable.battery_normal_big);
                else if (vo.Battery > 0) batteryImg.setImageResource(R.drawable.battery_low_big);
                if (vo.Battery == -1) {
                    batteryImg.setVisibility(View.GONE);
                    batteryValue.setText(R.string.unknown);
                } else {
                    batteryImg.setVisibility(View.VISIBLE);
                    batteryValue.setText(String.format(Locale.getDefault(), "%.0f%%", vo.Battery));
                }
                firmware.setText(vo.Firmware);
                latestFirmware.setText(vo.getLatestFWVersion());
                battery_row.setVisibility(View.VISIBLE);
                firmware_row.setVisibility(View.VISIBLE);
                last_firmware_row.setVisibility(View.VISIBLE);
            } else {
                battery_row.setVisibility(View.GONE);
                firmware_row.setVisibility(View.GONE);
                last_firmware_row.setVisibility(View.GONE);
            }
            online.setText(deviceOnline(vo.mOnLine));
            if (vo.NeedUpdate) {
                newFirmware.setVisibility(View.VISIBLE);
            } else {
                newFirmware.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private String deviceOnline(boolean online) {
        return online ? context.getString(R.string.gamepad_online) : context.getString(R.string.gamepad_offline);
    }
}
