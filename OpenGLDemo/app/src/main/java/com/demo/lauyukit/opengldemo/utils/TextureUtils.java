package com.demo.lauyukit.opengldemo.utils;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * 纹理相关的Utility
 * <p>
 * Created by Frost on 2017/12/27.
 */
public class TextureUtils {

    /**
     * 将{@param view}绘制的图像截图保存到指定路径{@param savePath}上。
     *
     * @param view     GLSurfaceView
     * @param savePath 保存文件的路径
     * @param fileName 文件名
     * @param cb       保存结果的回调
     */
    public static void saveTexture(final GLSurfaceView view, final String savePath,
                                   final String fileName, final SaveTextureCallback cb) {
        final int width = view.getWidth();
        final int height = view.getHeight();
        view.queueEvent(new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffer = ByteBuffer.allocate(width * height * 4);
                buffer.position(0);
                GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                // 因为GLES20.glReadPixels的方法读取的图像是倒置的，所以需要手动做180度旋转
                Bitmap rotatedBitmap = rotateBitmapByXAxis(bitmap, 180f);
                bitmap.recycle();

                if (BitmapUtils.saveBitmapToFile(rotatedBitmap, savePath + File.separator + fileName)) {
                    if (cb != null) {
                        cb.onSuccess();
                    }
                } else {
                    if (cb != null) {
                        cb.onFailed();
                    }
                }
                if (rotatedBitmap != null) {
                    rotatedBitmap.recycle();
                }
            }
        });
    }

    public interface SaveTextureCallback {
        @WorkerThread
        void onSuccess();

        @WorkerThread
        void onFailed();
    }

    /**
     * 生成一张旋转Bitmap
     *
     * @param bitmap       Bitmap
     * @param rotateDegree 旋转角度
     * @return 旋转后的Bitmap
     */
    @Nullable
    private static Bitmap rotateBitmapByXAxis(Bitmap bitmap, float rotateDegree) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        Matrix matrix = new Matrix();
        Camera camera = new Camera();
        camera.rotateX(rotateDegree);
        camera.getMatrix(matrix);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
