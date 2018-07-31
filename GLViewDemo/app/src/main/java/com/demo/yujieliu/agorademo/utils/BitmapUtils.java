package com.demo.yujieliu.agorademo.utils;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yujieliu on 2018/7/31.
 */

public class BitmapUtils {

    /**
     * 保存图片到指定目录路径中，并返回文件名。保存格式为JPG，质量为90%
     *
     * @param dirpath 保存目录路径
     * @param bitmap  Bitmap
     * @return 文件名，非路径名
     */
    public static String saveBitmap(String dirpath, Bitmap bitmap) {
        return saveBitmap(dirpath, bitmap, Bitmap.CompressFormat.JPEG, 90);
    }

    /**
     * 保存图片到指定目录路径中，并返回文件名
     *
     * @param saveDirectoryPath 保存目录路径
     * @param bitmap            Bitmap
     * @param format            图片格式
     * @param quality           图片质量
     * @return 图片文件名，不是路径名
     */
    public static String saveBitmap(String saveDirectoryPath, Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        File dir = new File(saveDirectoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Date date = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()); // 格式化时间
        String filename = timeFormat.format(date) + ".jpg";

        File file = new File(saveDirectoryPath, filename);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(format, quality, out)) {
                out.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filename;
    }
}
