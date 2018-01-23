package com.demo.lauyukit.opengldemo.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 绘制矩形形的Renderer
 * <p>
 * Created by LiuYujie on 2017/12/25.
 */
public class RectangleRenderer implements GLSurfaceView.Renderer {
    /**
     * Vertex Shader程序中的Position的变量名
     */
    private static final String VERTEX_A_POSITION_NAME = "aPosition";
    /**
     * Vertex Shader程序中的MVP Matrix变量名
     */
    private static final String VERTEX_MVP_MATRIX_NAME = "uMVPMatrix";
    /**
     * Vertex Shader程序
     */
    private static final String VERTEX_SHADER =
            "attribute vec4 " + VERTEX_A_POSITION_NAME + ";\n" +
                    "uniform mat4 " + VERTEX_MVP_MATRIX_NAME + ";\n" +
                    "void main() {\n" + // 将平面上的三个顶点的坐标左乘一个矩阵，得出三个顶点的最终坐标
                    "gl_Position = " + VERTEX_MVP_MATRIX_NAME + " * " + VERTEX_A_POSITION_NAME + ";\n" +
                    "}";

    /**
     * Fragment Shader程序，这里控制绘制Fragment的颜色
     */
    private static final String FRAGMENT_SHADER = "precision mediump float;\n" +
            "void main() {\n" +
            "gl_FragColor = vec4(0.5, 1, 0, 1);\n" + // 这里控制颜色 R G B A
            "}";
    /**
     * 矩形四个顶点的坐标
     */
    private static final float[] VERTEX = {   // in counterclockwise order:
            1, 1, 0,   // top right
            -1, 1, 0,  // top left
            -1, -1, 0, // bottom left
            1, -1, 0,  // bottom right
    };
    /**
     * 绘制顶点的index，由于绘制矩形需要两个三角形来拼接，所以这里就指定了第一个三角形由第0，1，2个点形成、第二个三角形由
     * 第0，2，3个点形成
     *
     * @see #VERTEX
     */
    private static final short[] VERTEX_INDEX = {
            0, 1, 2,
            0, 2, 3
    };
    /**
     * FloatBuffer，用于在native层存储4个顶点{@link #VERTEX}
     */
    private final FloatBuffer mVertexBuffer;
    /**
     * Short buffer，用于在native层才存储{@link #VERTEX_INDEX}
     */
    private final ShortBuffer mVertexIndexBuffer;
    /**
     * {@link #VERTEX_MVP_MATRIX_NAME} 变量的句柄
     */
    private int mMVPMatrixLoc;
    /**
     * MVP矩阵
     */
    private float[] mMVPMatrix = new float[16];

    public RectangleRenderer() {
        // 在native层生成float buffer，并把VERTEX的值传入
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);
        // 在native层生成short buffer，并把VERTEX_INDEX的值传入
        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 清空画布颜色
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        // 创建GLSL程序并获取其句柄
        int program = GLES20.glCreateProgram();
        // 加载vertex和fragment的shader
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        // 绑定vertex和fragment shader到GLSL程序中
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        // 链接并启用GLSL程序
        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);
        // 从GLSL程序中获取Vertex Shader里的aPosition的句柄
        int positionLoc = GLES20.glGetAttribLocation(program, VERTEX_A_POSITION_NAME);
        // 启用aPosition
        GLES20.glEnableVertexAttribArray(positionLoc);
        // 把mVertexBuffer中存储的VERTEX数组的值赋给Vertex Shader程序中的aPosition
        GLES20.glVertexAttribPointer(positionLoc, 3, GLES20.GL_FLOAT, false, 12, mVertexBuffer);
        // 获取MVP矩阵的句柄
        mMVPMatrixLoc = GLES20.glGetUniformLocation(program, VERTEX_MVP_MATRIX_NAME);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // 计算透视和平移矩阵并赋值到mMVPMatrix上
        // fovy是y轴的field of view, 也就是视角大小，视角越大，可见范围越广
        // aspect是宽高比
        // zNear和zFar分别是视锥体近平面和远平面的z轴坐标
        Matrix.perspectiveM(mMVPMatrix, 0, 45, (float) width / height, .1f, 100f);
        // 由于历史原因，Matrix.perspectiveM 会让 z 轴方向倒置，所以左乘投影矩阵之后，顶点z坐标需要在 -zNear~-zFar 范围内才会可见
        // 所以下面计算往z轴负方向平移5f，使得顶点z坐标在 -100f~-0.1f之间
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -5f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 把MVP矩阵的值通过mMVPMatrixLoc赋值给VertexShader中的uMVPMatrix矩阵变量
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mMVPMatrix, 0);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        // 绘制矩形。因为一个矩形可以以对角线划分为两个全等三角形，所以有两个点是共用的。由于我们的VERTEX只存了四个点，如果
        // 用上面的语句绘制图形的话，只能画出一个三角形，因此我们需要额外用VERTEX_INDEX来告诉OpenGL，我们要画两组三角形，
        // 第一个三角形用第0, 1, 2个点，第二个三角形用第0, 2, 3个点，这样，OpenGL就会用画两个三角形，视觉上就能看到这
        // 两个三角形拼成的矩形了
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length, GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
    }

    private static int loadShader(int type, String shaderCode) {
        // 创建Shader程序并获取句柄
        int shader = GLES20.glCreateShader(type);
        // 指定Shader源码并绑定
        GLES20.glShaderSource(shader, shaderCode);
        // 编译Shader程序
        GLES20.glCompileShader(shader);
        return shader;
    }
}
