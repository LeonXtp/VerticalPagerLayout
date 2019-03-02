package com.leonxtp.fingerview.util;

import android.util.Log;

/**
 * Created by LeonXtp on 2018/12/28 下午9:10
 */
public class Logger {

    public static boolean logging = true;

    public static void d(String tag, String msg) {
        if (logging) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (logging) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (logging) {
            Log.e(tag, msg);
        }
    }

}
