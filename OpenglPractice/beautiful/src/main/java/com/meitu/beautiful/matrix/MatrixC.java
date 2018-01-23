package com.meitu.beautiful.matrix;

/**
 * 矩阵常量
 * <p/>
 * Created by 周代亮 on 2018/1/16.
 */

public class MatrixC {

    /**
     * 顶点
     */
    public static final float[] VERTEX = {
            -1, -1, 0,
            1, -1, 0,
            1, 1, 0,
            -1, 1, 0,
    };

    /**
     * 索引
     */
    public static final short[] INDEX = {
            0, 1, 2,
            0, 2, 3
    };

    /**
     * 纹理-0
     */
    public static final float[] TEXTURE = {
            0, 0,
            1, 0,
            1, 1,
            0, 1,
    };

    /**
     * 纹理-90
     */
    public static final float[] TEXTURE_90 = {
            1, 0,
            1, 1,
            0, 1,
            0, 0,
    };

    /**
     * 纹理-180
     */
    public static final float[] TEXTURE_180 = {
            1, 1,
            0, 1,
            0, 0,
            1, 0,
    };

    /**
     * 纹理-270
     */
    public static final float[] TEXTURE_270 = {
            0, 1,
            0, 0,
            1, 0,
            1, 1,
    };

}
