package com.polar.browser.bean;

/**
 * Created by yd_lp on 2016/10/19.
 */

public class VideoInfo extends BaseFileClass {
    /**
     * id
     */
    private long id;
    /**
     * 视频名称
     * */
    private String name;
    /**
     * 视频大小
     * */
    private long size;
    /**
     * 视频修改日期
     * */
    private long date;
    /**
     * 视频路径
     * */
    private String path;

    private String thumbnailPath;

    public VideoInfo() {

    }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

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

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    @Override
    public String toString() {
        return "VideoInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", date=" + date +
                ", path='" + path + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                '}';
    }
}
