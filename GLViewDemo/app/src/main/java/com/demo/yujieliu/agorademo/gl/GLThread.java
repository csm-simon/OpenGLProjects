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

    private SurfaceTexture mSurfaceTexture;
    private EGLContext mEGLContext;
    private EGLDisplay mEGLDisplay;
    private EGLSurface mEGLSurface;
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
        // 获取EGLDisplay
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (EGL14.EGL_NO_DISPLAY == mEGLDisplay) {
            throwError("Get EGLDisplay failed: ");
        }
        int[] eglImplVersion = new int[2];
        // 初始化EGLDisplay,并获取EGL实现的最大版本号和最小版本号
        if (!EGL14.eglInitialize(mEGLDisplay, eglImplVersion, 0, eglImplVersion, 1)) {
            throwError("Initialize EGLDisplay failed: ");
        }
        // 根据用到的OpenGLES版本，选择对应的EGL_RENDERABLE_TYPE的值，使得后续创建的EGLSurface中的FrameBuffer满足
        // 我们所需的OpenGL ES版本的要求。
        int eglVersionBit = mEGLContextClientVersion == 2 ? EGL14.EGL_OPENGL_ES2_BIT : EGLExt.EGL_OPENGL_ES3_BIT_KHR;
        Dog.d(TAG, "EGL Context Client Version Bit: " + eglVersionBit + ", mEGLContextClientVersion: " + mEGLContextClientVersion);
        // EGLConfig的属性值，这里指定了RGBA分别占8bits, 深度为0，surface类型为window(显示用的surface必须为window)
        int[] configAttribs = {
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 0,
                EGL14.EGL_RENDERABLE_TYPE, eglVersionBit,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_NONE
        };
        // 存储EGLConfig数目的数组
        int[] numConfigs = new int[1];
        // 存储获取到的EGLConfig的数组
        EGLConfig[] configs = new EGLConfig[1];
        // 将我们需要的EGLConfig属性列表传给EGLDisplay，让系统选择最接近我们需求的EGLConfig,存到configs里返回给我们。
        // 符合的EGLConfig数量会保存在numConfigs中。
        if (!EGL14.eglChooseConfig(mEGLDisplay, configAttribs, 0, configs,
                0, 1, numConfigs, 0)) {
            throwError("Choose EGLConfig failed: ");
        }
        queryEGLConfig(mEGLDisplay, configs[0]);
        // EGLContext创建的属性，只需指定使用的EGLContext Client Version版本。
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
                EGL14.EGL_NONE
        };
        // 用EGLDisplay, EGLConfig和EGLContext的父Context以及属性列表创建一个EGLContext.如果没有父Context,
        // 传EGL_NO_CONTEXT即可。
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
        // 指定EGLSurface的RenderBuffer为BACK_BUFFER,如果是离屏渲染的话，就用EGL_SINGLE_BUFFER
        int[] surfaceAttribs = {
                EGL14.EGL_RENDER_BUFFER, EGL14.EGL_BACK_BUFFER, EGL14.EGL_NONE
        };
        // 创建EGLSurface，由于要显示到屏幕上，所以要创建WindowSurface，要用到SurfaceTexture
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurfaceTexture, surfaceAttribs, 0);
        // 检查是否创建EGLSurface或者EGLContext失败
        if (EGL14.EGL_NO_SURFACE == mEGLSurface || EGL14.EGL_NO_CONTEXT == mEGLContext) {
            int error = EGL14.eglGetError();
            if (EGL14.EGL_BAD_NATIVE_WINDOW == error) {
                throw new RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ");
            }
            throwError("eglCreateWindowSurface failed : ");
        }
        // 配置好GL环境
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throwError("eglMakeCurrent failed: ");
        }
        // 调试代码，查询EGLContext的client version
        int[] eglContextClientVersion = new int[1];
        EGL14.eglQueryContext(mEGLDisplay, mEGLContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, eglContextClientVersion, 0);
        Dog.i(TAG, "EGL context client version: " + eglContextClientVersion[0]);
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
            EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
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
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
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
