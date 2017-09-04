package com.polar.business.search.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.i.IQuickInputView;
import com.polar.browser.utils.DensityUtil;

/**
 * 键盘上 www. .com . \ 等 view
 */
public class QuickInputView extends RelativeLayout implements IQuickInputView, android.view.View.OnClickListener, android.view.View.OnTouchListener {

	// 手指移动在多少以内的范围，认为是点击，目前是5dp
	private static final int CLICK_MOVE_RANGE_DP = 20;
	private InputDelegate mDelegate;
	private View mLeftView;
	private TextView mWww;
	private TextView mCom;
	private TextView mSlash;
	private View mTouchView;
	// 计算按下的坐标
	private float mTouchX;
	private float mTouchY;
	// 是否为按钮点击事件
	private boolean mToolbarBottomClick = false;
	// 当前按下的按钮
	private View mPressedBtn;


	public QuickInputView(Context context) {
		this(context, null);
	}

	public QuickInputView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.view_quick_input, this);
		initView();
		initListener();
	}

	private void initView() {
		mLeftView = findViewById(R.id.left_layout);
		mWww = (TextView) findViewById(R.id.www);
		mCom = (TextView) findViewById(R.id.com);
		mSlash = (TextView) findViewById(R.id.slash);
		mTouchView = findViewById(R.id.touch_view);
	}

	@Override
	public void init(InputDelegate delegate) {
		mDelegate = delegate;
	}

	private void initListener() {
		mWww.setOnClickListener(this);
		mCom.setOnClickListener(this);
		mSlash.setOnClickListener(this);
		mTouchView.setOnTouchListener(this);
	}

	@Override
	public void onClick(View v) {
		String inputStr = null;
		switch (v.getId()) {
			case R.id.www:
				inputStr = "www.";
				break;
			case R.id.slash:
				inputStr = "/";
				break;
			case R.id.com:
				inputStr = ".com";
				break;
			default:
				break;
		}
		if (mDelegate != null && inputStr != null) {
			mDelegate.input(inputStr);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handleActionDown(ev);
				return true;
			case MotionEvent.ACTION_MOVE:
				return handleActionMove(ev);
			case MotionEvent.ACTION_UP:
				return handleActionUp(ev);
		}
		return false;
	}

	private void handleActionDown(MotionEvent ev) {
		mTouchX = ev.getX();
		mTouchY = ev.getY();
		mToolbarBottomClick = true;
		mPressedBtn = getCurrentBottomController(mTouchX);
		if (mPressedBtn != null && mPressedBtn.isEnabled()) {
			mPressedBtn.setPressed(true);
		}
	}
	
	/*
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
//		if (mGesture.onTouchEvent(event)) {
//            event.setAction(MotionEvent.ACTION_CANCEL);
//		}
//		if (event.getAction() == MotionEvent.ACTION_UP) {
//			mBGListener.setSlided(false);
//		}
		
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handleActionDown(ev);
			return true;
		case MotionEvent.ACTION_MOVE:
			return handleActionMove(ev);
		case MotionEvent.ACTION_UP:
			return handleActionUp(ev);
		}
		return super.dispatchTouchEvent(ev);
	}
	*/

	private boolean handleActionMove(MotionEvent ev) {
		float curX = ev.getX();
		float curY = ev.getY();
		// 如果手指移动的范围很小，认为是点击
		if (mToolbarBottomClick
				&& Math.abs(mTouchX - curX) < DensityUtil.dip2px(getContext(),
				CLICK_MOVE_RANGE_DP)
				&& Math.abs(mTouchY - curY) < DensityUtil.dip2px(getContext(),
				CLICK_MOVE_RANGE_DP)) {
			mToolbarBottomClick = true;
			return true;
		} else {
			mToolbarBottomClick = false;
			if (mPressedBtn != null && mPressedBtn.isEnabled()) {
				mPressedBtn.setPressed(false);
			}
		}
		mTouchX = curX;
		mTouchY = curY;
		boolean ret = false;
		return ret;
	}

	private boolean handleActionUp(MotionEvent ev) {
		// 如果是点击，则执行点击，否则执行滑动
		if (mToolbarBottomClick) {
			View v = getCurrentBottomController(ev.getX());
			if (v != null && v.isEnabled()) {
				v.performClick();
				mToolbarBottomClick = false;
				if (mPressedBtn != null && mPressedBtn.isEnabled()) {
					mPressedBtn.setPressed(false);
				}
			} else {
			}
			return true;
		}
		return false;
	}

	private View getCurrentBottomController(float x) {
		if (x > mLeftView.getWidth() + mWww.getX() && x < mLeftView.getWidth() + mWww.getX() + mWww.getWidth()) {
			return mWww;
		}
		if (x > mLeftView.getWidth() + mCom.getX() && x < mLeftView.getWidth() + mCom.getX() + mCom.getWidth()) {
			return mCom;
		}
		if (x > mLeftView.getWidth() + mSlash.getX() && x < mLeftView.getWidth() + mSlash.getX() + mSlash.getWidth()) {
			return mSlash;
		}
		return null;
	}

	@Override
	public void showWithAnim() {
		clearAnimation();
		Animation animation = new AlphaAnimation(0, 1);
		animation.setDuration(500);
		animation.setStartOffset(150);
		setVisibility(View.VISIBLE);
//		startAnimation(animation);
	}

	@Override
	public int getTopMargin() {
		// TODO Auto-generated method stub
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		return lp.topMargin;
	}

	@Override
	public void setTopMargin(int topMargin) {
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) getLayoutParams();
		lp.topMargin = topMargin;
//		setLayoutParams(lp);
	}

	@Override
	public void onOrientationChanged() {
		// TODO Auto-generated method stub
	}

	public interface InputDelegate {
		public void input(String inputStr);
	}
}
