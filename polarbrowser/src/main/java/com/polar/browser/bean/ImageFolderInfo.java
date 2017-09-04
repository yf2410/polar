package com.polar.browser.bean;

/**
 * Created by yd_lp on 2016/10/20.
 */

public class ImageFolderInfo extends BaseFileClass {
    /**
     * 图片文件夹名bucket_display_name
     * */
    private String name;
    /**
     * 图片文件夹bucket_id
     * */
    private long id;
    /**
     * 图片文件夹下图片数量
     * */
    private int imageCount;
    /**
     * 图片文件夹封面图片id
     * */
    private long imageID;
    /**
     * 图片文件夹封面图片path
     * */
    private String imagePath;
    /**
     * 图片文件夹path
     * */
    private String folderPath;

    private long date;

    public ImageFolderInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public long getImageID() {
        return imageID;
    }

    public void setImageID(long imageID) {
        this.imageID = imageID;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "ImageFolderInfo{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", imageCount=" + imageCount +
                ", imageID=" + imageID +
                ", imagePath='" + imagePath + '\'' +
                ", folderPath='" + folderPath + '\'' +
                ", date=" + date +
                '}';
    }
}
