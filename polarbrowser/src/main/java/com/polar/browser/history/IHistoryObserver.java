package com.polar.browser.history;

/**
 * IHistoryObserver接口，用于通知History的改变
 * <p/>
 * 谁需要知道History的改变，就实现一个该接口，并调用HistoryManager.registerObserver进行注册
 * 当History改变时，会回调notifyChanged()方法，实现该函数即可
 *
 * @author dpk
 */
public interface IHistoryObserver {

	void notifyChanged(int dataType);
}
