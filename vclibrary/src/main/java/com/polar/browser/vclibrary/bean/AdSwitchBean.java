package com.polar.browser.vclibrary.bean;

/**
 * Created by FKQ on 2017/2/20.
 */

public class AdSwitchBean {

    private String adVersion;
    private boolean fbStart;
    private boolean fbHomenative;
    private boolean adjustLife;
    private boolean uploadSk;
    private boolean uploadUrl;
    private boolean videoPlug = true;
    private boolean videoCustom;

    public boolean isUploadSk() {
        return uploadSk;
    }

    public void setUploadSk(boolean uploadSk) {
        this.uploadSk = uploadSk;
    }

    public boolean isUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(boolean uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public AdSwitchBean() {

    }

    public String getAdVersion() {
        return adVersion;
    }

    public void setAdVersion(String adVersion) {
        this.adVersion = adVersion;
    }

    public boolean isFbStart() {
        return fbStart;
    }

    public void setFbStart(boolean fbStart) {
        this.fbStart = fbStart;
    }

    public boolean isFbHomenative() {
        return fbHomenative;
    }

    public void setFbHomenative(boolean fbHomenative) {
        this.fbHomenative = fbHomenative;
    }

    public boolean isAdjustLife() {
        return adjustLife;
    }

    public void setAdjustLife(boolean adjustLife) {
        this.adjustLife = adjustLife;
    }

    public boolean isVideoPlug() {
        return videoPlug;
    }

    public void setVideoPlug(boolean videoPlug) {
        this.videoPlug = videoPlug;
    }

    public boolean isVideoCustom() {
        return videoCustom;
    }

    public void setVideoCustom(boolean videoCustom) {
        this.videoCustom = videoCustom;
    }
}
