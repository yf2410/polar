package com.polar.browser.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	private static final int DAY_MILLIS = 24 * 60 * 60 * 1000;

	public static String getDateString() {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(d);
		return dateStr;
	}

	public static String getDateString(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(d);
		return dateStr;
	}

	public static String getYesterdayString() {
		Date yesterday = new Date(new Date().getTime() - DAY_MILLIS);
		return getDateString(yesterday);
	}

	public static boolean isSameDay(Date date1, Date date2) {
		if (date1.getYear() == date2.getYear() &&
				date1.getMonth() == date2.getMonth() &&
				date1.getDay() == date2.getDay()) {
			SimpleLog.d("date", "same day");
			return true;
		} else {
			SimpleLog.d("date", "different day");
			return false;
		}
	}

	/**
	 * 计算时间差
	 *
	 * @param pre
	 * @param next
	 * @return
	 */
	public static String getDateDiff(Date pre, Date next) {
		long diff = next.getTime() - pre.getTime();
		String s = "";
		long second = diff / 1000;
		long min = second / 60;
		long hr = min / 60;
		if (hr >= 24) {
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
			s = sdf.format(pre);
		} else if (1 < hr && hr < 24) {
			s = hr + "hrs";
		} else if (hr == 1) {
			s = hr + "hr";
		} else if (hr == 0) {
			if (1 < min && min < 60) {
				s = min + "mins";
			} else if (min >= 0 && min <= 1) {
				s = 1 + "min";
			}
		}
		return s;
	}

	/**
	 * 首页新闻计算时间差
	 *
	 * @param pre
	 * @param next
	 * @return
	 */
	public static String getNewsDateDiff(long pre, Date next) {
		String s = "";
		long pres = pre*1000;
		long nexts = next.getTime();
		long diff = nexts - pres;
		long second = diff / 1000;
		long min = second / 60;
		long hr = min / 60;

		if (min <= 15) {
			s = "iust now";
		} else if (min > 15 && min < 60) {
			s = min + " minutes ago";
		} else if (hr >= 1 && hr < 2) {
			s = "1 hour ago";
		} else if (hr >= 2 && hr < 24) {
			s = hr + " hours ago";
		} else if (hr >= 24 && hr < 48) {
			s = "1 day ago";
		} else if (hr >= 48 && hr < 96) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd");
			int day = Integer.parseInt(sdf.format(nexts))
					- Integer.parseInt(sdf.format(pres));
			s = day + " days ago";
		} else if (hr >= 96) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			s = sdf.format(pres);
		}
		return s;
	}

	public static String formatFileDate(long date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sdf.format(date);
	}

	/**
	 * 格式化日期
	 * */

	public static String formatDate(long time) {
		SimpleDateFormat sdf = null;
		try{
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			//使用默认时区和语言环境获得一个日历
			Calendar cale = Calendar.getInstance();
			if((time+"").length() == 10){  //10位，毫秒应该为13位
				time = time * 1000L;
			}
			cale.setTimeInMillis(time);
			//将Calendar类型转换成Date类型
			Date tasktime = cale.getTime();
			//设置日期输出的格式
			//格式化输出
			return sdf.format(tasktime);
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}

	public static String formatDate(Date datetime) {
		SimpleDateFormat sdf = null;
		try{
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			return sdf.format(datetime);
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}

	public static String formatDate(String format, Date datetime) {
		SimpleDateFormat sdf = null;
		try{
			sdf = new SimpleDateFormat(format, Locale.getDefault());
			return sdf.format(datetime);
		}catch (Exception e){
			e.printStackTrace();
			return "";
		}
	}
}
