package com.polar.browser.vclibrary.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class GlideStackTransform extends BitmapTransformation {

    private Context mContext;
    private int mStackDrawable;

    public GlideStackTransform(Context context, int stackDrawable) {
        super(context);
        this.mContext = context;
        this.mStackDrawable = stackDrawable;
    }

    @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return circleCrop(pool, toTransform);
    }

    private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        Bitmap src = BitmapFactory.decodeResource(mContext.getResources(), mStackDrawable);
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap result = pool.get(width, height, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        Bitmap temp = source;
        Bitmap resizedBm = resizeBitmap(temp, width, height);

        Paint mPaint = new Paint();
        Rect mRect = new Rect(0, 0, resizedBm.getWidth(), resizedBm.getHeight());
        Canvas canvas = new Canvas(result);

        int x = (resizedBm.getWidth() - src.getWidth()) / 2;
        int y = (resizedBm.getHeight() - src.getHeight()) / 2;

        canvas.drawBitmap(src, x, y, mPaint);

        //设置图像的叠加模式
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //绘制图像
        canvas.drawBitmap(resizedBm, mRect, mRect, mPaint);
        return result;
    }

    @Override public String getId() {
        return getClass().getName();
    }

    /**等比例缩放bitmap*/
    private Bitmap resizeBitmap(Bitmap bm, int newWidth ,int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }
}