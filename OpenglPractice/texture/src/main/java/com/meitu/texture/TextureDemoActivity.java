package com.meitu.texture;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class TextureDemoActivity extends AppCompatActivity {

    private TextureGLSurfaceView mTextureGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextureGLSurfaceView = new TextureGLSurfaceView(this);
        setContentView(mTextureGLSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTextureGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureGLSurfaceView.onPause();
    }
}
