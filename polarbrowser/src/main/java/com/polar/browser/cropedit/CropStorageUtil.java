package com.polar.browser.cropedit;

import android.graphics.Bitmap;

/**
 * 用于管理截屏图片的工具类
 *
 * @author dpk
 */
public class CropStorageUtil {
	private static Bitmap mBitmap;

	public static Bitmap getBitmap() {
		return mBitmap;
	}

	public static void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;
	}

	public static void recycle() {
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
		}
	}
}
