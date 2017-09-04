package com.polar.browser.vclibrary.bean.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;

/**
 * Created by FKQ on 2017/5/10.
 */

@DatabaseTable(tableName = "historyrecord")
public class HistoryRecord {

    public static final String TS = "ts";
    public static final String HISTORY_ADDR = "historyAddr";
    public static final String HISTORY_COUNT = "count";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName="historyTitle")
    private String historyTitle;
    @DatabaseField(columnName="historyAddr")
    private String historyAddr;
    @DatabaseField(columnName="count")
    private int count;
    @DatabaseField(columnName="source")
    private int source;
    @DatabaseField(columnName="ts")
    private Date ts;

    public HistoryRecord() {
    }

    public HistoryRecord(String historyTitle, String historyAddr, int count, int source, Date ts) {
        this.historyTitle = historyTitle;
        this.historyAddr = historyAddr;
        this.count = count;
        this.source = source;
        this.ts = ts;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHistoryTitle() {
        return historyTitle;
    }

    public void setHistoryTitle(String historyTitle) {
        this.historyTitle = historyTitle;
    }

    public String getHistoryAddr() {
        return historyAddr;
    }

    public void setHistoryAddr(String historyAddr) {
        this.historyAddr = historyAddr;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }
}
