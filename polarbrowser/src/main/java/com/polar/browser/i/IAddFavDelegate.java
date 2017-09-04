package com.polar.browser.i;

import android.content.Context;

public interface IAddFavDelegate {
	public void addFav();

	public void addLogo();

	public void addShortcut(Context c);

	public String getTitle();

	public String getUrl();

	public void addFav(String title, String url);

	public void addLogo(String title, String url);

	public void addShortcut(Context c, String title, String url);
}
