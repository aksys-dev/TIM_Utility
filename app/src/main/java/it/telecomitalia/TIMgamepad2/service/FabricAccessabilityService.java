package it.telecomitalia.TIMgamepad2.service;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import it.telecomitalia.TIMgamepad2.utils.LogUtil;

/**
 * Created by cmx on 2018/8/22.
 */

public class FabricAccessabilityService extends AccessibilityService{
    private static final String TAG = "FabricAccessabilityService";

    @Override
    public void onCreate() {
        LogUtil.d(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        LogUtil.d(TAG, "Keycode : " + event.getKeyCode());
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            switch(event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:

                    break;
            }
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }
}
