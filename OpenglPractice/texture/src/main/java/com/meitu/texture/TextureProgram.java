package com.meitu.texture;

import android.content.Context;
import android.opengl.GLES20;

import com.meitu.base.base.BaseProgram;
import com.meitu.base.util.GLSLUtil;

import java.nio.FloatBuffer;

/**
 * 纹理加载的Program
 * <p/>
 * Created by 周代亮 on 2018/1/26.
 */

public class TextureProgram extends BaseProgram {

    /**
     * 对应的program
     */
    private int mProgram;
    /**
     * 位置信息对应的句柄
     */
    private int mPositionHandle;
    /**
     * 纹理坐标对应的句柄
     */
    private int mTexCoordHandle;
    /**
     * 纹理单元对应的句柄
     */
    private int mTexSamplerHandle;

    public TextureProgram(Context context) {
        String vertexStr = GLSLUtil.read(context, R.raw.vertex);
        String fragmentStr = GLSLUtil.read(context, R.raw.fragment);
        mProgram = genProgram(vertexStr, fragmentStr);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
    }

    /**
     * 设置顶点坐标参数和纹理坐标参数
     */
    public void setTextureParams(FloatBuffer mVertexBuffer, FloatBuffer mTexVertex) {
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTexVertex);
    }

    public void useProgram() {
        GLES20.glUseProgram(mProgram);
    }

    public void setSamplerParam(int x) {
        GLES20.glUniform1i(mTexSamplerHandle, x);
    }
}
