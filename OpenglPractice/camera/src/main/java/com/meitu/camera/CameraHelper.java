package com.meitu.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.io.IOException;
import java.util.List;

/**
 * 相机帮助类
 * <p/>
 * Created by 周代亮 on 2018/1/29.
 */

public class CameraHelper {

    /**
     * CameraHelper单例
     */
    private static volatile CameraHelper mCameraHelper;
    /**
     * 相机
     */
    private Camera mCamera;

    /**
     * 避免外部实例化
     */
    private CameraHelper() {

    }

    /**
     * 使用双重检测获取单例对象
     *
     * @return CameraHelper的单例
     */
    public static CameraHelper getInstance() {
        if (mCameraHelper == null) {
            synchronized (CameraHelper.class) {
                if (mCameraHelper == null) {
                    mCameraHelper = new CameraHelper();
                }
            }
        }
        return mCameraHelper;
    }

    /**
     * 启动相机预览
     */
    public void startCamera(SurfaceTexture surfaceTexture) {
        try {
            // 打开前置摄像头(0-后置，1-前置)
            mCamera = Camera.open(0);
            // 设置预览纹理到surfaceTexture
            mCamera.setPreviewTexture(surfaceTexture);
            // 获取相机最小预览大小
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size temp = sizes.get(0);
            for (int i = 1; i < sizes.size(); i++) {
                if (temp.width < sizes.get(i).width) {
                    temp = sizes.get(i);
                }
            }
            // 设置预览图片的宽高
            parameters.setPreviewSize(temp.width, temp.height);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        mCamera.stopPreview();
        mCamera.release();
    }

}
