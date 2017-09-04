package com.polar.browser.impl;

import android.view.View;
import android.view.View.OnClickListener;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IExitBrowser;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.SettingSyncManager;
import com.polar.browser.sync.UserHomeSiteManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.db.SearchRecordApi;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import java.sql.SQLException;

/**
 * 浏览器退出实现类
 * 用于处理浏览器按back键退出时的逻辑
 *
 * @author dpk
 */
public class ExitBrowserImpl implements IExitBrowser {

	private CommonDialog dialog;
	private static long exitTime = 0;

	@Override
	public void exitFromBackBtn(final RxFragmentActivity mainActivity) {
		final ConfigManager configMgr = ConfigManager.getInstance();
		boolean isEnableExitClear = configMgr.isEnableExitClear();
		boolean isNeverRemind = configMgr.isNeverRemindExit();
		if (isNeverRemind) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				CustomToastUtils.getInstance().showTextToast(R.string.exit_press_back_again);
				exitTime = System.currentTimeMillis();
				return;
			} else {
				if (isEnableExitClear) {
					clearBrowserDataSyn();
				}
				executeExit(mainActivity);
				Statistics.sendOnceStatistics(GoogleConfigDefine.BROWSER_EXIT,
						GoogleConfigDefine.BROWSER_EXIT_DOUBLE_CLICK);
				return;
			}
		}
		dialog = new CommonDialog(mainActivity);
		dialog.setTitle(R.string.exit_dialog_title);
		dialog.setCenterView(R.layout.dialog_exit_browser);
		final CommonCheckBox1 cbClear = (CommonCheckBox1) dialog.findViewById(R.id.cb_clear_browser);
		final CommonCheckBox1 cbNeverRemind = (CommonCheckBox1) dialog.findViewById(R.id.cb_never_remind);
		cbClear.setChecked(isEnableExitClear);
		dialog.setBtnOkListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (AccountLoginManager.getInstance().isUserLogined()){
				Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC, GoogleConfigDefine.BOOKMARK_AUTO_SYNC,GoogleConfigDefine.BOOKMARK_EXIT_BROWSER_SYNC);
				}
				BookmarkManager.getInstance().syncBookmark(mainActivity,false);
				SettingSyncManager.getInstance().syncSetting(SettingSyncManager.SYNC_TYPE_EXIT);
				//UserHomeSiteManager.getInstance().syncHomeSite(UserHomeSiteManager.SYNC_TYPE_EXIT);
				if (cbClear.isChecked()) {
					configMgr.setEnableExitClear(true);
					clearBrowserDataSyn();
				} else {
					configMgr.setEnableExitClear(false);
				}
				if (cbNeverRemind.isChecked()) {
					ConfigManager.getInstance().setNeverRemindExitClear(true);
				}
				dialog.dismiss();
				// click ok to exit browser 
				Statistics.sendOnceStatistics(GoogleConfigDefine.BROWSER_EXIT,
						GoogleConfigDefine.BROWSER_EXIT_CLICK_OK);
				executeExit(mainActivity);
			}
		});
		dialog.setBtnCancelListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public void hindExitDialog(){
		if (dialog != null && dialog.isShowing()){
			dialog.dismiss();
		}
	}

	/**
	 * 执行退出操作
	 * @param mainActivity
	 */
	@Override
	public void executeExit(RxFragmentActivity mainActivity) {
		ConfigManager.getInstance().setForceExit(false);
		if (!ConfigManager.getInstance().isSaveTab()) {
			TabViewManager.getInstance().clearAllSubImage();
			ConfigManager.getInstance().setTabList("");
		}
		ThreadManager.destroy();
		mainActivity.finish();
	}

	/**
	 * 清理历史记录
	 */
//	public void clearBrowserRecord(Activity mainActivity) {
//		TabViewManager.getInstance().clearCache();
//		HistoryManager.getInstance().deleteAllOftenHistory();
//		HistoryManager.getInstance().deleteAllHistory();
//		HistoryManager.getInstance().deleteAllSearchHistory();
//
//	}

	/**
	 * 清理记录
	 */
	public void clearBrowserDataSyn() {
		TabViewManager.getInstance().clearCache();
		try {
			SearchRecordApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).clearAllSearchRecord();
			HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).clearAllHistoryRecord();
			SiteManager.getInstance().updateHistoryRecords(null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
