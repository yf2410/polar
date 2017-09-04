package com.polar.browser.impl;

import com.polar.browser.common.data.ConfigData;
import com.polar.browser.i.IJsCallbackDelegate;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.tabview.TabView;

public class JsCallbackImpl implements IJsCallbackDelegate {
	@Override
	public void showContent(final TabView tab, final String url) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				// 如果该页面曾经打开过其他页面，回到主页，并且主页不能再回退。此时又点击了其他链接
				// 需要清除之前的记录
				if (tab.isShowHome() && !tab.canGoBack()) {
					ConfigData config = new ConfigData(ConfigManager.getInstance().isEnableImg(),
							ConfigManager.getInstance().isEnableNightMode());
					tab.reset(config);
				}
				tab.setFontSize(TabViewManager.getInstance().getFontSize());
				tab.showContent(url);
			}
		};
		ThreadManager.postTaskToUIHandler(r);
	}
}
