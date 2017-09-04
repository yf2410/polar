package com.polar.browser.cropedit;

import android.graphics.Bitmap;

public interface IFinishedCrop {
	public void finishedCrop(Bitmap bitmap);

	public void cancelCrop();
}