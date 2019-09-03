package it.telecomitalia.TIMgamepad2.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import it.telecomitalia.TIMgamepad2.R;

public class ButtonSettingActivity extends AppCompatActivity {
	ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_button_setting);
		
		listView = findViewById(R.id.o_button_list);
		List<String> list = new ArrayList<>();
		list.add("Camera Key");
		list.add("Screenshot Key");
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, list);
		
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
	
	public void onClick(View view) {
		view.callOnClick();
	}
}
