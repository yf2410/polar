package com.polar.browser.i;

import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.polar.browser.manager.ServiceManager;

public interface IWebViewClientDelegate {

    WebResourceResponse shouldInterceptRequest(WebView view, String url);

    boolean shouldOverrideUrlLoading(WebView view, String url);

    void onPageFinished(WebView view, String url, int tabId, final int src);

    void onPageStarted(WebView view, String url, int tabId);

    void registUrlChangedObserver(IUrlChangedObserver observer);

    void unregistUrlChangedObserver(IUrlChangedObserver observer);

    void doUpdateVisitedHistory(WebView view, String url, boolean isReload, int src, int tabId);

    void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm);

    void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

    // TODO:需要优化，把这个接口换个地方，否则与ServiceManager耦合，破坏架构
    void setServiceManager(ServiceManager manager);

    void setprogress(WebView webView, int progress);

    void switchCurrentWebView(View view);

    //在webview不同加载阶段回调
    void registWbLoadUrlStatusObserver(IWbLoadUrlStatusObserver observer);

    void unregistWbLoadUrlStatusObserver(IWbLoadUrlStatusObserver observer);

    void unregisterWbVideoPlayObserver();

    void findAllAsync(String text, WebView.FindListener findListener);

    void clearFind();

    void findNext(boolean isNext);

    public WebBackForwardList copyBackForwardList();

    public void onReceivedTitle(WebView view, String title);

    void setMainUrl(String url);

    void saveUrl();
}
