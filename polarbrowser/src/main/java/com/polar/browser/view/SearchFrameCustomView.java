package com.polar.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.env.AppEnv;

public class SearchFrameCustomView extends View {

	public SearchFrameCustomView(Context context) {
		this(context, null);
	}

	public SearchFrameCustomView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SearchFrameCustomView(Context context, AttributeSet attrs,
								 int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
							int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int[] a = new int[2];
		getLocationInWindow(a);
		int currentY = a[1];
		if (android.os.Build.VERSION.SDK_INT > 18) {
			int height = getHeight();
			// TODO 大于0可能会有问题
			if (currentY > 0 && height != 0) {
				post(new Runnable() {
					@Override
					public void run() {
						ViewGroup.LayoutParams params = getLayoutParams();
						params.height = 0;
						setLayoutParams(params);
					}
				});
			} else if (currentY == 0 && height == 0) {
				post(new Runnable() {
					@Override
					public void run() {
						ViewGroup.LayoutParams params = getLayoutParams();
						params.height = AppEnv.STATUS_BAR_HEIGHT;
						setLayoutParams(params);
					}
				});
			}
		}
	}
}
