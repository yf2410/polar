package com.polar.business.search;

import com.polar.browser.jni.NativeManager;
import com.polar.browser.utils.FileUtils;

/**
 * 获取推荐网址列表
 */
public class SuggestTask2 implements Runnable {

	private String mKey;
	private ISuggestCallBack mCallBack;

	public SuggestTask2(String key, ISuggestCallBack callBack) {
		this.mKey = key;
		this.mCallBack = callBack;
	}

	@Override
	public void run() {
		String result = NativeManager.addressInput(FileUtils.stringToUtf8Bytes(mKey));
		mCallBack.callBack(result);
	}
}
