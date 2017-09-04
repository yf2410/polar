package com.polar.browser.vclibrary.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.support.ConnectionSource;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.vclibrary.bean.Site;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.SiteInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by James on 2016/9/22.
 */

public class SiteInfoApi {


    private static SiteInfoApi instance;


    private Dao<SiteInfo, Long> dao;

    private SiteInfoApi(CustomOpenHelper customOpenHelper) throws SQLException{
        this.dao = customOpenHelper.getDao(SiteInfo.class);
    }

    public static SiteInfoApi getInstance(CustomOpenHelper customOpenHelper) throws SQLException{
        if (instance == null) {
            synchronized (SiteInfoApi.class) {
                if (instance == null) {
                    instance = new SiteInfoApi(customOpenHelper);
                }
            }
        }
        return instance;
    }

//    public void init(Dao<SiteInfo, Long> dao) {
//        this.dao = dao;
//    }


    public void insert(List<Site> siteList, int type) throws SQLException {
        for (Site site : siteList) {
            insert(new SiteInfo(site, type));
        }
    }

    public SiteInfo insert(SiteInfo siteInfo) throws SQLException {
        return dao.createIfNotExists(siteInfo);
    }

    public void insert(List<SiteInfo> siteInfoList) throws SQLException {
        for (SiteInfo siteInfo : siteInfoList) {
            insert(siteInfo);
        }
    }

    /**
     * 查询某一类型数据,按照id顺序
     *
     * @param type
     * @return
     * @throws SQLException
     */
    public List<SiteInfo> queryAllSiteInfo(int type) throws SQLException {
        return dao.queryBuilder().where().eq(SiteInfo.TYPE, type).query();
    }

    /**
     * 查询某一类型数据,按照id顺序
     *
     * @param type
     * @return
     * @throws SQLException
     */
    public List<Site> queryAllSite(int type) throws SQLException {
        List<SiteInfo> siteInfos = queryAllSiteInfo(type);
        List<Site> sites = new ArrayList<>();
        if (!ListUtils.isEmpty(siteInfos)) {
            for (int i = 0; i < siteInfos.size(); i++) {
                SiteInfo siteInfo = siteInfos.get(i);
                sites.add(new Site(siteInfo.getSiteId(), siteInfo.getSiteName(), siteInfo.getSiteAddr(), siteInfo.getSitePic()));
            }
        }
        return sites;
    }

    /**
     * 删除某一类型数据
     *
     * @param type
     * @throws SQLException
     */
    public int delete(int type) throws SQLException {
        return dao.delete((PreparedDelete<SiteInfo>) dao.deleteBuilder().where().eq(SiteInfo.TYPE, type).prepare());
    }

    public void deleteOldInsertNew(final List<Site> sites, final int type) throws SQLException {
        transaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                delete(type);
                insert(sites, type);
                return null;
            }
        });
    }

    public <T> void transaction(Callable<T> callable) throws SQLException {
        ConnectionSource connectionSource = dao.getConnectionSource();
        TransactionManager.callInTransaction(connectionSource, callable);
    }

    /**
     * 查询siteId或者siteAddr一致的数据
     *
     * @param homeSite
     * @return
     * @throws SQLException
     */
    public SiteInfo query(HomeSite homeSite) throws SQLException {
        return dao.queryBuilder().where().eq(SiteInfo.SITE_ID, homeSite.getSiteId()).or().eq(SiteInfo.SITE_ADDR, homeSite.getSiteAddr()).queryForFirst();
    }

    public int clear() throws SQLException {
        return dao.delete(dao.queryForAll());
    }
//    private SiteInfo querySiteInfoFromSiteAddr(String siteAddr) throws SQLException {
//        return dao.queryBuilder().where().eq(SiteInfo.SITE_ADDR, siteAddr).queryForFirst();
//    }
//
//    private SiteInfo querySiteInfoFromSiteId(String siteId) throws SQLException {
//        return dao.queryBuilder().where().eq(SiteInfo.SITE_ID, siteId).queryForFirst();
//    }
}
