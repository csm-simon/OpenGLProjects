package com.demo.yujieliu.agorademo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.demo.yujieliu.agorademo.gl.OpenGLUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int CODE_REQUEST_PERMISSION = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.opengl_2_btn).setOnClickListener(this);
        findViewById(R.id.opengl_3_btn).setOnClickListener(this);
        requestPermission();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODE_REQUEST_PERMISSION:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.opengl_2_btn:
                startDemo(2);
                break;
            case R.id.opengl_3_btn:
                int glEsVer = OpenGLUtils.getMaxSupportedGLESVersion(this);
                if (!OpenGLUtils.supportGLES3(glEsVer)) {
                    Toast.makeText(this, "OpenGL ES 3.0 is not supported on current device!", Toast.LENGTH_SHORT).show();
                    return;
                }
                startDemo(3);
                break;
        }
    }

    private void startDemo(int glVersion) {
        RadioButton radioButton = findViewById(R.id.radio_btn_gl_surface_view);
        if (radioButton.isChecked()) {
            Dog.d("MainActivity", "start GLSurfaceActivity");
            startActivity(OffscreenRenderActivity.create(this, glVersion));
        } else {
            Dog.d("MainActivity", "start TextureActivity");
            startActivity(TextureActivity.create(this, glVersion));
        }
    }
}
