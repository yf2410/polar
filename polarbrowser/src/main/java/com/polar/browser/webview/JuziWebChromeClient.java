package com.polar.browser.webview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.polar.browser.common.data.CommonData;
import com.polar.browser.i.IProgressStart;
import com.polar.browser.i.IWebChromeClientDelegate;
import com.polar.browser.i.IWebViewClientDelegate;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.PermissionsHelper;
import com.polar.browser.utils.UrlUtils;

public class JuziWebChromeClient extends WebChromeClient {

	private IReceivedTitle mTitleCallback;
	private Activity mActivity;
	private IProgressStart mProgressStart;
	private IWebChromeClientDelegate mChromeClientDelegate;
	private IWebViewClientDelegate mDelegate;
	public JuziWebChromeClient(IWebViewClientDelegate webviewDelegate, IReceivedTitle titleCallback, Activity activity, IProgressStart progressStart, IWebChromeClientDelegate delegate) {
		mTitleCallback = titleCallback;
		mActivity = activity;
		mProgressStart = progressStart;
		mChromeClientDelegate = delegate;
		mDelegate = webviewDelegate;
	}

	@Override
	public Bitmap getDefaultVideoPoster() {
		Bitmap bitmap = null;
		if (mChromeClientDelegate != null) {
			bitmap = mChromeClientDelegate.getDefaultVideoPoster();
		}
		if (bitmap != null) {
			return bitmap;
		}
		return super.getDefaultVideoPoster();
	}

	@Override
	public void onShowCustomView(View view, CustomViewCallback callback) {
		if (mChromeClientDelegate != null) {
			mChromeClientDelegate.onShowCustomView(view, callback);
			// 发送视频播放统计
			if (TabViewManager.getInstance() != null && TabViewManager.getInstance().getCurrentTabView() != null) {
				String url = TabViewManager.getInstance().getCurrentTabView().getUrl();
				if (!TextUtils.isEmpty(url)) {
					Statistics.sendDownloadResourceStatistics(2, url, "", "", "", 0, "", "");
				}
			}
			return;
		}
		super.onShowCustomView(view, callback);
	}

	@Override
	public void onHideCustomView() {
		if (mChromeClientDelegate != null) {
			mChromeClientDelegate.onHideCustomView();
			return;
		}
		super.onHideCustomView();
		return;
	}

	@Override
	public void onProgressChanged(WebView view, int newProgress) {
		mProgressStart.setProgress(view, newProgress);
		//TabViewManager.getInstance().getCurrentTabView().setRealProgress(newProgress);
		super.onProgressChanged(view, newProgress);
	}

	@Override
	public void onReceivedTitle(WebView view, String title) {
		super.onReceivedTitle(view, title);
		mTitleCallback.onReceivedTitle(title);
		mDelegate.onReceivedTitle(view, title);
		JavaScriptManager.injectOnReceivedTitle(view);
	}

	@Override
	public void onReceivedIcon(WebView view, final Bitmap icon) {
		final String host = UrlUtils.getHost(view.getUrl());
		final String path = String.format("%s/%s", view.getContext().getFilesDir().toString(),
				CommonData.ICON_DIR_NAME);
		Runnable r = new Runnable() {

			@Override
			public void run() {
				FileUtils.saveBitmapToFile(icon, path, host);
			}
		};
		ThreadManager.postTaskToIOHandler(r);
	}

	// TODO:请求获取地理位置信息
	@Override
	public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
		super.onGeolocationPermissionsShowPrompt(origin, callback);
		callback.invoke(origin, true, false);
		PermissionsHelper.requestPermissions(mActivity);
	}
	/*****************
	 * android中使用WebView来打开本机的文件选择器
	 *************************/
	// js上传文件的<input type="file" name="fileField" id="fileField" />事件捕获
	// Android > 4.1.1 调用这个方法
	public void openFileChooser(ValueCallback<Uri> uploadFile,
								String acceptType, String capture) {
        FileUtils.openFileChooser(mActivity, uploadFile, acceptType, capture);
	}
//    @Override
//    public boolean onCreateWindow(WebView view, boolean isDialog,
//    		boolean isUserGesture, Message resultMsg) {
//    	return true;
//    	// 当前窗口数量已经达到上限，不能再开新窗口进行处理了
//    	if (TabViewManager.getInstance().getSize() == TabViewManager.MAX_TAB_SIZE) {
//    		WebView webview = new WebView(view.getContext());
//    		webview.setWebViewClient(new WebViewClient() {
//    			@Override
//                public boolean shouldOverrideUrlLoading(WebView view, String url) {
//    				TabViewManager.getInstance().getCurrentTabView().loadUrl(url, NavigateSource.NORMAL);
//                    return true;  
//                }
//    		});
//    		
//    		setWebViewTransport(webview, resultMsg);
//        	return true;
//    	}
//    	
//    	// 页面开启新窗口时调用
//    	WebView webview = (WebView) TabViewManager.getInstance().addTabView(false).getContentView().getWebView().getView();
//		webview.addJavascriptInterface(mWebviewClient, "video");
//		webview.addJavascriptInterface(mWebviewClient, "domready");
//    	setWebViewTransport(webview, resultMsg);
//    	return true;
//    }
	// 设置新窗口打开时需要传递的参数
//    private void setWebViewTransport(WebView webview, Message resultMsg) {
//    	WebView.WebViewTransport webViewTransport = (WebViewTransport) resultMsg.obj;
//    	webViewTransport.setWebView(webview);
//    	resultMsg.sendToTarget();
//    }
	// 3.0 + 调用这个方法

	public void openFileChooser(ValueCallback<Uri> uploadMsg,
								String acceptType) {
		FileUtils.openFileChooser(mActivity, uploadMsg, acceptType, null);
	}

	// Android < 3.0 调用这个方法
	public void openFileChooser(ValueCallback<Uri> uploadMsg) {

	}

	@Override
	public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
									 FileChooserParams fileChooserParams) {
		FileUtils.openFileChooser(mActivity, filePathCallback, fileChooserParams);
		return true;
	}
	/**************
	 * end
	 ***************/


	@Override
	public View getVideoLoadingProgressView() {
		View view = null;
		if (mChromeClientDelegate != null) {
			view = mChromeClientDelegate.getVideoLoadingProgressView();
		}
		if (view != null) {
			return view;
		} else {
			return super.getVideoLoadingProgressView();
		}
	}

	public interface IReceivedTitle {
		public void onReceivedTitle(String title);
	}

	@Override
	public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
		WebView webview = (WebView) TabViewManager.getInstance().addTabView(false, true,false).getContentView().getWebView().getView();
    	setWebViewTransport(webview, resultMsg);
    	return true;
	}

	private void setWebViewTransport(WebView webview, Message resultMsg) {
    	WebView.WebViewTransport webViewTransport = (WebView.WebViewTransport) resultMsg.obj;
    	webViewTransport.setWebView(webview);
    	resultMsg.sendToTarget();
    }

	@Override
	public void onCloseWindow(WebView window) {
		super.onCloseWindow(window);
		int closeTabId = TabViewManager.getInstance().getCloseTabId(window);
		TabViewManager.getInstance().removeTabViewById(closeTabId);
	}
}
