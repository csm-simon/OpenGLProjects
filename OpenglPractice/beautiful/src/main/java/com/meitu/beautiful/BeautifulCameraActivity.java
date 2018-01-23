package com.meitu.beautiful;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.meitu.beautiful.adapter.EffectAdapter;
import com.meitu.beautiful.filter.BlackWhiteFilter;
import com.meitu.beautiful.widget.CameraGLSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class BeautifulCameraActivity extends AppCompatActivity
        implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    /**
     * GLSurfaceView 用于显示照相机补获的数据
     */
    private CameraGLSurfaceView mCameraGLSurfaceView;
    /**
     * 拍照按钮
     */
    private ImageButton mTakePhotoBtn;
    /**
     * 美颜按钮
     */
    private TextView mBeautifulTv;
    /**
     * 滤镜按钮
     */
    private TextView mEffectTv;
    /**
     * 供选择的列表
     */
    private RecyclerView mSelectRv;
    /**
     * 滤镜适配器，提供选择不同的滤镜功能
     */
    private EffectAdapter mEffectAdapter;
    /**
     * 调节美颜效果
     */
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置为沉侵式状态栏
        initFullScreen();

        // 初始化GLSurfaceView
        mCameraGLSurfaceView = new CameraGLSurfaceView(this);
        setContentView(mCameraGLSurfaceView);

        // 添加拍照控制层
        initControllerLayout();
    }

    /**
     * 初始化屏幕显示状态
     */
    private void initFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        setTheme(R.style.CameraTheme);
    }

    /**
     * 初始化拍照控制栏
     */
    private void initControllerLayout() {
        View mainLayout = getLayoutInflater().inflate(R.layout.activity_beautiful_camera, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        addContentView(mainLayout, params);

        // 绑定控件并添加点击事件
        findViews();
        addEvents();

        initRecyclerView();
    }

    /**
     * 绑定对应的View
     */
    private void findViews() {
        mTakePhotoBtn = findViewById(R.id.ibtn_take_photo);
        mBeautifulTv = findViewById(R.id.tv_beautiful);
        mEffectTv = findViewById(R.id.tv_effect);
        mSelectRv = findViewById(R.id.rv_select);
        mSeekBar = findViewById(R.id.sb_beautiful_level);
    }

    /**
     * 给各控件添加点击事件
     */
    private void addEvents() {
        mTakePhotoBtn.setOnClickListener(this);
        mBeautifulTv.setOnClickListener(this);
        mEffectTv.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mBeautifulTv) {
            // 美颜
            if (mSelectRv.getVisibility() != View.GONE) {
                mSelectRv.setVisibility(View.GONE);
            }
            mSeekBar.setVisibility(mSeekBar.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        } else if (v == mEffectTv) {
            // 滤镜
            if (mSeekBar.getVisibility() != View.GONE) {
                mSeekBar.setVisibility(View.GONE);
            }
            mSelectRv.setVisibility(mSelectRv.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        List<String> data = new ArrayList<>();
        data.add("无");
        data.add("黑白");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mSelectRv.setLayoutManager(linearLayoutManager);
        mEffectAdapter = new EffectAdapter(data);
        mSelectRv.setAdapter(mEffectAdapter);

        mEffectAdapter.setOnItemClickListener(new EffectAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(int position) {
                if (position == 0) {
                    mCameraGLSurfaceView.setFilter(null);
                } else if (position == 1) {
                    mCameraGLSurfaceView.setFilter(new BlackWhiteFilter(BeautifulCameraActivity.this));
                }
            }
        });
    }

    /**
     * SeekBar 的回调
     *
     * @param seekBar  对应的seekBar对象
     * @param progress 目前的进度
     * @param fromUser 是否被用户初始化设置导致的回调
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mCameraGLSurfaceView.setBeautifulLevel(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraGLSurfaceView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraGLSurfaceView.destroy();
    }
}
