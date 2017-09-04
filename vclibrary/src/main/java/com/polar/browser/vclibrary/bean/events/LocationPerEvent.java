package com.polar.browser.vclibrary.bean.events;

/**
 * Created by yxx on 2017/4/21.
 */

public class LocationPerEvent {
    private boolean isGranted;
    public LocationPerEvent(boolean granted) {
        setGranted(granted);
    }

    public void setGranted(boolean granted) {
        isGranted = granted;
    }

    public boolean isGranted() {
        return isGranted;
    }

}
