package com.demo.yujieliu.agorademo.offscreenrender;

import com.demo.yujieliu.agorademo.gl.GLProgram;
import com.demo.yujieliu.agorademo.gl.OpenGLUtils;

import java.nio.Buffer;

import static android.opengl.GLES20.*;

/**
 * 使用FrameBuffer实现的离屏渲染Renderer。每一帧绘制结束都会通过{@link OnDrawFrameCallback#onFrameDrawn()}回调
 * 出去，可以在该回调内通过{@link android.opengl.GLES20#glReadPixels(int, int, int, int, int, int, Buffer)}
 * 读取渲染内容。
 * <p>
 * Created by yujieliu on 2018/7/31.
 */
public class FrameBufferOffScreenRenderer extends AbsOffScreenRenderer {
    private GLProgram mShaderProgram;
    /**
     * FrameBufferObject以及挂载在该FrameBuffer上的纹理
     */
    private int mFBO = -1, mTex = -1;
    private static final float[] VERTICES = {
            -1f, -1f, 0f, 0f,
            1f, -1f, 1f, 0f,
            -1f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f,
    };

    public FrameBufferOffScreenRenderer(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void initOffScreenRenderEnvironment(Object nativeWindow, int surfaceWidth, int surfaceHeight) {
        int[] holder = new int[1];
        // 创建FrameBuffer
        glGenFramebuffers(1, holder, 0);
        mFBO = holder[0];
        // 创建用于离屏渲染的纹理，相当于画布
        mTex = OpenGLUtils.generateTexture(surfaceWidth, surfaceHeight);
        // 绑定FrameBuffer，在解绑或者绑定其他FrameBuffer前，当用到GL_FRAMEBUFFER时，都会应用到mFBO上
        glBindFramebuffer(GL_FRAMEBUFFER, mFBO);
        // 将上面生成的纹理挂载在FrameBuffer的color buffer上
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mTex, 0);
        // 解绑mFBO，避免影响到外部绘制
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    protected void initShaderProgram(int textureId, int surfaceWidth, int surfaceHeight, String vertexShader, String fragmentShader) {
        mShaderProgram = new GLProgram(vertexShader, fragmentShader);
        mShaderProgram.init();
        // 设置绘制的内容
        mShaderProgram.setTextureId(textureId);
        mShaderProgram.putVertices(VERTICES);
    }

    @Override
    public void onSurfaceChanged(int surfaceWidth, int surfaceHeight) {
        glViewport(0, 0, surfaceWidth, surfaceHeight);
    }

    @Override
    public void drawFrame() {
        // 绑定mFBO，开始离屏渲染
        glBindFramebuffer(GL_FRAMEBUFFER, mFBO);
        glClearColor(1f, 0f, 0f, 1f);
        // 清空纹理
        glClear(GL_COLOR_BUFFER_BIT);
        // 执行绘制逻辑
        mShaderProgram.use();
        mShaderProgram.draw();
        if (mOnDrawFrameCallback != null) {
            // 通知外部绘制完毕
            mOnDrawFrameCallback.onFrameDrawn();
        }
        // 解绑mFBO，避免影响到外部绘制
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void release() {
        if (mFBO != -1) {
            glDeleteFramebuffers(1, new int[]{mFBO}, 0);
            mFBO = -1;
        }
        if (mTex != -1) {
            glDeleteTextures(1, new int[]{mTex}, 0);
            mTex = -1;
        }
        if (mShaderProgram != null) {
            mShaderProgram.destroy();
            mShaderProgram = null;
        }
    }
}
