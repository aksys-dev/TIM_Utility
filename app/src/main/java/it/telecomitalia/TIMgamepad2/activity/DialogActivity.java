package it.telecomitalia.TIMgamepad2.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.service.UpdateFotaMainService;

/**
 * Created by czy on 2017/3/8.
 */

public class DialogActivity extends Activity {
    private Button dialog_ok;
    private Button dialog_cancel;
    private TextView dialog_txt;
    public static final String EXTRA_UPGRADE_BACKGROUND = "BackGround";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        final String action = intent.getExtras().getString(EXTRA_UPGRADE_BACKGROUND, "");

        setContentView(R.layout.activity_dialog);
        dialog_ok=(Button)findViewById(R.id.dialog_ok);
        dialog_cancel=(Button)findViewById(R.id.dialog_cancel);
        dialog_txt=(TextView)findViewById(R.id.dialog_txt);
        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentBroadcast= new Intent();
                intentBroadcast.setAction(UpdateFotaMainService.DIALOG_OK_BROADCAST);
                sendBroadcast(intentBroadcast);

                finish();
            }
        });

        dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(action == EXTRA_UPGRADE_BACKGROUND) {

                } else {
                    Intent intent = new Intent();
                    intent.setAction(UpdateFotaMainService.DIALOG_CANCEL_BROADCAST);
                    sendBroadcast(intent);
                    finish();
                }
            }
        });
    }
}
