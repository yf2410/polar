package com.polar.browser.env;

import com.polar.browser.BuildConfig;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.library.utils.SystemUtils;

import static com.polar.browser.library.utils.SystemUtils.getArea;
import static com.polar.browser.library.utils.SystemUtils.getLan;

public class AppEnv {
	public static final boolean DEBUG = BuildConfig.DEBUG;
	public static final boolean ENABLE_STRICT_MODE = false;
	/**
	 * @brief package name.
	 */
	public static final String PACKAGE_NAME = JuziApp.getInstance().getPackageName();
	/**
	 * 最小滑动距离，区分手势中滑动和点击
	 */
	public static int MIN_SLIDING;
	/**
	 * 屏幕宽
	 **/
	public static int SCREEN_WIDTH;
	/**
	 * 屏幕高
	 **/
	public static int SCREEN_HEIGHT;
	/**
	 * 边缘滑动前进后退(最小边缘距离)
	 **/
	public static int MIN_SLIDE_BORDER;
	/**
	 * 边缘滑动前进后退(前进or后退的最小差值)
	 **/
	public static int MIN_SLIDE_OFF_SET;
	/**
	 * 顶部状态栏高度
	 **/
	public static int STATUS_BAR_HEIGHT;
	/**
	 * 是否是全屏状态
	 **/
	public static boolean sIsFullScreen = false;
	/**
	 * 是否显示快捷搜索
	 **/
	public static boolean sIsQuickSearch = true;
	/**
	 * 是否展示过边缘滑动向导
	 **/
	public static boolean sIsShownSlideGuide = false;
	/**
	 * youtube是否展示过下载提示
	 **/
	public static boolean sIsShownVideoDownloadTip = false;
	/**
	 * 打开APP过渡Logo是否展示过
	 **/
	public static boolean sIsShownLogoGuide = false;
	/**
	 * 本地是否保存服务端下发新闻开关状态
	 **/
	public static boolean sIsSaveCardNewsnetState = false;
	/**
	 * 是否展示过apk升级提醒
	 **/
	public static boolean sIsShownUpdateApkTip = false;
	/**
	 * 是否展示过长按编辑向导
	 **/
	public static boolean sIsShownEditLogoGuide = false;

	/**
	 * 获取语言topic
	 *
	 * @return
	 */
	public static String getLanguageTopic() {
		try {
			String[] languageArray = JuziApp.getInstance().getResources().getStringArray(R.array.topic_language);
			for (String language : languageArray) {
				if (language.equalsIgnoreCase(getLan())) {
					return String.format("language_%s", language);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	/**
	 * 获取国家topic
	 *
	 * @return
	 */
	public static String getNationTopic() {
		try {
			String[] nationArray = JuziApp.getInstance().getResources().getStringArray(R.array.topic_nation);
			for (String nation : nationArray) {
				if (nation.equalsIgnoreCase(getArea())) {
					return String.format("nation_%s", nation);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	public static String getVersionTopic() {
		return String.format("version_%s", SystemUtils.getVersionName(JuziApp.getInstance()));
	}
}
