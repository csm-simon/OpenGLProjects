package com.demo.yujieliu.agorademo.egl;

import android.annotation.TargetApi;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Build;


/**
 * 使用{@link EGL14}实现的{@link EGLCore}的子类，在4.1以上系统使用
 * <p>
 * Created by yujieliu on 2018/6/7.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class EGLCore14 extends EGLCore<EGLContext, EGLSurface, EGLDisplay, EGLConfig> {

    EGLCore14(EGLContext sharedContext, int flag) {
        super(sharedContext, flag);
    }

    @Override
    public boolean isCurrent() {
        return mEGLContext != null && EGL14.eglGetCurrentContext() == mEGLContext
                && mEGLSurface == EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
    }

    @Override
    protected void clearStuff() {
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEGLDisplay);
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    }

    @Override
    protected EGLDisplay eglGetDefaultDisplay() {
        return EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
    }

    @Override
    protected boolean validEGLDisplay(EGLDisplay display) {
        return display != null && display != EGL14.EGL_NO_DISPLAY;
    }

    @Override
    protected boolean validEGLContext(EGLContext context) {
        return context != null && context != EGL14.EGL_NO_CONTEXT;
    }

    @Override
    protected boolean validEGLSurface(EGLSurface eglSurface) {
        return eglSurface != null && eglSurface != EGL14.EGL_NO_SURFACE;
    }

    @Override
    public EGLDisplay eglGetCurrentDisplay() {
        return EGL14.eglGetCurrentDisplay();
    }

    @Override
    public EGLContext eglGetCurrentContext() {
        return EGL14.eglGetCurrentContext();
    }

    @Override
    public EGLDisplay eglGetDisplay(Object obj) {
        return EGL14.eglGetDisplay((Integer) obj);
    }

    @Override
    public boolean eglInitialize(EGLDisplay eglDisplay, int[] version) {
        return EGL14.eglInitialize(eglDisplay, version, 0, version, 1);
    }

    @Override
    public boolean eglChooseConfig(EGLDisplay eglDisplay, int[] configAttribs, Object[] eglConfigs, int configSize, int[] numConfigs) {
        EGLConfig[] eglConfigArr = new EGLConfig[eglConfigs.length];
        boolean ret = EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, eglConfigArr, 0, eglConfigs.length, numConfigs, 0);
        System.arraycopy(eglConfigArr, 0, eglConfigs, 0, eglConfigs.length);
        return ret;
    }

    @Override
    public EGLContext eglCreateContext(EGLDisplay eglDisplay, EGLConfig eglConfig, EGLContext sharedContext, int[] contextAttribs) {
        return EGL14.eglCreateContext(eglDisplay, eglConfig, sharedContext, contextAttribs, 0);
    }

    @Override
    public EGLSurface eglCreateWindowSurface(EGLDisplay eglDisplay, EGLConfig eglConfig, Object nativeWindow, int[] surfaceAttribs) {
        return EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, nativeWindow, surfaceAttribs, 0);
    }

    @Override
    protected EGLSurface eglCreatePbufferSurface(EGLDisplay eglDisplay, EGLConfig eglConfig, int[] surfaceAttribs) {
        return EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0);
    }

    @Override
    public int eglGetError() {
        return EGL14.eglGetError();
    }

    @Override
    public boolean eglMakeCurrent(EGLDisplay eglDisplay, EGLSurface eglDrawSurface, EGLSurface eglReadSurface, EGLContext eglContext) {
        return EGL14.eglMakeCurrent(eglDisplay, eglDrawSurface, eglReadSurface, eglContext);
    }

    @Override
    public void eglDestroyContext(EGLDisplay eglDisplay, EGLContext eglContext) {
        EGL14.eglDestroyContext(eglDisplay, eglContext);
    }

    @Override
    public void eglDestroySurface(EGLDisplay eglDisplay, EGLSurface eglSurface) {
        EGL14.eglDestroySurface(eglDisplay, eglSurface);
    }

    @Override
    public void eglSwapBuffers(EGLDisplay eglDisplay, EGLSurface eglSurface) {
        EGL14.eglSwapBuffers(eglDisplay, eglSurface);
    }

    @Override
    public void eglQueryContext(EGLDisplay eglDisplay, EGLContext eglContext, int attribute, int[] value) {
        EGL14.eglQueryContext(eglDisplay, eglContext, attribute, value, 0);
    }

    @Override
    protected void eglQuerySurface(EGLDisplay eglDisplay, EGLSurface eglSurface, int attribute, int[] value, int offset) {
        EGL14.eglQuerySurface(eglDisplay, eglSurface, attribute, value, offset);
    }

    @Override
    public void eglGetConfigAttrib(EGLDisplay eglDisplay, EGLConfig eglConfig, int attribute, int[] value) {
        EGL14.eglGetConfigAttrib(eglDisplay, eglConfig, attribute, value, 0);
    }

    @Override
    public EGLSurface eglGetCurrentDrawSurface() {
        return EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);
    }

    @Override
    public EGLSurface eglGetCurrentReadSurface() {
        return EGL14.eglGetCurrentSurface(EGL14.EGL_READ);
    }

    @Override
    public void restoreState(EGLContext context, EGLSurface readSurface, EGLSurface drawSurface, EGLDisplay display) {
        EGL14.eglMakeCurrent(display, drawSurface, readSurface, context);
    }
}
