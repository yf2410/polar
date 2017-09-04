package com.polar.browser.impl;

import android.app.Activity;
import android.content.Intent;

import com.polar.browser.R;
import com.polar.browser.bookmark.BookmarkActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.i.IShareClick;
import com.polar.browser.i.IToolbarMenuDelegate;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.push.SystemNewsListActivity;
import com.polar.browser.setting.AdBlockSettingActivity;
import com.polar.browser.setting.SettingActivity;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

public class ToolbarMenuImpl implements IToolbarMenuDelegate {

	private static final int ITEM_HISTORY = 0;
	private static final int ITEM_FAVORITE = 1;
	@SuppressWarnings("unused")
	private static final String TAG = "ToolbarMenuImpl";

	private RxFragmentActivity mActivity;
	private IShareClick mShareClick;

	public ToolbarMenuImpl(RxFragmentActivity activity, IShareClick shareClick) {
		mActivity = activity;
		mShareClick = shareClick;
	}

	@Override
	public void openFavorite() {
		Intent intent = new Intent();
		intent.setClass(mActivity, BookmarkActivity.class);
		intent.putExtra("item", ITEM_FAVORITE);
		mActivity.startActivity(intent);
		mActivity.overridePendingTransition(R.anim.slide_in_from_right,
				R.anim.slide_out_to_left);
	}

	@Override
	public void openHistory() {
		Intent intent = new Intent();
		intent.setClass(mActivity, BookmarkActivity.class);
		intent.putExtra("item", ITEM_HISTORY);
		mActivity.startActivity(intent);
		mActivity.overridePendingTransition(R.anim.slide_in_from_right,
				R.anim.slide_out_to_left);
		Statistics.sendOnceStatistics(GoogleConfigDefine.MENU_EVENTS, GoogleConfigDefine.MENU_HISTORY_MARK);
	}

	@Override
	public void openSettings() {
		mActivity.startActivity(new Intent(mActivity, SettingActivity.class));
		mActivity.overridePendingTransition(R.anim.slide_in_from_right,
				R.anim.slide_out_to_left);
	}

	@Override
	public void openDownload() {
		boolean isWifiDownloadEnable = ConfigManager.getInstance().isEnableOnlyWifiDownload();
		Intent intent = new Intent(mActivity, DownloadActivity.class);
		intent.putExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, isWifiDownloadEnable);
		intent.putStringArrayListExtra(CommonData.CHANGED_FILE_LIST,ConfigManager.getInstance().getChangedFileTypes());
		mActivity.startActivity(intent);
		mActivity.overridePendingTransition(R.anim.slide_in_from_right,
				R.anim.slide_out_to_left);
	}

	@Override
	public void exitBrowser() {
		ConfigManager configMgr = ConfigManager.getInstance();
		ExitBrowserImpl exitBrowser = new ExitBrowserImpl();

		if (configMgr.isEnableExitClear()) {
//			exitBrowser.clearBrowserRecord(mActivity);
			exitBrowser.clearBrowserDataSyn();
		}
		exitBrowser.executeExit(mActivity);
	}

	@Override
	public void openShare() {
		mShareClick.click();
	}

	@Override
	public void switchImgMode() {
		boolean isEnabled = ConfigManager.getInstance().isEnableImg();
		if (!isEnabled) {
			CustomToastUtils.getInstance().showImgToast(R.string.image_mode_off_toast, R.drawable.image_off);
			ConfigManager.getInstance().setEnableImgAsync(!isEnabled);
		} else {
			CustomToastUtils.getInstance().showImgToast(R.string.image_mode_on_toast, R.drawable.menu_noimage_on);
			ConfigManager.getInstance().setEnableImgAsync(!isEnabled);
			Statistics.sendOnceStatistics(GoogleConfigDefine.FUCTION_CLICK, GoogleConfigDefine.FUCTION_CLICK_NOIMAGE_MODE);
		}
	}

	@Override
	public void switchWebMode() {
		int uaType = ConfigManager.getInstance().getUaType();
		if (uaType == ConfigDefine.UA_TYPE_PC) {
			TabViewManager.getInstance().setUa(ConfigManager.getInstance().getDefaultUa());
			ConfigManager.getInstance().setUaType(ConfigDefine.UA_TYPE_DEFAULT);
			CustomToastUtils.getInstance().showImgToast(R.string.web_mode_off_toast, R.drawable.web_off);
		} else {
			TabViewManager.getInstance().setUa(CommonData.UA_PC);
			ConfigManager.getInstance().setUaType(ConfigDefine.UA_TYPE_PC);
			CustomToastUtils.getInstance().showImgToast(R.string.web_mode_on_toast, R.drawable.web_on);
			Statistics.sendOnceStatistics(GoogleConfigDefine.MENU_EVENTS, GoogleConfigDefine.MENU_PC);
		}
	}

	@Override
	public void openSystemNewsListActivity() {
		mActivity.startActivity(new Intent(mActivity, SystemNewsListActivity.class));
		mActivity.overridePendingTransition(R.anim.slide_in_from_right,
				R.anim.slide_out_to_left);
		Statistics.sendOnceStatistics(GoogleConfigDefine.FCM_SYSTEM, GoogleConfigDefine.FCM_SYSTEM_ENTRANCE);
	}

	@Override
	public void switchNightMode() {
		boolean isEnabled = ConfigManager.getInstance().isEnableNightMode();
		isEnabled = !isEnabled;
		//改变界面逻辑
		ConfigManager.getInstance().setEnableNightModeAsync(isEnabled);
		if (isEnabled) {
			Statistics.sendOnceStatistics(GoogleConfigDefine.MENU_EVENTS, GoogleConfigDefine.FUCTION_CLICK_NIGHT_MODE);
		}
	}

	@Override
	public void switchAdBlockMode() {
//		boolean isEnabled = ConfigManager.getInstance().isAdBlock();
//		if (!isEnabled) {
//			CustomToastUtils.getInstance().showImgToast(R.string.start_full_figure, R.drawable.menu_ad_block_on);
//			ConfigManager.getInstance().setEnableAdBlock(!isEnabled);
//		} else {
//			CustomToastUtils.getInstance().showImgToast(R.string.btn_ssl_close, R.drawable.menu_ad_block_off);
//			ConfigManager.getInstance().setEnableAdBlock(!isEnabled);
//			Statistics.sendOnceStatistics(GoogleConfigDefine.FUCTION_CLICK, GoogleConfigDefine.FUCTION_CLICK_AD_BLOCK_MODE);
//		}

		mActivity.startActivity(new Intent(mActivity, AdBlockSettingActivity.class));

	}
}
