package com.meitu.base.base;

import android.opengl.GLES20;

/**
 * 基础Program
 * <p/>
 * Created by 周代亮 on 2018/1/23.
 */

public class BaseProgram {

    /**
     * 生成program
     *
     * @param vertexGlsl   顶点GLSL对应的字符串
     * @param fragmentGlsl 片段GLSL对应的字符串
     * @return 生成的program
     */
    protected int genProgram(String vertexGlsl, String fragmentGlsl) {
        int program = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexGlsl);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentGlsl);
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        return program;
    }

    /**
     * 加载Shader并返回
     *
     * @param type shader的类型：GL_VERTEX_SHADER 和 GL_FRAGMENT_SHADER
     * @param glsl 对应GLSL写的顶点和片段所对应的字符串
     * @return shader
     */
    protected int loadShader(int type, String glsl) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, glsl);
        GLES20.glCompileShader(shader);
        return shader;
    }

}
