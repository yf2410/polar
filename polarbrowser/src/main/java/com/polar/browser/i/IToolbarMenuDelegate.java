package com.polar.browser.i;

/**
 * IToolbarMenuDelegate，定义了底部工具条上的菜单操作
 *
 * @author dpk
 */
public interface IToolbarMenuDelegate {

	void openFavorite();

	void openHistory();

	void openSettings();

	void openDownload();

	void exitBrowser();

	void openShare();

	void switchImgMode();

	void switchNightMode();

	void switchWebMode();

	void openSystemNewsListActivity();

	void switchAdBlockMode();

}
