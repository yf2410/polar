package com.polar.browser.download.uncompress;

import com.polar.browser.bean.BaseFileClass;

/**
 * Created by yd_lp on 2016/10/21.
 */

public class UncompressInfo extends BaseFileClass {

    public static final int TYPE_COUNT = 4;  //类型数量
    public static final int TYPE_ROOT = 3;   //压缩包根目录
    public static final int TYPE_DIR = 2;    //压缩包内文件夹
    public static final int TYPE_FILE = 1;   //文件

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
    /**
     * 文件类型
     */
    private int type;
    /**
     * 子文件数量
     */
    private int children;

    public UncompressInfo() {

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

    public int getType() {
        if(type<=0) {
            type = UncompressInfo.TYPE_DIR;
        }
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getChildren() {
        return children;
    }

    public void setChildren(int children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "UncompressInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", date=" + date +
                ", path='" + path + '\'' +
                ", type=" + type +
                ", children=" + children +
                '}';
    }
}
