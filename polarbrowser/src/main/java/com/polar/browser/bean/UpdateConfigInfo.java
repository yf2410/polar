package com.polar.browser.bean;

import java.util.List;

/**
 * Created by FKQ on 2016/8/25.
 */

public class UpdateConfigInfo {

    private List<UpdateConfigDataInfo> data;
    private String cv;

    public UpdateConfigInfo() {
    }

    public List<UpdateConfigDataInfo> getData() {
        return data;
    }

    public void setData(List<UpdateConfigDataInfo> data) {
        this.data = data;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    @Override
    public String toString() {
        return "UpdateConfigInfo{" +
                "data=" + data +
                ", cv='" + cv + '\'' +
                '}';
    }
}
