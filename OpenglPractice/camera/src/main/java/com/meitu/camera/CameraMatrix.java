package com.meitu.camera;

/**
 * 相机模块的矩阵
 * <p/>
 * Created by 周代亮 on 2018/1/29.
 */

public class CameraMatrix {

    /**
     * 顶点坐标
     */
    public static final float[] VERTEX = {
            -1, 1, 0,
            -1, -1, 0,
            1, -1, 0,
            1, 1, 0,
    };

    /**
     * 索引坐标
     */
    public static final short[] INDICES = {
            0, 1, 2,
            0, 2, 3
    };

    /**
     * 纹理坐标
     */
    public static final float[] FRAGMENT = {
            0, 0,
            1, 0,
            1, 1,
            0, 1
    };

}
