package com.polar.browser.i;

/**
 * 地址栏接口，用于处理地址栏、进度条的一些显示、隐藏、更新进度的任务
 *
 * @author dpk
 */
public interface IAddressBar {
	// 通知进度条改变
	public void notifyProgressTabSwitched(int progress,
										  boolean isHome, int tabId);

	// 通知进度条需要隐藏，如返回了主页等情况
	public void notifyProgressInvisible();

	// 通知进度条开始运行
	public void notifyProgressStart(int tabId, boolean isHome);

	// 通知进度条停止运行
	public void notifyProgressFinished(int tabId, boolean isHome);
}
