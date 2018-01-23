package com.meitu.beautiful.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.meitu.base.base.BaseGLSurfaceView;
import com.meitu.beautiful.filter.BaseFilter;
import com.meitu.beautiful.filter.BeautifulFilter;
import com.meitu.beautiful.helper.CameraHelper;
import com.meitu.beautiful.matrix.MatrixC;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * <p/>
 * Created by 周代亮 on 2018/1/14.
 */

public class CameraGLSurfaceView extends BaseGLSurfaceView implements
        GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    /**
     * 获取相机预览纹理
     */
    private SurfaceTexture mSurfaceTexture;

    /**
     * 对应相机的纹理id
     */
    private int mTextureId = -1;

    /**
     * 基本滤镜
     */
    private BaseFilter mBaseFilter;

    /**
     * 绘制表面的宽度
     */
    private int mSurfaceWidth;

    /**
     * 绘制表面的高度
     */
    private int mSurfaceHeight;

    /**
     * 美颜处理
     */
    private BeautifulFilter mBeautifulFilter;

    /**
     * 顶点数据
     */
    private FloatBuffer mVertexBuffer;

    /**
     * 纹理
     */
    private FloatBuffer mTexBuffer;

    public CameraGLSurfaceView(Context context) {
        super(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();

        mVertexBuffer = ByteBuffer.allocateDirect(MatrixC.VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(MatrixC.VERTEX);
        mVertexBuffer.position(0);

        mTexBuffer = ByteBuffer.allocateDirect(MatrixC.TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(MatrixC.TEXTURE_90);
        mTexBuffer.position(0);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GLES20.GL_DITHER);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // 初始化美颜滤镜
        mBeautifulFilter = new BeautifulFilter(getContext());
        mBeautifulFilter.init();

        // 生成纹理id传入到SurfaceTexture中
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mTextureId = textures[0];
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mSurfaceWidth = width;
        mSurfaceHeight = height;

        // 将宽高回调到BeautifulFilter中
        mBeautifulFilter.onSizeChanged(mSurfaceWidth, mSurfaceHeight);

        // 打开前置相机
        CameraHelper.getInstance().startCamera(mSurfaceTexture);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 从图片流中拿取最新的一帧到mTextureId对应的纹理中
        mSurfaceTexture.updateTexImage();

        float[] mtx = new float[16];
        // 获取SurfaceTexture的转化矩阵
        mSurfaceTexture.getTransformMatrix(mtx);
        mBeautifulFilter.setTextureTransformMatrix(mtx);

        if (mBaseFilter == null) {
            // 如果没有其他滤镜，就直接将当前帧绘制到屏幕
            mBeautifulFilter.onDrawFrame(mTextureId, mVertexBuffer, mTexBuffer);
        } else {
            // 如果有其他滤镜就先将经过美颜滤镜的纹理传入FBO，等待其他滤镜的处理
            int id = mBeautifulFilter.onDrawToTexture(mTextureId, mVertexBuffer, mTexBuffer);
            FloatBuffer texBuffer = ByteBuffer.allocateDirect(MatrixC.TEXTURE.length * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer().put(MatrixC.TEXTURE);
            texBuffer.position(0);
            mBaseFilter.onDrawFrame(id, mVertexBuffer, texBuffer);
        }
    }

    /**
     * 设置基本滤镜
     *
     * @param baseFilter 基本滤镜
     */
    public void setFilter(final BaseFilter baseFilter) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                // 之前有滤镜就先释放之前的滤镜
                if (mBaseFilter != null) {
                    mBaseFilter.destroy();
                }
                mBaseFilter = baseFilter;
                if (mBaseFilter != null) {
                    mBaseFilter.init();
                    mBaseFilter.onSizeChanged(mSurfaceWidth, mSurfaceHeight);
                    mBeautifulFilter.initCameraFrameBuffer(mSurfaceWidth, mSurfaceHeight);
                }
            }
        });
        requestRender();
    }

    /**
     * 设置美颜的级别
     *
     * @param beautifulLevel 美颜级别（0 ~ 5）
     */
    public void setBeautifulLevel(final int beautifulLevel) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mBeautifulFilter.setBeautyLevel(beautifulLevel);
            }
        });
        requestRender();
    }

    /**
     * SurfaceTexture 的可用帧回调
     *
     * @param surfaceTexture 对应的surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    /**
     * 释放资源
     */
    public void destroy() {
        CameraHelper.getInstance().releaseCamera();
        GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        mBeautifulFilter.destroy();
        if (mBaseFilter != null) {
            mBaseFilter.destroy();
        }
    }
}
