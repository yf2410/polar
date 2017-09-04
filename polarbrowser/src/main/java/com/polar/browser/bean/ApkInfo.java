package com.polar.browser.bean;

/**
 * Created by yd_lp on 2016/10/20.
 */

public class ApkInfo extends BaseFileClass {
    /**
     * id
     * */
    private long id;
    /**
     * apk icon path
     */
    private String iconPath;
    /**
     * apk 名称
     * */
    private String name;
    /**
     * apk 大小
     * */
    private long size;
    /**
     * apk 修改时间
     * */
    private long date;

    private boolean isInstalled;

    public ApkInfo() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean installed) {
        isInstalled = installed;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    @Override
    public String toString() {
        return "ApkInfo{" +
                "id=" + id +
                ", iconPath='" + iconPath + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", date=" + date +
                ", path='" + path + '\'' +
                ", isInstalled=" + isInstalled +
                '}';
    }
}
