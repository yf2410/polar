package com.polar.browser.view;

import android.app.Activity;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.polar.browser.R;
import com.polar.browser.manager.ThreadManager;

public class NightModeAnimation {

	private Activity mActivity;

	// 无图模式动画
	private View mNightModeLayout;
	private ImageView mIconDay;
	private ImageView mIconNight;

	public NightModeAnimation(Activity activity, View root) {
		mActivity = activity;
	}

	public void init() {
		ViewStub stub = (ViewStub) mActivity.findViewById(R.id.night_mode_layout);
		mNightModeLayout = stub.inflate();
		mIconDay = (ImageView) mNightModeLayout.findViewById(R.id.iv_day_mode);
		mIconNight = (ImageView) mNightModeLayout.findViewById(R.id.iv_night_mode);
	}

	/**
	 * 开始执行夜间模式的动画
	 */
	public void startNightModeAnimation(final CallBack callBack) {
		Animation slideInAnim = AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.night_mode_in);
		Animation slideOutAnim = AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.night_mode_out);
		mNightModeLayout.setVisibility(View.VISIBLE);
		mIconDay.setVisibility(View.VISIBLE);
		mIconNight.setVisibility(View.VISIBLE);
		mIconDay.startAnimation(slideOutAnim);
		mIconNight.startAnimation(slideInAnim);
		slideInAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
//				ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
//					@Override
//					public void run() {
//						callBack.callBack();
//					}
//				}, 1500);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mIconNight.setVisibility(View.GONE);
				mNightModeLayout.setVisibility(View.GONE);
				callBack.animEnd();
				ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						callBack.callBack();
						mIconDay.clearAnimation();
						mIconNight.clearAnimation();
					}
				}, 0);
			}
		});
		slideOutAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mIconDay.setVisibility(View.GONE);
			}
		});
	}

	/**
	 * 开始执行白天模式的动画
	 */
	public void startDayModeAnimation(final CallBack callBack) {
		Animation slideInAnim = AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.night_mode_in);
		Animation slideOutAnim = AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.night_mode_out);
		mNightModeLayout.setVisibility(View.VISIBLE);
		mIconDay.setVisibility(View.VISIBLE);
		mIconNight.setVisibility(View.VISIBLE);
		mIconDay.startAnimation(slideInAnim);
		mIconNight.startAnimation(slideOutAnim);
		slideInAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
//				ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
//					@Override
//					public void run() {
//						callBack.callBack();
//					}
//				}, 1500);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mIconDay.setVisibility(View.GONE);
				mNightModeLayout.setVisibility(View.GONE);
				callBack.animEnd();
				ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						callBack.callBack();
						mIconDay.clearAnimation();
						mIconNight.clearAnimation();
					}
				}, 0);
			}
		});
		slideOutAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mIconNight.setVisibility(View.GONE);
			}
		});
	}

	public interface CallBack {
		public void callBack();

		public void animEnd();
	}
}
