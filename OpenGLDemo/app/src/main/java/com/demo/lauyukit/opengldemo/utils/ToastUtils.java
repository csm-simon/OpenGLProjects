package com.demo.lauyukit.opengldemo.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Toast
 * <p>
 * Created by Frost on 2017/12/27.
 */
public class ToastUtils {

    public static void show(Context context, String msg) {
        Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, @StringRes int resId) {
        Toast.makeText(context.getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
    }
}
