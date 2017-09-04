package com.polar.browser.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.adblock.AdFilter;
import com.polar.browser.bean.LoginAccountInfo;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.download.DownloadHelper;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IProgressCallback;
import com.polar.browser.i.IScrollChanged;
import com.polar.browser.i.IUrlChangedObserver;
import com.polar.browser.i.IVideoPlay;
import com.polar.browser.i.IWbLoadUrlStatusObserver;
import com.polar.browser.i.IWebViewClientDelegate;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.loginassistant.LoginAssistantManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.manager.ServiceManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.service.IAdBlockService;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.ButtomTipBar;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.bean.events.SyncDatabaseEvent;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.util.GooglePlayUtil;
import com.polar.browser.video.IPlay;
import com.polar.browser.video.VideoItem;
import com.polar.browser.video.VideoManager;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用来处理webview的各种回调，得到各种事件
 *
 * @author dpk
 */
@SuppressLint("DefaultLocale")
public class WebViewClientImpl implements IWebViewClientDelegate {

    private static final String TAG = "WebViewClientImpl";

    private static final int FINISH_PROGRESS_TIME = 30;
    private static final int FINISH_PROGRESS = 85;
    private static final String HAS_VIDEO_JS = "hasvideo.js";
    private static final String HOOK_VIDEO_JS = "hookvideo.js";
    private static final String GET_VIDEO_URL_JS = "getvideourl.js";
    private static final String LOGIN_JS = "login.js";
    private static final String[] COMMON_SCHEMES = {"http://", "https://",
            "rtsp://"};
    private static final String[] BLACK_VIDEO_LIST = {"m.cctv.com",
            "map.baidu.com", "m.iqiyi.com"};
    private static String sCssJs = "(function(){" +
            "function insertStyleCss(cssText){" +
            "var head = document.getElementsByTagName('head')[0];" +
            "var cssNode = document.createElement('style');" +
            "cssNode.type = 'text/css';" +
            "cssNode.appendChild(document.createTextNode(cssText));" +
            "head.appendChild(cssNode);" +
            "}" +
            "insertStyleCss('SHADCSS')" +
            "})();";
    private static String sHasVideoJs;
    private static String sHookVideoJs;
    private static String sGetVideoUrlJs;
    private static String sLoginJs;
    private static String sHasofferJs;
    private Activity mActivity;
    private ServiceManager mService;
//    private HistoryManager mHistoryManager;
    private IScrollChanged mScrollChanged;
    private IProgressCallback mProgressCallback;
    private String mUrl; // 用于记住mainurl，作为广告拦截的url
    private WebView mLastWebView;
    private boolean isErrorPageShow = false;

    private String mLastVideoUrl;

    private String mCurrentEvent;

    private long mLastDestroyVideoPlayerTime;

    private List<IUrlChangedObserver> mObserverList = new ArrayList<IUrlChangedObserver>();

    private List<IWbLoadUrlStatusObserver> mLoadUrlStatusObserverList = new ArrayList<IWbLoadUrlStatusObserver>();

    private int mTabId;

    private String mCurrentTextEncoding;
    private String mDefaultTextEncoding;

    /**
     * 进度条是否加载到85%，人为将进度条赋值100%
     */
    private boolean hintProgress;
    /**
     * 进度条是否加载到30%，注入比价插件JS
     */
    private boolean mIsInsertHasofferJs;
    private AtomicInteger mPageBlockedCount = new AtomicInteger(0);

    public void setEnnableBlockTips(boolean ennableBlockTips) {
        isEnnableBlockTips = ennableBlockTips;
    }
    private boolean isEnnableBlockTips =true;

    private IVideoPlay mVideoPlayObserver = new IVideoPlay() {

        @Override
        public void onNotifyPlayEnd() {
            onPlayEnd();
        }

        @Override
        public void onNotifyVideoPlayerDestroy() {
            mLastDestroyVideoPlayerTime = System.currentTimeMillis();
            // 优酷的界面，自己会有播放按钮，刷新一下，否则播放按钮会停留在界面上
            // if (mUrl != null && mUrl.contains("youku.com")
            // && mLastWebView != null
            // && TabViewManager.getInstance().isWebViewInvalid(mLastWebView)) {
            //
            // mLastWebView.reload();
            // }
        }
    };
    private ArrayList<String> mUrls = new ArrayList<>();;


    public WebViewClientImpl(Activity activity, ServiceManager service, IScrollChanged scrollChanged,
                             IProgressCallback callback) {
        mActivity = activity;
        mService = service;
        mScrollChanged = scrollChanged;
        mProgressCallback = callback;
        VideoManager.getInstance().registerPlayDelegate(mVideoPlayObserver);
    }

    /**
     * 因为广告拦截最开始不一定初始化，webviewclientImpl最初得到的manager为空 需要一个外部方法来设定manager
     *
     * @param manager
     */
    @Override
    public void setServiceManager(ServiceManager manager) {
        mService = manager;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView,
                                                      String url) {
        WebResourceResponse wr = null;
        if (ConfigManager.getInstance().isAdBlock() && mService != null) {
            IAdBlockService adBlockService = mService.getAdBlockService();
            String mainUrl = mUrl;
            if (mainUrl == null) {
                mainUrl = url;
            }
            try {
                if (adBlockService != null

                        && adBlockService.shouldBlockUrlSync(mainUrl, url)) {
                    SimpleLog.d(TAG, "isBlocked!");
                    SimpleLog.d(TAG, "shouldInterceptRequest() " + "mainUrl:"
                            + mainUrl + " url:" + url);
                    if(isEnnableBlockTips)
                        mPageBlockedCount.incrementAndGet();

                    SimpleLog.d(TAG,"blockCount="+mPageBlockedCount.get());
                    byte resourceType = AdFilter.getResourceType(url);
                    InputStream data = null;
                    String mime = new String();
                    String encoding = new String();
                    switch (resourceType) {
                        case AdFilter.RES_TYPE_JS:
                            data = new ByteArrayInputStream(AdFilter.JsData.getBytes());
                            mime = "application/javascript";
                            encoding = "utf-8";
                            break;
                        case AdFilter.RES_TYPE_CSS:
                            mime = "text/css";
                            encoding = "utf-8";
                            break;
                        case AdFilter.RES_TYPE_PIC:
                            data = new ByteArrayInputStream(AdFilter.PicData);
                            mime = "image/gif";
                            break;
                        case AdFilter.RES_TYPE_HTML:
                            data = new ByteArrayInputStream(AdFilter.HtmData.getBytes());
                            mime = "text/html";
                            encoding = "utf-8";
                            break;
                        case AdFilter.RES_TYPE_XML:
                            data = new ByteArrayInputStream(AdFilter.XmlData.getBytes());
                            mime = "text/xml";
                            encoding = "utf-8";
                            break;
                    }

                    if (data == null)
                        data = new ByteArrayInputStream((new String()).getBytes());
                    return new WebResourceResponse(mime, encoding, data);
                }
            } catch (RemoteException e) {
                SimpleLog.e(e);
            } catch (Exception e) {
                SimpleLog.e(e);
            }
        }
        return wr;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (AppEnv.DEBUG) {
            SimpleLog.d(TAG, "shouldOverrideUrlLoading:" + url);
        }
        if (isCommonScheme(url)) {
            if (GooglePlayUtil.isGooglePlayUrl(url)) {
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
            mScrollChanged.onScrollShow();
            return false;
        } else { // TODO: 对各种特殊协议的处理

            if (url.startsWith("magicvideo://")) {        // 是视频
                SimpleLog.e("video", url);
                handleVideoPlayEvent(view, url);
            } else if (url.startsWith("tel")) {
                try {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return true;
                } catch (Exception e) {
                }
            } else if (url.startsWith("whatsapp://")) {
                try {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } catch (Exception e) {
                }
            } else if (url.startsWith("market://")) {
                //针对Go Downloader 市场链接处理（若检测本地已安装直接跳转下载器）
                try {
                    Uri uri = Uri.parse(url);
                    String id = uri.getQueryParameter("id");
                    if (!TextUtils.isEmpty(id) && TextUtils.equals("com.go.downloader", id)) {
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
//					String a[] = url.split("/?");
//					String appUrl = a[1];
//					if (!TextUtils.isEmpty(appUrl)) {
//						view.loadUrl(GooglePlayUtil.GOOGLE_PLAY_APP_DETAILS_URL_PREFIX + appUrl);
////						Uri uri = Uri.parse(GooglePlayUtil.GOOGLE_PLAY_APP_DETAILS_URL_PREFIX + appUrl);
////						view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
//					}
                }
            }
            return true;
        }
    }

    /**
     * 注入js脚本
     *
     * @param webview
     * @param pluginScript
     */
    private void injectPluginScriptInner(final WebView webview, final String pluginScript) {
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

    // 对于播放视频的事件进行处理
    private void handleVideoPlayEvent(WebView webview, String url) {
        long now = System.currentTimeMillis();
        // 点击前进后退的2s内不让自动播放
        if (now - TabViewManager.getInstance().getLastClickNavigateTime() < 2000) {
            return;
        }
        // 如果刚刚关闭了播放器，在1秒内又收到播放视频的信号，且不是用户自主行为，不要播放
        if (now - mLastDestroyVideoPlayerTime < 1000 && !url.contains("click")) {
            return;
        }
        // 如果视频没在播，收到的也不是click，不要自动播放了
        if (!VideoManager.getInstance().isVideoPlayerRunning()
                && (!url.contains("click") && !url.contains("touch"))) {
            if (mUrl != null) {
                if (!mUrl.contains("youku.com")) {
                    return;
                }
            } else {
                return;
            }
        }
        // 对于乐视tv出现的前进后退重复播放问题做一下处理
        if (mUrl != null && url.contains("newPlay")) {
            String host = UrlUtils.getHost(mUrl);
            if (host != null && host.contains("letv")) {
                SimpleLog.i(TAG, "mainUrl:" + mUrl);
                return;
            }
        }
        mCurrentEvent = url;
        if (mCurrentEvent.contains("ended")
                && !VideoManager.getInstance().isVideoPlayerRunning()) {
            return;
        }
        int findIndex = url.indexOf("index");
        String index = "";
        if (findIndex != -1) {
            int sharp = url.indexOf("#");
            if (sharp > findIndex) {
                index = url.substring(findIndex, sharp);
                index = index.substring("index=".length());
                SimpleLog.i(TAG, "index:" + index);
                injectPluginScriptInner(webview, CommonData.EXEC_JAVASCRIPT + "window.magicPlayingIndex=" + index);
            }
        }
        if (TextUtils.isEmpty(index)) {
            injectPluginScriptInner(webview, CommonData.EXEC_JAVASCRIPT + "window.magicPlayingIndex=0");
        }
        initGetVideoUrlJs();
        injectPluginScriptInner(webview, CommonData.EXEC_JAVASCRIPT + sGetVideoUrlJs);
        SimpleLog.i(TAG, url);
        return;
    }

    private void initHasVideoJs() {
        if (TextUtils.isEmpty(sHasVideoJs)) {
            byte[] js = FileUtils.readFileFromAssets(JuziApp.getAppContext(),
                    HAS_VIDEO_JS);
            sHasVideoJs = new String(js);
        }
    }

    private void initHookVideoJs() {
        if (TextUtils.isEmpty(sHookVideoJs)) {
            byte[] js = FileUtils.readFileFromAssets(JuziApp.getAppContext(),
                    HOOK_VIDEO_JS);
            sHookVideoJs = new String(js);
        }
    }

    private void initGetVideoUrlJs() {
        if (TextUtils.isEmpty(sGetVideoUrlJs)) {
            byte[] js = FileUtils.readFileFromAssets(JuziApp.getAppContext(),
                    GET_VIDEO_URL_JS);
            sGetVideoUrlJs = new String(js);
        }
    }

    private void initLoginJs() {
        if (TextUtils.isEmpty(sLoginJs)) {
            byte[] js = FileUtils.readFileFromAssets(JuziApp.getAppContext(),
                    LOGIN_JS);
            sLoginJs = new String(js);
        }
    }

    private void initHasofferJs(WebView webView, String hasofferPlugPath) {
        if (TextUtils.isEmpty(sHasofferJs)) {
            try {
//				String dataPath = VCStoragerManager.getInstance().getDataPath() + HASOFFER_JS;
                File file = new File(hasofferPlugPath);
                byte[] jss = FileUtils.readFile(file);
                sHasofferJs = new String(jss);

                String androidId = SystemUtils.getAndroidId(webView.getContext());
                String imeiId = SystemUtils.getImeiId(webView.getContext());
                String brand = "VCBrowser";
                String model = SystemUtils.getModel();
                String osVersion = SystemUtils.getOSVersion();
                String serial = SystemUtils.getDeviceSerial();
                String deviceMac = SystemUtils.getDeviceMac(webView.getContext());

                if (TextUtils.isEmpty(sHasofferJs)) {
                    return;
                }
                sHasofferJs = sHasofferJs.replace("##deviceId##", androidId);
                sHasofferJs = sHasofferJs.replace("##imeiId##", imeiId);
                sHasofferJs = sHasofferJs.replace("##brand##", brand);
                sHasofferJs = sHasofferJs.replace("##deviceName##", model);
                sHasofferJs = sHasofferJs.replace("##osVersion##", osVersion);
                sHasofferJs = sHasofferJs.replace("##serial##", serial);
                sHasofferJs = sHasofferJs.replace("##mac##", deviceMac);

            } catch (Exception e) {

            }
        }
    }


    private boolean isCommonScheme(String url) {
        boolean isCommonScheme = false;
        if (url != null) {
            for (int i = 0; i < COMMON_SCHEMES.length; ++i) {
                String urlLowerCase = url.toLowerCase();
                if (urlLowerCase.startsWith(COMMON_SCHEMES[i])) {
                    isCommonScheme = true;
                    break;
                }
            }
        }
        return isCommonScheme;
    }

    private void injectCssAndLocalStorageScriptIfNeeded(WebView view, String url) {
        if (ConfigManager.getInstance().isAdBlock() && mService != null) {
            IAdBlockService adBlockService = mService.getAdBlockService();
            if (adBlockService != null) {
                try {
                    String cssScript = adBlockService.getCssAndLocalStorageScript(UrlUtils.getHost(url));
                    if (!TextUtils.isEmpty(cssScript)) {
                        AdFilter.injectScriptIntoWebview(view, cssScript);
                    }
                } catch (Throwable e) {
                }
            }
        }
    }
    private String lastUrl="";
    private String title;
    @Override
    public void onPageFinished(final WebView view, final String url, int tabId,
                               final int src) {

        if (TabViewManager.getInstance().getCurrentTabView() == null)
            return;
        // TODO url统计
        mUrls.add(url);
        if (mUrls.size() >= 10) {
            FileUtils.saveToJsonFile(FileUtils.getFileWithName(Constants.URLS_FILE_NAME), Constants.URL_UPLOAD_KEY, mUrls);
            mUrls.clear();
        }
        // 2014-12-22 dpk: 如果tabId相同，要保证resume了，否则可能会出现白屏现象（如系统调用了onPause）
        if (TabViewManager.getInstance().getCurrentTabId() == tabId) {
            view.onResume();
        }
        // 新增css广告过滤
        injectCssAndLocalStorageScriptIfNeeded(view, view.getUrl());
        TabViewManager.getInstance().enableNightMode(
                ConfigManager.getInstance().isEnableNightMode());
        if (isNeedInsertJs(url)) {
            insertJs(view);
            delayInsertJs(300);
        }
        JavaScriptManager.injectOnPageFinished(view, url);
        initInsertHasofferJs(view, url);
        if (ConfigManager.getInstance().isEnableSaveAccount()) {
            insertLoginJs(view);
        }
        title = view.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = url;
        } else if (url.equals(mUrl)) {
            notifyUrlChanged(title, tabId, false, url);//什么情况,title url 混用?
//			notifyUrlChanged(url, title, tabId, isOnlyUpdateToolbar);
        }
        SimpleLog.i(TAG, "onPageFinished! title:" + title+"--blockCount="+mPageBlockedCount.get());
        ConfigManager.getInstance().saveAdBlockedCount(mPageBlockedCount.get(),isNotSamePage(url,lastUrl)  &&isEnnableBlockTips);
        // 如果是人为添加的首页，不记录
        if (!url.startsWith(TabViewManager.HOME_URL) && !ConfigManager.getInstance().isPrivacyMode()) {
            SimpleLog.i(TAG, "onPageFinished! addHistory:" + "title:" + title + "url:" + url);

            ThreadManager.getIOHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        HistoryRecord historyRecord = new HistoryRecord();
                        historyRecord.setCount(0);
                        historyRecord.setSource(src);
                        historyRecord.setHistoryTitle(title);
                        historyRecord.setHistoryAddr(url);
                        historyRecord.setTs(new Date());
                        HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(mActivity)).insertOrUpdate(historyRecord);
                        EventBus.getDefault().post(new SyncDatabaseEvent(SyncDatabaseEvent.TYPE_HISTORY_RECORD));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            //保存历史记录表
//            mHistoryManager.addHistory(url, title, src);
            //保存常访问记录表
//            mHistoryManager.addOftenHistorys(url, title);
        }
        mProgressCallback.onProgressFinished(tabId, TabViewManager
                .getInstance().isCurrentHome());
//		notifyUrlChanged(url, tabId, isOnlyUpdateToolbar,url);
        notifyLoadingStatusChanged(CommonData.WBLOADURL_STATUS_PAGEFINSH, url);
//		ConfigManager.getInstance().notifyHideSearchInPage();
        lastUrl = url;
        isEnnableBlockTips = true;
    }

    private boolean isNotSamePage(String url, String lastUrl) {

        if(TextUtils.isEmpty(lastUrl)||TextUtils.isEmpty(url))
            return true;
        try {
            URL mUrl = new URL(url);
            URL mLastUrl = new URL(lastUrl);
            return !mUrl.getHost().equals(mLastUrl.getHost());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return true;


    }


    private void initInsertHasofferJs(final WebView webView, final String url) {
        if (ConfigManager.getInstance().getHasofferEnabled()
                && ConfigManager.getInstance().getServerHasofferEnabled()) {
            String hasofferPlugPath = VCStoragerManager.getInstance().getHasOfferJsPath()
                    + File.separator + VCStoragerManager.PLUG_DATA_HASOFFER;
//            String md5 = SecurityUtil.getFileMD5(hasofferPlugPath);
//            String hasofferPlugMd5 = ConfigManager.getInstance().getHasofferPlugMd5();
//            if (TextUtils.isEmpty(md5) || !TextUtils.equals(md5, hasofferPlugMd5)) {
//                ConfigManager.getInstance().setHasofferPlugVersion("1");
//                return;
//            }
            String hasofferPlugSupport = ConfigManager.getInstance().getHasofferPlugSupport();
            if (TextUtils.isEmpty(hasofferPlugSupport)) {
                return;
            }

            String[] HASOFFER_LIST = hasofferPlugSupport.split("@");
            if (HASOFFER_LIST.length > 0) {
                for (int i = 0; i < HASOFFER_LIST.length; i++) {

                    if (url.contains(HASOFFER_LIST[i])) {
                        insertHasofferJs(webView, hasofferPlugPath);
                    }
                }
            }
        }
    }


    private void insertLoginJs(final WebView webview) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (webview != null
                        && TabViewManager.getInstance().isWebViewInvalid(
                        webview)) {
                    SimpleLog.i(TAG, "insertLoginJs");
                    initLoginJs();
                    injectPluginScriptInner(webview, CommonData.EXEC_JAVASCRIPT + sLoginJs);
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private void insertJs(final WebView webview) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (webview != null
                        && TabViewManager.getInstance().isWebViewInvalid(
                        webview)) {
                    SimpleLog.i(TAG, "insertJs");
                    injectPluginScriptInner(webview, CommonData.EXEC_JAVASCRIPT + sHasVideoJs);
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private void insertHasofferJs(final WebView webView, final String hasofferPlugPath) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (webView != null
                        && TabViewManager.getInstance().isWebViewInvalid(
                        webView)) {
                    SimpleLog.i(TAG, "insertHasofferJs");
                    initHasofferJs(webView, hasofferPlugPath);
                    injectPluginScriptInner(webView, CommonData.EXEC_JAVASCRIPT + sHasofferJs);
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    /**
     * 吊起截屏涂鸦功能
     * @param bm
     * @param url
     */
    @JavascriptInterface
    public void editImg(Bitmap bm, String url) {
        if (bm == null) {}

    }

    /**
     * 通知native查询url对应的用户名和密码
     * 如果存在,调用js填写表单
     *
     * @param url
     */
    @JavascriptInterface
    public void fillForm(final String url) {
        final String host = UrlUtils.getHost(url) == null ? url : UrlUtils.getHost(url);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (mLastWebView != null
                        && TabViewManager.getInstance().isWebViewInvalid(
                        mLastWebView)) {
                    LoginAssistantManager loginAssistantManager = LoginAssistantManager.getInstance();
                    loginAssistantManager.init(mActivity.getApplicationContext());
                    String fillJs = loginAssistantManager.getFillFormJs(host);
                    if (fillJs != null) {
                        if (!fillJs.isEmpty()) {
                            injectPluginScriptInner(mLastWebView, CommonData.EXEC_JAVASCRIPT
                                    + fillJs);
                        }
                    }
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    /**
     * 提示用户是否保存用户名和密码
     *
     * @param url
     * @param userName
     * @param passWord
     */
    @JavascriptInterface
    public void saveUserNamePassWord(final String url, final String userName, final String passWord) {
        /**
         * 参数非空检查
         */
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {
            return;
        }
        final LoginAssistantManager loginAssistantManager = LoginAssistantManager.getInstance();
        loginAssistantManager.init(mActivity.getApplicationContext());
        final String host = UrlUtils.getHost(url) == null ? url : UrlUtils.getHost(url);
        final LoginAccountInfo loginAccountInfo = loginAssistantManager.getUserNamePasswordByUrl(host);
        if (ConfigManager.getInstance().isPrivacyMode()) {
            return;
        }
        if (!ConfigManager.getInstance().isEnableSaveAccount()) {
            return;
        }
        if (loginAccountInfo != null && userName.equals(loginAccountInfo.getUsername()) && passWord.equals(loginAccountInfo.getPassword())) {
            return;
        }
        ThreadManager.postTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                final RelativeLayout tipLayout = (RelativeLayout) mActivity.findViewById(R.id.rl_bottom_tip);
                String format = String.format(mActivity.getString(R.string.setting_remember_username_and_password_tip), host);
                String okTip = mActivity.getResources().getString(R.string.setting_editlogo_type_ok);
                ButtomTipBar.showButtomTipBar(mActivity, tipLayout, new ButtomTipBar.onTipBtnListener() {
                    @Override
                    public void onClickSetting(View v) {
                        if (loginAccountInfo == null) {
                            loginAssistantManager.saveUsernamePassword(host, userName, passWord);
                        } else {
                            loginAssistantManager.updateUsernamePassword(host, userName, passWord);
                        }
                    }

                    @Override
                    public void onClickClose(View v) {

                    }
                }, format, okTip);
            }
        });
    }


    @JavascriptInterface
    public void isHasVideo(String isHasVideo) {
        SimpleLog.i(TAG, "isHasVideo:" + isHasVideo);
        if (isHasVideo != null && isHasVideo.equals("YES")) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    initHookVideoJs();
                    if (mLastWebView != null
                            && TabViewManager.getInstance().isWebViewInvalid(
                            mLastWebView)) {
                        injectPluginScriptInner(mLastWebView, CommonData.EXEC_JAVASCRIPT
                                + sHookVideoJs);
                    }
                }
            };
            ThreadManager.postTaskToUIHandler(r);
        } else {
        }
    }

    @JavascriptInterface
    public void jsOutput(String output) {
        SimpleLog.i(TAG, "js output:" + output);
    }

    @JavascriptInterface
    public void getVideoUrl(final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        SimpleLog.i(TAG, "video url:" + url);
        if (VideoManager.getInstance().sameWithCurrentUrl(url)) {
            return;
        }
        if (url.equals(mLastVideoUrl)
                && VideoManager.getInstance().isVideoPlayerRunning()) {
            return;
        }
        // 为了防止退出视频后，连续再进入播放器重新播放，在这里做一下判断
        if ((mCurrentEvent.contains("newPlay") || mCurrentEvent
                .contains("loadeddata")) && (url.equals(mLastVideoUrl))) {
            return;
        }
        // 判断视频弹框
        if (mLastWebView != null) {
            if (VideoManager.getInstance().checkShowDialog(
                    mLastWebView.getContext())) {
                VideoManager.getInstance().showNetWorkChangedDialog(
                        mLastWebView.getContext(), new IPlay() {
                            @Override
                            public void play() {
                                playVideo(url);
                            }
                        });
                return;
            }
        }
        playVideo(url);
    }

    private void playVideo(final String url) {
        mLastVideoUrl = url;
        Runnable r = new Runnable() {

            @Override
            public void run() {
                VideoItem videoItem = new VideoItem();
                videoItem.url = url;
                if (mLastWebView != null
                        && TabViewManager.getInstance().isWebViewInvalid(
                        mLastWebView)) {
                    videoItem.title = mLastWebView.getTitle();
                    VideoManager.getInstance().playVideo(videoItem,
                            mLastWebView.getContext());
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private boolean isNeedInsertJs(String url) {
        if (url != null) {
            String host = UrlUtils.getHost(url);
            if (host != null) {
                for (int i = 0; i < BLACK_VIDEO_LIST.length; ++i) {
                    if (host.contains(BLACK_VIDEO_LIST[i])) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, int tabId) {
        TabViewManager.getInstance().getCurrentTabView().getContentView().setErrorPageVisibility(View.GONE);
        mUrl = url;
        if (isErrorPageShow) {
//            addWebview(view);
        }
        if ((view.getVisibility() == View.INVISIBLE) || (view.getVisibility() == View.GONE)) {
            view.setVisibility(View.VISIBLE);
        }
        mPageBlockedCount.set(0);
        mIsInsertHasofferJs = true;
        hintProgress = true;
        mTabId = tabId;
        SimpleLog.d(TAG, "PageStarted! url:" + url);
        boolean isOnlyUpdateToolbar = true;
        String host = UrlUtils.getHost(url);
        if (url.equals(TabViewManager.HOME_URL)) {
            TabViewManager.getInstance().hideHomeUrl();
        } else {
            Statistics.sendOnceStatistics(GoogleConfigDefine.LOAD_URL,
                    GoogleConfigDefine.LOAD_URL_PAGESTART);
            SimpleLog.e(TAG, "load url:" + url);
            String key = SearchUtils.getSearchKey(url);
            ConfigManager.getInstance().notifySearchKeyChanged(key);

            setDefaultTextEncodingName(view, host);
            initHasVideoJs();
            JavaScriptManager.injectOnPageStarted(view, url);
            long now = System.currentTimeMillis();
            long interval = now
                    - TabViewManager.getInstance().getLastClickNavigateTime();
            SimpleLog.d(TAG,
                    "back forward interval:" + String.valueOf(interval));
            mLastWebView = view;
            // 认为是前进后退，进度条直接走完
            if (interval < 500) {
                mProgressCallback.onProgressFinished(tabId, TabViewManager
                        .getInstance().isCurrentHome());
                isOnlyUpdateToolbar = false;
            } else {
                mProgressCallback.onProgressStart(tabId, TabViewManager
                        .getInstance().isCurrentHome());
                handleIfCanDownload(url);
            }
        }
        notifyUrlChanged(url, tabId, isOnlyUpdateToolbar, url);
        notifyLoadingStatusChanged(CommonData.WBLOADURL_STATUS_PAGESTART, url);
        // view.getSettings().setBlockNetworkImage(true);
//		ConfigManager.getInstance().notifyHideSearchInPage();
        if (mOnNeedHideCallBack != null) mOnNeedHideCallBack.onNeedHide();


    }

    /**
     * duanwenqiang 20160818
     * 处理有些网页可能是视频,音乐,网页直接播放了,我们弹出框,提示用户可以下载
     *
     * @param url
     */
    private void handleIfCanDownload(String url) {
        if (checkUrlCanDownload(url)) {
            DownloadHelper.download(url, url, null, null, null, null, 0);
        }
    }

    private boolean checkUrlCanDownload(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String lowUrl = url.toLowerCase();
        if (lowUrl.endsWith(".mp4") || lowUrl.endsWith(".mp3")) {
            return true;
        }
        return false;
    }

    /**
     * 设置页面默认编码
     *
     * @param webView
     */
    private void setDefaultTextEncodingName(WebView webView, String host) {
        if (webView == null || TextUtils.isEmpty(host)) {
            return;
        }
        if (TextUtils.isEmpty(mDefaultTextEncoding)) {
            mDefaultTextEncoding = webView.getSettings()
                    .getDefaultTextEncodingName();
        }
        if (TextUtils.equals(host, "www.hnflcp.com")) {
            if (!TextUtils.equals(mCurrentTextEncoding, "GBK")) {
                mCurrentTextEncoding = "GBK";
                webView.getSettings().setDefaultTextEncodingName(
                        mCurrentTextEncoding);
            }
        } else {
            if (TextUtils.equals(mCurrentTextEncoding, "GBK")) {
                webView.getSettings().setDefaultTextEncodingName(
                        mDefaultTextEncoding);
                mCurrentTextEncoding = "other";
            }
        }
    }

    @Override
    public void registUrlChangedObserver(IUrlChangedObserver observer) {
        mObserverList.add(observer);
    }

    @Override
    public void unregistUrlChangedObserver(IUrlChangedObserver observer) {
        mObserverList.remove(observer);
    }

    private void notifyUrlChanged(String url, int tabId,
                                  boolean isOnlyUpdateToolbar, String rawUrl) {
        for (IUrlChangedObserver observer : mObserverList) {
            observer.notifyUrlChanged(url, tabId, isOnlyUpdateToolbar, rawUrl);
        }
    }

    private void delayInsertJs(int delayedTime) {
        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {

            @Override
            public void run() {
                insertJs(mLastWebView);
            }
        }, delayedTime);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url,
                                       boolean isReload, final int src, int tabId) {
        SimpleLog.i(TAG, "doUpdateVisitedHistory! url:" + url);
        mUrl = url;
        JavaScriptManager.injectOnUpdateVisitedHistory(view, url);
        TabViewManager.getInstance().enableNightMode(
                ConfigManager.getInstance().isEnableNightMode());
        if (isNeedInsertJs(url)) {
            // 一共就注册3次js，提高页面效率
            insertJs(mLastWebView);
            delayInsertJs(300);
            delayInsertJs(600);
        }

        //地址更新时，注入一次，解决reddit跳转到登录页不调用onPageFinish，无法的问题
        if (ConfigManager.getInstance().isEnableSaveAccount()) {
            insertLoginJs(view);
        }
        // 新增css广告过滤
        injectCssAndLocalStorageScriptIfNeeded(view, url);
        String title = view.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = url;
        } else {
            notifyUrlChanged(title, tabId, true, url);//什么情况,title url 混用?
        }
        // SimpleLog.i(TAG, "doUpdateVisitedHistory! title:" + title);
        // mHistoryManager.addHistory(url, title, src);
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view,
                                          final HttpAuthHandler handler, String host, String realm) {
        final CommonDialog dialog = new CommonDialog(view.getContext());
        String title = view.getContext().getString(R.string.auth_request_login);
        dialog.setTitle(title + host);
        dialog.setCenterView(R.layout.dialog_http_auth_request);
        dialog.setBtnOkListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                EditText username = (EditText) dialog
                        .findViewById(R.id.username);
                EditText password = (EditText) dialog
                        .findViewById(R.id.password);
                handler.proceed(username.getText().toString(), password
                        .getText().toString());
            }
        });
        dialog.setBtnCancelListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        view.setVisibility(View.INVISIBLE);
        String data="";
        view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
        showErrorPage(view);//显示错误页面
        TabViewManager.getInstance().getCurrentTabView().getContentView().setErrorMsg(String.valueOf(errorCode), description);
        // view.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        // view.clearHistory();
        // view.stopLoading();
        // view.clearView();
        // view.goBack();
        // AssetManager am = view.getContext().getAssets();
        // DataInputStream dis = null;
        // try {
        // dis = new DataInputStream(am.open("404.html"));
        // dis.toString();
        // } catch (Exception e) {
        //
        // }
        // String data =
        // "<img id=\"404p\" src=\"file:///android_asset/404p.png\" width=\"200\" style=\"margin-top:20px;\" />";
        // SimpleLog.d(TAG, data);
        // String js = String.format("javascript:window.location.href=\"%s\"",
        // ERROR_URL);
        // SimpleLog.d(TAG, js);
        // view.loadUrl(js);
        // String js2 =
        // String.format("javascript:window.location.replace(\"%s\")",
        // ERROR_URL);
        // SimpleLog.d(TAG, js2);
        // view.loadUrl(js2);
    }

    protected void addWebview(WebView view) {
        isErrorPageShow = false;
        RelativeLayout webParentView = (RelativeLayout)view.getParent();

        if (webParentView != null) {
            while (webParentView.getChildCount() > 1) {
                webParentView.addView(view, 0);
            }
        }
    }

    private void showErrorPage(WebView view) {
        isErrorPageShow = true;
//        RelativeLayout webParentView = (RelativeLayout)view.getParent();
//
//        if (webParentView != null) {
//            while (webParentView.getChildCount() > 1) {
//                webParentView.removeViewAt(0);
//            }
//        }
        TabViewManager.getInstance().getCurrentTabView().getContentView().setErrorPageVisibility(View.VISIBLE);
    }

    private void onPlayEnd() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                if (mLastWebView != null
                        && TabViewManager.getInstance().isWebViewInvalid(
                        mLastWebView)) {
                    SimpleLog.d(TAG, "onPlayEnd");
                    injectPluginScriptInner(mLastWebView, CommonData.EXEC_JAVASCRIPT
                            + "MolemonplayVideoEnded()");
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    @Override
    public void setprogress(WebView webView, int progress) {
        // SimpleLog.d("WebViewClientImplProgress", "Progress="+progress);
        if (progress > FINISH_PROGRESS_TIME && mIsInsertHasofferJs) {
            initInsertHasofferJs(webView, mUrl);
            mIsInsertHasofferJs = false;
        }
        if (progress > FINISH_PROGRESS && hintProgress) {
            notifyLoadingStatusChanged(CommonData.WBLOADURL_STATUS_PROGRESS, null);

            mProgressCallback.onProgressFinished(mTabId, TabViewManager
                    .getInstance().isCurrentHome());
            hintProgress = false;
        }
    }

    @Override
    public void switchCurrentWebView(View view) {
        if (view != null && view instanceof WebView) {
            mLastWebView = (WebView) view;
            SimpleLog.d(TAG, "switchCurrentWebView");
        }
    }

    @Override
    public void registWbLoadUrlStatusObserver(IWbLoadUrlStatusObserver observer) {
        mLoadUrlStatusObserverList.add(observer);
    }

    @Override
    public void unregistWbLoadUrlStatusObserver(IWbLoadUrlStatusObserver observer) {
        mLoadUrlStatusObserverList.remove(observer);
    }

    @Override
    public void unregisterWbVideoPlayObserver() {
        VideoManager.getInstance().unRegisterPlayDelegate();
    }

    @Override
    public void findAllAsync(String text, WebView.FindListener listener) {
        if (mLastWebView == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            mLastWebView.findAllAsync(text);
            mLastWebView.setFindListener(listener);


        } else {
            mLastWebView.findAll(text);
            try {
                Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                m.invoke(mLastWebView, true);
            } catch (Throwable ignored) {
            }
        }

    }

    @Override
    public void clearFind() {
        if (mLastWebView != null) {
            mLastWebView.clearMatches();
        }
    }

    @Override
    public void findNext(boolean isNext) {
        if (mLastWebView != null) {
            mLastWebView.findNext(isNext);
        }
    }

    @Override
    public WebBackForwardList copyBackForwardList() {
        if (mLastWebView != null) {
            return mLastWebView.copyBackForwardList();
        }
        return null;
    }

    private void notifyLoadingStatusChanged(int loadUrlStatu, String url) {
        for (IWbLoadUrlStatusObserver observer : mLoadUrlStatusObserverList) {
            observer.notifyLoadingStatusChanged(loadUrlStatu, url);
        }
    }

    private OnNeedHideCallBack mOnNeedHideCallBack;

    public void setmOnNeedHideCallBack(OnNeedHideCallBack mOnNeedHideCallBack) {
        this.mOnNeedHideCallBack = mOnNeedHideCallBack;
    }

    public interface OnNeedHideCallBack {
        public void onNeedHide();
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        injectCssAndLocalStorageScriptIfNeeded(view, view.getUrl());
    }

    @Override
    public void setMainUrl(String url) {
        mUrl = url;
    }

    @Override
    public void saveUrl() {
        if (mUrls.size() > 0) {
            FileUtils.saveToJsonFile(FileUtils.getFileWithName(Constants.URLS_FILE_NAME), Constants.URL_UPLOAD_KEY, mUrls);
            mUrls.clear();
        }
    }
}
