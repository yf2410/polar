package com.polar.browser.i;


public interface ISlideDelegate {

	public void leftSlide(float offSet);

	public void rightSlide(float offSet);

	public void touchDown(float downX, float downY);

	public void touchUp(float upX, float upY);

	public void onOrientationChanged();
}
