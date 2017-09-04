package com.polar.browser.bean;

import java.io.Serializable;

/**
 * Created by saifei on 16/12/30.
 */

public class BaseFileClass implements Serializable{
    /**
     * id
     * */
    protected long id;
    protected boolean isEditing;
    protected String path;
    protected String name;
    protected long size;
    protected long date;

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
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

    public boolean isEditing() {
        return isEditing;
    }

    public void setEditing(boolean editing) {
        isEditing = editing;
    }

    public boolean isChecked;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
