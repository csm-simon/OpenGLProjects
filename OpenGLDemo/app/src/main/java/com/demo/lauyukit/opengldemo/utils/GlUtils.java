package com.demo.lauyukit.opengldemo.utils;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * GL Utilities
 * <p>
 * Created by LiuYujie on 2018/1/12.
 */
public class GlUtils {
    private static final String TAG = "GlUtils";
    /**
     * 一个float类型占用4个bytes
     */
    private static final int SIZEOF_FLOAT = 4;
    /**
     * 一个short类型占用2个bytes
     */
    private static final int SIZEOF_SHORT = 2;

    private GlUtils() {
        throw new RuntimeException("Stub!");
    }

    /**
     * 给定Vertex和Fragment的Shader代码，创建GL程序并返回程序句柄
     *
     * @return GL程序句柄，如果创建失败就返回0
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        // 先加载Vertex和Fragment的Shader
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            GLog.e(TAG, "Failed to create program");
            return 0;
        }
        // 绑定vertex和fragment shader到GL程序中
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader VertexShader");
        GLES20.glAttachShader(program, fragmentShader);
        checkGlError("glAttachShader FragmentShader");
        // link GL程序
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        // 检查link状态，失败的话就中止GL程序并返回0
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLog.e(TAG, "Failed to link program: ");
            GLog.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    /**
     * 加载Shader并返回其句柄
     *
     * @param shaderType Shader类型，一种是Vertex Shader，另一种是Fragment Shader
     * @param source     Shader代码字符串
     * @return Shader句柄，如果加载失败了，会返回0
     * @see GLES20#GL_VERTEX_SHADER
     * @see GLES20#GL_FRAGMENT_SHADER
     */
    public static int loadShader(int shaderType, String source) {
        // 创建Shader程序并获取shader句柄
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader# type:" + shaderType);
        // 指定Shader源码并绑定
        GLES20.glShaderSource(shader, source);
        // 编译Shader程序
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLog.e(TAG, "Could not compile shader " + shaderType + ":");
            GLog.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    /**
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            GLog.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * 给定float数组创建FloatBuffer并返回
     *
     * @return FloatBuffer
     */
    public static FloatBuffer createFloatBuffer(float[] arr) {
        FloatBuffer ret = ByteBuffer.allocateDirect(arr.length * SIZEOF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(arr);
        ret.position(0);
        return ret;
    }

    /**
     * 给定short数组创建ShortBuffer并返回
     *
     * @return ShortBuffer
     */
    public static ShortBuffer createShortBuffer(short[] arr) {
        ShortBuffer ret = ByteBuffer.allocateDirect(arr.length * SIZEOF_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(arr);
        ret.position(0);
        return ret;
    }
}
