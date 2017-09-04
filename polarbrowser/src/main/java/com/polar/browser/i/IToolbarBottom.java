package com.polar.browser.i;

/**
 * 底部工具条接口
 * 共包含5+2个按钮的功能：
 * 1. 后退
 * 2. 前进
 * 3. 主页
 * 4. 打开菜单
 * 5. 切换标签
 * ---2015.3.2新添加---
 * 6. 设置
 * 7. 分享
 *
 * @author dpk
 */
public interface IToolbarBottom {
	public void goBack();

	public void goForward();

	public void goHome();

	public void openMenu();

	public void switchTab();

	void closeWindow();
}
