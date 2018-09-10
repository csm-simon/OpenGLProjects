package com.lauyukit.demo.glfilterdemo.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * 封装了GLSurfaceView的自定义View
 * <p>
 * 外部可以通过{@link #setFilter(GLFilter)}和{@link #setImage(Bitmap)}方法来设置滤镜效果和显示的图片。
 * 如果需要离屏渲染，也就是输入一个Bitmap，输出一个带有滤镜效果的Bitmap的话，可以通过{@link #getGLImage()}来做。
 * <p>
 * Created by yujieliu on 2018/3/19.
 */
public class GLImageView extends FrameLayout {
    private GLSurfaceView mGLSurfaceView;
    private GLImage mGLImage;
    private GLFilter mFilter;

    public GLImageView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public GLImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GLImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        mGLSurfaceView = new GLSurfaceView(context);
        addView(mGLSurfaceView);
        mGLImage = new GLImage(context);
        mGLImage.setGLSurfaceView(mGLSurfaceView);
    }

    /**
     * 设置图片ScaleType
     *
     * @param scaleType ScaleType
     * @see GLImage.ScaleType
     */
    public void setScaleType(GLImage.ScaleType scaleType) {
        mGLImage.setScaleType(scaleType);
    }

    public void setFilter(GLFilter filter) {
        mFilter = filter;
        mGLImage.setFilter(filter);
        requestRender();
    }

    public GLFilter getFilter() {
        return mFilter;
    }

    public void setImage(Bitmap bitmap) {
        mGLImage.setImage(bitmap);
    }

    @AnyThread
    public GLImage getGLImage() {
        return mGLImage;
    }

    public void requestRender() {
        mGLSurfaceView.requestRender();
    }

    /**
     * Pauses the GLSurfaceView.
     */
    public void onPause() {
        mGLSurfaceView.onPause();
    }

    /**
     * Resumes the GLSurfaceView.
     */
    public void onResume() {
        mGLSurfaceView.onResume();
    }

}
