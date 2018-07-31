package com.demo.yujieliu.agorademo.egl;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * 用{@link EGL10}实现的{@link EGLCore}的子类
 * <p>
 * Created by yujieliu on 2018/6/7.
 */
public class EGLCore10 extends EGLCore<EGLContext, EGLSurface, EGLDisplay, EGLConfig> {
    private final EGL10 mEgl;

    EGLCore10(EGLContext sharedContext, int flag) {
        super(sharedContext, flag);
        mEgl = (EGL10) EGLContext.getEGL();
    }

    @Override
    public boolean isCurrent() {
        return mEGLContext != null && mEGLContext != EGL10.EGL_NO_CONTEXT && mEGLDisplay != null &&
                mEGLDisplay != EGL10.EGL_NO_DISPLAY;
    }

    @Override
    protected void clearStuff() {
        mEGLContext = EGL10.EGL_NO_CONTEXT;
        mEGLDisplay = EGL10.EGL_NO_DISPLAY;
        mEGLSurface = EGL10.EGL_NO_SURFACE;
    }

    @Override
    protected EGLDisplay eglGetDefaultDisplay() {
        return mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
    }

    @Override
    protected boolean validEGLDisplay(EGLDisplay eglDisplay) {
        return eglDisplay != null && eglDisplay != EGL10.EGL_NO_DISPLAY;
    }

    @Override
    protected boolean validEGLContext(EGLContext eglContext) {
        return eglContext != null && eglContext != EGL10.EGL_NO_CONTEXT;
    }

    @Override
    protected boolean validEGLSurface(EGLSurface eglSurface) {
        return eglSurface != null && eglSurface != EGL10.EGL_NO_SURFACE;
    }

    @Override
    protected EGLDisplay eglGetDisplay(Object obj) {
        return mEgl.eglGetDisplay(obj);
    }

    @Override
    protected boolean eglInitialize(EGLDisplay eglDisplay, int[] version) {
        return mEgl.eglInitialize(eglDisplay, version);
    }

    @Override
    protected boolean eglChooseConfig(EGLDisplay eglDisplay, int[] configAttribs, Object[] eglConfigs, int configSize, int[] numConfigs) {
        EGLConfig[] eglConfigArr = new EGLConfig[1];
        boolean ret = mEgl.eglChooseConfig(eglDisplay, configAttribs, eglConfigArr, configSize, numConfigs);
        System.arraycopy(eglConfigArr, 0, eglConfigs, 0, eglConfigs.length);
        return ret;
    }

    @Override
    protected EGLContext eglCreateContext(EGLDisplay eglDisplay, EGLConfig eglConfig, EGLContext sharedContext, int[] contextAttribs) {
        return mEgl.eglCreateContext(eglDisplay, eglConfig, sharedContext, contextAttribs);
    }

    @Override
    protected EGLSurface eglCreateWindowSurface(EGLDisplay eglDisplay, EGLConfig eglConfig, Object nativeWindow, int[] surfaceAttribs) {
        return mEgl.eglCreateWindowSurface(eglDisplay, eglConfig, nativeWindow, surfaceAttribs);
    }

    @Override
    protected EGLSurface eglCreatePbufferSurface(EGLDisplay eglDisplay, EGLConfig eglConfig, int[] surfaceAttribs) {
        return mEgl.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs);
    }

    @Override
    public int eglGetError() {
        return mEgl.eglGetError();
    }

    @Override
    protected boolean eglMakeCurrent(EGLDisplay eglDisplay, EGLSurface eglDrawSurface, EGLSurface eglReadSurface, EGLContext eglContext) {
        return mEgl.eglMakeCurrent(eglDisplay, eglDrawSurface, eglReadSurface, eglContext);
    }

    @Override
    protected void eglDestroyContext(EGLDisplay eglDisplay, EGLContext eglContext) {
        mEgl.eglDestroyContext(eglDisplay, eglContext);
    }

    @Override
    protected void eglDestroySurface(EGLDisplay eglDisplay, EGLSurface eglSurface) {
        mEgl.eglDestroySurface(eglDisplay, eglSurface);
    }

    @Override
    protected void eglSwapBuffers(EGLDisplay eglDisplay, EGLSurface eglSurface) {
        mEgl.eglSwapBuffers(eglDisplay, eglSurface);
    }

    @Override
    protected void eglQueryContext(EGLDisplay eglDisplay, EGLContext eglContext, int attribute, int[] value) {
        mEgl.eglQueryContext(eglDisplay, eglContext, attribute, value);
    }

    @Override
    protected void eglQuerySurface(EGLDisplay eglDisplay, EGLSurface eglSurface, int attribute, int[] value, int offset) {
        int[] subVal = new int[1];
        mEgl.eglQuerySurface(eglDisplay, eglSurface, attribute, subVal);
        value[offset] = subVal[0];
    }

    @Override
    protected void eglGetConfigAttrib(EGLDisplay eglDisplay, EGLConfig eglConfig, int attribute, int[] value) {
        mEgl.eglGetConfigAttrib(eglDisplay, eglConfig, attribute, value);
    }

    @Override
    public EGLDisplay eglGetCurrentDisplay() {
        return mEgl.eglGetCurrentDisplay();
    }

    @Override
    public EGLContext eglGetCurrentContext() {
        return mEgl.eglGetCurrentContext();
    }

    @Override
    public EGLSurface eglGetCurrentDrawSurface() {
        return mEgl.eglGetCurrentSurface(EGL10.EGL_DRAW);
    }

    @Override
    public EGLSurface eglGetCurrentReadSurface() {
        return mEgl.eglGetCurrentSurface(EGL10.EGL_READ);
    }

    @Override
    public void restoreState(EGLContext context, EGLSurface readSurface, EGLSurface drawSurface, EGLDisplay display) {
        mEgl.eglMakeCurrent(display, drawSurface, readSurface, context);
    }
}
