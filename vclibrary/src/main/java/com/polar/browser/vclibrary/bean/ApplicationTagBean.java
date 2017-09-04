package com.polar.browser.vclibrary.bean;

/**
 * Created by FKQ on 2017/3/16.
 */

public class ApplicationTagBean {

    /** 首页广告位是否加载过 */
    private boolean adLocationHome;
    /** 是否初始化系统消息列表产品关于数据 */
    private boolean presetSystemNewsData;

    public ApplicationTagBean() {
    }

    public boolean isAdLocationHome() {
        return adLocationHome;
    }

    public void setAdLocationHome(boolean adLocationHome) {
        this.adLocationHome = adLocationHome;
    }

    public boolean isPresetSystemNewsData() {
        return presetSystemNewsData;
    }

    public void setPresetSystemNewsData(boolean presetSystemNewsData) {
        this.presetSystemNewsData = presetSystemNewsData;
    }
}
