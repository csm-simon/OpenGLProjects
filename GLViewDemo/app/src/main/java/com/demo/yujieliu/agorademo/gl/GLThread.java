package com.demo.yujieliu.agorademo.gl;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;

import com.demo.yujieliu.agorademo.Dog;
import com.demo.yujieliu.agorademo.egl.EGLCore;
import com.demo.yujieliu.agorademo.egl.EGLSurfaceConfig;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GL渲染线程。需要在该线程执行操作的话就调用{@link #queueEvent(Runnable)}
 * <p>
 * Created by yujieliu on 2018/5/16.
 */
public class GLThread extends Thread {
    private static final String TAG = "GLThread";
    /**
     * 标记当前线程是否GG
     */
    private final AtomicBoolean mAlive;
    /**
     * 配合{@link #mRenderWhenDirty}使用，在绘制完一帧后会调用wait等待下一次{@link #requestRender()}
     */
    private final Object mRenderLock = new Object();
    /**
     * 使用的EGL Context Client Version, 2或3
     */
    private final int mEGLContextClientVersion;
    /**
     * 在每个绘制循环里，绘制前执行的Runnable队列
     */
    private final Queue<Runnable> mRunOnDrawQueue;

    /**
     * 绘制流程回调
     */
    private RenderCallback mRenderCallback;

    private EGLCore mEGLCore;

    private SurfaceTexture mSurfaceTexture;
    /**
     * 是否在请求绘制时才调用重新绘制
     *
     * @see #requestRender()
     */
    private boolean mRenderWhenDirty;

    /**
     * 你懂的
     *
     * @param surfaceTexture    SurfaceTexture, 创建用于显示屏幕上的EGLSurface所用的参数之一。
     * @param eglContextVersion EGLContext client version, 2或3, 分别表示OpenGL ES 2.x或者OpenGL ES 3.x
     */
    public GLThread(SurfaceTexture surfaceTexture, int eglContextVersion) {
        super();
        mAlive = new AtomicBoolean(true);
        mSurfaceTexture = surfaceTexture;
        mEGLContextClientVersion = eglContextVersion;
        mRunOnDrawQueue = new ArrayDeque<>();
    }

    private void initGLEnvironment() {
        int flag = mEGLContextClientVersion >= 3 ? EGLCore.FLAG_PREFER_GLES3 : 0;
        flag |= EGLCore.FLAG_RECORDABLE;
        mEGLCore = EGLCore.createEGL(null, flag);

        mEGLCore.createSurface(new EGLSurfaceConfig() {
            @Override
            public int chooseSurfaceType() {
                return SurfaceType.WINDOW_SURFACE;
            }

            @Override
            public Object getNativeWindowForWindowSurface() {
                return mSurfaceTexture;
            }
        });

        mEGLCore.makeCurrent();
    }

    /**
     * 查询EGLConfig的EGL_RENDERABLE_TYPE属性值。
     *
     * @param dpy    EGLDisplay
     * @param config EGLConfig
     */
    private void queryEGLConfig(EGLDisplay dpy, EGLConfig config) {
        int[] val = new int[1];
        EGL14.eglGetConfigAttrib(dpy, config, EGL14.EGL_RENDERABLE_TYPE, val, 0);
        Dog.i(TAG, "queryEGLConfig renderable type: " + val[0]);
    }

    private void throwError(String prefix) {
        throw new RuntimeException(prefix + GLUtils.getEGLErrorString(EGL14.eglGetError()));
    }

    @Override
    public void run() {
        super.run();
        // 初始化GL环境
        initGLEnvironment();
        // 通知外部可以初始化GL Shader
        if (null != mRenderCallback) {
            mRenderCallback.onGLResourceInitialized();
        }
        // 绘制循环
        while (mAlive.get()) {
            // 绘制前执行外部的Runnable
            synchronized (mRunOnDrawQueue) {
                while (!mRunOnDrawQueue.isEmpty()) {
                    mRunOnDrawQueue.poll().run();
                }
            }
            // 通知外部绘制
            if (null != mRenderCallback) {
                mRenderCallback.onDrawFrame();
            }
            // 将EGLSurface的FrameBuffer呈交到屏幕上
            mEGLCore.swapBuffers();
            if (mRenderWhenDirty) { // 如果是RenderWhenDirty8的话，就等待外部调用requestRender()
                synchronized (mRenderLock) {
                    try {
                        mRenderLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        releaseInner();
    }

    /**
     * 外部释放接口
     */
    public void release() {
        mAlive.set(false);
        notifyRenderLock();
    }

    private void releaseInner() {
        if (null != mRenderCallback) {
            mRenderCallback.onGLResourceRelease();
        }
        destroyGLEnvironment();
        mSurfaceTexture = null;
        Dog.d(TAG, "release GLThread finished");
    }

    /**
     * 释放EGL资源
     */
    private void destroyGLEnvironment() {
        mEGLCore.release();
        mSurfaceTexture = null;
    }

    /**
     * 释放RenderLock,执行下一个绘制操作
     */
    public void requestRender() {
        notifyRenderLock();
    }

    /**
     * 在下一次绘制之前执行指定操作
     *
     * @param r Runnable
     */
    public void queueEvent(Runnable r) {
        synchronized (mRunOnDrawQueue) {
            mRunOnDrawQueue.add(r);
        }
    }

    /**
     * notify释放 mRenderLock
     */
    private void notifyRenderLock() {
        synchronized (mRenderLock) {
            mRenderLock.notifyAll();
        }
    }

    public void setRenderCallback(RenderCallback renderCallback) {
        mRenderCallback = renderCallback;
    }

    /**
     * 设置绘制模式是否为Render_When_Dirty, 类似于{@link android.opengl.GLSurfaceView#RENDERMODE_WHEN_DIRTY}
     *
     * @param renderWhenDirty 是否等外部调用{@link #requestRender()}才绘制下一帧
     */
    public void setRenderWhenDirty(boolean renderWhenDirty) {
        mRenderWhenDirty = renderWhenDirty;
    }

    /**
     * 渲染流程回调，这三个回调都会在GL线程中执行。
     */
    public interface RenderCallback {
        /**
         * GL环境初始化成功的回调
         */
        void onGLResourceInitialized();

        /**
         * 绘制一帧的callback，可以在这里执行Shader program的绘制
         */
        void onDrawFrame();

        /**
         * GL环境释放前的回调，Shader program应该在此回调中释放资源
         */
        void onGLResourceRelease();
    }
}
