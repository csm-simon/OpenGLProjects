package com.demo.lauyukit.opengldemo.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.demo.lauyukit.opengldemo.program.DrawProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 最简单的Renderer实现类，仅做画布清空的操作
 * <p>
 * Created by LiuYujie on 2017/12/25.
 */
public class SimpleRenderer implements GLSurfaceView.Renderer {
    /**
     * 承载绘制逻辑的Program
     */
    private final DrawProgram mProgram;

    public SimpleRenderer(DrawProgram program) {
        mProgram = program;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 指定了清空画布颜色时用黑色来清
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        mProgram.createProgram(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mProgram.onSizeChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清空画布
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mProgram.draw(gl);
    }
}
