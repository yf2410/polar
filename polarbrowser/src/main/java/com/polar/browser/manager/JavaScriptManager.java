package com.polar.browser.manager;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.polar.browser.bean.JsInfo;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IWebView;
import com.polar.browser.shortcut.ParseConfig;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 管理js的注入
 * Created by yxx on 2017/3/9.
 */

public class JavaScriptManager {

    public static final String SUPER_CODE = "China" + String.valueOf(new Random().nextInt(50000));

    private static final String TAG = "JavaScriptManager";
    public static final String INSTAGRAM_HOST = "www.instagram.com";
    public static final String INSTAGRAM_HOME= "https://www.instagram.com/";
    public static final String FACEBOOK_HOST = "m.facebook.com";
    public static final String FACEBOOK_HOME = "https://m.facebook.com/";

    private static final String INSTAGRAM_JS = "vc-upImgIns.js";
    private static final String FACEBOOK_IMG_JS = "vc-upImgFb.js";
    private static final String INJECT_TIMING_ONE = "1";
    private static final String INJECT_TIMING_TWO = "2";
    private static final String INJECT_TIMING_THREE = "3";
    private static final String INJECT_TIMING_FOUR = "4";
    private static final String COMMON = "common";

    private static String sAlbumInsJs;
    private static String sAlbumInsAvailableJs;
    private static String sAlbumInsAdblockJs;
    private static String sFbNotiJs;
    private static String sFbLoginJs;

    private static List<JsInfo> mJsInfoList = new ArrayList<>();
    private static JsInfo jsbean;

    /**
     * 针对性的处理，比较的是url，并非host。
     * @param url
     * @param target
     * @return
     */
    private static boolean isTarget(String url, String target) {
        if (url != null && target != null && TextUtils.equals(url, target)) {
             return true;
        }
        return false;
    }

    /**
     * 看图模式js,IWebView  点击按钮调用。
     * @param webview
     */
    public static void injectAlbumInsJs(IWebView webview) {
        try {
            if (TextUtils.isEmpty(sAlbumInsJs)) {
                sAlbumInsJs = FileUtils.readFile(ParseConfig.sPluginPath  +  ParseConfig.VC_ALBUMINS + File.separator + ParseConfig.VC_ALBUMINS_JS);
            }
            if (sAlbumInsJs != null) {
                webview.loadUrl(CommonData.EXEC_JAVASCRIPT + sAlbumInsJs);
            }else {
//                CustomToastUtils.getInstance().showDurationToast("albumins_js_null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注入某个js
     * @param webView
     * @param jsInfo
     */
    private static void injectSomeJs(final WebView webView, String url, JsInfo jsInfo) {
        try {
            if (TextUtils.equals(jsInfo.getExtName(), ParseConfig.VC_ALBUMINS)) {//该js手动注入，不在这里处理
                return;
            }
            String configHosts = jsInfo.getHost();
            String host = new URL(url).getHost();
            if (!TextUtils.isEmpty(configHosts) && url != null) {
                if (configHosts.contains(host) || TextUtils.equals(configHosts, COMMON)) {//在配置的host中，或者配置的是common的  注入js  要求.json一定要配置host了。
                    final String js = FileUtils.readFile(ParseConfig.sPluginPath + File.separator + jsInfo.getExtName() + File.separator + jsInfo.getHook());
                    if (!TextUtils.isEmpty(js)) {
                        if (TextUtils.equals(jsInfo.getExtName(), ParseConfig.FACEBOOK_IMG)) {//fb  长按图片的js需要延迟注入
                            ThreadManager.postDelayedTaskToLogicHandler(new Runnable() {
                                @Override
                                public void run() {
                                    injectScriptIntoWebview(webView, CommonData.EXEC_JAVASCRIPT + js);
                                }
                            }, 3000);
                        }else {
                            injectScriptIntoWebview(webView, CommonData.EXEC_JAVASCRIPT + js);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void injectScriptIntoWebview(final WebView webview, final String pluginScript) {
        if (!TextUtils.isEmpty(pluginScript) && webview != null) {
            ThreadManager.postTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            webview.evaluateJavascript(pluginScript, new ValueCallback() {
                                @Override
                                public void onReceiveValue(Object arg0) {
                                    if (AppEnv.DEBUG)
                                        SimpleLog.e(TAG, "onReceiveValue:" + arg0.toString());
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


    public static void injectOnUpdateVisitedHistory(final WebView view, final String url) {
        processSpecialJs(view, url);
        injectScriptWithTiming(view, url, INJECT_TIMING_THREE);
    }

    public static void injectOnPageStarted(WebView view, String url) {
        injectScriptWithTiming(view, url, INJECT_TIMING_ONE);
    }

    public static void injectOnPageFinished(WebView view, String url) {
        injectScriptWithTiming(view, url, INJECT_TIMING_FOUR);
    }

    public static void injectOnReceivedTitle(WebView view) {
        injectScriptWithTiming(view,  "", INJECT_TIMING_TWO);
    }

    /**
     * 初始化js配置信息列表
     */
    public static void initJsInfoList() {
        try {
            mJsInfoList.clear();//避免重复
            Gson gson  = new Gson();
            File jsDir = new File(ParseConfig.sPluginPath);
            List<File> configFiles = new ArrayList<>();
            configFiles = FileUtils.getAllFiles(configFiles, jsDir.toString());
            if (configFiles != null && configFiles.size() > 0) {
                for (File file :configFiles) {
                   if (TextUtils.equals(file.getName(), ParseConfig.HASOFFER_JSON)) {continue;}
                    String jsonStr = FileUtils.readFile(file.getPath());
                    if (!TextUtils.isEmpty(jsonStr)) {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        String json = jsonObj.getString("vc-injectList");
                        jsbean = gson.fromJson(json, JsInfo.class);
                        if (jsbean != null && jsbean.getInjectTiming() != null &&  jsbean.getHost() != null) {
                            mJsInfoList.add(jsbean);
                        }
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private static void injectScriptWithTiming(WebView view, String url, String injectTiming) {
        if (mJsInfoList != null && mJsInfoList.size() > 0) {
            for (JsInfo info: mJsInfoList) {
                if (TextUtils.equals(info.getExtName(), ParseConfig.HASOFFER) || TextUtils.equals(info.getExtName(), ParseConfig.VC_FBNOTI)) {
                    continue;//比价和fb通知插件不在这里注入
                }
                if (info.getInjectTiming().contains(injectTiming)) {
                    injectSomeJs(view, url, info);
                }
            }
        }
    }

    /**
     * 注入判断Instagram是否登录js
     * @param webView
     */
    private static void injectAlbumInsAvailableJs(WebView webView) {
        try {
            if (!isTarget(webView.getUrl(), INSTAGRAM_HOME)) {
                return;
            }
            if (TextUtils.isEmpty(sAlbumInsAvailableJs)) {
                sAlbumInsAvailableJs = FileUtils.readFile(ParseConfig.sPluginPath  +  ParseConfig.VC_ALBUMINS + File.separator + ParseConfig.VC_ALBUMINS_AVAILABLE_JS);
            }
            if (sAlbumInsAvailableJs != null) {
                injectScriptIntoWebview(webView, CommonData.EXEC_JAVASCRIPT + sAlbumInsAvailableJs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * fb通知的js，单独的webview中注入，需要单独处理。
     * @param webview
     */
    public static void injectFbNotiJs(final WebView webview) {
        ThreadManager.postDelayedTaskToLogicHandler(new Runnable() {
            @Override
            public void run() {
                try {
                    if (TextUtils.isEmpty(sFbNotiJs)) {
                        sFbNotiJs = FileUtils.readFile(ParseConfig.sPluginPath  +  ParseConfig.VC_FBNOTI + File.separator + ParseConfig.VC_FBNOTI_JS);
                    }
                    if (sFbNotiJs != null) {
                        injectScriptIntoWebview(webview, CommonData.EXEC_JAVASCRIPT + sFbNotiJs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 5000);
    }

    /**
     * Facebook登录页js,IWebView  点击按钮调用。
     * @param webview
     */
    public static void injectFacebookLoginJs(final WebView webview) {
        try {
            if (TextUtils.isEmpty(sFbLoginJs)) {
                sFbLoginJs = FileUtils.readFile(ParseConfig.sPluginPath  +  ParseConfig.VC_FBLOGIN + File.separator + ParseConfig.VC_FBLOGIN_JS);
            }
            if (sFbLoginJs != null) {
                injectScriptIntoWebview(webview, CommonData.EXEC_JAVASCRIPT + sFbLoginJs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 注入屏蔽Instagram页面的广告（在应用中打开）
     * @param webView
     * @param url
     */
    private static void injectInsAdblockJs(WebView webView, String url) {
        try {
            if (url != null) {
                String host = new URL(url).getHost();
                if (TextUtils.equals(host, INSTAGRAM_HOST)) {
                    if (TextUtils.isEmpty(sAlbumInsAdblockJs)) {
                        sAlbumInsAdblockJs = FileUtils.readFile(ParseConfig.sPluginPath  +  ParseConfig.VC_ALBUMINS + File.separator + ParseConfig.VC_ALBUMINS_ADBLOCK_JS);
                    }
                    if (sAlbumInsAdblockJs != null) {
                        injectScriptIntoWebview(webView, CommonData.EXEC_JAVASCRIPT + sAlbumInsAdblockJs);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *需要单独处理，在时机3注入的js
     * @param view
     * @param url
     */
    private static void processSpecialJs(WebView view, String url) {
        injectAlbumInsAvailableJs(view);
        injectInsAdblockJs(view, url);
    }
}
