package com.polar.browser.i;

import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

/**
 * 浏览器退出接口
 *
 * @author dpk
 */
public interface IExitBrowser {
	void exitFromBackBtn(RxFragmentActivity mainActivity);

	void executeExit(RxFragmentActivity mainActivity);

}
