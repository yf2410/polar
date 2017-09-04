package com.polar.browser.vclibrary.network;

import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Created by James on 2016/6/6.
 */

public class NoCookieJar implements CookieJar {


	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		return Collections.EMPTY_LIST;
	}
}
