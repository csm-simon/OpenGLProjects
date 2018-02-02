package com.meitu.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.meitu.base.base.BaseGLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 相机预览
 * <p/>
 * Created by 周代亮 on 2018/1/29.
 */

public class CameraGLSurfaceView extends BaseGLSurfaceView implements SurfaceTexture.OnFrameAvailableListener {

    private SurfaceTexture mSurfaceTexture;

    private CameraProgram mCameraProgram;

    public CameraGLSurfaceView(Context context) {
        super(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 1);

        mCameraProgram = new CameraProgram(getContext());

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

        mSurfaceTexture = new SurfaceTexture(textures[0]);

        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        CameraHelper.getInstance().startCamera(mSurfaceTexture);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        mSurfaceTexture.updateTexImage();

        float[] mtx = new float[16];
        mSurfaceTexture.getTransformMatrix(mtx);

        mCameraProgram.draw(mtx, 0);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    public void destroy() {
        CameraHelper.getInstance().releaseCamera();
        mCameraProgram.destroy();
    }
}
