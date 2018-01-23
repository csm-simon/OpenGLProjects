package com.meitu.base.util;

import android.content.Context;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 读取GLSL的工具
 * <p/>
 * Created by 周代亮 on 2018/1/18.
 */

public class GLSLUtil {

    /**
     * 读取 raw 文件夹下的 glsl，并返回字符串
     *
     * @param context 上下文
     * @param rawId   raw 资源的id
     * @return 字符串
     */
    public static String read(Context context, @RawRes int rawId) {
        InputStream inputStream = context.getResources().openRawResource(rawId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return body.toString();
    }

}
