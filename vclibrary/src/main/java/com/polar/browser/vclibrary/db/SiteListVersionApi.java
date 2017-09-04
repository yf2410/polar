package com.polar.browser.vclibrary.db;

import com.j256.ormlite.dao.Dao;
import com.polar.browser.vclibrary.bean.db.SiteListVersion;
import java.sql.SQLException;

/**
 * Created by James on 2016/9/27.
 */

public class SiteListVersionApi {
    private static SiteListVersionApi instance;
    private Dao<SiteListVersion, Long> dao;

    private SiteListVersionApi(CustomOpenHelper customOpenHelper) throws SQLException{
        this.dao = customOpenHelper.getDao(SiteListVersion.class);
    }

    public static SiteListVersionApi getInstance(CustomOpenHelper customOpenHelper) throws SQLException{
        if (instance == null) {
            synchronized (SiteListVersionApi.class) {
                if (instance == null) {
                    instance = new SiteListVersionApi(customOpenHelper);
                }
            }
        }
        return instance;
    }

//    public void init(Dao<SiteListVersion, Long> dao) {
//        this.dao = dao;
//    }

    /**
     * 插入
     *
     * @param siteListVersion 无id
     * @return 带id的数据
     * @throws SQLException
     */
    public SiteListVersion insert(SiteListVersion siteListVersion) throws SQLException {
        return dao.createIfNotExists(siteListVersion);
    }

    /**
     * 更新
     *
     * @param siteListVersion
     * @return
     * @throws SQLException
     */
    public int update(SiteListVersion siteListVersion) throws SQLException {
        return dao.update(siteListVersion);
    }

    public SiteListVersion query(int type) throws SQLException {
        SiteListVersion siteListVersion = dao.queryBuilder().where().eq(SiteListVersion.TYPE, type).queryForFirst();
        return siteListVersion;
    }
//    /**
//     * 根据mcc area和language匹配版本数据
//     * 目前是根据mcc和语言来查
//     *
//     * @param mcc
//     * @param language
//     * @return
//     * @throws SQLException
//     */
//    public SiteListVersion query(String mcc, String area, String language) throws SQLException {
//        Where<SiteListVersion, Long> where = dao.queryBuilder().where();
//        if (mcc != null) {
//            where.and().eq(SiteListVersion.MCC, mcc);
//        }
//        if (area != null) {
//            where.and().eq(SiteListVersion.AREA, area);
//        }
//        if (language != null) {
//            where.and().eq(SiteListVersion.LANGUAGE, language);
//        }
//        SiteListVersion siteListVersion = where.queryForFirst();
//        return siteListVersion;
//    }

    /**
     * 删除
     *
     * @param siteListVersion
     * @return
     * @throws SQLException
     */
    public int delete(SiteListVersion siteListVersion) throws SQLException {
        return dao.delete(siteListVersion);
    }

    public int clear() throws SQLException {
        return dao.delete(dao.queryForAll());
    }


}
