package com.polar.browser.vclibrary.bean.db;

import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.polar.browser.vclibrary.bean.Site;

/**
 * Created by yangfan on 2017/5/23.
 */
@DatabaseTable(tableName="userhomesite")
public class UserHomeSite {

    public static final String SITE_ID = "siteId";
    public static final String SITE_ADDR = "siteAddr";
    public static final String SITE_NAME = "siteName";
    public static final String ORDER = "order";
    public static final String IS_CUSTOM = "custom";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField
    private String siteId;
    @DatabaseField
    private String siteName;
    @DatabaseField
    private String siteAddr;
    @DatabaseField
    private String sitePic;
    @DatabaseField
    private long order;
    @DatabaseField
    private boolean custom;

    public UserHomeSite() {
    }

    public UserHomeSite(@NonNull Site site, long order, boolean custom) {
        this(site.getSiteId(), site.getSiteName(), site.getSiteAddr(), site.getSitePic(), order, custom);
    }

    public UserHomeSite(@NonNull com.polar.browser.vclibrary.bean.db.SiteInfo siteInfo, long id, long order, boolean custom) {
        this(siteInfo.getSiteId(), siteInfo.getSiteName(), siteInfo.getSiteAddr(), siteInfo.getSitePic(), order, custom);
        this.id = id;
    }

    public UserHomeSite(String siteId, String siteName, String siteAddr, String sitePic, long order, boolean custom) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteAddr = siteAddr;
        this.sitePic = sitePic;
        this.order = order;
        this.custom = custom;
    }


    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSitePic() {
        return sitePic;
    }

    public void setSitePic(String sitePic) {
        this.sitePic = sitePic;
    }

    public String getSiteAddr() {
        return siteAddr;
    }

    public void setSiteAddr(String siteAddr) {
        this.siteAddr = siteAddr;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "UserHomeSite{" +
                "id=" + id +
                ", siteId='" + siteId + '\'' +
                ", siteName='" + siteName + '\'' +
                ", siteAddr='" + siteAddr + '\'' +
                ", sitePic='" + sitePic + '\'' +
                ", order=" + order +
                ", custom=" + custom +
                '}';
    }
}
