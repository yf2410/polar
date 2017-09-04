package com.polar.browser.vclibrary.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.polar.browser.vclibrary.bean.db.SystemNews;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Created by James on 2016/7/12.
 */
public class SystemNewsApi {
    private static SystemNewsApi instance;
    private Dao<SystemNews, Long> dao;

    private SystemNewsApi(CustomOpenHelper customOpenHelper) throws SQLException{
            this.dao = customOpenHelper.getDao(SystemNews.class);
//        customOpenHelper.getSystemNewsDao();
    }

    public static SystemNewsApi getInstance(CustomOpenHelper customOpenHelper) throws SQLException{
        if (instance == null) {
            synchronized (SystemNewsApi.class) {
                if (instance == null) {
                    instance = new SystemNewsApi(customOpenHelper);
                }
            }
        }
        return instance;
    }

//    public void init(Dao<com.polar.browser.vclibrary.bean.db.SystemNews, Long> dao) {
//        this.dao = dao;
//    }

    /**
     * @param limit limit per page
     * @param page  start from 0
     * @return 消息列表
     * @throws SQLException
     */
    public List<com.polar.browser.vclibrary.bean.db.SystemNews> querySystemNewsList(long limit, int page) throws SQLException {
        return dao.queryBuilder()
                .orderBy(com.polar.browser.vclibrary.bean.db.SystemNews.COLUMN_RECEIVED_TIME, false)
                .offset((page) * limit)
                .limit(limit)
                .query();
    }

    public List<com.polar.browser.vclibrary.bean.db.SystemNews> queryAllSystemNews() throws SQLException {
        return dao.queryBuilder()
                .orderBy(com.polar.browser.vclibrary.bean.db.SystemNews.COLUMN_RECEIVED_TIME, false)
                .query();
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return 系统消息
     * @throws SQLException
     */
    public com.polar.browser.vclibrary.bean.db.SystemNews queryForId(long id) throws SQLException {
        return dao.queryForId(id);
    }

    /**
     * 查询所有未读消息列表
     *
     * @return 消息列表
     * @throws SQLException
     */
    public List<com.polar.browser.vclibrary.bean.db.SystemNews> queryUnreadSystemNewsList() throws SQLException {
        return dao.queryForEq(com.polar.browser.vclibrary.bean.db.SystemNews.COLUMN_READ, false);
    }

    /**
     * 查询未读消息数
     *
     * @return
     * @throws SQLException
     */
    public long queryUnreadSystemNewsCount() throws SQLException {
        return dao.queryBuilder().where().eq(com.polar.browser.vclibrary.bean.db.SystemNews.COLUMN_READ, false).countOf();
    }

    /**
     * insert a systemNews
     *
     * @param systemNews
     * @return
     * @throws SQLException
     */
    public int insert(com.polar.browser.vclibrary.bean.db.SystemNews systemNews) throws SQLException {
        return dao.create(systemNews);
    }

    /**
     * update a systemNews
     *
     * @param systemNews
     * @return
     * @throws SQLException
     */
    public int updateReadState(com.polar.browser.vclibrary.bean.db.SystemNews systemNews) throws SQLException {
        return dao.update(systemNews);
    }

    /**
     * delete a systemNews
     *
     * @param systemNews
     * @return
     * @throws SQLException
     */
    public int delete(com.polar.browser.vclibrary.bean.db.SystemNews systemNews) throws SQLException {
        return dao.delete(systemNews);
    }

    public int delete(Collection systemNewses) throws SQLException {
        return dao.delete(systemNewses);
    }

    public List<SystemNews> queryLastSystemNewses(long limit) throws SQLException {
        return dao.queryBuilder().orderBy(com.polar.browser.vclibrary.bean.db.SystemNews.COLUMN_ID, false).limit(limit).query();
    }

    /**
     * 查询消息总数
     *
     * @return
     * @throws SQLException
     */
    public long count() throws SQLException {
        return dao.countOf();
    }

    public long clear() throws SQLException {
        DeleteBuilder<SystemNews, Long> deleteBuilder = dao.deleteBuilder();
        deleteBuilder
                .where()
                .ne(com.polar.browser.vclibrary.bean.db.SystemNews.COLUMN_ID, -1);
        return deleteBuilder.delete();
    }
}
