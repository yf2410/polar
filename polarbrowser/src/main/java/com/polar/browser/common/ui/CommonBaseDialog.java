package com.polar.browser.common.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.polar.browser.utils.SimpleLog;

public class CommonBaseDialog extends Dialog {

	public CommonBaseDialog(Context context) {
		super(context);
	}

	public CommonBaseDialog(Context context, int theme) {
		super(context, theme);
	}

	@Override
	public void show() {
		try {
			Context context = getContext();
			if (context != null && context instanceof Activity) {
				if (((Activity) context).isFinishing()) {
					return;
				}
			}
			if (isShowing()) {
				return;
			}
			super.show();
		} catch (Throwable t) {
            t.printStackTrace();
			SimpleLog.e("dialog show error", t.getMessage());
		}
	}

	@Override
	public void dismiss() {
		try {
			Context context = getContext();
			if (context != null && context instanceof Activity) {
				if (((Activity) context).isFinishing()) {
					return;
				}
			}
			if (isShowing()) {
				super.dismiss();
			}
		} catch (Throwable t) {
			SimpleLog.e("dialog dismiss error", t.getMessage());
		}
	}
}
