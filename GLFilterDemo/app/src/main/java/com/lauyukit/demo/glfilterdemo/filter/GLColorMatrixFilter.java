package com.lauyukit.demo.glfilterdemo.filter;

import android.opengl.GLES20;
import android.support.annotation.FloatRange;

/**
 * 调整颜色的滤镜效果基类，该滤镜会把所有像素的颜色值都与一个4x4矩阵相乘得到每一个像素的新颜色, 并将新颜色与原颜色进行混合。
 * 混合比例取决于{@link #mIntensity}，最终颜色 = ( 1 - intensity) * 原颜色 + intensity * 新颜色
 * <p>
 * Created by yujieliu on 2018/3/21.
 */
public class GLColorMatrixFilter extends GLFilter {
    private static final String COLOR_MATRIX_FRAGMENT_SHADER = ShaderInitializer.instance().getColorMatrixFragmentShader();
    /**
     * 新颜色占的权重
     */
    private float mIntensity;
    /**
     * 颜色处理矩阵，4*4大小
     */
    private float[] mColorMatrix;
    /**
     * Shader中ColorMatrix和Intensity的location
     */
    private int mGLUniformColorMatrix, mGLUniformIntensity;

    /**
     * 默认效果，不对颜色做任何操作
     */
    public GLColorMatrixFilter() {
        this(1f, new float[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        });
    }

    /**
     * 构造方法
     *
     * @param intensity   intensity，指处理后颜色的权重
     * @param colorMatrix 颜色矩阵，4x4
     */
    public GLColorMatrixFilter(@FloatRange(from = 0f, to = 1f) float intensity, float[] colorMatrix) {
        super(NO_FILTER_VERTEX_SHADER, COLOR_MATRIX_FRAGMENT_SHADER);
        validateMatrix(colorMatrix);
        mIntensity = intensity;
        mColorMatrix = colorMatrix;
    }

    @Override
    protected void onInit() {
        super.onInit();
        int programId = getProgram();
        mGLUniformColorMatrix = GLES20.glGetUniformLocation(programId, "colorMatrix");
        mGLUniformIntensity = GLES20.glGetUniformLocation(programId, "intensity");
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        setIntensity(mIntensity);
        setColorMatrix(mColorMatrix);
    }

    public void setIntensity(float intensity) {
        mIntensity = intensity;
        setFloat(mGLUniformIntensity, intensity);
    }

    public void setColorMatrix(float[] colorMatrix) {
        validateMatrix(colorMatrix);
        mColorMatrix = colorMatrix;
        setUniformMatrix4f(mGLUniformColorMatrix, colorMatrix);
    }

    protected void validateMatrix(float[] matrix) {
        if (matrix == null || matrix.length < 16) {
            throw new IllegalArgumentException("Color matrix should be of length 16!");
        }
    }

}
