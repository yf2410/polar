package com.polar.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.polar.browser.env.AppEnv;
import com.polar.browser.utils.SimpleLog;

public class FullScreenButton extends ImageButton {

	private float mBtnWidth;
	private float mBtnHeight;

	/**
	 * 记录当前手指位置在屏幕上的横坐标值
	 */
	private float xInScreen;

	/**
	 * 记录当前手指位置在屏幕上的纵坐标值
	 */
	private float yInScreen;

	/**
	 * 记录手指按下时在屏幕上的横坐标的值
	 */
	private float xDownInScreen;

	/**
	 * 记录手指按下时在屏幕上的纵坐标的值
	 */
	private float yDownInScreen;

	/**
	 * 记录当前手指是否按下
	 */
	private boolean isPressed;

	private OnClickListener mClickListener;

	public FullScreenButton(Context context) {
		this(context, null);
	}

	public FullScreenButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(int leftMargin, int topMargin) {
		RelativeLayout.LayoutParams lp = (LayoutParams) getLayoutParams();
		lp.leftMargin = leftMargin;
		lp.topMargin = topMargin;
		setLayoutParams(lp);
	}

	public void setOnClickListener(OnClickListener clickListener) {
		mClickListener = clickListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mBtnWidth == 0) {
			mBtnWidth = getWidth();
			mBtnHeight = getHeight();
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				isPressed = true;
				// 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
				xDownInScreen = event.getRawX();
				yDownInScreen = event.getRawY();
				xInScreen = event.getRawX();
				yInScreen = event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				xInScreen = event.getRawX();
				yInScreen = event.getRawY();
				// 手指移动的时候更新小悬浮窗的状态和位置
				updateViewPosition();
				break;
			case MotionEvent.ACTION_UP:
				isPressed = false;
				// 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
				if (Math.abs(xDownInScreen - xInScreen) < 10 && Math.abs(yDownInScreen - yInScreen) < 10) {
					doBtnClick();
				}
				break;
			default:
				break;
		}
		updateViewStatus();
		return true;
	}

	private void updateViewPosition() {
		RelativeLayout.LayoutParams lp = (LayoutParams) getLayoutParams();
		int leftMargin = (int) (xInScreen - mBtnWidth / 2);
		int topMargin = (int) (yInScreen - mBtnHeight / 2);
		if (leftMargin < 0) {
			leftMargin = 0;
		} else if (xInScreen + mBtnWidth / 2 > AppEnv.SCREEN_WIDTH) {
			leftMargin = (int) (AppEnv.SCREEN_WIDTH - mBtnWidth);
		}
		if (topMargin < 0) {
			topMargin = 0;
		} else if (yInScreen + mBtnHeight / 2 > AppEnv.SCREEN_HEIGHT) {
			topMargin = (int) (AppEnv.SCREEN_HEIGHT - mBtnHeight);
		}
		lp.leftMargin = leftMargin;
		lp.topMargin = topMargin;
		setLayoutParams(lp);
	}

	private void updateViewStatus() {
		setPressed(isPressed);
	}

	private void doBtnClick() {
		SimpleLog.e("", "doBtnClick!!!!!!!!");
		if (mClickListener != null) {
			mClickListener.onClick(this);
		}
	}
}
