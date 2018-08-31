package com.demo.yujieliu.agorademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.demo.yujieliu.agorademo.gl.GLProgram;
import com.demo.yujieliu.agorademo.gl.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

/**
 * 利用FBO做离屏渲染的示例Activity
 */
public class FBOActivity extends AppCompatActivity {
    private GLSurfaceView mGLSurfaceView;
    private ImageView mImageView;
    private Renderer mRenderer;
    private String mVertexShader, mFragmentShader;
    private int mWaterMarkWidth, mWaterMarkHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbo);
        prepareView();
        initData();
        initView();
    }

    private void prepareView() {
        mGLSurfaceView = findViewById(R.id.fbo_gl_surface_view);
        mImageView = findViewById(R.id.fbo_offscreen_render_iv);
    }

    private void initView() {
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        mRenderer = new Renderer();
        mGLSurfaceView.setPreserveEGLContextOnPause(true);
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private void initData() {
        mVertexShader = OpenGLUtils.loadShaderInAssets("no_filter_vs.glsl", this);
        mFragmentShader = OpenGLUtils.loadShaderInAssets("no_filter_fs.glsl", this);
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
    protected void onDestroy() {
        super.onDestroy();
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.destroy();
            }
        });
    }

    /**
     * Renderer实现，按照{@link #onSurfaceCreated(GL10, EGLConfig)}, {@link #onSurfaceChanged(GL10, int, int)}
     * {@link #onDrawFrame(GL10)}的顺序阅读.
     */
    private class Renderer implements GLSurfaceView.Renderer {
        private GLProgram mOrigProgram, mWaterMarkProgram;
        private int mFBO = -1, mOffScreenRenderTex = -1;
        private int mSurfaceWidth, mSurfaceHeight;

        private void loadOrigProgram() {
            mOrigProgram = new GLProgram(mVertexShader, mFragmentShader);
            mOrigProgram.init();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.beauty1);
            mOrigProgram.loadTexture(bitmap);
            bitmap.recycle();
        }

        private void loadWaterMarkProgram() {
            mWaterMarkProgram = new GLProgram(mVertexShader, mFragmentShader);
            mWaterMarkProgram.init();
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.arrow_down_float);
            mWaterMarkWidth = bitmap.getWidth();
            mWaterMarkHeight = bitmap.getHeight();
            mWaterMarkProgram.loadTexture(bitmap);
            bitmap.recycle();
        }

        private void createFBO(int width, int height) {
            int[] holder = new int[1];
            glGenFramebuffers(1, holder, 0);
            mFBO = holder[0];
            mOffScreenRenderTex = OpenGLUtils.generateTexture(width, height);
            glBindTexture(GL_TEXTURE_2D, mOffScreenRenderTex);
            glBindFramebuffer(GL_FRAMEBUFFER, mFBO);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mOffScreenRenderTex, 0);
            glBindTexture(GL_TEXTURE_2D, 0);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        private void deleteFBO() {
            if (mFBO != -1) {
                glDeleteFramebuffers(1, new int[]{mFBO}, 0);
            }
            if (mOffScreenRenderTex != -1) {
                glDeleteTextures(1, new int[]{mOffScreenRenderTex}, 0);
            }
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // 创建Shader Program
            loadOrigProgram();
            loadWaterMarkProgram();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mSurfaceWidth = width;
            mSurfaceHeight = height;
            // 配置Shader Program的顶点、变换矩阵等
            glViewport(0, 0, width, height);
            mOrigProgram.onSurfaceChanged(width, height);
            mWaterMarkProgram.onSurfaceChanged(width, height);

            float[] origVertices = new float[]{
                    0, 0, 0, 1, // 0, 0
                    width, 0, 1, 1, // 1, 0
                    0, height, 0, 0, // 0, 1
                    width, height, 1, 0 // 1, 1
            };
            mOrigProgram.putVertices(origVertices);

            float[] waterMarkVertices = new float[]{
                    0, 0, 0, 1, // 0, 0
                    mWaterMarkWidth, 0, 1, 1, // 1, 0
                    0, mWaterMarkHeight, 0, 0, // 0, 1
                    mWaterMarkWidth, mWaterMarkHeight, 1, 0 // 1, 1
            };
            mWaterMarkProgram.putVertices(waterMarkVertices);
            // 创建FBO
            createFBO(width, height);
            glClearColor(1, 0, 0, 1);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            glClear(GL_COLOR_BUFFER_BIT);
            // 上屏渲染，只渲染beauty1图片
            mOrigProgram.use();
            mOrigProgram.draw();
            // 绑定FrameBuffer，开始离屏渲染
            glBindFramebuffer(GL_FRAMEBUFFER, mFBO);
            // 先画beauty1
            mOrigProgram.use();
            mOrigProgram.draw();
            // 开启alpha混合
            glEnable(GL_BLEND);
            glBlendFunc(GL_ONE, GL_ONE);
            // 绘制"水印"
            mWaterMarkProgram.use();
            mWaterMarkProgram.draw();
            // 关闭alpha混合
            glDisable(GL_BLEND);
            // 创建ByteBuffer准备读取FBO绘制的内容
            ByteBuffer buffer = ByteBuffer.allocateDirect(mSurfaceWidth * mSurfaceHeight * 4)
                    .order(ByteOrder.nativeOrder());
            buffer.position(0);
            // 读取FBO绘制内容
            glReadPixels(0, 0, mSurfaceWidth, mSurfaceHeight, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            // 将读取到的像素转为Bitmap
            final Bitmap bitmap = Bitmap.createBitmap(mSurfaceWidth, mSurfaceHeight, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 离屏渲染的图片读取出来并显示到下方ImageView
                    mImageView.setImageBitmap(bitmap);
                }
            });
            // 解绑FrameBuffer，想一下，如果没有这一句，会发生什么事情？为什么？
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }

        private void destroy() {
            deleteFBO();
            if (mOrigProgram != null) {
                mOrigProgram.destroy();
            }

            if (mWaterMarkProgram != null) {
                mWaterMarkProgram.destroy();
            }
        }
    }
}
