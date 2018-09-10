package com.lauyukit.demo.glfilterdemo.filter;

/**
 * 棕色滤镜，可以营造老照片的效果。继承自{@link GLColorMatrixFilter}。
 * <p>
 * Created by yujieliu on 2018/3/21.
 */
public class GLSepiaFilter extends GLColorMatrixFilter {

    public GLSepiaFilter() {
        this(1.0f);
    }

    /**
     * 构造方法
     *
     * @param intensity 棕色的权重
     */
    public GLSepiaFilter(final float intensity) {
        super(intensity, new float[]{
                0.3588f, 0.7044f, 0.1368f, 0.0f,
                0.2990f, 0.5870f, 0.1140f, 0.0f,
                0.2392f, 0.4696f, 0.0912f, 0.0f,
                0f, 0f, 0f, 1.0f
        });
    }
}
