package com.polar.browser.vclibrary.bean;

/**
 * Created by FKQ on 2016/8/24.
 */
public class UpdateApkInfo {

    private UpdateApkDataInfo data;
    private int updateStatus;
    private String cv;

    public UpdateApkInfo() {
    }

    public UpdateApkDataInfo getData() {
        return data;
    }

    public void setData(UpdateApkDataInfo data) {
        this.data = data;
    }

    public int getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(int updateStatus) {
        this.updateStatus = updateStatus;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    @Override
    public String toString() {
        return "UpdateApkInfo{" +
                "data=" + data +
                ", updateStatus=" + updateStatus +
                ", cv='" + cv + '\'' +
                '}';
    }
}


