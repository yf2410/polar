package com.polar.browser.homepage.sitelist.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.polar.browser.JuziApp;
import com.polar.browser.homepage.customlogo.IComplete;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.push.api.IAllSiteListCallback;
import com.polar.browser.push.api.SiteInfoAsyncApi;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.SiteList;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.bean.db.SiteListVersion;
import com.polar.browser.vclibrary.bean.events.SyncHomeSiteEvent;
import com.polar.browser.vclibrary.common.CommonCallback;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SiteListVersionApi;
import com.polar.browser.vclibrary.network.ResultCallback;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by James on 2016/9/20.
 * 网站图标列表展示页
 */

public class SiteListView extends ListView {
    public static final String TAG = "SiteListView";
    protected List<Site> sites = new ArrayList<>();

    protected IComplete mComplete;
    protected SiteAdapter mAdapter;

    public SiteListView(Context context) {
        super(context, null);

    }

    public SiteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 用户点击网址列表添加按钮，刷新当前列表数据
     *
     * @author FKQ
     * @time 2016/11/3 17:23
     */
    public void refreshSiteItemHide(final int position) {
        mAdapter.remove(position);
    }

    public void refreshData(final int type) {

        ListAdapter adapter = getAdapter();
        if (adapter instanceof SiteAdapter) {
            mAdapter = (SiteAdapter) adapter;
        }

        SiteInfoAsyncApi.getInstance().queryAllSiteAsync(type, new IAllSiteListCallback() {
            @Override
            public void notifyQueryResult(List<Site> siteList) {

                ThreadManager.postTaskToIOHandler(new Runnable() {
                    @Override
                    public void run() {
                        SiteManager.getInstance().loadSiteListFromLocal(type, new CommonCallback<List<Site>>() {

                            @Override
                            public void onSuccess(final List<Site> sites) throws Exception {
                                ThreadManager.postTaskToUIHandler(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.updateData(sites);
                                        SiteListView.this.complete();
                                    }
                                });

                            }

                            @Override
                            public void onError(Exception e) {
                                /**
                                 * 发生错误时清空本地该版本数据,下次加载时会走网络
                                 * 不建议在此处走网络请求,可能造成死循环
                                 */
                            }
                        });
                    }
                });

                try {
                    SiteListVersion siteListVersion = SiteListVersionApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).query(type);
                    if (siteListVersion != null) {
                        final String siteListVersionVersion = siteListVersion.getVersion();
                        Api.getInstance().siteList(siteListVersionVersion,type).enqueue(new ResultCallback<SiteList>() {
                            @Override
                            public void success(SiteList data, Call<Result<SiteList>> call, Response<Result<SiteList>> response) throws Exception {
                                String serverSiteListVersion = data.getSiteListVersion();
                                if (!Util.equals(serverSiteListVersion, siteListVersionVersion)){
                                    List<Site> serverSites = data.getSiteList();
                                    if (!Util.isCollectionEmpty(serverSites)) {
                                        List<Site> filterServerSites = new ArrayList<>();
                                        for (Site serverSite : serverSites) {
                                            try {
                                                if (!SiteManager.getInstance().exist(serverSite)) {
                                                    filterServerSites.add(serverSite);
                                                }
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        mAdapter.updateData(filterServerSites);

                                        SiteListVersion siteListVersion = new SiteListVersion();
                                        siteListVersion.setType(type);
                                        siteListVersion.setVersion(serverSiteListVersion);
                                        SiteManager.getInstance().saveSiteList2Local(type, siteListVersion, data, new CommonCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) throws Exception {
                                                //请求到数据以后, 数据库同步
                                                SiteManager.getInstance().syncHomeSitesFromSiteInfo();
                                                EventBus.getDefault().post(new SyncHomeSiteEvent());
                                            }

                                            @Override
                                            public void onError(Exception e) {

                                            }
                                        });
                                        SiteListView.this.complete();
                                    } else {
                                        SiteListView.this.error();
                                    }
                                }
                            }

                            @Override
                            public void error(Call<Result<SiteList>> call, Throwable t) {

                            }
                        });
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void listIsNull() {
                Api.getInstance().siteList("", type).enqueue(new ResultCallback<SiteList>() {
                    @Override
                    public void success(SiteList data, Call<Result<SiteList>> call, Response<Result<SiteList>> response) throws Exception {
                        String serverSiteListVersion = data.getSiteListVersion();
                        List<Site> serverSites = data.getSiteList();
                        if (!ListUtils.isEmpty(serverSites)) {
                            List<Site> filterServerSites = new ArrayList<>();
                            for (Site serverSite : serverSites) {
                                try {
                                    if (!SiteManager.getInstance().exist(serverSite)) {
                                        filterServerSites.add(serverSite);
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                            mAdapter.updateData(filterServerSites);

                            SiteListVersion siteListVersion = new SiteListVersion();
                            siteListVersion.setType(type);
                            siteListVersion.setVersion(serverSiteListVersion);
                            SiteManager.getInstance().saveSiteList2Local(type, siteListVersion, data, new CommonCallback<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) throws Exception {
                                    //请求到数据以后, 数据库同步
                                    SiteManager.getInstance().syncHomeSitesFromSiteInfo();
                                    EventBus.getDefault().post(new SyncHomeSiteEvent());
                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                            SiteListView.this.complete();
                        } else {
                            SiteListView.this.error();
                        }

                    }

                    @Override
                    public void error(Call<Result<SiteList>> call, Throwable t) {

                    }
                });
            }

            @Override
            public void error(Exception e) {

            }
        });


    }
    private void error() {
        if (mComplete != null) {
            mComplete.error();
        }
    }

    private void complete() {
        if (mComplete != null) {
            mComplete.complete();
        }
    }
}
