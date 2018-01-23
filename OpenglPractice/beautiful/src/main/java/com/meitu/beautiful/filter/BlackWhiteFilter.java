package com.meitu.beautiful.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.meitu.beautiful.R;

/**
 * 黑白滤镜效果
 * <p/>
 * Created by 周代亮 on 2018/1/15.
 */

public class BlackWhiteFilter extends BaseFilter {

    /**
     * 纹理
     */
    private int mSampleHandle;

    public BlackWhiteFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.black_white_fragment);
    }

    @Override
    public void init() {
        super.init();
        mSampleHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
    }

    @Override
    protected void onPreDrawFrame() {
        GLES20.glUniform1i(mSampleHandle, 0);
    }
}
