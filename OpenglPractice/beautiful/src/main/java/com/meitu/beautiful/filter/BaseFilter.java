package com.meitu.beautiful.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.support.annotation.CallSuper;


import com.meitu.base.base.BaseProgram;
import com.meitu.base.util.GLSLUtil;
import com.meitu.beautiful.matrix.MatrixC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Filter基类
 * <p/>
 * Created by 周代亮 on 2018/1/14.
 */

public abstract class BaseFilter extends BaseProgram {

    /**
     * 上下文环境
     */
    private Context mContext;
    /**
     * 顶点GLSL文件的raw id
     */
    private int mVertexRawId;
    /**
     * 片段GLSL文件的raw id
     */
    private int mFragmentRawId;
    /**
     * program
     */
    protected int mProgram;
    /**
     * 位置句柄
     */
    protected int mPositionHandle;
    /**
     * 纹理句柄
     */
    protected int mTextureHandle;
    /**
     * 要绘制表面的宽度
     */
    protected int mSurfaceWidth;
    /**
     * 要绘制表面的高度
     */
    protected int mSurfaceHeight;
    /**
     * 顶点索引的缓存
     */
    private ShortBuffer mIndexBuffer;
    /**
     * 是否调用了init()初始化方法
     */
    private boolean mInitialization = false;

    /**
     * 构造方法
     *
     * @param context 需要上下文去读取资源文件
     */
    public BaseFilter(Context context, int vertexRawId, int fragmentRawId) {
        mContext = context;
        mVertexRawId = vertexRawId;
        mFragmentRawId = fragmentRawId;

        mIndexBuffer = ByteBuffer.allocateDirect(MatrixC.INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(MatrixC.INDEX);
        mIndexBuffer.position(0);
    }

    /**
     * 初始化方法
     */
    @CallSuper
    public void init() {
        mInitialization = true;

        // 生成纹理
        String vertexStr = GLSLUtil.read(mContext, mVertexRawId);
        String fragmentStr = GLSLUtil.read(mContext, mFragmentRawId);
        mProgram = genProgram(vertexStr, fragmentStr);

        // 获取着色器上对应属性的句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
    }

    /**
     * 绘制textureId数据到surface
     *
     * @param textureId    纹理id
     * @param vertexBuffer 顶点数据的buffer
     * @param texBuffer    纹理数据的buffer
     */
    public void onDrawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer texBuffer) {
        if (!mInitialization) {
            throw new IllegalStateException(getClass().getSimpleName() + " 还未初始化！");
        }
        // 使用
        GLES20.glUseProgram(mProgram);

        // 根据句柄给对应的属性赋值
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        texBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
        GLES20.glEnableVertexAttribArray(mTextureHandle);

        // 绘制之前回调
        onPreDrawFrame();

        // 绘制
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        setTexParamters(GLES20.GL_TEXTURE_2D);
        draw();

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
    }

    /**
     * 在绘制之前的回调
     */
    protected void onPreDrawFrame() {

    }

    /**
     * 绘制当前元素
     */
    protected void draw() {
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, MatrixC.INDEX.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

    /**
     * GLSurfaceView 有变化的时候回调到这里
     *
     * @param surfaceWidth  GLSurfaceView的宽度
     * @param surfaceHeight GLSurfaceView的高度
     */
    public void onSizeChanged(int surfaceWidth, int surfaceHeight) {
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
    }

    /**
     * 设置纹理扩展参数
     *
     * @param target 对应的目标纹理
     */
    public void setTexParamters(int target) {
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    /**
     * 释放数据
     */
    public void destroy() {
        GLES20.glDeleteProgram(mProgram);
    }

}
