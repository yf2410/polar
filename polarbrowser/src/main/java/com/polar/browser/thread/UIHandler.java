package com.polar.browser.thread;

import android.os.Looper;
import android.os.Message;

import com.polar.browser.base.BaseHandler;

/**
 * 用于处理主线程请求的Handler，工作在主线程中
 *
 * @author dpk
 */
public class UIHandler extends BaseHandler {

	public UIHandler(Looper looper) {
		super(looper);
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		}
	}
}
