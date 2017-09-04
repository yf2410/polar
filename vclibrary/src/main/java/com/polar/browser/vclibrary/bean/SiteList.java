package com.polar.browser.vclibrary.bean;

import java.util.List;

/**
 * Created by James on 2016/9/18.
 */
public class SiteList {
    private String siteListVersion;
    private List<Site> siteList;

    public String getSiteListVersion() {
        return siteListVersion;
    }

    public void setSiteListVersion(String siteListVersion) {
        this.siteListVersion = siteListVersion;
    }

    public List<Site> getSiteList() {
        return siteList;
    }

    public void setSiteList(List<Site> siteList) {
        this.siteList = siteList;
    }
}
