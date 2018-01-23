package com.meitu.beautiful.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.meitu.beautiful.R;

import java.nio.FloatBuffer;

/**
 * 美颜滤镜
 * <p/>
 * Created by 周代亮 on 2018/1/15.
 */

public class BeautifulFilter extends BaseFilter {
    /**
     * SurfaceTexture对应的变换矩阵
     */
    private float[] mTextureTransformMatrix;
    /**
     * 对应变换矩阵的句柄
     */
    private int mTextureTransformMatrixLocation;
    /**
     * 但不偏移量的句柄
     */
    private int mSingleStepOffsetLocation;
    /**
     * 美颜参数对应的句柄
     */
    private int mParamsLocation;
    /**
     * 输入图片纹理的句柄
     */
    private int mInputImageTextureLocation;
    /**
     * 帧缓存
     */
    private int[] mFrameBuffers = null;
    /**
     * 帧缓存对应的纹理id
     */
    private int[] mFrameBufferTextures = null;

    public BeautifulFilter(Context context) {
        super(context, R.raw.beautiful_vertex, R.raw.beautiful_fragment);
    }

    @Override
    public void init() {
        super.init();
        mTextureTransformMatrixLocation = GLES20.glGetUniformLocation(mProgram, "textureTransform");
        mInputImageTextureLocation = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(mProgram, "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(mProgram, "params");
    }

    @Override
    public void onDrawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer texBuffer) {
        GLES20.glUseProgram(mProgram);

        // 根据句柄给对应的属性赋值
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        texBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
        GLES20.glEnableVertexAttribArray(mTextureHandle);

        setTexelSize(mSurfaceWidth, mSurfaceHeight);
        GLES20.glUniform1i(mInputImageTextureLocation, 0);
        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        draw();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    public int onDrawToTexture(int textureId, FloatBuffer vertexBuffer, FloatBuffer texBuffer) {
        GLES20.glUseProgram(mProgram);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        texBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

        setTexelSize(mSurfaceWidth, mSurfaceHeight);
        GLES20.glUniform1i(mInputImageTextureLocation, 0);

        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        }

        draw();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }

    public void setTextureTransformMatrix(float[] mtx) {
        mTextureTransformMatrix = mtx;
    }

    /**
     * 设置美颜的等级
     *
     * @param beautyLevel 美颜等级0~5，其中0是无美颜，1~5数字越大美颜程度越高
     */
    public void setBeautyLevel(int beautyLevel) {
        switch (beautyLevel) {
            case 1:
                GLES20.glUniform1f(mParamsLocation, 2.0f);
                break;
            case 2:
                GLES20.glUniform1f(mParamsLocation, 1.5f);
                break;
            case 3:
                GLES20.glUniform1f(mParamsLocation, 1f);
                break;
            case 4:
                GLES20.glUniform1f(mParamsLocation, 0.5f);
                break;
            case 5:
                GLES20.glUniform1f(mParamsLocation, 0.3f);
                break;
            default:
                GLES20.glUniform1f(mParamsLocation, 0.0f);
                break;
        }
    }

    /**
     * 初始化美颜的FBO
     *
     * @param width  显示区域的宽
     * @param height 显示区域的高
     */
    public void initCameraFrameBuffer(int width, int height) {
        if (mFrameBuffers != null) {
            if (mFrameBufferTextures != null) {
                GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
                mFrameBufferTextures = null;
            }
            if (mFrameBuffers != null) {
                GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
                mFrameBuffers = null;
            }
        }
        if (mFrameBuffers == null) {
            mFrameBuffers = new int[1];
            mFrameBufferTextures = new int[1];
            // 创建FBO
            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
            GLES20.glGenTextures(1, mFrameBufferTextures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            setTexParamters(GLES20.GL_TEXTURE_2D);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    /**
     * 设置单步的偏移量
     *
     * @param w surfaceView的宽
     * @param h surfaceView的高
     */
    private void setTexelSize(final float w, final float h) {
        GLES20.glUniform2fv(mSingleStepOffsetLocation, 1,
                FloatBuffer.wrap(new float[]{2.0f / w, 2.0f / h}));
    }
}
