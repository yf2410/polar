package com.polar.browser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.UserManager;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ViewConfiguration;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.polar.browser.adjust.AdjustLifecycleCallbacks;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.crashhandler.CrashHandler;
import com.polar.browser.crashhandler.CrashUploadTask;
import com.polar.browser.download.DownloadService;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.db.DownloadProvider;
import com.polar.browser.download_refactor.netstatus_manager.NetStatusManager;
import com.polar.browser.download_refactor.ui.DownloadDataPersistence;
import com.polar.browser.download_refactor.ui.DownloadNotify;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.env.AppEnv;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.Callback;
import com.polar.browser.jni.NativeManager;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.loginassistant.LoginAssistantManager;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCBroadcastReceiver;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.push.topic.TopicManager;
import com.polar.browser.shortcut.ParseConfig;
import com.polar.browser.update.UpdateUtils;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.DnsUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.ReflectionHook;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.UncompressPrefs;
import com.polar.browser.vclibrary.bean.NormalSwitchBean;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.bean.events.GoBrowserActivityEvent;
import com.polar.browser.vclibrary.bean.login.UserAccountData;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.network.NetworkManager;
import com.polar.browser.vclibrary.network.ResultCallback;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.network.api.ApiConstants;
import com.polar.browser.vclibrary.util.AdapterConvertor;
import com.polar.business.ad_business.AdManager;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import retrofit2.Call;
import retrofit2.Response;

public class JuziApp extends MultiDexApplication {

    private static final String TAG = "JuziApp";

    private static final String ADBLOCK_PROCESS_POSTFIX = ":AdBlockService";
    private static final String DOWNLOAD_PROCESS_POSTFIX = ":DownloadService";
    private static final String CRASH_PROCESS_POSTFIX = "crashhandler";
    private static final String SERVICE_PROCESS_POSTFIX = ":service";
    private static JuziApp sInstance;
    private static UserAccountData mUserAccountData;  //用户数据
    private ConfigManager mConfigManager;
    public boolean mIsFirstRun = true;
    private boolean mIsApkUpdated = false;
    private Tracker mTracker;
    public boolean isInitialized = false;
    //activity计数，处理前后台
    public int count = 0;

    public static JuziApp getInstance() {
        return sInstance;
    }
    public static final Context getAppContext() {
        return sInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        if (AppEnv.ENABLE_STRICT_MODE) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog().build());
        }
        super.onCreate();

        sInstance = this;
        initApp();

        registerActivityLifecycle();

    }

    /**
     * 分割 Dex 支持
     * @param base
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (count == 0) {
                    ConfigManager.getInstance().setForeground(true);
                }
                count++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                count--;
                if (count == 0) {
                    ConfigManager.getInstance().setForeground(false);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }


    //解决api 22 以上 android内部内存泄漏的
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initInternalUserMannager(){
        try {
            final Method m = UserManager.class.getDeclaredMethod("get", Context.class);
            m.setAccessible(true);
            m.invoke(null, this);

            //above is reflection for below...
            //UserManager.get();
        } catch (Throwable e) {
            if(BuildConfig.DEBUG) {
//                throw new RuntimeException(e);
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initApp() {
        initInternalUserMannager();
        initLeakCanary();
        ThreadManager.init();
        ConfigWrapper.init();
        UncompressPrefs.initialize(JuziApp.getAppContext()); // 记录已解压文件数据
        final String sRuntimeProcessName = SysUtils.getCurrentProcessName();

        ThreadManager.postTaskToLogicHandler(new Runnable() {
            @Override
            public void run() {

                if (sRuntimeProcessName.equals(getApplicationInfo().packageName)) {
                    initMainProcess();
                }else if (sRuntimeProcessName.contains(ADBLOCK_PROCESS_POSTFIX)) {
                    // 加载native库
                    NativeManager.loadLibrary();
                    ConfigWrapper.initializeServicePref(JuziApp.getAppContext());
                    CrashHandler crashHandler = CrashHandler.getInstance();
                    crashHandler.init(JuziApp.getAppContext());
                } else if (sRuntimeProcessName.contains(CRASH_PROCESS_POSTFIX)) {
                    mConfigManager = ConfigManager.getInstance();
                } else if (sRuntimeProcessName.contains(SERVICE_PROCESS_POSTFIX)) {
                    SimpleLog.d("AutoRun", "Receiver");
                    // 初始化线程管理器
                    ThreadManager.init();
                    ConfigWrapper.initializeServicePref(JuziApp.getAppContext());
                    mConfigManager = ConfigManager.getInstance();
                }
            }
        });

        // 保存图片时，下载进程需要提前初始化好
        if (sRuntimeProcessName.contains(DOWNLOAD_PROCESS_POSTFIX)) {
            // 初始化线程管理器
            ConfigWrapper.init();
            NetworkManager networkManager = new NetworkManager(JuziApp.getInstance());
            networkManager.init(JuziApp.getAppContext(), ApiConstants.SERVER_API_ADDRESS);
            Api.getInstance().init(networkManager);
            if (AppEnv.DEBUG) {
                CrashHandler crashHandler = CrashHandler.getInstance();
                crashHandler.init(JuziApp.getInstance());
            }
            initScreenSize();
            VCStoragerManager vcStoragerManager = VCStoragerManager.getInstance();
            vcStoragerManager.init();

            DownloadManager.getInstance().init(JuziApp.getAppContext());
            PathResolver.initialize();
            NetStatusManager.getInstance().init(JuziApp.getAppContext(), com.polar.browser.download_refactor.util.ThreadManager.getHandler(com.polar.browser.download_refactor.util.ThreadManager.THREAD_UI));
            NetStatusManager.getInstance().addObserver(DownloadManager.getInstance());
            DownloadManager.getInstance().addObserver(new DownloadNotify());
            DownloadManager.getInstance().addObserver(new DownloadDataPersistence());
            initReciever();
        }

    }

    private void initScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        AppEnv.SCREEN_WIDTH = dm.widthPixels;
        AppEnv.SCREEN_HEIGHT = dm.heightPixels;
        SimpleLog.e(TAG, "AppEnv.SCREEN_WIDTH == " + AppEnv.SCREEN_WIDTH);
    }

    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }


    public boolean isApkUpdated() {
        return mIsApkUpdated;
    }


    @Override
    public void onTerminate() {
        ThreadManager.destroy();
        super.onTerminate();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.analytics);
        }
        return mTracker;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public boolean isFirstRun() {
        return mIsFirstRun;
    }


    /**
     * 接收广播(只需要在下载进程中初始化) 1.下载数据清除后，刷新列表数据 2.夜间模式 改变 3.竖屏锁定 改变 4.全屏模式 改变
     * 5.自定义下载的路径 改变
     */
    private void initReciever() {
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TextUtils.equals(action,
                        CommonData.ACTION_NIGHT_MODE_CHANGED)) {
                    // 夜间模式 改变
                    SimpleLog.e("APP", "ACTION_NIGHT_MODE_CHANGED");
                    boolean isEnabled = intent.getBooleanExtra(
                            ConfigDefine.ENABLE_NIGHT_MODE, false);
                    ConfigManager.getInstance().setEnableNightModeAsync(
                            isEnabled);
                } else if (TextUtils.equals(action,
                        CommonData.ACTION_SCREEN_LOCKED)) {
                    // 竖屏锁定 改变
                    SimpleLog.e("APP", "ACTION_SCREEN_LOCKED");
                    boolean isEnabled = intent.getBooleanExtra(
                            ConfigDefine.ENABLE_SCREEN_LOCK, false);
                    ConfigManager.getInstance().setEnableScreenLock(isEnabled);
                } else if (TextUtils.equals(action,
                        CommonData.ACTION_FULL_SCREEN_CHANGED)) {
                    // 全屏模式 改变
                    SimpleLog.e("APP", "ACTION_FULL_SCREEN_CHANGED");
                    boolean isEnabled = intent.getBooleanExtra(
                            ConfigDefine.ENABLE_FULL_SCREEN, false);
                    ConfigManager.getInstance().setEnableFullScreen(isEnabled);
                } else if (TextUtils.equals(action,
                        CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED)) {
                    // 自定义了下载文件夹 改变
                    SimpleLog.e("APP", "ACTION_DOWNLOAD_FOLDER_CHANGED");
                    String downlaodFolderPath = intent
                            .getStringExtra(CommonData.KEY_DOWN_ROOT);
                    if (!TextUtils.isEmpty(downlaodFolderPath)) {
                        VCStoragerManager.getInstance().setDownloadDirPath(downlaodFolderPath);
                    }
                } else if (TextUtils.equals(action, CommonData.ACTION_DOWNLOAD_ONLY_WIFI)) {
                    // 仅wifi下载开关状态 改变
                    SimpleLog.e("APP", "ACTION_DOWNLOAD_ONLY_WIFI");
                    if (intent.hasExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD)) {
                        DownloadManager.getInstance().isOnlyWifiDownload = intent.getBooleanExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, true);
                    }
                }
            }
        };
        // 注册Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonData.ACTION_NIGHT_MODE_CHANGED);
        filter.addAction(CommonData.ACTION_SCREEN_LOCKED);
        filter.addAction(CommonData.ACTION_FULL_SCREEN_CHANGED);
        filter.addAction(CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED);
        filter.addAction(CommonData.ACTION_DOWNLOAD_ONLY_WIFI);
        registerReceiver(mReceiver, filter);
    }


    private void registerVCBroadcastReceiver() {
        // 注册Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Constants.ADJUST_LIFE_ACTION);
        registerReceiver(new VCBroadcastReceiver(), filter);
    }

    public void initMainProcess() {
        registerVCBroadcastReceiver();
        //下载数据库路径
        final String DB_TABLE_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/com.polar.browser/databases/download_db";
        //下载数据库老版本表名
        final String DB_TABLE_NAME = "table_download_info";

        VCStoragerManager.getInstance().init();
        JuziApp.getInstance().mIsFirstRun = mIsFirstRun = VCStoragerManager.getInstance().firstRunCheck();

        //初始化Facebook-SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(JuziApp.getInstance());

        initAdjustSdk();

        setMCCIfNeeded();

        NetworkManager networkManager = new NetworkManager(JuziApp.getInstance());
        networkManager.init(JuziApp.getAppContext(), ApiConstants.SERVER_API_ADDRESS);
        Api.getInstance().init(networkManager);

        // 加载native库
        NativeManager.loadLibrary();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(JuziApp.getAppContext());

        mConfigManager = ConfigManager.getInstance();
        mConfigManager.init();

        if (!JuziApp.getInstance().isFirstRun()) {
            mIsApkUpdated = apkUpdateCheck();
        }
        // patch,修复4.1.1-4.1.2版本解析url时的异常
        try {
            ReflectionHook.hookAccessibilityManager(JuziApp.getAppContext());
        } catch (Throwable t) {
            // TODO: 2016/5/30
        }
        mConfigManager.setLastRunVersion(SystemUtils.getVersionName(JuziApp.getAppContext()));
        mConfigManager.setLanChangedRestart(false);
        //初始化首页八大金刚图标
        initHomeSite();

        initTaskOnIoThread();
        //初始化比价插件数据
        initTaskOnNetworkThread();
        //升级重构下载数据库，检测旧版本下载表是否存在，需提前起下载进程做数据迁移操作
        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                if (JuziApp.getInstance().isApkUpdated()) {
                    boolean checkTableIsExist = FileUtils.checkTableIsExist(DB_TABLE_PATH, DB_TABLE_NAME);
                    if (checkTableIsExist) {
                        ThreadManager.postTaskToUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(JuziApp.getInstance(), DownloadService.class);
                                intent.putExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, ConfigManager.getInstance().isEnableOnlyWifiDownload());
                                JuziApp.getInstance().startService(intent);
                            }
                        });
                    }
                }
            }
        });

        initScreenSize();
        initAppEnvConfig();
        initDownloadFlag();
        AdManager.getInstance().initServiceAdSwitch();
        initSearchEngineConfig();
        UpdateUtils.checkAppUpdate();
        AccountLoginManager.getInstance().getShadowAccount();
        //判断用户是否已经登录，如果已经登录，获取最新的用户信息
        if(AccountLoginManager.getInstance().isUserLogined()){
            AccountLoginManager.getInstance().getUserData(ConfigManager.getInstance().getUserToken() , AccountLoginManager.APP_NAME);
        }
        initFirebase();

        if (JuziApp.getInstance().isFirstRun()) {
            SimpleLog.d(TAG, "第一次启动");
            Intent intent = new Intent(JuziApp.getInstance(), DownloadService.class);
            intent.putExtra("isFirstRun", JuziApp.getInstance().isFirstRun());
            JuziApp.getInstance().startService(intent);
        }

        // TODO: 2017/2/16 EventBus.getDefault().post(new GoBrowserActivityEvent());通知必须放初始化完成后
        RxBus.get().post(new GoBrowserActivityEvent(Constants.GOBROWSERTYPE_INIT,0));

        initDownloadResume();
    }

    /**
     * 恢复因为wifi断开而暂停的下载任务
     */
    private void initDownloadResume() {
        DownloadProvider.getInstance().init(this);
        DownloadProvider.getInstance().isNeedRecover(new Callback() {
            @Override
            public void callback(boolean flag) {
                if (flag) {
                    Intent intent = new Intent(JuziApp.getInstance(), DownloadService.class);
                    intent.putExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, ConfigManager.getInstance().isEnableOnlyWifiDownload());
                    JuziApp.getInstance().startService(intent);
                }
            }
        });
    }

    private void initAdjustSdk() {
        // TODO: 2017/4/5 polar 更换新的的apptoken
        String appToken = "u22riid21hc0";
        String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        // allow to send in the background
        config.setSendInBackground(true);
        Adjust.onCreate(config);
        JuziApp.getInstance().registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    private void setMCCIfNeeded() {
        String mcc = ConfigManager.getInstance().getMCC();
        if (!TextUtils.isEmpty(mcc)) {
            SystemUtils.setMCC(mcc);
        }
    }
    private void initSearchEngineConfig() {
        SearchUtils.initEngine(mConfigManager);
    }


    //下载页面，顶部title 右面是否显示godownload 图标
    private void initDownloadFlag() {
        Api.getInstance().goDownloadSwitch().enqueue(new ResultCallback<NormalSwitchBean>() {
            @Override
            public void success(NormalSwitchBean data, Call<Result<NormalSwitchBean>> call, Response<Result<NormalSwitchBean>> response) {
                if (response == null) return;
                Result<NormalSwitchBean> result = response.body();
                if (result == null || result.getData() == null) return;
                ConfigManager.getInstance().saveGoDownloadFlag(new Gson().toJson(result.getData()));
            }

            @Override
            public void error(Call<Result<NormalSwitchBean>> call, Throwable t) {

            }
        });

    }

    /**
     * 打开App设置一些全局配置
     */
    private void initAppEnvConfig() {
        ViewConfiguration vc = ViewConfiguration.get(this);
        AppEnv.MIN_SLIDING = vc.getScaledTouchSlop();
        AppEnv.MIN_SLIDE_BORDER = DensityUtil.dip2px(this, 20);
        AppEnv.MIN_SLIDE_OFF_SET = DensityUtil.dip2px(this, 100);
        AppEnv.sIsShownLogoGuide = ConfigManager.getInstance()
                .isShownlogoGuide();
        AppEnv.sIsSaveCardNewsnetState = ConfigManager.getInstance()
                .isSaveCardNewsNetState();
        AppEnv.sIsFullScreen = ConfigManager.getInstance().isFullScreen();
        AppEnv.sIsQuickSearch = ConfigManager.getInstance().isQuickSearch();
        AppEnv.sIsShownSlideGuide = ConfigManager.getInstance()
                .isShownSlideGuide();
        AppEnv.sIsShownEditLogoGuide = ConfigManager.getInstance()
                .isShownEditlogoGuide();
        AppEnv.sIsShownVideoDownloadTip = ConfigManager.getInstance()
                .isShownVideoDownloadTip();
        AppEnv.sIsShownUpdateApkTip = ConfigManager.getInstance()
                .isShownUpdateApkTip();
        String lan = SystemUtils.getLan();
        String lastRunLan = ConfigManager.getInstance().getLastRunLan();
        if (JuziApp.getInstance().isFirstRun()) {
            ConfigManager.getInstance().setLastRunLan(lan);
            // TODO: 2017/1/13 fix 升级下载按钮状态不变
            ConfigManager.getInstance().setEnableVedioDownload(false);
        }
        if (JuziApp.getInstance().isApkUpdated()) {
            if (TextUtils.equals("", lastRunLan)) {
                ConfigManager.getInstance().setLastRunLan(lan);
            }
        }
    }

    /**
     * 应用是否刚刚升级过
     *
     * @return
     */
    private boolean apkUpdateCheck() {
        String lastVersion = ConfigManager.getInstance().getLastRunVersion();
        if (SystemUtils.getVersionName(this).equals(lastVersion)) {
            return false;
        } else {
            return true;
        }
    }

    private void initHomeSite() {
        try {
            if (!ConfigManager.getInstance().isHomeSiteInited()) {
                SiteManager.getInstance().initHomeData(SystemUtils.getMCC(JuziApp.getAppContext()), SystemUtils.getArea(), SystemUtils.getLan());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 一些需要在IO线程上执行的初始化任务
     */
    private void initTaskOnIoThread() {
        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                initData();
            }
        });
    }

    private void initData() {
        // 初始化收藏
        BookmarkManager.getInstance().init();
        // 初始化快捷方式数据
        initShortcutData();
        // 初始化历史记录
//        HistoryManager.getInstance().init(this);
        // genCityList();
        // 初始化预置列表
        initPresetList();
        // 初始化LoginAssistant
        LoginAssistantManager.getInstance().init(this);
        // 更新 history_often 数据
//        updateOftenDB();
    }

    private void initShortcutData() {
        File file = new File(ParseConfig.sFolderPath + File.separator
                + ParseConfig.SHORTCUT_CONFIG_FILE_PATH);
        if (file.exists()) {
            ParseConfig.parseData();
        }
    }

    /**
     * 初始化预置网址列表
     */
    private void initPresetList() {
        File file = null;
        String lanFromMCC = AdapterConvertor.getLanFromMCC(SystemUtils.getMCC(this));
        String presetListName = FileUtils.getPresetListName(lanFromMCC);
        SimpleLog.d(TAG, "presetListName==" + presetListName);
        file = new File(this.getFilesDir() + File.separator + presetListName);
        if (!file.exists()) {
            try {
                FileUtils.copyAssetsFile(this, presetListName, file);
            } catch (IOException e) {
                SimpleLog.e(e);
            }
        }
        if (file.exists()) {
            byte[] data = FileUtils.readFile(file);
            String strData = new String(data);
            DnsUtils.preloadPresetDns(strData);
            NativeManager.initNativeQueryData(NativeManager.NATIVE_QUERY_DATA_TYPE_Preset, strData);
        }
    }

    private void initFirebase() {
        //topic
        TopicManager.getInstance().subscribeTopics(SystemUtils.getMCC(getApplicationContext()),SystemUtils.getLan());
        //id
        if (AppEnv.DEBUG) {
            String token = FirebaseInstanceId.getInstance().getToken();
            SimpleLog.d(TAG, "firebase token: " + token);
        }
    }

    /**
     * 一些需要在网络线程上执行的初始化任务
     */
    private void initTaskOnNetworkThread() {
        ThreadManager.postDelayedTaskToNetworkHandler(new Runnable() {
            @Override
            public void run() {
                if (!NetWorkUtils.isWifiConnected(JuziApp.getAppContext())) {
                    return;
                }
                CrashUploadTask uploadTask = new CrashUploadTask(null, null, JuziApp.getAppContext(), true);
                String path = Environment.getExternalStorageDirectory().getPath();
                uploadTask.execute(path + CrashHandler.JAVA_CRASH_DIR, path + CrashHandler.ANR_DIR);
            }
        }, 3000);
    }

    /**
     * 获取用户信息
     * @return
     */
    public static UserAccountData getUserAccountData() {
        if (mUserAccountData == null) {
            mUserAccountData = ConfigManager.getInstance().getUserInfo();
        }
        return mUserAccountData;
    }

    /**
     * 更新用户信息
     * @param userInfo
     */
    public static void updateUserAccountData(UserAccountData userInfo) {
        if (userInfo != null) {
            mUserAccountData = userInfo;
            ConfigManager.getInstance().setUserInfo(userInfo);
        }
    }

    /**
     * 清除用户数据
     *
     */
    public static void clearUserAccountData() {
        mUserAccountData = null;
        ConfigManager.getInstance().setUserInfo(null);
        ConfigManager.getInstance().setUserToken(null);
    }

    /**
     * 更新
     */
    public void updataUserData(){}

}
