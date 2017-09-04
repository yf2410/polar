package com.polar.browser.utils;

import android.util.Log;

import com.polar.browser.env.AppEnv;

public class SimpleLog {

	public static void d(String tag, String msg) {
		if (AppEnv.DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (AppEnv.DEBUG) {
			Log.e(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (AppEnv.DEBUG) {
			Log.i(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (AppEnv.DEBUG) {
			Log.w(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (AppEnv.DEBUG) {
			Log.v(tag, msg);
		}
	}

	public static void e(Exception e) {
		if (AppEnv.DEBUG) {
			e.printStackTrace();
		}
	}
}
