package com.polar.browser.jni;


import android.content.Context;

import com.polar.browser.JuziApp;

public class NativeManager {

	public static final int NATIVE_QUERY_DATA_TYPE_Preset = 0;
	public static final int NATIVE_QUERY_DATA_TYPE_History = 1;
	public static final int NATIVE_QUERY_DATA_TYPE_Bookmark = 2;
	public static final int NATIVE_QUERY_DATA_TYPE_All = 3;
	private static final String JNI_LIB = "native";

	/**
	 * 加载 JNI 库
	 *
	 * @param c
	 */
	public static void loadLibrary() {
		System.loadLibrary(JNI_LIB);
		init(JuziApp.getAppContext());
	}
	/****************** 广告拦截接口 *************************/
	/******************************************************/

	/**
	 * 初始化Adblock数据
	 * 目前只需要加载一个配置文件，传String过去
	 *
	 * @param data
	 */
	public native static void initAdBlock(String data, String data2);

	/**
	 * 判断某个链接是否应该拦截
	 *
	 * @param mainUrl
	 * @param mainHost
	 * @param url
	 * @param urlHost
	 * @return
	 */
	public native static boolean isNeedBlock(String mainUrl, String mainHost, String url, String urlHost);

	/**
	 * 判断广告css
	 *
	 * @param referer
	 * @param refererHost
	 * @return
	 */
	public native static String getAdCss(String referer, String refererHost);

	/**
	 * 加载历史、收藏等数据
	 *
	 * @param type
	 * @param data
	 */
	public native static void initNativeQueryData(int type, String data);

	/**
	 * 输入数据，得到地址栏返回值
	 *
	 * @param data
	 * @return
	 */
	public native static String addressInput(byte[] data);

	/**
	 * 当历史记录、收藏改变的时候进行数据的更新
	 *
	 * @param nType
	 * @param title
	 * @param url
	 * @param navType
	 * @param time
	 */
	public native static void addItem(int nType, byte[] title, String url, int navType, long time);

	/**
	 * 初始化native代码。
	 * @param context
	 */
	public native static void init(Context context);
}
