package com.demo.lauyukit.opengldemo.program;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL绘制程序接口
 * <p>
 * Created by LiuYujie on 2018/1/16.
 */
public interface DrawProgram {
    int SIZEOF_FLOAT = 4;
    /**
     * 创建GL程序，这两个参数其实我也不知道有啥用，以后再研究吧
     *
     * @param gl     GL
     * @param config EGLConfig
     */
    void createProgram(GL10 gl, EGLConfig config);

    /**
     * Surface尺寸变化时触发
     *
     * @param gl     GL
     * @param width  新Surface宽度
     * @param height 新Surface高度
     */
    void onSizeChanged(GL10 gl, int width, int height);

    /**
     * 执行绘制操作
     *
     * @param gl GL
     */
    void draw(GL10 gl);
}
