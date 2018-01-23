package com.demo.lauyukit.opengldemo.program;

import android.opengl.GLES20;

import com.demo.lauyukit.opengldemo.utils.GlUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 绘制矩形的GL程序
 * <p>
 * Created by LiuYujie on 2018/1/17.
 */
public class DrawRectangleProgram implements DrawProgram {
    /**
     * {@link #VERTEX_SHADER}中的attribute vec4 aPosition的变量名
     */
    private static final String VERTEX_SHADER_A_POSITION = "aPosition";

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "void main() {\n" +
                    "gl_Position = aPosition;\n" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "void main() {\n" +
                    "gl_FragColor = vec4(1, 1, 0, 1);\n" +
                    "}";

    private static final float[] VERTEX = {
            -.5f, -.5f,// bottom left
            -.5f, .5f, // top left
            .5f, .5f, // top right
            .5f, -.5f // bottom right
    };

    private static final short[] DRAW_INDEX = {
            0, 1, 2, // 第一个三角形，用到左下，左上和右上三个点
            0, 2, 3 // 第二个点，用到左下，右下和右上三个点
    };

    private final FloatBuffer mVertexBuffer;
    private final ShortBuffer mDrawIndexBuffer;

    public DrawRectangleProgram() {
        super();
        mVertexBuffer = GlUtils.createFloatBuffer(VERTEX);
        mDrawIndexBuffer = GlUtils.createShortBuffer(DRAW_INDEX);
    }

    @Override
    public void createProgram(GL10 gl, EGLConfig config) {
        int programHandle = GlUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        GLES20.glUseProgram(programHandle);

        int positionLoc = GLES20.glGetAttribLocation(programHandle, VERTEX_SHADER_A_POSITION);
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(positionLoc, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);
    }

    @Override
    public void onSizeChanged(GL10 gl, int width, int height) {
        // do nothing
    }

    @Override
    public void draw(GL10 gl) {
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, DRAW_INDEX.length, GLES20.GL_UNSIGNED_SHORT, mDrawIndexBuffer);
    }
}
