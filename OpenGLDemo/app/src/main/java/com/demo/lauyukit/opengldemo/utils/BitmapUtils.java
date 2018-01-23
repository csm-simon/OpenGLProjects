package com.demo.lauyukit.opengldemo.utils;

import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Bitmap相关Utility
 * <p>
 * Created by Frost on 2017/12/27.
 */
public class BitmapUtils {

    /**
     * 保存Bitmap到指定文件路径上
     */
    public static boolean saveBitmapToFile(Bitmap bitmap, String filePath) {
        if (bitmap == null || bitmap.isRecycled()) {
            return false;
        }
        BufferedOutputStream bos = null;
        boolean ret = false;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filePath));
            ret = bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            StreamUtils.closeSilently(bos);
        }
        return ret;
    }
}
