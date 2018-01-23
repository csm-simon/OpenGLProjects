package com.meitu.base.base;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.annotation.CallSuper;
import android.util.AttributeSet;

/**
 * 基础类 GLSurfaceView
 * <p/>
 * Created by 周代亮 on 2018/1/23.
 */

public abstract class BaseGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    public BaseGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public BaseGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @CallSuper
    protected void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setRenderer(this);
    }

}