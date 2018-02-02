package com.meitu.texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.AttributeSet;

import com.meitu.base.base.BaseGLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 纹理加载示例
 * <p/>
 * Created by 周代亮 on 2018/1/26.
 */

public class TextureGLSurfaceView extends BaseGLSurfaceView {

    /**
     * 顶点坐标缓存
     */
    private FloatBuffer mVertexBuffer;
    /**
     * 顶点坐标索引缓存
     */
    private ShortBuffer mIndexBuffer;
    /**
     * 纹理坐标缓存
     */
    private FloatBuffer mTextureBuffer;
    /**
     * 对应的纹理Program
     */
    private TextureProgram mTextureProgram;
    /**
     * 对应的纹理id
     */
    private int mTextureId;

    public TextureGLSurfaceView(Context context) {
        super(context);
    }

    public TextureGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();

        mVertexBuffer = ByteBuffer.allocateDirect(CMatrix.VERTEXS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CMatrix.VERTEXS);
        mVertexBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(CMatrix.INDEXS.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(CMatrix.INDEXS);
        mIndexBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(CMatrix.TEXTURES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(CMatrix.TEXTURES);
        mTextureBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0, 0, 0, 0);

        mTextureProgram = new TextureProgram(getContext());

        int[] texNames = new int[1];
        // 创建纹理
        GLES20.glGenTextures(1, texNames, 0);
        // 全局引用起来
        mTextureId = texNames[0];
        // 将新建的纹理和编号绑定起来
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        // 设置纹理的参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.timg);
        // 将图片数据拷贝到纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        // 加载到纹理中了，就可以回收图片数据了。
        bitmap.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        mTextureProgram.useProgram();
        mTextureProgram.setTextureParams(mVertexBuffer, mTextureBuffer);
        mTextureProgram.setSamplerParam(0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, CMatrix.INDEXS.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);
    }
}
