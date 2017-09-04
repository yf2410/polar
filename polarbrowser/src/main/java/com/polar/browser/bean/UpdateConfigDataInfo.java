package com.polar.browser.bean;

/**
 * Created by KQ on 2016/8/25.
 */

public class UpdateConfigDataInfo {

    private String url;
    private String file;
    private String md5;

    public UpdateConfigDataInfo() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "UpdateConfigDataInfo{" +
                "url='" + url + '\'' +
                ", file='" + file + '\'' +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
