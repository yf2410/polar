package com.polar.browser.vclibrary.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.support.ConnectionSource;
import com.polar.browser.vclibrary.common.CommonCallback;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.SiteInfo;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by James on 2016/9/22.
 */

public class HomeSiteApi {
    private static HomeSiteApi instance;
    private Dao<HomeSite, Long> dao;

    private HomeSiteApi(CustomOpenHelper customOpenHelper) throws SQLException{
        this.dao = customOpenHelper.getDao(HomeSite.class);
    }

    public static HomeSiteApi getInstance(CustomOpenHelper customOpenHelper) throws SQLException{
        if (instance == null) {
            synchronized (HomeSiteApi.class) {
                if (instance == null) {
                    instance = new HomeSiteApi(customOpenHelper);
                }
            }
        }
        return instance;
    }

//    public void init(Dao<HomeSite, Long> dao) {
//        this.dao = dao;
//    }

    public HomeSite insert(HomeSite homeSite) throws SQLException {
        return dao.createIfNotExists(homeSite);
    }


    public int deleteAll(int type) throws SQLException {
        return dao.delete((PreparedDelete<HomeSite>) dao.deleteBuilder().where().eq(SiteInfo.TYPE, type).prepare());
    }

    public long count() throws SQLException {
        return dao.countOf();
    }

    /**
     * 按照order顺序排序
     *
     * @return
     * @throws SQLException
     */
    public List<HomeSite> queryAllOrderByOrder() throws SQLException {
        return dao.queryBuilder().orderBy(HomeSite.ORDER, true).query();
    }

    /**
     * 更新数据
     *
     * @param homeSite
     * @return
     * @throws SQLException
     */
    public int update(HomeSite homeSite) throws SQLException {
        return dao.update(homeSite);
    }

    /**
     * @param callable
     * @throws SQLException
     */
    public <T> void transaction(Callable<T> callable) throws SQLException {
        ConnectionSource connectionSource = dao.getConnectionSource();
        TransactionManager.callInTransaction(connectionSource, callable);
    }

    public int delete(HomeSite homeSite) throws SQLException {
        String siteId = homeSite.getSiteId();
        if (siteId != null) {
            return deleteBySiteId(siteId);
        } else {
            return deleteBySiteAddr(homeSite.getSiteAddr());
        }
    }

    public int deleteBySiteId(String siteId) throws SQLException {
        return dao.delete((PreparedDelete<HomeSite>) dao.deleteBuilder().where().eq((HomeSite.SITE_ID), siteId).prepare());
    }

    public int deleteBySiteAddr(String siteAddr) throws SQLException {
        return dao.delete(((PreparedDelete<HomeSite>) dao.deleteBuilder().where().eq(HomeSite.SITE_ADDR, siteAddr).prepare()));
    }

    public <T> void changePosition(int dragPostion, int dropPostion, final CommonCallback<T> callback) throws SQLException {


//        sqlite 里between 包括边界
        final List<HomeSite> homeSites;
        if (dragPostion < dropPostion) {
            homeSites = queryOrderBetween(dragPostion, dropPostion);
            transaction(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    updateHomeSiteOrder(homeSites.get(0), homeSites.get(homeSites.size() - 1).getOrder());
                    for (int i = 1; i < homeSites.size(); i++) {
                        HomeSite homeSite = homeSites.get(i);
                        updateHomeSiteOrder(homeSite, homeSite.getOrder() - 1);
                    }
                    callback.onSuccess(null);
                    return null;
                }
            });
        } else {
            homeSites = queryOrderBetween(dropPostion, dragPostion);
            transaction(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    updateHomeSiteOrder(homeSites.get(homeSites.size() - 1), homeSites.get(0).getOrder());
                    for (int i = 0; i < homeSites.size() - 1; i++) {
                        HomeSite homeSite = homeSites.get(i);
                        updateHomeSiteOrder(homeSite, homeSite.getOrder() + 1);
                    }
                    callback.onSuccess(null);
                    return null;
                }
            });
        }
    }


    public int clear() throws SQLException {
        List<HomeSite> homeSites = dao.queryForAll();
        return dao.delete(homeSites);
    }

    public List<HomeSite> queryCustom() throws SQLException {
        return dao.queryBuilder().orderBy(HomeSite.ORDER, true).where().eq(HomeSite.IS_CUSTOM, true).query();
    }

    public int clearAllButCustom() throws SQLException {
        List<HomeSite> homeSites = dao.queryBuilder().where().eq(HomeSite.IS_CUSTOM, false).query();
        return dao.delete(homeSites);
    }


    public void updateHomeSiteOrder(HomeSite homeSite, long order) throws SQLException {
        homeSite.setOrder(order);
        update(homeSite);
    }


    public HomeSite queryBySiteId(String siteId) throws SQLException {
        return dao.queryBuilder().where().eq(HomeSite.SITE_ID, siteId).queryForFirst();
    }

    public HomeSite queryBySiteName(String siteName) throws SQLException {
        return dao.queryBuilder().where().eq(HomeSite.SITE_NAME, siteName).queryForFirst();
    }

    public HomeSite queryBySiteAddr(String siteAddr) throws SQLException {
        return dao.queryBuilder().where().eq(HomeSite.SITE_ADDR, siteAddr).queryForFirst();
    }

    public List<HomeSite> queryOrderBetween(long start, long end) throws SQLException {
        return dao.queryBuilder().orderBy(HomeSite.ORDER, true).where().between(HomeSite.ORDER, start, end).query();
    }

    public List<HomeSite> queryOrderGreaterThan(long order) throws SQLException {
        return dao.queryBuilder().orderBy(HomeSite.ORDER, true).where().gt(HomeSite.ORDER, order).query();
    }

    public void insert(List<HomeSite> homeSites) throws SQLException {
        for (HomeSite homeSite : homeSites) {
            dao.createIfNotExists(homeSite);
        }
    }
}
