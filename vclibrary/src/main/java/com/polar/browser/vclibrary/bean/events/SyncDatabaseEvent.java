package com.polar.browser.vclibrary.bean.events;

/**
 * Created by FKQ on 2017/5/11.
 */

public class SyncDatabaseEvent {

    public static final int TYPE_HISTORY_RECORD = 1;//历史记录
    public static final int TYPE_VISITED_RECORD = 2;//首页长访问记录

    public int type;

    public SyncDatabaseEvent(int type) {
        this.type = type;
    }

}
