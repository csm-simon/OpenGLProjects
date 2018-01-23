package com.meitu.animation.program;

import android.content.Context;
import android.opengl.GLES20;

import com.meitu.animation.R;
import com.meitu.base.base.BaseProgram;
import com.meitu.base.util.GLSLUtil;

/**
 * 对象发射器的渲染 program
 * <p/>
 * Created by 周代亮 on 2018/1/18.
 */

public class ObjProgram extends BaseProgram {

    /**
     * 投影矩阵
     */
    private static final String MATRIX = "uMatrix";
    /**
     * 当前时间
     */
    private static final String TIME = "uTime";
    /**
     * 对象位置
     */
    private static final String POSITION = "aPosition";
    /**
     * 对象颜色
     */
    private static final String COLOR = "aColor";
    /**
     * 对象的方向
     */
    private static final String DIRECTION = "aDirection";
    /**
     * 对象的创建时间
     */
    private static final String CREATE_TIME = "aCreateTime";
    /**
     * 纹理单元
     */
    private static final String TEXUTRE_UNIT = "uTextureUnit";
    /**
     * 对应的Program
     */
    private int mProgram;
    /**
     * 对象位置的句柄
     */
    private int mPositionLocation;
    /**
     * 对象颜色的句柄
     */
    private int mColorLocation;
    /**
     * 对象方向的句柄
     */
    private int mDirectionLocation;
    /**
     * 对象创建时间的句柄
     */
    private int mCreateTimeLocation;
    /**
     * 对象投影矩阵的句柄
     */
    private int mMatrixLocation;
    /**
     * 对象当前时间的句柄
     */
    private int mTimeLocation;
    /**
     * 纹理单元句柄
     */
    private int mSampler2D;

    public ObjProgram(Context context) {
        // 生成program
        mProgram = genProgram(
                GLSLUtil.read(context, R.raw.vertex),
                GLSLUtil.read(context, R.raw.fragment));

        // 绑定句柄
        bindLocation();
    }

    /**
     * 绑定Location
     */
    private void bindLocation() {
        mPositionLocation = GLES20.glGetAttribLocation(mProgram, POSITION);
        mColorLocation = GLES20.glGetAttribLocation(mProgram, COLOR);
        mDirectionLocation = GLES20.glGetAttribLocation(mProgram, DIRECTION);
        mCreateTimeLocation = GLES20.glGetAttribLocation(mProgram, CREATE_TIME);

        mMatrixLocation = GLES20.glGetUniformLocation(mProgram, MATRIX);
        mTimeLocation = GLES20.glGetUniformLocation(mProgram, TIME);
        mSampler2D = GLES20.glGetUniformLocation(mProgram, TEXUTRE_UNIT);
    }

    /**
     * 设置Uniforms
     *
     * @param matrix      投影矩阵
     * @param currentTime 当前时间
     * @param textureId   纹理id
     */
    public void setUniforms(float[] matrix, float currentTime, int textureId) {
        GLES20.glUniformMatrix4fv(mMatrixLocation, 1, false, matrix, 0);
        GLES20.glUniform1f(mTimeLocation, currentTime);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(mSampler2D, 0);
    }

    /**
     * 使用当前当program
     */
    public void useProgram() {
        GLES20.glUseProgram(mProgram);
    }

    /**
     * 获取位置句柄
     *
     * @return 返回生成的位置句柄
     */
    public int getPositionLocation() {
        return mPositionLocation;
    }

    /**
     * 获取颜色句柄
     *
     * @return 颜色句柄
     */
    public int getColorLocation() {
        return mColorLocation;
    }

    /**
     * 获取方向句柄
     *
     * @return 方向句柄
     */
    public int getDirectionLocation() {
        return mDirectionLocation;
    }

    /**
     * 获取创建时间的句柄
     *
     * @return 创建时间的句柄
     */
    public int getCreateTimeLocation() {
        return mCreateTimeLocation;
    }

}
