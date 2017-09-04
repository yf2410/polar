package com.polar.browser.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class UIUtils {

	private static final String TAG = "UIUtils";

	public static String getTextFromAttrs(Context context, AttributeSet attrs) {
		return getTextFromAttrs(context, attrs, "text");
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static String getTextFromAttrs(Context context, AttributeSet attrs, String name) {
		String value = getValueFromAttrs(context, attrs, name);
		if (!TextUtils.isEmpty(value)) {
			if (value.startsWith("@")) {
				int res = str2Int(value.substring(1), -1);
				if (res != -1) {
					try {
						value = context.getString(res);
					} catch (Exception e) {
						SimpleLog.e(TAG, e.getMessage());
					}
				}
			}
		}
		return value;
	}

	public static String getValueFromAttrs(Context context, AttributeSet attrs, String key) {
		if (attrs != null) {
			int aCount = attrs.getAttributeCount();
			for (int i = 0; i < aCount; i++) {
				String name = attrs.getAttributeName(i);
				if (!TextUtils.isEmpty(name) && name.equals(key)) {
					return attrs.getAttributeValue(i);
				}
			}
		}
		return null;
	}

	/**
	 * 字符串转换成int
	 *
	 * @param str
	 * @return
	 */
	public static int str2Int(String str, int defValue) {
		int ret = defValue;
		try {
			if (!TextUtils.isEmpty(str)) {
				ret = Integer.parseInt(str.trim());
			}
		} catch (Exception ex) {
			SimpleLog.e(TAG, "str2Int error");
		}
		return ret;
	}

	/**
	 * 字符串转换成int
	 *
	 * @param str
	 * @return
	 */
	public static int str2Int(String str, int defValue, int radix) {
		int ret = defValue;
		try {
			if (!TextUtils.isEmpty(str)) {
				ret = Integer.parseInt(str.trim(), radix);
			}
		} catch (Exception ex) {
			SimpleLog.e(TAG, "str2Int radix error");
		}
		return ret;
	}

	public static Drawable getDrawableFromAttrs(Context context, AttributeSet attrs, String key) {
		String value = getValueFromAttrs(context, attrs, key);
		if (!TextUtils.isEmpty(value)) {
			if (value.startsWith("@")) {
				int res = str2Int(value.substring(1), -1);
				if (res != -1) {
					try {
						Drawable drawable = context.getResources().getDrawable(res);
						return drawable;
					} catch (Exception e) {
						SimpleLog.e(TAG, e.getMessage());
					}
				}
			}
		}
		return null;
	}

	// FIXME
	// setEnable(false)时需要改变子view的背景，android:duplicateParentState不管用，暂时用这种方法
	public static void setViewGroupEnabled(ViewGroup v, boolean enabled) {
		for (int i = 0; i < v.getChildCount(); i++) {
			setViewGroupEnabled(v.getChildAt(i), enabled);
		}
	}

	private static void setViewGroupEnabled(View v, boolean enabled) {
		if (v == null) {
			return;
		}
		if (v instanceof ViewGroup) {
			ViewGroup vg = (ViewGroup) v;
			vg.setEnabled(enabled);
			for (int i = 0; i < vg.getChildCount(); i++) {
				setViewGroupEnabled(vg.getChildAt(i), enabled);
			}
		} else {
			v.setEnabled(enabled);
		}
	}
}
