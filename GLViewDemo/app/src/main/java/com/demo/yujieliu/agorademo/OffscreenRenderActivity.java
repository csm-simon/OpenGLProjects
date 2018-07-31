package com.demo.yujieliu.agorademo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;

import com.demo.yujieliu.agorademo.gl.OpenGLUtils;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 给基哥验证接入美拍直播的demo页，接受textureID和Surface来做离屏渲染，把textureId的纹理渲染到Surface上
 */
public class OffscreenRenderActivity extends AppCompatActivity
        implements GLSurfaceView.Renderer, OffScreenRenderer.OnDrawFrameCallback {
    private static final String EXTRA_GL_VERSION = "gl_ver";
    private static final int DEFAULT_GL_VERSION = 2;
    private GLSurfaceView mGLSurfaceView;
    private int mGLVersion;
    private OffScreenRenderer mOffScreenRenderer;
    private Surface mOffScreenRenderSurface;
    private ByteBuffer mReadPixelBuffer;
    private int mSurfaceWidth, mSurfaceHeight;

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
        mOffScreenRenderer = new OffScreenRenderer(vs, fs);
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

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        initOffscreenRenderer(width, height);
    }

    private void initOffscreenRenderer(int surfaceWidth, int surfaceHeight) {
        int offscreenRenderTexture = OpenGLUtils.generateTexture(surfaceWidth, surfaceHeight);
        SurfaceTexture surfaceTexture = new SurfaceTexture(offscreenRenderTexture);
        surfaceTexture.setDefaultBufferSize(surfaceWidth, surfaceHeight);
        // fixme 这里替换为编码器的Surface
        mOffScreenRenderSurface = new Surface(surfaceTexture);
        int photoTexture = OpenGLUtils.loadTexture(BitmapFactory.decodeResource(getResources(), R.drawable.beauty1), OpenGLUtils.NO_TEXTURE);
        mOffScreenRenderer.init(photoTexture, mOffScreenRenderSurface, surfaceWidth, surfaceHeight);
        mReadPixelBuffer = ByteBuffer.allocateDirect(surfaceWidth * surfaceHeight * 4);
        mSurfaceWidth = surfaceWidth;
        mSurfaceHeight = surfaceHeight;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 这里调用离屏渲染
        mOffScreenRenderer.saveEGLState();
        mOffScreenRenderer.makeCurrent();
        mOffScreenRenderer.drawFrame();
        mOffScreenRenderer.restoreEGLState();
    }

    @Override
    public void onFrameDrawn() {
        // 调试代码，读取离屏渲染的东西看看是否正常
        mReadPixelBuffer.position(0);
        GLES20.glReadPixels(0, 0, mSurfaceWidth, mSurfaceHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mReadPixelBuffer);
        Bitmap bm = Bitmap.createBitmap(mSurfaceWidth, mSurfaceHeight, Bitmap.Config.ARGB_8888);
        bm.copyPixelsFromBuffer(mReadPixelBuffer);
        // 可以这里断点看渲染的图片
        bm.recycle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mOffScreenRenderer) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mOffScreenRenderer.release();
                    mOffScreenRenderSurface.release();
                    mOffScreenRenderer = null;
                }
            });
        }
    }

}
