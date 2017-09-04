package com.polar.browser.vclibrary.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedDelete;
import com.polar.browser.vclibrary.bean.db.SearchRecord;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by FKQ on 2017/5/2.
 */

public class SearchRecordApi {

    private static SearchRecordApi mInstance;
    private Dao<SearchRecord, Long> dao;
    private SearchRecordApi(CustomOpenHelper customOpenHelper) throws SQLException {
        this.dao = customOpenHelper.getDao(SearchRecord.class);
    }
    public static SearchRecordApi getInstance(CustomOpenHelper customOpenHelper) throws SQLException{
        if (mInstance == null) {
            synchronized (SearchRecordApi.class) {
                if (mInstance == null) {
                    mInstance = new SearchRecordApi(customOpenHelper);
                }
            }
        }
        return mInstance;
    }

    /**
     *
     * @param searchRecord
     * @return
     * @throws SQLException
     */
    public SearchRecord insert(SearchRecord searchRecord) throws SQLException {
        return dao.createIfNotExists(searchRecord);
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    public List<SearchRecord> queryAllSearchRecordLimit(int limit) throws SQLException {
        return dao.queryBuilder().orderBy(SearchRecord.TS,false).limit(limit).distinct().query();
    }

    public int deleteBySearchRecordAddr(String searchRecordAddr) throws SQLException {
        return dao.delete(((PreparedDelete<SearchRecord>)
                dao.deleteBuilder().where().eq(SearchRecord.SEARCH_ADDR, searchRecordAddr).prepare()));
    }

    public int clearAllSearchRecord() throws SQLException {
        return dao.delete(dao.queryForAll());
    }

    public SearchRecord querySearchRecordExist(String searchContent) throws SQLException {
        return dao.queryBuilder().where().eq(SearchRecord.SEARCH_ADDR, searchContent).queryForFirst();
    }
}
