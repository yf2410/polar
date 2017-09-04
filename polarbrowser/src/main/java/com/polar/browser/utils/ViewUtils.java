package com.polar.browser.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.EditText;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.ICaptureScreenCallback;
import com.polar.browser.manager.ConfigManager;

import java.lang.reflect.Field;

public class ViewUtils {

	private static final String TAG = "ViewUtils";

	public static void getScreenShotAsync(View v,
										  ICaptureScreenCallback callback, float scaleX, float scaleY,
										  boolean isAdjustSize) {
		getScreenShotAsync(v, callback, scaleX, scaleY, false, 0);
	}

	public static Bitmap getScreenShotSync(View v, int width, int height) {
		Bitmap bitmap = null;
		final Bitmap cacheBitmap = getMagicDrawingCache(v);
		if (cacheBitmap == null) {
			return bitmap;
		}
		bitmap = Bitmap.createBitmap(cacheBitmap, 0, 0, width, height, null, false);
		if (!cacheBitmap.isRecycled()) {
			cacheBitmap.recycle();
		}
		return bitmap;
	}

	/**
	 * 不进行任何缩放，直接截图
	 *
	 * @param v
	 * @return
	 */
	public static Bitmap getScreenShotSync(View v) {
		Bitmap bitmap = null;
		// 将绘图缓存得到的,注意这里得到的只是一个图像的引用
		final Bitmap cacheBitmap = getMagicDrawingCache(v);
		if (cacheBitmap == null) {
			return bitmap;
		}
		bitmap = Bitmap.createBitmap(cacheBitmap, 0, 0, cacheBitmap.getWidth(),
				cacheBitmap.getHeight(), null, false);
		if (!cacheBitmap.isRecycled()) {
			cacheBitmap.recycle();
		}
		return bitmap;
	}

	/**
	 * 设置缩放比例和宽高比例进行截图
	 *
	 * @param v
	 * @param scaleX
	 * @param scaleY
	 * @param heightWidthRatio
	 * @return
	 */
	public static Bitmap getScreenShotSync(View v, final float scaleX,
										   final float scaleY, final float heightWidthRatio) {
		Bitmap bitmap = null;
		final Bitmap cacheBitmap = getMagicDrawingCache(v, Bitmap.Config.RGB_565);
		if (cacheBitmap == null) {
			return bitmap;
		}
		// 获取矩阵对象
		Matrix matrix = getMatrix(scaleX, scaleY);
		bitmap = getBitmap(cacheBitmap, matrix, heightWidthRatio, scaleX, scaleY);
		if (!cacheBitmap.isRecycled()) {
			cacheBitmap.recycle();
		}
		return bitmap;
	}

	public static Bitmap getScreenShotSync(final View v, final float scaleX,
										   final float scaleY, Bitmap.Config quality) {
		Bitmap bitmap = null;
		final Bitmap cacheBitmap = getMagicDrawingCache(v, quality);
		if (cacheBitmap == null) {
			return bitmap;
		}
		// 获取矩阵对象
		Matrix matrix = getMatrix(scaleX, scaleY);
		bitmap = Bitmap.createBitmap(cacheBitmap, 0, 0, cacheBitmap.getWidth(),
				cacheBitmap.getHeight(), matrix, false);
		if (!cacheBitmap.isRecycled()) {
			cacheBitmap.recycle();
		}
		return bitmap;
	}

	public static Bitmap getScreenShotSync(final View v, final float scaleX,
										   final float scaleY) {
		return getScreenShotSync(v, scaleX, scaleY, Bitmap.Config.ARGB_8888);
	}

	public static void getScreenShotAsync(final View v,
										  final ICaptureScreenCallback callback, final float scaleX,
										  final float scaleY, final boolean isAdjustSize,
										  final float heightWidthRatio) {
		SimpleLog.i(TAG, "getMagicDrawingCache start:" + String.valueOf(System.currentTimeMillis()));
		final Bitmap cacheBitmap = getMagicDrawingCache(v);
		SimpleLog.i(TAG, "getMagicDrawingCache end:" + String.valueOf(System.currentTimeMillis()));
		if (cacheBitmap == null) {
			return;
		}
		SimpleLog.i(TAG, "scaleBitmap start:" + String.valueOf(System.currentTimeMillis()));
		// 获取矩阵对象
		Matrix matrix = getMatrix(scaleX, scaleY);
		Bitmap bitmap = null;
		if (isAdjustSize) {
			bitmap = getBitmap(cacheBitmap, matrix, heightWidthRatio, scaleX,
					scaleY);
		} else {
			bitmap = Bitmap.createBitmap(cacheBitmap, 0, 0,
					cacheBitmap.getWidth(), cacheBitmap.getHeight(), matrix,
					false);
		}
		if (!cacheBitmap.isRecycled()) {
			cacheBitmap.recycle();
		}
		SimpleLog.i(TAG, "scaleBitmap end:" + String.valueOf(System.currentTimeMillis()));
		if (callback != null) {
			callback.notifyCapture(bitmap);
		}
	}

	private static Bitmap getBitmap(Bitmap cacheBitmap, Matrix matrix,
									float heightWidthRatio, float scaleX, float scaleY) {
		Bitmap bitmap = null;
		int height = cacheBitmap.getHeight();
		int width = cacheBitmap.getWidth();
		if (width * heightWidthRatio > height) {
			bitmap = Bitmap.createBitmap(cacheBitmap, 0, 0, width, height,
					matrix, false);
		} else {
			height = (int) (width * heightWidthRatio);
			bitmap = Bitmap.createBitmap(cacheBitmap, 0, 0, width, height,
					matrix, false);
		}
		if (!cacheBitmap.isRecycled()) {
			cacheBitmap.recycle();
		}
		return bitmap;
	}

	private static Matrix getMatrix(float scaleX, float scaleY) {
		// 定义矩阵对象
		Matrix matrix = null;
		if (scaleX != 0 || scaleY != 0) {
			// 缩放原图
			matrix = new Matrix();
			matrix.postScale(scaleX, scaleY);
		}
		return matrix;
	}

	public static Bitmap getMagicDrawingCache(View view, Bitmap.Config quality, boolean addWatermark) {
		Bitmap bitmap = null;
		if (view.getWidth() + view.getHeight() == 0) {
			view.measure(
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
					MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			view.layout(0, 0, AppEnv.SCREEN_WIDTH, AppEnv.SCREEN_HEIGHT);
		}
		int viewWidth = view.getWidth();
		int viewHeight = view.getHeight();
		bitmap = Bitmap.createBitmap(viewWidth, viewHeight, quality);
		bitmap.eraseColor(android.R.color.white);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		if (addWatermark) {
			Bitmap watermark = BitmapFactory.decodeResource(view.getResources(), R.drawable.watermark);
			canvas.drawBitmap(watermark, bitmap.getWidth() - watermark.getWidth(),
					bitmap.getHeight() - watermark.getHeight() - 20, null);
		}
		return bitmap;
	}

	public static Bitmap getMagicDrawingCache(View view) {
		return getMagicDrawingCache(view, Bitmap.Config.RGB_565, false);
	}

	public static Bitmap getMagicDrawingCache(View view, Bitmap.Config quality) {
		return getMagicDrawingCache(view, quality, false);
	}

	public static Bitmap getMagicDrawingCache(View view, boolean addWatermark) {
		return getMagicDrawingCache(view, Bitmap.Config.RGB_565, addWatermark);
	}

	private static void recycleBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return;
		}
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	public static Bitmap takeScreenShot(Activity activity, boolean isNeedRemoveStatus) {
		return takeScreenShot(activity, isNeedRemoveStatus, false);
	}

	/**
	 * 获取指定Activity的截屏
	 */
	public static Bitmap takeScreenShot(Activity activity, boolean isNeedRemoveStatus, boolean addWatermark) {
		// View是你需要截图的View
		View view = activity.getWindow().getDecorView();
		Bitmap b1 = getMagicDrawingCache(view, addWatermark);
		if ((Build.VERSION.SDK_INT > 18 && !isNeedRemoveStatus) || ConfigManager.getInstance().isFullScreen()) {
			if (b1 != null) {
				Bitmap b = Bitmap.createBitmap(b1, 0, 0, b1.getWidth(), b1.getHeight());
				recycleBitmap(b1);
				return b;
			}
		} else {
			int statusBarHeight = 0;
			// 获取状态栏高度
			Rect frame = new Rect();
			activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			statusBarHeight = frame.top;
			// 去掉标题栏
			if (b1 != null) {
				Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight,
						b1.getWidth(), b1.getHeight() - statusBarHeight);
				recycleBitmap(b1);
				return b;
			}
		}
		recycleBitmap(b1);
		return null;
	}

	public static Bitmap takeScreenShot(Activity activity, final float scaleX,
										final float scaleY, final float heightWidthRatio) {
		View view = activity.getWindow().getDecorView();
		return getScreenShotSync(view, scaleX, scaleY, heightWidthRatio);
	}

	/**
	 * 获得圆角图片
	 *
	 * @param bitmap
	 * @param roundPx
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xffffffff;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, width, height);
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 从res中获取bitmap
	 *
	 * @param c
	 * @param resId
	 * @return
	 */
	public static Bitmap getBitmapFromResources(Context c, int resId) {
		Resources res = c.getResources();
		return BitmapFactory.decodeResource(res, resId);
	}

	/**
	 * 动态改变光标颜色
	 *
	 * @param edit
	 * @param cursorDrawableId
	 */
	public static void changeCursor(EditText edit, int cursorDrawableId) {
		try {
			// https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
			Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
			f.setAccessible(true);
			f.set(edit, cursorDrawableId);
		} catch (Exception ignored) {
		}
	}

	public static Bitmap getRGB565fromBytes(byte[] bytes) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
		return bitmap;
	}
}
