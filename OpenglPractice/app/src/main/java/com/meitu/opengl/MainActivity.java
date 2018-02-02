package com.meitu.opengl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.meitu.beautiful.BeautifulCameraActivity;
import com.meitu.camera.CameraActivity;
import com.meitu.texture.TextureDemoActivity;

/**
 * 工程主界面
 *
 * @author 周代亮 2018/1/23
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 点击进入美颜相机
        findViewById(R.id.btn_beautiful_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(BeautifulCameraActivity.class);
            }
        });

        // 点击进入火焰动画
        findViewById(R.id.btn_animation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AnimationActivity.class);
            }
        });

        // 点击进入纹理加载
        findViewById(R.id.btn_texture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(TextureDemoActivity.class);
            }
        });

        findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(CameraActivity.class);
            }
        });
    }

    /**
     * 跳转到目标Activity
     * @param cls 目标Activity的class
     */
    private void startActivity(Class<?> cls) {
        startActivity(new Intent(MainActivity.this, cls));
    }
}