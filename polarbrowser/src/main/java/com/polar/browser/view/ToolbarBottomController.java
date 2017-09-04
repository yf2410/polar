package com.polar.browser.view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IShowOrHideDelegate;
import com.polar.browser.i.IToolbarBottom;
import com.polar.browser.i.IViewShownListener;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.setting.SettingActivity;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.ButtomTipBar;
import com.polar.browser.utils.DensityUtil;

public class ToolbarBottomController implements OnClickListener, IShowOrHideDelegate {
	private static final String TAG = "ToolbarBottomController";

	private static final int TOOLBAR_HEIGHT_DP = 40;
	// 按钮序号
	private static final int INDEX_BACK = 0;
	private static final int INDEX_FORWARD = 1;
	private static final int INDEX_MENU = 2;
	private static final int INDEX_HOME = 3;
	private static final int INDEX_SWITCH = 4;
	private static final int INDEX_MENU_DONE = 1;
	// 手指移动在多少以内的范围，认为是点击，目前是20dp
	private static final int CLICK_MOVE_RANGE_DP = 20;
	// Activity
	private Activity mActivity;
	private View mToolbarBottom;
	// 后退按钮
	private View mBackBtn;
	// 容纳后退按钮
	private View mBackLayout;
	// 前进按钮
	private View mForwardBtn;
	// 容纳前进按钮
	private View mForwardLayout;
	// Home键
	private View mHomeBtn;
	// 菜单键
	private View mMenuBtn;
	// 多窗口切换键
	private LongClickAddRelativeLayout mTabSwitchBtn;
	private ImageView mTabSwitchBorder;
	// 真正按下按钮进行操作的delegate
	private IToolbarBottom mToolbarBottomDelegate;
	// 容纳设置&隐藏菜单&分享
	private View mMenuDoneView;
	// 容纳前进&后退&显示菜单&主页&多标签
	private View mMenuShownView;
	// 容纳隐藏菜单
	private View mMenuDoneLayout;
	// 计算按下的坐标
	private float mTouchX;
	private float mTouchY;
	// 是否为按钮点击事件
	private boolean mToolbarBottomClick = false;
	// 当前按下的按钮
	private View mPressedBtn;
	//底部滑动引导View
	private RelativeLayout mSlideGuideView;
	private ToolbarMenuView mMenu;
	private boolean mToolbarBottomIsShown = true;
	private ImageView mMenuImg;

	private View mDot;
	/**关闭窗口按钮*/
	private View mCloseWindowLayout;
	private View mCloseWindowBtn;

	public ToolbarBottomController(Activity activity, IToolbarBottom delegate
								   , ToolbarMenuView menu) {

		mActivity = activity;
		mToolbarBottomDelegate = delegate;
		mMenu = menu;
		init();
		initListener();
	}

	private void initListener() {
		mBackLayout.setOnClickListener(this);
		mForwardLayout.setOnClickListener(this);
		mMenuBtn.setOnClickListener(this);
		mHomeBtn.setOnClickListener(this);
		mTabSwitchBtn.setOnClickListener(this);
		mMenuDoneLayout.setOnClickListener(this);
		mCloseWindowLayout.setOnClickListener(this);
		mTabSwitchBtn.setOnLoadAnimationListener(new NewTabAnimation.OnLoadListener() {
			@Override
			public void onLoadFinish() {
				//新建标签
				TabViewManager.getInstance().addTabView(true,true,true);
			}
			@Override
			public void onLoadCancel() {
			}
		});
	}

	public void destroy() {

	}

	public void init() {
		mToolbarBottom = mActivity.findViewById(R.id.toolbar_bottom);
		mBackBtn = mActivity.findViewById(R.id.backward);
		mBackLayout = mActivity.findViewById(R.id.rl_backward);
		mForwardBtn = mActivity.findViewById(R.id.forward);
		mForwardLayout = mActivity.findViewById(R.id.rl_forward);
		mHomeBtn = mActivity.findViewById(R.id.rl_home);
		mMenuBtn = mActivity.findViewById(R.id.rl_menu);
		mMenuImg = (ImageView) mActivity.findViewById(R.id.menu);
		mTabSwitchBtn = (LongClickAddRelativeLayout)mActivity.findViewById(R.id.rl_multiwindow);
		mTabSwitchBorder = (ImageView) mTabSwitchBtn.findViewById(R.id.iv_multi);
		mMenuDoneView = mActivity.findViewById(R.id.menu_done_view);
		mMenuShownView = mActivity.findViewById(R.id.menu_shown);
		mMenuDoneLayout = mActivity.findViewById(R.id.rl_menu_done);
		mDot = mActivity.findViewById(R.id.toolbar_dot);
		mSlideGuideView = (RelativeLayout) mActivity.findViewById(R.id.rl_bottom_tip);
		mCloseWindowLayout = mActivity.findViewById(R.id.rl_close_window);
		mCloseWindowBtn = mActivity.findViewById(R.id.close_window);
		mBackBtn.setEnabled(false);
		mBackLayout.setEnabled(false);
		mForwardBtn.setEnabled(false);
		mForwardLayout.setEnabled(false);
		setPrivacyMode(ConfigManager.getInstance().isPrivacyMode());
		mMenu.setShownListener(new IViewShownListener() {

			@Override
			public void onViewShown() {
				mMenuDoneView.setVisibility(View.VISIBLE);
				mMenuShownView.setVisibility(View.GONE);
			}

			@Override
			public void onViewHide() {
				mMenuDoneView.setVisibility(View.GONE);
				mMenuShownView.setVisibility(View.VISIBLE);
			}
		});
	}


	public void refreshDownloadUI() {
		if (ConfigManager.getInstance().isHasDownload()) {
			mDot.setVisibility(View.VISIBLE);
		} else {
			mDot.setVisibility(View.GONE);
		}
	}


	private void judgeShowSlideGuide() {
		if (!AppEnv.sIsShownSlideGuide) {
			ConfigManager.getInstance().setShownSlideGuide();
			String slidingTip = mActivity.getResources().getString(R.string.setting_sliding_type_tip);
			String settingTip = mActivity.getResources().getString(R.string.setting);
			ButtomTipBar.showButtomTipBar(mActivity, mSlideGuideView, new ButtomTipBar.onTipBtnListener() {
				@Override
				public void onClickSetting(View v) {
					Intent intent = new Intent(mActivity, SettingActivity.class);
					intent.setAction(SettingActivity.ACTION_EMPHASIZE_SLIDING);
					mActivity.startActivity(intent);
					mActivity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				}

				@Override
				public void onClickClose(View v) {

				}
			},slidingTip,settingTip);
			AppEnv.sIsShownSlideGuide = true;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_backward:
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER, GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER_BACK);
				// 后退
				judgeShowSlideGuide();
				mToolbarBottomDelegate.goBack();
				break;
			case R.id.rl_forward:
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER, GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER_FORWARD);
				// 前进
				judgeShowSlideGuide();
				mToolbarBottomDelegate.goForward();
				break;
			case R.id.rl_home:
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER, GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER_HOME);
				// 主页
				mToolbarBottomDelegate.goHome();
				break;
			case R.id.rl_menu:
				mDot.setVisibility(View.GONE);
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER, GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER_MENU);
				// 设置菜单
				mMenuImg.setBackgroundResource(R.drawable.toolbar_menu_selector);
				//打开菜单
				mToolbarBottomDelegate.openMenu();
				break;
			case R.id.rl_multiwindow:
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER, GoogleConfigDefine.TOOLBARBOTTOMCONTROLLER_MULTIWINDOWVIEW);
				// 多窗口管理
				mToolbarBottomDelegate.switchTab();
				break;
			//菜单按钮
			case R.id.rl_menu_done:
				// 隐藏菜单
				mToolbarBottomDelegate.openMenu();
				break;
			//关闭标签
			case R.id.rl_close_window:
				mToolbarBottomDelegate.closeWindow();
				break;
			default:
				break;
		}
	}

	/**
	 * 更新back键和forward键的状态
	 */
	public void updateBackAndForward(boolean isHome, boolean canGoForward, boolean canGoBack) {
		if (isHome) {
			if (canGoBack) {
				mBackBtn.setEnabled(true);
				mBackLayout.setEnabled(true);
			} else {
				mBackBtn.setEnabled(false);
				mBackLayout.setEnabled(false);
			}
			if (canGoForward) {
				mForwardBtn.setEnabled(true);
				mForwardLayout.setEnabled(true);
			} else {
				mForwardBtn.setEnabled(false);
				mForwardLayout.setEnabled(false);
			}
		} else {
			mBackBtn.setEnabled(true);
			mBackLayout.setEnabled(true);
			if (canGoForward) {
				mForwardBtn.setEnabled(true);
				mForwardLayout.setEnabled(true);
			} else {
				mForwardBtn.setEnabled(false);
				mForwardLayout.setEnabled(false);
			}
		}
	}

	public void onOrientationChanged() {
	}

	public int getHeight() {
		return DensityUtil.dip2px(mActivity, TOOLBAR_HEIGHT_DP);
	}

	@Override
	public void show() {
		Animation anim = AnimationUtils.loadAnimation(mActivity,
				R.anim.toolbar_bottom_out);
		mToolbarBottom.startAnimation(anim);
		mToolbarBottom.setVisibility(View.VISIBLE);
		mToolbarBottomIsShown = true;
//		SysUtils.setFullScreen(mActivity, false);
	}

	@Override
	public void showWithoutAnim() {
		mToolbarBottom.setVisibility(View.VISIBLE);
		mToolbarBottomIsShown = true;
	}

	@Override
	public void hideWithoutAnim() {
		mToolbarBottom.setVisibility(View.GONE);
		mToolbarBottomIsShown = false;
	}

	@Override
	public void hide() {
		Animation anim = AnimationUtils.loadAnimation(mActivity,
				R.anim.toolbar_bottom_in);
		mToolbarBottom.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				mToolbarBottom.setVisibility(View.GONE);
				mToolbarBottomIsShown = false;
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});
	}

	@Override
	public boolean isShown() {
		return mToolbarBottomIsShown;
	}

	public void setPrivacyMode(boolean privacyMode) {
		if (privacyMode) {
			mTabSwitchBorder.setBackgroundResource(R.drawable.toolbar_multiwindow_privacy_selector);
		} else {
			mTabSwitchBorder.setBackgroundResource(R.drawable.toolbar_multiwindow_selector);
		}
	}

	public void refreshMenuPushUI(boolean hint) {
		if (hint) {
			mDot.setVisibility(View.VISIBLE);
		} else {
			mDot.setVisibility(View.GONE);
		}
	}

	public void updateBackAndForward(boolean isHome, boolean canGoForward, boolean canGoBack,
									 boolean isFromBottomMenu) {
		if (isHome) {
            showCloseWindowLayout(false);
			if (canGoBack) {
				mBackBtn.setEnabled(true);
				mBackLayout.setEnabled(true);
			} else {
				mBackBtn.setEnabled(false);
				mBackLayout.setEnabled(false);
			}
			if (canGoForward) {
				mForwardBtn.setEnabled(true);
				mForwardLayout.setEnabled(true);
			} else {
				mForwardBtn.setEnabled(false);
				mForwardLayout.setEnabled(false);
			}
		} else {
			mBackBtn.setEnabled(true);
			mBackLayout.setEnabled(true);
			if (canGoForward) {
				mForwardBtn.setEnabled(true);
				mForwardLayout.setEnabled(true);
			} else {
				mForwardBtn.setEnabled(false);
				mForwardLayout.setEnabled(false);
			}
			if (!isFromBottomMenu) {
				if (canGoBack) {
                    showCloseWindowLayout(false);
				} else {
                    showCloseWindowLayout(true);
				}
			} else {
                showCloseWindowLayout(false);
            }
		}
	}
	/**显示关闭窗口布局*/
	private void showCloseWindowLayout(boolean show) {
        if (show) {
            mCloseWindowLayout.setVisibility(View.VISIBLE);
            mBackLayout.setVisibility(View.GONE);
        } else {
            mCloseWindowLayout.setVisibility(View.GONE);
            mBackLayout.setVisibility(View.VISIBLE);
        }
    }
}

