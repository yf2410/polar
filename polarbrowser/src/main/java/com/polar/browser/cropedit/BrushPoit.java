package com.polar.browser.cropedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.utils.DensityUtil;

public class BrushPoit extends View {

	public static final int TYPE_SMALL = 0;
	public static final int TYPE_BIG = 1;
	private float mSize;
	private int mColor;
	/**
	 * 0,xiao; 1,da
	 **/
	private int mType;
	private Paint mPaint;

	public BrushPoit(Context context) {
		this(context, null);
	}

	public BrushPoit(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mType = TYPE_SMALL;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mSize = 15;
		mColor = getResources().getColor(R.color.color_box_item_7);
		mPaint.setColor(mColor);
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
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Bitmap bitmap = null;
		if (mType == TYPE_SMALL) {
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_point_small);
		} else {
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_point_big);
		}
		int width = DensityUtil.dip2px(JuziApp.getInstance(), 34);
		Rect rect = new Rect(0, 0, width, width);
		canvas.drawBitmap(bitmap, null, rect, mPaint);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
		canvas.drawCircle(getWidth() / 2, getHeight() / 2, mSize / 2, mPaint);
	}
}
