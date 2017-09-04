package com.polar.browser.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.WebHistoryItem;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.adblock.AdFilter;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.bookmark.IBookmarkObserver;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.Blur;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.cropedit.CropEditActivity;
import com.polar.browser.cropedit.CropStorageUtil;
import com.polar.browser.download.savedpage.SavedPageUtil;
import com.polar.browser.env.AppEnv;
import com.polar.browser.homepage.customlogo.EditLogoView;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.i.IDownloadDelegate;
import com.polar.browser.i.IOpenToolBarTopMore;
import com.polar.browser.i.IOpenUrlDelegate;
import com.polar.browser.i.IProgressStart;
import com.polar.browser.i.IScrollChanged;
import com.polar.browser.i.IShareClick;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.i.ITabChangedObserver;
import com.polar.browser.i.IToolbarBottom;
import com.polar.browser.i.IToolbarMenuDelegate;
import com.polar.browser.i.IUpdateFileCallback;
import com.polar.browser.i.IUrlChangedObserver;
import com.polar.browser.i.IWbLoadUrlStatusObserver;
import com.polar.browser.i.IWebChromeClientDelegate;
import com.polar.browser.i.IWebViewClientDelegate;
import com.polar.browser.imagebrowse.ImageBrowseView;
import com.polar.browser.impl.AddFavImpl;
import com.polar.browser.impl.DownloadDelegateImpl;
import com.polar.browser.impl.ExitBrowserImpl;
import com.polar.browser.impl.ShareDelegateImpl;
import com.polar.browser.impl.SlideDelegateImpl;
import com.polar.browser.impl.TabViewOnLongClickListener;
import com.polar.browser.impl.ToolbarMenuImpl;
import com.polar.browser.impl.WebChromeClientImpl;
import com.polar.browser.impl.WebViewClientImpl;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.loginassistant.login.LoginSuccessDialog;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.JSInterfaceManager;
import com.polar.browser.manager.JavaScriptManager;
import com.polar.browser.manager.ServiceManager;
import com.polar.browser.manager.TabViewCallbackManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.push.SystemNewsListActivity;
import com.polar.browser.push.fbnotify.FbNotifyManager;
import com.polar.browser.service.IAdBlockService;
import com.polar.browser.setting.AdBlockSettingActivity;
import com.polar.browser.setting.ClearDataActivity;
import com.polar.browser.setting.FontSizeSettingDialog;
import com.polar.browser.setting.SettingActivity;
import com.polar.browser.shortcut.ParseConfig;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.SettingSyncManager;
import com.polar.browser.tabview.ContentFrame;
import com.polar.browser.tabview.ContentView.IReceivedTitleCallback;
import com.polar.browser.tabview.HomeFrame;
import com.polar.browser.update.DownloadApkService;
import com.polar.browser.update.UpdateDataTask;
import com.polar.browser.upload.UploadHandler;
import com.polar.browser.utils.ButtomTipBar;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CookieUtil;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.PermissionsHelper;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.utils.ShortCutUtil;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.utils.ViewUtils;
import com.polar.browser.vclibrary.bean.db.News;
import com.polar.browser.vclibrary.bean.db.SystemNews;
import com.polar.browser.vclibrary.bean.events.BottomMenuNotifyBrowserEvent;
import com.polar.browser.vclibrary.bean.events.BottomMenuNotifyBrowserEvent.Menu;
import com.polar.browser.vclibrary.bean.events.FbNotifyMsgEvent;
import com.polar.browser.vclibrary.bean.events.IntoImageBrowseEvent;
import com.polar.browser.vclibrary.bean.events.SyncBookmarkEvent;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SystemNewsApi;
import com.polar.browser.vclibrary.util.GooglePlayUtil;
import com.polar.browser.video.H5PlayerFullScreenMgr;
import com.polar.browser.video.VideoManager;
import com.polar.browser.view.AddFavMenuView;
import com.polar.browser.view.AddressBarController;
import com.polar.browser.view.FullScreenController;
import com.polar.browser.view.ISearchPageCallBack;
import com.polar.browser.view.MultiWindowView;
import com.polar.browser.view.NightModeAnimation;
import com.polar.browser.view.SearchFrame;
import com.polar.browser.view.SearchPageController;
import com.polar.browser.view.ShareView;
import com.polar.browser.view.SuspenWindowController;
import com.polar.browser.view.ToolbarBottomController;
import com.polar.browser.view.ToolbarMenuView;
import com.polar.browser.webview.CustomFbWebViewClient;
import com.polar.business.ad_business.AdManager;
import com.polar.business.search.view.QuickInputView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import cn.finalteam.okhttpfinal.OkHttpFinal;
import cn.finalteam.okhttpfinal.OkHttpFinalConfiguration;
import io.reactivex.functions.Consumer;

import static com.polar.browser.download_refactor.Constants.TYPE_APK;
import static com.polar.browser.download_refactor.Constants.TYPE_AUDIO;
import static com.polar.browser.download_refactor.Constants.TYPE_DOC;
import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_VIDEO;
import static com.polar.browser.download_refactor.Constants.TYPE_ZIP;
import static com.polar.browser.utils.FileUtils.upgradeHistoryDatabase;

/**
 * BrowserActivity 浏览器的主界面Activity 最重要的类之一
 * 实现IConfigObserver，监听配置变化 实现ITabChangedObserver，监听Tab页改变事件
 * 实现IUrlChangedObserver，监听地址变化 实现IBookmarkObserver，监听书签变化
 * 实现IOpenUrlDelegate，执行打开地址的操作
 */
@SuppressWarnings("deprecation")
public class BrowserActivity extends LemonBaseActivity implements
        IConfigObserver, ITabChangedObserver, IUrlChangedObserver, IWbLoadUrlStatusObserver,
        IBookmarkObserver, IOpenUrlDelegate, OnClickListener, ISearchPageCallBack {

    private static final String TAG = "BrowserActivity";
    // 截图时的模糊半径
    private static final int BLUR_RADIUS = 4;
    // 服务管理器（adblock服务）
    private ServiceManager mServiceManager;
    private boolean mIsInitService = false;
    // 菜单视图
    private ToolbarMenuView mToolbarMenuView;
    // 多标签切换视图
    private MultiWindowView mMultiWindowView;
    // 搜索视图
    private SearchFrame mSearchFrame;
    // 分享视图
    private ShareView mShareView;
    //全图模式下的浮标
    private ImageView mBtnSuspensionWindow;

    private View mHomeFrameView, mToolbarBottom, mSuspensionWindow;
    // 添加收藏视图
    private AddFavMenuView mAddFavView;
    // 键盘上 www. .com . \ 等 view
    private QuickInputView mQuickInputView;
    // 多标签管理器
//	private TabViewManager TabViewManager.getInstance(); fixBug TabViewManager实例应该通过getInstance调用。因为有可能为空
    // tabview一些回调的管理器
    private TabViewCallbackManager mTabViewCallbackManager;
    // 历史记录管理器
//    private HistoryManager mHistoryManager;
    // 书签管理器
    private BookmarkManager mBookmarkManager;
    // 显示有几个tabview的数字
    private TextView mTextTabSize;
    // 处理WebView的delegate
    private IWebViewClientDelegate mWebViewClientDelegate;
    // 处理WebChromeClient的delegate
    private IWebChromeClientDelegate mWebChromeClientDelegate;
    // browserActivity是否在前台显示
    private boolean mIsShow;
    // 视图根节点
    private ViewGroup mRoot;
    // 浏览器模糊截屏
    private Bitmap mBitmap;
    // 浏览器模糊截屏缩小的尺寸
    private float mBitmapRatio = 0.07f;
    // 装载网页内容的frame，在地址栏需要消失或出现的时候，需要调整contentframe的margin
    // TODO: 待优化
    private ContentFrame mContentFrame;
    // 主页
    private HomeFrame mHomeFrame;
    // 滑动侧边前进or后退
    private ISlideDelegate mSlideDelegate;
    // 管理下方菜单
    private ToolbarBottomController mToolbarBottomController;
    // 管理Addressbar
    private AddressBarController mAddressBarController;
    private SearchPageController mSearchPageController;
    // 管理全屏
    private FullScreenController mFullScreenController;
    private boolean mIsLastHome = true;
    // 夜间动画
    private NightModeAnimation mNightModeAnimation;
    // fix bug 。第一次打开浏览器底部工具栏会有动画
    private boolean isFirstOpen = true;
    private View mToolTopMoreMenu;
    private int mStatusBarHeight;
    //自定义webview  for  fb
    private WebView mFbWebview;

    /**
     * 是否初始化了shareSDK
     **/
    private boolean isShareSDKInited;
    /**
     * 退出App时进行一些操作
     */
    private ExitBrowserImpl exitHandler;
    /**
     * 沉浸式底部
     **/
    private View mNavigationBarView;
    private EditLogoView mEditLogoView;
    /**
     * 视频下载提示语
     */
    private RelativeLayout downloadVideoTip;
    /**
     * 看图模式View
     */
    private ImageBrowseView mImageBrowseView;

    IProgressStart mProgressStart = new IProgressStart() {

        @Override
        public void setProgress(WebView webView, int newProgress) {
            mWebViewClientDelegate.setprogress(webView, newProgress);
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(android.content.Context context, Intent intent) {

            // 下载在其他进程→_→，so 接收广播，下载任务变化了，菜单中下载项icon变化
            if (TextUtils.equals(intent.getAction(),
                    CommonData.ACTION_HAS_DOWNLOADING_TASK)) {
                boolean hasDownload = intent.getBooleanExtra(
                        ConfigDefine.HAS_DOWNLOADING_TASK, false);
                ConfigWrapper.put(ConfigDefine.HAS_DOWNLOADING_TASK,
                        hasDownload);
                ConfigWrapper.apply();
                refreshToolbarMenuStatus(hasDownload);
            } else if (TextUtils.equals(intent.getAction(),
                    CommonData.ACTION_DOWNLOAD_ADD_STATISTICS)) {
                String fileName = intent
                        .getStringExtra(ConfigDefine.DOWNLOAD_FILENAME);
                addStatistics(fileName);
            } else if (TextUtils.equals(intent.getAction(),
                    CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED)) {
                // 下载路径更改了，写入文件
                String downlaodFolderPath = intent
                        .getStringExtra(CommonData.KEY_DOWN_ROOT);
                ConfigWrapper.put(CommonData.KEY_DOWN_ROOT, downlaodFolderPath);
                ConfigWrapper.apply();
            }
        }
    };
    private SuspenWindowController suspenWindowController;

    private RelativeLayout mFbNotifyMsgTip;

    private void refreshToolbarMenuStatus(boolean hasDownload) {
        if (mToolbarBottomController != null) {
            mToolbarBottomController.refreshDownloadUI();
        }
    }

    private IScrollChanged mScrollChanged = new IScrollChanged() {

        @Override
        public void onScrollUp() {
            if (AppEnv.sIsFullScreen) {
                return;
            }
            if (mSearchPageController.isNeedShow()) {
                mAddressBarController.hideWithoutAnim();
            } else {
                mAddressBarController.hide();
            }
        }

        @Override
        public void onScrollDown() {
            if (AppEnv.sIsFullScreen) {
                return;
            }
            String url = TabViewManager.getInstance().getCurrentUrl();
            if (url != null ) {
                if (ConfigManager.getInstance().getAlbumAvailable() && mBtnSuspensionWindow != null && UrlUtils.matchInstagramUrl(url)) {//非Instagram网站不显示
                    mBtnSuspensionWindow.setVisibility(View.VISIBLE);
                }
            }

            if (!mSearchPageController.isNeedShow()) {
                mAddressBarController.needshowAddress(false);
            }
        }

        @Override
        public void onScrollChanged() {
        }

        @Override
        public void onScrollShow() {
            if (AppEnv.sIsFullScreen) {
                return;
            }
            if (!mSearchPageController.isShown()) {
                if (mContentFrame.isShown() && !TabViewManager.getInstance().isCurrentHome()) {
                    mAddressBarController.forceShowAddress();
                }
            }
        }
    };
    private IReceivedTitleCallback mReceivedTitleCallback = new IReceivedTitleCallback() {

        @Override
        public void onReceivedTitle(final String title, int tabId) {
            if (TabViewManager.getInstance() == null || TabViewManager.getInstance().getCurrentTabView() == null)
                return;
            if (tabId == TabViewManager.getInstance().getCurrentTabId()) {
                mAddressBarController.updateTitle(title);
            }
            String url = TabViewManager.getInstance().getTabViewByKey(tabId).getUrl();
            if (AppEnv.DEBUG) {
                SimpleLog.d(TAG, "更新标题==" + title + ",,,更新链接===" + url);
            }

            if (ConfigManager.getInstance().isVedioDownload() && UrlUtils.matchYoutubeVideoUrl(url)) {
                mAddressBarController.showDownloadVideoButton(true);
                if (!AppEnv.sIsShownVideoDownloadTip && !AppEnv.sIsFullScreen) {
                    ConfigManager.getInstance().setShownVideoDownloadTip();
                    downloadVideoTip.setVisibility(View.VISIBLE);
                    AppEnv.sIsShownVideoDownloadTip = true;
                }
            } else {
                mAddressBarController.showDownloadVideoButton(false);
            }
            if (!TextUtils.isEmpty(url)) {
                if (mBookmarkManager.isUrlExist(url)) {
                    // 如果当前网页已经被添加过到收藏，并且name和url相同，就把name更换成title
                    if (TextUtils.equals(
                            mBookmarkManager.queryBookmarkInfoByUrl(url).name, url)) {
                        mBookmarkManager.reAddToFav(title, url);
                    }
                }
            }
            mSearchPageController.onNeedHide();
            checkIsNeedHideSearchEngineImg();
        }
    };

    /**
     * 判断是否需要隐藏切换搜索引擎图标
     */
    private void checkIsNeedHideSearchEngineImg() {
        if (mWebViewClientDelegate.copyBackForwardList() != null) {

            WebHistoryItem currentItem = mWebViewClientDelegate.copyBackForwardList().getCurrentItem();
            if (currentItem != null && SearchUtils.checkIsSearch(currentItem.getUrl())) {
                mAddressBarController.setSearchEngineImgVisible(View.VISIBLE);
            } else {
                mAddressBarController.setSearchEngineImgVisible(View.GONE);
            }
        }
    }

    /**
     * 点击分享
     */
    private IShareClick mShareClickImpl = new IShareClick() {

        @Override
        public void click() {
            handleShareClick();
        }
    };

    /**
     * 显示地址栏更多菜单
     */
    private IOpenToolBarTopMore mOpenMoreMenu = new IOpenToolBarTopMore() {

        @Override
        public void openMoreMenu() {
            if (mToolTopMoreMenu.isShown()) {
                mToolTopMoreMenu.setVisibility(View.GONE);
            } else {
                mToolTopMoreMenu.setVisibility(View.VISIBLE);
                mToolTopMoreMenu.setClickable(true);
                //TODO  统计地址栏菜单出现的次数
            }
        }

        @Override
        public void initMoreMenu() {
            if (mToolTopMoreMenu.isShown()) {
                mToolTopMoreMenu.setVisibility(View.GONE);
            }
        }

        @Override
        public void showAddresbar() {
            mAddressBarController.forceShowAddress();
        }
    };
    private IToolbarMenuDelegate mToolbarMenuOperation = new ToolbarMenuImpl(
            this, mShareClickImpl);
    // 处理底层工具栏事件的接口，用于执行前进、后退、主页等操作
    private IToolbarBottom mToolbarBottomOperation = new IToolbarBottom() {

        @Override
        public void switchTab() {
            showSwitchTabView();
            closeMenu();
            mSearchPageController.onNeedHide();
        }

        @Override
        public void openMenu() {
            mToolbarMenuView.refreshImgPushUI(true);
//            mToolbarMenuView.refreshUserInformationUI();
            doMenuClick();
        }

        @Override
        public void goHome() {
            if (TabViewManager.getInstance().isCurrentHome()) {
                TabViewManager.getInstance().scrollToTop();
                CustomToastUtils.getInstance().showTextToast(R.string.already_on_home_page);
            } else {
                TabViewManager.getInstance().goHome();
                mAddressBarController.hideAddressBar();
            }
            closeMenu();
        }

        @Override
        public void goForward() {
            TabViewManager.getInstance().goForward();
            if (!AppEnv.sIsFullScreen) {
                mAddressBarController.setNeedShowAddressBar();
            }
            closeMenu();
            checkIsNeedHideSearchEngineImg();
            setBlockTipEnable(false);
        }

        @Override
        public void goBack() {
            TabViewManager.getInstance().goBack();
            mWebViewClientDelegate.clearFind();
            hideSearchInPageView();
            closeMenu();
            checkIsNeedHideSearchEngineImg();
            setBlockTipEnable(false);
        }

        @Override
        public void closeWindow() {
            int currentTabId = TabViewManager.getInstance().getCurrentTabId();
            TabViewManager.getInstance().removeTabViewById(currentTabId);
            closeMenu();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.CustomThemeActionBarOverlay);
        setContentView(R.layout.activity_browser);
        OkHttpFinalConfiguration.Builder builder = new OkHttpFinalConfiguration.Builder();
        OkHttpFinal.getInstance().init(builder.build());
        EventBus.getDefault().register(this);
        firstInit();
        postInit();
        loadCustomFbWebview();
        initWeatherModule();
    }
    private void initWeatherModule() {
//        AlxLocationManager.onCreateGPS(JuziApp.getAppContext());
    }

    private void loadCustomFbWebview() {
        if (!ConfigManager.getInstance().getFbMessageNotificationEngine()) {
            return;
        }
        mFbWebview = (WebView) findViewById(R.id.custom_fb_webview);

        mFbWebview.setAlwaysDrawnWithCacheEnabled(true);
        mFbWebview.setAnimationCacheEnabled(true);
        mFbWebview.setScrollbarFadingEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mFbWebview, true);
        }

        WebSettings settings = mFbWebview.getSettings();
        settings.setJavaScriptEnabled(true);
        mFbWebview.addJavascriptInterface(new JSInterfaceManager(this), "JSInterfaceManager");
        if (Build.VERSION.SDK_INT < 18) {
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        }
        settings.setDomStorageEnabled(true);
        String appCacheDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(appCacheDir);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mFbWebview.loadUrl(JavaScriptManager.FACEBOOK_HOME);
        mFbWebview.setWebViewClient(new CustomFbWebViewClient());
        injectFbNotiJs(mFbWebview);
    }

    private void injectFbNotiJs(WebView webview) {
        if (ConfigManager.getInstance().getFbMessageNotificationEngine()) {
            JavaScriptManager.injectFbNotiJs(webview);
        }
    }

    private void initUpdateApkTip() {
        mIsShow = true;
        int updateStatus = ConfigWrapper.get(ConfigDefine.APP_UPDATE_STATUS, 0);
        SimpleLog.d(TAG, "BA_updateStatus==" + updateStatus);
        switch (updateStatus) {
            case CommonData.APP_NO_UPDATE:
                return;
            case CommonData.APP_NEED_UPDATE:
                if (isHome() && !AppEnv.sIsShownUpdateApkTip) {
                    String desc = ConfigWrapper.get(ConfigDefine.APP_UPDATE_DESC, "");
                    if (TextUtils.isEmpty(desc)) {
                        return;
                    }
                    ConfigManager.getInstance().setShownUpdateApkTip();
                    AppEnv.sIsShownUpdateApkTip = true;
                    RelativeLayout mUpdateApkTip = (RelativeLayout) findViewById(R.id.rl_bottom_tip);
                    String updateTipStr = getResources().getString(R.string.update_tip);
                    ButtomTipBar.showButtomTipBar(BrowserActivity.this, mUpdateApkTip, new ButtomTipBar.onTipBtnListener() {
                        @Override
                        public void onClickSetting(View v) {

                            if (NetWorkUtils.isNetworkConnected(BrowserActivity.this)) {
                                try {
                                    GooglePlayUtil.launchAppDetail(BrowserActivity.this, AppEnv.PACKAGE_NAME, GooglePlayUtil.GOOGLE_PLAY_APP_PKGNAME);
                                    SimpleLog.d(TAG, "BA_去谷歌市场");
                                } catch (ActivityNotFoundException e) {
                                    String url = ConfigWrapper.get(ConfigDefine.APP_UPDATE_URL, "");
                                    String md5 = ConfigWrapper.get(ConfigDefine.APP_UPDATE_MD5, "");
                                    if (TextUtils.isEmpty(url) || TextUtils.isEmpty(md5)) {
                                        return;
                                    }
                                    Intent intents = new Intent(BrowserActivity.this, DownloadApkService.class);
                                    intents.putExtra(DownloadApkService.DOWNLOAD_URL, url);
                                    intents.putExtra(DownloadApkService.DOWNLOAD_MD5, md5);
                                    startService(intents);
                                    SimpleLog.d(TAG, "BA_Exception");
                                }
                            } else {
                                CustomToastUtils.getInstance().showTextToast(R.string.net_no_connect);
                            }
                            Statistics.sendOnceStatistics(GoogleConfigDefine.UPDATE, GoogleConfigDefine.UPDATE_OK);
                        }

                        @Override
                        public void onClickClose(View v) {
                            Statistics.sendOnceStatistics(GoogleConfigDefine.UPDATE, GoogleConfigDefine.UPDATE_CANCEL);
                        }
                    }, desc, updateTipStr);
                }
                break;
            case CommonData.APP_FORCE_UPDATE:
                break;
            default:
                break;
        }

    }

    private void firstInit() {
        initManager();
        initService();
        initObserver();
        // 初始化图标存储，对于低版本有效
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
        }
        updateDataFile();
        //检测语言是否发生变化
        checkLanIsChanged();
    }

    private void checkLanIsChanged() {
        String lan = SystemUtils.getLan();
        String lastRunLan = ConfigManager.getInstance().getLastRunLan();
        if (!TextUtils.equals(lastRunLan, lan)) {
            if (ConfigManager.getInstance().getLanChangedRestart()) {
                showLanChangedDialog();
            }
            ConfigManager.getInstance().setLastRunLan(lan);
        }
    }

    private void postInit() {
        initView();
        initDelegate();
        initReceiver();
        TabViewManager.getInstance().fromJson();
        initPrivacyMode();
        handleOnCreateIntent();
        PermissionsHelper.requestPermissions(this);
        Statistics.sendForceExitStatistics();
        initListener();
        initUpdateApkTip();
        initDataDelayOnIoThread(this);
        JavaScriptManager.initJsInfoList();
        Statistics.procStaJobs();
    }

    /**
     * 在主界面显示后，IO线程做一些延迟加载数据
     */
    private void initDataDelayOnIoThread(final Context context) {
        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                addShortCutToDesktop(context);
                initPresetSystemNews();
                if (SysUtils.getAppVersionCode() == 66 && JuziApp.getInstance().isApkUpdated()) {
                    upgradeHistoryDatabase(context);
                }
                sendStatisticsVideoPlugState();
            }
        });
    }

    /**
     * 每天上报一次下载插件开关状态
     */
    private void sendStatisticsVideoPlugState() {
        if (System.currentTimeMillis()- ConfigManager.getInstance().getRecordVideoPlugLastState()> Constants.ONE_DAY_TIMEMILLIS) {
            ConfigManager.getInstance().setRecordVideoPlugLastState(System.currentTimeMillis());
            Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.PLUG_VIDEO,
                    ConfigManager.getInstance().isVedioDownload()?"开":"关");
        }
    }

    /**
     * 首次安装or应用升级，发送附属图标到手机桌面
     */
    private void addShortCutToDesktop(Context context) {
        if (!ConfigManager.getInstance().isAddShortCutToDesktop()) {
            if (JuziApp.getInstance().isFirstRun()) {
                ShortCutUtil.addShortCutToDesktop(context, getString(R.string.launcher_affiliate_name));
            } else if (JuziApp.getInstance().isApkUpdated() && 0 < SysUtils.getAppVersionCode()
                    && SysUtils.getAppVersionCode() <= 44) {
                ShortCutUtil.addShortCutToDesktop(context, getString(R.string.launcher_affiliate_name));
            }
            ConfigManager.getInstance().setAddShortCutToDesktop();
        }
    }

    /**
     * 首次安装在系统消息列预置产品介绍页数据
     */
    private void initPresetSystemNews() {
        if (JuziApp.getInstance().isFirstRun() && !AdManager.getInstance().getApplicationTagBean().isPresetSystemNewsData()) {
            try {
                String loadHelpUrl = Statistics.getLoadHelpUrl();
                SystemNews systemNews = new SystemNews(getString(R.string.systemnews_app_title), loadHelpUrl, new Date());
                SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).insert(systemNews);
                ConfigManager.getInstance().updateSystemNewsHintState(true);
                AdManager.getInstance().getApplicationTagBean().setPresetSystemNewsData(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private void initListener() {
        if (mWebViewClientDelegate instanceof WebViewClientImpl) {
            ((WebViewClientImpl) mWebViewClientDelegate).setmOnNeedHideCallBack(mSearchPageController);
        }
    }

    private void initPrivacyMode() {
        if (ConfigManager.getInstance().isPrivacyMode()) {
            try {
                CookieUtil.clearCookie(this.getApplicationContext());
                TabViewManager.getInstance().clearCache();
                if (WebViewDatabase.getInstance(this.getApplicationContext())
                        .hasFormData()) {
                    WebViewDatabase.getInstance(
                            this.getApplicationContext()
                                    .getApplicationContext()).clearFormData();
                }
                if (WebViewDatabase.getInstance(this.getApplicationContext())
                        .hasHttpAuthUsernamePassword()) {
                    WebViewDatabase.getInstance(
                            this.getApplicationContext()
                                    .getApplicationContext())
                            .clearHttpAuthUsernamePassword();
                }
                if (WebViewDatabase.getInstance(this.getApplicationContext())
                        .hasUsernamePassword()) {
                    WebViewDatabase.getInstance(
                            this.getApplicationContext()
                                    .getApplicationContext())
                            .clearUsernamePassword();
                }
            } catch (Exception e) {
                SimpleLog.e(e);
            }
        }
    }

    private boolean isHaveFbTab;
    private int fbTabIndex;
    private void handleIntent(Intent intent) {
        SimpleLog.d(TAG, "handleIntent");
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Intent.ACTION_WEB_SEARCH:
                    try {
                        String words = intent.getStringExtra(SearchManager.QUERY);
                        if (!TextUtils.isEmpty(words)) {
                            if (words.length() > 200) {
                                words = words.substring(0, 200);
                            }
                            if (TextUtils.isEmpty(words)) {
                                return;
                            }
                            final String url = SearchUtils.buildSearchUrl(words, this);
                            mRoot.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!TextUtils.isEmpty(url)) {
                                        openUrl(url, Constants.TYPE_FROM_SEARCH);
                                    }
                                }
                            });
                        }
                    } catch (OutOfMemoryError e) {
                    }
                    break;
                case CommonData.OPEN_HISTORY_OR_BOOKMARK_ITEM:
                    final int typeFrom = intent.getIntExtra(CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_DEFAULT);
                    final String itemUrl = intent.getStringExtra(CommonData.ACTION_GOTO_URL);
                    if (!TextUtils.isEmpty(itemUrl)) {
                        mRoot.post(new Runnable() {
                            @Override
                            public void run() {
                                openUrl(itemUrl, typeFrom);
                            }
                        });
                    }
                    break;
                case CommonData.ACTION_LOGIN_SUCCESS_TIP:
                    final LoginSuccessDialog loginSuccessDialog = new LoginSuccessDialog(this);
                    loginSuccessDialog.show();
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_FIRST_LOGIN_SYNC_BOOKMARK);
                    ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                        @Override
                        public void run() {
                            loginSuccessDialog.dismiss();
                            ConfigManager.getInstance().setShowLoginSuccessTip();
                        }
                    },3000);

                    break;
                case CommonData.ACTION_OPEN_PRODUCT_ABOUT:
                    final String loadHelpUrl = intent.getStringExtra(CommonData.SYSTEM_CONTENT_URL);
                    if (!TextUtils.isEmpty(loadHelpUrl)) {
                        mRoot.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!TabViewManager.getInstance().isCurrentHome()) {
                                    TabViewManager.getInstance().addTabView(true);
                                    TabViewManager.getInstance().showContentHideHome();
                                }
                                openUrl(loadHelpUrl, Constants.TYPE_FROM_PRODUCT_ABOUT);
                            }
                        });
                    }

                    break;
                case CommonData.ACTION_INTENT_MOBONUS_DATA:
                    String mobonusUrl = intent.getStringExtra(CommonData.MOBONUS_DATA_URL);
                    if (!TextUtils.isEmpty(mobonusUrl)) {
                        ConfigWrapper.put(ConfigDefine.MOBONUS_URL, mobonusUrl);
                        ConfigWrapper.apply();
                        openUrl(mobonusUrl, Constants.TYPE_FROM_MOBONUS);
                    }
                    break;
                case CommonData.ACTION_INTENT_WIFIGUARDER_DATA:
                    String wifiGuarderUrl = intent.getStringExtra(CommonData.MOBONUS_DATA_URL);
                    Statistics.sendOnceStatistics(GoogleConfigDefine.OPEN_BY_WIFIGUARDER, TextUtils.isEmpty(wifiGuarderUrl) ? "noUrl" : wifiGuarderUrl);
                    if (!TextUtils.isEmpty(wifiGuarderUrl)) {
                        openUrl(wifiGuarderUrl, Constants.TYPE_FROM_WIFIGUARDER);
                    }
                    break;
                case CommonData.QUICK_SEARCH_SETTING:
                    startActivity(new Intent(BrowserActivity.this, SettingActivity.class));
                    Statistics.sendOnceStatistics(
                            GoogleConfigDefine.NOTIFI, GoogleConfigDefine.NOTIFI_SETTING);
                    Statistics.sendOnceStatistics(
                            GoogleConfigDefine.NOTIFI, GoogleConfigDefine.NOTIFI_WAKEUP_APP);
                    break;
                case CommonData.ACTION_CLICK_NOTIFICATION:
                    String typeString = intent.getStringExtra(CommonData.DATA_TYPE);
                    if (typeString == null) {
                        return;
                    }
                    switch (typeString) {
                        case CommonData.TYPE_NEWS:
                            News news = (News) intent.getParcelableExtra(CommonData.EXTRA_NEWS);
                            String url = news.getUrl();
                            openUrl(url, Constants.TYPE_FROM_FCM_NOTIF);
                            Statistics.sendOnceStatistics(GoogleConfigDefine.FCM_NEW_CLICK, url);
                            break;
                        case CommonData.TYPE_SYSTEM_NEWS:
                            startActivity(new Intent(this, SystemNewsListActivity.class));
                            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                            Statistics.sendOnceStatistics(GoogleConfigDefine.FCM_SYSTEM, GoogleConfigDefine.FCM_SYSTEM_NOTIFY);
                            break;
                        default:
                            break;
                    }
                    break;
                case CommonData.ACTION_CLICK_FB_NOTIFICATION:
                    SimpleLog.d("--MyLog--", "ACTION_CLICK_FB_NOTIFICATION接收");
                    String stringFbNumber = intent.getStringExtra(FbNotifyManager.FB_NOTIFY_MEG_NUMBER);
                    if (!TextUtils.isEmpty(stringFbNumber) && TextUtils.equals(FbNotifyManager.FB_NOTIFY_MEG_NUMBER,stringFbNumber)) {
                        int fbNotifyMsgNumber = ConfigManager.getInstance().getFbNotifyMsgNumber();
                        ConfigManager.getInstance().setFbNotifyMsgNumber(--fbNotifyMsgNumber);
                    }
                    final String fbNotifyMsgUrl = intent.getStringExtra(CommonData.FB_NOTIFY_DATA_LINK);
                    SimpleLog.d("--MyLog--", "ACTION_CLICK_FB_NOTIFICATION=="+fbNotifyMsgUrl);

                    List<TabViewManager.TabData> tabViewList = TabViewManager.getInstance().getTabViewList();
                    if (ListUtils.isEmpty(tabViewList) || tabViewList.size() == 1) {
                        if (!TabViewManager.getInstance().isCurrentHome()) {
                            TabViewManager.getInstance().addTabView(true);
                        }
                        if (TextUtils.isEmpty(fbNotifyMsgUrl)) {
                            openUrl(FbNotifyManager.FB_URL_WEBSITE, Constants.NAVIGATESOURCE_NORMAL);
                        } else {
                            openUrl(fbNotifyMsgUrl, Constants.NAVIGATESOURCE_NORMAL);
                        }
                    } else {
                        for (int i = 0; i < tabViewList.size(); i++) {
                            TabViewManager.TabData tabData = tabViewList.get(i);
                            String contentUrl = TabViewManager.getInstance().getTabViewByKey(tabData.tabId).getContentUrl();
                            if (!TextUtils.isEmpty(contentUrl) && contentUrl.contains(FbNotifyManager.FB_URL_PREFIX)) {
                                isHaveFbTab = true;
                                fbTabIndex = tabData.tabId;
                            }
                        }
                        if (isHaveFbTab) {
                            TabViewManager.getInstance().switchTabByKey(fbTabIndex);
                            if (!TextUtils.isEmpty(fbNotifyMsgUrl)) {
                                TabViewManager.getInstance().getTabViewByKey(fbTabIndex).loadUrl(fbNotifyMsgUrl,Constants.NAVIGATESOURCE_NORMAL);
                            }
                        } else {
                            if (!TabViewManager.getInstance().isCurrentHome()) {
                                TabViewManager.getInstance().addTabView(true);
                            }
                            if (TextUtils.isEmpty(fbNotifyMsgUrl)) {
                                openUrl(FbNotifyManager.FB_URL_WEBSITE, Constants.NAVIGATESOURCE_NORMAL);
                            } else {
                                openUrl(fbNotifyMsgUrl, Constants.NAVIGATESOURCE_NORMAL);
                            }
                        }
                    }
                    break;
                default:
                    if (intent.hasExtra(CommonData.ACTION_GOTO_URL)) {
                        final int typeFroms = intent.getIntExtra(CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_DEFAULT);
                        final String url = intent.getStringExtra(CommonData.ACTION_GOTO_URL);
                        if (!TextUtils.isEmpty(url)) {
                            mRoot.post(new Runnable() {
                                @Override
                                public void run() {
//									if (TabViewManager.getInstance() != null) {
//										TabViewManager.getInstance().showContentHideHome();
//									}
                                    if (!TabViewManager.getInstance().isCurrentHome()) {
                                        TabViewManager.getInstance().addTabView(true, true, false);
                                    }
                                    openUrl(url, typeFroms);
                                }
                            });
                        }
                    }
                    if (TextUtils.equals(Intent.ACTION_MAIN, action)
                            && Constants.TYPE_FROM_DESKTOP_LAUNCHER == intent.getIntExtra(
                            CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_DEFAULT)) {
                        Statistics.sendOnceStatistics(GoogleConfigDefine.DESKTOP_LAUNCHER, GoogleConfigDefine.DESKTOP_LAUNCHER_CLICK);
                    }
                    break;
            }
        }
    }


    private void handleOnCreateIntent() {

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (CommonData.QUICK_SEARCH_SEARCH.equals(action)) {
            if (mSearchFrame != null) {
                SimpleLog.e(TAG, "onCreat..mAddressBarController != null");
                if (TabViewManager.getInstance() != null && !TabViewManager.getInstance().isCurrentHome()) {
                    TabViewManager.getInstance().addTabView(true);
                }
                mSearchFrame.show(CommonData.NOTIFY_SEARCH_CLICK, "", mHomeFrameView);
            } else {
                SimpleLog.e(TAG, "onCreat..mAddressBarController = null");
            }
            Statistics.sendOnceStatistics(
                    GoogleConfigDefine.NOTIFI, GoogleConfigDefine.NOTIFI_SEARCH);
            Statistics.sendOnceStatistics(
                    GoogleConfigDefine.NOTIFI, GoogleConfigDefine.NOTIFI_WAKEUP_APP);
        } else {
            handleIntent(intent);
        }
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonData.ACTION_HAS_DOWNLOADING_TASK);
        filter.addAction(CommonData.ACTION_DOWNLOAD_ADD_STATISTICS);
        filter.addAction(CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    /**
     * 更新数据文件
     */
    private void updateDataFile() {
        IUpdateFileCallback updateFileCallback = new IUpdateFileCallback() {
            @Override
            public void notifyFileDownload(String fileName, File file) {
                if (fileName.equals(AdFilter.ADBLOCK_URL_RULE) ||
                        fileName.equals(AdFilter.ADBLOCK_CSS_RULE)) {
                    if (!AdFilter.DEBUG_RULE) {
                        if (!mIsInitService) {
                            initService();
                        } else {
                            IAdBlockService adBlockService = mServiceManager.getAdBlockService();
                            try {
                                adBlockService.updateRule(fileName);
                            } catch (Throwable e) {
                            }
                        }
                    }
                } else if (fileName.equals(ParseConfig.SHORTCUT_ZIP_FILE)) {
                    ParseConfig.unzipFile(file);
                    ParseConfig.parseData();
                } else if (fileName.endsWith(".zip")) {
                    ParseConfig.unzipPluginFile(file);
                }
            }
        };
        UpdateDataTask task = new UpdateDataTask(updateFileCallback);
        ThreadManager.postDelayedTaskToNetworkHandler(task, 8000);
    }

    private void openUrl(String url, int type) {
        openUrl(url, type, false);
    }

    private void openUrl(String url, int type, boolean isSearch) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        refreshDownloadVideoButton(url, mAddressBarController);
        switch (type) {
            case Constants.TYPE_FROM_ADDR:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_ADDR);
                break;
            case Constants.TYPE_FROM_BOOKMARK:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_BOOKMARK);
                break;
            case Constants.TYPE_FROM_HISTORY:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_HISTORY);
                break;
            case Constants.TYPE_FROM_PRODUCT_ABOUT:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_PRODUCT_ABOUT);
                break;
            case Constants.TYPE_FROM_SHORTCUT:
                if (TabViewManager.getInstance().isCurrentHome()) {
                    TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_OTHER);
                } else {
                    TabViewManager.getInstance().addTabView(url, false);
                }
                break;
            case Constants.TYPE_FROM_SEARCH:
                String engineNameByEngineType = SearchUtils.getEngineNameByEngineType();
                Statistics.sendOnceStatistics(GoogleConfigDefine.SEARCH_ENGINE, engineNameByEngineType);
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_NORMAL);
                break;
            case Constants.TYPE_FROM_NOTIFICATION:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_NORMAL);
                break;
            case Constants.TYPE_FROM_MOBONUS:
            case Constants.TYPE_FROM_WIFIGUARDER:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_NORMAL);
                break;
            case Constants.TYPE_FROM_FCM_NOTIF:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_NORMAL);
                break;
            default:
                TabViewManager.getInstance().loadUrl(url, Constants.NAVIGATESOURCE_NORMAL);
                break;
        }
    }

    @Override
    public Intent getIntent() {
        return super.getIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        SimpleLog.d(TAG, "onNewIntent");
        checkLanIsChanged();
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }
        try {
            if (mToolbarMenuView != null) {
                mToolbarMenuView.hide();
            }
            if (mMultiWindowView != null) {
                mMultiWindowView.setVisibility(View.GONE);
            }
            if (mShareView != null) {
                mShareView.setVisibility(View.GONE);
            }
            if (exitHandler != null) {
                exitHandler.hindExitDialog();
            }
            if (mSearchFrame != null) {
                mSearchFrame.hideSelf(false);
            }
            if (mEditLogoView != null) {
                mEditLogoView.onHind();
            }
        } catch (Exception e) {
            SimpleLog.e(e);
        }
        if (CommonData.ACTION_LOAD_SAVED_PAGES.equals(action)) {
            final String filePath = intent.getStringExtra(CommonData.EXTRA_LOAD_SAVED_PAGES_DATA);
            SimpleLog.d(TAG, "filePath:" + filePath);
            if (Build.VERSION.SDK_INT < 19) {
                final String data = new String(FileUtils.readFile(new File(filePath)));
                TabViewManager.getInstance().loadUrl(filePath, data, Constants.NAVIGATESOURCE_OTHER, null, true);
            } else {
                TabViewManager.getInstance().loadUrl("file://" + filePath, Constants.NAVIGATESOURCE_NORMAL);
            }
        } else if (CommonData.QUICK_SEARCH_SEARCH.equals(action)) {
            if (mSearchFrame != null) {
                SimpleLog.e(TAG, "onNewIntent..");
                if (TabViewManager.getInstance() != null && !TabViewManager.getInstance().isCurrentHome()) {
                    TabViewManager.getInstance().addTabView(true);
                }
                mSearchFrame.show(CommonData.NOTIFY_SEARCH_CLICK, "", mHomeFrameView);
            } else {
                SimpleLog.e(TAG, "onNewIntent..mAddressBarController==null");
            }
            Statistics.sendOnceStatistics(GoogleConfigDefine.NOTIFI, GoogleConfigDefine.NOTIFI_SEARCH);
        } else {
            handleIntent(intent);
        }
    }

    @Override
    public void open(int typeFrom, String url, boolean isSearch) {
        openUrl(url, typeFrom, isSearch);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsShow = false;
        VideoManager.getInstance().onPause(this);
        //H5PlayerFullScreenMgr.getInstance().onPause();
        TabViewManager.getInstance().pauseAll();
        setBlockTipEnable(false);
        if (mFbWebview != null) {
            mFbWebview.onResume();
            mFbWebview.resumeTimers();
        }
    }

    @Override
    protected void onStop() {
        if (mWebViewClientDelegate != null) {
            mWebViewClientDelegate.saveUrl();
        }
        if (mSearchFrame != null) {
            mSearchFrame.saveSk();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsShow = true;
        if (mRoot != null) {
            mRoot.post(new Runnable() {
                @Override
                public void run() {
                    VideoManager.getInstance().onResume(BrowserActivity.this);
                    if (H5PlayerFullScreenMgr.getInstance().isVideoFullScreen()) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                }
            });
        }
        TabViewManager.getInstance().resumeCurrent();
        PermissionsHelper.onResume(this);
        setBlockTipEnable(true);
        if (mFbWebview != null ) {
            mFbWebview.reload();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initService() {
        if (mIsInitService)
            return;
        //Main Process. Shouldn't create AdFilter's object.
        File filesPath;
        if (AdFilter.DEBUG_RULE)
            filesPath = Environment.getExternalStorageDirectory();
        else
            filesPath = JuziApp.getAppContext().getFilesDir();
        File fileUrlRule = new File(filesPath, AdFilter.ADBLOCK_URL_RULE);
        File fileCssRule = new File(filesPath, AdFilter.ADBLOCK_CSS_RULE);

        if ((fileUrlRule.exists() || fileCssRule.exists())) {
            mServiceManager = new ServiceManager();
            mServiceManager.bindAdBlockService(this);
            if (mWebViewClientDelegate != null) {
                mWebViewClientDelegate.setServiceManager(mServiceManager);
            }
            mIsInitService = true;
        }
    }

    private void initNightModeAnimation() {
        if (mNightModeAnimation == null) {
            mNightModeAnimation = new NightModeAnimation(this, mRoot);
            mNightModeAnimation.init();
        }
    }

    private void initView() {
        ConfigData config = new ConfigData(ConfigManager.getInstance().isEnableImg(),
                ConfigManager.getInstance().isEnableNightMode());
        mRoot = (ViewGroup) findViewById(R.id.root);
        mToolbarMenuView = (ToolbarMenuView) findViewById(R.id.toolbar_menu_view);
        mToolbarMenuView.init(mToolbarMenuOperation, config);
        mToolbarMenuView.initView();
        mToolbarBottom = findViewById(R.id.toolbar_bottom);
        mToolbarBottomController = new ToolbarBottomController(this, mToolbarBottomOperation, mToolbarMenuView);
        mMultiWindowView = (MultiWindowView) findViewById(R.id.multi_window_view);
        mAddFavView = (AddFavMenuView) findViewById(R.id.addfav_view);
        mContentFrame = (ContentFrame) findViewById(R.id.content_frame);
        mHomeFrameView = findViewById(R.id.home_frame);
        mHomeFrame = new HomeFrame(mRoot);
        mQuickInputView = (QuickInputView) findViewById(R.id.quick_input);
        mSearchFrame = (SearchFrame) findViewById(R.id.search_frame);
        mSearchFrame.init(this, mQuickInputView);
        mAddressBarController = new AddressBarController(mOpenMoreMenu, this, mContentFrame, mSearchFrame);
        mAddressBarController.init();

        mSearchPageController = new SearchPageController(this, this, mContentFrame);

        mAddressBarController.initAddressBar();
        mSearchPageController = new SearchPageController(this, this, mContentFrame);
        mShareView = (ShareView) findViewById(R.id.share_view);
        mShareView.init(new ShareDelegateImpl(mRoot));
        mTextTabSize = (TextView) findViewById(R.id.multiwindow_size);
        mFullScreenController = new FullScreenController(this);

        mFullScreenController.init(mToolbarBottomController, mAddressBarController, mSearchPageController);

        mNavigationBarView = findViewById(R.id.browser_navigation_bar);
        mSlideDelegate = new SlideDelegateImpl(this);
        mEditLogoView = (EditLogoView) findViewById(R.id.editlogo_view);
        downloadVideoTip = (RelativeLayout) findViewById(R.id.tv_download_video_tip);
        mFbNotifyMsgTip = (RelativeLayout) findViewById(R.id.rl_fb_notify_msg);
        mFbNotifyMsgTip.setOnClickListener(this);
        mToolTopMoreMenu = findViewById(R.id.toolbar_top_more);
        mToolTopMoreMenu.findViewById(R.id.menu_add_bookmark).setOnClickListener(this);
        mToolTopMoreMenu.findViewById(R.id.menu_share_page).setOnClickListener(this);
        mToolTopMoreMenu.findViewById(R.id.menu_save_page).setOnClickListener(this);
        mToolTopMoreMenu.findViewById(R.id.menu_edit_img).setOnClickListener(this);
        mToolTopMoreMenu.findViewById(R.id.menu_search_page).setOnClickListener(this);
        mStatusBarHeight = SysUtils.getStatusHeight(this);
        refreshFullScreenUI(AppEnv.sIsFullScreen);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

        //// FIXME: 17/2/15 后期研究下源码，初始化时序 严格一些。
        TabViewManager.getInstance().init(mTabViewCallbackManager,
                ConfigManager.getInstance(), this,
                mAddressBarController.getAddressBarHandler(), mScrollChanged,
                mSearchFrame, mSlideDelegate, mReceivedTitleCallback,
                mFullScreenController, mHomeFrame, mEditLogoView,
                mProgressStart, mToolbarBottomController);
        TabViewManager.getInstance().postInit();
        mBtnSuspensionWindow = (ImageView) findViewById(R.id.suspension_window_btn);
        mSuspensionWindow =findViewById(R.id.suspension_window);
        suspenWindowController = new SuspenWindowController(BrowserActivity.this ,mBtnSuspensionWindow ,mSuspensionWindow );
        if (mSlideDelegate instanceof SlideDelegateImpl) {
            ((SlideDelegateImpl)mSlideDelegate).setSuspensionWindow(suspenWindowController);
        }
        mImageBrowseView = (ImageBrowseView) findViewById(R.id.image_browse);
    }

    private void initObserver() {

        ConfigManager.getInstance().registerObserver(this);
        TabViewManager.getInstance().registerObserver(this);

        mBookmarkManager.registerObserver(this);
//		EventBus.getDefault().register(this);
        RxBus.get().safetySubscribe(SyncBookmarkEvent.class,this)
                .subscribe(new Consumer<SyncBookmarkEvent>() {
                    @Override
                    public void accept(SyncBookmarkEvent syncBookmarkEvent) throws Exception {
                        switch (syncBookmarkEvent.type){
                            case SyncBookmarkEvent.TYPE_LOGIN_SUCCESS:
                                Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC,GoogleConfigDefine.BOOKMARK_AUTO_SYNC,GoogleConfigDefine.BOOKMARK_LOGIN_SYNC);
                                ConfigManager.getInstance().setLoginSyncBookmarkFinished("false");
                                BookmarkManager.getInstance().syncBookmark(BrowserActivity.this,false);
                                SettingSyncManager.getInstance().syncSetting(SettingSyncManager.SYNC_TYPE_LOGIN);
                                //UserHomeSiteManager.getInstance().syncHomeSite(UserHomeSiteManager.SYNC_TYPE_LOGIN);
                                break;
                            case SyncBookmarkEvent.TYPE_MANUAL_SYNC_SUCCESS:
                                CustomToastUtils.getInstance().showTextToast(R.string.sync_bookmark_success);
                                break;
                            case SyncBookmarkEvent.TYPE_SYNC_FAILED:
                                CustomToastUtils.getInstance().showTextToast(R.string.sync_bookmark_failed);
                                break;
                            case SyncBookmarkEvent.TYPE_AUTO_SYNC_SUCCESS:
                                ConfigManager.getInstance().setLoginSyncBookmarkFinished("true");
                                BookmarkManager.getInstance().addTempToUserFile();
                                break;
                            case SyncBookmarkEvent.TYPE_LOGOUT_SYNC_FAILED:
                                ConfigManager.getInstance().setLoginSyncBookmarkFinished("true");
                                break;
                        }



//
                    }
                });
    }

    private void initDelegate() {
        IDownloadDelegate downloadDelegateImpl = new DownloadDelegateImpl();
        mWebViewClientDelegate = new WebViewClientImpl(this, mServiceManager, mScrollChanged,
                mAddressBarController.getProgressCallback());
        ((WebViewClientImpl) mWebViewClientDelegate).setmOnNeedHideCallBack(mSearchPageController);
        mWebViewClientDelegate.registUrlChangedObserver(this);
        mWebViewClientDelegate.registWbLoadUrlStatusObserver(this);
        mTabViewCallbackManager.registerDownloadDelegate(downloadDelegateImpl);
        mTabViewCallbackManager.registerWebViewClientDelegate(mWebViewClientDelegate);
        mWebChromeClientDelegate = new WebChromeClientImpl(this);
        mTabViewCallbackManager.registerWebChromeClientDelegate(mWebChromeClientDelegate);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        TabViewOnLongClickListener onClickListener = new TabViewOnLongClickListener(
                this, width, height, mRoot, mShareClickImpl);
        mTabViewCallbackManager.registerOnLongClickListener(onClickListener);
        mTabViewCallbackManager.registerTouchListener(onClickListener);
    }

    private void initManager() {
        ConfigManager.getInstance().setAlbumAvailable(false);
//        mHistoryManager = HistoryManager.getInstance();
        mBookmarkManager = BookmarkManager.getInstance();
//		TabViewManager.getInstance() = TabViewManager.getInstance();
        mTabViewCallbackManager = TabViewCallbackManager.getInstance();
    }

    /**
     * 显示添加/取消收藏的提示
     *
     * @param isAdd
     */
    private void showBookmarkTips(boolean isAdd) {
        if (mIsShow) {
            if (isAdd) {
                CustomToastUtils.getInstance().showImgToast(
                        R.string.add_bookmark_tips, R.drawable.address_bookmark_star_added);
            } else {
                CustomToastUtils.getInstance().showTextToast(R.string.delete_bookmark_tips);
            }
        }
    }

    @Override
    protected void onDestroy() {
        SimpleLog.d("TabViewManager", "onDestroy");
        EventBus.getDefault().unregister(this);
        ThreadManager.destroy();
//		EventBus.getDefault().unregister(this);
        try {
            mRoot.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
        } catch (Throwable e) {
        }
        FileUtils.dismissFileUploadHandler();
        FileUtils.resetFileUploadHandler();
        if (TabViewManager.getInstance() != null) {
            TabViewManager.getInstance().unregisterObserver(this);
            TabViewManager.getInstance().destroy();
        }
        SiteManager.getInstance().updateHistoryRecords(null);
        if (mWebViewClientDelegate != null) {
            mWebViewClientDelegate.unregistUrlChangedObserver(this);
        }
        if (mWebViewClientDelegate != null) {
            mWebViewClientDelegate.unregistWbLoadUrlStatusObserver(this);
        }
        if (mWebViewClientDelegate != null) {
            mWebViewClientDelegate.unregisterWbVideoPlayObserver();
        }
        if (mTabViewCallbackManager != null) {
            mTabViewCallbackManager.destroy();
            mTabViewCallbackManager = null;
        }
//        if (mHistoryManager != null) {
//            mHistoryManager.destroy();
//            mHistoryManager = null;
//        }
        if (mBookmarkManager != null) {
            mBookmarkManager.unregisterObserver(this);
            mBookmarkManager = null;
        }
        if (mMultiWindowView != null) {
            mMultiWindowView.destory();
        }
        if (mFullScreenController != null) {
            mFullScreenController.onDestory();
        }
        // 防止内存泄露，activity不能被回收
        ConfigManager.getInstance().unregisterObserver(this);
        // 服务必须要解除绑定
        if (mServiceManager != null) {
            mServiceManager.unBindAdBlockService(this);
        }
        if (mHomeFrame != null) {
            mHomeFrame.destroy();
        }
        if (mToolbarMenuView != null) {
            mToolbarMenuView.unInit();
        }
        if (mToolbarBottomController != null) {
            mToolbarBottomController.destroy();
        }
        if (mShareView != null) {
            mShareView.destroy();
        }
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        if (mReceiver != null) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Throwable e) {
            }
        }

        if (mSearchFrame != null) {
            mSearchFrame.destroy();
        }
        super.onDestroy();
    }

    private void closeMenu() {
        if (mToolbarMenuView.getVisibility() == View.VISIBLE) {
            mToolbarMenuView.hide();
        }
    }

    // 监听Config状态的改变
    @Override
    public void notifyChanged(String key, final boolean value) {
        if (key.equals(ConfigDefine.ENABLE_IMG)) {
            notifyNoImgMode(value);
        } else if (key.equals(ConfigDefine.ENABLE_NIGHT_MODE)) {
            notifyNightMode(value);
        } else if (key.equals(ConfigDefine.PLUG_VIDEO_DOWNLOAD)) {
            notifyPlugDownloadMode(value);
        } else if (key.equals(ConfigDefine.ENABLE_FULL_SCREEN)) {
            notifyFullScreen(value);
        } else if (key.equals(ConfigDefine.PRIVACY_MODE)) {
            notifyPrivacyMode(value);
        } else if (key.equals(ConfigDefine.FCM_SYSTEM_NEWS_HINT_STATE)) {
            SimpleLog.d(TAG, "FCM_有信息更新回调");
            notifySystemNewsHintStatus(value);
        } else if (key.equals(ConfigDefine.GOFOWARD_OR_GOBACK)) {
            checkIsNeedHideSearchEngineImg();
            mWebViewClientDelegate.clearFind();
            hideSearchInPageView();

        }else if(key.equals(ConfigDefine.IS_AD_BLOCK_TOAST)){
            setBlockTipEnable(value);
        } else if (key.equals(ConfigDefine.HIDE_IM)) {
            if (mSearchPageController != null && mSearchPageController.isShown())
                hideIM();
        }
    }

    @Override
    public void notifyChanged(String key, String value) {
        if (key.equals(ConfigDefine.SEARCH_KEY_CHANGED)) {
            mSearchFrame.setSearchKey(value);
        }
    }

    @Override
    public void notifyChanged(String key, final int value) {
        if (key.equals(ConfigDefine.FONT_SIZE)) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    TabViewManager.getInstance().setFontSize(value);
                }
            };
            ThreadManager.postTaskToUIHandler(r);
        } else if (key.equals(ConfigDefine.SEARCH_ENGINE)) {
            ThreadManager.postTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    if (mSearchFrame != null) {
                        mSearchFrame.refreshSearchEngineUI(value);
                    }
                    if (mAddressBarController != null) {
                        mAddressBarController.refreshSearchEngineUI(value);
                    }
                }
            });

        }else if (key.equals(ConfigDefine.AD_BLOCKED_COUNT)) {
            showAdBlockToast(value);
        }
    }

    private void showAdBlockToast(int value) {
        int blockedCount = ConfigManager.getInstance().getAdBlockedCount();
        String toastContent = "";
        String toastTip = "";
        if(blockedCount<100&&blockedCount+value>=100){
            toastContent = getString(R.string.ad_block_num_100);
            toastTip = getString(R.string.extract_view_now);
            Statistics.sendOnceStatistics(GoogleConfigDefine.SETTING, GoogleConfigDefine.AB_ADBLOCK_MENU_CLICK,GoogleConfigDefine.AB_ADBLOCK_TIP_MORE_THAN100);
        }else if(ConfigManager.getInstance().isAdBlockTip()){
            toastContent = String.format(getString(R.string.setting_ad_block_count),value);
            toastTip = getString(R.string.never_remind);
            Statistics.sendOnceStatistics(GoogleConfigDefine.SETTING,GoogleConfigDefine.AB_ADBLOCK_MENU_CLICK, GoogleConfigDefine.AB_ADBLOCK_TIP_SHOW);
        }
        final String finalToastTip = toastTip;
        if(TextUtils.isEmpty(toastContent)||TextUtils.isEmpty(toastTip))
            return;


        CustomToastUtils.getInstance().showArrowClickToast(this,toastContent,
                toastTip, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String type;
                        if(getString(R.string.never_remind).equals(finalToastTip)){
                            ConfigManager.getInstance().setEnableAdBlockTip(false);
                            type = GoogleConfigDefine.AB_ADBLOCK_NEVER_REMIND;
                        } else{
                            startActivity(new Intent(BrowserActivity.this, AdBlockSettingActivity.class));
                            type = GoogleConfigDefine.AB_ADBLOCK_VIEW_NOW;
                        }

                        if (!TextUtils.isEmpty(type)) {
                            Statistics.sendOnceStatistics(GoogleConfigDefine.SETTING,GoogleConfigDefine.AB_ADBLOCK_MENU_CLICK, type);
                        }

                    }
                });
    }

    private void notifySystemNewsHintStatus(final boolean hint) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mToolbarBottomController.refreshMenuPushUI(hint);
                mToolbarMenuView.refreshImgPushUI(hint);
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    // TODO 智能无图模式切换动画
    private void notifyNoImgMode(final boolean enableImg) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //切换无图模式刷新页面
                TabViewManager.getInstance().getCurrentTabView().reload();
                mToolbarMenuView.refreshBtnNoImgUI(enableImg);
                // 没有开启图片，这时候需要智能判断一下
                if (!enableImg) {
                    TabViewManager.getInstance().setLoadsImages(false);
                } else {
                    TabViewManager.getInstance().setLoadsImages(true);
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private void notifyPlugDownloadMode(final boolean isVedioDownload) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //插件中心视频下载开关变化刷新页面
                TabViewManager.getInstance().getCurrentTabView().reload();
                if (!isVedioDownload) {
                    downloadVideoTip.setVisibility(View.GONE);
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private void notifyNightMode(final boolean nightMode) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (nightMode) {
                    if (mIsShow) {
                        initNightModeAnimation();
                        mNightModeAnimation
                                .startNightModeAnimation(new com.polar.browser.view.NightModeAnimation.CallBack() {
                                    @Override
                                    public void callBack() {
                                        setBrightness(CommonData.NIGHT_MODE_BRIGHTNESS);
                                        switch2nightMode(true);
                                    }

                                    @Override
                                    public void animEnd() {
                                    }
                                });
                    } else {
                        setBrightness(CommonData.NIGHT_MODE_BRIGHTNESS);
                        switch2nightMode(true);
                    }
                } else {
                    if (mIsShow) {
                        initNightModeAnimation();
                        mNightModeAnimation.startDayModeAnimation(new com.polar.browser.view.NightModeAnimation.CallBack() {
                                    @Override
                                    public void callBack() {
                                        setBrightness(-1);
                                        switch2nightMode(false);
                                    }

                                    @Override
                                    public void animEnd() {
                                    }
                                });
                    } else {
                        setBrightness(-1);
                        switch2nightMode(false);
                    }
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

        @Override
        public void onGlobalLayout() {
            if (!mContentFrame.isShown()) {
                return;
            }
//            // 应用可以显示的区域。此处包括应用占用的区域，
//            // 以及ActionBar和状态栏，但不含设备底部的虚拟按键。
//            Rect r = new Rect();
//            mRoot.getWindowVisibleDisplayFrame(r);
//            // 屏幕高度。这个高度不含虚拟按键的高度
//            int screenHeight = mRoot.getRootView().getHeight();
//            int heightDiff = screenHeight - (r.bottom - r.top);
//            // 在不显示软键盘时，heightDiff等于状态栏的高度
//            // 在显示软键盘时，heightDiff会变大，等于软键盘加状态栏的高度。
//            // 所以heightDiff大于状态栏高度时表示软键盘出现了，
//            // 这时可算出软键盘的高度，即heightDiff减去状态栏的高度
//            if (heightDiff > mStatusBarHeight) {
//                if (mToolbarBottomController.isShown())
//                    mToolbarBottomController.hideWithoutAnim();
//            } else {
//                if (!mToolbarBottomController.isShown() && !AppEnv.sIsFullScreen)
//                    mToolbarBottomController.showWithoutAnim();
//            }

            if (mRoot.getHeight() < AppEnv.SCREEN_HEIGHT * 4 / 5) {
                if (mToolbarBottomController.isShown()) {
                    mToolbarBottomController.hideWithoutAnim();
                }
            } else {
                if (!mToolbarBottomController.isShown() && !AppEnv.sIsFullScreen) {
                    mToolbarBottomController.showWithoutAnim();
                }
            }

        }
    };

    private void notifyFullScreen(final boolean fullScreen) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                SysUtils.setFullScreen(BrowserActivity.this, fullScreen);
                AppEnv.sIsFullScreen = fullScreen;
                refreshFullScreenUI(fullScreen);
                if (fullScreen) {
                    mFullScreenController.hideUI(mIsShow);
                } else {
                    mFullScreenController.showUI(mIsShow);
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private void notifyPrivacyMode(final boolean privacyMode) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mMultiWindowView.setPrivacyMode(privacyMode, true);
                mToolbarBottomController.setPrivacyMode(privacyMode);
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private void refreshFullScreenUI(boolean isFullScreen) {
        mToolbarMenuView.refreshFullScreenUI(isFullScreen);

        if (isFullScreen) {
            RelativeLayout.LayoutParams lp = (LayoutParams) mContentFrame
                    .getLayoutParams();
            lp.topMargin = 0;
            mContentFrame.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpHome = (LayoutParams) mHomeFrame
                    .getView().getLayoutParams();
            lpHome.topMargin = 0;
            mHomeFrame.getView().setLayoutParams(lpHome);
            if (isFirstOpen) {
                mToolbarBottomController.hideWithoutAnim();
                mAddressBarController.hideWithoutAnim();
            } else {
                if (mIsShow) {
                    mToolbarBottomController.hide();
                } else {
                    mToolbarBottomController.hideWithoutAnim();
                }
                mAddressBarController.hide();
            }
            RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) mNavigationBarView
                    .getLayoutParams();
            params1.height = 0;
            mNavigationBarView.setLayoutParams(params1);
        } else {
            RelativeLayout.LayoutParams lp = (LayoutParams) mContentFrame
                    .getLayoutParams();
            lp.addRule(RelativeLayout.ABOVE, R.id.toolbar_bottom);
            mContentFrame.setLayoutParams(lp);
            RelativeLayout.LayoutParams lpHome = (LayoutParams) mHomeFrame
                    .getView().getLayoutParams();
            lpHome.addRule(RelativeLayout.ABOVE, R.id.toolbar_bottom);
            mHomeFrame.getView().setLayoutParams(lpHome);
            if (isFirstOpen) {
                mToolbarBottomController.showWithoutAnim();
                mAddressBarController.showWithoutAnim();
            } else {
                if (mIsShow) {
                    mToolbarBottomController.show();
                } else {
                    mToolbarBottomController.showWithoutAnim();
                }
                mAddressBarController.show();
            }
        }
        /*if (!isFullScreen && android.os.Build.VERSION.SDK_INT > 18) {
			RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.root);
			relativeLayout.setPadding(0, mStatusBarHeight, 0, 0);
		}*/
        if (mMultiWindowView != null) {
            mMultiWindowView.switch2fullScreen(isFullScreen);
        }
        isFirstOpen = false;
    }

    /**
     * 点击设置菜单
     */
    private void doMenuClick() {
        if (mToolbarMenuView.isShown()) {
            mToolbarMenuView.hide();
        } else {
            if (!mToolbarMenuView.isInitView()) {
                mToolbarMenuView.initView();
            }
            mToolbarMenuView.show();
        }
    }

    /**
     * 处理点击分享按钮
     */
    private void handleShareClick() {
        if (mShareView.isShown()) {
            mShareView.hide();
        } else {
            if (!isShareSDKInited) {
                isShareSDKInited = true;
            }
            mShareView.show();
        }
    }

    public void handleAddFavClick() {
        if (mAddFavView.isShown()) {
            mAddFavView.hide();
        } else {
            if (!mAddFavView.isInit()) {
                mAddFavView.init(new AddFavImpl());
            }
            mAddFavView.initTitle();
            hideIM();
            mAddFavView.show();
            mToolTopMoreMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public void notifyTabChanged(final String url, final String isYoutube) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    refreshDownloadVideoButton(url, mAddressBarController);
                    mTextTabSize.setText(String.valueOf(TabViewManager.getInstance().getSize()));
                    mToolbarBottomController.updateBackAndForward(TabViewManager.getInstance()
                            .isCurrentHome(), TabViewManager.getInstance().getCurrentTabView()
                            .canGoForward(), TabViewManager.getInstance().getCurrentTabView()
                            .canGoBack(), TabViewManager.getInstance().getCurrentTabView().isFromBottomMenu());
                    if (TabViewManager.getInstance().isCurrentHome()) {
                        mAddressBarController.hideAddressBar();
                        mIsLastHome = true;
                    } else {
                        if (!AppEnv.sIsFullScreen) {
                            if (mIsLastHome) {
                                mAddressBarController.showAddressBar();
                            } else {
                                mAddressBarController.needshowAddress(true);
                            }
                            if (TabViewManager.getInstance().getCurrentTabView().getProgress() >= 100) {
                                mAddressBarController.showRefreshAddressBar();
                            } else {
                                mAddressBarController.showStopAddressBar();
                            }
                            SimpleLog.d(TAG, "get progress="
                                    + TabViewManager.getInstance().getCurrentTabView()
                                    .getProgress());
                        }
                        mAddressBarController.initAddressBar(url);
                        mIsLastHome = false;
                    }
                    checkShowFloatBtn(TabViewManager.getInstance().getCurrentTabView().getUrl());
                } catch (Throwable e) {
                }
            }
        };
        ThreadManager.postTaskToUIHandler(r);
        TabViewManager.getInstance().toJsonAsync();
    }

    @Override
    public void notifyUrlChanged(final String url, int tabId,
                                 final boolean isOnlyUpdateToolbar, final String rawUrl) {
        if (tabId == TabViewManager.getInstance().getCurrentTabId()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (!AppEnv.sIsFullScreen && !isOnlyUpdateToolbar) {
//                        mAddressBarController.needshowAddress(true);
                    }
                    if (!isOnlyUpdateToolbar) {
                        mAddressBarController.initAddressBar(url);
                    }
                    try {
                        // Add try-catch to escape crash after onDestroy.
                        mToolbarBottomController.updateBackAndForward(
                                TabViewManager.getInstance().isCurrentHome(), TabViewManager.getInstance()
                                        .getCurrentTabView().canGoForward(),
                                TabViewManager.getInstance().getCurrentTabView().canGoBack(),
                                TabViewManager.getInstance().getCurrentTabView().isFromBottomMenu());
                    } catch (Throwable e) {
                    }
                    checkShowFloatBtn(rawUrl);
                    refreshBgFbWebview(rawUrl);
                }
            };
            ThreadManager.postTaskToUIHandler(r);
            if (!isOnlyUpdateToolbar) {
                TabViewManager.getInstance().toJsonAsync();
            }
        }
        SimpleLog.d(TAG, "notifyUrlChanged" + rawUrl);
    }

    private void refreshDownloadVideoButton(String url, AddressBarController addressBarController) {
        if (url == null || addressBarController == null) {
            return;
        }
        if (ConfigManager.getInstance().isVedioDownload() &&
                ConfigManager.getInstance().isServerVideoDownload()) {
            addressBarController.showDownloadVideoButton(UrlUtils.matchYoutubeVideoUrl(url));
        }
    }

    private void refreshBgFbWebview(String rawUrl) {
        if (mFbWebview != null && UrlUtils.matchFbHost(rawUrl)) {
            mFbWebview.loadUrl(rawUrl);
        }else if (UrlUtils.matchFbHost(rawUrl) && mFbWebview == null) {
            loadCustomFbWebview();
        }
    }

    private void checkShowFloatBtn(String url) {
        if (UrlUtils.matchInstagramUrl(url)) {
            if (!mBtnSuspensionWindow.isShown()) {
                suspenWindowController.show();
            }
        } else {
            if (mBtnSuspensionWindow.isShown()) {
                mBtnSuspensionWindow.setVisibility(View.GONE);
            }
        }
    }

    // 展现多窗口切换页面
    private void showSwitchTabView() {
        SimpleLog.d(TAG, "multi tabview");
        forceReloadBlurScreenShot();
        if (TabViewManager.getInstance().isCurrentHome()) {
            mMultiWindowView.init(TabViewManager.getInstance(), mBitmap, isLandscape(), mHomeFrame.getView());
        } else {
            mMultiWindowView.init(TabViewManager.getInstance(), mBitmap, isLandscape(), mContentFrame);
        }
        mMultiWindowView.show();
    }

    /**
     * 强制更新截屏
     */
    public void forceReloadBlurScreenShot() {
        mBitmap = ViewUtils.getScreenShotSync(mRoot, mBitmapRatio,
                mBitmapRatio, Config.RGB_565);
        long start = System.currentTimeMillis();
        mBitmap = Blur.fastblur(BrowserActivity.this, mBitmap, BLUR_RADIUS);
        long end = System.currentTimeMillis();
        SimpleLog.d(TAG, "Blur time:" + String.valueOf(end - start));
    }

    /**
     * 处理back键的各种事件，如关闭菜单等操作，以及回退到上个webview
     */
    @Override
    public void onBackPressed() {
        try {
            if (mImageBrowseView.isShown()) {
                mImageBrowseView.hide();
            } else if (VideoManager.getInstance().isVideoPlayerRunning()) {
                VideoManager.getInstance().onBackPressed();
            } else if (H5PlayerFullScreenMgr.getInstance().isVideoFullScreen()) {
                H5PlayerFullScreenMgr.getInstance().onBackPressed();
            } else if (mSearchFrame != null && mSearchFrame.isShown()) {
                mSearchFrame.hideSelf(true);
            } else if (mToolbarMenuView != null && mToolbarMenuView.isShown()) {
                mToolbarMenuView.hide();
            } else if (mShareView != null && mShareView.isShown()) {
                mShareView.hide();
            } else if (mMultiWindowView != null && mMultiWindowView.isShown()) {
                mMultiWindowView.hide();
            } else if (mAddFavView != null && mAddFavView.isInit()
                    && mAddFavView.isShown()) {
                mAddFavView.hide();
            } else if (mEditLogoView != null && mEditLogoView.isShown()) {
                mEditLogoView.onBackPressed();
            } else if (mToolTopMoreMenu != null && mToolTopMoreMenu.isShown()) {
                mToolTopMoreMenu.setVisibility(View.GONE);

            } else if (mSearchPageController != null && mSearchPageController.isNeedShow()) {
                hideSearchInPageView();
            } else {
                if (TabViewManager.getInstance() != null) {
                    if (TabViewManager.getInstance().isCurrentHome()
                            && TabViewManager.getInstance().getCurrentTabView() != null
                            && !TabViewManager.getInstance().getCurrentTabView()
                            .canGoBack()) {
                        exitHandler = new ExitBrowserImpl();
                        exitHandler.exitFromBackBtn(this);
                    } else {
                        setBlockTipEnable(false);
                        TabViewManager.getInstance().goBack();
                    }
                }
            }
        } catch (Exception e) {
            SimpleLog.e(e);
        }
    }

    private void hideSearchInPageView() {
//		mSearchPageController.setNeedShow(false);
//		hideIM();
//		mSearchPageController.hideWithoutAnim();
//		onClickClose();
        hideIM();
        mSearchPageController.onClickClose();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SimpleLog.e(TAG, "当前屏幕为横屏");
        } else {
            SimpleLog.e(TAG, "当前屏幕为竖屏");
        }
        //在全图模式下切换横竖屏做屏幕适配
        if(mImageBrowseView.isShown()){
            mImageBrowseView.getAdapter().notifyDataSetChanged();
        }

        //判断全图模式提示条是否显示过
        String url = TabViewManager.getInstance().getCurrentUrl();
        if (url != null && !mBtnSuspensionWindow.isShown() && UrlUtils.matchInstagramUrl(url)) {
            suspenWindowController.show();
        }else{
            mSuspensionWindow.setVisibility(View.GONE);
        }
        // 需要判断是否初始化了，否则会出现崩溃
        if (mSearchFrame != null) {
            mSearchFrame.resetLayoutDelay();
        }
        if (mToolbarBottomController != null) {
            mToolbarBottomController.onOrientationChanged();
        }
        if (mToolbarMenuView != null) {
            if (mToolbarMenuView.isInitView()) {
                mToolbarMenuView.onOrientationChanged();
            }
        }
        if (mShareView != null) {
            mShareView.onOrientationChanged();
        }
        if (mMultiWindowView != null) {
            boolean isLandScape = isLandscape();
            mMultiWindowView.onOrientationChanged(isLandScape, mBitmap);
        }
        if (mSlideDelegate != null) {
            mSlideDelegate.onOrientationChanged();
        }
        if (mFullScreenController != null) {
            mFullScreenController.onOrientationChanged();
        }
        if (mHomeFrame != null) {
            mHomeFrame.onOrientationChanged();
        }
    }

    private boolean isLandscape() {
        return AppEnv.SCREEN_WIDTH > AppEnv.SCREEN_HEIGHT;
    }

    @Override
    public void notifyBookmarkChanged(final boolean isAdd,final boolean showTip) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                if(showTip)
                    showBookmarkTips(isAdd);
            }
        };
        ThreadManager.postTaskToUIHandler(r);
    }

    private void addStatistics(String fileName) {
        String type = FileUtils.getFileType(fileName);
        switch (type) {
            case TYPE_DOC:
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_FILE,
                        GoogleConfigDefine.FILE_TYPE_TXT);
                break;
            case TYPE_AUDIO:
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_FILE,
                        GoogleConfigDefine.FILE_TYPE_MUSIC);
                break;
            case TYPE_APK:
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_FILE,
                        GoogleConfigDefine.FILE_TYPE_APK);
                break;
            case TYPE_VIDEO:
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_FILE,
                        GoogleConfigDefine.FILE_TYPE_VIDEO);
                break;
            case TYPE_ZIP:
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_FILE,
                        GoogleConfigDefine.FILE_TYPE_ZIP);
                break;
            case TYPE_IMAGE:
                Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_FILE,
                        GoogleConfigDefine.FILE_TYPE_IMAGE);
                break;
            default:
                break;
        }
    }

    /**
     * 夜间模式or白天模式切换
     *
     * @param nightMode 夜间模式？
     */
    private void switch2nightMode(boolean nightMode) {
        TabViewManager.getInstance().enableNightMode(nightMode);
        TabViewManager.getInstance().forceCaptureScreen();
        mToolbarMenuView.refreshBtnNightModeUI(nightMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == UploadHandler.FILE_SELECTED) {
            UploadHandler handler = FileUtils.getFileUploadHandler();
            if (handler != null) {
                handler.onActivityResult(requestCode, resultCode, intent);
                FileUtils.resetFileUploadHandler();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_add_bookmark:
                handleAddFavClick();
                break;
            case R.id.menu_share_page:

                mToolTopMoreMenu.setVisibility(View.GONE);
                handleShareClick();
                CropStorageUtil.recycle();
                Statistics.sendOnceStatistics(GoogleConfigDefine.ADDRESS_TOOLBAR_MENU, GoogleConfigDefine.FAVORITE_TYPE_SHARE_PAGE);
                break;
            case R.id.menu_save_page:
                String url = TabViewManager.getInstance().getCurrentTabView()
                        .getContentOrigUrl();
                String title = TabViewManager.getInstance().getCurrentTabView()
                        .getContentTitle();
                SavedPageUtil.savePage(this, title, url);
                mToolTopMoreMenu.setVisibility(View.GONE);
                CropStorageUtil.recycle();
                Statistics.sendOnceStatistics(GoogleConfigDefine.ADDRESS_TOOLBAR_MENU, GoogleConfigDefine.FAVORITE_TYPE_SAVE_PAGE);
                break;
            case R.id.menu_edit_img:
                mToolTopMoreMenu.setVisibility(View.GONE);
                CropStorageUtil.recycle();
                Bitmap bitmap = ViewUtils.takeScreenShot(this, true, true);
                CropStorageUtil.setBitmap(bitmap);
                Intent intent = new Intent(this, CropEditActivity.class);
                startActivity(intent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.ADDRESS_TOOLBAR_MENU, GoogleConfigDefine.FAVORITE_TYPE_EDIT_IMG);
                break;
            case R.id.menu_search_page:
                mToolTopMoreMenu.setVisibility(View.GONE);
                mAddressBarController.hideWithoutAnim();
                mSearchPageController.showWithoutAnim();
                mSearchPageController.setNeedShow(true);
                Statistics.sendOnceStatistics(GoogleConfigDefine.FIND_IN_PAGE, GoogleConfigDefine.MENU_FIND_IN_PAGE_CLICK);
                break;
            case R.id.rl_fb_notify_msg:
                TabViewManager.getInstance().getCurrentTabView().webViewJumpToTop();
                if (mFbNotifyMsgTip.isShown()) {
                    mFbNotifyMsgTip.setVisibility(View.GONE);
                }
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_PAGETIP, GoogleConfigDefine.FB_NOTIFY_PAGETIP_CLICK);
            default:
                mToolTopMoreMenu.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 点击空白界面隐藏地址栏更多菜单
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mToolTopMoreMenu == null) {
            return super.dispatchTouchEvent(ev);
        }
        if (mToolTopMoreMenu.isShown()) {
            Rect rectwindow = new Rect();
            mToolTopMoreMenu.getWindowVisibleDisplayFrame(rectwindow);
            int[] location = new int[2];
            mToolTopMoreMenu.getLocationInWindow(location);
            Rect rect = new Rect(location[0], location[1], location[0]
                    + mToolTopMoreMenu.getWidth(), location[1]
                    + mToolTopMoreMenu.getHeight());
            if (!rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                mToolTopMoreMenu.setVisibility(View.GONE);
                return false;
            }
        }
        if (ConfigManager.getInstance().isVedioDownload() && downloadVideoTip.isShown()) {
            downloadVideoTip.setVisibility(View.GONE);
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void notifyLoadingStatusChanged(int loadUrlStatu, String url) {
        switch (loadUrlStatu) {
            case CommonData.WBLOADURL_STATUS_PAGESTART:        //webview 开始加载网页回调
                SimpleLog.d(TAG, "webview 开始加载网页回调=url" + url);
                refreshDownloadVideoButton(url, mAddressBarController);
                if (mFbNotifyMsgTip.isShown()) {
                    mFbNotifyMsgTip.setVisibility(View.GONE);
                }
                break;
            case CommonData.WBLOADURL_STATUS_PAGEFINSH:        //webview 网页加载完成回调
                SimpleLog.d(TAG, "webview 网页加载完成回调=url" + url);
                break;
            case CommonData.WBLOADURL_STATUS_PROGRESS:
                String mobonusUrl = ConfigWrapper.get(ConfigDefine.MOBONUS_URL, "");
                if (!TextUtils.isEmpty(mobonusUrl)) {
                    Intent intent = new Intent();
                    intent.setAction(CommonData.ACTION_MOBONUS_LOADLINK_FINSH);
                    intent.putExtra(CommonData.MOBONUS_LOADFINSH_BACKDATA, mobonusUrl);
                    sendBroadcast(intent);

                    ConfigWrapper.put(ConfigDefine.MOBONUS_URL, "");
                    ConfigWrapper.apply();
                }

                break;
            default:
                break;
        }
    }

    private boolean isHome() {
        if (mIsShow && TabViewManager.getInstance() != null && TabViewManager.getInstance().isCurrentHome()) {
            if (mSearchFrame != null && mSearchFrame.isShown()) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionsHelper.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onSearch(String searchText) {
        mWebViewClientDelegate.clearFind();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebViewClientDelegate.findAllAsync(searchText, new WebView.FindListener() {
                @Override
                public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                    if (isDoneCounting) {
                        mSearchPageController.setSearchResultCount(numberOfMatches == 0 ? 0 : (activeMatchOrdinal + 1), numberOfMatches);
                    }

                }
            });
        } else {
            mWebViewClientDelegate.findAllAsync(searchText, null);
        }
    }


    @Override
    public void onClickClose() {
        mAddressBarController.showWithoutAnim();
        mWebViewClientDelegate.clearFind();
    }

    @Override
    public void onNext() {
        if (mWebViewClientDelegate != null)
            mWebViewClientDelegate.findNext(true);
        hideIM();
    }

    @Override
    public void onPrev() {
        if (mWebViewClientDelegate != null)
            mWebViewClientDelegate.findNext(false);
        hideIM();
    }

    public void showLanChangedDialog() {
        final CommonDialog dialog = new CommonDialog(this, getString(R.string.tips),
                getString(R.string.dialog_lanchanged_tip));
        dialog.setBtnCancel(getString(R.string.cancel), new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setBtnOk(getString(R.string.dialog_lanchanged_restart), new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //重启APP
                Intent intent = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                PendingIntent restartIntent = PendingIntent.getActivity(
                        getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntent); // 1秒钟后重启应用
                System.exit(0);
            }
        });
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIntoImageBrowseEvent(IntoImageBrowseEvent data) {
        if (data.isLoadMore() && !mImageBrowseView.isShown()) {
            return;
        }
        mImageBrowseView.setVisibility(View.VISIBLE);
        mImageBrowseView.showImages(data.getImgs());
    }

    private void setBlockTipEnable(boolean isEnable) {
        if(mWebViewClientDelegate instanceof WebViewClientImpl){
            ((WebViewClientImpl)mWebViewClientDelegate).setEnnableBlockTips(isEnable);
        }
	}
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFbNotifyMsgEvent(FbNotifyMsgEvent data) {
        if (data == null) {
            return;
        }
        String fbNotifyType = data.getFbNotifyType();
        if (TextUtils.isEmpty(fbNotifyType)) {
            return;
        }
        FbNotifyManager.getInstance().sendFbNotifiationPushStatistics(fbNotifyType);
        String fbNotifyMsgUrl = data.getUrl();
        if (TextUtils.isEmpty(fbNotifyMsgUrl)) {
            return;
        }
        if (ConfigManager.getInstance().isAppFg()) {
            if (isHome()) {
                try {
                    FbNotifyManager.getInstance().showFbNotifyMeg(this,true,fbNotifyType,fbNotifyMsgUrl);
                } catch (Exception e) {
                }
            } else {
                String contentUrl = TabViewManager.getInstance().getCurrentTabView().getContentUrl();
                if (!TextUtils.isEmpty(contentUrl) && contentUrl.contains(FbNotifyManager.FB_URL_PREFIX)) {
                    if (TabViewManager.getInstance().getCurrentTabView().isCurrentWebviewTop()) {
                        mFbNotifyMsgTip.setVisibility(View.GONE);
                    } else {
                        mFbNotifyMsgTip.setVisibility(View.VISIBLE);
                        Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_PAGETIP, GoogleConfigDefine.FB_NOTIFY_PAGETIP_SHOW);
                        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                mFbNotifyMsgTip.setVisibility(View.GONE);
                            }

                        }, FbNotifyManager.DISMISS_DURATION);
                    }
                } else {
                    try {
                        FbNotifyManager.getInstance().showFbNotifyMeg(this,true,fbNotifyType,fbNotifyMsgUrl);
                    } catch (Exception e) {
                    }
                }
            }

       } else {
            try {
                FbNotifyManager.getInstance().showFbNotifyMeg(this,false,fbNotifyType,fbNotifyMsgUrl);
            } catch (Exception e) {
            }
       }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMenuEvent(BottomMenuNotifyBrowserEvent event) {
        switch (event.getMenu()) {
            case Menu.ADD_BOOKMARK:
                if(TabViewManager.getInstance().isCurrentHome())return;
                handleAddFavClick();
                break;
            case Menu.BOOKMARK_HISTORY:
                mToolbarMenuOperation.openHistory();
                break;
            case Menu.DOWNLOAD_MANAGE:
                mToolbarMenuOperation.openDownload();
                break;
            case Menu.SAVE_WEB_PAGE:
                String url = TabViewManager.getInstance().getCurrentTabView()
                        .getContentOrigUrl();
                String title = TabViewManager.getInstance().getCurrentTabView()
                        .getContentTitle();
                SavedPageUtil.savePage(this, title, url);
                mToolTopMoreMenu.setVisibility(View.GONE);
                CropStorageUtil.recycle();
                Statistics.sendOnceStatistics(GoogleConfigDefine.ADDRESS_TOOLBAR_MENU, GoogleConfigDefine.FAVORITE_TYPE_SAVE_PAGE);
                break;
            case Menu.PC_MODE:
                mToolbarMenuOperation.switchWebMode();
                break;
            case Menu.NO_IMAGE:
                mToolbarMenuOperation.switchImgMode();
                break;
            case Menu.SETTING:
                mToolbarMenuOperation.openSettings();
                break;
            case Menu.EXIT:
                mToolbarMenuOperation.exitBrowser();
                break;
            case Menu.SCREEN_SHOT:
                mToolTopMoreMenu.setVisibility(View.GONE);
                mToolbarMenuView.setVisibility(View.GONE);
                mToolbarMenuView.hide();
                CropStorageUtil.recycle();
                Bitmap bitmap = ViewUtils.takeScreenShot(this, true, true);
                CropStorageUtil.setBitmap(bitmap);
                Intent intent = new Intent(this, CropEditActivity.class);
                startActivity(intent);
                Statistics.sendOnceStatistics(GoogleConfigDefine.ADDRESS_TOOLBAR_MENU, GoogleConfigDefine.FAVORITE_TYPE_EDIT_IMG);
                break;
            case Menu.FIND_IN_PAGE:
                mToolTopMoreMenu.setVisibility(View.GONE);
                mAddressBarController.hideWithoutAnim();
                mSearchPageController.showWithoutAnim();
                mSearchPageController.setNeedShow(true);
                Statistics.sendOnceStatistics(GoogleConfigDefine.FIND_IN_PAGE,GoogleConfigDefine.MENU_FIND_IN_PAGE_CLICK);
                break;
            case Menu.SHARE:
                mToolbarMenuOperation.openShare();
                break;
            case Menu.NIGHT_MODE:
                mToolbarMenuOperation.switchNightMode();
                break;
            case Menu.FULL_SCREEN:
                boolean isEnable = ConfigManager.getInstance().isFullScreen();
                ConfigManager.getInstance().setEnableFullScreen(!isEnable);

                break;
            case Menu.CLEAN_DATA:
                startActivity(new Intent(this, ClearDataActivity.class));
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            case Menu.OPEN_SYSTEM_NEWS:
                mToolbarMenuOperation.openSystemNewsListActivity();
                break;
            case Menu.AD_BLOCK:
                mToolbarMenuOperation.switchAdBlockMode();
                break;
            case Menu.FONT_SIZE:
                if(TabViewManager.getInstance().isCurrentHome())return;
                FontSizeSettingDialog dialog = new FontSizeSettingDialog(BrowserActivity.this);
                dialog.show();
                break;
            default:
                break;
        }
        mToolbarMenuView.hide();

    }

}