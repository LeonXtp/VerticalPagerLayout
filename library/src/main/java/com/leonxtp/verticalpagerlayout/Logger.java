package com.leonxtp.verticalpagerlayout;

import android.util.Log;

/**
 * Created by LeonXtp on 2018/12/28 下午9:10
 */
public class Logger {

    /**
     * 日志总开关
     */
    public static boolean logging = false;

    public static void setLogging(boolean isLogging) {
        logging = isLogging;
    }

    public static void d(String tag, String msg) {
        if (logging) {
            Log.d(tag, msg);
        }
    }

    /**
     * @param verbose 是否全部log都需要打，在move事件中，因会产生大量的log，可能看日志时不需要，
     *                可以在开启日志开关的情况下，单独关闭此move事件的日志
     */
    public static void d(String tag, String msg, boolean verbose) {
        if (logging && verbose) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (logging) {
            Log.w(tag, msg);
        }
    }

    /**
     * @param verbose 是否全部log都需要打，在move事件中，因会产生大量的log，可能看日志时不需要，
     *                可以在开启日志开关的情况下，单独关闭此move事件的日志
     */
    public static void w(String tag, String msg, boolean verbose) {
        if (logging && verbose) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (logging) {
            Log.e(tag, msg);
        }
    }

    /**
     * @param verbose 是否全部log都需要打，在move事件中，因会产生大量的log，可能看日志时不需要，
     *                可以在开启日志开关的情况下，单独关闭此move事件的日志
     */
    public static void e(String tag, String msg, boolean verbose) {
        if (logging && verbose) {
            Log.e(tag, msg);
        }
    }

}
