package com.demo.lauyukit.opengldemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;

import com.demo.lauyukit.opengldemo.R;
import com.demo.lauyukit.opengldemo.renderer.IDestroyableRenderer;
import com.demo.lauyukit.opengldemo.renderer.RectTransformation;
import com.demo.lauyukit.opengldemo.renderer.RectanglePicRenderer;
import com.demo.lauyukit.opengldemo.utils.TextureUtils;
import com.demo.lauyukit.opengldemo.utils.ToastUtils;

public class RectangleDemoActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1024;

    private IDestroyableRenderer mRenderer;
    private Context mContext;
    private GLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rectangle_demo);
        prepareView();
        initView();
        initListener();
        mContext = this;
    }

    private void prepareView() {
        mGLSurfaceView = findViewById(R.id.triangle_demo_glsv);
    }

    private void initView() {
        // 用OpenGL ES 2.0
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        RectanglePicRenderer renderer = new RectanglePicRenderer(this);
        RectTransformation transformation = new RectTransformation();
        transformation.setFlip(RectTransformation.Flip.FLIP_X);
        transformation.setRotation(RectTransformation.Rotation.ROTATE_270);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.whee_icon, opt);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        transformation.setScale(RectTransformation.ScaleType.FIT_CENTER,
                new RectTransformation.Size(opt.outWidth, opt.outHeight),
                new RectTransformation.Size(dm.widthPixels, dm.heightPixels));
        transformation.setCropRect(new RectTransformation.Rect(0.2f, 0.2f, 0.8f, 0.8f));
        renderer.setTransformation(transformation);
        mRenderer = renderer;
        mGLSurfaceView.setRenderer(mRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    private void initListener() {
        findViewById(R.id.rectangle_save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    saveTexture();
                } else {
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            }
        });
    }

    private void saveTexture() {
        String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "texture_rotated.png";
        TextureUtils.saveTexture(mGLSurfaceView, directory, fileName, new TextureUtils.SaveTextureCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(mContext, "Save texture success!");
                    }
                });
            }

            @Override
            public void onFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show(mContext, "Save texture failed!");
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveTexture();
                } else {
                    ToastUtils.show(mContext, "Write external permission denied!");
                }
                break;
            default:
                break;
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRenderer.destroy();
    }
}
