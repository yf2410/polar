package com.polar.browser.webview;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.polar.browser.i.IWebViewClientDelegate;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.safe.ssl.SafeSslErrorHandler;
import com.polar.browser.tabview.ContentView;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.common.Constants;

/**
 * 用于接收WebViewClient回调事件，真正的处理过程交给delegate，由上层进行
 *
 * @author dpk
 */
public class JuziWebViewClient extends WebViewClient {
	private static final String TAG = "JuziWebViewClient";

	private IWebViewClientDelegate mDelegate;

	private ContentView mContent;

	public JuziWebViewClient(IWebViewClientDelegate delegate,
							 ContentView content) {
		mDelegate = delegate;
		mContent = content;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		SimpleLog.d(TAG, "shouldOverrideUrlLoading:" + "url");
		return mDelegate.shouldOverrideUrlLoading(view, url);
	}

	// 注意：不工作在主线程！
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		SimpleLog.d(TAG, "shouldInterceptRequest:" + url);
		return mDelegate.shouldInterceptRequest(view, url);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		mDelegate.onPageFinished(view, url, mContent.getTab().getId(), mContent.getSource());
		if (mContent.getSource() != Constants.NAVIGATESOURCE_NORMAL) {
			mContent.resetNavigateSource();
		}
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		mDelegate.onPageStarted(view, url, mContent.getTab().getId());
	}

	@Override
	public void doUpdateVisitedHistory(WebView view, String url,
									   boolean isReload) {
		mDelegate.doUpdateVisitedHistory(view, url, isReload,
				mContent.getSource(), mContent.getTab().getId());
	}

	@Override
	public void onReceivedHttpAuthRequest(WebView view,
										  HttpAuthHandler handler, String host, String realm) {
		mDelegate.onReceivedHttpAuthRequest(view, handler, host, realm);
	}

	@Override
	public void onReceivedError(WebView view, int errorCode,
								String description, String failingUrl) {
		super.onReceivedError(view, errorCode, description, failingUrl);
		mDelegate.onReceivedError(view, errorCode, description, failingUrl);
	}

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		if (ConfigManager.getInstance().isSafetyWarningEnabled()) {
			SafeSslErrorHandler.getInstance().onReceivedSslError(handler, error);
		} else {
			handler.proceed();
		}
    }

	public void setMainUrl(String url) {
		mDelegate.setMainUrl(url);
	}
}
