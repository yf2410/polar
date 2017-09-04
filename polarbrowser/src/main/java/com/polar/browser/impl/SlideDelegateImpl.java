package com.polar.browser.impl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.i.ISuspensionWindow;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.setting.SettingActivity;
import com.polar.browser.utils.ButtomTipBar;
import com.polar.browser.utils.SimpleLog;

public class SlideDelegateImpl implements ISlideDelegate {

	private static final int ANIM_DURATION = 400;
	private float mDownX;
	private Activity mActivity;
	private RelativeLayout mSlideGuideView;
	private ImageView mArrowBack;
	private ImageView mArrowForward;
	/** 控制 instagram 看图模式悬浮按钮显示or隐藏 **/
	private ISuspensionWindow mSuspensionWindow;

	public SlideDelegateImpl(Activity activity) {
		mActivity = activity;
		mArrowBack = (ImageView) mActivity.findViewById(R.id.iv_arrow_back);
		mArrowForward = (ImageView) mActivity.findViewById(R.id.iv_arrow_forward);
		mArrowBack.setVisibility(View.GONE);
		mArrowForward.setVisibility(View.GONE);
		mSlideGuideView = (RelativeLayout) mActivity.findViewById(R.id.rl_bottom_tip);
		measureView(mArrowBack);
		measureView(mArrowForward);
		mArrowBack.setTranslationX(-mArrowBack.getMeasuredWidth());
		mArrowForward.setTranslationX(AppEnv.SCREEN_WIDTH + mArrowForward.getMeasuredWidth());
	}

	@Override
	public void leftSlide(float offSet) {
		if (!mArrowForward.isShown()) {
			mArrowBack.setVisibility(View.VISIBLE);
			mArrowBack.setTranslationX(-mArrowBack.getMeasuredWidth() + offSet);
		}
	}

	@Override
	public void rightSlide(float offSet) {
		if (!mArrowBack.isShown()) {
			mArrowForward.setVisibility(View.VISIBLE);
			mArrowForward.setTranslationX(AppEnv.SCREEN_WIDTH - offSet);
		}
	}

	@Override
	public void touchDown(float downX, float downY) {
		mDownX = downX;
		if (mSuspensionWindow != null) {
			mSuspensionWindow.hide();
		}
	}

	@Override
	public void touchUp(float upX, float upY) {
		SimpleLog.e("", "upX == " + upX);
		SimpleLog.e("", "mDownX == " + mDownX);
		SimpleLog.e("", "AppEnv.SCREEN_WIDTH == " + AppEnv.SCREEN_WIDTH);
		int type = ConfigManager.getInstance().getSlidingScreenMode();
		if (type == ConfigDefine.SLIDING_BACK_FORWARD_border) {
			handleSlidingBorder(upX);
		} else if (type == ConfigDefine.SLIDING_BACK_FORWARD_fullscreen) {
			handleSlidingFullScreen(upX);
		}
		if (mSuspensionWindow != null) {
			mSuspensionWindow.show();
		}
	}

	public void setSuspensionWindow(ISuspensionWindow suspensionWindow) {
		this.mSuspensionWindow = suspensionWindow;
	}

	private void rightArrowGo(float upX) {
		long duration = (long) (ANIM_DURATION * upX / AppEnv.SCREEN_WIDTH);
		if (duration <= 0) {
			duration = 100;
		}
		mArrowForward.animate()
				.translationX(-mArrowForward.getMeasuredWidth())
				.setDuration(duration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mArrowForward.setTranslationX(AppEnv.SCREEN_WIDTH);
						mArrowForward.setVisibility(View.GONE);
						TabViewManager.getInstance().goForward();
						//TODO   判断是否显示底部提示
						judgeShowSlideGuide();
						ConfigManager.getInstance().notifyGoBackOrGoFoward();
						ConfigManager.getInstance().setAdBlockToast(false);
					}
				});
	}

	private void rightArrowBack(float upX) {
		long duration = (long) (ANIM_DURATION * (AppEnv.SCREEN_WIDTH - upX) / AppEnv.SCREEN_WIDTH);
		if (duration <= 0) {
			duration = 100;
		}
		mArrowForward.animate()
				.translationX(AppEnv.SCREEN_WIDTH + mArrowForward.getMeasuredWidth())
				.setDuration(duration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mArrowForward.setTranslationX(AppEnv.SCREEN_WIDTH);
						mArrowForward.setVisibility(View.GONE);
					}
				});
	}

	private void leftArrowGo(float upX) {
		long duration = (long) (ANIM_DURATION * (AppEnv.SCREEN_WIDTH - upX) / AppEnv.SCREEN_WIDTH);
		if (duration <= 0) {
			duration = 100;
		}
		mArrowBack.animate()
				.translationX(mArrowBack.getMeasuredWidth())
				.setDuration(duration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mArrowBack.setTranslationX(-mArrowBack.getMeasuredWidth());
						mArrowBack.setVisibility(View.GONE);
						TabViewManager.getInstance().goBack();
						//TODO  判断是否显示底部提示
						judgeShowSlideGuide();
						ConfigManager.getInstance().notifyGoBackOrGoFoward();
						ConfigManager.getInstance().setAdBlockToast(false);
					}
				});
	}

	private void leftArrowBack(float upX) {
		long duration = (long) (ANIM_DURATION * upX / mArrowBack.getMeasuredWidth());
		if (duration <= 0) {
			duration = 100;
		}
		mArrowBack.animate()
				.translationX(-mArrowBack.getMeasuredWidth())
				.setDuration(duration)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mArrowBack.setTranslationX(-mArrowBack.getMeasuredWidth());
						mArrowBack.setVisibility(View.GONE);
					}
				});
	}


	private void handleSlidingFullScreen(float upX) {
		// 左侧的火箭需要动作
		if (mArrowBack.isShown()) {
			if (upX > mDownX && upX - mDownX > AppEnv.MIN_SLIDE_OFF_SET) {
				leftArrowGo(upX);
			} else {
				leftArrowBack(upX);
			}
		} else if (mArrowForward.isShown()) {
			if (upX < mDownX && mDownX - upX > AppEnv.MIN_SLIDE_OFF_SET) {
				rightArrowGo(upX);
			} else {
				rightArrowBack(upX);
			}
		}
	}

	private void handleSlidingBorder(float upX) {
		if (mDownX < AppEnv.MIN_SLIDE_BORDER) {
			if (!mArrowBack.isShown()) {
				return;
			}
			// 在左侧点击的
			if (upX < AppEnv.MIN_SLIDE_OFF_SET) {
				// 回去
				leftArrowBack(upX);
			} else {
				// 跑到最右边
				leftArrowGo(upX);
			}
		} else if (AppEnv.SCREEN_WIDTH - mDownX < AppEnv.MIN_SLIDE_BORDER) {
			if (!mArrowForward.isShown()) {
				return;
			}
			// 在右侧点击的
			if (AppEnv.SCREEN_WIDTH - upX < AppEnv.MIN_SLIDE_OFF_SET) {
				// 回去
				rightArrowBack(upX);
			} else {
				// 跑到最左边
				rightArrowGo(upX);
			}
		}
	}

	// 由于OnCreate里面拿不到header的高度所以需要手动计算
	private void measureView(View childView) {
		android.view.ViewGroup.LayoutParams p = childView.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int height = p.height;
		int childHeightSpec;
		if (height > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(height,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		childView.measure(childWidthSpec, childHeightSpec);
	}

	@Override
	public void onOrientationChanged() {
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				mArrowForward.setTranslationX(AppEnv.SCREEN_WIDTH);
				mArrowForward.setVisibility(View.GONE);
				mArrowBack.setTranslationX(-mArrowBack.getMeasuredWidth());
				mArrowBack.setVisibility(View.GONE);
			}
		}, 600);
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
}
