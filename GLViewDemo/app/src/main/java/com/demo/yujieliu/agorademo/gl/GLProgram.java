package com.demo.yujieliu.agorademo.gl;

import android.graphics.Bitmap;
import android.opengl.Matrix;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * OpenGL Shader Program封装，在GL线程调用。非常简陋，临时用用，这个设计跟Vertex Shader和Fragment Shader是强相关
 * 关系，不通用。
 *
 * Created by yujieliu on 2018/5/16.
 */
public class GLProgram {
    /**
     * 表示没有VBO的数值
     */
    private static final int NO_VBO = -1;
    /**
     * 没有Texture
     */
    private static final int NO_TEXTURE = OpenGLUtils.NO_TEXTURE;
    /**
     * Shader Program 句柄
     */
    private int mProgramHandle;
    /**
     * 水印纹理ID
     */
    private int mTextureId = NO_TEXTURE;
    /**
     * 顶点坐标数组
     */
    private FloatBuffer mVertexBuffer;
    /**
     * Model View Projection 矩阵
     */
    private float[] mMVPMatrix = new float[16];
    /**
     * Vertex Shader中position attribute句柄
     */
    private int mAttribPositionLoc;
    /**
     * Vertex Shader中inputTextureCoordinate attribute句柄
     */
    private int mAttribTextureCoordinateLoc;
    /**
     * Fragment Shader中纹理的句柄
     */
    private int mUniformTextureLoc;
    /**
     * Vertex Shader中MVP矩阵的句柄
     */
    private int mUniformMVPMatrixLoc;

    private int mVBO = NO_VBO;

    private String mVertexShader, mFragmentShader;


    public GLProgram(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    public void init() {
        if (mProgramHandle != 0) {
            return;
        }
        mProgramHandle = OpenGLUtils.loadProgram(mVertexShader, mFragmentShader);
        // 获取这些属性的句柄
        mAttribPositionLoc = glGetAttribLocation(mProgramHandle, "position");
        mAttribTextureCoordinateLoc = glGetAttribLocation(mProgramHandle, "inputTextureCoordinate");
        mUniformTextureLoc = glGetUniformLocation(mProgramHandle, "inputImageTexture");
        mUniformMVPMatrixLoc = glGetUniformLocation(mProgramHandle, "mvpMatrix");
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    public void onSurfaceChanged(int w, int h) {
        // 获取绘制窗口宽高后，我们就可以创建MVP矩阵，这里采用了正射投影矩阵，有了这个矩阵，我们使用宽为[0, w]，高为[0, h]
        // 的顶点坐标，最后可以在Vertex Shader里，通过把坐标跟MVP矩阵相乘，把顶点坐标映射到[-1, 1]的范围内
        Matrix.orthoM(mMVPMatrix, 0, 0, w, 0, h, -1.0f, 1.0f);
    }

    public void use() {
        // 启用Shader Program
        glUseProgram(mProgramHandle);
        glBindBuffer(GL_ARRAY_BUFFER, mVBO);
        // 启用所有属性并传值
        glEnableVertexAttribArray(mAttribPositionLoc);
        // vertexBuffer每个顶点有4个component, 前两个是顶点坐标，后两个是纹理坐标，都是float
        final int stride = 4 * 4;
        glVertexAttribPointer(mAttribPositionLoc, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(mAttribTextureCoordinateLoc);
        // 纹理坐标在顶点坐标之后，而顶点坐标是二维的，所以offset = sizeof(float) * 2 = 4 * 2 = 8
        glVertexAttribPointer(mAttribTextureCoordinateLoc, 2, GL_FLOAT, false, stride, 8);

        // 激活第0个纹理
        glActiveTexture(GL_TEXTURE0);
        // 绑定纹理
        glBindTexture(GL_TEXTURE_2D, mTextureId);
        // 告诉Fragment Shader，用TEXTURE_2D的纹理时，就用第0个纹理，也就是mTextureId对应的纹理
        glUniform1i(mUniformTextureLoc, 0);
        // MVP矩阵传值
        glUniformMatrix4fv(mUniformMVPMatrixLoc, 1, false, mMVPMatrix, 0);
    }

    public void draw() {
        // 第0， 1， 2个顶点画一个三角形，第1，2，3个顶点画一个三角形
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        // Vertex Buffer 解绑
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        // 纹理解绑
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
    }

    public void putVertices(float[] vertices) {
        if (mVBO == NO_VBO) {
            // 生成VBO
            int[] vbo = new int[1];
            glGenBuffers(1, vbo, 0);
            mVBO = vbo[0];
        }
        glBindBuffer(GL_ARRAY_BUFFER, mVBO);
        mVertexBuffer = OpenGLUtils.newFloatBuffer(vertices);
        glBufferData(GL_ARRAY_BUFFER, mVertexBuffer.capacity() * 4, mVertexBuffer, GL_STATIC_DRAW);
    }

    public void loadTexture(Bitmap bitmap) {
        mTextureId = OpenGLUtils.loadTexture(bitmap, mTextureId, false);
    }

    public int getTextureId() {
        return mTextureId;
    }

    public void setTextureId(int texId) {
        mTextureId = texId;
    }

    /**
     * 释放纹理等资源，要在GL线程调用
     */
    public void destroy() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
        }
        if (mVBO == NO_VBO) {
            glDeleteBuffers(1, new int[] {mVBO}, 0);
            mVBO = NO_VBO;
        }
        if (mTextureId != OpenGLUtils.NO_TEXTURE) {
            glDeleteTextures(1, new int[]{mTextureId}, 0);
            mTextureId = OpenGLUtils.NO_TEXTURE;
        }
        if (mProgramHandle != 0) {
            glDeleteProgram(mProgramHandle);
            mProgramHandle = 0;
        }
    }

}
