package it.telecomitalia.TIMgamepad2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

public class OptionListAdapter extends ArrayAdapter<OptionListVO> {
	Context context;
	int resId;
	ArrayList<OptionListVO> datas;
	public OptionListAdapter(Context context, int resId, ArrayList<OptionListVO> datas) {
		super(context, resId);
		this.context = context;
		this.resId = resId;
		this.datas = datas;
	}
	
	@Override
	public int getCount() {
		return datas.size();
	}
	
	@NonNull
	@Override
	public View getView(int position, View convertView, @Nullable ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (inflater != null)
				convertView = inflater.inflate(resId, null);
		}
		
		CheckedTextView mainText = convertView.findViewById(R.id.checkedTextView);
		TextView subText = convertView.findViewById(R.id.sub_text_view);
		
		final OptionListVO vo = datas.get(position);
		
		mainText.setText(vo.mainText);
		subText.setText(vo.subText);
		
		return convertView;
	}
}