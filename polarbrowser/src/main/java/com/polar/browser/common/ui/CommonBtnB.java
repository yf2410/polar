package com.polar.browser.common.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.polar.browser.R;

/**
 * 绿button，下面无阴影
 */
public class CommonBtnB extends CustomBtn {

	public CommonBtnB(Context context) {
		super(context);
		initView();
	}

	public CommonBtnB(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		setTextColor(getResources().getColorStateList(R.drawable.common_btn_a_txt_color));
		setShadowLayer(3f, 0, 2f, 0x26000000);
		setBackgroundResource(R.drawable.common_btn_b);
	}
}
