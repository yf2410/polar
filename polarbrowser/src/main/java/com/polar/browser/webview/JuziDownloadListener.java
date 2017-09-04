package com.polar.browser.webview;

import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;

import com.polar.browser.i.IDownloadDelegate;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.utils.SimpleLog;

import java.net.URL;

public class JuziDownloadListener implements DownloadListener {

	private static final String TAG = "JuziDownloadListener";

	private IDownloadDelegate mDelegate;

	public JuziDownloadListener(IDownloadDelegate delegate) {
		mDelegate = delegate;
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition,
								String mimetype, long contentLength) {
		SimpleLog.d(TAG, "download url: " + url);
		SimpleLog.d(TAG, "userAgent: " + userAgent);
		SimpleLog.d(TAG, "contentDisposition url: " + contentDisposition);
		String pageUrl = null;
		String cookies = null;
		try {
			pageUrl = TabViewManager.getInstance().getCurrentUrl();
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		URL uRL = null;
		String hostUrl = null;
		if (!TextUtils.isEmpty(pageUrl)) {
			try {
				uRL = new URL(pageUrl);
				hostUrl = uRL.getProtocol() + "://" + uRL.getHost();
				// 百度云，不用给传Cookie....就能下载，传了cookie倒是有时候下载失败
//				if (TextUtils.equals(hostUrl, "http://yun.baidu.com")) {
//					SimpleLog.d(TAG, "onDownloadStart   yun.baidu.com /////////:");
//					if (mDelegate != null) {
//						mDelegate.onDownloadStart(pageUrl, url, cookies, userAgent, contentDisposition, mimetype, contentLength);
//					} else {
//						SimpleLog.d(TAG, "IDownloadDelegate is null !!:");
//					}
//					return;
//				}
			} catch (Exception e) {
				SimpleLog.e(e);
			}
		}

		if (!TextUtils.isEmpty(url)) {
			try {
				cookies = CookieManager.getInstance().getCookie(url);
			} catch (Exception e) {
				SimpleLog.e(e);
			}
		}

		if (TextUtils.isEmpty(cookies)) {
			if (!TextUtils.isEmpty(pageUrl)) {
				try {
					cookies = CookieManager.getInstance().getCookie(pageUrl);
				} catch (Exception e) {
					SimpleLog.e(e);
				}
				SimpleLog.e("", "pageUrl cookie == " + cookies);
			} else if (!TextUtils.isEmpty(hostUrl)) {
				try {
					cookies = CookieManager.getInstance().getCookie(hostUrl);
				} catch (Exception e) {
					SimpleLog.e(e);
				}
				SimpleLog.e("", "hostUrl cookie == " + cookies);
			}
		}
		SimpleLog.d(TAG, "onDownloadStart   All the cookies in a string:" + cookies);
		if (mDelegate != null) {
			mDelegate.onDownloadStart(pageUrl, url, cookies, userAgent, contentDisposition, mimetype, contentLength);
		} else {
			SimpleLog.d(TAG, "IDownloadDelegate is null !!:");
		}
	}
}