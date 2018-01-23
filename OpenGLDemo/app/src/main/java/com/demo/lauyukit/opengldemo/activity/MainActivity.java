package com.demo.lauyukit.opengldemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.demo.lauyukit.opengldemo.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fuckView();
    }

    private void fuckView() {
        findViewById(R.id.triangle_btn).setOnClickListener(this);
        findViewById(R.id.rectangle_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.triangle_btn:
                startActivity(new Intent(this, TriangleDemoActivity.class));
                break;
            case R.id.rectangle_btn:
                startActivity(new Intent(this, RectangleDemoActivity.class));
                break;
            default:
                break;
        }
    }
}
