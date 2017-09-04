package com.polar.browser.impl;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.polar.browser.common.ui.CommonProgressBar1;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IAddressBar;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.tabview.TabView;
import com.polar.browser.utils.SimpleLog;

public class AddressBarImpl implements IAddressBar {

	private static final String TAG = "AddressBarImpl";
	private static final int MESSAGE_UPDATE_PROGRESS = 0x201;
	private static final int MESSAGE_UPDATE_PROGRESS_TO_END = 0x202;
	private CommonProgressBar1 mProgressBar;
	private CommonProgressBar1 mFullScreenProgress;
	private View mAddressBar;
	private View mBtnRefresh;
	private View mBtnStop;
	private int mProgress = 0;
	private boolean mProgressRunning = false;
	/**
	 * 每25ms更新进度条一次
	 */
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if (msg.what == MESSAGE_UPDATE_PROGRESS) {
				handleMessageUpdate();
			} else if (msg.what == MESSAGE_UPDATE_PROGRESS_TO_END) {
				handleMessageProgressEnd();
			}
		}

		;
	};

	public AddressBarImpl(View addressBar, CommonProgressBar1 progressBar,
						  CommonProgressBar1 fullScreenProgress, View btnRefresh, View btnStop) {
		mAddressBar = addressBar;
		mProgressBar = progressBar;
		mFullScreenProgress = fullScreenProgress;
		mBtnRefresh = btnRefresh;
		mBtnStop = btnStop;
	}

	private void handleMessageUpdate() {
		mProgressBar.fadeOut(false, ConfigManager.getInstance().isEnableNightMode());
		if (mProgress < 40) {
			mProgress += 1;
			mProgressBar.setProgress(mProgress);
			mFullScreenProgress.setProgress(mProgress);
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MESSAGE_UPDATE_PROGRESS), 6);
		} else if (mProgress >= 40 && mProgress < 85) {
			mProgress += 1;
			mProgressBar.setProgress(mProgress);
			mFullScreenProgress.setProgress(mProgress);
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MESSAGE_UPDATE_PROGRESS), 4);
		} else if (mProgress >= 85 && mProgress <= 95) {
			mProgress += 1;
			mProgressBar.setProgress(mProgress);
			mFullScreenProgress.setProgress(mProgress);
			mHandler.sendMessageDelayed(
					mHandler.obtainMessage(MESSAGE_UPDATE_PROGRESS), 30);
		}
		if (TabViewManager.getInstance().getCurrentTabView() != null) {
			TabViewManager.getInstance().getCurrentTabView().setProgress(mProgress);
		}
	}

	private void handleMessageProgressEnd() {
		if (mProgress < 100) {
			mProgressBar.fadeOut(true, ConfigManager.getInstance().isEnableNightMode());
			mProgress += 6;
			mProgressBar.setProgress(mProgress);
			mFullScreenProgress.setProgress(mProgress);
			mHandler.sendMessageDelayed(mHandler
					.obtainMessage(MESSAGE_UPDATE_PROGRESS_TO_END), 5);
		} else {
			mProgressBar.fadeOut(false, ConfigManager.getInstance().isEnableNightMode());
			mBtnRefresh.setVisibility(View.VISIBLE);
			mBtnStop.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.INVISIBLE);
			mFullScreenProgress.setVisibility(View.INVISIBLE);
			mProgressRunning = false;
		}
		try {
			TabViewManager.getInstance().getCurrentTabView().setProgress(100);
		} catch (Throwable e) {
		}
	}

	@Override
	public void notifyProgressTabSwitched(int progress,
										  boolean isHome, int tabId) {
		if (tabId != TabViewManager.getInstance().getCurrentTabId()) {
			return;
		}
		SimpleLog.d(TAG, "notifyProgressTabSwitched");
		if (progress >= 100) {
			SimpleLog.d(TAG, "progress=100");
			if (isHome) {
				mBtnRefresh.setVisibility(View.GONE);
				mBtnStop.setVisibility(View.GONE);
			} else {
				mBtnRefresh.setVisibility(View.VISIBLE);
				mBtnStop.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.INVISIBLE);
				mFullScreenProgress.setVisibility(View.INVISIBLE);
				mProgressRunning = false;
				mProgress = 0;
			}
		} else {
			SimpleLog.d(TAG, "progress=" + String.valueOf(progress));
			mProgress = progress;
			mProgressRunning = false;
			if (isHome) {
				mProgressBar.setVisibility(View.INVISIBLE);
				mFullScreenProgress.setVisibility(View.INVISIBLE);
			} else {
				doProgressChange(mProgress);
			}
		}
	}

	@Override
	public void notifyProgressInvisible() {
		mProgressRunning = false;
		mProgressBar.setVisibility(View.INVISIBLE);
		mFullScreenProgress.setVisibility(View.INVISIBLE);
	}

	private void doProgressChange(int progress) {
		if (AppEnv.sIsFullScreen) {
			mFullScreenProgress.setVisibility(View.VISIBLE);
		}
		if (mAddressBar.isShown()) {
			mFullScreenProgress.setVisibility(View.INVISIBLE);
		}
		mProgressBar.setVisibility(View.VISIBLE);
		if (!mProgressRunning) {
			mProgress = progress;
			mProgressRunning = true;
			mProgressBar.setProgress(mProgress);
			if (AppEnv.sIsFullScreen) {
				mFullScreenProgress.setProgress(mProgress);
			}
		}
		mHandler.sendMessage(mHandler
				.obtainMessage(MESSAGE_UPDATE_PROGRESS));
		mBtnRefresh.setVisibility(View.GONE);
		mBtnStop.setVisibility(View.VISIBLE);
	}

	@Override
	public void notifyProgressStart(int tabId, boolean isHome) {
		if (TabViewManager.getInstance().getCurrentTabView() == null)
			return;
		if (tabId != TabViewManager.getInstance().getCurrentTabId()) {
			TabView tab = TabViewManager.getInstance().getTabViewByKey(tabId);
			if (tab != null) {
				tab.setProgress(30);
			}
			return;
		}
		if (!isHome) {
//			mProgressRunning = false;
			doProgressChange(0);
		} else {
			mFullScreenProgress.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
			mBtnRefresh.setVisibility(View.GONE);
			mBtnStop.setVisibility(View.GONE);
		}
	}

	@Override
	public void notifyProgressFinished(int tabId, boolean isHome) {
		if (TabViewManager.getInstance().getCurrentTabView() == null)
			return;
		if (tabId != TabViewManager.getInstance().getCurrentTabId()) {
			TabView tab = TabViewManager.getInstance().getTabViewByKey(tabId);
			if (tab != null) {
				tab.setProgress(100);
			}
			return;
		}
		TabViewManager.getInstance().getCurrentTabView().setProgress(100);
		if (isHome) {
			mBtnRefresh.setVisibility(View.GONE);
			mBtnStop.setVisibility(View.GONE);
		} else {
			mHandler.removeMessages(MESSAGE_UPDATE_PROGRESS);
			Message msg = new Message();
			msg.what = MESSAGE_UPDATE_PROGRESS_TO_END;
			mHandler.sendMessage(msg);
		}
	}
}
