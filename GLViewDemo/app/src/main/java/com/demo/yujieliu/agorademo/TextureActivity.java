package com.demo.yujieliu.agorademo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;

import com.demo.yujieliu.agorademo.gl.GLProgram;
import com.demo.yujieliu.agorademo.gl.GLThread;
import com.demo.yujieliu.agorademo.gl.OpenGLUtils;

public class TextureActivity extends AppCompatActivity
        implements TextureView.SurfaceTextureListener, GLThread.RenderCallback {
    private static final String EXTRA_GL_VERSION = "gl_ver";
    private static final int DEFAULT_GL_VERSION = 2;
    private TextureView mTextureView;
    private int mGLVersion, mSurfaceWidth, mSurfaceHeight;
    private GLProgram mGLProgram;
    private GLThread mGLThread;
    private Bitmap[] mBackgroundBitmaps;
    private int mCurrentBgIndex;


    public static Intent create(Context context, int glVersion) {
        Intent intent = new Intent(context, TextureActivity.class);
        intent.putExtra(EXTRA_GL_VERSION, glVersion);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);

        findViews();
        initData();
        initListeners();
    }

    private void findViews() {
        mTextureView = findViewById(R.id.texture_view);
    }

    private void initData() {
        Intent intent = getIntent();
        mGLVersion = intent.getIntExtra(EXTRA_GL_VERSION, DEFAULT_GL_VERSION);

        mBackgroundBitmaps = new Bitmap[3];
        @DrawableRes int[] resIds = {
                R.drawable.beauty1,
                R.drawable.beauty2,
                R.drawable.beauty3,
        };
        Resources res = getResources();
        for (int i = 0; i < resIds.length; i++) {
            mBackgroundBitmaps[i] = BitmapFactory.decodeResource(res, resIds[i]);
        }
    }

    private void initListeners() {
        mTextureView.setSurfaceTextureListener(this);
        findViewById(R.id.switch_bg_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLThread.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentBgIndex++;
                        if (mCurrentBgIndex >= mBackgroundBitmaps.length) {
                            mCurrentBgIndex = 0;
                        }
                        // 更新mGLProgram绘制的纹理
                        mGLProgram.loadTexture(mBackgroundBitmaps[mCurrentBgIndex]);
                    }
                });
                mGLThread.requestRender();
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, final int width, final int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        if (mGLThread == null) {
            mGLThread = new GLThread(surface, mGLVersion);
            mGLThread.setRenderWhenDirty(true);
            mGLThread.setRenderCallback(this);
            mGLThread.start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, final int width, final int height) {
        if (null != mGLThread) {
            mGLThread.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mGLProgram.onSurfaceChanged(width, height);
                }
            });
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // mGLThread.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // do nothing
    }

    @Override
    public void onGLResourceInitialized() {
        String vs = OpenGLUtils.loadShaderInAssets("no_filter_vs.glsl", this);
        String fs = OpenGLUtils.loadShaderInAssets("no_filter_fs.glsl", this);
        mGLProgram = new GLProgram(vs, fs);
        mGLProgram.init();
        mGLProgram.onSurfaceChanged(mSurfaceWidth, mSurfaceHeight);
        mGLProgram.loadTexture(mBackgroundBitmaps[mCurrentBgIndex]);
        float[] fullWindowVertices = {
                0, 0, 0, 1, // 0, 0
                mSurfaceWidth, 0, 1, 1, // 1, 0
                0, mSurfaceHeight, 0, 0, // 0, 1
                mSurfaceWidth, mSurfaceHeight, 1, 0 // 1, 1
        };
        mGLProgram.putVertices(fullWindowVertices);

        GLES20.glClearColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void onDrawFrame() {
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (mGLProgram != null) {
            mGLProgram.use();
            mGLProgram.draw();
        }
    }

    @Override
    public void onGLResourceRelease() {
        if (mGLProgram != null) {
            mGLProgram.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mGLThread) {
            mGLThread.release();
        }
        if (null != mBackgroundBitmaps) {
            for (int i = 0; i < mBackgroundBitmaps.length; i++) {
                mBackgroundBitmaps[i].recycle();
                mBackgroundBitmaps[i] = null;
            }
        }
    }

}
