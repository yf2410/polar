package com.polar.browser.vclibrary.bean;

/**
 * Created by James on 2016/9/18.
 */
public class Site {
    private String siteId;
    private String siteName;
    private String siteAddr;
    private String sitePic;

    public Site() {
    }

    /**
     * 网站,用于server返回的数据
     *
     * @param siteId
     * @param siteName
     * @param siteAddr
     * @param sitePic
     */
    public Site(String siteId, String siteName, String siteAddr, String sitePic) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteAddr = siteAddr;
        this.sitePic = sitePic;
    }

    /**
     * 只有网站名和网址,用于首页/收藏/历史
     *
     * @param siteName
     * @param siteAddr
     */
    public Site(String siteName, String siteAddr) {
        this.siteName = siteName;
        this.siteAddr = siteAddr;
    }

    public Site(String siteName, String siteAddr, String sitePic) {
        this.siteName = siteName;
        this.siteAddr = siteAddr;
        this.sitePic = sitePic;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteAddr() {
        return siteAddr;
    }

    public void setSiteAddr(String siteAddr) {
        this.siteAddr = siteAddr;
    }

    public String getSitePic() {
        return sitePic;
    }

    public void setSitePic(String sitePic) {
        this.sitePic = sitePic;
    }
}
