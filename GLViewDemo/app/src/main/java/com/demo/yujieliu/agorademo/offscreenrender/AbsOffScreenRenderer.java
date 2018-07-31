package com.demo.yujieliu.agorademo.offscreenrender;

import android.opengl.EGL14;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;

import com.demo.yujieliu.agorademo.egl.EGLCore;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * 离屏渲染Renderer基类
 * <p>
 * Created by yujieliu on 2018/7/31.
 */
public abstract class AbsOffScreenRenderer {
    protected final String mVertexShader, mFragmentShader;
    protected OnDrawFrameCallback mOnDrawFrameCallback;

    public AbsOffScreenRenderer(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    /**
     * 初始化方法，要在GL线程调用
     *
     * @param textureId     绘制纹理的ID
     * @param nativeWindow  Surface/SurfaceTexture/SurfaceHolder的实例，用于创建EGLSurface。用FrameBuffer
     *                      做离屏渲染的话该值传null即可。
     * @param surfaceWidth  创建的离屏渲染纹理的宽度
     * @param surfaceHeight 创建的离屏渲染纹理的高度
     */
    @CallSuper
    public void init(int textureId, @Nullable Object nativeWindow, int surfaceWidth, int surfaceHeight) {
        if (!isGLThread()) {
            throw new IllegalThreadStateException("Init method must be called from a thread that contains EGLContext!");
        }
        initOffScreenRenderEnvironment(nativeWindow, surfaceWidth, surfaceHeight);
        initShaderProgram(textureId, surfaceWidth, surfaceHeight, mVertexShader, mFragmentShader);
    }

    /**
     * 初始化离屏渲染环境。如果是用FrameBuffer做离屏渲染的话，只需要创建FrameBuffer即可；如果是用EGLSurface做离屏渲染的话，
     * 需要创建共享EGLContext。
     *
     * @param nativeWindow  Surface/SurfaceTexture/SurfaceHolder的实例，用于创建EGLSurface。用FrameBuffer
     *                      做离屏渲染的话该值传null即可。
     * @param surfaceWidth  创建的离屏渲染纹理的宽度
     * @param surfaceHeight 创建的离屏渲染纹理的高度
     */
    protected abstract void initOffScreenRenderEnvironment(@Nullable Object nativeWindow, int surfaceWidth, int surfaceHeight);

    /**
     * 初始化Shader Program
     *
     * @param textureId      离屏渲染的纹理id，纹理的内容就是离屏渲染的内容
     * @param surfaceWidth   创建的离屏渲染纹理的宽度
     * @param surfaceHeight  创建的离屏渲染纹理的高度
     * @param vertexShader   Vertex Shader
     * @param fragmentShader Fragment Shader
     */
    protected abstract void initShaderProgram(int textureId, int surfaceWidth, int surfaceHeight,
                                              String vertexShader, String fragmentShader);

    public abstract void onSurfaceChanged(int surfaceWidth, int surfaceHeight);

    public abstract void drawFrame();

    /**
     * @return 当前线程是否拥有EGLContext
     */
    protected boolean isGLThread() {
        if (EGLCore.SUPPORT_EGL14) {
            return EGL14.eglGetCurrentContext() != EGL14.EGL_NO_CONTEXT;
        } else {
            return ((EGL10) EGLContext.getEGL()).eglGetCurrentContext() != EGL10.EGL_NO_CONTEXT;
        }
    }

    public abstract void release();

    public void setOnDrawFrameCallback(OnDrawFrameCallback onDrawFrameCallback) {
        mOnDrawFrameCallback = onDrawFrameCallback;
    }

    public interface OnDrawFrameCallback {
        void onFrameDrawn();
    }
}
