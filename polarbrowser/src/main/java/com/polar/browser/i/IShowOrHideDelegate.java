package com.polar.browser.i;

public interface IShowOrHideDelegate {

	public void show();

	public void showWithoutAnim();

	public void hide();

	public void hideWithoutAnim();

	public boolean isShown();
}
