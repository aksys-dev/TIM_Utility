package it.telecomitalia.TIMgamepad2.utils;

import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.telecomitalia.TIMgamepad2.GamePadV2UpgadeApplication;


/**
 * 控制Log打印
 *
 * @author czy
 */
public class LogUtil {
    private static final boolean DEBUG = GamePadV2UpgadeApplication.IS_DEBUG;

    //private static String TUTONG_LOG_FILE = getSDPath() + "/gamepadv2upagde_log.log";


    private static final String TAG = "GamePadUtility";

    private static final boolean DEBUG_LINE = false;

//    private static String LOG_FILE = getSDPath() + "/gamepad_log.log";

    public static String getStringResource(int stringResource) {
        return GamePadV2UpgadeApplication.getStringResource(stringResource);
    }

    public static void d(String TAG, String method, String msg) {
        if (DEBUG) Log.d(TAG, "[" + method + "]" + msg);
    }

    public static void d(String TAG, String msg) {
        if (DEBUG) {
            Log.d(TAG, getFileLineMethod() + msg);
        }
    }

    public static void d(String TAG, int msgResource) {
        if (DEBUG) {
            Log.d(TAG, getFileLineMethod() + getStringResource(msgResource));
        }
    }

//    public static void d(String msg) {
//        if (DEBUG) {
//            Log.d(_FILE_(), "[" + getLineMethod() + "]" + msg);
//        }
//    }

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, getFileLineMethod() + msg);
        }
    }

    public static void d(int msgResource) {
        if (DEBUG) {
            Log.d(TAG, getLineMethod() + getStringResource(msgResource));
        }
    }

    public static void d(int msgResource, Object... args) {
        if (DEBUG) Log.d(TAG, getLineMethod() + String.format(getStringResource(msgResource), args));
    }

    public static void d(String TAG, int msgResource, Object... args) {
        if (DEBUG) Log.d(TAG, getLineMethod() + String.format(getStringResource(msgResource), args));
    }

    public static void w(String TAG, String msg) {
        if (DEBUG) {
            Log.w(TAG, "[" + getFileLineMethod() + "]" + msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) Log.w(TAG, getFileLineMethod() + msg);
    }

    public static void w(int msgRes) {
        if (DEBUG) Log.w(TAG, getFileLineMethod() + getStringResource(msgRes));
    }

    public static void w(int msgResource, Object... args) {
        if (DEBUG) Log.w(TAG, getLineMethod() + String.format(getStringResource(msgResource), args));
    }

    public static void i(String TAG, String msg) {
        if (DEBUG) {
            Log.i(TAG, "[" + getFileLineMethod() + "]" + msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            Log.i(TAG, getLineMethod() + msg);
        }
    }

    public static void i(int msgResource) {
        if (DEBUG) {
            Log.i(TAG, getLineMethod() + getStringResource(msgResource));
        }
    }

    public static void i(int msgResource, Object... args) {
        if (DEBUG) {
            Log.i(TAG, getLineMethod() + String.format(getStringResource(msgResource), args));
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, getLineMethod() + msg);
        }
    }

    public static void e(int msgRes) {
        if (DEBUG) Log.e(TAG, getFileLineMethod() + getStringResource(msgRes));
    }

    public static void e(int msgResource, Object... args) {
        if (DEBUG) Log.e(TAG, getLineMethod() + String.format(getStringResource(msgResource), args));
    }

    public static void l() {
        if (DEBUG_LINE) {
            Log.i(TAG, getLineMethod());
        }
    }

    public static void e(String TAG, String msg) {
        if (DEBUG) Log.e(TAG, getLineMethod() + msg);
    }

//    public static void f(String TAG, String msg) {
//        if (DEBUG) {
//            try {
//                FileWriter fw = new FileWriter(LOG_FILE, true);
//                fw.write(msg + "\n");
//                fw.close();
//                i(TAG, msg);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static String getFileLineMethod() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        StringBuffer toStringBuffer = new StringBuffer("[(")
                .append(traceElement.getFileName()).append(":")
                .append(traceElement.getLineNumber()).append(")")
                .append(traceElement.getMethodName()).append("]  ");
        return toStringBuffer.toString();
    }

    public static String getLineMethod() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        StringBuffer toStringBuffer = new StringBuffer("[")
                .append(traceElement.getLineNumber()).append("|")
                .append(traceElement.getMethodName()).append("]");
        return toStringBuffer.toString();
    }

    public static String _FILE_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getFileName();
    }

    public static String _FUNC_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        return traceElement.getMethodName();
    }

    public static int _LINE_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        return traceElement.getLineNumber();
    }

    public static String _TIME_() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(now);
    }

    private static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        }
        return sdDir.toString();
    }

    private static long _time_start = -1;
    private static long _time_end = -1;

    public static void timeStart() {
        _time_start = System.currentTimeMillis();
    }

    public static void timeEnd(String msg, boolean isSeconds) {
        _time_end = System.currentTimeMillis();
        i("time", msg + " cost time ========>" + (isSeconds ? ((_time_end - _time_start) / 1000) : (_time_end - _time_start)));
        _time_start = -1;
        _time_end = -1;
    }

    public static void printStackTraces() {
        if (DEBUG) {
            Throwable ex = new Throwable();
            StackTraceElement[] stackElements = ex.getStackTrace();
            if (stackElements != null) {
                for (int i = 0; i < stackElements.length; i++) {
                    i(stackElements[i].getClassName() + "\t" + stackElements[i].getLineNumber() + "\t" + stackElements[i].getMethodName());
                }
            }
        }
    }
    
    public static void logKeyEvent(KeyEvent event) {
        d("Controller: " + event.getDevice().getName() + " ( Code: " + event.getKeyCode() + ")" );
    }
}
