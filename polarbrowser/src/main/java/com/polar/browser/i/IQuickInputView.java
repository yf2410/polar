package com.polar.browser.i;

import com.polar.business.search.view.QuickInputView.InputDelegate;

public interface IQuickInputView {

	public void init(InputDelegate delegate);

	public void showWithAnim();

	public int getTopMargin();

	public void setTopMargin(int topMargin);

	public void setVisibility(int visibility);

	public void onOrientationChanged();
}
