package com.polar.browser.common.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;

import com.polar.browser.R;

/**
 * {@hide}
 */
class CustomBtn extends Button {

	private boolean mDefineTextSize;

	public CustomBtn(Context context) {
		super(context);
		init();
	}

	public CustomBtn(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (attrs != null) {
			int aCount = attrs.getAttributeCount();
			for (int i = 0; i < aCount; i++) {
				String name = attrs.getAttributeName(i);
				if (!TextUtils.isEmpty(name) && name.equals("textSize")) {
					mDefineTextSize = true;
				}
			}
		}
		init();
	}

	private void init() {
		if (!mDefineTextSize) {
			setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.common_font_size_d));
		}
	}
}
