package com.demo.lauyukit.opengldemo.program;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.demo.lauyukit.opengldemo.utils.GlUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 绘制风车的Program
 * <p>
 * Created by LiuYujie on 2018/1/23.
 */
public class DrawFanProgram implements DrawProgram {
    private static final float[] VERTEX;

    static {
        List<Float> vertices = new ArrayList<>();
        vertices.add(0f);
        vertices.add(0f);
        vertices.add(0f);
        final float radius = 0.25f;
        final float deltaDegree = 360f / 20f;
        final float endDegree = deltaDegree + 360f;
        double radian;
        for (float i = 0; i < endDegree; i += deltaDegree) {
            radian = Math.toRadians(i);
            vertices.add((float)(radius * Math.sin(radian)));
            vertices.add((float)(radius * Math.cos(radian)));
            vertices.add(0f);
        }
        VERTEX = new float[vertices.size()];
        for (int i = vertices.size() - 1; i >= 0; i--) {
            VERTEX[i] = vertices.get(i);
        }
    }

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "void main() {\n" +
                    "gl_Position = uMVPMatrix * aPosition;\n" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "void main() {\n" +
                    "gl_FragColor = vec4(1, 1, 0, 1);\n" +
                    "}";

    private final FloatBuffer mVertexBuffer;
    private final float[] mMVPMatrix = new float[16];
    private int mMVPMatrixLoc;

    public DrawFanProgram() {
        mVertexBuffer = GlUtils.createFloatBuffer(VERTEX);
    }

    @Override
    public void createProgram(GL10 gl, EGLConfig config) {
        int programHandle = GlUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        GLES20.glUseProgram(programHandle);
        int positionLoc = GLES20.glGetAttribLocation(programHandle, "aPosition");
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(positionLoc, 3, GLES20.GL_FLOAT, false, 3 * SIZEOF_FLOAT, mVertexBuffer);

        mMVPMatrixLoc = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
    }

    @Override
    public void onSizeChanged(GL10 gl, int width, int height) {
        Matrix.perspectiveM(mMVPMatrix, 0, 45f, width / (float) height, .1f, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0, 0, -3f);
    }

    @Override
    public void draw(GL10 gl) {
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX.length / 3);
    }
}
