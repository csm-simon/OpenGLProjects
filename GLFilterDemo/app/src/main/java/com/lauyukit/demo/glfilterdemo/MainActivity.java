package com.lauyukit.demo.glfilterdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lauyukit.demo.glfilterdemo.R;
import com.lauyukit.demo.glfilterdemo.filter.GLContrastFilter;
import com.lauyukit.demo.glfilterdemo.filter.GLFilter;
import com.lauyukit.demo.glfilterdemo.filter.GLGrayFilter;
import com.lauyukit.demo.glfilterdemo.filter.GLMosaicFilter;
import com.lauyukit.demo.glfilterdemo.filter.GLSepiaFilter;
import com.lauyukit.demo.glfilterdemo.filter.ShaderInitializer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mNoFilterBtn, mContrastBtn, mGrayBtn, mMosaicBtn, mSepiaBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepareView();
        initData();
        initView();
    }

    private void prepareView() {
        mNoFilterBtn = findViewById(R.id.main_no_filter_btn);
        mContrastBtn = findViewById(R.id.main_contrast_filter_btn);
        mGrayBtn = findViewById(R.id.main_gray_filter_btn);
        mMosaicBtn = findViewById(R.id.main_mosaic_filter_btn);
        mSepiaBtn = findViewById(R.id.main_sepia_filter_btn);
    }

    private void initView() {
        mNoFilterBtn.setOnClickListener(this);
        mContrastBtn.setOnClickListener(this);
        mGrayBtn.setOnClickListener(this);
        mMosaicBtn.setOnClickListener(this);
        mSepiaBtn.setOnClickListener(this);
    }

    private void initData() {
        ShaderInitializer.instance().init(this);
    }

    @Override
    public void onClick(View v) {
        Class<? extends GLFilter> filterClass = GLFilter.class;
        switch (v.getId()) {
            case R.id.main_contrast_filter_btn:
                filterClass = GLContrastFilter.class;
                break;
            case R.id.main_gray_filter_btn:
                filterClass = GLGrayFilter.class;
                break;
            case R.id.main_mosaic_filter_btn:
                filterClass = GLMosaicFilter.class;
                break;
            case R.id.main_sepia_filter_btn:
                filterClass = GLSepiaFilter.class;
                break;
            case R.id.main_no_filter_btn:
            default:
                break;
        }
        startActivity(FilterActivity.create(this, filterClass));
    }
}
