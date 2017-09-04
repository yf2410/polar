package com.polar.browser.thread;

import android.os.Looper;

import com.polar.browser.base.BaseHandler;

public class LogicHandler extends BaseHandler {

	private static LogicHandler sInstace;

	private LogicHandler(Looper looper) {
		super(looper);
	}

	public static LogicHandler getInstance(Looper looper) {
		if (sInstace == null) {
			sInstace = new LogicHandler(looper);
		}
		return sInstace;
	}
}
