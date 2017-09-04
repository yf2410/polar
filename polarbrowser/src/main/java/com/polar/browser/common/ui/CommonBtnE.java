package com.polar.browser.common.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.polar.browser.R;


/**
 * 红button，下面无阴影
 */
public final class CommonBtnE extends CommonBtnB {

	public CommonBtnE(Context context) {
		super(context);
		initView();
	}

	public CommonBtnE(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setBackgroundResource(R.drawable.common_btn_e);
	}
}
