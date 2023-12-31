package it.telecomitalia.TIMgamepad2;

import android.app.Application;
import android.os.Build;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;

import it.telecomitalia.TIMgamepad2.fota.FabricController;

/**
 * Created by czy on 2018/6/25.
 */

public class GamePadV2UpgadeApplication extends Application{
    //是否是开发版本
    public static final boolean isDebug = true;
    private static GamePadV2UpgadeApplication appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext=this;
        FabricController.setContext(getApplicationContext());
        FabricController.getInstance().initializeFabric();
    }

    public static GamePadV2UpgadeApplication getContext(){
        return appContext;
    }
}
