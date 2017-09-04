package com.polar.browser.i;

import android.view.View;

public interface IEditLogo {
	void openEditLogoView(int height, int scrollTop, View mainView, boolean addLogo);

	void onLongClickUp();
}
