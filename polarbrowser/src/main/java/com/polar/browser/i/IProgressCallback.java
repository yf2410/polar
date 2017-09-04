package com.polar.browser.i;

public interface IProgressCallback {
	public void onProgressStart(int tabId, boolean isHome);

	public void onProgressFinished(int tabId, boolean isHome);

	public void progressChanged(int progress, boolean isHome);
}