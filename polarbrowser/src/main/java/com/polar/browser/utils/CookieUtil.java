package com.polar.browser.utils;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class CookieUtil {

	public static void clearCookie(Context c) {
		CookieSyncManager.createInstance(c);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
	}
}
