package com.demo.lauyukit.opengldemo.utils;

import android.util.Log;

/**
 * Log wrapper
 * <p>
 * Created by LiuYujie on 2018/1/12.
 */
public class GLog {
    private static final boolean DEBUG = true;

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(wrapTag(tag), msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(wrapTag(tag), msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(wrapTag(tag), msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(wrapTag(tag), msg);
        }
    }

    private static String wrapTag(String tag) {
        return Thread.currentThread().getName() + "->" +  tag;
    }

}
