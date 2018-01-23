package com.demo.lauyukit.opengldemo.renderer;

import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 控制矩形纹理平移、缩放、裁剪还有旋转、翻转的类
 * <p>
 * Created by LiuYujie on 2018/1/13.
 */
public class RectTransformation {
    static final Rect FULL_RECT = new Rect(0, 0, 1f, 1f);

    @Rotation
    int rotation = Rotation.ROTATE_0;
    @Flip
    int flip = Flip.FLIP_NONE;

    Rect cropRect;

    @ScaleType int scaleType = ScaleType.FIT_XY;
    Size inputSize, outputSize;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Rotation.ROTATE_0, Rotation.ROTATE_90, Rotation.ROTATE_180, Rotation.ROTATE_270})
    public @interface Rotation {
        int ROTATE_0 = 0;
        int ROTATE_90 = 90;
        int ROTATE_180 = 180;
        int ROTATE_270 = 270;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Flip.FLIP_NONE, Flip.FLIP_X, Flip.FLIP_Y, Flip.FLIP_XY})
    public @interface Flip {
        int FLIP_NONE = ' ';
        int FLIP_X = 'x';
        int FLIP_Y = 'y';
        int FLIP_XY = 'z';
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ScaleType.FIT_XY, ScaleType.CENTER_CROP, ScaleType.FIT_CENTER})
    public @interface ScaleType {
        int FIT_XY = ' ';
        /**
         * 短边填充满，长边等比例缩放，超出部分两端裁掉。也就是让顶点坐标取值范围超过[-1, 1]
         */
        int CENTER_CROP = 'c';
        /**
         * 长边填充满，短边等比例缩放，不足部分两端留黑边。也就是让顶点坐标取值范围小于等于[-1, 1]
         */
        int FIT_CENTER = 'f';
    }

    public RectTransformation() {
        super();
    }

    public void setRotation(@Rotation int rotation) {
        this.rotation = rotation;
    }

    public void setFlip(@Flip int flip) {
        this.flip = flip;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = new Rect(cropRect);
    }

    public void setScale(@ScaleType int scaleType, Size inputSize, Size outputSize) {
        this.scaleType = scaleType;
        this.inputSize = new Size(inputSize);
        this.outputSize = new Size(outputSize);
    }

    public static final class Rect {
        final float x, y, width, height;

        public Rect(@FloatRange(from = 0f, to = 1f) float x, @FloatRange(from = 0f, to = 1f) float y,
                    @FloatRange(from = 0f, to = 1f) float width, @FloatRange(from = 0f, to = 1f) float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        Rect(Rect rect) {
            this.x = rect.x;
            this.y = rect.y;
            this.width = rect.width;
            this.height = rect.height;
        }
    }

    public static final class Size {
        final int width, height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        Size(Size size) {
            this.width = size.width;
            this.height = size.height;
        }
    }
}
