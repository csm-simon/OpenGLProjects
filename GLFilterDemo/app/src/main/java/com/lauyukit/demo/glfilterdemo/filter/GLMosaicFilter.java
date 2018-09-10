package com.lauyukit.demo.glfilterdemo.filter;

import android.opengl.GLES20;

/**
 * 马赛克效果滤镜，其实这算滤镜吗？实现看{@link #PIXELATION_FRAGMENT_SHADER}
 * <p>
 * Created by yujieliu on 2018/3/21.
 */
public class GLMosaicFilter extends GLFilter {
    private static final String PIXELATION_FRAGMENT_SHADER =
            ShaderInitializer.instance().getMosaicFragmentShader();
    /**
     * 马赛克像素大小
     */
    private float mPixel;
    /**
     * Surface窗口宽和高的倒数以及马赛克像素大小的uniform location
     */
    private int mGLUniformImageWidthFactor, mGLUniformImageHeightFactor, mGLUniformPixel;

    public GLMosaicFilter() {
        this(20.0f);
    }

    /**
     * 构造方法
     * @param pixel 马赛克大小，如果值为2，说明单个马赛克大小是2X2像素
     */
    public GLMosaicFilter(float pixel) {
        super(NO_FILTER_VERTEX_SHADER, PIXELATION_FRAGMENT_SHADER);
        mPixel = pixel;
    }

    @Override
    protected void onInit() {
        super.onInit();
        int program = getProgram();
        mGLUniformPixel = GLES20.glGetUniformLocation(program, "pixel");
        mGLUniformImageWidthFactor = GLES20.glGetUniformLocation(program, "imageWidthFactor");
        mGLUniformImageHeightFactor = GLES20.glGetUniformLocation(program, "imageHeightFactor");
    }

    @Override
    protected void onInitialized() {
        super.onInitialized();
        setPixel(mPixel);
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        setFloat(mGLUniformImageWidthFactor, 1f / width);
        setFloat(mGLUniformImageHeightFactor, 1f / height);
    }

    public void setPixel(float pixel) {
        mPixel = pixel;
        setFloat(mGLUniformPixel, pixel);
    }
}
