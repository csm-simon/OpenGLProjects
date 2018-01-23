package com.meitu.opengl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.meitu.animation.widget.AnimGLSurfaceView;

public class AnimationActivity extends AppCompatActivity {

    /**
     * 火焰动画界面
     */
    private AnimGLSurfaceView mAnimGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnimGLSurfaceView = new AnimGLSurfaceView(this);
        setContentView(mAnimGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnimGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAnimGLSurfaceView.onPause();
    }
}
