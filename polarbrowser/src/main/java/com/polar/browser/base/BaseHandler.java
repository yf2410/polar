package com.polar.browser.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * BaseHandler
 *
 * @author dpk
 */
public class BaseHandler extends Handler {
	public BaseHandler(Looper looper) {
		super(looper);
	}

	public void quit() {
		Looper looper = getLooper();
		if (looper != null) {
			looper.quit();
		}
	}

	@Override
	public void handleMessage(Message msg) {
	}
}
