package com.demo.yujieliu.agorademo.egl;

import android.annotation.TargetApi;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLExt;
import android.opengl.GLUtils;
import android.os.Build;
import android.support.annotation.Nullable;

import com.demo.yujieliu.agorademo.Dog;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;

/**
 * 管理EGLContext, EGLSurface, EGLConfig的基类，具体实现类
 * <p>
 * Created by yujieliu on 2018/6/7.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public abstract class EGLCore<Context, Surface, Display, Config> {
    private static final String TAG = "EGLCore";
    /**
     * 当前设备是否支持{@link EGL14}
     */
    public static final boolean SUPPORT_EGL14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    /**
     * 当前设备是否支持{@link EGLExt}
     */
    public static final boolean SUPPORT_EGLEXT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    protected static final int EGL_CONTEXT_CLIENT_VERSION = SUPPORT_EGL14 ? EGL14.EGL_CONTEXT_CLIENT_VERSION : 0x3098;
    protected static final int EGL_BACK_BUFFER = SUPPORT_EGL14 ? EGL14.EGL_BACK_BUFFER : 0x3084;
    /**
     * EGLConfig EGL_RENDERABLE_TYPE attribute的值，表示使用OpenGL ES 2或者3
     */
    private static final int EGL_OPENGL_ES3_BIT_KHR = SUPPORT_EGLEXT ? EGLExt.EGL_OPENGL_ES3_BIT_KHR : 0x0040;
    private static final int EGL_OPENGL_ES2_BIT = SUPPORT_EGL14 ? EGL14.EGL_OPENGL_ES2_BIT : 0x0004;

    private static final int EGL_NONE = SUPPORT_EGL14 ? EGL14.EGL_NONE : EGL10.EGL_NONE;
    /**
     * Android专用扩展，创建EGLConfig时如果传入该标志的话，EGL创建的Surface用的Buffer会与{@link android.media.MediaCodec}兼容
     */
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    public static final int FLAG_RECORDABLE = 0x01;
    public static final int FLAG_PREFER_GLES3 = 0x02;

    // 我也不知道这个到底要不要private
    protected Context mEGLContext;
    protected Surface mEGLSurface;
    protected Display mEGLDisplay;
    protected Config mEGLConfig;
    private int mGLESVersion;

    /**
     * 创建EGLCore实现类
     *
     * @param sharedContext ShareContext, 如果要共享外部GL环境的资源的话，就传入，否则传null即可
     * @param flag          创建EGLContext的flag
     * @return EGLCore实现类
     * @see #FLAG_PREFER_GLES3
     * @see #FLAG_RECORDABLE
     */
    public static EGLCore createEGL(@Nullable Object sharedContext, int flag) {
        if (sharedContext instanceof android.opengl.EGLContext) {
            return new EGLCore14((EGLContext) sharedContext, flag);
        } else if (sharedContext instanceof javax.microedition.khronos.egl.EGLContext) {
            return new EGLCore10((javax.microedition.khronos.egl.EGLContext) sharedContext, flag);
        } else if (sharedContext == null) {
            if (SUPPORT_EGL14) {
                return new EGLCore14(EGL14.EGL_NO_CONTEXT, flag);
            }
            return new EGLCore10(EGL10.EGL_NO_CONTEXT, flag);
        }
        throw new IllegalArgumentException("Parameter 'context' should be of type 'android.opengl.EGLContext' or " +
                "'javax.microedition.khronos.egl.EGLContext'!");
    }

    protected EGLCore(Context sharedContext, int flag) {
        init(sharedContext, flag);
    }

    private void init(Context sharedContext, int flag) {
        mEGLDisplay = eglGetDefaultDisplay();
        if (!validEGLDisplay(mEGLDisplay)) {
            throw new RuntimeException("Unable to get default EGLDisplay!");
        }
        int[] majorMinorVersion = new int[2];
        if (!eglInitialize(mEGLDisplay, majorMinorVersion)) {
            throw new RuntimeException("eglInitialize failed!");
        }
        Dog.d(TAG, "EGL major minor version:" + Arrays.toString(majorMinorVersion));
        if ((FLAG_PREFER_GLES3 & flag) != 0) {
            Config eglConfig = getConfig(flag, 3);
            if (eglConfig != null) {
                int[] attrib3_list = {
                        // 0x3098
                        EGL_CONTEXT_CLIENT_VERSION, 3,
                        EGL_NONE
                };

                Context context = eglCreateContext(mEGLDisplay, eglConfig, sharedContext,
                        attrib3_list);

                if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                    mEGLConfig = eglConfig;
                    mEGLContext = context;
                    mGLESVersion = 3;
                }
            }
        }
        if (mEGLContext == null) {
            Config config = getConfig(flag, 2);
            if (null == config) {
                throw new RuntimeException("Cannot get any suitable EGLConfig!");
            }
            int[] attrib2_list = {
                    // 0x3098
                    EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL_NONE
            };
            mEGLContext = eglCreateContext(mEGLDisplay, config, sharedContext, attrib2_list);
            mGLESVersion = 2;
            mEGLConfig = config;
        }
        checkEGLError("create EGLContext");
    }

    protected void checkEGLError(String msg) {
        int errCode = eglGetError();
        if (errCode != EGL10.EGL_SUCCESS) {
            throw new RuntimeException(msg + ", error: " + GLUtils.getEGLErrorString(errCode));
        }
    }

    @SuppressWarnings("unchecked")
    protected Config getConfig(int flag, int glesVersion) {
        int renderableType = EGL_OPENGL_ES2_BIT;
        if (glesVersion >= 3) {
            renderableType |= EGL_OPENGL_ES3_BIT_KHR;
        }
        int[] attribList = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, renderableType,
                EGL10.EGL_NONE, 0,      // placeholder for EGL_RECORDABLE_ANDROID
                EGL10.EGL_NONE
        };
        if ((flag & FLAG_RECORDABLE) != 0) {
            attribList[attribList.length - 3] = EGL_RECORDABLE_ANDROID;
            attribList[attribList.length - 2] = 1;
        }

        int[] numConfigs = new int[1];
        Object[] eglConfigs = new Object[1];

        if (!eglChooseConfig(mEGLDisplay, attribList, eglConfigs, eglConfigs.length,
                numConfigs)) {
            return null;
        }
        return (Config) eglConfigs[0];
    }

    public void createSurface(EGLSurfaceConfig surfaceConfig) {
        if (validEGLSurface(mEGLSurface)) {
            Dog.d(TAG, "EGLSurface already created");
            return;
        }
        @EGLSurfaceConfig.SurfaceType int surfaceType = surfaceConfig.chooseSurfaceType();
        switch (surfaceType) {
            case EGLSurfaceConfig.SurfaceType.PBUFFER_SURFACE: {
                EGLSurfaceConfig.Size size = surfaceConfig.getSizeForPbufferSurface();
                int[] surfaceAttribs = {
                        EGL10.EGL_WIDTH, size.width,
                        EGL10.EGL_HEIGHT, size.height,
                        EGL10.EGL_NONE
                };
                mEGLSurface = eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttribs);
                checkEGLError("CreatePbufferSurface");
                break;
            }
            case EGLSurfaceConfig.SurfaceType.WINDOW_SURFACE: {
                int[] surfaceAttribs = {
                         EGL10.EGL_RENDER_BUFFER, EGL10.EGL_SINGLE_BUFFER, EGL10.EGL_NONE
//                        EGL10.EGL_RENDER_BUFFER, EGL_BACK_BUFFER, EGL10.EGL_NONE
//                        EGL10.EGL_NONE
                };
                Object nativeWindow = surfaceConfig.getNativeWindowForWindowSurface();
                mEGLSurface = eglCreateWindowSurface(mEGLDisplay, mEGLConfig, nativeWindow, surfaceAttribs);
                checkEGLError("CreateWindowSurface");
                checkSurfaceSize();
                break;
            }
        }
        if (!validEGLDisplay(mEGLDisplay)) {
            throw new RuntimeException("Unable to create an EGLSurface!");
        }
    }

    private void checkSurfaceSize() {
        int[] value = new int[4];
        eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_WIDTH, value, 0);
        eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_HEIGHT, value, 1);
        eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_RENDER_BUFFER, value, 2);
        eglQuerySurface(mEGLDisplay, mEGLSurface, EGL10.EGL_WINDOW_BIT, value, 3);
        Dog.d(TAG, "EGLSurfaceAttributes: " + Arrays.toString(value));
    }

    public void makeCurrent() {
        if (!eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            checkEGLError("eglMakeCurrent ");
        }
    }

    public void swapBuffers() {
        eglSwapBuffers(mEGLDisplay, mEGLSurface);
    }

    public void release() {
        eglDestroyContext(mEGLDisplay, mEGLContext);
        eglDestroySurface(mEGLDisplay, mEGLSurface);
        clearStuff();
    }

    public abstract boolean isCurrent();

    /**
     * 设置{@link #mEGLContext}, {@link #mEGLSurface}, {@link #mEGLDisplay}为EGL_NO_XX
     */
    protected abstract void clearStuff();

    protected abstract Display eglGetDefaultDisplay();

    protected abstract boolean validEGLDisplay(Display display);

    protected abstract boolean validEGLContext(Context context);

    protected abstract boolean validEGLSurface(Surface surface);

    protected abstract Display eglGetDisplay(Object obj);

    protected abstract boolean eglInitialize(Display eglDisplay, int[] version);

    protected abstract boolean eglChooseConfig(Display eglDisplay, int[] configAttribs, Object[] eglConfigs, int configSize, int[] numConfigs);

    protected abstract Context eglCreateContext(Display eglDisplay, Config eglConfig, Context sharedContext, int[] contextAttribs);

    protected abstract Surface eglCreateWindowSurface(Display eglDisplay, Config eglConfig, Object nativeWindow, int[] surfaceAttribs);

    protected abstract Surface eglCreatePbufferSurface(Display eglDisplay, Config eglConfig, int[] surfaceAttribs);

    public abstract int eglGetError();

    protected abstract boolean eglMakeCurrent(Display eglDisplay, Surface eglDrawSurface, Surface eglReadSurface, Context eglContext);

    protected abstract void eglDestroyContext(Display eglDisplay, Context eglContext);

    protected abstract void eglDestroySurface(Display eglDisplay, Surface eglSurface);

    protected abstract void eglSwapBuffers(Display eglDisplay, Surface eglSurface);

    protected abstract void eglQuerySurface(Display eglDisplay, Surface eglSurface, int attribute, int[] value, int offset);

    protected abstract void eglQueryContext(Display eglDisplay, Context eglContext, int attribute, int[] value);

    protected abstract void eglGetConfigAttrib(Display eglDisplay, Config eglConfig, int attribute, int[] value);

    public abstract Display eglGetCurrentDisplay();

    public abstract Context eglGetCurrentContext();

    public abstract Surface eglGetCurrentDrawSurface();

    public abstract Surface eglGetCurrentReadSurface();

    public abstract void restoreState(Context context, Surface readSurface, Surface drawSurface, Display display);

    public final int getGLESVersion() {
        return mGLESVersion;
    }
}
