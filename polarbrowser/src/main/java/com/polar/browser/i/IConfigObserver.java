package com.polar.browser.i;

/**
 * 用于通知Config改变的接口
 *
 * @author dpk
 */
public interface IConfigObserver {

	public void notifyChanged(String key, boolean value);

	public void notifyChanged(String key, String value);

	public void notifyChanged(String key, int value);
}
