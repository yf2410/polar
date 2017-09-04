package com.polar.browser.vclibrary.bean.events;

/**
 * Created by duan on 17/3/20.
 */

public class IntoImageBrowseEvent {

    private String imgs;
    private boolean isLoadMore;

    public IntoImageBrowseEvent(String imgs, boolean isLoadMore) {
        this.imgs = imgs;
        this.isLoadMore = isLoadMore;
    }

    public String getImgs() {
        return imgs;
    }

    public boolean isLoadMore() {
        return isLoadMore;
    }
}
