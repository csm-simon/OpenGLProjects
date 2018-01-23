package com.meitu.opengl;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.meitu.beautiful.BeautifulCameraActivity;

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
                startActivity(new Intent(MainActivity.this, BeautifulCameraActivity.class));
            }
        });

        // 点击进入火焰动画
        findViewById(R.id.btn_animation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AnimationActivity.class));
            }
        });
    }
}