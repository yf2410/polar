package com.polar.browser.vclibrary.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.support.ConnectionSource;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.UserHomeSite;
import com.polar.browser.vclibrary.common.CommonCallback;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by yangfan on 2017/5/23.
 */

public class UserHomeSiteApi {

    private static UserHomeSiteApi instance;
    private Dao<UserHomeSite, Long> dao;

    private UserHomeSiteApi(CustomOpenHelper customOpenHelper) throws SQLException {
        this.dao = customOpenHelper.getDao(UserHomeSite.class);
    }

    public static UserHomeSiteApi getInstance(CustomOpenHelper customOpenHelper) throws SQLException{
        if (instance == null) {
            synchronized (UserHomeSiteApi.class) {
                if (instance == null) {
                    instance = new UserHomeSiteApi(customOpenHelper);
                }
            }
        }
        return instance;
    }

    public UserHomeSite insert(UserHomeSite userHomeSite) throws SQLException {
        return dao.createIfNotExists(userHomeSite);
    }

    public int delete(UserHomeSite userHomeSite) throws SQLException {
        String siteId = userHomeSite.getSiteId();
        if (siteId != null) {
            return deleteBySiteId(siteId);
        } else {
            return deleteBySiteAddr(userHomeSite.getSiteAddr());
        }
    }

    public int deleteBySiteId(String siteId) throws SQLException {
        return dao.delete((PreparedDelete<UserHomeSite>) dao.deleteBuilder().where().eq((UserHomeSite.SITE_ID), siteId).prepare());
    }

    public int deleteBySiteAddr(String siteAddr) throws SQLException {
        return dao.delete(((PreparedDelete<UserHomeSite>) dao.deleteBuilder().where().eq(UserHomeSite.SITE_ADDR, siteAddr).prepare()));
    }

    public <T> void changePosition(int dragPostion, int dropPostion, final CommonCallback<T> callback) throws SQLException {


//        sqlite 里between 包括边界
        final List<UserHomeSite> homeSites;
        if (dragPostion < dropPostion) {
            homeSites = queryOrderBetween(dragPostion, dropPostion);
            transaction(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    updateHomeSiteOrder(homeSites.get(0), homeSites.get(homeSites.size() - 1).getOrder());
                    for (int i = 1; i < homeSites.size(); i++) {
                        UserHomeSite homeSite = homeSites.get(i);
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
                        UserHomeSite homeSite = homeSites.get(i);
                        updateHomeSiteOrder(homeSite, homeSite.getOrder() + 1);
                    }
                    callback.onSuccess(null);
                    return null;
                }
            });
        }
    }

    public List<UserHomeSite> queryOrderBetween(long start, long end) throws SQLException {
        return dao.queryBuilder().orderBy(UserHomeSite.ORDER, true).where().between(UserHomeSite.ORDER, start, end).query();
    }


    public int clear() throws SQLException {
        List<UserHomeSite> homeSites = dao.queryForAll();
        return dao.delete(homeSites);
    }

    public <T> void transaction(Callable<T> callable) throws SQLException {
        ConnectionSource connectionSource = dao.getConnectionSource();
        TransactionManager.callInTransaction(connectionSource, callable);
    }

    public void updateHomeSiteOrder(UserHomeSite homeSite, long order) throws SQLException {
        homeSite.setOrder(order);
        update(homeSite);
    }

    private int update(UserHomeSite homeSite) throws SQLException {
        return dao.update(homeSite);
    }

    public List<UserHomeSite> queryAllOrderByOrder() throws SQLException {
        return dao.queryBuilder().orderBy(UserHomeSite.ORDER, true).query();
    }

    public UserHomeSite queryBySiteId(String siteId) throws SQLException {
        return dao.queryBuilder().where().eq(HomeSite.SITE_ID, siteId).queryForFirst();
    }

    public void insert(List<UserHomeSite> userHomesites) throws SQLException {
        for (UserHomeSite userHomeSite : userHomesites) {
            dao.createIfNotExists(userHomeSite);
        }
    }
}
