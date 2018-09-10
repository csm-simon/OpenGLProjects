package com.lauyukit.demo.glfilterdemo.filter;

import android.opengl.GLES20;
import android.support.annotation.FloatRange;

/**
 * 调整对比度的滤镜效果，对比度默认设置是1.2f，取值范围是[0, 4.0]，具体看{@link #CONTRAST_FRAGMENT_SHADER}
 * <p>
 * Created by yujieliu on 2018/3/21.
 */
public class GLContrastFilter extends GLFilter {
    private static final String CONTRAST_FRAGMENT_SHADER =
            ShaderInitializer.instance().getContrastFragmentShader();
    /**
     * 对比度值
     */
    private float mContrast;
    /**
     * {@link #CONTRAST_FRAGMENT_SHADER}中的contrast uniform location
     */
    private int mGLUniformContrast;

    public GLContrastFilter() {
        this(2.0f);
    }

    public GLContrastFilter(@FloatRange(from = 0f, to = 4.0f) float contrast) {
        super(NO_FILTER_VERTEX_SHADER, CONTRAST_FRAGMENT_SHADER);
        mContrast = contrast;
    }


    @Override
    protected void onInit() {
        super.onInit();
        mGLUniformContrast = GLES20.glGetUniformLocation(getProgram(), "contrast");
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        setContrast(mContrast);
    }

    public void setContrast(float contrast) {
        mContrast = contrast;
        setFloat(mGLUniformContrast, contrast);
    }
}
