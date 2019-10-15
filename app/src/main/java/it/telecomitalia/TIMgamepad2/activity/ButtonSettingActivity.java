package it.telecomitalia.TIMgamepad2.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import it.telecomitalia.TIMgamepad2.OptionListAdapter;
import it.telecomitalia.TIMgamepad2.OptionListVO;
import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.utils.SharedPreferenceUtils;

public class ButtonSettingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
	
	ListView listView;
	ArrayList<OptionListVO> list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_button_setting);
		
		listView = findViewById(R.id.o_button_list);
		list = new ArrayList<>();
		list.add(new OptionListVO(getApplicationContext().getString(R.string.list_camera), null));
		list.add(new OptionListVO(getApplicationContext().getString(R.string.list_screenshot), null));
		list.add(new OptionListVO(getApplicationContext().getString(R.string.list_turbo), "Set Keys: "));
		
		OptionListAdapter adapter = new OptionListAdapter(this, R.layout.list_string_2_single_choice, list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		
		/// Get Savedata.
		
//		listView.setSelection();
	}
	
	public void onClick(View view) {
	
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (list.get(position).getMainText().equals(getApplicationContext().getString(R.string.list_camera))) {
			/// TODO: Camera Key
			
		}
		else if (list.get(position).getMainText().equals(getApplicationContext().getString(R.string.list_screenshot))) {
			/// TODO: Screenshot Key
		}
		else if (list.get(position).getMainText().equals(getApplicationContext().getString(R.string.list_turbo))) {
			/// TODO: Turbo Key
		
		}
	}
}
