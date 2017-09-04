package com.polar.browser.vclibrary.db;

import android.app.ActionBar;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.bean.db.HomeSite;
import com.polar.browser.vclibrary.bean.db.SearchRecord;
import com.polar.browser.vclibrary.bean.db.SiteInfo;
import com.polar.browser.vclibrary.bean.db.SiteListVersion;
import com.polar.browser.vclibrary.bean.db.SystemNews;
import com.polar.browser.vclibrary.bean.db.UserHomeSite;

import java.sql.SQLException;

/**
 * Created by James on 2016/7/12.
 * <p>
 * 目前为多库多表结构,以后规划统一为一库多表,将其他数据库中的表迁移到vcbrowser数据库
 * 已知的数据库有
 * loginassistant.db 记住用户名密码
 * history.db 历史记录
 * download_db.db 下载
 */
public class CustomOpenHelper extends OrmLiteSqliteOpenHelper {
    /**
     * vcbrowser database name
     */
    private static final String DATABASE_NAME = "polar_browser.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 4;
    private static CustomOpenHelper instance;

    private CustomOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static CustomOpenHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (CustomOpenHelper.class) {
                if (instance == null) {
                    instance = new CustomOpenHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * create table
     *
     * @param database
     * @param connectionSource
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, SystemNews.class);
            TableUtils.createTableIfNotExists(connectionSource, SiteInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, HomeSite.class);
            TableUtils.createTableIfNotExists(connectionSource, SiteListVersion.class);
            TableUtils.createTableIfNotExists(connectionSource, SearchRecord.class);
            TableUtils.createTableIfNotExists(connectionSource, HistoryRecord.class);
            TableUtils.createTableIfNotExists(connectionSource, UserHomeSite.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * update database
     *
     * @param database
     * @param connectionSource
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                try {
                    TableUtils.createTableIfNotExists(connectionSource, SearchRecord.class);
                    TableUtils.createTableIfNotExists(connectionSource, HistoryRecord.class);
                    TableUtils.createTableIfNotExists(connectionSource, UserHomeSite.class);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    TableUtils.createTableIfNotExists(connectionSource, HistoryRecord.class);
                    TableUtils.createTableIfNotExists(connectionSource, UserHomeSite.class);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 3:
                try {
                    TableUtils.createTableIfNotExists(connectionSource, UserHomeSite.class);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            default:
                break;
        }
    }



//    public Dao<SystemNews, Long> getSystemNewsDao() throws SQLException {
//        return getDao(SystemNews.class);
//    }
//
//
//    public Dao<com.polar.browser.vclibrary.bean.db.SiteInfo, Long> getSiteInfoDao() throws SQLException {
//        return getDao(com.polar.browser.vclibrary.bean.db.SiteInfo.class);
//    }
//
//    public Dao<com.polar.browser.vclibrary.bean.db.HomeSite, Long> getHomeSiteDao() throws SQLException {
//        return getDao(com.polar.browser.vclibrary.bean.db.HomeSite.class);
//    }
//
//    public Dao<com.polar.browser.vclibrary.bean.db.SiteListVersion, Long> getSiteListVersionDao() throws SQLException {
//        return getDao(com.polar.browser.vclibrary.bean.db.SiteListVersion.class);
//    }
}
