package com.polar.browser.vclibrary.bean.events;

/**
 * Created by hacke on 2016/11/3.
 */

public class SyncSiteItemHideEvent {

    private int position;

    public SyncSiteItemHideEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
