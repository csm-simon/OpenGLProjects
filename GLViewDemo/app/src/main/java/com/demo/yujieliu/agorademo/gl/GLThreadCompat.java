package com.demo.yujieliu.agorademo.gl;

import android.graphics.SurfaceTexture;
import android.opengl.GLUtils;

import com.demo.yujieliu.agorademo.Dog;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGL10;

/**
 * GL渲染线程。需要在该线程执行操作的话就调用{@link #queueEvent(Runnable)}
 * <p>
 * Created by yujieliu on 2018/5/16.
 */
public class GLThreadCompat extends Thread {
    private static final String TAG = "GLThreadCompat";

    private final AtomicBoolean mAlive;
    private final GLProgram mGLProgram;
    private final Object mRenderLock = new Object();
    private final int mEGLContextClientVersion;
    private final Queue<Runnable> mRunOnDrawQueue;

    private SurfaceTexture mSurfaceTexture;
    private Object mEGLContext;
    private Object mEGLDisplay;
    private Object mEGLSurface;
    private EGLCompat mEGLCompat;

    public GLThreadCompat(SurfaceTexture surfaceTexture, GLProgram program, int eglContextVersion) {
        super();
        mGLProgram = program;
        mAlive = new AtomicBoolean(true);
        mSurfaceTexture = surfaceTexture;
        mEGLContextClientVersion = eglContextVersion;
        mRunOnDrawQueue = new ArrayDeque<>();
    }

    private void initGLEnvironment() {
        mEGLCompat = new EGLCompat();
        mEGLDisplay = mEGLCompat.eglGetDisplay(EGLCompat.EGL_DEFAULT_DISPLAY);
        if (!EGLCompat.isValidEGLDisplay(mEGLDisplay)) {
            throwError("Get EGLDisplay failed: ");
        }
        int[] eglImplVersion = new int[2];
        if (!mEGLCompat.eglInitialize(mEGLDisplay, eglImplVersion)) {
            throwError("Initialize EGLDisplay failed: ");
        }
        int eglVersionBit = EGLCompat.getEGLContextClientVersionBit(mEGLContextClientVersion);
        Dog.d(TAG, "EGL Context Client Version Bit: " + eglVersionBit + ", mEGLContextClientVersion: " + mEGLContextClientVersion);
        int[] configAttribs = {
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_RENDERABLE_TYPE, eglVersionBit,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
        };
        int[] numConfigs = new int[1];
        Object[] configs = new Object[1];
        if (!mEGLCompat.eglChooseConfig(mEGLDisplay, configAttribs, configs,
                1, numConfigs)) {
            throwError("Choose EGLConfig failed: ");
        }
        queryEGLConfig(mEGLDisplay, configs[0]);
        int[] contextAttribs = {
                EGLCompat.EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
                EGL10.EGL_NONE
        };
        mEGLContext = mEGLCompat.eglCreateContext(mEGLDisplay, configs[0], EGLCompat.EGL_NO_CONTEXT, contextAttribs);
        int[] surfaceAttribs = {
                EGL10.EGL_RENDER_BUFFER, EGLCompat.EGL_BACK_BUFFER, EGL10.EGL_NONE
        };
        mEGLSurface = mEGLCompat.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurfaceTexture, surfaceAttribs);
        if (!EGLCompat.isValidEGLSurface(mEGLSurface) || !EGLCompat.isValidEGLContext(mEGLContext)) {
            int error = mEGLCompat.eglGetError();
            if (EGL10.EGL_BAD_NATIVE_WINDOW == error) {
                throw new RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ");
            }
            throwError("eglCreateWindowSurface failed : ");
        }
        if (!mEGLCompat.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throwError("eglMakeCurrent failed: ");
        }

        int[] eglContextClientVersion = new int[1];
        mEGLCompat.eglQueryContext(mEGLDisplay, mEGLContext, EGLCompat.EGL_CONTEXT_CLIENT_VERSION, eglContextClientVersion);
        Dog.i(TAG, "EGL context client version: " + eglContextClientVersion[0]);
    }

    private void queryEGLConfig(Object dpy, Object config) {
        int[] val = new int[1];
        mEGLCompat.eglGetConfigAttrib(dpy, config, EGL10.EGL_RENDERABLE_TYPE, val);
        Dog.i(TAG, "queryEGLConfig renderable type: " + val[0]);
    }

    private void throwError(String prefix) {
        throw new RuntimeException(prefix + GLUtils.getEGLErrorString(mEGLCompat.eglGetError()));
    }

    @Override
    public void run() {
        super.run();
        initGLEnvironment();
        while (mAlive.get()) {
            synchronized (mRunOnDrawQueue) {
                while (!mRunOnDrawQueue.isEmpty()) {
                    mRunOnDrawQueue.poll().run();
                }
            }
            mGLProgram.use();
            mGLProgram.draw();
            mEGLCompat.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            synchronized (mRenderLock) {
                try {
                    mRenderLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        releaseInner();
    }

    public void release() {
        mAlive.set(false);
        notifyRenderLock();
    }

    private void releaseInner() {
        mGLProgram.destroy();
        destroyGLEnvironment();
        mSurfaceTexture = null;
        Dog.d(TAG, "release GLThread finished");
    }

    private void destroyGLEnvironment() {
        mEGLCompat.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGLContext = EGLCompat.EGL_NO_CONTEXT;
        mEGLCompat.eglDestroySurface(mEGLDisplay, mEGLSurface);
        mEGLSurface = EGLCompat.EGL_NO_SURFACE;
        mEGLDisplay = EGLCompat.EGL_NO_DISPLAY;
        mSurfaceTexture = null;
    }

    public void requestRender() {
        notifyRenderLock();
    }

    public void queueEvent(Runnable r) {
        synchronized (mRunOnDrawQueue) {
            mRunOnDrawQueue.add(r);
        }
    }

    private void notifyRenderLock() {
        synchronized (mRenderLock) {
            mRenderLock.notifyAll();
        }
    }
}
