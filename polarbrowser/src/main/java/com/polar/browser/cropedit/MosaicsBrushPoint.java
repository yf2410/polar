package com.polar.browser.cropedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.utils.BitmapUtils;
import com.polar.browser.utils.DensityUtil;

public class MosaicsBrushPoint extends View {

	public static final int TYPE_MIN = -1;
	public static final int TYPE_MID = 0;
	public static final int TYPE_MAX = 1;
	private float mSize;
	private int mColor;
	/**
	 * 马赛克颗粒大小
	 **/
	private int mType;
	private Paint mPaint;

	private int mWidth;

	public MosaicsBrushPoint(Context context) {
		this(context, null);
	}

	public MosaicsBrushPoint(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mType = TYPE_MID;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mSize = 30;
		mColor = getResources().getColor(R.color.color_box_item_7);
		mPaint.setColor(mColor);
		mWidth = DensityUtil.dip2px(JuziApp.getInstance(), 34);
	}

	public float getSize() {
		return mSize;
	}

	public void setSize(float size) {
		this.mSize = size;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		this.mColor = color;
		mPaint.setColor(mColor);
	}

	public void setType(int type) {
		this.mType = type;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Bitmap bitmap = null;
		if (mType == TYPE_MIN) {
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_mosaics_min);
		} else if (mType == TYPE_MID) {
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_mosaics_mid);
		} else {
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_mosaics_max);
		}
		Bitmap rectBmp = createCircleImage(bitmap, (int) (mWidth * mSize / 100));
		int left = (mWidth - rectBmp.getWidth()) / 2;
		canvas.drawBitmap(rectBmp, left, left, mPaint);
		BitmapUtils.recycleBitmap(bitmap);
		BitmapUtils.recycleBitmap(rectBmp);
	}

	/**
	 * 根据原图和变长绘制圆形图片
	 *
	 * @param source
	 * @param min
	 * @return
	 */
	private Bitmap createCircleImage(Bitmap source, int min) {
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		Bitmap target = Bitmap.createBitmap(min, min, Config.ARGB_8888);
		/**
		 * 产生一个同样大小的画布
		 */
		Canvas canvas = new Canvas(target);
		/**
		 * 首先绘制圆形
		 */
		canvas.drawCircle(min / 2, min / 2, min / 2, paint);
		/**
		 * 使用SRC_IN
		 */
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		int offset = 0;
		if (min < 20) {
			offset = 5;
		}
		/**
		 * 绘制图片
		 */
		canvas.drawBitmap(source, -34 / 3.0f - offset, -34 / 3.0f - offset, paint);
		return target;
	}
}
