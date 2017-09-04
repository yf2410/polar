package com.polar.browser.webview;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.utils.SimpleLog;

/**
 * Created by yxx on 2017/3/24.
 */

public class CustomFbWebViewClient extends WebViewClient {

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
//        JavaScriptManager.injectFbNotiJs(view);
        if (ConfigManager.getInstance().getFbMessageNotificationEngine()) {
            JavaScriptManager.injectFbNotiJs(view);
            SimpleLog.d("*******************************", "doUpdateVisitedHistory  inject js");
        }else {
            //TODO 要不要提示让用户打开notify功能。
        }
    }
}
