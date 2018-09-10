package com.lauyukit.demo.glfilterdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lauyukit.demo.glfilterdemo.filter.GLFilter;
import com.lauyukit.demo.glfilterdemo.filter.GLImage;
import com.lauyukit.demo.glfilterdemo.filter.GLImageView;
import com.lauyukit.demo.glfilterdemo.filter.GLSepiaFilter;

public class FilterActivity extends AppCompatActivity {
    private static final String EXTRA_FILTER_CLASS = "filter_class";

    public static Intent create(Context context, Class<? extends GLFilter> filterClass) {
        Intent intent = new Intent(context, FilterActivity.class);
        intent.putExtra(EXTRA_FILTER_CLASS, filterClass);
        return intent;
    }

    private Class<? extends GLFilter> mGLFilterClass;
    private GLImageView mGLImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        prepareView();
        initData();
        initView();
    }

    private void prepareView() {
        mGLImageView = findViewById(R.id.gl_iv);
    }

    private void initData() {
        Intent intent = getIntent();
        //noinspection unchecked
        mGLFilterClass = (Class<? extends GLFilter>) intent.getSerializableExtra(EXTRA_FILTER_CLASS);
    }

    private void initView() {
        mGLImageView.setScaleType(GLImage.ScaleType.CENTER_INSIDE);
        mGLImageView.setImage(BitmapFactory.decodeResource(getResources(), R.mipmap.fuck_sky));

        GLFilter filter = null;
        try {
            filter = mGLFilterClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        } finally {
            if (filter == null) {
                filter = new GLSepiaFilter();
            }
        }
        mGLImageView.setFilter(filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGLImageView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGLImageView.onPause();
    }
}
