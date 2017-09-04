package com.polar.browser.video.share;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.UrlUtils;

/**
 * Created by yd_lp on 2017/3/10.
 * facebook 网页视频分享工具类
 */

public class FbVideoShareUtils {
    public static final String FACEBOOK_HOST = "m.facebook.com";
    private static final String FACEBOOK_JS = "vc-changeVideo.js";
    private static String sFacebookJs;

    private FbVideoShareUtils() {

    }

    public static boolean isInFacebook(String url) {
        if (!TextUtils.isEmpty(url)) {
            String host = UrlUtils.getHost(url);
            if (host != null && host.contains(FACEBOOK_HOST)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 注入facebook js 脚本
     */
    public static void insertFacebookJs(final WebView webview) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (webview != null
                        && TabViewManager.getInstance().isWebViewInvalid(
                        webview)) {
                    if (TextUtils.isEmpty(sFacebookJs)) {
                        byte[] js = FileUtils.readFileFromAssets(JuziApp.getAppContext(),
                                FACEBOOK_JS);
                        sFacebookJs = new String(js);
                    }
                    injectPluginScriptInner(webview, CommonData.EXEC_JAVASCRIPT + sFacebookJs);
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private static void injectPluginScriptInner(final WebView webview, final String pluginScript) {
        if (!TextUtils.isEmpty(pluginScript) && webview != null) {
            ThreadManager.postTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            webview.evaluateJavascript(pluginScript, new ValueCallback() {
                                @Override
                                public void onReceiveValue(Object arg0) {
                                }
                            });
                        } else {
                            webview.loadUrl(CommonData.EXEC_JAVASCRIPT + pluginScript);
                        }
                    } catch (Throwable e) {
                    }
                }
            });
        }
    }
}
