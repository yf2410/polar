package com.polar.browser.manager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.AdSwitchBean;
import com.polar.browser.vclibrary.bean.NormalSwitchBean;
import com.polar.browser.vclibrary.bean.events.SPConfigChangedEvent;
import com.polar.browser.vclibrary.bean.events.SyncBookmarkEvent;
import com.polar.browser.vclibrary.bean.login.UserAccountData;
import com.polar.browser.vclibrary.common.Constants;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.polar.browser.common.data.ConfigDefine.BOOKMARK_SYNC_TIME_STAMP;
import static com.polar.browser.download_refactor.Constants.TYPE_APK;
import static com.polar.browser.download_refactor.Constants.TYPE_AUDIO;
import static com.polar.browser.download_refactor.Constants.TYPE_DOC;
import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_OTHER;
import static com.polar.browser.download_refactor.Constants.TYPE_VIDEO;
import static com.polar.browser.download_refactor.Constants.TYPE_WEB_PAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_ZIP;
import static com.polar.browser.utils.ConfigWrapper.put;


public class ConfigManager {

	private static final String TAG = "ConfigManager";

	private static final boolean DEFAULT_SAVE_ACCOUNT_STATE = true;
	private static ConfigManager sInstance;
	/**
	 * 无图模式
	 **/
	private boolean mIsEnableImg = true;
	/**
	 * 字体大小
	 **/
	private int mFontSize = ConfigDefine.FONT_SIZE_MID;
	/**
	 * 视频下载
	 **/
	private boolean mIsVedioDownload = false;
	/**
	 * 服务端下发YouTube视频下载开关
	 **/
	private boolean mIsServerVideoDownload = true;
	/**
	 * 广告拦截
	 **/
	private boolean mIsAdBlock = true;
	/**
	 * 最常访问
	 */
	private boolean mIsOftenVisit = false;
	/**
	 * 夜间模式
	 **/
	private boolean mIsNightMode = false;
	/**
	 * 全屏模式
	 **/
	private boolean mIsFullScreen = false;
	/**
	 * 快捷搜索
	 **/
	private boolean mIsQuickSearch = true;
	/**
	 * 竖屏锁定
	 **/
	private boolean mIsScreenLock = false;
	/**
	 * 退出时保留全屏
	 **/
	private boolean mIsSaveTab = false;
	private int mUaType = ConfigDefine.UA_TYPE_DEFAULT;
	private CopyOnWriteArrayList<IConfigObserver> mObservers = new CopyOnWriteArrayList<IConfigObserver>();

	private ConfigManager() {
		init();
	}

	public static ConfigManager getInstance() {
		if (null == sInstance) {
			synchronized (ConfigManager.class) {
				if (null == sInstance) {
					sInstance = new ConfigManager();
				}
			}
		}
		return sInstance;
	}

	public synchronized void registerObserver(IConfigObserver observer) {
			mObservers.add(observer);
	}

	public  synchronized void unregisterObserver(IConfigObserver observer) {
		mObservers.remove(observer);
	}

	// 做一些最基本的初始化，将配置加载到内存中
	public void init() {
		mIsEnableImg = isEnableImgInner();
		mFontSize = getFontSizeInner();
		mUaType = getUaTypeInner();
		mIsVedioDownload = isVedioDownloadInner();
		mIsServerVideoDownload = isServerVideoDownloadInner();
		mIsAdBlock = isEnableAdBlockInner();
		mIsNightMode = isEnableNightModeInner();
		mIsFullScreen = isFullScreenInner();
		mIsQuickSearch = isEnableQuickSearchInner();
		mIsScreenLock = isScreenLockInner();
		mIsSaveTab = isSaveTabInner();
	}

	/**
	 * 无图模式
	 **/
	public boolean isEnableImg() {
		return mIsEnableImg;
	}

	/**
	 * 夜间模式 由于跨进程，需要读文件，不能读内存
	 **/
	public boolean isEnableNightMode() {
//		return JuziApp.getInstance()
//				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS)
//				.getBoolean(ConfigDefine.ENABLE_NIGHT_MODE, false);
//		SimpleLog.e("", "isEnableNightMode == " + mIsNightMode);
		return mIsNightMode;
	}

	private boolean isEnableNightModeInner() {
//		mIsNightMode = ConfigWrapper.get(ConfigDefine.ENABLE_NIGHT_MODE, false);
		mIsNightMode = JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS)
				.getBoolean(ConfigDefine.ENABLE_NIGHT_MODE, false);
		return mIsNightMode;
	}

	/**
	 * 设置夜间模式
	 *
	 * @param isEnabled
	 */
	private synchronized final void setEnableNightModeInner(boolean isEnabled) {
		if (isEnabled == mIsNightMode) {
			return;
		}
//		ConfigWrapper.put(ConfigDefine.ENABLE_NIGHT_MODE, isEnabled);
		JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS).edit().putBoolean(ConfigDefine.ENABLE_NIGHT_MODE, isEnabled).commit();
		if (ConfigWrapper.commit()) {
			mIsNightMode = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_NIGHT_MODE, isEnabled);
			}
			Intent intent = new Intent(CommonData.ACTION_NIGHT_MODE_CHANGED);
			intent.putExtra(ConfigDefine.ENABLE_NIGHT_MODE, mIsNightMode);
			JuziApp.getInstance().sendBroadcast(intent);
		}
	}

	private final boolean isEnableImgInner() {
		return ConfigWrapper.get(ConfigDefine.ENABLE_IMG, true);
	}

	/**
	 * 设置无图模式：内部调用
	 *
	 * @param isEnabled
	 */
	private synchronized final void setEnableImgInner(final boolean isEnabled) {
		ConfigWrapper.put(ConfigDefine.ENABLE_IMG, isEnabled);
		if (ConfigWrapper.commit()) {
			SimpleLog.d(TAG, "无图模式现在值：" + isEnabled);
			mIsEnableImg = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_IMG, mIsEnableImg);
			}
		}
	}

	/**
	 * 字体大小
	 *
	 * @return
	 */
	public int getFontSize() {
		return mFontSize;
	}

	/**
	 * 设置字体大小
	 *
	 * @param fontSize <br>
	 *                 ConfigDefine.FONT_SIZE_MIN 小 <br>
	 *                 ConfigDefine.FONT_SIZE_MID 中 <br>
	 *                 ConfigDefine.FONT_SIZE_BIG 大
	 *                 ConfigDefine.FONT_SIZE_LARGE 特大
	 */
	public synchronized final void setFontSize(int fontSize) {
		ConfigWrapper.put(ConfigDefine.FONT_SIZE, fontSize);
		if (ConfigWrapper.commit()) {
			mFontSize = fontSize;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.FONT_SIZE, fontSize);
			}
		}
	}

	public int getUaType() {
		return mUaType;
	}

	public synchronized final void setUaType(int uaType) {
		ConfigWrapper.put(ConfigDefine.UA_TYPE, uaType);
		if (ConfigWrapper.commit()) {
			mUaType = uaType;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.UA_TYPE, uaType);
			}
		}
	}

	/**
	 * 竖屏锁定 由于跨进程，需要读文件，不能读内存
	 **/
	public boolean isScreenLock() {
//		return JuziApp.getInstance()
//				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS)
//				.getBoolean(ConfigDefine.ENABLE_SCREEN_LOCK, false);
		SimpleLog.e(TAG, "isScreenLock == " + mIsScreenLock);
		return mIsScreenLock;
	}

	public boolean isSaveTab() {
		return mIsSaveTab;
	}

	private boolean isScreenLockInner() {
//		mIsScreenLock = ConfigWrapper.get(ConfigDefine.ENABLE_SCREEN_LOCK, false);
		mIsScreenLock = JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS)
				.getBoolean(ConfigDefine.ENABLE_SCREEN_LOCK, false);
		return mIsScreenLock;
	}

	private boolean isSaveTabInner() {
		mIsSaveTab = JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS)
				.getBoolean(ConfigDefine.ENABLE_SAVE_TAB, false);
		return mIsSaveTab;
	}

	/**
	 * 是否设置为了全屏
	 **/
	public boolean isFullScreen() {
//		return JuziApp.getInstance()
//				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS)
//				.getBoolean(ConfigDefine.ENABLE_FULL_SCREEN, false);
		SimpleLog.e(TAG, "isFullScreen == " + mIsFullScreen);
		return mIsFullScreen;
	}

	private boolean isFullScreenInner() {
//		mIsFullScreen = ConfigWrapper.get(ConfigDefine.ENABLE_FULL_SCREEN, false);
		mIsFullScreen = JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS)
				.getBoolean(ConfigDefine.ENABLE_FULL_SCREEN, false);
		return mIsFullScreen;
	}

	/**
	 * 插件-视频下载
	 **/
	public boolean isVedioDownload() {
		return mIsVedioDownload;
	}

	/**
	 * 广告拦截
	 **/
	public boolean isAdBlock() {
		return mIsAdBlock;
	}

	/**
	 * 快捷搜索
	 **/
	public boolean isQuickSearch() {
		return mIsQuickSearch;
	}

	// 直接在调用进程同步执行
	public final void setEnableImgSync(final boolean isEnabled) {
		setEnableImgInner(isEnabled);
	}

	// post到IO线程上，observer也在io线程调用，目前需要自己转换线程
	// TODO: 传需要执行的线程的handler参数，notify在该线程上调用
	public final void setEnableImgAsync(final boolean isEnabled) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				setEnableImgInner(isEnabled);
			}
		};
		ThreadManager.postTaskToIOHandler(r);
	}

	private final boolean isVedioDownloadInner() {
		return ConfigWrapper.get(ConfigDefine.PLUG_VIDEO_DOWNLOAD, true);
	}

	private final boolean isEnableAdBlockInner() {
		return ConfigWrapper.get(ConfigDefine.ENABLE_AD_BLOCK, true);
	}

	private final boolean isEnableQuickSearchInner() {
		return ConfigWrapper.get(ConfigDefine.ENABLE_QUICK_SEARCH, true);
	}

	private final int getFontSizeInner() {
		return ConfigWrapper.get(ConfigDefine.FONT_SIZE,
				ConfigDefine.FONT_SIZE_MID);
	}

	private final int getUaTypeInner() {
		return ConfigWrapper.get(ConfigDefine.UA_TYPE,
				ConfigDefine.UA_TYPE_DEFAULT);
	}

	/**
	 * 设置夜间模式：对外接口
	 *
	 * @param isEnabled
	 */
	public final void setEnableNightModeAsync(final boolean isEnabled) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				setEnableNightModeInner(isEnabled);
			}
		};
		ThreadManager.postTaskToIOHandler(r);
	}

	/**
	 * 设置推出保存标签
	 *
	 * @param isEnabled
	 */
	public synchronized final void setEnableSaveTab(boolean isEnabled) {
		if (isEnabled == mIsSaveTab) {
			return;
		}
//		ConfigWrapper.put(ConfigDefine.ENABLE_SCREEN_LOCK, isEnabled);
		JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS).edit().putBoolean(ConfigDefine.ENABLE_SAVE_TAB, isEnabled).commit();
		if (ConfigWrapper.commit()) {
			mIsSaveTab = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_SAVE_TAB,
						isEnabled);
			}
		}
	}

	/**
	 * 设置竖屏锁定
	 *
	 * @param isEnabled
	 */
	public final void setEnableScreenLock(boolean isEnabled) {
		if (isEnabled == mIsScreenLock) {
			return;
		}
//		ConfigWrapper.put(ConfigDefine.ENABLE_SCREEN_LOCK, isEnabled);
		JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS).edit().putBoolean(ConfigDefine.ENABLE_SCREEN_LOCK, isEnabled).commit();
		if (ConfigWrapper.commit()) {
			mIsScreenLock = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_SCREEN_LOCK,
						isEnabled);
			}
		}
		Intent intent = new Intent(CommonData.ACTION_SCREEN_LOCKED);
		intent.putExtra(ConfigDefine.ENABLE_SCREEN_LOCK, mIsScreenLock);
		JuziApp.getInstance().sendBroadcast(intent);
	}

	/**
	 * 设置是否全屏
	 *
	 * @param isEnabled
	 */
	public synchronized final void setEnableFullScreen(boolean isEnabled) {
		if (isEnabled == mIsFullScreen) {
			return;
		}
//		ConfigWrapper.put(ConfigDefine.ENABLE_FULL_SCREEN, isEnabled);
		JuziApp.getInstance()
				.getSharedPreferences("config", Context.MODE_MULTI_PROCESS).edit().putBoolean(ConfigDefine.ENABLE_FULL_SCREEN, isEnabled).commit();
		if (ConfigWrapper.commit()) {
			mIsFullScreen = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_FULL_SCREEN,
						isEnabled);
			}
		}
		Intent intent = new Intent(CommonData.ACTION_FULL_SCREEN_CHANGED);
		intent.putExtra(ConfigDefine.ENABLE_FULL_SCREEN, mIsFullScreen);
		JuziApp.getInstance().sendBroadcast(intent);
	}

	/**
	 * 插件—视频下载
	 *
	 * @param isEnabled
	 */
	public final void setEnableVedioDownload(boolean isEnabled) {
		put(ConfigDefine.PLUG_VIDEO_DOWNLOAD, isEnabled);
		if (ConfigWrapper.commit()) {
			mIsVedioDownload = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.PLUG_VIDEO_DOWNLOAD, isEnabled);
			}
		}
	}

	/**
	 * 设置广告拦截
	 *
	 * @param isEnabled
	 */
	public final void setEnableAdBlock(boolean isEnabled) {
		put(ConfigDefine.ENABLE_AD_BLOCK, isEnabled);
		if (ConfigWrapper.commit()) {
			mIsAdBlock = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_AD_BLOCK, isEnabled);
			}
		}
	}

	/**
	 * 设置最常访问
	 *
	 * @param
	 */
	public final void setOftenVisit(boolean oftenVisit) {
		put(ConfigDefine.HISTORY_VISITED, oftenVisit);
		if (ConfigWrapper.commit()) {
			mIsOftenVisit = oftenVisit;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.HISTORY_VISITED, oftenVisit);
			}
		}
	}

	/**
	 * 设置快捷搜索
	 *
	 * @param isEnabled
	 */
	public final void setEnableQuickSearch(boolean isEnabled) {
		put(ConfigDefine.ENABLE_QUICK_SEARCH, isEnabled);
		if (ConfigWrapper.commit()) {
			mIsQuickSearch = isEnabled;
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_QUICK_SEARCH, isEnabled);
			}
		}
	}

	/**
	 * firebase 更新系统消息的提示状态
	 *
	 * @param hint 是否需要提示 需要提示为true,不需要为false
	 */
	public final void updateSystemNewsHintState(boolean hint) {
		put(ConfigDefine.FCM_SYSTEM_NEWS_HINT_STATE, hint);
		if (ConfigWrapper.commit()) {
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.FCM_SYSTEM_NEWS_HINT_STATE, hint);
			}
		}
	}

	/**
	 * 查询是否开启保存账号和密码功能
	 *
	 * @return
	 */
	public boolean isEnableSaveAccount() {
		return ConfigWrapper.get(ConfigDefine.SAVE_ACCOUNT, DEFAULT_SAVE_ACCOUNT_STATE);
	}

	/**
	 * 设置开启保存网页账号和密码
	 *
	 * @param isEnabled
	 */
	public void setEnableSaveAccount(boolean isEnabled) {
		SimpleLog.i(TAG, "[account]" + isEnabled);
		put(ConfigDefine.SAVE_ACCOUNT, isEnabled);
		if (ConfigWrapper.commit()) {
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.SAVE_ACCOUNT, isEnabled);
			}
		}
	}

	/**
	 * 获取搜索引擎
	 *
	 * @return
	 */
	public int getSearchEngine() {
		return ConfigWrapper.get(ConfigDefine.SEARCH_ENGINE, ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK);
	}

	/**
	 * 设置当前搜索引擎
	 *
	 * @param engine
	 */
	public final void setSearchEngine(int engine,boolean isNotify) {
		put(ConfigDefine.SEARCH_ENGINE, engine);
		if (ConfigWrapper.commit()&&isNotify) {
				for (IConfigObserver observer : mObservers) {
					observer.notifyChanged(ConfigDefine.SEARCH_ENGINE, engine);
				}

		}
	}


	/**
	 *  检测设备语言切换是否重启
	 *
	 * @return
	 */
	public boolean getLanChangedRestart() {
		return ConfigWrapper.get(ConfigDefine.LAN_CHANGED_RESTART, false);
	}

	/**
	 *  检测设备语言切换是否重启
	 *
	 * @param lanChangedRestart
	 */
	public void setLanChangedRestart(boolean lanChangedRestart) {
		put(ConfigDefine.LAN_CHANGED_RESTART, lanChangedRestart);
		ConfigWrapper.apply();
	}

	/**
	 * 获取上次打开的tab列表
	 *
	 * @return
	 */
	public String getTabList() {
		return ConfigWrapper.get(ConfigDefine.TAB_LIST, "");
	}

	/**
	 * 记录当前的标签列表
	 *
	 * @param tabList
	 */
	public void setTabList(String tabList) {
		put(ConfigDefine.TAB_LIST, tabList);
		ConfigWrapper.apply();
	}

	/**
	 * 是否退出时清除浏览记录
	 *
	 * @return
	 */
	public boolean isEnableExitClear() {
		return ConfigWrapper.get(ConfigDefine.ENABLE_EXIT_CLEAR, false);
	}

	/**
	 * 设置是否退出时清除浏览记录
	 *
	 * @param isEnabled
	 */
	public void setEnableExitClear(boolean isEnabled) {
		put(ConfigDefine.ENABLE_EXIT_CLEAR, isEnabled);
		ConfigWrapper.apply();
	}

	/**
	 * 设置不再提示退出对话框
	 */
	public void setNeverRemindExitClear(boolean isNeverRemind) {
		put(ConfigDefine.EXIT_NEVER_REMIND, isNeverRemind);
		ConfigWrapper.apply();
	}

	/**
	 * 是否退出时不再弹提示对话框
	 *
	 * @return
	 */
	public boolean isNeverRemindExit() {
		return ConfigWrapper.get(ConfigDefine.EXIT_NEVER_REMIND, false);
	}

	/**
	 * 恢复默认设置
	 */
	public void resetToDefault() {
		setFontSize(ConfigDefine.FONT_SIZE_MID);
		setUaType(ConfigDefine.UA_TYPE_DEFAULT);
		setUa(getDefaultUa());
		setCustomUa(getDefaultUa());
		TabViewManager.getInstance().setUa(getDefaultUa());
		setEnableScreenLock(false);
		setEnableQuickSearch(true);
		setSafetyWarningEnabled(true);
		setEnableImgSync(true);
		setNeverRemindExitClear(false);
		setEnableExitClear(false);
		setEnableFullScreen(false);
		setSlidingScreenMode(ConfigDefine.SLIDING_BACK_FORWARD_border);
		setPrivacyMode(false);
		setEnableSaveTab(false);
		setEnableOnlyWifiDownload(true);
		setEnableAdBlock(true);
		setEnableAdBlockTip(true);
		// 延迟后，再恢复夜间模式状态
		Runnable r = new Runnable() {

			@Override
			public void run() {
				setEnableNightModeAsync(false);
			}
		};
		setShowSuggestion(true);
		ThreadManager.postDelayedTaskToUIHandler(r, 100);
	}

	/**
	 * 获取最后一次运行时的version
	 * @return
	 */
	public String getLastRunVersion() {
		return ConfigWrapper.get(ConfigDefine.LAST_RUN_VERSION, "");
	}

	/**
	 * 设置version
	 *
	 * @param ver
	 */
	public void setLastRunVersion(String ver) {
		put(ConfigDefine.LAST_RUN_VERSION, ver);
		ConfigWrapper.apply();
	}

	/**
	 * 获取上一次运行时的Lan
	 * @return
	 */
	public String getLastRunLan() {
		return ConfigWrapper.get(ConfigDefine.LAST_RUN_LANGUAGE, "");
	}

	/**
	 * 设置上一次运行时的Lan
	 *
	 * @param lan
	 */
	public void setLastRunLan(String lan) {
		ConfigWrapper.put(ConfigDefine.LAST_RUN_LANGUAGE, lan);
		ConfigWrapper.apply();
	}
	/**
	 * 是否展示过滑动引导界面
	 *
	 * @return
	 */
	public boolean isShownSlideGuide() {
		return ConfigWrapper.get(ConfigDefine.IS_SHOWN_SLIDE_GUIDE, false);
	}

	/**
	 * 设置已经展示过了侧滑界面
	 */
	public void setShownSlideGuide() {
		put(ConfigDefine.IS_SHOWN_SLIDE_GUIDE, true);
		ConfigWrapper.apply();
	}

	/**
	 * 是否执行过发送附属图标去手机桌面
	 *
	 * @return
	 */
	public boolean isAddShortCutToDesktop() {
		return ConfigWrapper.get(ConfigDefine.IS_ADD_SHORTCUT_TODESKTOP, false);
	}

	/**
	 * 是否执行过发送附属图标去手机桌面
	 */
	public void setAddShortCutToDesktop() {
		put(ConfigDefine.IS_ADD_SHORTCUT_TODESKTOP, true);
		ConfigWrapper.apply();
	}

	/**
	 * youtube是否展示过下载提示
	 *
	 * @return
	 */
	public boolean isShownVideoDownloadTip() {
		return ConfigWrapper.get(ConfigDefine.IS_SHOWN_VIDEODOWNLOAD_TIP, false);
	}

	/**
	 * 设置youtube是否展示过下载提示
	 */
	public void setShownVideoDownloadTip() {
		put(ConfigDefine.IS_SHOWN_VIDEODOWNLOAD_TIP, true);
		ConfigWrapper.apply();
	}

	/**
	 *
	 *打开APP过渡Logo是否展示过
	 * @return
	 */
	public boolean isShownlogoGuide() {
		return ConfigWrapper.get(ConfigDefine.IS_SHOWN_LOGOGUIDE, false);
	}

	/**
	 * 设置APP过渡Logo是否展示过
	 */
	public void setShownLogoGuide() {
		ConfigWrapper.put(ConfigDefine.IS_SHOWN_LOGOGUIDE, true);
		ConfigWrapper.apply();
	}

	/**
	 * 是否展示过长按编辑引导界面
	 *
	 * @return
	 */
	public boolean isShownEditlogoGuide() {
		return ConfigWrapper.get(ConfigDefine.IS_SHOWN_EDITLOGO_GUIDE, false);
	}

	/**
	 * 设置已经展示长按编辑引导界面
	 */
	public void setShownEditlogoGuide() {
		put(ConfigDefine.IS_SHOWN_EDITLOGO_GUIDE, true);
		ConfigWrapper.apply();
	}

	/**
	 * 用户是否开启了logo
	 *
	 * @return
	 */
	public boolean isLogosEnabled() {
		return ConfigWrapper.get(ConfigDefine.CARD_LOGOS_ENABLE, true);
	}

	public int getSlidingScreenMode() {
		return ConfigWrapper.get(ConfigDefine.SLIDING_BACK_FORWARD,
				ConfigDefine.SLIDING_BACK_FORWARD_border);
	}

	public void setSlidingScreenMode(int mode) {
		put(ConfigDefine.SLIDING_BACK_FORWARD, mode);
		ConfigWrapper.apply();
	}

	public String getDefaultUa() {
		return ConfigWrapper.get(ConfigDefine.DEFAULT_UA, "");
	}

	public void setDefaultUa(String ua) {
		put(ConfigDefine.DEFAULT_UA, ua);
		ConfigWrapper.apply();
	}

	public String getUa() {
		return ConfigWrapper.get(ConfigDefine.USER_UA, "");
	}

	public void setUa(String ua) {
		put(ConfigDefine.USER_UA, ua);
		ConfigWrapper.apply();
	}

	public String getCustomUa() {
		String customUa = ConfigWrapper.get(ConfigDefine.CUSTOM_UA, "");
		if (TextUtils.isEmpty(customUa)) {
			customUa = ConfigWrapper.get(ConfigDefine.DEFAULT_UA, "");
		}
		return customUa;
	}

	public void setCustomUa(String ua) {
		put(ConfigDefine.CUSTOM_UA, ua);
		ConfigWrapper.apply();
	}

	public boolean isDisableScreenShot() {
		return ConfigWrapper.get(ConfigDefine.DISABLE_SCREEN_SHOT, false);
	}

	public void setDisableScreenShot(boolean isDisabled) {
		put(ConfigDefine.DISABLE_SCREEN_SHOT, isDisabled);
		ConfigWrapper.apply();
	}

	public void setBookmarkDefaultSelected(int type) {
		put(ConfigDefine.DEFAULT_BOOKMARK_HISTORY, type);
		ConfigWrapper.apply();
	}

	public int getWelcomeShowTimes(long time) {
		return ConfigWrapper.get(ConfigDefine.WELCOME_SHOW + String.valueOf(time), 0);
	}

	public void setWelcomeShowTimes(long time) {
		int times = getWelcomeShowTimes(time) + 1;
		put(ConfigDefine.WELCOME_SHOW + String.valueOf(time), times);
		ConfigWrapper.apply();
	}


	public boolean isPrivacyMode() {
		return ConfigWrapper.get(ConfigDefine.PRIVACY_MODE, false);
	}

	public void setPrivacyMode(boolean isPrivacyMode) {
		put(ConfigDefine.PRIVACY_MODE, isPrivacyMode);
		if (ConfigWrapper.commit()) {
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.PRIVACY_MODE, isPrivacyMode);
			}
		}
	}

	public void setUserSensitive() {
		put(CommonData.USER_TYPE, CommonData.USER_TYPE_SENSITIVE);
		ConfigWrapper.apply();
	}

	/**
	 * 强制推出标记
	 *
	 * @return
	 */
	public boolean isForceExit() {
		return ConfigWrapper.get(GoogleConfigDefine.BROWSER_EXIT_FORCE, false);
	}

	public void setForceExit(boolean b) {
		ConfigWrapper.put(GoogleConfigDefine.BROWSER_EXIT_FORCE, b);
		ConfigWrapper.apply();
	}

	/**
	 * 是否展示过升级apk提醒
	 * @return
	 */
	public boolean isShownUpdateApkTip() {
		return ConfigWrapper.get(ConfigDefine.IS_SHOWN_UPDATE_APK_TIP, false);
	}

	/**
	 * 设置adjust统计开关状态
	 */
	public void setAdjustSwitchState(String adjustSwitchState) {
		ConfigWrapper.put(ConfigDefine.ADJUST_SWITCH_STATE, adjustSwitchState);
		ConfigWrapper.apply();
	}

	/**
	 * adjust统计开关状态
	 * @return
	 */
	public String getAdjustSwitchState(){
		return ConfigWrapper.get(ConfigDefine.ADJUST_SWITCH_STATE, Constants.ADJUST_CLOSE);
	}



	/**
	 * 设置是否展示过升级apk提醒
	 */
	public void setShownUpdateApkTip() {
		put(ConfigDefine.IS_SHOWN_UPDATE_APK_TIP, true);
		ConfigWrapper.apply();
	}
	/**
	 * 设置是否展示过升级apk提醒
	 */
	public void setShownNOUpdateApkTip() {
		put(ConfigDefine.IS_SHOWN_UPDATE_APK_TIP, false);
		ConfigWrapper.apply();
	}

	/**
	 * 获取当前是否开启了仅wifi下载
	 * @return 开启了的话返回true
	 */
	public boolean isEnableOnlyWifiDownload() {
		return ConfigWrapper.get(ConfigDefine.ENABLE_ONLY_WIFI_DOWNLOAD, true);
	}

	/**
	 * 设置是否开启仅wifi模式下载
	 *
	 * @param isEnabled true 开启
	 */
	public synchronized void setEnableOnlyWifiDownload(boolean isEnabled) {
		ConfigWrapper.put(ConfigDefine.ENABLE_ONLY_WIFI_DOWNLOAD, isEnabled);
		if (ConfigWrapper.commit()) {
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_ONLY_WIFI_DOWNLOAD, isEnabled);
			}
		}
	}

	public synchronized void notifySearchKeyChanged(String key) {
		for (IConfigObserver observer : mObservers) {
			observer.notifyChanged(ConfigDefine.SEARCH_KEY_CHANGED, key);
		}
	}

	/**
	 * 用户本地是否开启了新闻
	 * @return
	 */
	public boolean isCardNewsEnabled() {
		return ConfigWrapper.get(ConfigDefine.CARD_NEWS_ENABLE, true);
	}

	public synchronized void setCardNewsEnabled(boolean isEnabled) {
		ConfigWrapper.put(ConfigDefine.CARD_NEWS_ENABLE, isEnabled);
		if (ConfigWrapper.commit()) {
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.CARD_NEWS_ENABLE, isEnabled);
			}
		}
	}

	/**
	 * 本地是否保存服务端下发新闻开关状态
	 *
	 * @return
	 */
	public boolean isSaveCardNewsNetState() {
		return ConfigWrapper.get(ConfigDefine.IS_SAVE_CARDNEWS_STATE, false);
	}

	/**
	 * 设置本地保存服务端下发新闻开关状态
	 */
	public void setSaveCardNewsNetState() {
		ConfigWrapper.put(ConfigDefine.IS_SAVE_CARDNEWS_STATE, true);
		ConfigWrapper.apply();
	}

	public void notifyGoBackOrGoFoward() {
		for (IConfigObserver mObserver : mObservers) {
			mObserver.notifyChanged(ConfigDefine.GOFOWARD_OR_GOBACK,true);
		}
	}

	public void notifyHideIm() {
		for (IConfigObserver observer : mObservers) {
			observer.notifyChanged(ConfigDefine.HIDE_IM, true);
		}
	}


	public void setHomeSiteInited() {
		ConfigWrapper.put(ConfigDefine.HOME_SITE_INITED, true);
		ConfigWrapper.commit();
	}


	public boolean isHomeSiteInited() {
		return  ConfigWrapper.get(ConfigDefine.HOME_SITE_INITED, false);
	}


	/**是否 显示搜索建议*/
	public boolean isShowSuggestion() {
		return ConfigWrapper.get(ConfigDefine.SEARCH_SHOW_SUGGESTION,true);
	}

	public void setShowSuggestion(boolean isShow) {
		ConfigWrapper.put(ConfigDefine.SEARCH_SHOW_SUGGESTION, isShow);
		ConfigWrapper.commit();
	}

	public String getSearchEngineVersion() {
		return ConfigWrapper.get(ConfigDefine.SEARCH_ENGINE_V,"");
	}

	public void setSearchEngineVersion(String version) {
		ConfigWrapper.put(ConfigDefine.SEARCH_ENGINE_V, version);
		ConfigWrapper.commit();
	}

	public void saveSearchEngineList(String json) {
		ConfigWrapper.put(ConfigDefine.SEARCH_ENGINE_LIST, json);
		ConfigWrapper.commit();
	}

	public String getSearchEngineList() {
		return ConfigWrapper.get(ConfigDefine.SEARCH_ENGINE_LIST,"");
	}

	public String getLastEngineList() {
		return ConfigWrapper.get(ConfigDefine.SEARCH_ENGINE_LAST_LIST,"");
	}

	public void setLastEngineList(String json) {
		ConfigWrapper.put(ConfigDefine.SEARCH_ENGINE_LAST_LIST, json);
		ConfigWrapper.commit();
	}

	/**默认搜索引擎是否改过*/
	public boolean isModifiedDefaultSearchEngine() {
		return ConfigWrapper.get(ConfigDefine.SEARCH_ENGINE_MODIFIED,false);
	}

	public void setDefaultSearchEngineModified() {
		ConfigWrapper.put(ConfigDefine.SEARCH_ENGINE_MODIFIED, true);
		ConfigWrapper.commit();
	}

	/**
	 * 安全警告开关
	 * @return
     */
	public boolean isSafetyWarningEnabled() {
		return ConfigWrapper.get(ConfigDefine.SAFETY_WARNING,true);
	}

	public void setSafetyWarningEnabled(boolean isSafetyWarning) {
		ConfigWrapper.put(ConfigDefine.SAFETY_WARNING, isSafetyWarning);
		ConfigWrapper.commit();
		EventBus.getDefault().post(new SPConfigChangedEvent(isSafetyWarning,ConfigDefine.SAFETY_WARNING));
	}

	/**
	 * 长访问记录开关
	 * @return
	 */
	public boolean isHistoryVisitedEnabled() {
		return ConfigWrapper.get(ConfigDefine.HISTORY_VISITED,true);
	}
	public void setHistoryVisitedEnabled(boolean isHistoryVisited) {
		ConfigWrapper.put(ConfigDefine.HISTORY_VISITED, isHistoryVisited);
		ConfigWrapper.commit();
		EventBus.getDefault().post(new SPConfigChangedEvent(isHistoryVisited,ConfigDefine.HISTORY_VISITED));
	}

	/**
	 * 是否修改过首页LOGO
	 *
	 * @return
	 */
	public boolean isCheckModifiedHomeSite() {
		return ConfigWrapper.get(ConfigDefine.IS_MODIFIED_HOME_SITE, false);
	}

	/**
	 * 设置修改过首页LOGO
	 */
	public void setCheckModifiedHomeSite(boolean isCheckModifiedHomeSite) {
		put(ConfigDefine.IS_MODIFIED_HOME_SITE, isCheckModifiedHomeSite);
		ConfigWrapper.apply();
	}

	/**
	 * 获取五分钟之内的搜索建议的 统计事件
	 * @return
     */
	public synchronized String getSuggestionEvent() {

		return ConfigWrapper.get(ConfigDefine.SUGGESTION_EVENT, "");
	}

	public synchronized void saveSuggestionEvent(String json,boolean notifyClear) {
		put(ConfigDefine.SUGGESTION_EVENT, json);
		if (ConfigWrapper.commit()&&notifyClear) {
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.SUGGESTION_EVENT,json);
			}
		}
	}

	public boolean isNeedSendSuggesitonEvent(){
		return ConfigWrapper.get(ConfigDefine.IS_NEED_SEND_SUGGESTION_EVENT, true);
	}

	public void setNeedSendSuggesitonEvent(boolean isNeed){
		put(ConfigDefine.IS_NEED_SEND_SUGGESTION_EVENT, isNeed);
		ConfigWrapper.apply();
	}

	/**
	 * 获取比价插件开关状态
	 * @return
	 */
	public boolean getHasofferEnabled() {
		return ConfigWrapper.get(ConfigDefine.HASOFFER_ENABLED, true);
	}

	/**
	 * 设置比价插件开关状态
	 */
	public void setHasofferEnabled(boolean hasofferEnabled) {
		ConfigWrapper.put(ConfigDefine.HASOFFER_ENABLED, hasofferEnabled);
		ConfigWrapper.apply();
	}

	/**
	 * 获取比价插件服务端开关状态
	 * @return
	 */
	public boolean getServerHasofferEnabled() {
		return ConfigWrapper.get(ConfigDefine.SERVER_HASOFFER_ENABLED, true);
	}

	/**
	 * 设置比价插件服务端开关状态
	 */
	public void setServerHasofferEnabled(boolean serverHasofferEnabled) {
		ConfigWrapper.put(ConfigDefine.SERVER_HASOFFER_ENABLED, serverHasofferEnabled);
		ConfigWrapper.apply();
	}


	/**
	 * 获取比价插件版本
	 * @return
	 */
	public String getHasofferPlugVersion() {
		return ConfigWrapper.get(ConfigDefine.HASOFFER_PLUG_VERSION, "1");
	}

	public boolean isHasDownload() {
		return ConfigWrapper.get(ConfigDefine.HAS_DOWNLOADING_TASK, false);
	}

	public void notifyFileCountChanged(String type) {
		ConfigWrapper.put(type,true);
		if(ConfigWrapper.commit()){
			for(IConfigObserver observer:mObservers){
				observer.notifyChanged(ConfigDefine.FILE_COUNT_CHANGED,type);
			}
		}

	}

	/**
	 * 设置比价插件版本
	 */
	public void setHasofferPlugVersion(String hasofferPlugVersion) {
		ConfigWrapper.put(ConfigDefine.HASOFFER_PLUG_VERSION, hasofferPlugVersion);
		ConfigWrapper.apply();
	}

	/**
	 * 获取比价插件js文件MD5
	 * @return
	 */
	public String getHasofferPlugMd5() {
		return ConfigWrapper.get(ConfigDefine.HASOFFER_PLUG_MD5, "");
	}

	/**
	 * 设置比价插件js文件MD5
	 */
	public void setHasofferPlugMd5(String hasofferPlugMd5) {
		ConfigWrapper.put(ConfigDefine.HASOFFER_PLUG_MD5, hasofferPlugMd5);
		ConfigWrapper.apply();
	}

	/**
	 * 获取比价插件支持网站列表
	 * @return
	 */
	public String getHasofferPlugSupport() {
		return ConfigWrapper.get(ConfigDefine.HASOFFER_PLUG_SUPPORT, "");
	}

	/**
	 * 设置比价插件支持网站列表
	 */
	public void setHasofferPlugSupport(String hasofferPlugSupport) {
		ConfigWrapper.put(ConfigDefine.HASOFFER_PLUG_SUPPORT, hasofferPlugSupport);
		ConfigWrapper.apply();
	}

	public ArrayList<String> getChangedFileTypes() {
		ArrayList<String> arrayList = new ArrayList<>();
		if (ConfigWrapper.get(TYPE_APK, false)) {
			arrayList.add(TYPE_APK);
		}

		if (ConfigWrapper.get(TYPE_IMAGE, false)) {
			arrayList.add(TYPE_IMAGE);
		}


		if (ConfigWrapper.get(TYPE_OTHER, false)) {
			arrayList.add(TYPE_OTHER);
		}


		if (ConfigWrapper.get(TYPE_DOC, false)) {
			arrayList.add(TYPE_DOC);
		}


		if (ConfigWrapper.get(TYPE_WEB_PAGE, false)) {
			arrayList.add(TYPE_WEB_PAGE);
		}


		if (ConfigWrapper.get(TYPE_AUDIO, false)) {
			arrayList.add(TYPE_AUDIO);
		}


		if (ConfigWrapper.get(TYPE_VIDEO, false)) {
			arrayList.add(TYPE_VIDEO);
		}


		if (ConfigWrapper.get(TYPE_ZIP, false)) {
			arrayList.add(TYPE_ZIP);
		}

		return arrayList;
	}

	public void setAllFileTypeUnChanged() {
		ConfigWrapper.put(TYPE_APK,false);
		ConfigWrapper.put(TYPE_IMAGE,false);
		ConfigWrapper.put(TYPE_AUDIO,false);
		ConfigWrapper.put(TYPE_VIDEO,false);

		ConfigWrapper.put(TYPE_WEB_PAGE,false);
		ConfigWrapper.put(TYPE_ZIP,false);
		ConfigWrapper.put(TYPE_DOC,false);
		ConfigWrapper.put(TYPE_OTHER,false);
		ConfigWrapper.commit();
	}

	public void saveGoDownloadFlag(String flagBeanJson) {
		ConfigWrapper.put(ConfigDefine.GO_DOWNLODE_FLAG, flagBeanJson);
	}

	public NormalSwitchBean getGoDownloadFlag() {
		String beanJson  = ConfigWrapper.get(ConfigDefine.GO_DOWNLODE_FLAG,"");
		Gson gson = new Gson();
		return gson.fromJson(beanJson, NormalSwitchBean.class);
	}

	public void gotoBrowserActivity() {
		for(IConfigObserver observer:mObservers){
			observer.notifyChanged(ConfigDefine.GO_TO_BROWSER,"");
		}
	}

	public void saveCurrentPath(String path) {
		ConfigWrapper.put(CommonData.KEY_CURRENT_DOWN_FOLDER,path);
		ConfigWrapper.commit();
	}

	public String getCurrentDownloadFolder() {
		return ConfigWrapper.get(CommonData.KEY_CURRENT_DOWN_FOLDER,"");
	}

	public boolean getNotifyNewsEngine() {
		return ConfigWrapper.get(ConfigDefine.NOTIFY_NEWS_ENGINE, true);
	}
	public void setNotifyNewsEngine(boolean isNotifyNewsEngine) {
		ConfigWrapper.put(ConfigDefine.NOTIFY_NEWS_ENGINE, isNotifyNewsEngine);
		ConfigWrapper.commit();
	}

	public boolean getNotifySystemEngine() {
		return ConfigWrapper.get(ConfigDefine.NOTIFY_SYSTEM_ENGINE, true);
	}
	public void setNotifySystemEngine(boolean isNotifySystemEngine) {
		ConfigWrapper.put(ConfigDefine.NOTIFY_SYSTEM_ENGINE, isNotifySystemEngine);
	}
	public boolean getFbMessageNotificationEngine() {
		return ConfigWrapper.get(ConfigDefine.ENABLE_FB_MESSAGE_NOTIFICATION, true);
	}
	public void setFbMessageNotificationEngine(boolean isFbMessageNotificationEngine) {
		ConfigWrapper.put(ConfigDefine.ENABLE_FB_MESSAGE_NOTIFICATION, isFbMessageNotificationEngine);
		ConfigWrapper.commit();
	}


	//===========facebook-广告SDK开关==========


	public void setMCC(String mcc) {
		ConfigWrapper.put(ConfigDefine.MCC, mcc);
		ConfigWrapper.commit();
	}

	public String getMCC() {
		return ConfigWrapper.get(ConfigDefine.MCC, "");
	}

	public void setSusWinShow(boolean isShowSusWin) {
		ConfigWrapper.put(ConfigDefine.SHOW_SUS_WIN, isShowSusWin);
		ConfigWrapper.commit();
	}

	public boolean isShowSusWin() {
		return ConfigWrapper.get(ConfigDefine.SHOW_SUS_WIN, true);
	}

	public void setAlbumAvailable(boolean availavle) {
		ConfigWrapper.put(ConfigDefine.ALBUM_AVAILABLE, availavle);
		ConfigWrapper.commit();
	}

	public boolean getAlbumAvailable() {
		return ConfigWrapper.get(ConfigDefine.ALBUM_AVAILABLE, false);
	}

	/**已拦截广告数量*/
	public void saveAdBlockedCount(int pageCount,boolean isEnableTips){

		setSaveTraffic(calculateSumTraffic(pageCount));
		setSaveTime(calculateSumTime(pageCount));
		if(pageCount>0){
			if(isEnableTips||(getAdBlockedCount()<100&&(getAdBlockedCount()+pageCount)>=100)){
				for (IConfigObserver observer : mObservers) {
					observer.notifyChanged(ConfigDefine.AD_BLOCKED_COUNT, pageCount);
				}
			}

			ConfigWrapper.put(ConfigDefine.AD_BLOCKED_COUNT, pageCount+getAdBlockedCount());
			ConfigWrapper.commit();
		}

	}

	private void setSaveTime(long ms) {
		ConfigWrapper.put(ConfigDefine.AD_SAVE_TIME, ms + getSaveTime());
	}

	public long getSaveTime() {
		return ConfigWrapper.get(ConfigDefine.AD_SAVE_TIME, 0L);
	}

	private long calculateSumTime(int pageCount) {
		int max=30;

		int min=100;
		Random random = new Random();

		int sum = 0;
		for(int i = 0;i<pageCount;i++) {
			int randomNum = random.nextInt(max)%(max-min+1) + min;
			sum+= randomNum;
		}
		return sum;
	}

	private long calculateSumTraffic(int pageCount) {
		final int kb = 1000;
		int max=30*kb;

		int min=10*kb;
		Random random = new Random();

		int sum = 0;
		for(int i = 0;i<pageCount;i++) {
			int randomNum = random.nextInt(max)%(max-min+1) + min;
			sum+= randomNum;
		}
		return sum;
	}
	
	public void setForeground(boolean isFg) {
		ConfigWrapper.put(ConfigDefine.IS_FG, isFg);
		ConfigWrapper.commit();
	}
	public boolean isAppFg() {
		return ConfigWrapper.get(ConfigDefine.IS_FG, true);
	}

	public void setAddFbNotifyFlag(boolean isAddFbNotify) {
		ConfigWrapper.put(ConfigDefine.ADD_FB_NOTIFY_FLAG, isAddFbNotify);
		ConfigWrapper.commit();
	}
	public boolean isAddFbNotifyFlag() {
		return ConfigWrapper.get(ConfigDefine.ADD_FB_NOTIFY_FLAG, false);
	}

	public void setFbNotifyMsgNumber(int isFbNotifyMsgNumber) {
		ConfigWrapper.put(ConfigDefine.FB_NOTIFY_MEG_NUMBER, isFbNotifyMsgNumber);
		ConfigWrapper.commit();
	}
	public int getFbNotifyMsgNumber() {
		return ConfigWrapper.get(ConfigDefine.FB_NOTIFY_MEG_NUMBER, 0);
	}

	/**
	 * 获得 节省的流量数
	 */
	public long getSaveTraffic() {
		return ConfigWrapper.get(ConfigDefine.AD_SAVE_TRAFFIC,0L);
	}

	public void setSaveTraffic(long size) {
		ConfigWrapper.put(ConfigDefine.AD_SAVE_TRAFFIC, size+getSaveTraffic());
		ConfigWrapper.commit();
	}

	public void setAdBlockToast(boolean isToast) {
		for (IConfigObserver observer : mObservers) {
			observer.notifyChanged(ConfigDefine.IS_AD_BLOCK_TOAST, isToast);
		}
	}



	public void clearAdBlockCount(){
		ConfigWrapper.put(ConfigDefine.AD_BLOCKED_COUNT, 0);
		ConfigWrapper.commit();
	}

	/**get已拦截广告数量*/
	public int getAdBlockedCount(){
		return ConfigWrapper.get(ConfigDefine.AD_BLOCKED_COUNT, 0);
	}

	/**是否开启拦截提示*/
	public boolean isAdBlockTip(){
		return ConfigWrapper.get(ConfigDefine.ENABLE_AD_BLOCK_TIP, true);
	}

	/**
	 * 设置广告拦截提示
	 *
	 * @param isEnabled
	 */
	public final void setEnableAdBlockTip(boolean isEnabled) {
		ConfigWrapper.put(ConfigDefine.ENABLE_AD_BLOCK_TIP, isEnabled);
		if (ConfigWrapper.commit()) {
			for (IConfigObserver observer : mObservers) {
				observer.notifyChanged(ConfigDefine.ENABLE_AD_BLOCK_TIP, isEnabled);
			}
		}
	}


	//是否显示底部菜单打开后的 向导效果
	public boolean isShowBottomMenuNavigate() {
		return ConfigWrapper.get(ConfigDefine.BOTTOM_MENU_NAVIGATE,true);
	}

	public void setNotShowBottomNavigate() {
		ConfigWrapper.put(ConfigDefine.BOTTOM_MENU_NAVIGATE,false);
		ConfigWrapper.commit();
	}

	public long getLastStaTime() {
		return ConfigWrapper.get(ConfigDefine.LASTSTATIME, Long.MIN_VALUE);
	}

	public void setStaPkgsTime(long time) {
		ConfigWrapper.put(ConfigDefine.LASTSTATIME, time);
		ConfigWrapper.commit();
	}

	/**
	 *  ==================== Account begin=========================
	 */

	/**
	 * 影子Token
	 */
	public void setShadowToken(String shadowToken){
		ConfigWrapper.put(ConfigDefine.SHADOW_TOKEN,shadowToken);
		ConfigWrapper.commit();
	}

	public String getShadowToken(){
		return ConfigWrapper.get(ConfigDefine.SHADOW_TOKEN,null);
	}

	/**
	 * 登录授权Token
	 */
	public void setUserToken(String userToken){
		ConfigWrapper.put(ConfigDefine.USER_TOKEN,userToken);
		ConfigWrapper.commit();
		if(!TextUtils.isEmpty(userToken))
			RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_LOGIN_SUCCESS));
	}

	public void setUserId(String userId) {
		ConfigWrapper.put(ConfigDefine.USER_ID,userId);
		ConfigWrapper.commit();
	}

	public String getUserId() {
		return ConfigWrapper.get(ConfigDefine.USER_ID, "");
	}

	public String getUserToken(){
		return ConfigWrapper.get(ConfigDefine.USER_TOKEN,null);
	}

	/**
	 * 持久化用户信息
	 */
	public void setUserInfo(UserAccountData userInfo){
		try{
			if(userInfo != null){
				Gson gson = new Gson();
				String userInfoStr =  gson.toJson(userInfo);
				ConfigWrapper.put(ConfigDefine.USER_INFORMATION,userInfoStr);
			}else {
				ConfigWrapper.put(ConfigDefine.USER_INFORMATION,null);
			}
			ConfigWrapper.commit();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public UserAccountData getUserInfo(){
		UserAccountData userData = null;
		try{
			Gson gson = new Gson();
			String userInfoStr = ConfigWrapper.get(ConfigDefine.USER_INFORMATION,null);
			userData = gson.fromJson(userInfoStr,UserAccountData.class);
		}catch (Exception e){
			e.printStackTrace();
		}
		return userData != null ? userData : new UserAccountData();
	}

	public boolean isLoginStatus(){
		return getUserToken() != null;
	}

	/**
	 * 设置是否进行过第一次登录成功书签同步
	 */
	public void setShowLoginSuccessTip(){
		ConfigWrapper.put(ConfigDefine.LOGIN_SUCCESS_TIP,true);
		ConfigWrapper.commit();
	}

	public boolean isShowLoginSuccessTip(){
		return ConfigWrapper.get(ConfigDefine.LOGIN_SUCCESS_TIP,false);
	}

	/**
	 * 记录最后一次登录的账户所显示的用户头像
	 * NOTE:
	 * 1、不同账户类型，分别存储本地头像，避免相互覆盖
	 * 2、使用sId 标识当前头像路径 和 当前登录用户是否匹配，不配备则将本地路径置为null
	 */
	public void setLocalUserAvatarPath(String avatarPath){
		String prefix = getUserAvatarPrefix();
		String finalPath;
		if(prefix == null || avatarPath == null){
			finalPath = null;
		}else{
			finalPath = prefix + avatarPath;
		}
		if(ConfigDefine.TYPE_LOGIN_PHONE_NUMBER.equals(JuziApp.getUserAccountData().getAccountType())){   //Phone num Login
			ConfigWrapper.put(ConfigDefine.USER_AVATAR_PHONE_NUM_LOGIN, finalPath);
		}else if(ConfigDefine.TYPE_LOGIN_FACEBOOK.equals(JuziApp.getUserAccountData().getAccountType())){ //Facebook login
			ConfigWrapper.put(ConfigDefine.USER_AVATAR_FACEBOOK_LOGIN, finalPath);
		}
		ConfigWrapper.commit();
	}

	public String getLocalUserAvatarPath(){
		try {
			String prefix = getUserAvatarPrefix();
			String avatarPath = null;
			if(ConfigDefine.TYPE_LOGIN_PHONE_NUMBER.equals(JuziApp.getUserAccountData().getAccountType())){   //Phone num Login
				avatarPath = ConfigWrapper.get(ConfigDefine.USER_AVATAR_PHONE_NUM_LOGIN, null);
			}else if(ConfigDefine.TYPE_LOGIN_FACEBOOK.equals(JuziApp.getUserAccountData().getAccountType())){ //Facebook login
				avatarPath = ConfigWrapper.get(ConfigDefine.USER_AVATAR_FACEBOOK_LOGIN, null);
			}
			if(prefix != null && avatarPath != null){
				if(avatarPath.startsWith(prefix)){
					String avatar = avatarPath.substring(avatarPath.indexOf(prefix)+prefix.length());
					if(new File(avatar).exists()){
						return avatar;
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		setLocalUserAvatarPath(null);  //头像路径已失效
		return null;
	}

	private String getUserAvatarPrefix(){
		if(JuziApp.getUserAccountData().getsId() == null) return null;
		return JuziApp.getUserAccountData().getsId();
	}

	public long getLocalUserAvatarLastModified(){
		return ConfigWrapper.get(ConfigDefine.USER_AVATAR_UPDATE_TIME,-1L);
	}

	/**
	 * 本地头像最新更新时间
	 * @param updateTime
     */
	public void setLocalUserAvatarLastModified(long updateTime){
		ConfigWrapper.put(ConfigDefine.USER_AVATAR_UPDATE_TIME,updateTime);
		ConfigWrapper.commit();
	}

	/**
	 * 判断本地存储的用户头像是否有效
	 * @return
     */
	public boolean isLocalUserAvatarValid(){
		try{
			if(getLocalUserAvatarLastModified() == JuziApp.getUserAccountData().getAvatarLastModified()){
				return true;
			}else{
				return false;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 得到图片的保存名称
	 * Note:
	 * 1. 得到的图片名称后面追加时间戳，避免重名
	 * @param isTemp  是否要得到临时文件名
	 * @return
     */
	public String getAvatarSaveName(boolean isTemp){
		StringBuffer strBuf = new StringBuffer();
		if(!isTemp){  //正式头像名称
			//区别不同账号类型的头像
			if(JuziApp.getUserAccountData().getAccountType() != null){
				strBuf.append(JuziApp.getUserAccountData().getAccountType()).append("_");
			}
		}else{ //编辑头像时，临时头像名称
			strBuf.append("temp_");
		}
		strBuf.append("avatar_").append(System.currentTimeMillis()).append(".png");
		return strBuf.toString();
	}

	/**
	 *  ==================== Account end=========================
	 */

	public String getSyncBookmarkTimeStamp() {
		return ConfigWrapper.get(getUserId(), "");
	}

	public void setSyncBookmarkTimeStamp(String timeStamp) {
		ConfigWrapper.put(getUserId(), timeStamp);
		ConfigWrapper.commit();
		setLocalBookmarkSyncTime(TextUtils.isEmpty(timeStamp)?timeStamp:String.valueOf(System.currentTimeMillis()));
	}

	public void setLocalBookmarkSyncTime(String timeStamp) {
		ConfigWrapper.put(BOOKMARK_SYNC_TIME_STAMP,timeStamp);
		ConfigWrapper.commit();
	}

	public String getLocalBookmarkSyncTime() {
		return ConfigWrapper.get(BOOKMARK_SYNC_TIME_STAMP, "");
	}

	/**
	 * 保存 登录之后的 同步书签是否完成

	 */
	public void setLoginSyncBookmarkFinished(String state) {
		ConfigWrapper.put(ConfigDefine.LOGIN_SYNC_BOOKMARK_RESULT+getUserId(),state);
		ConfigWrapper.commit();
	}

	/**
	 * 获得 用户登录账号之后的自动同步书签 是否成功
	 */
	public String getLoginSyncBookmarkStateByUserId() {
		return ConfigWrapper.get(ConfigDefine.LOGIN_SYNC_BOOKMARK_RESULT+getUserId(), "");
	}

	/**
	 * 首页卡片是否显示滑动提醒
	 * @param isHomeCardSlideTip
	 */
	public void setHomeCardSlideTip(boolean isHomeCardSlideTip) {
		ConfigWrapper.put(ConfigDefine.SLIDE_TIP, isHomeCardSlideTip);
		ConfigWrapper.commit();
	}
	public boolean isHomeCardSlideTip() {
		return ConfigWrapper.get(ConfigDefine.SLIDE_TIP, true);
	}

	public void setLastWeatherJson(String lastJson) {
		ConfigWrapper.put(ConfigDefine.LAST_WEATHER_JSON, lastJson);
		ConfigWrapper.commit();
	}

	public String getLastWeatherJson() {
		return ConfigWrapper.get(ConfigDefine.LAST_WEATHER_JSON, "");
	}

	public boolean isHistoryRecordCountEngine() {
		return ConfigWrapper.get(ConfigDefine.VISITED_SITE_COUNT,true);
	}
	public void setHistoryRecordCountEngine() {
		ConfigWrapper.put(ConfigDefine.VISITED_SITE_COUNT, false);
		ConfigWrapper.commit();
	}

    public void setLocalSetting(String json) {
        ConfigWrapper.put(ConfigDefine.LOCAL_SETTING, json);
        ConfigWrapper.commit();
    }

    public String getLocalSetting() {
        return ConfigWrapper.get(ConfigDefine.LOCAL_SETTING, "");
    }

    public String getOnLineSetting(){
		return ConfigWrapper.get(ConfigDefine.ONLINE_SETTING, "");
	}

	public void setIsSettingSync(boolean isSettingSync) {
		ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_BROWSER_SETTING, isSettingSync);
		ConfigWrapper.apply();
	}

	public void setIsBookmarSync(boolean isBookmarkSync) {
		ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_BOOKMARK, isBookmarkSync);
		ConfigWrapper.apply();
	}


	public void setIsHomeSiteSync(boolean isHomeSiteSync) {
		ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_HOMEPAGE, isHomeSiteSync);
		ConfigWrapper.apply();
	}

	public void setIsOnlyWifeSync(boolean isOnlywifiSync) {
		ConfigWrapper.put(ConfigDefine.PERSONAL_CENTER_SYNC_IN_WIFI, isOnlywifiSync);
		ConfigWrapper.apply();
	}

	public boolean isBookmarkSync() {
		return ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BOOKMARK,true);
	}

	public boolean isHomesiteSync() {
		return ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_HOMEPAGE,true);
	}


	public boolean isSettingSync() {
		return ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BROWSER_SETTING,true);
	}

	public boolean isOnlyWifiSync() {
		return ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_SYNC_IN_WIFI,true);
	}

	public void setOnlineSetting(String json) {
		ConfigWrapper.put(ConfigDefine.ONLINE_SETTING, json);
		ConfigWrapper.commit();
	}

	public void saveAdSwitchBySP(AdSwitchBean adSwitchBean) {
		if (adSwitchBean == null) return;
		ConfigWrapper.put(ConfigDefine.FB_WELCOME, adSwitchBean.isFbStart());
		ConfigWrapper.put(ConfigDefine.FB_HOME, adSwitchBean.isFbHomenative());
		ConfigWrapper.put(ConfigDefine.ADJUST_LIFE_SWITCH, adSwitchBean.isAdjustLife());
		ConfigWrapper.put(ConfigDefine.IS_UPLOAD_SK, adSwitchBean.isUploadSk());
		ConfigWrapper.put(ConfigDefine.IS_UPLOAD_URL, adSwitchBean.isUploadUrl());
		ConfigWrapper.put(ConfigDefine.SERVER_VIDEO_STATE, adSwitchBean.isVideoPlug());
		ConfigWrapper.put(ConfigDefine.SERVER_VIDEO_CUSTOM, adSwitchBean.isVideoCustom());
		ConfigWrapper.commit();
		mIsServerVideoDownload = adSwitchBean.isVideoPlug();
	}
	public boolean getFbWelcomeEngine() {
		return ConfigWrapper.get(ConfigDefine.FB_WELCOME, false);
	}
	public boolean getFbHomeEngine() {
		return ConfigWrapper.get(ConfigDefine.FB_HOME,false);
	}
	public boolean getAdjustLifeSwitchState(){
		return ConfigWrapper.get(ConfigDefine.ADJUST_LIFE_SWITCH, false);
	}
	public boolean isUploadSk() {
		return ConfigWrapper.get(ConfigDefine.IS_UPLOAD_SK, true);
	}
	public boolean isUploadUrl() {
		return ConfigWrapper.get(ConfigDefine.IS_UPLOAD_URL, true);
	}
	private boolean isServerVideoDownloadInner(){
		return ConfigWrapper.get(ConfigDefine.SERVER_VIDEO_STATE, true);
	}
	public boolean isServerVideoDownload(){
		return mIsServerVideoDownload;
	}
	public boolean getVideoIsCustomState(){
		return ConfigWrapper.get(ConfigDefine.SERVER_VIDEO_CUSTOM, false);
	}

	/**
	 * 记录上次上报视频插件开关状态时间
	 */
	public long getRecordVideoPlugLastState() {
		return ConfigWrapper.get(ConfigDefine.RECORD_VIDEO_PLUG_STATE,0L);
	}
	public void setRecordVideoPlugLastState(long time) {
		ConfigWrapper.put(ConfigDefine.RECORD_VIDEO_PLUG_STATE, time);
		ConfigWrapper.commit();
	}
}
