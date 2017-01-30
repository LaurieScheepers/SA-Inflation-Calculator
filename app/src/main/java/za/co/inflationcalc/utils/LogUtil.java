package za.co.inflationcalc.utils;

import android.util.Log;

import za.co.inflationcalc.ui.activities.MainActivity;

/**
 * Created by Laurie on 9/14/2015.
 */
public class LogUtil {

    private static String getTag() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String[] cname = ste[4].getClassName().split("\\.");
        String className = cname[cname.length - 1];
        return className + "." + ste[4].getMethodName();
    }

    public static void d(String msg) {
        if (MainActivity.DEBUG) {
            Log.d(getTag(), msg);
        }
    }

    public static void e(Throwable tr) {
        e("", tr);
    }

    public static void e(String msg, Throwable tr) {
        if (MainActivity.DEBUG) {
            Log.e(getTag(), msg, tr);
        }
    }
}
