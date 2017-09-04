package com.polar.browser.i;

public interface IScrollChanged {
	// 滚动条停止滚动
	public void onScrollChanged();

	// 滚动条上移
	public void onScrollUp();

	// 滚动条下移
	public void onScrollDown();

	// 滚动条需要出现
	public void onScrollShow();
}
