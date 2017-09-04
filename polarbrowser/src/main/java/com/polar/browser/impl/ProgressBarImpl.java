package com.polar.browser.impl;

import com.polar.browser.i.IAddressBar;
import com.polar.browser.i.IProgressCallback;

public class ProgressBarImpl implements IProgressCallback {

	private IAddressBar mAddressBar;

	public ProgressBarImpl(IAddressBar addressBar) {
		mAddressBar = addressBar;
	}

	@Override
	public void onProgressStart(int tabId, boolean isHome) {
		mAddressBar.notifyProgressStart(tabId, isHome);
	}

	@Override
	public void onProgressFinished(int tabId, boolean isHome) {
		mAddressBar.notifyProgressFinished(tabId, isHome);
	}

	@Override
	public void progressChanged(int progress, boolean isHome) {
	}
}
