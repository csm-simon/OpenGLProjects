package com.lauyukit.demo.glfilterdemo.filter;

/**
 * 灰色滤镜效果，具体看{@link #GRAY_FILTER_FRAGMENT_SHADER}
 * <p>
 * Created by yujieliu on 2018/3/21.
 */
public class GLGrayFilter extends GLFilter {
    private static final String GRAY_FILTER_FRAGMENT_SHADER =
            ShaderInitializer.instance().getGrayColorFragmentShader();

    public GLGrayFilter() {
        super(NO_FILTER_VERTEX_SHADER, GRAY_FILTER_FRAGMENT_SHADER);
    }
}
