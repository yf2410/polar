package com.polar.browser.common.data;

public class ConfigData {

	private boolean mIsEnableImg;

	private boolean mIsEnableNightMode;

	public ConfigData() {
		mIsEnableImg = true;
		mIsEnableNightMode = false;
	}

	public ConfigData(boolean isEnableImg, boolean isEnableNightMode) {
		mIsEnableImg = isEnableImg;
		mIsEnableNightMode = isEnableNightMode;
	}

	public boolean isEnableImg() {
		return mIsEnableImg;
	}

	public boolean isEnableNightMode() {
		return mIsEnableNightMode;
	}
}
