package com.polar.browser.vclibrary.bean.db;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by James on 2016/9/22.
 */
@DatabaseTable(tableName = "sitelistversion")
public class SiteListVersion {
    public static final String TYPE = "type";

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(unique = true)
    private long type;
    @DatabaseField
    private String version;


    public SiteListVersion() {
    }

    public SiteListVersion(long id, long type, String version) {
        this.id = id;
        this.type = type;
        this.version = version;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
