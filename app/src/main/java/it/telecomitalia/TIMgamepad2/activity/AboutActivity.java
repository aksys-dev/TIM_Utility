package it.telecomitalia.TIMgamepad2.activity;

import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import it.telecomitalia.TIMgamepad2.BuildConfig;
import it.telecomitalia.TIMgamepad2.R;

public class AboutActivity extends AppCompatActivity {
	TextView currentVersion, updateVersion;
	TextView lastUpdateDay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		currentVersion = findViewById(R.id.value_currentVersion);
		lastUpdateDay = findViewById(R.id.value_updateDay);
		
		try {
			android.content.pm.PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			currentVersion.setText( packageInfo.versionName);
			lastUpdateDay.setText(BuildConfig.BUILD_TIME);
		} catch (PackageManager.NameNotFoundException e) {
			currentVersion.setText(R.string.app_version);
			lastUpdateDay.setText(R.string.lastupdate);
		}
		
	}
}
