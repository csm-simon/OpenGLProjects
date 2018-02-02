package com.meitu.camera;

import android.content.Context;
import android.opengl.GLES20;

import com.meitu.base.base.BaseProgram;
import com.meitu.base.util.GLSLUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * 相机渲染器程序
 * <p/>
 * Created by 周代亮 on 2018/1/29.
 */

public class CameraProgram extends BaseProgram {

    private FloatBuffer mVertexBuffer;

    private ShortBuffer mIndexBuffer;

    private FloatBuffer mTextureBuffer;

    private int mProgram;

    private int mPositionHandle;

    private int mTextureCoordinateHandle;

    private int mTextureTransformHandle;

    private int mInputImageTexture;

    public CameraProgram(Context context) {
        mVertexBuffer = ByteBuffer.allocateDirect(4 * CameraMatrix.VERTEX.length)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CameraMatrix.VERTEX);
        mVertexBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(2 * CameraMatrix.INDICES.length)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(CameraMatrix.INDICES);
        mIndexBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(4 * CameraMatrix.FRAGMENT.length)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CameraMatrix.FRAGMENT);
        mTextureBuffer.position(0);

        String vertexStr = GLSLUtil.read(context, R.raw.camera_vertex);
        String fragmentStr = GLSLUtil.read(context, R.raw.camera_fragment);
        mProgram = genProgram(vertexStr, fragmentStr);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        mTextureTransformHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform");
        mInputImageTexture = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
    }

    public void draw(float[] mtx, int x) {
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        GLES20.glUniformMatrix4fv(mTextureTransformHandle, 1, false, mtx, 0);
        GLES20.glUniform1i(mInputImageTexture, x);

        GLES20.glUseProgram(mProgram);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, CameraMatrix.INDICES.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
    }

    public void destroy() {
        GLES20.glDeleteProgram(mProgram);
    }

}
