package com.polar.browser.vclibrary.bean.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.polar.browser.vclibrary.bean.Site;

/**
 * Created by James on 2016/9/22.
 */
@DatabaseTable(tableName = "siteinfo")
public class SiteInfo {
    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String SITE_ID = "siteId";
    public static final String SITE_ADDR = "siteAddr";
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
    private int type;


    public SiteInfo() {
    }

    public SiteInfo(String siteId, String siteName, String siteAddr, String sitePic, int type) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.siteAddr = siteAddr;
        this.sitePic = sitePic;
        this.type = type;
    }

    public SiteInfo(Site site, int type) {
        siteName = site.getSiteName();
        siteId = site.getSiteId();
        siteAddr = site.getSiteAddr();
        sitePic = site.getSitePic();
        this.type = type;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
