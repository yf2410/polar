package com.polar.browser.utils;

import android.graphics.Color;

public class ColorUtils {

	/**
	 * @param color
	 * @param alpha between 0 to 255.
	 */
	public static int getColorAtAlpha(int color, int alpha) {
		if (alpha < 0 || alpha > 255) {
			throw new IllegalArgumentException("The alpha should be 0 - 100.");
		}
		return Color.argb(alpha, Color.red(color), Color.green(color),
				Color.blue(color));
	}
}