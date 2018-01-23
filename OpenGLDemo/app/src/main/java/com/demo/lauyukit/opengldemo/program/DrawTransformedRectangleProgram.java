package com.demo.lauyukit.opengldemo.program;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.demo.lauyukit.opengldemo.utils.GLog;
import com.demo.lauyukit.opengldemo.utils.GlUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 讲解MVPMatrix用的GL绘制程序
 * <p>
 * Created by LiuYujie on 2018/1/17.
 */
public class DrawTransformedRectangleProgram implements DrawProgram {

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "void main() { \n" +
                    "gl_Position = uMVPMatrix * aPosition;\n}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "void main() {\n" +
                    "gl_FragColor = vec4(1, 0, 0, 1);\n}";

    private static final float[] VERTEX = {
            -.25f, .25f, // top left
            .25f, .25f, // top right
            -.25f, -.25f,// bottom left
            .25f, -.25f // bottom right
    };

    private int mMVPMatrixLoc;
    private final float[] mMVPMatrix = new float[16];
    private long mInitialRotateTime;

    private final FloatBuffer mVertexBuffer;

    public DrawTransformedRectangleProgram() {
        mVertexBuffer = GlUtils.createFloatBuffer(VERTEX);
    }

    @Override
    public void createProgram(GL10 gl, EGLConfig config) {
        int programHandle = GlUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        GLES20.glUseProgram(programHandle);

        int positionLoc = GLES20.glGetAttribLocation(programHandle, "aPosition");
        GLES20.glEnableVertexAttribArray(positionLoc);
        GLES20.glVertexAttribPointer(positionLoc, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);

        mMVPMatrixLoc = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
    }

    @Override
    public void onSizeChanged(GL10 gl, int width, int height) {
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.rotateM(mMVPMatrix, 0, 0f, 0f, 0f, 1f);
        Matrix.scaleM(mMVPMatrix, 0, 1.5f, 1f, 1f);
        Matrix.translateM(mMVPMatrix, 0, .2f, 0f, 0f);
//        Matrix.perspectiveM(mMVPMatrix, 0, 45, width / (float) height, .1f, 100f);
//        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -5f);
//        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f);
//        Matrix.scaleM(mMVPMatrix, 0, 1f, 1f, 1f);
    }

    private float getRotateAngle() {
        if (mInitialRotateTime == 0L) {
            mInitialRotateTime = System.currentTimeMillis();
            return 0f;
        }
        long deltaTime = System.currentTimeMillis() - mInitialRotateTime;
        GLog.d("Test", Long.toString(deltaTime));
        return deltaTime * .001f;
    }

    @Override
    public void draw(GL10 gl) {
        // 旋转，xyz参数组成一个[x,y,z]向量，图形会围绕这个向量旋转，在这里是围绕Z轴旋转。
//        Matrix.rotateM(mMVPMatrix, 0, getRotateAngle(), 0f, 0f, 1f);
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
