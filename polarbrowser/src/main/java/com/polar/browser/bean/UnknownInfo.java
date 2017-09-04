package com.polar.browser.bean;

/**
 * Created by liuzikuo on 2016/11/4.
 */

public class UnknownInfo extends BaseFileClass {
    private long id;
    /**
     * 文档名称
     * */
    private String name;
    /**
     * 文档大小
     * */
    private long size;
    /**
     * 文档修改时间
     * */
    private long date;
    /**
     * 文档路径
     * */
    private String path;

    public UnknownInfo() {

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
}
