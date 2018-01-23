package com.demo.lauyukit.opengldemo.renderer;

import android.opengl.GLSurfaceView;

/**
 * 需要显式释放资源的Renderer接口
 * <p>
 * Created by Frost on 2018/1/1.
 */
public interface IDestroyableRenderer extends GLSurfaceView.Renderer {

    void destroy();
}
