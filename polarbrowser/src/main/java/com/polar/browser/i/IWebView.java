package com.polar.browser.i;

import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import java.util.Map;

public interface IWebView {

	public View getView();

	public void addJavascriptInterface(Object object, String name);

	public boolean canGoBack();

	public boolean canGoBackOrForward(int steps);

	public boolean canGoForward();

	public void clearCache(boolean includeDiskFiles);

	// Removes the autocomplete popup from the currently focused form field, if present.
	public void clearFormData();

	// Tells this WebView to clear its internal back/forward list.
	public void clearHistory();

	// Clears the highlighting surrounding text matches created by findAllAsync(String).
	public void clearMatches();

	public void clearView();

	public WebBackForwardList copyBackForwardList();

	public void destroy();

	public boolean dispatchKeyEvent(KeyEvent event);

	public void documentHasImages(Message response);

	public String findAddress(String addr);

	public void findAllAsync(String find);

	public String getOriginalUrl();

	public WebSettings getSettings();

	public String getTitle();

	public String getUrl();

	public void goBack();

	public void goBackOrForward(int steps);

	public void goForward();

	// Loads the given data into this WebView using a 'data' scheme URL.
	public void loadData(String data, String mimeType, String encoding);

	// Loads the given data into this WebView, using baseUrl as the base URL for the content.
	public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl);

	public void loadUrl(String url);

	// Loads the given URL with the specified additional HTTP headers.
	public void loadUrl(String url, Map<String, String> additionalHttpHeaders);

	public boolean onKeyDown(int keyCode, KeyEvent event);

	public boolean onKeyUp(int keyCode, KeyEvent event);

	public void onPause();

	public void onResume();

	public void postUrl(String url, byte[] postData);

	public void reload();

	public void removeJavascriptInterface(String name);

	public void setDownloadListener(DownloadListener listener);

	public void setWebChromeClient(WebChromeClient client);

	public void setWebViewClient(WebViewClient client);

	public void stopLoading();

	public boolean zoomIn();

	public boolean zoomOut();

	public void setFontSize(int size);

	public void saveWebArchive(String fileName);

	public void saveWebArchive(String basename, boolean autoname, ValueCallback<String> callback);

	public boolean isInvalid();

	public int getContentHeight();

	public float getScale();
}
