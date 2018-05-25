package com.demo.yujieliu.agorademo.gl;

import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.os.Build;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * 代理{@link EGL10}和{@link EGL14}的Compat类。因为{@link EGL10}和{@link EGL14}的方法参数基本一致，但是两者
 * 不是继承关系，所以才有这个类来统一代理。如果设备支持{@link EGL14}的话就会优先使用{@link EGL14}，否则就用{@link EGL10}
 * <p>
 * Created by yujieliu on 2018/5/16.
 */
public class EGLCompat {
    /**
     * 当前设备是否支持{@link EGL14}
     */
    public static final boolean SUPPORT_EGL14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    /**
     * 当前设备是否支持{@link EGLExt}
     */
    public static final boolean SUPPORT_EGLEXT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    /**
     * 一些{@link EGL10}里没有的而{@link EGL14}里有的常量声明。实际上这些常量在OpenGL ES2和3里都通用。
     */
    public static Object EGL_DEFAULT_DISPLAY = SUPPORT_EGL14 ? Integer.valueOf(EGL14.EGL_DEFAULT_DISPLAY) : EGL10.EGL_DEFAULT_DISPLAY;
    public static Object EGL_NO_CONTEXT = SUPPORT_EGL14 ? EGL14.EGL_NO_CONTEXT : EGL10.EGL_NO_CONTEXT;
    public static Object EGL_NO_SURFACE = SUPPORT_EGL14 ? EGL14.EGL_NO_SURFACE : EGL10.EGL_NO_SURFACE;
    public static Object EGL_NO_DISPLAY = SUPPORT_EGL14 ? EGL14.EGL_NO_DISPLAY : EGL10.EGL_NO_DISPLAY;
    public static final int EGL_CONTEXT_CLIENT_VERSION = SUPPORT_EGL14 ? EGL14.EGL_CONTEXT_CLIENT_VERSION : 0x3098;
    public static final int EGL_BACK_BUFFER = SUPPORT_EGL14 ? EGL14.EGL_BACK_BUFFER : 0x3084;
    /**
     * EGLConfig EGL_RENDERABLE_TYPE attribute的值，表示使用OpenGL ES 2或者3
     */
    private static final int EGL_OPENGL_ES3_BIT_KHR = SUPPORT_EGLEXT ? EGLExt.EGL_OPENGL_ES3_BIT_KHR : 0x0040;
    private static final int EGL_OPENGL_ES2_BIT = SUPPORT_EGL14 ? EGL14.EGL_OPENGL_ES2_BIT : 0x0004;
    private EGL10 mEgl;

    public EGLCompat() {
        if (!SUPPORT_EGL14) {
            mEgl = (EGL10) EGLContext.getEGL();
        }
    }

    public Object eglGetDisplay(Object obj) {
        if (SUPPORT_EGL14) {
            return EGL14.eglGetDisplay((Integer) obj);
        }
        return mEgl.eglGetDisplay(obj);
    }

    public boolean eglInitialize(Object eglDisplay, int[] version) {
        if (SUPPORT_EGL14) {
            return EGL14.eglInitialize((android.opengl.EGLDisplay) eglDisplay, version, 0, version, 1);
        }
        return mEgl.eglInitialize((EGLDisplay) eglDisplay, version);
    }

    public boolean eglChooseConfig(Object eglDisplay, int[] configAttribs, Object[] eglConfigs, int configSize, int[] numConfigs) {
        if (SUPPORT_EGL14) {
            android.opengl.EGLConfig[] configs = Arrays.copyOf(eglConfigs, eglConfigs.length, android.opengl.EGLConfig[].class);
            boolean ret = EGL14.eglChooseConfig((android.opengl.EGLDisplay) eglDisplay, configAttribs, 0, configs, 0, configSize, numConfigs, 0);
            eglConfigs[0] = configs[0];
            return ret;
        } else {
            EGLConfig[] configs = Arrays.copyOf(eglConfigs, eglConfigs.length, EGLConfig[].class);
            boolean ret = mEgl.eglChooseConfig((EGLDisplay) eglDisplay, configAttribs, configs, configSize, numConfigs);
            eglConfigs[0] = configs[0];
            return ret;
        }
    }

    public Object eglCreateContext(Object eglDisplay, Object eglConfig, Object sharedContext, int[] contextAttribs) {
        if (SUPPORT_EGL14) {
            return EGL14.eglCreateContext((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLConfig) eglConfig, (android.opengl.EGLContext) sharedContext, contextAttribs, 0);
        }
        return mEgl.eglCreateContext((EGLDisplay) eglDisplay, (EGLConfig) eglConfig, (EGLContext) sharedContext, contextAttribs);
    }

    public Object eglCreateWindowSurface(Object eglDisplay, Object eglConfig, Object nativeWindow, int[] surfaceAttribs) {
        if (SUPPORT_EGL14) {
            return EGL14.eglCreateWindowSurface((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLConfig) eglConfig, nativeWindow, surfaceAttribs, 0);
        }
        return mEgl.eglCreateWindowSurface((EGLDisplay) eglDisplay, (EGLConfig) eglConfig, nativeWindow, surfaceAttribs);
    }

    public int eglGetError() {
        if (SUPPORT_EGL14) {
            return EGL14.eglGetError();
        }
        return mEgl.eglGetError();
    }

    public boolean eglMakeCurrent(Object eglDisplay, Object eglDrawSurface, Object eglReadSurface, Object eglContext) {
        if (SUPPORT_EGL14) {
            return EGL14.eglMakeCurrent((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLSurface) eglDrawSurface, (android.opengl.EGLSurface) eglReadSurface, (android.opengl.EGLContext) eglContext);
        }
        return mEgl.eglMakeCurrent((EGLDisplay) eglDisplay, (EGLSurface) eglDrawSurface, (EGLSurface) eglReadSurface, (EGLContext) eglContext);
    }

    public void eglDestroyContext(Object eglDisplay, Object eglContext) {
        if (SUPPORT_EGL14) {
            EGL14.eglDestroyContext((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLContext) eglContext);
        } else {
            mEgl.eglDestroyContext((EGLDisplay) eglDisplay, (EGLContext) eglContext);
        }
    }

    public void eglDestroySurface(Object eglDisplay, Object eglSurface) {
        if (SUPPORT_EGL14) {
            EGL14.eglDestroySurface((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLSurface) eglSurface);
        } else {
            mEgl.eglDestroySurface((EGLDisplay) eglDisplay, (EGLSurface) eglSurface);
        }
    }

    public void eglSwapBuffers(Object eglDisplay, Object eglSurface) {
        if (SUPPORT_EGL14) {
            EGL14.eglSwapBuffers((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLSurface) eglSurface);
        } else {
            mEgl.eglSwapBuffers((EGLDisplay) eglDisplay, (EGLSurface) eglSurface);
        }
    }

    public void eglQueryContext(Object eglDisplay, Object eglContext, int attribute, int[] value) {
        if (SUPPORT_EGL14) {
            EGL14.eglQueryContext((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLContext) eglContext, attribute, value, 0);
        } else {
            mEgl.eglQueryContext((EGLDisplay) eglDisplay, (EGLContext) eglContext, attribute, value);
        }
    }

    public void eglGetConfigAttrib(Object eglDisplay, Object eglConfig, int attribute, int[] value) {
        if (SUPPORT_EGL14) {
            EGL14.eglGetConfigAttrib((android.opengl.EGLDisplay) eglDisplay, (android.opengl.EGLConfig) eglConfig, attribute, value, 0);
        } else {
            mEgl.eglGetConfigAttrib((EGLDisplay) eglDisplay, (EGLConfig) eglConfig, attribute, value);
        }
    }

    /**
     * 判断EGLDisplay是否有效
     *
     * @param object {@link EGLDisplay} or {@link android.opengl.EGLDisplay}
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEGLDisplay(Object object) {
        if (object instanceof EGLDisplay) {
            return object != EGL10.EGL_NO_DISPLAY;
        } else if (SUPPORT_EGL14 && object instanceof android.opengl.EGLDisplay) {
            return object != EGL14.EGL_NO_DISPLAY;
        }
        return false;
    }

    /**
     * 判断EGLContext是否有效
     *
     * @param object {@link EGLContext} or {@link android.opengl.EGLContext}
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEGLContext(Object object) {
        if (object instanceof EGLContext) {
            return object != EGL10.EGL_NO_CONTEXT;
        } else if (SUPPORT_EGL14 && object instanceof android.opengl.EGLContext) {
            return object != EGL14.EGL_NO_CONTEXT;
        }
        return false;
    }

    /**
     * 判断EGLSurface是否有效
     *
     * @param object {@link EGLSurface} or {@link android.opengl.EGLSurface}
     * @return true if valid, false otherwise.
     */
    public static boolean isValidEGLSurface(Object object) {
        if (object instanceof EGLSurface) {
            return object != EGL10.EGL_NO_SURFACE;
        } else if (SUPPORT_EGL14 && object instanceof android.opengl.EGLSurface) {
            return object != EGL14.EGL_NO_SURFACE;
        }
        return false;
    }

    /**
     * 根据给定的OpenGL ES版本，返回对应的EGL_CONTEXT_BIT，作为EGLConfig EGL_RENDERABLE_TYPE attribute的值。
     * @param desiredGLESVer OpenGL ES版本 2 or 3
     * @return EGL_RENDERABLE_TYPE bit
     */
    public static int getEGLContextClientVersionBit(int desiredGLESVer) {
        if (SUPPORT_EGL14 && 3 == desiredGLESVer) {
            return EGL_OPENGL_ES3_BIT_KHR;
        }
        if (2 == desiredGLESVer) {
            return EGL_OPENGL_ES2_BIT;
        }
        return 1;
    }

    /**
     * 查询EGL Context client version, 要在含有EGLContext的线程中调用
     * @return EGL Context client version
     */
    public static int queryEGLContextClientVersion() {
        int[] val = new int[1];
        // 查询EGL Context Client Versio，即OpenGL ES版本
        if (EGLCompat.SUPPORT_EGL14) {
            android.opengl.EGLContext eglContext = EGL14.eglGetCurrentContext();
            android.opengl.EGLDisplay eglDisplay = EGL14.eglGetCurrentDisplay();
            EGL14.eglQueryContext(eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, val, 0);
        } else {
            EGL10 egl = (EGL10) javax.microedition.khronos.egl.EGLContext.getEGL();
            javax.microedition.khronos.egl.EGLContext eglContext = egl.eglGetCurrentContext();
            javax.microedition.khronos.egl.EGLDisplay eglDisplay = egl.eglGetCurrentDisplay();
            egl.eglQueryContext(eglDisplay, eglContext, EGLCompat.EGL_CONTEXT_CLIENT_VERSION, val);
        }
        return val[0];
    }
}
