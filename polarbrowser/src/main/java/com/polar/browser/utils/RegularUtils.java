package com.polar.browser.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegularUtils {

	/**
	 * 匹配汉字
	 * @param str
	 * @return
	 */
	public static boolean isCheneseChar(String str) {
		boolean flag = false;
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.matches()) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 匹配字符数字 串
	 * @param str
	 * @return
	 */
	public static boolean isAllLettersAndNum(String str) {
		boolean flag = false;
		
		if (str.matches("[a-zA-Z0-9]+")) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 返回大写字母
	 * @param ch
	 * @return
	 */
	public static String getUpperLetter(String ch){
		if (ch.matches("[a-zA-Z]")) {
			ch = ch.toUpperCase();
		}
		return ch;
	}
}
