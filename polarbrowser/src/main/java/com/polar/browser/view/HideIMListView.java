package com.polar.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.polar.browser.i.IHideIMListener;

public class HideIMListView extends ListView {

	private IHideIMListener mHideIMListener;

	public HideIMListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HideIMListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HideIMListView(Context context) {
		super(context);
	}

//	public void setHideImListener(IHideIMListener hideIMListener) {
//		this.mHideIMListener = hideIMListener;
//	}

	// 12-05 点击隐藏键盘
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
//		hideIM();
		return super.dispatchTouchEvent(ev);
	}

//	private void hideIM() {
//		// 隐藏键盘
////		((Activity)getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
//		if (mHideIMListener != null) {
//			mHideIMListener.onIMHide();
//		}
//	}
}
