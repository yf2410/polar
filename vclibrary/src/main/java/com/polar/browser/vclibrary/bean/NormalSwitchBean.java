package com.polar.browser.vclibrary.bean;

/**
 * Created by FKQ on 2016/12/27.
 * 通用 开关 bean
 */

public class NormalSwitchBean {


    public NormalSwitchBean() {
    }

    private boolean switchStatus;
    private String des;
    private String icon;
    private String url;

    public boolean isSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(boolean switchStatus) {
        this.switchStatus = switchStatus;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
