package com.polar.browser.utils;

public class CommonUtils {

	private static long lastClickTime;

	/**
	 * 判断双击
	 *
	 * @return
	 */
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 600) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
}
