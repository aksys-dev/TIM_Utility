package it.telecomitalia.TIMgamepad2.fota;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cmx on 2018/8/14.
 */

public class FabricController {
    private static volatile FabricController mController;

    private static String EVENT_UPGRADE = "FOTA_Upgrade_Statistic";
    private static String EVENT_CONNECTION = "FOTA_Connections";
    private static String EVENT_STREAMING = "FOTA_StreamingGame";
    private static String EVENT_MODE = "FOTA_Mode";
    private static String EVENT_STB_CON = "FOTA_Numbers";
    private static String EVENT_SPEC_KEY = "FOTA_SpecialKey";
    private static Context mContext;

    public static final String STATUS_CONNECTED = "connected";
    public static final String STATUS_DISCONNECTED = "disconnected";
    public static final String STATUS_UNPAIRED = "unpaired";

    public static final String GP_V2_NAME = "V2";

    private FabricController() {
        Fabric.with(mContext, new Crashlytics());
    }

    public static void setContext(Context context) {
        mContext = context;
    }

    public static FabricController getInstance() {
        if (mController == null) {
            if (mController == null) {
                mController = new FabricController();
            }
        }
        return mController;
    }

    public void initializeFabric() {
        LoginEvent event = new LoginEvent().putMethod(Build.MODEL + "(" + Build.VERSION.RELEASE + ")").putSuccess(true);
        Answers.getInstance().logLogin(event);
    }

    public void upgradeStatistics(String preVerion, String newVersion, String gamepad, String result) {
        CustomEvent event = new CustomEvent(EVENT_UPGRADE)
                .putCustomAttribute("FW_previous", assertNull(preVerion))
                .putCustomAttribute("FW_upgraded", assertNull(newVersion))
                .putCustomAttribute("gamepad", assertNull(gamepad))
                .putCustomAttribute("result", assertNull(result));
        logCustom(event);
    }

    public void gamepadConnection(int numbers, String action, String gamepad, String version) {
        CustomEvent event = new CustomEvent(EVENT_CONNECTION)
                .putCustomAttribute("connections", numbers)
                .putCustomAttribute("action", assertNull(action))
                .putCustomAttribute("version", assertNull(version))
                .putCustomAttribute("gamepad", assertNull(gamepad));
        logCustom(event);
    }

    public void gamepadPlaying(int gamepadv1, int gamepadv2) {
        CustomEvent event = new CustomEvent(EVENT_STREAMING)
                .putCustomAttribute("V1_playing", gamepadv1)
                .putCustomAttribute("V2_playing", gamepadv2);
        logCustom(event);
    }

    public void gamepadMode(String mode) {
        CustomEvent event = new CustomEvent(EVENT_MODE)
                .putCustomAttribute("mode", assertNull(mode));
        logCustom(event);
    }

    public void gamepadNumPerSTB(int gamepadV1, int gamepadV2) {
        CustomEvent event = new CustomEvent(EVENT_STB_CON)
                .putCustomAttribute("connections", gamepadV1 + gamepadV2)
                .putCustomAttribute("conn_V1", gamepadV1)
                .putCustomAttribute("conn_V2", gamepadV2);
        logCustom(event);
    }

    public void specialKey(String keycode) {
        CustomEvent event = new CustomEvent(EVENT_SPEC_KEY)
                .putCustomAttribute("pressed_special", assertNull(keycode));
        logCustom(event);
    }

    private void logCustom(CustomEvent event) {
        Answers.getInstance().logCustom(event);
    }

    private String assertNull(String item) {
        return !TextUtils.isEmpty(item) ? item : "";
    }
}
