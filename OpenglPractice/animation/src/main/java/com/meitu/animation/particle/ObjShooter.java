package com.meitu.animation.particle;

import android.opengl.Matrix;

import java.util.Random;

/**
 * 对象发射器
 * <p/>
 * Created by 周代亮 on 2018/1/18.
 */

public class ObjShooter {

    /**
     * 发射器的发射位置
     */
    private float[] mPosition;
    /**
     * 发射器发射物品的颜色
     */
    private int mColor;
    /**
     * 发射器的发射方向
     */
    private float[] mDirection;
    /**
     * 发射器的发射速度
     */
    private float mSpeed;
    /**
     * 随机变量
     */
    private Random mRandom;

    public ObjShooter(float[] position, int color, float[] direction, float speed) {
        this.mPosition = position;
        this.mColor = color;
        this.mDirection = direction;
        this.mSpeed = speed;
        mRandom = new Random();
    }

    public void shoot(ObjSystem objSystem, float startTime, int n) {
        for (int i = 0; i < n; i++) {

            float[] rotationMatrix = new float[16];
            Matrix.setIdentityM(rotationMatrix, 0);
            //setRotateEulerM 旋转矩阵  随机改变值
            Matrix.setRotateEulerM(rotationMatrix, 0,
                    (mRandom.nextFloat() - 0.5f) * 5f,
                    (mRandom.nextFloat() - 0.5f) * 5f,
                    (mRandom.nextFloat() - 0.5f) * 5f);

            float[] resultVector = new float[4];
            Matrix.multiplyMV(
                    resultVector, 0,
                    rotationMatrix, 0,
                    new float[]{mDirection[0], mDirection[1], mDirection[2], 0},0);

            // 调整粒子速度
            float speedAdjustment = 1 + mRandom.nextFloat() * mSpeed;
            float[] afterDirection = {
                    resultVector[0] * speedAdjustment,
                    resultVector[1] * speedAdjustment,
                    resultVector[2] * speedAdjustment};
            objSystem.addObj(mPosition, mColor, afterDirection, startTime);
        }
    }

}
