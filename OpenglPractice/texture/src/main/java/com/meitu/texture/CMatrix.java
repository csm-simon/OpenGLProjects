package com.meitu.texture;

/**
 * 加载纹理所要用到的一些数据
 * <p/>
 * Created by 周代亮 on 2018/1/26.
 */

public class CMatrix {

    /**
     * 顶点坐标
     */
    public static final float[] VERTEXS = {
            -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f
    };

    /**
     * 顶点索引
     */
    public static final short[] INDEXS = {
            0, 1, 2,
            0, 2, 3
    };

    /**
     * 纹理坐标
     */
    public static final float[] TEXTURES = {
            0, 0,
            1, 0,
            1, 1,
            0, 1
    };
}
