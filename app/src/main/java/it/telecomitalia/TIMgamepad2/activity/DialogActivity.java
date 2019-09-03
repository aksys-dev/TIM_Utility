package it.telecomitalia.TIMgamepad2.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import it.telecomitalia.TIMgamepad2.R;
import it.telecomitalia.TIMgamepad2.service.UpdateFotaMainService;
import it.telecomitalia.TIMgamepad2.utils.LogUtil;


public class DialogActivity extends Activity {

    public static final String INTENT_FROM_SERVICE = "INTENT_FROM_SERVICE";
    public static final String INTENT_FROM_USER = "INTENT_FROM_USER";
    public static final String INTENT_KEY = "INTENT_KEY";
    public static final String INTENT_MAC = "INTENT_MAC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dialog);
//        Button dialog_ok = findViewById(R.id.dialog_ok);
//        Button dialog_cancel = findViewById(R.id.dialog_cancel);
        Intent i = getIntent();  //直接获取传过来的intent
        final String targetMAC = i.getStringExtra(INTENT_MAC);
        final String whereFrom = i.getStringExtra(INTENT_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dailog_tips);
//        builder.setTitle(R.string.firmware);
//        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (whereFrom.equals(INTENT_FROM_SERVICE)) {
                    Intent intents = new Intent(DialogActivity.this, FOTA_V2.class);
                    intents.putExtra(INTENT_KEY, INTENT_FROM_SERVICE);
                    intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intents);
                } else if (whereFrom.equals(INTENT_FROM_USER)) {
                    Intent intentBroadcast = new Intent();
                    intentBroadcast.setAction(UpdateFotaMainService.DIALOG_OK_BROADCAST);
                    intentBroadcast.putExtra(INTENT_KEY, INTENT_FROM_USER);
                    intentBroadcast.putExtra(INTENT_MAC, targetMAC);
                    sendBroadcast(intentBroadcast);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(UpdateFotaMainService.DIALOG_CANCEL_BROADCAST);
                sendBroadcast(intent);
                finish();
            }
        });
        builder.show();

//        dialog_ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                LogUtil.d("OK button clicked");
//
//                if (whereFrom.equals(INTENT_FROM_SERVICE)) {
//                    Intent intents = new Intent(DialogActivity.this, FOTA_V2.class);
//                    intents.putExtra(INTENT_KEY, INTENT_FROM_SERVICE);
//                    intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intents);
//                } else if (whereFrom.equals(INTENT_FROM_USER)) {
//                    Intent intentBroadcast = new Intent();
//                    intentBroadcast.setAction(UpdateFotaMainService.DIALOG_OK_BROADCAST);
//                    intentBroadcast.putExtra(INTENT_KEY, INTENT_FROM_USER);
//                    intentBroadcast.putExtra(INTENT_MAC, targetMAC);
//                    sendBroadcast(intentBroadcast);
//                }
//
//                finish();
//            }
//        });
//
//        dialog_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setAction(UpdateFotaMainService.DIALOG_CANCEL_BROADCAST);
//                sendBroadcast(intent);
//                finish();
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
