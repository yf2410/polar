package com.polar.browser.bean;

/**
 * Created by yd_lp on 2016/10/21.
 */

public class OthersInfo {
    /**文档*/
    public static final int TYPE_DOC = 0;
    /**压缩包*/
    public static final int TYPE_ZIP = 1;
    /**其他*/
    public static final int TYPE_OTHER = 2;

    /**
     * 文件夹类型 TYPE_DOC，TYPE_ZIP， TYPE_OTHER
     * */
    private int folderType;
    /**文件夹名称*/
    private String name;
    /**文件夹下文件数量*/
    private int count;
    private int iconResource;

    public OthersInfo() {

    }

    public int getFolderType() {
        return folderType;
    }

    public void setFolderType(int folderType) {
        this.folderType = folderType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getIconResource() {
        return iconResource;
    }

    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
}
