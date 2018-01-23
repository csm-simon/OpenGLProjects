package com.meitu.animation.particle;

import android.graphics.Color;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 对象系统
 * <p/>
 * Created by 周代亮 on 2018/1/18.
 */

public class ObjSystem {
    /**
     * 一个对象所占据的长度
     */
    private final int SIZE = 10;
    /**
     * 数据缓存
     */
    private FloatBuffer mFloatBuffer;
    /**
     * 对象数据数组
     */
    private float[] mObjArray;
    /**
     * 当前有多少个对象
     */
    private int count = 0;
    /**
     * 下一个
     */
    private int nextCount = 0;

    public ObjSystem(int n) {
        mObjArray = new float[n * SIZE];
        mFloatBuffer = ByteBuffer.allocateDirect(mObjArray.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mObjArray);
        mFloatBuffer.position(0);
    }

    /**
     * 添加对象
     *
     * @param position    位置
     * @param color       颜色
     * @param direction   方向
     * @param currentTime 创建时间
     */
    public void addObj(float[] position, int color, float[] direction, float currentTime) {
        // 当前放入数据开始的位置
        int start = nextCount * SIZE;
        int index = start;
        // 如果没有达到最大，数据就加一
        int max = mObjArray.length / SIZE;
        if (count < max) {
            count++;
        }
        // 如果达到最大，就开始回收利用
        nextCount++;
        if (nextCount == max) {
            nextCount = 0;
        }

        // 添加位置数据
        mObjArray[index++] = position[0];
        mObjArray[index++] = position[1];
        mObjArray[index++] = position[2];
        // 添加颜色数据
        mObjArray[index++] = Color.red(color) / 255f;
        mObjArray[index++] = Color.green(color) / 255f;
        mObjArray[index++] = Color.blue(color) / 255f;
        // 添加方向数据
        mObjArray[index++] = direction[0];
        mObjArray[index++] = direction[1];
        mObjArray[index++] = direction[2];
        // 添加创建时间
        mObjArray[index] = currentTime;

        // 将数据放入缓冲区
        mFloatBuffer.position(start);
        mFloatBuffer.put(mObjArray, start, SIZE);
        mFloatBuffer.position(0);
    }

    /**
     * 设置对应Location的值
     *
     * @param positionLocation   位置
     * @param colorLocation      颜色
     * @param directionLocation  方向
     * @param createTimeLocation 创建时间
     */
    public void setAttribute(int positionLocation,
                             int colorLocation,
                             int directionLocation,
                             int createTimeLocation) {
        int offset = 0;
        setAttributeLocation(offset, positionLocation, 3);
        offset += 3;
        setAttributeLocation(offset, colorLocation, 3);
        offset += 3;
        setAttributeLocation(offset, directionLocation, 3);
        offset += 3;
        setAttributeLocation(offset, createTimeLocation, 1);
    }

    /**
     * 设置对应属性的数据
     *
     * @param offset   偏移量
     * @param location 属性句柄
     * @param size     属性的大小
     */
    private void setAttributeLocation(int offset, int location, int size) {
        mFloatBuffer.position(offset);
        GLES20.glVertexAttribPointer(location, size, GLES20.GL_FLOAT, false, 40, mFloatBuffer);
        GLES20.glEnableVertexAttribArray(location);
        mFloatBuffer.position(0);
    }

    /**
     * 绘制点
     */
    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, count);
    }

}
