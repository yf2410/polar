package com.polar.browser.homepage.sitelist;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonTabViewPager;
import com.polar.browser.homepage.customlogo.IComplete;
import com.polar.browser.homepage.sitelist.recommand.RecommendView;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.SiteListType;
import com.polar.browser.vclibrary.bean.events.SyncSiteItemHideEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2016/9/19.
 * 网站图标列表页
 */
public class AddMoreSiteActivity extends LemonBaseActivity implements ViewPager.OnPageChangeListener {
    public static final String TAG = "AddMoreSiteActivity";
    private ViewGroup mClassifyLayout;
    private RecommendView mRecommendView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.CustomThemeActionBarOverlay);
        setContentView(R.layout.view_vp_custom_navigation);
        initRecomView();
        initClassifyView();
        initVPCustomNavi();
        EventBus.getDefault().register(this);
    }

    private void initRecomView() {
        mRecommendView = new RecommendView(this);
        mRecommendView.setComplete(new IComplete() {
            @Override
            public void complete() {
                SimpleLog.d(TAG, "customNaviView complete");
                ThreadManager.postTaskToUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        mRecommendView.hideEmptyView();
                    }
                });

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRecommendView.hideEmptyView();
//                    }
//                });
            }

            @Override
            public void error() {
                SimpleLog.d(TAG, "customNaviView error");
                ThreadManager.postTaskToUIHandler(new Runnable() {
                    @Override
                    public void run() {
                        if (!NetWorkUtils.isNetworkConnected(JuziApp.getAppContext())) {
                            mRecommendView.showEmptyView();
                        }
                    }
                });
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!NetWorkUtils.isNetworkConnected(JuziApp.getAppContext())) {
//                            mRecommendView.showEmptyView();
//                        }
//                    }
//                });
            }
        });
        mRecommendView.refreshData(SiteListType.TYPE_RECOMMAND);
    }

    /**
     * 当用户在推荐列表添加网址到首页，通知列表刷新（移除添加首页的item）
     *
     * @author FKQ
     * @time 2016/11/3 17:21
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncSiteItemHide(SyncSiteItemHideEvent syncSiteItemHideEvent) {
        int position = syncSiteItemHideEvent.getPosition();
        mRecommendView.refreshSiteItemHide(position);
    }

    private void initClassifyView() {
        mClassifyLayout = (ViewGroup)LayoutInflater.from(JuziApp.getAppContext()).inflate(R.layout.view_classify, null);
//        mClassifyLayout = (ViewGroup) inflate(JuziApp.getAppContext(), R.layout.view_classify, null);
    }

    private void initVPCustomNavi() {
        CommonTabViewPager commonTabViewPager = (CommonTabViewPager) findViewById(R.id.vp_custom_navi);
        commonTabViewPager.setStyle(CommonTabViewPager.STYLE_GREY);
        List<String> listStr = new ArrayList<String>();
        listStr.add(JuziApp.getAppContext().getString(R.string.url_recommendation));
        listStr.add(JuziApp.getAppContext().getString(R.string.url_classify));
        commonTabViewPager.setTitles(listStr);
        List<View> listPage = new ArrayList<View>();
        listPage.add(mRecommendView);
        listPage.add(mClassifyLayout);
        commonTabViewPager.setPageViews(listPage);
        commonTabViewPager.setOnPageChangedListener(this);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int currentPage) {
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
