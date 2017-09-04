package com.polar.browser.bean;

/**
 * Created by yd_lp on 2016/10/20.
 */

public class ImageItemInfo extends BaseFileClass{
    /**
     * 图片id
     * */
    private long id;
    /**
     * 图片名称
     * */
    private String name;
    /**
     * 图片路径
     * */
    private String path;

    private long bucketId;
    private String bucketName;

    public ImageItemInfo() {

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getBucketId() {
        return bucketId;
    }

    public void setBucketId(long bucketId) {
        this.bucketId = bucketId;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public String toString() {
        return "ImageItemInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", bucketId=" + bucketId +
                ", bucketName='" + bucketName + '\'' +
                '}';
    }
}
