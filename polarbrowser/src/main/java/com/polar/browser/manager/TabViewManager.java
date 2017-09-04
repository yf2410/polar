package com.polar.browser.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.ConfigData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.IAddressBar;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.i.IEditLogo;
import com.polar.browser.i.IFullScreenDelegate;
import com.polar.browser.i.IJsCallbackDelegate;
import com.polar.browser.i.IProgressStart;
import com.polar.browser.i.IScrollChanged;
import com.polar.browser.i.ISearchFrame;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.i.ITabChangedObserver;
import com.polar.browser.impl.JsCallbackImpl;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.tabview.ContentFrame;
import com.polar.browser.tabview.ContentView.IReceivedTitleCallback;
import com.polar.browser.tabview.HomeFrame;
import com.polar.browser.tabview.TabView;
import com.polar.browser.utils.CookieUtil;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.view.ToolbarBottomController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TabViewManager {

    /**
     * 需求：为了点击home键不新建页面，前进后退正常，需要构造一个虚假的本地home页来实现
     */
    public static final String HOME_URL = "file:///android_asset/html/home.html";
    public static final int MAX_TAB_SIZE = 15;
    public static final String BLANK_TAB_URL = "about:blank";
    // TabView内部使用的字体缩放比例，默认为TEXT_SIZE_NORMAL，即100%比例显示，无缩放
    private static final int TEXT_SIZE_SMALL = 80;
    private static final int TEXT_SIZE_NORMAL = 100;
    private static final int TEXT_SIZE_BIG = 125;
    private static final int TEXT_SIZE_LARGE = 150;
    private static final String TAG = "TabViewManager";
    public static String sNightModeJs;
    public static String sDayModeJs;
    private static TabViewManager sInstance;
    // 自增id，每次增1
    private Integer mId = 0;

    private List<TabData> mTabViewList = new ArrayList<TabData>();

    private TabView mCurrentTabView;

    private TabViewCallbackManager mTabViewCallbackMgrRef;

    private IJsCallbackDelegate mJsDelegate;

    private Activity mActivity;

    private ConfigManager mConfigRef;
    private HomeFrame mHomeFrame;
    private ContentFrame mContentFrame;
    private ToolbarBottomController mToolbarController;
    private ISearchFrame mSearchFrame;
    private IAddressBar mAddressBarDelegate;
    private IScrollChanged mScrollChanged;
    private ISlideDelegate mSlideDelegate;
    private IFullScreenDelegate mFullScreenDelegate;
    private IEditLogo mEditLogoDelegate;
    private IProgressStart mProgressStart;
    private IReceivedTitleCallback mReceivedTitleCallback;
    private int mTextSize = TEXT_SIZE_NORMAL;
    private List<ITabChangedObserver> mTabChangedObserverList = new ArrayList<ITabChangedObserver>();
    private Animation mNewTabAnim;
    private Animation mNewTabAnimFromBottom; //从底部滑出
    private boolean isFirstLoadTab = true;
    private String NODE_TITLE = "title";
    private String NODE_URL = "url";
    private String NODE_IMAGE_PATH = "image";
    private String NODE_IS_CURRENT = "is_current";
    private String NODE_TABS = "tabs";
    private long mLastClickNavigateTime = 0;
    private IConfigObserver mConfigObserver = new IConfigObserver() {

        @Override
        public void notifyChanged(String key, int value) {
            // TODO Auto-generated method stub
        }

        @Override
        public void notifyChanged(String key, String value) {
            // TODO Auto-generated method stub
        }

        @Override
        public void notifyChanged(String key, boolean value) {
            if (key.equals(ConfigDefine.PRIVACY_MODE)) {
                if (!value) {
                    try {
                        CookieUtil.clearCookie(mActivity.getApplicationContext());
                        clearCache();
                        if (WebViewDatabase.getInstance(mActivity).hasFormData()) {
                            WebViewDatabase.getInstance(mActivity.getApplicationContext()).clearFormData();
                        }
                        if (WebViewDatabase.getInstance(mActivity).hasHttpAuthUsernamePassword()) {
                            WebViewDatabase.getInstance(mActivity.getApplicationContext()).clearHttpAuthUsernamePassword();
                        }
                        if (WebViewDatabase.getInstance(mActivity).hasUsernamePassword()) {
                            WebViewDatabase.getInstance(mActivity.getApplicationContext()).clearUsernamePassword();
                        }
                    } catch (Exception e) {
                        SimpleLog.e(e);
                    }
                }
                Iterator<TabData> it = mTabViewList.iterator();
                while (it.hasNext()) {
                    TabData data = it.next();
                    data.tabView.setSavePassword(!value);
                }
            }
        }
    };

    /**
     * 禁止直接创建
     */
    private TabViewManager() {
    }

    public static TabViewManager getInstance() {
        if (null == sInstance) {
            synchronized (TabViewManager.class) {
                if (null == sInstance) {
                    sInstance = new TabViewManager();
                }
            }
        }
        return sInstance;
    }

    private Runnable mSaveTabJsonTask;

    /**
     * 将标签列表转化为json，供外部调用 由于涉及到webview，必须在UI上调用
     */
    public void toJsonAsync() {
        if (mSaveTabJsonTask == null) {
            mSaveTabJsonTask = new Runnable() {

                @Override
                public void run() {
                    toJsonSync();
                }
            };
        }
        ThreadManager.getUIHandler().removeCallbacks(mSaveTabJsonTask);
        ThreadManager.postDelayedTaskToUIHandler(mSaveTabJsonTask, 50);
    }

    /**
     * 将tablist转化为json，供内部调用
     */
    private void toJsonSync() {
        JSONObject json = new JSONObject();
        try {
            json.put(NODE_TABS, tabsToJson());
            String tab = json.toString();
            SimpleLog.e(TAG, "toJson:" + tab);
            ConfigManager.getInstance().setTabList(tab);
        } catch (JSONException e) {
            SimpleLog.e(e);
        }
    }

    /**
     * 从json中获取tablist
     */
    public void fromJson() {
        String tabList = ConfigManager.getInstance().getTabList();
        SimpleLog.d(TAG, "tabList:" + tabList);
        if (tabList == null || tabList.isEmpty() || ConfigManager.getInstance().isPrivacyMode() || !ConfigManager.getInstance().isSaveTab()) {
            addTabView(true);
            return;
        }
        try {
            JSONObject json = new JSONObject(tabList);
            if (json != null && json.has(NODE_TABS)) {
                JSONArray tabArray = json.getJSONArray(NODE_TABS);
                if (tabArray != null) {
                    SimpleLog.d(TAG, "JsonLength :" + String.valueOf(tabArray.length()));
                    int currentPage = 0;
                    for (int i = tabArray.length() - 1; i >= 0; --i) {
                        JSONObject info = tabArray.getJSONObject(i);
                        if (info.has(NODE_URL)) {
                            String url = info.getString(NODE_URL);
                            if (url != null && !url.isEmpty()) {
                                if (url.startsWith("/data")) {
                                    continue;
                                }
                                boolean isCurrent = false;
                                if (info.has(NODE_IS_CURRENT)) {
                                    isCurrent = info.getBoolean(NODE_IS_CURRENT);
                                    if (isCurrent) {
                                        currentPage = i;
                                    }
                                }
                                String subImagePathString = "";
                                if (info.has(NODE_IMAGE_PATH)) {
                                    subImagePathString = info.getString(NODE_IMAGE_PATH);
                                }
                                String title = "";
                                if (info.has(NODE_TITLE)) {
                                    title = info.getString(NODE_TITLE);
                                }
                                addTabView(title, url, subImagePathString);
                            } else {
                                addTabView(true);
                            }
                        }
                    }
                    if (mTabViewList == null || mTabViewList.size() == 0) {
                        addTabView(true);
                    }
                    switchTabByIndex(currentPage, false);
                }
            }
        } catch (JSONException e) {
            SimpleLog.e(e);
        }
    }

    /**
     * 将内存中的书签数据转化为JSON
     *
     * @return
     */
    private JSONArray tabsToJson() {
        JSONArray array = new JSONArray();
        synchronized (mTabViewList) {
            if (mTabViewList != null && mTabViewList.size() > 0) {
                for (TabData info : mTabViewList) {
                    JSONObject object = new JSONObject();
                    try {
                        String title = info.tabView.getTitle();
                        if (title == null) {
                            title = "";
                        }
                        object.put(NODE_TITLE, title);
                        object.put(NODE_URL, info.tabView.getUrl());
                        object.put(NODE_IMAGE_PATH, info.tabView.getSubImagePath());
                        if (mCurrentTabView.getId() == info.tabView.getId()) {
                            object.put(NODE_IS_CURRENT, true);
                        } else {
                            object.put(NODE_IS_CURRENT, false);
                        }
                        array.put(object);
                    } catch (JSONException e) {
                        SimpleLog.e(e);
                    }
                }
            }
        }
        return array;
    }

    public void switchTabByIndex(int index, boolean withAnim) {
        hideCurrentTab();
        TabView tabView = getTabViewByIndex(index);
        switchTabInner(tabView);
    }

    /**
     * 恢复标签时，需要先恢复title，url，非当前标签的不加载页面
     *
     * @param title
     * @param url
     */
    public void addTabView(String title, String url, String subImage) {
        TabView tabView = addTabView(false, true);
        url = UrlUtils.checkUrlIsContainsHttp(url);
        if (tabView != null) {
            // TODO 将title，url赋值给tabView
            tabView.initRestoreData(title, url);
            tabView.setSubImagePath(subImage);
        }
    }

    public void destroy() {
        /** 2014.12.11 存储tab的数据结构改动 by dpk **/
        if (mTabViewList != null) {
            Iterator<TabData> it = mTabViewList.iterator();
            while (it.hasNext()) {
                TabData data = it.next();
                data.tabView.destroy();
            }
            mTabViewList.clear();
        }
        if (mHomeFrame != null) {
            mHomeFrame.destroy();
        }
        unRegisterConfigObservable();
        mContentFrame = null;
        mHomeFrame = null;
        mActivity = null;
        mCurrentTabView = null;
        sInstance = null;
    }

    public void registerObserver(ITabChangedObserver observer) {
        mTabChangedObserverList.add(observer);
    }

    public void unregisterObserver(ITabChangedObserver observer) {
        mTabChangedObserverList.remove(observer);
    }

    private void notifyChanged(String url, String isYoutube) {
        for (ITabChangedObserver observer : mTabChangedObserverList) {
            observer.notifyTabChanged(url, isYoutube);
        }
    }

    private void initHomeView() {
        mHomeFrame.init(this, mSearchFrame, mSlideDelegate,
                mFullScreenDelegate, mEditLogoDelegate, mToolbarController);
    }

    private void initContentFrame() {
        mContentFrame = (ContentFrame) mActivity
                .findViewById(R.id.content_frame);
    }

    public HomeFrame getHomeView() {
        return mHomeFrame;
    }

    private void initNightModeJs() {
        if (TextUtils.isEmpty(sNightModeJs)) {
            byte[] js = FileUtils.readFileFromAssets(JuziApp.getAppContext(), "night_mode.js");
            sNightModeJs = new String(js);
        }
        if (TextUtils.isEmpty(sDayModeJs)) {
            byte[] js = FileUtils.readFileFromAssets(JuziApp.getAppContext(), "day_mode.js");
            sDayModeJs = new String(js);
        }
    }

    public void init(TabViewCallbackManager tabViewCallbackMgr,
                     ConfigManager config, Activity activity,
                     IAddressBar addressbarDelegate, IScrollChanged scrollChanged,
                     ISearchFrame searchFrame, ISlideDelegate slideDelegate,
                     IReceivedTitleCallback callback, IFullScreenDelegate fullScreenDelegate,
                     HomeFrame homeFrame, IEditLogo editLogoDelegate, IProgressStart progressStart,
                     ToolbarBottomController toolbarController) {
        initNightModeJs();
        mJsDelegate = new JsCallbackImpl();
        mTabViewCallbackMgrRef = tabViewCallbackMgr;
        mTabViewCallbackMgrRef.registerJsCallBack(mJsDelegate);
        mConfigRef = config;
        mActivity = activity;
        mAddressBarDelegate = addressbarDelegate;
        mScrollChanged = scrollChanged;
        mSearchFrame = searchFrame;
        mSlideDelegate = slideDelegate;
        mReceivedTitleCallback = callback;
        mFullScreenDelegate = fullScreenDelegate;
        mEditLogoDelegate = editLogoDelegate;
        mHomeFrame = homeFrame;
        mToolbarController = toolbarController;
        mProgressStart = progressStart;
        mNewTabAnim = AnimationUtils.loadAnimation(mActivity, R.anim.new_tab);
        mNewTabAnimFromBottom = AnimationUtils.loadAnimation(mActivity,R.anim.new_tab_from_bottom);
        mConfigRef.registerObserver(mConfigObserver);
    }

    public void postInit() {
        // 初始化HomeView
        initHomeView();
        // 初始化ContentFrame
        initContentFrame();
        setFontSize(ConfigManager.getInstance().getFontSize());
    }

    public List<TabData> getTabViewList() {
        return mTabViewList;
    }


    public void addTabView(String url, boolean isHome) {
        addTabView(url, isHome, true, Constants.NAVIGATESOURCE_NORMAL);
    }

    public void addTabView(String url, boolean isHome, String subImagePath) {
        addTabView(url, isHome, true, Constants.NAVIGATESOURCE_NORMAL, subImagePath);
    }

    public void addTabView(String url, boolean isHome, boolean isForeground) {
        addTabView(url, isHome, isForeground, Constants.NAVIGATESOURCE_NORMAL);
    }

    public void addTabView(String url, boolean isHome, boolean isForeground,
                           int navigateSource, String subImage) {
        TabView tabView = addTabView(isHome, isForeground);
        if (tabView != null) {
            tabView.loadUrl(url, navigateSource);
            notifyChanged(url, null);
            tabView.setSubImagePath(subImage);
        }
    }

    public void addTabView(String url, boolean isHome, boolean isForeground,
                           int navigateSource) {
        TabView tabView = addTabView(isHome, isForeground);
        if (tabView != null) {
            tabView.loadUrl(url, navigateSource);
            notifyChanged(url, null);
        }
    }

    public void addTabView(String url, boolean isHome, int navigateSource) {
        TabView tabView = addTabView(isHome, true);
        url = UrlUtils.checkUrlIsContainsHttp(url);
        if (tabView != null) {
            tabView.loadUrl(url, navigateSource);
            notifyChanged(url, null);
        }
    }

    private int getCurrentTabViewPos() {
        for (int i = 0; i < mTabViewList.size(); ++i) {
            TabData data = mTabViewList.get(i);
            if (data.tabId == getCurrentTabId()) {
                return i;
            }
        }
        return -1;
    }

    public TabView addTabView(boolean isHome) {
        return addTabView(isHome, true);
    }

    public TabView addTabView(boolean isHome, boolean isForeground) {
        return addTabView(isHome,isForeground,true);
    }

    public TabView addTabView(boolean isHome, boolean isForeground, boolean isFromBottomMenu) {
        if (mTabViewList.size() == MAX_TAB_SIZE) {
            CustomToastUtils.getInstance().showTextToast(R.string.toast_tab_limited);
            return null;
        }
        SimpleLog.d(TAG, "addTabView()");
        ++mId;
        ConfigData config = new ConfigData(mConfigRef.isEnableImg(), mConfigRef.isEnableNightMode());
        TabView tabView = new TabView(this, mTabViewCallbackMgrRef, mActivity,
                config, isHome, mId, mSearchFrame, mSlideDelegate,
                mReceivedTitleCallback, mFullScreenDelegate, mProgressStart);
        tabView.setFromBottomMenu(isFromBottomMenu);
        int pos = getCurrentTabViewPos();
        TabData data = new TabData(mId, tabView);
        if (pos == -1) {
            mTabViewList.add(data);
        } else {
            mTabViewList.add(pos, data);
        }
        if (isForeground) {
            hideCurrentTab();
            showTab(tabView,isFromBottomMenu);
            notifyChanged(mCurrentTabView.getTitle(), null);
            mAddressBarDelegate.notifyProgressTabSwitched(
                    mCurrentTabView.getProgress(), mCurrentTabView.isShowHome(),
                    mCurrentTabView.getId());
        } else {
            CustomToastUtils.getInstance().showTextToast(R.string.toast_opened_background);
        }
        tabView.setFontSize(mTextSize);
        return tabView;
    }


    /**
     * 隐藏掉home url
     */
    public void hideHomeUrl() {
        mCurrentTabView.goHome();
        hideContentShowHome();
        mAddressBarDelegate.notifyProgressInvisible();
        notifyChanged(mCurrentTabView.getTitle(), null);
    }

    /**
     * 点击Home键的操作
     */
    public void goHome() {
        if (!mCurrentTabView.isShowHome()) {
            mCurrentTabView.loadUrl(HOME_URL, Constants.NAVIGATESOURCE_NORMAL);
            TabViewManager.getInstance().hideHomeUrl();
        }
    }

    public void hideContentShowHome() {
        setContentVisible(View.GONE);
        setHomeVisible(View.VISIBLE);
//        scrollToTop();
    }

    public void showContentHideHome() {
        setContentVisible(View.VISIBLE);
        setHomeVisible(View.GONE);
    }

    public TabView getCurrentTabView() {
        return mCurrentTabView;
    }

    /**
     * 根据id删除tabview
     *
     * @param id
     */
    public void removeTabViewById(Integer id) {
        int index = getIndexByKey(id);
        if (index == -1) {
            // 未找到要删除的(也许已经删除了)
            return;
        }
        TabData data = mTabViewList.get(index);
        TabView tabView = data.tabView;
        if (tabView != null) {
            // 要删除的是当前的tab
            if (tabView.equals(mCurrentTabView)) {
                if (mTabViewList.size() == 1) {
                    mTabViewList.remove(index);
                    addTabView(true);
                    tabView.destroy();
                    return;
                } else {
                    mTabViewList.remove(index);
                    if (index >= 0) {
                        switchTabByIndex(index == 0 ? index : index - 1);
                        tabView.destroy();
                    }
                }
            } else {
                mTabViewList.remove(index);
                tabView.destroy();
            }
            notifyChanged(mCurrentTabView.getTitle(), null);
        }
    }

    /**
     * 根据index删除tabview
     *
     * @param index
     */
    public void removeTabViewByIndex(int index) {
        if (index >= mTabViewList.size() || index < 0) {
            return;
        }
        TabView tabView = mTabViewList.get(index).tabView;
        // 要删除的是当前的tab
        if (tabView.equals(mCurrentTabView)) {
            if (mTabViewList.size() == 1) {
                mTabViewList.remove(index);
                addTabView(true);
                tabView.destroy();
                return;
            } else {
                mTabViewList.remove(index);
                switchTabByIndex(index == 0 ? index : index - 1);
                tabView.destroy();
            }
        } else {
            mTabViewList.remove(index);
            tabView.destroy();
        }
        notifyChanged(mCurrentTabView.getTitle(), null);
    }

    /**
     * 删除所有标签
     */
    public void removeAllTabViews() {
        Iterator<TabData> it = mTabViewList.iterator();
        while (it.hasNext()) {
            it.next().tabView.destroy();
        }
        mTabViewList.clear();
        mCurrentTabView = null;
    }

    public TabView getTabViewByKey(Integer id) {
        int index = getIndexByKey(id);
        if (index >= mTabViewList.size() || index < 0) {
            return null;
        } else {
            return mTabViewList.get(index).tabView;
        }
    }

    /**
     * 根据key值找到index
     *
     * @param index
     * @return
     */
    public TabView getTabViewByIndex(int index) {
        if (index >= mTabViewList.size() || index < 0) {
            SimpleLog.d(TAG, "index > mTabViewMap.size()");
            if (mTabViewList.size() == 0)
                return null;
            else
                return mTabViewList.get(0).tabView;
        }
        return mTabViewList.get(index).tabView;
    }

    private int getIndexByKey(Integer key) {
        for (int i = 0; i < mTabViewList.size(); ++i) {
            if (key == mTabViewList.get(i).tabId) {
                return i;
            }
        }
        return -1;
    }
    // private Integer getKeyByIndex(int index) {
    // return mTabViewList.get(index).tabId;
    // }

    public int getSize() {
        return mTabViewList.size();
    }

    public int getFontSize() {
        return mTextSize;
    }

    /**
     * 设置字体，供外部调用，size的参数需要为config中定义的大、中、小三种
     *
     * @param configDefineSize
     */
    public void setFontSize(int configDefineSize) {
        switch (configDefineSize) {
            case ConfigDefine.FONT_SIZE_LARGE:
                mTextSize = TEXT_SIZE_LARGE;
                break;
            case ConfigDefine.FONT_SIZE_BIG:
                mTextSize = TEXT_SIZE_BIG;
                break;
            case ConfigDefine.FONT_SIZE_MID:
                mTextSize = TEXT_SIZE_NORMAL;
                break;
            case ConfigDefine.FONT_SIZE_MIN:
                mTextSize = TEXT_SIZE_SMALL;
                break;
            default:
                break;
        }
        Iterator<TabData> it = mTabViewList.iterator();
        while (it.hasNext()) {
            TabData data = it.next();
            data.tabView.setFontSize(mTextSize);
        }
    }

    public void setLoadsImages(boolean isEnabled) {
        Iterator<TabData> it = mTabViewList.iterator();
        while (it.hasNext()) {
            TabData data = it.next();
            data.tabView.setLoadsImages(isEnabled);
        }
    }

    public void enableNightMode(boolean isEnabled) {
        Iterator<TabData> it = mTabViewList.iterator();
        while (it.hasNext()) {
            TabData data = it.next();
            if (isEnabled) {
                data.tabView.enableNightMode(isEnabled, sNightModeJs);
            } else {
                data.tabView.enableNightMode(isEnabled, sDayModeJs);
            }
        }
    }

    public void switchTabByIndex(int index) {
        hideCurrentTab();
        TabView tabView = getTabViewByIndex(index);
        switchTabInner(tabView);
    }

    public void switchTabByKey(Integer key) {
        hideCurrentTab();
        TabView tabView = getTabViewByKey(key);
        if (tabView != null) {
            switchTabInner(tabView);
        }
    }

    private void switchTabInner(TabView tabView) {
        showTab(tabView,false);
        notifyChanged(mCurrentTabView.getTitle(), mCurrentTabView.getUrl());
        mAddressBarDelegate.notifyProgressTabSwitched(
                mCurrentTabView.getProgress(), mCurrentTabView.isShowHome(),
                mCurrentTabView.getId());
        if (tabView.isFirstRestoredTab()) {
            tabView.loadInitialUrl();
        }
    }

    private void hideCurrentTab() {
        if (mCurrentTabView != null) {
            mCurrentTabView.setVisibility(false);
            mCurrentTabView.pause();
        }
        mContentFrame.removeAllView();
    }

    private void showTab(TabView tabView,boolean isFromBottomMenu) {
        mCurrentTabView = tabView;
        mCurrentTabView.setVisibility(true);
        mCurrentTabView.resume();
        mContentFrame.addContentView(mCurrentTabView.getContentView());
        if (isFirstLoadTab) {
            isFirstLoadTab = false;
            return;
        }
        if (!SysUtils.isLowLevelDevice(mActivity)) {
            Animation anim = isFromBottomMenu ? mNewTabAnimFromBottom : mNewTabAnim;
            if (mCurrentTabView.isShowHome()) {
                mHomeFrame.getView().startAnimation(anim);
            } else {
                mContentFrame.startAnimation(anim);
            }
        }
    }

    // 重置当前的内容webview
    public void resetCurrentTab(int tabId) {
        mContentFrame.removeAllView();
        int index = getIndexByKey(tabId);
        if (index >= 0 && index < mTabViewList.size()) {
            mCurrentTabView = mTabViewList.get(index).tabView;
            mContentFrame.addContentView(mCurrentTabView.getContentView());
        }
    }

    public void setContentVisible(int type) {
        mContentFrame.setVisibility(type);
    }

    public void setHomeVisible(int type) {
        mHomeFrame.setVisibility(type);
    }

    public void loadUrl(String url, int src) {
        loadUrl(url, null, src, null, false);
    }

    public void loadUrl(String url, String data, boolean isSaved) {
        loadUrl(url, data, Constants.NAVIGATESOURCE_OTHER, null, true);
    }

    public void loadUrl(String url, String data, int src, Map<String, String> headers, boolean isSaved) {
        if (TextUtils.isEmpty(url) || mCurrentTabView == null) {
            return;
        }
        if (!isSaved) {
            url = UrlUtils.checkUrlIsContainsHttp(url);
            if (mCurrentTabView.isShowHome() && !mCurrentTabView.canGoBack()) {
                ConfigData config = new ConfigData(ConfigManager.getInstance().isEnableImg(),
                        ConfigManager.getInstance().isEnableNightMode());
                mCurrentTabView.reset(config);
                mCurrentTabView.setFontSize(mTextSize);
            }
            if (headers != null) {
                mCurrentTabView.loadUrl(url, src, headers);
            } else {
                mCurrentTabView.loadUrl(url, src);
            }

            //添加比价插件支持网站入口访问量统计
            if (url.contains("amazon.in")) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.SITE_VISIT, "amazon.in");
            } else if (url.contains("snapdeal.com")) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.SITE_VISIT, "snapdeal.com");
            } else if (url.contains("flipkart.com")) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.SITE_VISIT, "flipkart.com");
            }
        } else {
            mCurrentTabView.loadDataWithBaseURL(null, data, "application/x-webarchive-xml", "UTF-8", url);
        }
        mCurrentTabView.showContent();
        notifyChanged(url, null);
    }

    // start-------------------测试html与java交互
    // 4.2之后必须加上，不然不允许调用 详见
    // http://blog.csdn.net/zgjxwl/article/details/9627685
    @SuppressLint("SetJavaScriptEnabled")
    @JavascriptInterface
    public void jsShowContent(final String url) {
        SimpleLog.d(TAG, "Js: jsShowContent()");
        if (mCurrentTabView == null)
            return;
        mCurrentTabView.setFontSize(mTextSize);
        mJsDelegate.showContent(mCurrentTabView, url);
        notifyChanged(url, null);
    }

    public boolean isCurrentHome() {
        if (mCurrentTabView == null) {
            return true;
        }
        return mCurrentTabView.isShowHome();
    }

    public String getCurrentUrl() {
        return mCurrentTabView.getUrl();
    }

    public String getCurrentTitle() {
        return mCurrentTabView.getTitle();
    }

    public void goBack() {
        if (!mCurrentTabView.canGoBack()) {
            if (!mCurrentTabView.isFromBottomMenu()) {
                removeTabViewById(mCurrentTabView.getId());
                return;
            }
            mCurrentTabView.goHome();
            hideContentShowHome();
            mAddressBarDelegate.notifyProgressInvisible();
        } else {
            mCurrentTabView.goBack();
        }
        notifyChanged(mCurrentTabView.getTitle(), null);
        mLastClickNavigateTime = System.currentTimeMillis();
    }

    public void goForward() {
        mCurrentTabView.goForward();
        notifyChanged(mCurrentTabView.getTitle(), null);
        mLastClickNavigateTime = System.currentTimeMillis();
    }

    public long getLastClickNavigateTime() {
        return mLastClickNavigateTime;
    }


    /**
     * //fix 有时候 mCurrentTabView = null TODO
     * 考虑跟BrowserActivity 使用TabViewManager 实例方式有关，之前是 初始化一次后 使用，现在改为 单例getInstance，
     * 需要验证是否解决了问题。
     *
     * @return
     */

    public int getCurrentTabId() {
        return mCurrentTabView.getId();
    }

    public boolean isWebViewInvalid(WebView webview) {
        Iterator<TabData> it = mTabViewList.iterator();
        while (it.hasNext()) {
            TabData data = it.next();
            if (data != null && data.tabView != null
                    && data.tabView.getContentView() != null
                    && data.tabView.getContentView().isSameWebView(webview)) {
                return data.tabView.getContentView().isInvalid();
            }
        }
        return false;
    }

    public IScrollChanged getScrollInterface() {
        return mScrollChanged;
    }

    public void clearAllSubImage() {
        try {
            if (mTabViewList != null && mTabViewList.size() > 0) {
                Iterator<TabData> it = mTabViewList.iterator();
                if (it.hasNext()) {
                    TabData data = it.next();
                    data.tabView.clearAllSubImages();
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * 清除webview缓存
     */
    public void clearCache() {
        try {
            if (mTabViewList != null && mTabViewList.size() > 0) {
                Iterator<TabData> it = mTabViewList.iterator();
                while (it.hasNext()) {
                    TabData data = it.next();
                    data.tabView.clearCache();
                }
            }
        } catch (Exception e) {
        }
//		String appCacheDir = mActivity.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
//
//		File file = new File(appCacheDir);
//		if (file.exists()) {
//			FileUtils.deleteFileOrDirectory(file);
//		}
//		mActivity.getApplicationContext().deleteDatabase("webview.db");
//		mActivity.getApplicationContext().deleteDatabase("webviewCache.db");
    }

    public void forceCaptureScreen() {
        if (mTabViewList != null && mTabViewList.size() > 0) {
            Iterator<TabData> it = mTabViewList.iterator();
            while (it.hasNext()) {
                TabData data = it.next();
                if (data.tabId != mCurrentTabView.getId()) {
                    data.tabView.resume();
                    data.tabView.forceCaptureScreenInNightMode();
//					data.tabView.pause();
                }
            }
        }
    }

    public void resumeCurrent() {
        if (mCurrentTabView != null) {
            mCurrentTabView.resume();
        }
    }

    public void pauseAll() {
        if (mTabViewList != null && mTabViewList.size() > 0) {
            Iterator<TabData> it = mTabViewList.iterator();
            while (it.hasNext()) {
                TabData data = it.next();
                data.tabView.pause();
            }
        }
    }

    public String getUa() {
        String ua = "";
        if (mCurrentTabView != null) {
            ua = mCurrentTabView.getUa();
        }
        return ua;
    }

    public void setUa(String ua) {
        Iterator<TabData> it = mTabViewList.iterator();
        while (it.hasNext()) {
            TabData data = it.next();
            data.tabView.setUa(ua);
        }
    }

    public class TabData {
        public Integer tabId;
        public TabView tabView;

        public TabData(Integer tabId, TabView tabView) {
            this.tabId = tabId;
            this.tabView = tabView;
        }
    }

    /***
     * 反注册ConfigObservable
     */
    private void unRegisterConfigObservable() {
        if (mConfigRef != null) {
            mConfigRef.unregisterObserver(mConfigObserver);
        }
    }
    /**主页ObservableScrollView 滚动到顶部*/
    public void scrollToTop() {
        mHomeFrame.scrollToTop();
    }

    /**
     * @param isFromBottomMenu 是否是从MultiWindowView中新建的标签
     * true 是
     * false 否*/
    public void addTabView(String url, boolean isHome, boolean isForeground, boolean isFromBottomMenu) {
        addTabView(url, isHome, isForeground, Constants.NAVIGATESOURCE_NORMAL, isFromBottomMenu);
    }

    public void addTabView(String url, boolean isHome, boolean isForeground,
                           int navigateSource, boolean isNormal) {
        TabView tabView = addTabView(isHome, isForeground, isNormal);
        if (tabView != null) {
            tabView.loadUrl(url, navigateSource);
            notifyChanged(url, null);
        }
    }



    /**获取系统回调onCloseWindow(WebView window)中window所在tab的id*/
    public Integer getCloseTabId(WebView window) {
        if (window == null) {
            return -1;
        }
        for (int i = 0; i < mTabViewList.size(); ++i) {
            View targetWindow = mTabViewList.get(i).tabView.getContentView().getWebView().getView();
            if (window.equals(targetWindow)) {
                return mTabViewList.get(i).tabView.getId();
            }
        }
        return -1;
    }
}
