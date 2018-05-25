package com.demo.yujieliu.agorademo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.demo.yujieliu.agorademo.gl.EGLCompat;
import com.demo.yujieliu.agorademo.gl.GLProgram;
import com.demo.yujieliu.agorademo.gl.OpenGLUtils;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private static final String EXTRA_GL_VERSION = "gl_ver";
    private static final int DEFAULT_GL_VERSION = 2;
    private GLSurfaceView mGLSurfaceView;
    private int mGLVersion;
    private GLProgram mGLProgram;
    private Bitmap[] mBackgroundBitmaps;
    private int mCurrentBgIndex;

    public static Intent create(Context context, int glVersion) {
        Intent intent = new Intent(context, GLSurfaceActivity.class);
        intent.putExtra(EXTRA_GL_VERSION, glVersion);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_surface);

        findViews();
        initData();
        initViews();
        initListeners();
    }

    private void findViews() {
        mGLSurfaceView = findViewById(R.id.gl_surface_view);
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

    private void initViews() {
        mGLSurfaceView.setEGLContextClientVersion(mGLVersion);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        String vs = OpenGLUtils.loadShaderInAssets("no_filter_vs.glsl", this);
        String fs = OpenGLUtils.loadShaderInAssets("no_filter_fs.glsl", this);
        mGLProgram = new GLProgram(vs, fs);
    }

    private void initListeners() {
        findViewById(R.id.switch_bg_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLSurfaceView.queueEvent(new Runnable() {
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
                mGLSurfaceView.requestRender();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGLSurfaceView.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mGLProgram.init();
        mGLProgram.loadTexture(mBackgroundBitmaps[mCurrentBgIndex]);
        GLES20.glClearColor(1f, 1f, 1f, 1f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mGLProgram.onSurfaceChanged(width, height);
        float[] fullWindowVertices = {
                0, 0, 0, 1, // 0, 0
                width, 0, 1, 1, // 1, 0
                0, height, 0, 0, // 0, 1
                width, height, 1, 0 // 1, 1
        };
        mGLProgram.putVertices(fullWindowVertices);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mGLProgram.use();
        mGLProgram.draw();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mGLProgram) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mGLProgram.destroy();
                    mGLProgram = null;
                }
            });
        }
    }

}
