package com.polar.browser.loginassistant.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.polar.browser.R;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.utils.SimpleLog;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lzk-pc on 2017/3/30.
 */

public class FacebookWebLoginActivity extends Activity {

    private static final String TAG = FacebookWebLoginActivity.class.getSimpleName();

    private WebView mLoginWebView;
    private WebView mWebviewPop;

    private static final String BASIC_URL = "http://account.polarbrowser.com/userac/login1.html";
//    private static final String BASIC_URL = "http://52.67.49.61:8081/userac/login1.html";
    private static final String target_url_prefix = BASIC_URL;

    public static final int RESULT_CODE_LOGIN_SUCCESS = 0x11;
    public static final int RESULT_CODE_LOGIN_FAIL = 0x22;
    public static final int RESULT_CODE_AUTH_FAIL = 0x33; //用户禁止授权
    public static final int RESULT_CODE_NORMAL_BACK = 0x44; //正常返回
    public static final String RESULT_CODE_AUTH_INPUT_TOKEN = "input_token"; //用户禁止授权

    private static final String INJECT_JS = "(function(){\n" +
            "\t\tvar fwb = document.getElementsByClassName('fwb').length;\n" +
            "\t\tif (fwb>0) {\n" +
            "\t\t\tdocument.getElementsByClassName('fwb')[0].innerText=control.getAppName()\n" +
            "\t\t}else{\n" +
            "\t\t\tvar _52je = document.getElementsByClassName('_52je').length;\n" +
            "\t\t\tif(_52je>0){\n" +
            "\t\t\t\tdocument.getElementsByClassName('_52je')[0].innerHTML = control.getAppName()\n" +
            "\t\t\t}\n" +
            "\t\t\tvar header = document.getElementById('header');\n" +
            "\t\t\tvar oDiv = document.createElement('div');\n" +
            "\t\t\toDiv.style.cssText = 'height:100%;width:50px;background:url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAzCAMAAADivasmAAAAZlBMVEUAAAD///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+Vn2moAAAAIXRSTlMAiPb8+fHiRuqsSe1gUj4zLCgg50EkVzswBgQ41VtOvadHcCdOAAAAvElEQVRIx7XW0Q6CMAyFYURxMgWnoqIo2vd/SS+kIV6Q9K/h3H9ZsnVts6mE9y1jCbmUzDS5iJR3KJipv0LyhEVtFddiEA0WwSo6LPaDWMUZxZaKVkWPRWUVaeMVS7M4qLiYxcktnlZxVrEzi7VXiFkcVTwWpqgAUUHICwLfKdw4boy/C3/9sSqJ4ZXMf9iY9I+p7Kb96TC8j+H+2jtMBKbgpgMGTDEwK8FEZnOfm+TYYdBupYLuYyxxUnwAx5pIN4+lAN4AAAAASUVORK5CYII=) no-repeat center center;position:absolute;left:0;top:0;background-size:16px 16px';\n" +
            "\t\t\theader.appendChild(oDiv)\n" +
            "\t\t\toDiv.onclick =function() {\n" +
            "\t\t\t\t control.back();\n" +
            "\t\t\t}\n" +
            "\t\t}\n" +
            "\t})()\n";

    private FrameLayout mContainer;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_web_login);
        initView();
        initData();
    }

    private void initView() {
        mLoginWebView = (WebView) findViewById(R.id.activity_facebook_web_login);
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);
    }

    private void initData() {

        //Cookie manager for the webview
        mLoginWebView.addJavascriptInterface(new JsBrige(), "control");
        WebSettings webSettings = mLoginWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
//        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //TODO
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        //These two lines are specific for my need. These are not necessary
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mLoginWebView, true);
        } else {
            cookieManager.setAcceptCookie(true);
        }
        mLoginWebView.setWebViewClient(new MyCustomWebViewClient());
        mLoginWebView.setWebChromeClient(new UriWebChromeClient());
        mLoginWebView.loadUrl(BASIC_URL);

        String cookie = CookieManager.getInstance().getCookie("facebook.com");
        SimpleLog.d(TAG, "facebook.com cookie:" + cookie);
    }

    /***
     * js 本地 交互
     */

    private class JsBrige {
        @JavascriptInterface
        public String getAppName() {
            return getString(R.string.app_name);
        }

        @JavascriptInterface
        public void getInputToken(String input_token) {
            SimpleLog.d(TAG,"getInputTokenSuccess");
            if(!TextUtils.isEmpty(input_token)){
                Intent intent = new Intent(FacebookWebLoginActivity.this, LoginActivity.class);
                intent.putExtra(RESULT_CODE_AUTH_INPUT_TOKEN,input_token);
                FacebookWebLoginActivity.this.setResult(RESULT_CODE_LOGIN_SUCCESS,intent);
                FacebookWebLoginActivity.this.finish();
                destroyWebView();
            }else{  //登录失败
                setResultAndFinish(RESULT_CODE_LOGIN_FAIL);
            }
        }

        @JavascriptInterface
        public void getInputTokenFail(String errorMsg){
            SimpleLog.d(TAG,"getInputTokenFail");
            setResultAndFinish(RESULT_CODE_LOGIN_FAIL);
        }

        @JavascriptInterface
        public void back(){
            SimpleLog.d(TAG,"back");
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (mLoginWebView.isFocused() && mLoginWebView.canGoBack()) {
            mLoginWebView.goBack();
        } else {
//            super.onBackPressed();
            setResultAndFinish(RESULT_CODE_NORMAL_BACK);
        }
    }

    private void setResultAndFinish(int resultCode){
        FacebookWebLoginActivity.this.setResult(resultCode);
        FacebookWebLoginActivity.this.finish();
        destroyWebView();
    }


    private class MyCustomWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.d(TAG, "shouldInterceptRequest - " + url);
            String final_url = url;

            if (!TextUtils.isEmpty(url) && url.startsWith("https://fb-s-c-a.akamaihd.net/h-ak-xaf1/v/")) {
                Log.i(TAG, "url has been intercept");
                WebResourceResponse response = null;
                try {
                    InputStream is = getAssets().open("ic_launcher.png");
                    response = new WebResourceResponse("image/png", "UTF-8", is);
                    return response;
                } catch (IOException e) {
                    e.printStackTrace();
                    return super.shouldInterceptRequest(view, url);
                }
            } else {
                return super.shouldInterceptRequest(view, final_url);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            String host = Uri.parse(url).getHost();
            Log.d(TAG, "shouldOverrideUrlLoading - " + url+" host = "+host);


            if (url.startsWith("http:") || url.startsWith("https:")) {

                if (Uri.parse(url).getPath().equals("/connection-compte.html")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASIC_URL));
                    startActivity(browserIntent);

                    return true;
                }

                if (host.equals(target_url_prefix)) {
                    if (mWebviewPop != null) {
                        mWebviewPop.setVisibility(View.GONE);
                        mContainer.removeView(mWebviewPop);
                        mWebviewPop = null;
                    }
                    return false;
                }
                if (host.equals("m.facebook.com") || host.equals("mobile.facebook.com") || host.equals("www.facebook.com") || host.equals("facebook.com")) {
                    SimpleLog.d(TAG,"into host = "+host);
                    return false;
                }
                // Otherwise, the link is not for a page on my site, so launch
                // another Activity that handles URLs
/*                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);*/
                return true;
            }
            // Otherwise allow the OS to handle it
            else if (url.startsWith("tel:")) {
                Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(tel);
                return true;
            }
            //This is again specific for my website
            else if (url.startsWith("mailto:")) {
                Intent mail = new Intent(Intent.ACTION_SEND);
                mail.setType("application/octet-stream");
                String AdressMail = new String(url.replace("mailto:", ""));
                mail.putExtra(Intent.EXTRA_EMAIL, new String[]{AdressMail});
                mail.putExtra(Intent.EXTRA_SUBJECT, "");
                mail.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(mail);
                return true;
            }
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d("onReceivedSslError", "onReceivedSslError");

        }

        @Override
        public void onPageFinished(WebView view, String url) {

            Log.d(TAG, "onPageFinished - " + url);

            if(url.startsWith("https://m.facebook.com/login.php")){
                if (mWebviewPop == null) {
                    return;
                }
                JavaScriptManager.injectFacebookLoginJs(mWebviewPop);
//                mWebviewPop.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            mWebviewPop.evaluateJavascript("javascript:"+INJECT_JS, null);
//                        } else {
//                            mWebviewPop.loadUrl("javascript:"+INJECT_JS);
//                        }
//                    }
//                });
            }
            else if (url.startsWith("https://m.facebook.com/v2.8/dialog/oauth") || url.startsWith("https://www.facebook.com/dialog")) {
                if (mWebviewPop != null) {
                    mWebviewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebviewPop);
                    mWebviewPop = null;
                }
                view.loadUrl(BASIC_URL);
                return;
            }
            super.onPageFinished(view, url);
        }
    }

    private class UriWebChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            Log.d(TAG, "onCreateWindow - " );
            mWebviewPop = new WebView(FacebookWebLoginActivity.this.getApplicationContext());
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); //TODO
            CookieManager cookieManager = CookieManager.getInstance();
//          cookieManager.removeAllCookie();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(mWebviewPop, true);
            } else {
                cookieManager.setAcceptCookie(true);
            }
            mWebviewPop.getSettings().setSavePassword(false);
            mWebviewPop.addJavascriptInterface(new JsBrige(), "control");
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d(TAG, "onCloseWindow");
        }

    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SimpleLog.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            SimpleLog.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void destroyWebView(){
/*        if(mLoginWebView != null){
            ((ViewGroup)mLoginWebView.getParent()).removeView(mLoginWebView);
            mLoginWebView.destroy();
        }*/
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup view = (ViewGroup) getWindow().getDecorView();
                view.removeAllViews();
            }
        });
    }
}