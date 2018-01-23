package com.demo.lauyukit.opengldemo.renderer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.demo.lauyukit.opengldemo.R;
import com.demo.lauyukit.opengldemo.utils.GlUtils;
import com.demo.lauyukit.opengldemo.renderer.RectTransformation.Flip;
import com.demo.lauyukit.opengldemo.renderer.RectTransformation.Rect;
import com.demo.lauyukit.opengldemo.renderer.RectTransformation.Rotation;
import com.demo.lauyukit.opengldemo.renderer.RectTransformation.ScaleType;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 绘制矩形图片纹理的Renderer
 * <p>
 * Created by LiuYujie on 2017/12/25.
 */
public class RectanglePicRenderer implements IDestroyableRenderer {
    /**
     * Vertex Shader程序中的position的变量名
     */
    private static final String VERTEX_A_POSITION_NAME = "aPosition";
    /**
     * Vertex Shader程序中的Model View Projection Matrix的变量名
     */
    private static final String VERTEX_MVP_MATRIX_NAME = "uMVPMatrix";
    /**
     * Vertex Shader程序中的Texture Matrix变量名
     */
    private static final String VERTEX_U_TEXTURE_MATRIX = "uTexMatrix";
    /**
     * Vertex Shader程序中的Texture Coordinate变量名
     */
    private static final String VERTEX_A_TEXTURE_COORDINATE = "aTexCoord";
    /**
     * Vertex和Fragment Shader程序中的Texture Coordinate变量名，这个变量会在Vertex和Fragment的Shader中共享
     */
    private static final String VERTEX_V_TEXTURE_COORDINATE = "vTexCoord";
    /**
     * Fragment Shader程序中的Texture变量名
     */
    private static final String FRAGMENT_U_SAMPLE_TEXTURE = "uSampleTexture";
    /**
     * Vertex Shader程序
     */
    private static final String VERTEX_SHADER =
            "attribute vec4 " + VERTEX_A_POSITION_NAME + ";\n" +
            "attribute vec4 " + VERTEX_A_TEXTURE_COORDINATE + ";\n" +
            "varying vec2 " + VERTEX_V_TEXTURE_COORDINATE + ";\n" +
            "uniform mat4 " + VERTEX_MVP_MATRIX_NAME + ";\n" +
            "uniform mat4 " + VERTEX_U_TEXTURE_MATRIX + ";\n" +
            "void main() {\n" + // 将平面上的四个顶点的坐标左乘一个矩阵，得出四个顶点的最终坐标
            "gl_Position = " + VERTEX_MVP_MATRIX_NAME + " * " + VERTEX_A_POSITION_NAME + ";\n" +
            VERTEX_V_TEXTURE_COORDINATE + " = (" + VERTEX_U_TEXTURE_MATRIX + " * " + VERTEX_A_TEXTURE_COORDINATE + ").xy;\n" +
            "}";

    /**
     * Fragment Shader程序，这里控制绘制Fragment的颜色/纹理
     */
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "varying vec2 " + VERTEX_V_TEXTURE_COORDINATE + ";\n" +
            "uniform sampler2D " + FRAGMENT_U_SAMPLE_TEXTURE + ";\n" +
            "void main() {\n" +
            "gl_FragColor = texture2D(" + FRAGMENT_U_SAMPLE_TEXTURE + " , " + VERTEX_V_TEXTURE_COORDINATE + ");\n" + // 这里控制颜色 A G P R
            "}";
    /**
     * 全屏矩形四个顶点的坐标
     */
    private static final float[] FULL_RECT_VERTEX = {   // in counterclockwise order:
            -1.0f, -1.0f,   // 0 bottom left
            -1.0f, 1.0f,   // 2 top left
            1.0f, 1.0f,   // 3 top right
            1.0f, -1.0f,   // 1 bottom right
    };
    /**
     * 绘制顶点的index，由于绘制矩形需要两个三角形来拼接，所以这里就指定了第一个三角形由第0，1，2个点形成、第二个三角形由
     * 第0，2，3个点形成
     *
     * @see #FULL_RECT_VERTEX
     */
    private static final short[] VERTEX_INDEX = {
            0, 1, 2,
            0, 2, 3
    };
    /**
     * FloatBuffer，用于在native层存储4个顶点
     */
    private FloatBuffer mVertexBuffer;
    /**
     * Short buffer，用于在native层才存储{@link #VERTEX_INDEX}
     */
    private final ShortBuffer mVertexIndexBuffer;
    /**
     * FloatBuffer，存储了纹理的四个坐标
     */
    private FloatBuffer mTextureVertexBuffer;
    /**
     * {@link #VERTEX_MVP_MATRIX_NAME} 变量的句柄
     */
    private int mMVPMatrixLoc;
    /**
     * {@link #VERTEX_U_TEXTURE_MATRIX} 变量的句柄
     */
    private int mTextureMatrixLoc;
    /**
     * {@link #FRAGMENT_U_SAMPLE_TEXTURE} 变量的句柄
     */
    private int mSamplerTextureLoc;
    /**
     * GL底层纹理的索引
     */
    private int[] mTextureNames;
    /**
     * Model View Projection Matrix
     */
    private final float[] mMVPMatrix = new float[16];
    /**
     * 纹理Matrix
     */
    private final float[] mTextureMatrix = new float[16];

    private final Reference<Context> mContextRef;

    private float[] mVertices, mTextureCoordinates;

    public RectanglePicRenderer(Context context) {
        // 在native层生成short buffer，并把VERTEX_INDEX的值传入
        mVertexIndexBuffer = GlUtils.createShortBuffer(VERTEX_INDEX);
        mContextRef = new WeakReference<>(context);
    }

    public void setTransformation(RectTransformation transformation) {
        if (transformation == null) {
            throw new NullPointerException("Parameter transformation must be not null!");
        }
        // 顶点坐标范围默认值 [-1, 1]
        mVertices = Arrays.copyOf(FULL_RECT_VERTEX, FULL_RECT_VERTEX.length);
        mTextureCoordinates = new float[8];
        Rect cropRect = transformation.cropRect;
        if (cropRect == null) { // 没指定裁剪范围的话，默认全部展示
            cropRect = RectTransformation.FULL_RECT;
        }
        resolveCrop(cropRect);
        resolveFlip(transformation.flip);
        resolveRotate(transformation.rotation);
        resolveScale(transformation.scaleType, transformation.inputSize, transformation.outputSize);
        // 在native层生成float buffer，并把VERTEX的值传入
        mVertexBuffer = GlUtils.createFloatBuffer(mVertices);
        mTextureVertexBuffer = GlUtils.createFloatBuffer(mTextureCoordinates);
    }

    /**
     * 裁剪纹理，通过调整纹理坐标的取值范围来实现。纹理坐标的默认取值范围是[0, 1]，当取值范围小于该区间时，纹理就会被裁剪。
     *
     * @param cropRect 裁剪区域
     */
    private void resolveCrop(Rect cropRect) {
        float minX = cropRect.x, minY = cropRect.y;
        float maxX = minX + cropRect.width, maxY = minY + cropRect.height;
        // bottom left
        mTextureCoordinates[0] = minX;
        mTextureCoordinates[1] = minY;
        // top left
        mTextureCoordinates[2] = minX;
        mTextureCoordinates[3] = maxY;
        // top right
        mTextureCoordinates[4] = maxX;
        mTextureCoordinates[5] = maxY;
        // bottom right
        mTextureCoordinates[6] = maxX;
        mTextureCoordinates[7] = minY;
    }

    /**
     * 翻转纹理
     *
     * @param flip 翻转模式
     */
    private void resolveFlip(@Flip int flip) {
        switch (flip) {
            case Flip.FLIP_NONE:
                break;
            case Flip.FLIP_X:
                swap(mTextureCoordinates, 1, 3);
                swap(mTextureCoordinates, 5, 7);
                break;
            case Flip.FLIP_Y:
                swap(mTextureCoordinates, 0, 6);
                swap(mTextureCoordinates, 2, 4);
                break;
            case Flip.FLIP_XY:
                // flip x
                swap(mTextureCoordinates, 1, 3);
                swap(mTextureCoordinates, 5, 7);
                // flip y
                swap(mTextureCoordinates, 0, 6);
                swap(mTextureCoordinates, 2, 4);
                break;
        }
    }

    /**
     * 交换arr数组中下标为i1和i2的数据
     *
     * @param arr 数组
     * @param i1  下标1
     * @param i2  下标2
     */
    private void swap(float[] arr, int i1, int i2) {
        arr[i2] = arr[i1] + arr[i2];
        arr[i1] = arr[i2] - arr[i1];
        arr[i2] = arr[i2] - arr[i1];
    }

    /**
     * 旋转纹理，可以旋转0， 90， 180， 270度
     *
     * @param rotate 旋转角度
     */
    private void resolveRotate(@Rotation int rotate) {
        switch (rotate) {
            case Rotation.ROTATE_0:
                break;
            case Rotation.ROTATE_90: {
                float x = mTextureCoordinates[0], y = mTextureCoordinates[1];
                mTextureCoordinates[0] = mTextureCoordinates[2];
                mTextureCoordinates[1] = mTextureCoordinates[3];
                mTextureCoordinates[2] = mTextureCoordinates[4];
                mTextureCoordinates[3] = mTextureCoordinates[5];
                mTextureCoordinates[4] = mTextureCoordinates[6];
                mTextureCoordinates[5] = mTextureCoordinates[7];
                mTextureCoordinates[6] = x;
                mTextureCoordinates[7] = y;
                break;
            }
            case Rotation.ROTATE_180:
                swap(mTextureCoordinates, 0, 4);
                swap(mTextureCoordinates, 1, 5);
                swap(mTextureCoordinates, 2, 6);
                swap(mTextureCoordinates, 3, 7);
                break;
            case Rotation.ROTATE_270: {
                float x = mTextureCoordinates[0], y = mTextureCoordinates[1];
                mTextureCoordinates[0] = mTextureCoordinates[6];
                mTextureCoordinates[1] = mTextureCoordinates[7];
                mTextureCoordinates[6] = mTextureCoordinates[4];
                mTextureCoordinates[7] = mTextureCoordinates[5];
                mTextureCoordinates[4] = mTextureCoordinates[2];
                mTextureCoordinates[5] = mTextureCoordinates[3];
                mTextureCoordinates[2] = x;
                mTextureCoordinates[3] = y;
                break;
            }
        }
    }

    /**
     * 缩放纹理，目前有CENTER_CROP和FIT_CENTER还有FIT_XY三种模式，默认是FIT_XY
     *
     * @param scaleType 缩放模式
     * @param inputSize 图片纹理的尺寸
     * @param outputSize SurfaceView的宽高
     */
    @SuppressLint("SwitchIntDef")
    private void resolveScale(@ScaleType int scaleType, RectTransformation.Size inputSize, RectTransformation.Size outputSize) {
        if (scaleType == ScaleType.FIT_XY) {
            return;
        }
        int inputW = inputSize.width, inputH = inputSize.height;
        int outputW = outputSize.width, outputH = outputSize.height;
        if (inputW * outputH == inputH * outputW) {
            // 宽高比一致的话不需要做缩放处理
            return;
        }
        float inputRatio = inputW / (float) inputH;
        float outputRatio = outputW / (float) outputH;
        switch (scaleType) {
            case ScaleType.CENTER_CROP:
                if (inputRatio < outputRatio) {
                    float heightRatio = outputRatio / inputRatio;
                    mVertices[1] *= heightRatio;
                    mVertices[3] *= heightRatio;
                    mVertices[5] *= heightRatio;
                    mVertices[7] *= heightRatio;
                } else {
                    float widthRatio = inputRatio / outputRatio;
                    mVertices[0] *= widthRatio;
                    mVertices[2] *= widthRatio;
                    mVertices[4] *= widthRatio;
                    mVertices[6] *= widthRatio;
                }
                break;
            case ScaleType.FIT_CENTER:
                if (inputRatio < outputRatio) {
                    float widthRatio = inputRatio / outputRatio;
                    mVertices[0] *= widthRatio;
                    mVertices[2] *= widthRatio;
                    mVertices[4] *= widthRatio;
                    mVertices[6] *= widthRatio;
                } else {
                    float heightRatio = outputRatio / inputRatio;
                    mVertices[1] *= heightRatio;
                    mVertices[3] *= heightRatio;
                    mVertices[5] *= heightRatio;
                    mVertices[7] *= heightRatio;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 清空画布颜色
        GLES20.glClearColor(0f, 0f, 0f, 0f);
        // 创建GLSL程序并获取其句柄
        int programHandle = GlUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        GLES20.glUseProgram(programHandle);

        // 获取Vertex Shader中的aPosition句柄
        int positionLoc = GLES20.glGetAttribLocation(programHandle, VERTEX_A_POSITION_NAME);
        // 启用aPosition
        GLES20.glEnableVertexAttribArray(positionLoc);
        // 把mVertexBuffer中存储的VERTEX数组的值赋给Vertex Shader程序中的aPosition
        GLES20.glVertexAttribPointer(positionLoc, 2, GLES20.GL_FLOAT, false, 8, mVertexBuffer);
        // 获取MVP矩阵和纹理矩阵的句柄
        mMVPMatrixLoc = GLES20.glGetUniformLocation(programHandle, VERTEX_MVP_MATRIX_NAME);
        mTextureMatrixLoc = GLES20.glGetUniformLocation(programHandle, VERTEX_U_TEXTURE_MATRIX);

        // 获取纹理坐标的句柄，启用并赋值
        int textureCoordinateLoc = GLES20.glGetAttribLocation(programHandle, VERTEX_A_TEXTURE_COORDINATE);
        GLES20.glEnableVertexAttribArray(textureCoordinateLoc);
        GLES20.glVertexAttribPointer(textureCoordinateLoc, 2, GLES20.GL_FLOAT, false, 8,
                mTextureVertexBuffer);
        // 获取纹理的句柄
        mSamplerTextureLoc = GLES20.glGetUniformLocation(programHandle, FRAGMENT_U_SAMPLE_TEXTURE);

        Context context = mContextRef.get();
        if (context == null) {
            return;
        }
        mTextureNames = new int[1];
        GLES20.glGenTextures(1, mTextureNames, 0);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.whee_icon);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureNames[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        // 因为这里的对纹理的操作都是通过我们手动调整的，所以MVP矩阵和纹理矩阵都设为单位矩阵即可
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mTextureMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mTextureMatrixLoc, 1, false, mTextureMatrix, 0);
        GLES20.glUniform1i(mSamplerTextureLoc, 0);
        // 绘制三角形了
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length, GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
    }

    /**
     * 销毁创建的纹理
     */
    public void destroy() {
        if (mTextureNames != null) {
            GLES20.glDeleteTextures(mTextureNames.length, mTextureNames, 0);
        }
    }
}
