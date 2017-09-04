package com.polar.browser.vclibrary.bean.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by FKQ on 2017/5/2.
 */

@DatabaseTable(tableName = "searchrecord")
public class SearchRecord {

    public static final int SEARCH = 1;
    public static final int GO = 2;
    public static final String TS = "ts";
    public static final String SEARCH_ADDR = "searchAddr";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName="searchAddr")
    private String searchAddr;
    @DatabaseField(columnName="type")
    private int type;
    @DatabaseField(columnName="ts")
    private Date ts;

    public SearchRecord() {
    }

    public SearchRecord(String searchAddr, int type, Date ts) {
        this.searchAddr = searchAddr;
        this.type = type;
        this.ts = ts;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSearchAddr() {
        return searchAddr;
    }

    public void setSearchAddr(String searchAddr) {
        this.searchAddr = searchAddr;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }
}
