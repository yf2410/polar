package com.polar.browser.bean;

/**
 * Created by yd_lp on 2016/10/21.
 */

public class ZipInfo extends BaseFileClass{
    private long id;
    /**
     * 压缩包名称
     * */
    private String name;
    /**
     * 压缩包大小
     * */
    private long size;
    /**
     * 压缩包修改时间
     * */
    private long date;
    /**
     * 压缩包路径
     * */
    private String path;

    public ZipInfo() {

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

    @Override
    public String toString() {
        return "ZipInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", date=" + date +
                ", path='" + path + '\'' +
                '}';
    }
}
