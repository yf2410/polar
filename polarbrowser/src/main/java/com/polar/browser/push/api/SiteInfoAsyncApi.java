package com.polar.browser.push.api;

import com.polar.browser.JuziApp;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.db.SiteInfo;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SiteInfoApi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by FKQ on 2016/11/10.
 */

public class SiteInfoAsyncApi {
    
    private static SiteInfoAsyncApi mInstance;
    private SiteInfoAsyncApi(){
    
    }
    public static SiteInfoAsyncApi getInstance() {
        if (mInstance == null) {
            synchronized (SiteInfoAsyncApi.class) {
                if (mInstance == null) {
                    mInstance = new SiteInfoAsyncApi();
                }
            }
        }
        return mInstance;
    }

    /**
     * 查询某一类型数据,按照id顺序
     *
     * @param type
     * @return
     * @throws SQLException
     */
    public void queryAllSiteAsync(final int type, final IAllSiteListCallback callback) {

        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                try {
                    List<SiteInfo> siteInfos = SiteInfoApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).queryAllSiteInfo(type);
                    List<Site> sites = new ArrayList<>();
                    if (!ListUtils.isEmpty(siteInfos)) {
                        for (int i = 0; i < siteInfos.size(); i++) {
                            SiteInfo siteInfo = siteInfos.get(i);
                            sites.add(new Site(siteInfo.getSiteId(), siteInfo.getSiteName(), siteInfo.getSiteAddr(), siteInfo.getSitePic()));
                        }
                    }
                    if (ListUtils.isEmpty(sites)) {
                        if (callback != null) {
                            callback.listIsNull();
                        }
                    } else {
                        if (callback != null) {
                            callback.notifyQueryResult(sites);
                        }
                    }
                } catch (SQLException e) {
                    if (callback != null) {
                        callback.error(e);
                    }
                }
            }
        });
    }
}
