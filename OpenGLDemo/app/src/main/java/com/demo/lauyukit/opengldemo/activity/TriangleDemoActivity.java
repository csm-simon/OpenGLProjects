package com.demo.lauyukit.opengldemo.activity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.demo.lauyukit.opengldemo.R;
import com.demo.lauyukit.opengldemo.program.DrawFanProgram;
import com.demo.lauyukit.opengldemo.program.DrawRectangleProgram;
import com.demo.lauyukit.opengldemo.program.DrawStripRectangleProgram;
import com.demo.lauyukit.opengldemo.program.DrawTransformedRectangleProgram;
import com.demo.lauyukit.opengldemo.program.DrawTriangleProgram;
import com.demo.lauyukit.opengldemo.renderer.SimpleRenderer;

public class TriangleDemoActivity extends AppCompatActivity {
    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triangle_demo);
        prepareView();
        initView();
    }

    private void prepareView() {
        mGLSurfaceView = findViewById(R.id.triangle_demo_glsv);
    }

    /**
     * 想画什么就自己选
     * @see DrawFanProgram
     * @see DrawRectangleProgram
     * @see DrawStripRectangleProgram
     * @see DrawTriangleProgram
     */
    private void initView() {
        // 用OpenGL ES 2.0
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.setRenderer(new SimpleRenderer(new DrawTriangleProgram()));
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGLSurfaceView.onPause();
    }
}
