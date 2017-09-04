package com.polar.browser.view;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;

public class ObservableScrollView extends NestedScrollView {

	public ObservableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ObservableScrollView(Context context) {
		this(context, null);
	}
}
