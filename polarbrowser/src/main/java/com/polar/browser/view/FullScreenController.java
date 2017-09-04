package com.polar.browser.view;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.polar.browser.R;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.i.IFullScreenDelegate;
import com.polar.browser.i.IShowOrHideDelegate;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.DensityUtil;

public class FullScreenController implements OnClickListener, IFullScreenDelegate, IConfigObserver {

	private static final int FULL_SCREEN_BTN_WIDTH = 110;
	private Activity mActivity;
	private FullScreenButton mFullScreenBtn;
	private View mFullScreenProgress;
	private IShowOrHideDelegate mToolbarBottom;
	private IShowOrHideDelegate mAddressBar;
	private boolean isUIShown;
	private boolean isFullScreen;
	private SearchPageController mSearchPageBar;
	public FullScreenController(Activity activity) {
		this.mActivity = activity;
	}

	public void init(IShowOrHideDelegate toolbarBottom, IShowOrHideDelegate addressBar,SearchPageController searchPageBar) {
		mToolbarBottom = toolbarBottom;
		mAddressBar = addressBar;
		mSearchPageBar = searchPageBar;
		mFullScreenBtn = (FullScreenButton) mActivity.findViewById(R.id.full_screen_btn);
		mFullScreenBtn.setOnClickListener(this);
		mFullScreenProgress = mActivity.findViewById(R.id.fullscreen_progress);
		isFullScreen = ConfigManager.getInstance().isFullScreen();
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				int leftMargin = AppEnv.SCREEN_WIDTH - FULL_SCREEN_BTN_WIDTH - DensityUtil.dip2px(mActivity, 10);
				int topMargin = AppEnv.SCREEN_HEIGHT - FULL_SCREEN_BTN_WIDTH - DensityUtil.dip2px(mActivity, 10);
				mFullScreenBtn.init(leftMargin, topMargin);
				if (isFullScreen) {
					mFullScreenBtn.setVisibility(View.VISIBLE);
				}
			}
		}, 100);
		if (isFullScreen) {
//			mFullScreenBtn.setVisibility(View.VISIBLE);
			isUIShown = false;
		} else {
			mFullScreenBtn.setVisibility(View.GONE);
			isUIShown = true;
		}
		ConfigManager.getInstance().registerObserver(this);
	}

	@Override
	public void onClick(View v) {
		// 点击按钮，只是显示toolbar，但并未退出全屏，再点击屏幕后，还会回到全屏状态
		showUI(true);
		mFullScreenProgress.setVisibility(View.INVISIBLE);
	}

	public void showUI(boolean isFront) {
		if (isFront) {
			mToolbarBottom.show();
		} else {
			mToolbarBottom.showWithoutAnim();
		}

		if(mSearchPageBar.isNeedShow()){
			mSearchPageBar.show();
		}else{
			mAddressBar.show();
		}
		mFullScreenBtn.setVisibility(View.GONE);
		isUIShown = true;
	}

	public void hideUI(boolean isFront) {
		if (mToolbarBottom.isShown()) {
			if (isFront) {
				mToolbarBottom.hide();
			} else {
				mToolbarBottom.hideWithoutAnim();
			}
		}
		if (mAddressBar.isShown()) {
			mAddressBar.hide();
		}
		mFullScreenBtn.setVisibility(View.VISIBLE);
		isUIShown = false;
	}

	/**
	 * 地址栏和底部工具栏是否显示
	 *
	 * @return
	 */
	public boolean isUIShown() {
		return isUIShown;
	}

	@Override
	public void check2showUI() {
		if (isFullScreen && isUIShown) {
			hideUI(true);
		}
	}

	public void onOrientationChanged() {
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				int leftMargin = AppEnv.SCREEN_WIDTH - FULL_SCREEN_BTN_WIDTH - DensityUtil.dip2px(mActivity, 10);
				int topMargin = AppEnv.SCREEN_HEIGHT - FULL_SCREEN_BTN_WIDTH - DensityUtil.dip2px(mActivity, 10);
				mFullScreenBtn.init(leftMargin, topMargin);
			}
		}, 100);
	}

	public void onDestory() {
		ConfigManager.getInstance().unregisterObserver(this);
	}

	@Override
	public void notifyChanged(String key, boolean value) {
		if (key.equals(ConfigDefine.ENABLE_FULL_SCREEN)) {
			isFullScreen = value;
		}
	}

	@Override
	public void notifyChanged(String key, String value) {
	}

	@Override
	public void notifyChanged(String key, int value) {
	}
}
