package za.co.inflationcalc.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * Created by Laurie on 9/14/2015.
 */
public class LogUtil {public enum LogLevel {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
    WTF(4),;
    private int mValue = 0;

    LogLevel(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}

    public static LogLevel getLogLevel() {
        return LogLevel.DEBUG;
    }

    private static String getTag() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String[] cname = ste[4].getClassName().split("\\.");
        String className = cname[cname.length - 1];
        return className + "." + ste[4].getMethodName();
    }

    public static void d(String msg, Throwable tr) {
        if (getLogLevel().getValue() <= LogLevel.DEBUG.getValue()) {
            Log.d(getTag(), msg, tr);
        }
    }

    public static void d(String msg) {
        if (getLogLevel().getValue() <= LogLevel.DEBUG.getValue()) {
            Log.d(getTag(), msg);
        }
    }

    public static void i(String msg, Throwable tr) {
        if (getLogLevel().getValue() <= LogLevel.INFO.getValue()) {
            Log.i(getTag(), msg);
        }
    }

    public static void i(String msg) {
        if (getLogLevel().getValue() <= LogLevel.INFO.getValue()) {
            Log.i(getTag(), msg);
        }
    }

    public static void w(String msg, Throwable tr) {
        if (getLogLevel().getValue() <= LogLevel.WARN.getValue()) {
            Log.w(getTag(), msg, tr);
        }
    }

    public static void w(String msg) {
        if (getLogLevel().getValue() <= LogLevel.WARN.getValue()) {
            Log.w(getTag(), msg);
        }
    }

    public static void e(Throwable tr) {
        e("", tr);
    }

    public static void e(String msg, Throwable tr) {
        if (getLogLevel().getValue() <= LogLevel.ERROR.getValue()) {
            Log.e(getTag(), msg, tr);
        }
    }

    public static void e(String msg) {
        if (getLogLevel().getValue() <= LogLevel.ERROR.getValue()) {
            Log.e(getTag(), msg);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void wtf(String msg, Throwable tr) {
        if (getLogLevel().getValue() <= LogLevel.WTF.getValue()) {
            Log.wtf(getTag(), msg, tr);
        }
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void wtf(String msg) {
        if (getLogLevel().getValue() <= LogLevel.WTF.getValue()) {
            Log.wtf(getTag(), msg);
        }
    }

}
