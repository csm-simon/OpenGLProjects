package com.demo.yujieliu.agorademo;

import android.opengl.EGL14;
import android.opengl.GLES20;
import android.os.Looper;
import android.view.Surface;

import com.demo.yujieliu.agorademo.egl.EGLCore;
import com.demo.yujieliu.agorademo.egl.EGLSurfaceConfig;
import com.demo.yujieliu.agorademo.gl.GLProgram;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * 离屏渲染器
 * <p>
 * Created by yujieliu on 2018/6/15.
 */
public class OffScreenRenderer {
    private GLProgram mGLProgram;
    private EGLCore mEGLCore;
    private final String mVertexShader, mFragmentShader;
    private int mSurfaceWidth, mSurfaceHeight;
    private Object mSavedEGLContext, mSavedEGLReadSurface, mSavedEGLDrawSurface, mSavedEGLDisplay;
    private OnDrawFrameCallback mOnDrawFrameCallback;
    private Surface mSurface;

    public OffScreenRenderer(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    /**
     * 初始化，创建离屏渲染所需的东西，必须在GL线程调用
     *
     * @param textureId 纹理Id
     * @param surface 离屏渲染用的Surface
     * @param surfaceWidth Surface宽
     * @param surfaceHeight Surface高
     */
    public void init(int textureId, final Surface surface, int surfaceWidth, int surfaceHeight) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("This method cannot be called from main thread!");
        }
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
        initGLEnvironment(surface, surfaceWidth, surfaceHeight);
        initGLProgram(textureId, surfaceWidth, surfaceHeight);
    }

    private void initGLEnvironment(final Surface surface, int surfaceWidth, int surfaceHeight) {
        mSurface = surface;
        mEGLCore = EGLCore.createEGL(getCurrentEGLContext(), EGLCore.FLAG_RECORDABLE);
        mEGLCore.createSurface(new EGLSurfaceConfig() {
            @Override
            public int chooseSurfaceType() {
                return SurfaceType.WINDOW_SURFACE;
            }

            @Override
            public Object getNativeWindowForWindowSurface() {
                return mSurface;
            }
        });
    }

    private void initGLProgram(int textureId, int surfaceWidth, int surfaceHeight) {
        mGLProgram = new GLProgram(mVertexShader, mFragmentShader);
        mGLProgram.init();
        mGLProgram.onSurfaceChanged(surfaceWidth, surfaceHeight);
        mGLProgram.setTextureId(textureId);
        float[] fullWindowVertices = {
                0, 0, 0, 1, // 0, 0
                surfaceWidth, 0, 1, 1, // 1, 0
                0, surfaceHeight, 0, 0, // 0, 1
                surfaceWidth, surfaceHeight, 1, 0 // 1, 1
        };
        mGLProgram.putVertices(fullWindowVertices);

        GLES20.glClearColor(1f, 1f, 1f, 1f);
    }

    /**
     * 绘制接口
     */
    public void drawFrame() {
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mGLProgram.use();
        mGLProgram.draw();
        mEGLCore.swapBuffers();
        if (mOnDrawFrameCallback != null) {
            mOnDrawFrameCallback.onFrameDrawn();
        }
    }

    public void saveEGLState() {
        mSavedEGLContext = mEGLCore.eglGetCurrentContext();
        mSavedEGLDisplay = mEGLCore.eglGetCurrentDisplay();
        mSavedEGLReadSurface = mEGLCore.eglGetCurrentReadSurface();
        mSavedEGLDrawSurface = mEGLCore.eglGetCurrentDrawSurface();
    }

    public void makeCurrent() {
        mEGLCore.makeCurrent();
    }

    public void restoreEGLState() {
        //noinspection unchecked
        mEGLCore.restoreState(mSavedEGLContext, mSavedEGLReadSurface, mSavedEGLDrawSurface, mSavedEGLDisplay);
    }

    /**
     * 释放接口，一定要在GL线程里执行
     */
    public void release() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("This method cannot be called from main thread!");
        }
        if (mGLProgram != null) {
            mGLProgram.destroy();
        }
        if (mEGLCore != null) {
            mEGLCore.release();
        }
    }

    private Object getCurrentEGLContext() {
        if (EGLCore.SUPPORT_EGL14) {
            return EGL14.eglGetCurrentContext();
        } else {
            return ((EGL10)EGLContext.getEGL()).eglGetCurrentContext();
        }
    }

    public void setOnDrawFrameCallback(OnDrawFrameCallback onDrawFrameCallback) {
        mOnDrawFrameCallback = onDrawFrameCallback;
    }

    public interface OnDrawFrameCallback {
        void onFrameDrawn();
    }
}
