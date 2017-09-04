package com.polar.browser.homepage.sitelist.classify;

/**
 * Created by James on 2016/9/21.
 */
public class ClassifyItemInfo {
    private int typeId;
    private String title;
    private int imgResource;

    public ClassifyItemInfo(int typeId, String title, int imgResource) {
        this.typeId = typeId;
        this.title = title;
        this.imgResource = imgResource;
    }

    public ClassifyItemInfo() {
    }

    public int getImgResource() {
        return imgResource;
    }

    public void setImgResource(int imgResource) {
        this.imgResource = imgResource;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
