package com.polar.browser.vclibrary.bean.events;

/**
 * Created by FKQ on 2017/4/7.
 */

public class NotifLoginResultEvent {

    private int type;

    public NotifLoginResultEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
