package com.meitu.base.util;

import android.opengl.GLES20;

/**
 * 检测工具
 * <p/>
 * Created by 周代亮 on 2018/1/23.
 */

public class CheckUtil {

    /**
     * 检测GL错误
     *
     * @param op 区别是哪里报错的标识
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            throw new RuntimeException(msg);
        }
    }

}
