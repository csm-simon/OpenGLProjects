package com.demo.yujieliu.agorademo.egl;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 创建EGLSurface所需的配置
 * <p>
 * Created by yujieliu on 2018/6/8.
 */
public abstract class EGLSurfaceConfig {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SurfaceType.WINDOW_SURFACE, SurfaceType.PBUFFER_SURFACE})
    public @interface SurfaceType {
        int WINDOW_SURFACE = 0; // 屏幕渲染
        int PBUFFER_SURFACE = 1; // 离屏渲染
    }

    /**
     * 选择创建的Surface类型
     *
     * @return Surface类型, {@link SurfaceType}
     */
    public abstract @SurfaceType int chooseSurfaceType();

    /**
     * 创建离屏渲染Pbuffer所需的配置：宽和高
     *
     * @return 离屏渲染Surface的宽高
     */
    public Size getSizeForPbufferSurface() {
        return null;
    }

    /**
     * 创建WindowSurface所需的nativeWindow, {@link android.graphics.SurfaceTexture}或者{@link android.view.Surface}
     *
     * @return nativeWindow Object
     */
    public Object getNativeWindowForWindowSurface() {
        return null;
    }

    public static class Size {
        final int width, height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
