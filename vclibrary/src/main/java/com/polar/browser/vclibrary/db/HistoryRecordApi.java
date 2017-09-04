package com.polar.browser.vclibrary.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedDelete;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by FKQ on 2017/5/10.
 */

public class HistoryRecordApi {

    private static HistoryRecordApi mInstance;
    private Dao<HistoryRecord, Long> dao;
    private HistoryRecordApi(CustomOpenHelper customOpenHelper) throws SQLException {
        this.dao = customOpenHelper.getDao(HistoryRecord.class);
    }
    public static HistoryRecordApi getInstance(CustomOpenHelper customOpenHelper) throws SQLException{
        if (mInstance == null) {
            synchronized (HistoryRecordApi.class) {
                if (mInstance == null) {
                    mInstance = new HistoryRecordApi(customOpenHelper);
                }
            }
        }
        return mInstance;
    }

    /**
     *
     * @param historyRecord
     * @return
     * @throws SQLException
     */
    public HistoryRecord insert(HistoryRecord historyRecord) throws SQLException {
        return dao.createIfNotExists(historyRecord);
    }

    /**
     *
     * @param historyRecord
     * @throws SQLException
     */
    public void insertOrUpdate(HistoryRecord historyRecord) throws SQLException {
        String historyAddr = historyRecord.getHistoryAddr();
        HistoryRecord historyRecord1 = queryForHistoryAddr(historyAddr);
        if(historyRecord1 != null) {
            int count = historyRecord1.getCount();
            historyRecord1.setCount(++count);
            historyRecord1.setTs(new Date());
            updateAllHistoryRecord(historyRecord1);
        } else {
            insert(historyRecord);
        }

    }

    /**
     * 清除历史记录
     * @return
     * @throws SQLException
     */
    public int clearAllHistoryRecord() throws SQLException {
        return dao.delete(dao.queryForAll());
    }

    /**
     * 查询一条历史记录
     * @param historyAddr
     * @return
     */
    public HistoryRecord queryForHistoryAddr(String historyAddr) throws SQLException{
        List<HistoryRecord> historyRecords = dao.queryForEq(HistoryRecord.HISTORY_ADDR, historyAddr);
        if(historyRecords != null && historyRecords.size()>0){
            HistoryRecord historyRecord = historyRecords.get(0);
            return historyRecord;
        }else{
            return null;
        }
    }

    /**
     * 查询所有历史记录
     * @return
     */
    public List<HistoryRecord> queryAllHistoryRecordByTS(int limit) throws SQLException{
        return dao.queryBuilder().orderBy(HistoryRecord.TS,false).limit(limit).distinct().query();
    }

    /**
     * 查询所有历史记录
     * @return
     */
    public List<HistoryRecord> queryAllHistoryRecord(int limit) throws SQLException{
        return dao.queryBuilder().limit(limit).distinct().query();
    }

    /**
     * 查询所有历史记录
     * @return
     */
    public int updateAllHistoryRecord(HistoryRecord historyRecord) throws SQLException{
        return dao.update(historyRecord);
    }

    /**
     * 根据 count字段查询出常访问历史记录
     * @param limit
     * @return
     * @throws SQLException
     */
    public List<HistoryRecord> queryHistoryRecordByCount(int limit) throws SQLException{
        return dao.queryBuilder().orderBy(HistoryRecord.HISTORY_COUNT,false).limit(limit).distinct().query();
    }

    /**
     * 根据 count字段查询出常访问历史记录
     * @param limit
     * @return
     * @throws SQLException
     */
    public List<HistoryRecord> queryHistoryVisitedByCount(int limit) throws SQLException{
        return dao.queryBuilder().orderBy(HistoryRecord.HISTORY_COUNT,false).limit(limit).where().ge(HistoryRecord.HISTORY_COUNT,5).query();
    }

    /**
     * 根据地址删除历史记录
     * @param historyAddr
     * @return
     * @throws SQLException
     */
    public int deleteHistoryRecordByAddr(String historyAddr) throws SQLException {
        return dao.delete(((PreparedDelete<HistoryRecord>)
                dao.deleteBuilder().where().eq(HistoryRecord.HISTORY_ADDR, historyAddr).prepare()));
    }
}
