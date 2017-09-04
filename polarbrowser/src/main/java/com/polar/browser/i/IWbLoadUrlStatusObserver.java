package com.polar.browser.i;

public interface IWbLoadUrlStatusObserver {

	void notifyLoadingStatusChanged(int loadUrlStatu, String url);

}
