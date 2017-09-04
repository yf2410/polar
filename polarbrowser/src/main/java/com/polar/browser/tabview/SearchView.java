package com.polar.browser.tabview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.homepage.HomeSiteUtil;
import com.polar.browser.homepage.customlogo.HomeLogoView;
import com.polar.browser.homepage.customlogo.HomeVisitedView;
import com.polar.browser.homepage.homecard.HomeWebCardController;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IEditLogo;
import com.polar.browser.i.IFullScreenDelegate;
import com.polar.browser.i.ISearchFrame;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.location_weather.AlxLocationManager;
import com.polar.browser.location_weather.ILocationCallBack;
import com.polar.browser.location_weather.SwitchLocationActivity;
import com.polar.browser.location_weather.WeatherLocationEvent;
import com.polar.browser.location_weather.WeatherLocationUtils;
import com.polar.browser.location_weather.WeatherManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.push.api.IAllSiteListCallback;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.LogOutEvent;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.PermissionsHelper;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.LastWeatherInfo;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.WeatherResult;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.events.LocationPerEvent;
import com.polar.browser.vclibrary.bean.events.SPConfigChangedEvent;
import com.polar.browser.vclibrary.bean.events.SyncDatabaseEvent;
import com.polar.browser.vclibrary.bean.events.SyncHomeSiteEvent;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SiteInfoApi;
import com.polar.browser.view.ObservableScrollView;
import com.polar.browser.view.ToolbarBottomController;
import com.polar.business.ad_business.AdManager;
import com.polar.business.zxing.CaptureActivity;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * 主页第一屏：搜索页面，使用原生android ui编写，不再使用html页面
 *
 * @author dpk
 */
public class SearchView implements OnClickListener, OnLongClickListener, ILocationCallBack {

    private static final int typeHomeSite = 0;

    private HomeWebCardController mHomeWebCardController;
    private ISearchFrame mSearchFrame;
    // TODO:需要控制后退按钮逻辑
    private ToolbarBottomController mToolbarController;
    private ObservableScrollView mScrollView;
    private View mView;
    private ISlideDelegate mSlideDelegate;
    private IFullScreenDelegate mFullScreenDelegate;
    private IEditLogo mEditLogoDelegate;
    private ViewGroup mRoot;
    private HomeLogoView mHomeLogoView;
    private HomeVisitedView mHomeVisitedView;
    private AppBarLayout mAppBarLayout;
    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;
    private ImageView mIvTopBg;
    private View mSearchBoxBg;
    private View mSearchBoxStandard;
    private View mSearchBox;
    private View mSearchBoxTxt;

    /**
     * scroll的onTouchListener
     **/
    private OnTouchListener mScrollTouchListener;
    /**
     * 可以下拉的内容
     **/
    private float mLastHeaderPadding;
    /**
     * 记录下拉点击位置
     **/
    private float lastY;
    /**
     * 记录按下位置 小火箭-_-
     **/
    private float downX;
    private float downY;
    private TextView mTvCity;
    private RelativeLayout mRlWeather;
    private ImageView mWeatherImg;
    private TextView mWeatherTemp;
    private View mQrIcon;
    private TextView mWeatherCondition;
    private LastWeatherInfo mLastWeatherInfo = new LastWeatherInfo();
    //是否初次加载天气
    private boolean isWeatherFirstLoad = false;
    private View mLocatioin;

    public SearchView(ViewGroup root) {
        mRoot = root;
        initView();
        EventBus.getDefault().register(this);
    }

    public View getView() {
        return mView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init(ToolbarBottomController toolbarController,
                     ISearchFrame searchFrame, ISlideDelegate slideDelegate,
                     IFullScreenDelegate fullScreenDelegate, IEditLogo editLogoDelegate) {
        mSearchFrame = searchFrame;
        mToolbarController = toolbarController;
        this.mSlideDelegate = slideDelegate;
        mFullScreenDelegate = fullScreenDelegate;
        mEditLogoDelegate = editLogoDelegate;
        if (mEditLogoDelegate != null) {
            mHomeLogoView.setIEditLogo(mEditLogoDelegate);
        }
        initListener();
        onPermitionGranted();
    }

    private void initView() {
        mView = mRoot.findViewById(R.id.home_frame);
        mScrollView = (ObservableScrollView) mRoot.findViewById(R.id.home_scroll_view);
        mHomeLogoView = (HomeLogoView) mRoot.findViewById(R.id.view_homepage_logo);
        mHomeVisitedView = (HomeVisitedView) mRoot.findViewById(R.id.view_homepage_visited);
        mAppBarLayout = (AppBarLayout) mRoot.findViewById(R.id.appbar);
        mIvTopBg = (ImageView) mRoot.findViewById(R.id.iv_top_bg);
        mSearchBoxBg = mRoot.findViewById(R.id.search_box_bg);
        mSearchBoxStandard = mRoot.findViewById(R.id.view_input_standard);
        mSearchBox = mRoot.findViewById(R.id.view_search_input_bg);
        mSearchBoxTxt = mRoot.findViewById(R.id.search_box_icon);
        mTvCity = (TextView) mRoot.findViewById(R.id.btn_switch_location);
        mRlWeather = (RelativeLayout) mRoot.findViewById(R.id.rl_weather);
        mWeatherImg = (ImageView) mRoot.findViewById(R.id.iv_weather_condition_img);
        mWeatherTemp = (TextView) mRoot.findViewById(R.id.tv_weather_tmp);
        mWeatherCondition = (TextView) mRoot.findViewById(R.id.tv_weather_condition);
        mQrIcon = mRoot.findViewById(R.id.group_search_box_btn_qrcode);

        initAdView();
        initHomeLogoArea();
        initHomeWebCard();
        initVisitedModule();
        initWeatherModule();
        if (!AlxLocationManager.getInstance().isGooglePlayServiceAvailable(JuziApp.getInstance().getApplicationContext())) {
            mRlWeather.setVisibility(View.GONE);
            mTvCity.setVisibility(View.GONE);
        }

        checkPermisionDelay(10);
    }

    private void initVisitedModule() {
        if (ConfigManager.getInstance().isHistoryVisitedEnabled()) {
            if (mHomeVisitedView != null) {
                mHomeVisitedView.init(mScrollView);
                initVisitedModuleData();
            }
        } else {
            if (mHomeVisitedView != null) {
                mHomeVisitedView.setVisibility(View.GONE);
            }
        }
    }

    private void initVisitedModuleData() {
        if (SiteManager.getInstance().getPreHistoryRecords() == null || SiteManager.getInstance().getPreHistoryRecords().size() == 0) {
            mHomeVisitedView.resetData(SiteManager.getInstance().getHistoryRecords());
        } else {
            if (mHomeVisitedView != null) {
                mHomeVisitedView.setVisibility(View.VISIBLE);
            }
        }

    }

    private void checkPermisionDelay(int delayTime) {
        Observable.timer(delayTime, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxFragmentActivity) getView().getContext()).<Long>bindToLifecycle())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.i("AlexLocation", "  check is granted location permision !");
                        if (!AlxLocationManager.getInstance().isLocationPerGranted()) {
                            processWeatherReqError();
                        }
                    }
                });
    }

    private void initWeatherModule() {
        if (!NetworkUtils.isAllNetWorkConnected(getView().getContext())) {
            setWeatherError();
            return;
        }
        rotateAnim(mWeatherImg, true);
        AlxLocationManager.getInstance().init(this);
        String lastJson = ConfigManager.getInstance().getLastWeatherJson();
        if (!TextUtils.isEmpty(lastJson)) {
            Gson gson = new Gson();
            mLastWeatherInfo = gson.fromJson(lastJson, LastWeatherInfo.class);
            if (mLastWeatherInfo != null) {
                Log.i("AlexLocation", "last weather city is :" + mLastWeatherInfo.getCity());
                mTvCity.setText(mLastWeatherInfo.getCity());
                loadWeather(mLastWeatherInfo.getLat(), mLastWeatherInfo.getLon());
            }else {
                Log.i("AlexLocation", "last weather city is  null");
                AlxLocationManager.pauseGPS();
                AlxLocationManager.onCreateGPS(JuziApp.getAppContext());
            }
        }else {
            Log.i("AlexLocation", "last weather city is  null");
            AlxLocationManager.pauseGPS();
            AlxLocationManager.onCreateGPS(JuziApp.getAppContext());
        }
    }

    private void initAdView() {
        if (!AdManager.getInstance().getApplicationTagBean().isAdLocationHome() && ConfigManager.getInstance().getFbHomeEngine()) {
            LinearLayout mNativeAdContainer = (LinearLayout) mRoot.findViewById(R.id.native_ad_container);
            AdManager.getInstance().loadHomePage(mNativeAdContainer);
            AdManager.getInstance().getApplicationTagBean().setAdLocationHome(true);
        }

    }

    private void initHomeLogoArea() {
        if (mHomeLogoView != null) {
            mHomeLogoView.init(mScrollView);
            if (mEditLogoDelegate != null) {
                mHomeLogoView.setIEditLogo(mEditLogoDelegate);
            }

            if (ConfigManager.getInstance().isCheckModifiedHomeSite()) {
                initHomeLogoData();
            } else {
                syncHomeLogoData();
            }
        }
    }

    private void syncHomeLogoData() {

        SiteManager.getInstance().loadHomeSiteFromLocad(typeHomeSite, new IAllSiteListCallback() {
            @Override
            public void notifyQueryResult(List<Site> siteList) {

                if (JuziApp.getInstance().isFirstRun() || JuziApp.getInstance().isApkUpdated()) {
                    initHomeLogoData();
                    return;
                }
                final ArrayList<HomeSite> allHomeSite = new ArrayList<>();
                int siteListSize = siteList.size();
                for (int i = 0; i < siteListSize; i++) {
                    Site site = siteList.get(i);
                    allHomeSite.add(new HomeSite(site,i,false));
                }
                allHomeSite.add(HomeSiteUtil.getMoreHomeSite(siteListSize));
                allHomeSite.add(HomeSiteUtil.getAddHomeSite(siteListSize+1));
                mHomeLogoView.resetData(allHomeSite);

                ThreadManager.postTaskToIOHandler(new Runnable() {
                    @Override
                    public void run() {
                        SiteManager.getInstance().saveHomeSiteList2Local(allHomeSite);
                        try {
                            SiteInfoApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).delete(typeHomeSite);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            public void listIsNull() {
                initHomeLogoData();
                if (!ConfigManager.getInstance().isCheckModifiedHomeSite()) {
                    SiteManager.getInstance().syncHomeSiteByService(typeHomeSite);
                }
            }

            @Override
            public void error(Exception e) {
                initHomeLogoData();
            }
        });
    }

    private void initHomeWebCard() {
        if (mHomeWebCardController == null) {
            mHomeWebCardController = new HomeWebCardController(mRoot);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLogOutEvent(LogOutEvent event) {
        SiteManager.getInstance().resetHomeSite();
        initHomeLogoData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncHomeSite(SyncHomeSiteEvent syncHomeSiteEvent) {
        SiteManager.getInstance().resetHomeSite();
        initHomeLogoData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherLocationEvent(WeatherLocationEvent event) {
        reLoadWeatherByLatLng(event.lat, event.lon);
        mTvCity.setText(event.city);
        saveLastInfo(event.country, event.city, event.lat, event.lon);
    }

    private void initHomeLogoData() {
        final List<HomeSite> localHomeSites = SiteManager.getInstance().getLocalHomeSites();
        mHomeLogoView.resetData(localHomeSites);
    }

    private void initListener() {
        mRoot.findViewById(R.id.search_box_bg).setOnClickListener(this);
        initScrollViewListeners();
        initOnOffsetChangedListener();
        mScrollView.setOnTouchListener(mScrollTouchListener);
        mAppBarLayout.addOnOffsetChangedListener(mOnOffsetChangedListener);
        mTvCity.setOnClickListener(this);
        mRlWeather.setOnClickListener(this);
        mQrIcon.setOnClickListener(this);
    }

    private int mStandardHeight;
    private int mSearchBoxHeight;

    private void initOnOffsetChangedListener() {
        mOnOffsetChangedListener = new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float alpha = (-verticalOffset) * 1.0f / (mIvTopBg.getHeight() - mSearchBoxBg.getHeight());
                mIvTopBg.setAlpha(1 - alpha);
                if (mStandardHeight == 0 || mSearchBoxHeight == 0) {
                    mStandardHeight = mSearchBoxStandard.getHeight();
                    mSearchBoxHeight = mSearchBox.getHeight();
                    return;
                }
                if (mStandardHeight == 0 || mSearchBoxHeight == 0) return;
                int height = (int) (mStandardHeight + (mSearchBoxHeight - mStandardHeight) * (1 - alpha));
                if (height != mSearchBoxTxt.getHeight()) {
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSearchBoxTxt.getLayoutParams();
                    layoutParams.height = height;
                    mSearchBoxTxt.setLayoutParams(layoutParams);
                }
                if (height != mSearchBox.getHeight()) {
                    ViewGroup.LayoutParams layoutParams2 = mSearchBox.getLayoutParams();
                    layoutParams2.height = height;
                    mSearchBox.setLayoutParams(layoutParams2);
                }
            }
        };
    }

    private void initScrollViewListeners() {
        mScrollTouchListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                if (mScrollView.getScrollY() <= 0) {
                    mScrollView.smoothScrollTo(0, 0);
                }
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_UP:
                        return handleActionUp(ev);
                    case MotionEvent.ACTION_DOWN:
                        return handleActionDown(ev);
                    case MotionEvent.ACTION_MOVE:
                        return handleActionMove(ev);
                }
                return checkIntercepte();
            }
        };
    }

    private boolean handleActionUp(MotionEvent ev) {
        downX = 0;
        downY = 0;
        mSlideDelegate.touchUp(ev.getX(), ev.getY());
        lastY = 0;
        return checkIntercepte();
    }

    private boolean checkIntercepte() {
        // 如果Header是完全被隐藏的则让ScrollView正常滑动，让事件继续否则的话就阻断事件
        if (mLastHeaderPadding > 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean handleActionDown(MotionEvent ev) {
        mFullScreenDelegate.check2showUI();
        downX = ev.getX();
        downY = ev.getY();
        mSlideDelegate.touchDown(downX, downY);
        return checkIntercepte();
    }

    private void handleLeftSliding(MotionEvent ev) {
        mSlideDelegate.leftSlide(Math.abs(ev.getX() - downX));
        MotionEvent cancelEvent = MotionEvent.obtain(ev);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
    }

    private void handleRightSliding(MotionEvent ev) {
        mSlideDelegate.rightSlide(Math.abs(ev.getX() - downX));
        MotionEvent cancelEvent = MotionEvent.obtain(ev);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
    }

    private boolean handleSliding(MotionEvent ev) {
        float deltaXf = ev.getX() - downX;
        float deltaYf = ev.getY() - downY;
        int type = ConfigManager.getInstance().getSlidingScreenMode();
        if (type == ConfigDefine.SLIDING_BACK_FORWARD_fullscreen) {
            if (deltaXf > 0 && Math.abs(deltaXf) > AppEnv.MIN_SLIDING && Math.abs(deltaXf) > Math.abs(deltaYf)) {
                if (TabViewManager.getInstance().getCurrentTabView() != null && TabViewManager.getInstance().getCurrentTabView().canGoBack()) {
                    handleLeftSliding(ev);
                    return true;
                }
            } else if (deltaXf < 0 && Math.abs(deltaXf) > AppEnv.MIN_SLIDING && Math.abs(deltaXf) > Math.abs(deltaYf)) {
                if (TabViewManager.getInstance().getCurrentTabView() != null && TabViewManager.getInstance().getCurrentTabView().canGoForward()) {
                    handleRightSliding(ev);
                    return true;
                }
            }
        } else if (type == ConfigDefine.SLIDING_BACK_FORWARD_border) {
            if (downX < AppEnv.MIN_SLIDE_BORDER && Math.abs(deltaXf) > AppEnv.MIN_SLIDING) {
                // 左侧
                if (TabViewManager.getInstance().getCurrentTabView() != null && TabViewManager.getInstance().getCurrentTabView().canGoBack()) {
                    handleLeftSliding(ev);
                    return true;
                }
            } else if (mScrollView.getWidth() - downX < AppEnv.MIN_SLIDE_BORDER && Math.abs(deltaXf) > AppEnv.MIN_SLIDING) {
                // 右侧
                if (TabViewManager.getInstance().getCurrentTabView() != null && TabViewManager.getInstance().getCurrentTabView().canGoForward()) {
                    handleRightSliding(ev);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleActionMove(MotionEvent ev) {
        if (downX == 0) {
            downX = ev.getX();
        }
        if (downY == 0) {
            downY = ev.getY();
        }
        if (lastY == 0) {
            lastY = ev.getY();
        }
        if (handleSliding(ev)) {
            return true;
        }
        return checkIntercepte();
    }


    /**
     * 搜索框是否已经在顶部，如果alpha值为1，认为已经在顶部
     *
     * @return
     */
    public boolean isSearchbarTop() {
        return true;
    }

    public int getSearchbarHeight() {
        return 0;
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.search_box_bg:
                showSearchFrame(false);
                break;
            case R.id.btn_switch_location:
                swtichLocation();
                break;
            case R.id.rl_weather:
                onWeatherDetail();
                break;
            case R.id.group_search_box_btn_qrcode:
                if (!PermissionsHelper.requestCameraPermission((Activity) mRoot.getContext())) {
                    // 二维码扫描
                    (mRoot.getContext()).startActivity(new Intent(mRoot.getContext(),
                            CaptureActivity.class));
                    ((Activity) mRoot.getContext()).overridePendingTransition(
                            R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.ENTER_QR_SCAN);
                }
                break;
            default:
                break;
        }
    }

    private void onPermitionGranted() {
        RxBus.get().safetySubscribe(LocationPerEvent.class, (RxFragmentActivity) getView().getContext())
                .subscribe(new Consumer<LocationPerEvent>() {
                    @Override
                    public void accept(LocationPerEvent locationPerEvent) throws Exception {
                        if (locationPerEvent.isGranted()) {
                            initWeatherModule();
                            SimpleLog.d("AlexLocation", "拿到權限， 開搞，permision  granted   request for weather ...");
                        }
                    }
                });
    }

    private void onWeatherDetail() {
        //TODO 统计进入天气详情页。
        SimpleLog.d("AlexLocation", "统计进入天气详情页");
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.WEATHER_DETAIL_CLICK);
        TabViewManager.getInstance().showContentHideHome();
        TabViewManager.getInstance().loadUrl(CommonData.WEATHER_DETAIL_URL, Constants.NAVIGATESOURCE_NORMAL);
    }

    private void swtichLocation() {
        //TODO 统计进入位置选择页。
        SimpleLog.d("AlexLocation", "统计进入位置选择页");
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.SWITCH_LOCATION_CLICK);
        Intent intent = new Intent(JuziApp.getInstance().getApplicationContext(), SwitchLocationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putSerializable("lastWeatherInfo", mLastWeatherInfo);
        intent.putExtras(bundle);
        JuziApp.getInstance().getApplicationContext().startActivity(intent);
    }

    /**
     * @param anim true,先滚动到顶部，然后出跳到搜索页<br>
     *             false,直接跳转到搜索页
     */
    private void showSearchFrame(boolean anim) {
        if (mSearchFrame != null) {
            mSearchFrame.show(CommonData.PAGE_EDIT_CLICK, "", mView);
        }
    }

    public void onConfigurationChanged() {
        // mSearchIcon.setX(x)
    }

    public void onOrientationChanged() {
    }


    private void rotateAnim(View view, boolean start) {
        Animation anim =new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        anim.setFillAfter(true); // 设置保持动画最后的状态
        anim.setDuration(3000); // 设置动画时间
        anim.setRepeatCount(-1);
        anim.setInterpolator(new AccelerateInterpolator()); // 设置插入器
        if (start) {
            view.startAnimation(anim);
        }else {
            view.clearAnimation();
        }
    }


    /**
     * 退出销毁
     */
    public void destroy() {
        EventBus.getDefault().unregister(this);

        if (mRoot != null) {
            mRoot = null;
        }

        if (mHomeWebCardController != null) {
            mHomeWebCardController.onDestroy();
            mHomeWebCardController = null;
        }
        if (mHomeLogoView != null) {
            mHomeLogoView = null;
        }
        if (mEditLogoDelegate != null) {
            mEditLogoDelegate = null;
        }
        if (mScrollView != null) {
            mScrollView = null;
        }
        // 移除监听，防止内存泄露
        if (mAppBarLayout != null) {
            mAppBarLayout.removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }
        AdManager.getInstance().destroy();
    }


    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public ObservableScrollView getScrollView() {
        return mScrollView;
    }

    @Override
    public void callback(double lat, double lng) {
        Log.i("AlexLocation", "callback     lat   is :"  + lat   + "  lng is :" + lng);
        //TODO 统计定位成功。
        SimpleLog.d("AlexLocation", "统计定位成功");
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.LOCATION_SUCC);
        loadWeather(lat, lng);
        AlxLocationManager.pauseGPS();
    }

    public void reLoadWeatherByLatLng(double lat, double lng) {
        loadWeather(lat, lng);
    }

    private void loadWeather(final double lat, final double lng) {
        WeatherManager.requestWeather(lat, lng)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    private WeatherResult.QueryBean.ResultsBean results;
                    private String temp;
                    private WeatherResult.QueryBean.ResultsBean.ChannelBean.LocationBean location;
                    private WeatherResult.QueryBean.ResultsBean.ChannelBean.ItemBean.ConditionBean condition;
                    private String text;
                    private String country;
                    private String weatherCode;
                    private String city;
                    @Override
                    public void accept(WeatherResult value) throws Exception {
                        results = value.getQuery().getResults();
                        if (results == null) {
                            processWeatherReqError();
                            return;
                        }
                        condition = results.getChannel().getItem().getCondition();
                        location = value.getQuery().getResults().getChannel().getLocation();
                        weatherCode = condition.getCode();
                        text = condition.getText();
                        country = value.getQuery().getResults().getChannel().getLocation().getCountry();
                        temp = condition.getTemp();
                        if(TextUtils.equals(country , "Belize") || TextUtils.equals(country , "Cayman Islands") || TextUtils.equals(country ,"United States")){
                            float fahrenheitTemp = WeatherLocationUtils.getFahrenheitByCentigrade(temp);
                            temp = fahrenheitTemp + " °F";
                        }else {
                            temp = temp + " °C";
                        }
                        city =location.getCity() ;
//                if (isWeatherFirstLoad && mLastWeatherInfo != null && TextUtils.equals(mLastWeatherInfo.getCity(), city)) {
//                    SimpleLog.d("AlexLocation", "current request city is  : " + city  + "    return;");
//                    return;
//                }
                        isWeatherFirstLoad = true;
                        saveLastInfo(country, city, lat, lng);
                        setWeather(weatherCode, text, country, city, temp);
                        mWeatherImg.clearAnimation();
                        //TODO 天气展示成功统计
                        Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.WEATHER_SHOW_SUCC);
                        SimpleLog.d("AlexLocation", "天气展示成功统计");
                        AlxLocationManager.getInstance().setWeatherGet(true);
                        SimpleLog.d("AlexLocation", "weather is : " + text  + "   , country is : " + country + " , weather code is : " + weatherCode + " , city is : " + city);
                    }
                });
    }

    private void setWeatherError() {
        rotateAnim(mWeatherImg, false);
        SimpleLog.d("AlexLocation", "天气无法访问");
        mWeatherImg.setImageResource(R.drawable.no_weather);
        mWeatherCondition.setText(R.string.no_weather);
        String city = JuziApp.getInstance().getString(R.string.location);
        if (!TextUtils.isEmpty(mLastWeatherInfo.getCity())) {
            city = mLastWeatherInfo.getCity();
        }
        mTvCity.setText(city);
        mWeatherTemp.setVisibility(View.INVISIBLE);
    }

    private void processWeatherReqError() {
        mWeatherImg.clearAnimation();
        //TODO 统计天气请求出错，定位成功，且有网，包括了权限被拒绝的情况
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.WEATHER_SHOW_FAILED);
        SimpleLog.d("AlexLocation", "统计天气请求出错，定位成功，且有网");
        mWeatherImg.setImageResource(R.drawable.no_weather);
        mWeatherCondition.setText(R.string.no_weather);
        String city = JuziApp.getInstance().getString(R.string.location);
        if (!TextUtils.isEmpty(mLastWeatherInfo.getCity())) {
            city = mLastWeatherInfo.getCity();
        }
        mTvCity.setText(city);
        mWeatherTemp.setVisibility(View.INVISIBLE);
    }

    private void saveLastInfo(String country, String city, double lat, double lng) {
        mLastWeatherInfo.setCountry(country);
        mLastWeatherInfo.setCity(city);
        mLastWeatherInfo.setLat(lat);
        mLastWeatherInfo.setLon(lng);
        Gson gson = new Gson();
        String lastJson = gson.toJson(mLastWeatherInfo);
        ConfigManager.getInstance().setLastWeatherJson(lastJson);
    }

    private void setWeather(String weatherCode, String text, String country, String city, String temp) {
        mTvCity.setVisibility(View.VISIBLE);
        mTvCity.setText(city);
        mWeatherTemp.setVisibility(View.VISIBLE);
        mWeatherTemp.setText(temp);
        mWeatherCondition.setText(WeatherLocationUtils.getWeatherByCode(weatherCode));
        int drawable = WeatherLocationUtils.getDrawableByCode(weatherCode);
        if(drawable == 0) {
            drawable = R.drawable.ww;
        }
        mWeatherImg.setImageResource(drawable);
    }

    @Override
    public void onFailed(int code, String msg) {
        //TODO   统计定位失败。
        SimpleLog.d("AlexLocation", "统计定位失败");
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEATHER_STA, GoogleConfigDefine.LOCATION_FAILED);
        mWeatherImg.clearAnimation();
        mWeatherImg.setImageResource(R.drawable.no_weather);
        mWeatherCondition.setText(R.string.no_weather);
        mWeatherTemp.setVisibility(View.INVISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSPConfigChanged(SPConfigChangedEvent spConfigChangedEvent) {
        String configDefineValue = spConfigChangedEvent.getConfigDefineValue();
        if (TextUtils.isEmpty(configDefineValue)) {
            return;
        }

        switch (configDefineValue) {
            case ConfigDefine.HISTORY_VISITED:
//                if (spConfigChangedEvent.isValue()) {
//                    if (mHomeVisitedView != null) {
//                        mHomeVisitedView.setVisibility(View.VISIBLE);
//                        initVisitedModuleData();
//                    }
//                } else {
//                    if (mHomeVisitedView != null) {
//                        mHomeVisitedView.setVisibility(View.GONE);
//                    }
//                }
                initVisitedModule();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncDatabaseEvent(SyncDatabaseEvent syncDatabaseEvent) {
        switch (syncDatabaseEvent.type) {
            case SyncDatabaseEvent.TYPE_VISITED_RECORD:
                if (mHomeVisitedView != null) {
                    mHomeVisitedView.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }
    }
}
