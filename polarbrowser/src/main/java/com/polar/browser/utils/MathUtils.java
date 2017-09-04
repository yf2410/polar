package com.polar.browser.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * 和算法、数学运算相关的工具类
 *
 * @author dpk
 */
public class MathUtils {

	/**
	 * 从total个数种选出count个数，以集合形式返回
	 *
	 * @param total
	 * @param count
	 * @return
	 */
	public static Set<Integer> getRandomInt(int total, int count) {
		Set<Integer> result = new HashSet<Integer>();
		// 如果total小于count，则直接可以返回全部
		if (total < count) {
			for (int i = 0; i < total; ++i) {
				result.add(i);
			}
			return result;
		}
		for (int i = 1; i <= count; ++i) {
			int number = (int) (Math.random() * total);
			while (!result.add(number)) {
				number = (int) (Math.random() * total);
			}
		}
		return result;
	}
}
