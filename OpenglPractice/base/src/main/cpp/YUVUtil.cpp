//
// Created by 周代亮 on 2018/1/23.
//

#include <jni.h>
#include <GLES2/gl2.h>
#include "com_meitu_base_util_YUVUtil.h"

/**
 * 将YUV数据转成纹理返回
 */
JNIEXPORT jint JNICALL Java_com_meitu_base_util_YUVUtil_YUV2TextureId
  (JNIEnv *env, jclass, jbyteArray array, jint width, jint height) {

    // 获取当前yuv数据转化为rgb格式
    jbyte *yuv = env->GetByteArrayElements(array, 0);
    jint size = width * height;

    int *rgba = new int[size];

    int n = 0;

    for (int i = 0; i < height; ++i) {
        for (int j = 0; j < width; ++j) {
            int y = (0xff & ((int) yuv[i * width + j]));
            int v = (0xff & ((int) yuv[size + (i >> 1) * width + (j & ~1)]));
            int u = (0xff & ((int) yuv[size + (i >> 1) * width + (j & ~1) + 1]));
            y = y < 16 ? 16 : y;

            int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
            int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
            int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

            r = r < 0 ? 0 : (r > 255 ? 255 : r);
            g = g < 0 ? 0 : (g > 255 ? 255 : g);
            b = b < 0 ? 0 : (b > 255 ? 255 : b);

            rgba[n++] = 0x000000ff | (r << 24) | (g << 16) | (b << 8);
        }
    }

    // 使用rgb数据来生成纹理
    GLuint textureId;
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba);

    delete[] rgba;

    return textureId;
}