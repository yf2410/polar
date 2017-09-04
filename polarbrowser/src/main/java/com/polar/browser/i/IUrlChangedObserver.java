package com.polar.browser.i;

public interface IUrlChangedObserver {

	public void notifyUrlChanged(String url, int tabId, boolean isOnlyUpdateToolbar, String rawUrl);
}
