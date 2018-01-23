package com.meitu.base.util;

/**
 * YUV纹理工具
 * <p/>
 * Created by 周代亮 on 2018/1/23.
 */

public class YUVUtil {

    static {
        System.loadLibrary("yuv-lib");
    }

    public static native int YUV2TextureId(byte[] yuv, int width, int height);

}
