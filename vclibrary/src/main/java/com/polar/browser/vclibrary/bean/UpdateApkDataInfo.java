package com.polar.browser.vclibrary.bean;

/**
 * Created by FKQ on 2016/8/24.
 */

public class UpdateApkDataInfo {

    private String desc;
    private String md5;
    private String ver;
    private String url;
    private int vercode;
    private long updateTime;

    public UpdateApkDataInfo() {
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVercode() {
        return vercode;
    }

    public void setVercode(int vercode) {
        this.vercode = vercode;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "UpdateApkDataInfo{" +
                "desc='" + desc + '\'' +
                ", md5='" + md5 + '\'' +
                ", ver='" + ver + '\'' +
                ", url='" + url + '\'' +
                ", vercode=" + vercode +
                ", updateTime=" + updateTime +
                '}';
    }
}
