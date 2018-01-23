package com.demo.lauyukit.opengldemo.utils;

import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;

/**
 * 流操作相关Utility
 * <p>
 * Created by Frost on 2017/12/27.
 */
public class StreamUtils {

    public static void closeSilently(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
