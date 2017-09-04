package com.polar.browser.push;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.util.GooglePlayUtil;

import java.lang.reflect.Method;

/**
 * Created by FKQ on 2016/7/14.
 */

public class loadWebDetailsActivity extends LemonBaseActivity {

    private WebView mWebView;
    private CommonTitleBar mCommonTitleBar;
    private ProgressBar mProgressBar;
    private RelativeLayout mRlContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notification);
        initWebview();
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case CommonData.ACTION_OPEN_RECOMMEND_DATA:     //点击推荐列表
                mCommonTitleBar.setTitle(R.string.preview);
                break;
            case CommonData.ACTION_OPEN_SYSTEMNEWS_DATA:    //点击消息推送列表
                mCommonTitleBar.setTitle(R.string.notification);
                Statistics.sendOnceStatistics(
                        GoogleConfigDefine.FCM_SYSTEM, GoogleConfigDefine.FCM_SYSTEM_DETAILS);
                break;
            default:
                break;
        }
        String url = intent.getStringExtra(CommonData.SYSTEM_CONTENT_URL);
        if (!TextUtils.isEmpty(url)){
            String checkedUrl = UrlUtils.checkUrlIsContainsHttp(url);
            loadWebUrl(checkedUrl);
        }
    }

    private void initWebview(){
        mCommonTitleBar = (CommonTitleBar) findViewById(R.id.preview_title_bar);
        mRlContainer = (RelativeLayout) findViewById(R.id.webview_container);
        mProgressBar = (ProgressBar) findViewById(R.id.preview_progress);
        mWebView = new WebView(this);
        mRlContainer.addView(mWebView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("market://")) {
                    //针对Go Downloader 市场链接处理（若检测本地已安装直接跳转下载器）
                    try {
                        Uri uri = Uri.parse(url);
                        String id = uri.getQueryParameter("id");
                        if (!TextUtils.isEmpty(id) && TextUtils.equals("com.go.downloader",id)) {
                            boolean packageInstalled = SysUtils.isPackageInstalled(view.getContext(), id);
                            if (packageInstalled) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                    intent.setAction("com.go.downloader.action.main");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ComponentName cn = new ComponentName(id, "com.go.downloader.MainActivity");
                                    intent.setComponent(cn);
                                    view.getContext().startActivity(intent);
                                    return true;
                                } catch (ActivityNotFoundException e) {
                                }

                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        GooglePlayUtil.goGooglePlayDetail(view.getContext(), url);
                    } catch (ActivityNotFoundException e) {
                        view.loadUrl(url.replace("market://", GooglePlayUtil.GOOGLE_PLAY_APP_STORE_URL_PREFIX));

//                        String a[] = url.split("/?");
//                        String appUrl = a[1];
//                       if (!TextUtils.isEmpty(appUrl)) {
//                           view.loadUrl(GooglePlayUtil.GOOGLE_PLAY_APP_DETAILS_URL_PREFIX + appUrl);
//                            Uri uri = Uri.parse(GooglePlayUtil.GOOGLE_PLAY_APP_DETAILS_URL_PREFIX + appUrl);
//                            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
//                        }
                    }
                    return true;
                } else if (GooglePlayUtil.isGooglePlayUrl(url)) {
                    Uri uri = Uri.parse(url);
                    String id = uri.getQueryParameter("id");
                    if (!TextUtils.isEmpty(id)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + id));
                        intent.setPackage(GooglePlayUtil.GOOGLE_PLAY_APP_PKGNAME);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            view.getContext().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            view.loadUrl(url);
                        }
                        return true;
                    }
                }
                    return false;
                }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT < 18) {
            try {
                settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            } catch (Exception e) {
            }
        }
        // 设置ua和viewport的支持
        String ua = mWebView.getSettings().getUserAgentString();
        String newUa = ua + " momeng" + " juziwang";
        settings.setUserAgentString(newUa);
        settings.setUseWideViewPort(true);
        // 支持缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // 缓存相关
        setPageCache(settings);
        settings.setDomStorageEnabled(true);
        String appCacheDir = JuziApp.getInstance().getDir("cache", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(appCacheDir);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void loadWebUrl(String url){
        mWebView.loadUrl(url);
    }

    private void setPageCache(WebSettings settings) {
        try {
            Object[] args = {Integer.valueOf(5)};
            Method m = WebSettings.class.getMethod("setPageCacheCapacity",
                    new Class[]{int.class});
            m.invoke(settings, args); // wSettings是WebSettings对象
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRlContainer.removeAllViews();
        mWebView.destroy();
    }
}
