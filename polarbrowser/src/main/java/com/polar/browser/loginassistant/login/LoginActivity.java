package com.polar.browser.loginassistant.login;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
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

import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.events.NotifLoginResultEvent;
import com.polar.browser.vclibrary.bean.login.UserAccountData;
import com.polar.browser.view.LoggingDialog;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.functions.Consumer;


/**
 * Crated by FKQ on 2017/3/30.
 */

public class LoginActivity extends LemonBaseActivity implements View.OnClickListener{

    private static final String TAG = LoginActivity.class.toString();

    private static final int FRAMEWORK_REQUEST_CODE = 1;
    private static final int FACEBOOK_LOGIN_REQUEST_CODE = 2;
//    private SubmitFeedBackDialog mSubmitFeedBackDialog;
    private LoggingDialog mSubmitFeedBackDialog;

    private ViewGroup mRootLayout; //根布局
    private View mOriginalLayout; // 最初界面

    //Facebook Login
    private WebView mLoginWebView;
    private WebView mWebviewPop;
    //private static final String BASIC_URL = "http://52.67.49.61:8081/userac/login1.html";
    private static final String BASIC_URL = "http://account.polarbrowser.com/userac/login1.html";
    private static final String target_url_prefix = BASIC_URL;

    private static final int RESULT_CODE_LOGIN_SUCCESS = 0x11;
    private static final int RESULT_CODE_LOGIN_FAIL = 0x22;
    private static final int RESULT_CODE_AUTH_FAIL = 0x33; //用户禁止授权
    private static final int RESULT_CODE_NORMAL_BACK = 0x44; //正常返回
    private static final String RESULT_CODE_AUTH_INPUT_TOKEN = "input_token"; //用户禁止授权

    private static final int FACEBOOK_LOGIN_TIMEOUT = 15000; //facebook登录超时时间

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mRootLayout = (ViewGroup) findViewById(R.id.root_setting);
        mOriginalLayout = findViewById(R.id.login_original_layout);
        findViewById(R.id.fl_phone_login).setOnClickListener(this);
        findViewById(R.id.fl_facebook_login).setOnClickListener(this);
        initTitleBar();

//        EventBus.getDefault().register(this);
        initLoginResultEvent();
    }

    private void initTitleBar(){
        CommonTitleBar titleBar = (CommonTitleBar)findViewById(R.id.title_bar);
        titleBar.setTitleBarBackground(R.color.white);
    }

    /**
     * 设置Facebook登录WebView是否可见
     * @param isShowWebView
     */
    public void setVisibleWebView(final boolean isShowWebView, final WebView webView){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isShowWebView && webView != null){  //显示WebView，隐藏自己的登录界面
                    mOriginalLayout.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }else{
                    if(mLoginWebView != null){
                        mLoginWebView.setVisibility(View.GONE);
                    }
                    if(mWebviewPop != null){
                        mWebviewPop.setVisibility(View.GONE);
                    }
                    mOriginalLayout.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    /**
     * 初始化Facebook登录WebView
     */
    private void initFacebookLoginView() {
        if(mLoginWebView == null){
            mLoginWebView = new WebView(LoginActivity.this.getApplicationContext());
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
            mRootLayout.addView(mLoginWebView);
            mLoginWebView.setVisibility(View.GONE);
            String cookie = CookieManager.getInstance().getCookie("facebook.com");
            SimpleLog.d(TAG, "facebook.com cookie:" + cookie);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 设置竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        //登录之前判断网络是否连接
        if(!NetworkUtils.isNetWorkConnected(this.getApplicationContext())){
            CustomToastUtils.getInstance().showTextToast(R.string.net_no_connect);
            return;
        }
        switch (v.getId()) {
            case R.id.fl_phone_login:
                intent = new Intent(this, AccountKitActivity.class);

                AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                        new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN);
                FbAdvancedUIManager fbAdvancedUIManager = new FbAdvancedUIManager(null,null,LoginType.PHONE,null, R.style.CustomFbLoginTheme);
                configurationBuilder.setUIManager(fbAdvancedUIManager);
                configurationBuilder.setTitleType(AccountKitActivity.TitleType.APP_NAME);
                final AccountKitConfiguration configuration = configurationBuilder.build();
                intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration);
                startActivityForResult(intent, FRAMEWORK_REQUEST_CODE);
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_FROM_PHONE_NUM_LOGIN);
                break;
            case R.id.fl_facebook_login:
                showLoggingDialog(true);
                initFacebookLoginView();
                mLoginWebView.loadUrl(BASIC_URL);
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_FROM_FACEBOOK_LOGIN);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != FACEBOOK_LOGIN_REQUEST_CODE){  //正常返回，不显示加载框
            showLoggingDialog(false);
        }
        switch (requestCode){
            case FRAMEWORK_REQUEST_CODE:
                final AccountKitLoginResult loginResult = AccountKit.loginResultWithIntent(data);
                if (loginResult == null || loginResult.wasCancelled()) {
                    SimpleLog.d("Login", "Login Cancelled");
//                    CustomToastUtils.getInstance().showTextToast("取消登录");
                    dismissLoggingDialog();
                } else if (loginResult.getError() != null) {
                    SimpleLog.d("Login", "loginResult.getError()=="+loginResult.getError().getErrorType().getMessage());
        //            showErrorActivity(loginResult.getError());
                    onNotifLoginResultEvent(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                } else {
                    final AccessToken accessToken = loginResult.getAccessToken();

                    if (accessToken != null) {
                        String token = accessToken.getToken();
                        if (!TextUtils.isEmpty(token)) {
                            String shadowToken = ConfigManager.getInstance().getShadowToken();
                            AccountLoginManager.getInstance().getUserAccountByPhone(accessToken.getToken(),shadowToken!=null?shadowToken:"");
                        } else {
                            SimpleLog.d("--Login--", "Login 登录失败");
//                            CustomToastUtils.getInstance().showTextToast("登录失败");
                            onNotifLoginResultEvent(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                            Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_PHONE_NUM);
                        }
                    } else {
                        SimpleLog.d("--Login--", "Login 登录失败");
//                        CustomToastUtils.getInstance().showTextToast("登录失败");
                        onNotifLoginResultEvent(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                        Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_PHONE_NUM);
                    }

                    if (AppEnv.DEBUG) {
                        final String authorizationCode = loginResult.getAuthorizationCode();
                        final long tokenRefreshIntervalInSeconds = loginResult.getTokenRefreshIntervalInSeconds();
                        if (accessToken != null) {
                            SimpleLog.d("Login", "getAccountId=="+accessToken.getAccountId());
                            SimpleLog.d("Login", "getApplicationId=="+accessToken.getApplicationId());
                            SimpleLog.d("Login", "getToken=="+accessToken.getToken());
                            SimpleLog.d("Login", "Login Success=="+accessToken.getAccountId()+tokenRefreshIntervalInSeconds);
                            //                showHelloActivity(loginResult.getFinalAuthorizationState());

                        } else if (authorizationCode != null) {
                            SimpleLog.d("Login", "Login Success=="+String.format("Success:%s...", authorizationCode.substring(0, 10)));
                            //                showHelloActivity(authorizationCode, loginResult.getFinalAuthorizationState());
                        } else {
                            SimpleLog.d("Login", "Unknown response type");
                        }
                    }
                }
                break;
        }
    }


    protected void onActivityResultForFbWebLogin(int requestCode, int resultCode, String inputToken) {
        if (requestCode == FACEBOOK_LOGIN_REQUEST_CODE ){
            switch (resultCode){
                case RESULT_CODE_LOGIN_SUCCESS:  //登录成功
                    String shadowToken = ConfigManager.getInstance().getShadowToken();
                    if (!TextUtils.isEmpty(inputToken)) {
                        AccountLoginManager.getInstance().getUserAccountByFacebook(inputToken,shadowToken!=null?shadowToken:"");
                    } else {
                        onNotifLoginResultEvent(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                        Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_FACEBOOK);
                    }
                    break;
                case RESULT_CODE_LOGIN_FAIL:
                case RESULT_CODE_AUTH_FAIL:  //授权失败,返回登录界面，不做任何提示
                    onNotifLoginResultEvent(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_FACEBOOK);
                    break;
                case RESULT_CODE_NORMAL_BACK:   //正常返回
                    onBackPressed();
                    break;
            }
        }
    }

    /**
     * 显示登录对话框
     */
    private void showLoggingDialog(final boolean isFacebookLogin){
        if(mSubmitFeedBackDialog == null){
            mSubmitFeedBackDialog = new LoggingDialog(this,R.string.account_logging_tip, R.drawable.login_loading);
            //超时只对facebook账号登录起作用，避免点击登录按钮后登录对话框一直显示
            mSubmitFeedBackDialog.setTimeoutListener(new LoggingDialog.OnShowTimeoutListener() {
                @Override
                public void showTimeout() {
                    if(isFacebookLogin && mSubmitFeedBackDialog != null && mSubmitFeedBackDialog.isShowing()
                            && mOriginalLayout.getVisibility() == View.VISIBLE){
                        onNotifLoginResultEvent(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                    }
                }
            },FACEBOOK_LOGIN_TIMEOUT);
        }
        mSubmitFeedBackDialog.show();
    }

    private void dismissLoggingDialog(){
        if(mSubmitFeedBackDialog != null){
            mSubmitFeedBackDialog.dismiss();
        }
    }

    private void onLoginResult() {
        if(ConfigManager.getInstance().isShowLoginSuccessTip()){
            CustomToastUtils.getInstance().showTextToast(R.string.account_login_success);
        }else{
            Intent intent = new Intent(this,BrowserActivity.class);
            intent.setAction(CommonData.ACTION_LOGIN_SUCCESS_TIP);
            startActivity(intent);
        }
        AccountLoginManager.getInstance().getUserData(ConfigManager.getInstance().getUserToken() , AccountLoginManager.APP_NAME);
        this.finish();
    }

    public void initLoginResultEvent() {
        RxBus.get().safetySubscribe(NotifLoginResultEvent.class,this).subscribe(new Consumer<NotifLoginResultEvent>() {
            @Override
            public void accept(NotifLoginResultEvent notifLoginResultEvent) throws Exception {
                onNotifLoginResultEvent(notifLoginResultEvent);
            }
        });
    }

    public void onNotifLoginResultEvent(NotifLoginResultEvent notifLoginResultEvent) {
        switch (notifLoginResultEvent.getType()) {
            case AccountLoginManager.NOTIFLOGINRESULT_SUCCESS:
                dismissLoggingDialog();
                onLoginResult();
                UserAccountData userAccountData = JuziApp.getUserAccountData();
                if (userAccountData != null) {
                    AccountLoginManager.getInstance().sendAccountLoginStatistics(AccountLoginManager.APP_NAME,userAccountData.getToken());
                }
                break;
            case AccountLoginManager.NOTIFLOGINRESULT_ERRO:
                dismissLoggingDialog();
                setVisibleWebView(false,null);
                CustomToastUtils.getInstance().showTextToast(getString(R.string.account_login_fail));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
        if(mLoginWebView != null){
            mRootLayout.removeView(mLoginWebView);
            mLoginWebView.destroy();
        }
        if(mWebviewPop != null){
            mRootLayout.removeView(mWebviewPop);
            mWebviewPop.destroy();
        }
        if(mSubmitFeedBackDialog != null){
            if(mSubmitFeedBackDialog.isShowing()){ mSubmitFeedBackDialog.dismiss();}
            mSubmitFeedBackDialog = null;
        }
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
            setFbLoginResult(RESULT_CODE_LOGIN_SUCCESS,input_token);
        }

        @JavascriptInterface
        public void getInputTokenFail(String errorMsg){
            SimpleLog.d(TAG,"getInputTokenFail");
            setFbLoginResult(RESULT_CODE_LOGIN_FAIL,null);
        }

        @JavascriptInterface
        public void back(){
            SimpleLog.d(TAG,"back");
            setFbLoginResult(RESULT_CODE_NORMAL_BACK,null);
        }
    }

    @Override
    public void onBackPressed() {
        if (mLoginWebView != null && mLoginWebView.isFocused() && mLoginWebView.canGoBack()) {
            mLoginWebView.goBack();
        }else if(mOriginalLayout.getVisibility() != View.VISIBLE){
            setVisibleWebView(false,mLoginWebView);
        }else{
            super.onBackPressed();
        }
    }

    public void setFbLoginResult(int code, String inputToken){
        onActivityResultForFbWebLogin(FACEBOOK_LOGIN_REQUEST_CODE,code,inputToken);
    }

    private class MyCustomWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.d(TAG, "shouldInterceptRequest - " + url);
/*            String final_url = url;

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
            } else {*/
                return super.shouldInterceptRequest(view, url);
//            }
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
                    setVisibleWebView(true,mLoginWebView);
                    if (mWebviewPop != null) {
                        mWebviewPop.setVisibility(View.GONE);
                        mRootLayout.removeView(mWebviewPop);
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
                dismissLoggingDialog();
                setVisibleWebView(true,mWebviewPop);
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
                setVisibleWebView(true,mLoginWebView);
                if (mWebviewPop != null) {
                    mWebviewPop.setVisibility(View.GONE);
                    mRootLayout.removeView(mWebviewPop);
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
            mWebviewPop = new WebView(LoginActivity.this.getApplicationContext());
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
            mRootLayout.addView(mWebviewPop);
            mWebviewPop.setVisibility(View.GONE);
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









}

