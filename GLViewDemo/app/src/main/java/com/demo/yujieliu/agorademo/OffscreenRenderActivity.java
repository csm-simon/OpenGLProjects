package com.demo.yujieliu.agorademo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.demo.yujieliu.agorademo.gl.GLProgram;
import com.demo.yujieliu.agorademo.gl.OpenGLUtils;
import com.demo.yujieliu.agorademo.offscreenrender.AbsOffScreenRenderer;
import com.demo.yujieliu.agorademo.offscreenrender.FrameBufferOffScreenRenderer;
import com.demo.yujieliu.agorademo.utils.BitmapUtils;

import java.io.File;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 用FrameBuffer实现离屏渲染的Activity，离屏渲染和上屏渲染的分别是{@link R.drawable#beauty1}和{@link R.drawable#beauty2} ,
 * 离屏渲染的图片保存路径详见{@link #onFrameDrawn()}
 */
public class OffscreenRenderActivity extends AppCompatActivity
        implements GLSurfaceView.Renderer, AbsOffScreenRenderer.OnDrawFrameCallback {
    private static final String TAG = "OffscreenRenderActivity";
    private static final String EXTRA_GL_VERSION = "gl_ver";
    private static final int DEFAULT_GL_VERSION = 2;

    private static final float[] VERTICES = {
            -1f, -1f, 0f, 1f,
            1f, -1f, 1f, 1f,
            -1f, 1f, 0f, 0f,
            1f, 1f, 1f, 0f,
    };

    private GLSurfaceView mGLSurfaceView;
    private int mGLVersion;
    private ByteBuffer mReadPixelBuffer;
    private int mSurfaceWidth, mSurfaceHeight;
    /**
     * 离屏渲染Renderer
     */
    private AbsOffScreenRenderer mOffScreenRenderer;
    /**
     * 上屏渲染绘制Shader Program
     */
    private GLProgram mDrawProgram;
    /**
     * 绘制到屏幕上的纹理
     */
    private int mOnScreenTex;

    public static Intent create(Context context, int glVersion) {
        Intent intent = new Intent(context, OffscreenRenderActivity.class);
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
        String vs = OpenGLUtils.loadShaderInAssets("no_filter_vs.glsl", this);
        String fs = OpenGLUtils.loadShaderInAssets("no_filter_fs.glsl", this);
        mOffScreenRenderer = new FrameBufferOffScreenRenderer(vs, fs);
        mDrawProgram = new GLProgram(vs, fs);
    }

    private void initViews() {
        mGLSurfaceView.setEGLContextClientVersion(mGLVersion);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void initListeners() {
        mOffScreenRenderer.setOnDrawFrameCallback(this);
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
        mDrawProgram.init();
        mDrawProgram.putVertices(VERTICES);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.beauty2);
        mOnScreenTex = OpenGLUtils.loadTexture(bm, OpenGLUtils.NO_TEXTURE);
        // 上屏渲染绘制beauty2
        mDrawProgram.setTextureId(mOnScreenTex);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        initOffscreenRenderer(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    private void initOffscreenRenderer(int surfaceWidth, int surfaceHeight) {
        // 离屏渲染绘制beauty1
        int photoTexture = OpenGLUtils.loadTexture(BitmapFactory.decodeResource(getResources(), R.drawable.beauty1), OpenGLUtils.NO_TEXTURE);
        mOffScreenRenderer.init(photoTexture, null, surfaceWidth, surfaceHeight);
        mReadPixelBuffer = ByteBuffer.allocateDirect(surfaceWidth * surfaceHeight * 4);
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mDrawProgram.use();
        mDrawProgram.draw();
        mOffScreenRenderer.drawFrame();
    }

    private boolean mSaveBitmap = false;

    @Override
    public void onFrameDrawn() {
        if (mSaveBitmap) {
            return;
        }
        mSaveBitmap = true;
        // 调试代码，读取离屏渲染的东西看看是否正常
        mReadPixelBuffer.position(0);
        GLES20.glReadPixels(0, 0, mSurfaceWidth, mSurfaceHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mReadPixelBuffer);
        Bitmap bm = Bitmap.createBitmap(mSurfaceWidth, mSurfaceHeight, Bitmap.Config.ARGB_8888);
        bm.copyPixelsFromBuffer(mReadPixelBuffer);
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String fileName = BitmapUtils.saveBitmap(dir, bm);
        String filePath = dir + File.separator + fileName;
        Dog.i(TAG, filePath);
        // 可以这里断点看渲染的图片
        bm.recycle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mOffScreenRenderer != null) {
                    mOffScreenRenderer.release();
                    mOffScreenRenderer = null;
                }
                if (mDrawProgram != null) {
                    mDrawProgram.destroy();
                    mDrawProgram = null;
                }
                if (mOnScreenTex != 0) {
                    GLES20.glDeleteTextures(1, new int[]{mOnScreenTex}, 0);
                    mOnScreenTex = 0;
                }
            }
        });
    }

}
