package com.polar.browser.common.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.polar.browser.R;


/**
 * 灰button，下面无阴影，暂时只有titlebar上用
 */
public final class CommonBtnD extends CustomBtn {

	public CommonBtnD(Context context) {
		super(context);
		initView();
	}

	public CommonBtnD(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setTextColor(getResources().getColorStateList(R.drawable.common_btn_d_txt_color));
		setBackgroundResource(R.drawable.common_btn_d);
	}
}
