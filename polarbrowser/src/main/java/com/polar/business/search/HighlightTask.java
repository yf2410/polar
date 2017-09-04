package com.polar.business.search;

import android.annotation.SuppressLint;
import android.text.TextUtils;

public class HighlightTask implements Runnable {

	private String mHandleText;
	private String mKey;
	private IHighlightCallBack mCallBack;

	public HighlightTask(String handleText, String key, IHighlightCallBack callBack) {
		this.mHandleText = handleText;
		this.mKey = key;
		this.mCallBack = callBack;
	}

	@Override
	public void run() {
		if (TextUtils.isEmpty(mHandleText) || TextUtils.isEmpty(mKey) || mCallBack == null) {
			return;
		}
		String oldKey = mKey;
		mHandleText = buildHighlight();
		mCallBack.callBack(oldKey, mHandleText);
	}


	@SuppressLint("DefaultLocale")
	private String buildHighlight() {
		StringBuilder sb = new StringBuilder();
		String lowerKey = mKey.toLowerCase();
		String lowerText = mHandleText.toLowerCase();
		int end = 0;
		while (end < lowerText.length()) {
			int start = end;
			end = lowerText.indexOf(lowerKey, start);
			if (end < 0) {
				sb.append(mHandleText.substring(start));
				break;
			}
			sb.append(mHandleText.substring(start, end));
			sb.append("<font color=\"#FFA500\"><b>");
			sb.append(mHandleText.substring(end, end + lowerKey.length()));
			sb.append("</b></font>");
			end += lowerKey.length();
		}
		return sb.toString();
	}
}
