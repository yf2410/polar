package com.polar.browser.homepage.homecard;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.polar.browser.R;
import com.polar.browser.homepage.customlogo.HomeLogoView;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.view.ObservableScrollView;

import java.lang.reflect.Method;

/**
 * Created by FKQ on 2016/12/23.
 */

public class HomeWebCardController {

    private static final String TAG = "HomeWebCardController";

    private Context mContext;
    private ViewGroup mRoot;
    private WebView mWebView;
    private static final String CARD_ACTION_DEFAULT = "1";
    public static final String HOME_WEB_CARD_URL = "file:///android_asset/html/homecard.html";

    private Task mTask;
    private int mCardSlideHeight = 0;

    public HomeWebCardController(ViewGroup root) {
        this.mRoot = root;
        this.mContext = mRoot.getContext();

        initWebView();
        mWebView.loadUrl(HOME_WEB_CARD_URL);
    }

    private void initWebView() {
        mWebView = (WebView) mRoot.findViewById(R.id.home_web_card);

        // 必须在加载页面前先注入对象，否则执行脚本时，会找不到java对象
        mWebView.addJavascriptInterface(new JsInterfaceHomeCard(), "card");
        mWebView.setHorizontalScrollBarEnabled(false);//水平不显示
        mWebView.setVerticalScrollBarEnabled(false); //垂直不显示
        mWebView.setScrollContainer(false);
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT < 18) {
            try {
                settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            } catch (Exception ignored) {
            }
        }
        // 设置ua和viewport的支持
        String ua = mWebView.getSettings().getUserAgentString();
        String newUa = ua + " vcbrowser";
        settings.setUserAgentString(newUa);
        settings.setUseWideViewPort(false);
        settings.setSupportZoom(true);
        // 支持缩放
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        // 缓存相关
        setPageCache(settings);
        settings.setDomStorageEnabled(true);
        String appCacheDir = mContext.getDir("cache", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(appCacheDir);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void setPageCache(WebSettings settings) {
        try {
            Object[] args = {5};
            Method m = WebSettings.class.getMethod("setPageCacheCapacity",
                    int.class);
            m.invoke(settings, args); // wSettings是WebSettings对象
        } catch (Exception ignored) {
        }

//        try {
//            Object[] args = {Integer.valueOf(5)};
//            Method m = WebSettings.class.getMethod("setPageCacheCapacity",
//                    new Class[]{int.class});
//            m.invoke(settings, args); // wSettings是WebSettings对象
//        } catch (Exception e) {
//        }
    }

    public class JsInterfaceHomeCard {

        public JsInterfaceHomeCard() {
        }

        @android.webkit.JavascriptInterface
        public void onCardClickBack(final String action, final String cardType, final String url) {

            ThreadManager.postTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(action) || TextUtils.isEmpty(url)) {
                        return;
                    }
                    SimpleLog.d(TAG, "action==" + action + ",,,cardType==" + cardType + ",,url==" + url);
                    switch (action) {
                        case CARD_ACTION_DEFAULT:
                            TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_NORMAL);
                            break;
                        case "2":
                            break;
                        case "3":
                            break;
                        default:
                            break;

                    }
                }
            });

        }

        @android.webkit.JavascriptInterface
        public void onCardSlideHeight(float height) {
            SimpleLog.d(TAG, "onCardSlideHeight-float="+height);
            mCardSlideHeight = (int) (height * mContext.getResources().getDisplayMetrics().density);
//            mCardSlideHeight = (int)height;
            SimpleLog.d(TAG, "onCardSlideHeight="+mCardSlideHeight);

        }

        @android.webkit.JavascriptInterface
        public void onContentSize(float height) {
            SimpleLog.d(TAG, "onContentSize=="+height);
            mTask=new Task(height);
            ThreadManager.postTaskToUIHandler(mTask);
        }

        @android.webkit.JavascriptInterface
        public void onClickCardMrg() {
        }

        @android.webkit.JavascriptInterface
        public void onStatistics(final String key, final String value, final String tag) {
            Statistics.sendOnceStatistics(key, value, tag);
        }

        @android.webkit.JavascriptInterface
        public String vurl() {
            return Statistics.getHomeWebCradUrl();
        }

//        private void callHtmlAction(final String action) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    String callHtmlAction = String.format("javascript: callHtmlAction('%s');", action);
//                    if (mWebView != null) {
//                        mWebView.loadUrl(callHtmlAction);
//                    }
//                    SimpleLog.d(TAG, "callHtmlAction="+callHtmlAction);
//                }
//            });
//
//        }

    }


    /***
     * 自定义任务
     */
    private class Task implements  Runnable{
        private float mHeight;
        private Task(float height){
            this.mHeight=height;
        }

        @Override
        public void run() {
            if (mWebView != null) {
                mWebView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (mHeight * mContext.getResources().getDisplayMetrics().density)));
                if (ConfigManager.getInstance().isHomeCardSlideTip()) {
                    final ObservableScrollView view = (ObservableScrollView) mRoot.findViewById(R.id.home_scroll_view);
                    mWebView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final HomeLogoView homeLogoView = (HomeLogoView) mRoot.findViewById(R.id.view_homepage_logo);
                            final int homeLogoViewHeight = homeLogoView.getHeight();
                            LinearLayout adContainer = (LinearLayout) mRoot.findViewById(R.id.native_ad_container);
                            final int adContainerHeight = adContainer.getHeight();

                            view.smoothScrollTo(0, mCardSlideHeight + homeLogoViewHeight + adContainerHeight);
                            String callHtmlAction = String.format("javascript:callHtmlAction('%s');", CARD_ACTION_DEFAULT);
                            mWebView.loadUrl(callHtmlAction);
                            SimpleLog.d(TAG, "callHtmlAction==" + callHtmlAction);
                            ConfigManager.getInstance().setHomeCardSlideTip(false);
                            SimpleLog.d(TAG, "homeLogoViewHeight=" + homeLogoViewHeight +
                                    ",mNativeAdContainerHeight=" + adContainerHeight + ",mCardSlideHeight=" + mCardSlideHeight);
                        }
                    }, 1000);
                }
            }
        }

    }

    public void onDestroy() {
//        mWebView.removeJavascriptInterface("card");
//        mWebView.removeAllViews();
//        mWebView.destroy();
//        mWebView = null;

        /**移除掉任务，避免异步造成的内存泄漏**/
        if(mTask!=null){
            ThreadManager.getUIHandler().removeCallbacks(mTask);
        }

        if (mWebView != null) {
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = mWebView.getParent();

            if (parent != null) {
                ((ViewGroup) parent).removeView(mWebView);
            }

            mWebView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.clearView();
            mWebView.removeAllViews();
            try {
                mWebView.destroy();
            } catch (Throwable ex) {

            }
        }
    }
}
