package com.lauyukit.demo.glfilterdemo.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.WindowManager;

/**
 * GLImage，管理GLImageView中用到Renderer、滤镜效果和当前展示的Bitmap对象。同时提供离屏渲染的截图功能。
 *
 * @see #getBitmapWithFilterApplied(Bitmap)
 * <p>
 * Created by yujieliu on 2018/3/19.
 */
public class GLImage {
    private final Context mContext;
    private GLSurfaceView mGLSurfaceView;
    private GLFilter mFilter;
    private GLRenderer mRenderer;
    private Bitmap mCurrentBitmap;
    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    /**
     * 构造方法，如果当前设备不支持OpenGL ES 2会抛异常
     *
     * @param context Context
     */
    GLImage(Context context) {
        if (!OpenGLUtils.supportsOpenGLES2(context)) {
            throw new UnsupportedOperationException("Current device does not support OpenGL ES 2.0!");
        }
        this.mContext = context;
        mFilter = new GLFilter();
        mRenderer = new GLRenderer(mFilter);
    }

    /**
     * 设置并初始化GLSurfaceView
     *
     * @param glSurfaceView GLSurfaceView
     */
    public void setGLSurfaceView(GLSurfaceView glSurfaceView) {
        mGLSurfaceView = glSurfaceView;
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        glSurfaceView.setRenderer(mRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.requestRender();
    }

    /**
     * 设置OpenGL清空画布时用的颜色，颜色范围[0, 1.0]
     *
     * @param r Red
     * @param g Green
     * @param b Blue
     */
    public void setBackgroundColor(float r, float g, float b) {
        mRenderer.setBackgroundColor(r, g, b);
    }

    public void requestRender() {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.requestRender();
        }
    }

    public void setFilter(GLFilter filter) {
        mFilter = filter;
        mRenderer.setFilter(filter);
        requestRender();
    }

    public void setImage(Bitmap bitmap) {
        mCurrentBitmap = bitmap;
        mRenderer.setImageBitmap(bitmap, false);
        requestRender();
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        mRenderer.setScaleType(scaleType);
        mRenderer.deleteTexture();
        mCurrentBitmap = null;
        requestRender();
    }

    public void deleteTexture() {
        mRenderer.deleteTexture();
        mCurrentBitmap = null;
        requestRender();
    }

    /**
     * 离屏渲染，给定Bitmap，返回一个加上滤镜效果的新Bitmap
     *
     * @param bitmap Bitmap原图
     * @return 加上滤镜效果的Bitmap
     */
    public Bitmap getBitmapWithFilterApplied(final Bitmap bitmap) {
        if (mGLSurfaceView != null) {
            mRenderer.deleteTexture();
            mRenderer.runOnDraw(new Runnable() {
                @Override
                public void run() {
                    synchronized (mFilter) {
                        mFilter.destroy();
                        mFilter.notify();
                    }
                }
            });
            // 确保在下一次绘制开始的时候执行后续的代码
            synchronized (mFilter) {
                requestRender();
                try {
                    mFilter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 离屏绘制时把图像翻转180度，这样在glReadPixels时读出来的图片就是正的，改良了原代码中手动翻转图片的内存和CPU消耗
        GLRenderer renderer = new GLRenderer(mFilter, true);
        renderer.setScaleType(mScaleType);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(renderer);
        renderer.setImageBitmap(bitmap, false);
        // 离屏渲染，并获取渲染后的Bitmap
        Bitmap result = buffer.getBitmap();
        mFilter.destroy();
        renderer.deleteTexture();
        buffer.destroy();

        mRenderer.setFilter(mFilter);
        if (mCurrentBitmap != null) {
            mRenderer.setImageBitmap(mCurrentBitmap, false);
        }
        requestRender();

        return result;
    }

    void runOnGLThread(Runnable r) {
        mRenderer.runOnDrawEnd(r);
    }

    private int getOutputWidth() {
        if (mRenderer != null && mRenderer.getOutputWidth() != 0) {
            return mRenderer.getOutputWidth();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getWidth();
        } else {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (wm == null) {
                return 0;
            }
            return wm.getDefaultDisplay().getWidth();
        }
    }

    private int getOutputHeight() {
        if (mRenderer != null && mRenderer.getOutputHeight() != 0) {
            return mRenderer.getOutputHeight();
        } else if (mCurrentBitmap != null) {
            return mCurrentBitmap.getHeight();
        } else {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (wm == null) {
                return 0;
            }
            return wm.getDefaultDisplay().getHeight();
        }
    }

    public enum ScaleType {
        CENTER_CROP,
        CENTER_INSIDE
    }
}
