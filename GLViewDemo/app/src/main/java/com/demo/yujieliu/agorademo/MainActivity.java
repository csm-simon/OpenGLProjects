package com.demo.yujieliu.agorademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.demo.yujieliu.agorademo.gl.OpenGLUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.opengl_2_btn).setOnClickListener(this);
        findViewById(R.id.opengl_3_btn).setOnClickListener(this);
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
            startActivity(GLSurfaceActivity.create(this, glVersion));
        } else {
            Dog.d("MainActivity", "start TextureActivity");
            startActivity(TextureActivity.create(this, glVersion));
        }
    }
}
