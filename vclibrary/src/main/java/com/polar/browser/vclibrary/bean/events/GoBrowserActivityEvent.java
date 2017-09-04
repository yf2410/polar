package com.polar.browser.vclibrary.bean.events;

/**
 * Created by FKQ on 2017/2/16.
 */

public class GoBrowserActivityEvent {

    private int type;
    private int delay;

    public GoBrowserActivityEvent(int type, int delay) {
        this.delay = delay;
        this.type = type;
    }

    public int getDelay() {
        return delay;
    }

    public int getType() {
        return type;
    }

}
