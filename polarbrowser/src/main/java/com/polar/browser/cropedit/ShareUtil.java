package com.polar.browser.cropedit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.ShareUtils;

public class ShareUtil {

	/**
	 * 涂鸦截屏分享，保存的截图名
	 **/
	public static final String SHARE_SHOT_IMG = "share_shot.png";
	private static ShareUtil mInstance;
	private boolean isInint;
	private String mImagePath;
//	private static final int THUMB_SIZE = 100;
	private String mShareText = "分享截图";

	private ShareUtil() {
		mImagePath = VCStoragerManager.getInstance().getImageDirPath() + SHARE_SHOT_IMG;
	}

	public static ShareUtil getInstance() {
		if (mInstance == null) {
			mInstance = new ShareUtil();
		}
		return mInstance;
	}

	/**
	 * 分享前先初始化新浪和微信的SDK
	 */
	public void init() {
		if (!isInint) {
			isInint = true;
		}
	}

	public void share2Facebook(Context context) {
		ShareUtils
				.share(context, "com.facebook.katana", mShareText, mImagePath);
	}

	public void share2Twitter(Context context) {
		ShareUtils
				.share(context, "com.twitter.android", mShareText, mImagePath);
	}

	public void share2Whatsapp(Context context) {
		ShareUtils.share(context, "com.whatsapp", mShareText, mImagePath);
	}

	@SuppressLint("NewApi")
	public int getBitmapSize(Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // API 19
			return bitmap.getAllocationByteCount();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {// API
			// 12
			return bitmap.getByteCount();
		}
		return bitmap.getRowBytes() * bitmap.getHeight(); // earlier version
	}

	public void share2WechatMoments() {
	}
}
