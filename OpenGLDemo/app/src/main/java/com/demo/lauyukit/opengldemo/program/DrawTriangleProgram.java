package com.demo.lauyukit.opengldemo.program;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 绘制三角形的GL程序
 * <p>
 * Created by LiuYujie on 2018/1/16.
 */
public class DrawTriangleProgram implements DrawProgram {
    /**
     * Vertex Shader程序中的position的变量名
     */
    private static final String VERTEX_A_POSITION_NAME = "aPosition";

    /**
     * Vertex Shader程序，定义了三角形的三个顶点位置{@link #VERTEX_A_POSITION_NAME}
     */
    private static final String VERTEX_SHADER =
            "attribute vec4 " + VERTEX_A_POSITION_NAME + ";\n" +
                    "void main() {\n" + // 将平面上的三个顶点的坐标左乘一个矩阵，得出三个顶点的最终坐标
                    "gl_Position = " + VERTEX_A_POSITION_NAME + ";\n" +
                    "}";

    /**
     * Fragment Shader程序，这里控制绘制Fragment的颜色
     */
    private static final String FRAGMENT_SHADER = "precision mediump float;\n" +
            "void main() {\n" +
            "gl_FragColor = vec4(1, 0, 0, 1);\n" + // 这里控制颜色 R G B A
            "}";
    /**
     * 三角形三个顶点的坐标，分别是x, y, z坐标
     */
    private static final float[] VERTEX = {
            0f, 1f, 0f,
            -0.5f, -1f, 0f,
            1f, -1f, 0f
    };
    /**
     * FloatBuffer，用于在native层存储3个顶点
     */
    private final FloatBuffer mVertexBuffer;

    public DrawTriangleProgram() {
        // 在native层生成float buffer，并把VERTEX的值传入
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * SIZEOF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);
    }

    @Override
    public void createProgram(GL10 gl, EGLConfig config) {
        // 创建GLSL程序并获取其句柄
        int programHandle = GLES20.glCreateProgram();
        // 加载vertex和fragment的shader
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        // 绑定vertex和fragment shader到GLSL程序中
        GLES20.glAttachShader(programHandle, vertexShader);
        GLES20.glAttachShader(programHandle, fragmentShader);
        // 链接并启用GLSL程序
        GLES20.glLinkProgram(programHandle);
        GLES20.glUseProgram(programHandle);
        // 从GLSL程序中获取Vertex Shader里的aPosition的句柄
        int positionLoc = GLES20.glGetAttribLocation(programHandle, VERTEX_A_POSITION_NAME);
        // 启用aPosition
        GLES20.glEnableVertexAttribArray(positionLoc);
        // 把mVertexBuffer中存储的VERTEX数组的值赋给Vertex Shader程序中的aPosition
        // 因为有三个顶点，所以size是3，stride的值一般是 顶点数x数据类型占用的byte数，float是4个bytes，所以stride = 12
        GLES20.glVertexAttribPointer(positionLoc, 3, GLES20.GL_FLOAT, false, 12, mVertexBuffer);
    }

    @Override
    public void onSizeChanged(GL10 gl, int width, int height) {
        // do nothing
    }

    @Override
    public void draw(GL10 gl) {
        // 绘制三角形了
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }

    private static int loadShader(int type, String shaderCode) {
        // 创建Shader程序并获取其句柄
        int shader = GLES20.glCreateShader(type);
        // 指定Shader源码并绑定
        GLES20.glShaderSource(shader, shaderCode);
        // 编译Shader程序
        GLES20.glCompileShader(shader);
        return shader;
    }
}
