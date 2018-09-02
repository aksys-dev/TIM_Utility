package it.telecomitalia.TIMgamepad2.utils;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;

@SuppressWarnings("WeakerAccess")
public class AndroidHacks {
    //    private static final String TAG = "Applications";
    private static Object sActivityThread;

    public static Object getActivityThread() {
        if (sActivityThread == null) {
            synchronized (AndroidHacks.class) {
                if (sActivityThread == null) {
                    if (Looper.getMainLooper() == Looper.myLooper()) {
                        sActivityThread = getActivityThreadFromUIThread();
                        if (sActivityThread != null) {
                            return sActivityThread;
                        }
                    }
                    Handler handler = new Handler(Looper.getMainLooper());
                    synchronized (AndroidHacks.class) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                sActivityThread = getActivityThreadFromUIThread();
                                synchronized (AndroidHacks.class) {
                                    AndroidHacks.class.notifyAll();
                                }
                            }
                        });
                        try {
                            while (sActivityThread == null) {
                                AndroidHacks.class.wait();
                            }
                        } catch (InterruptedException e) {
                            LogUtil.w("Waiting notification from UI thread error." + e.toString());
                        }
                    }
                }
            }
        }
        return sActivityThread;
    }

    private static Object getActivityThreadFromUIThread() {
        Object activityThread = null;
        try {
            Method method = Class.forName("android.app.ActivityThread").getMethod("currentActivityThread");
            method.setAccessible(true);
            activityThread = method.invoke(null);
        } catch (final Exception e) {
            LogUtil.w("Failed to get ActivityThread from ActivityThread#currentActivityThread. " +
                    "In some case, this method return null in worker thread." + e.toString());
        }
        return activityThread;
    }
}
