package com.meitu.animation.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;

import com.meitu.animation.R;
import com.meitu.animation.helper.MatrixHelper;
import com.meitu.animation.particle.ObjShooter;
import com.meitu.animation.particle.ObjSystem;
import com.meitu.animation.program.ObjProgram;
import com.meitu.base.base.BaseGLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * GL动画的SurfaceView
 * <p/>
 * Created by 周代亮 on 2018/1/18.
 */
public class AnimGLSurfaceView extends BaseGLSurfaceView {

    /**
     * 对象program
     */
    private ObjProgram mObjProgram;
    /**
     * 对象发射器
     */
    private ObjShooter mObjShooter;
    /**
     * 对象系统
     */
    private ObjSystem mObjSystem;
    /**
     * 开始时间
     */
    private long mGlobalStartTime;
    /**
     * 投影矩阵
     */
    private final float[] mProjectionMatrix = new float[16];
    /**
     * 视图矩阵
     */
    private final float[] mViewMatrix = new float[16];
    /**
     * 视图投影矩阵
     */
    private final float[] mViewProjectionMatrix = new float[16];
    /**
     * 纹理id
     */
    private int mTextureId;

    public AnimGLSurfaceView(Context context) {
        super(context);
    }

    public AnimGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        mObjProgram = new ObjProgram(getContext());
        mObjSystem = new ObjSystem(1000);
        mObjShooter = new ObjShooter(new float[]{0f, 0f, 0f}, 0xFFFFFFFF, new float[]{0f, 0.5f, 0f}, 1f);
        mGlobalStartTime = System.nanoTime();

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mTextureId = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dot);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(mProjectionMatrix, 45, (float) width / height, 1f, 10f);

        setIdentityM(mViewMatrix, 0);
        translateM(mViewMatrix, 0, 0f, -1.5f, -5f);
        multiplyMM(mViewProjectionMatrix, 0, mProjectionMatrix, 0,
                mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        float currentTime = (System.nanoTime() - mGlobalStartTime) / 1_000_000_000f;
        mObjShooter.shoot(mObjSystem, currentTime, 5);

        mObjProgram.useProgram();
        mObjProgram.setUniforms(mViewProjectionMatrix, currentTime, mTextureId);
        mObjSystem.setAttribute(
                mObjProgram.getPositionLocation(),
                mObjProgram.getColorLocation(),
                mObjProgram.getDirectionLocation(),
                mObjProgram.getCreateTimeLocation());
        mObjSystem.draw();
    }

}
