package com.polar.browser.bean;

/**
 * javascript  bean
 * Created by yxx on 2017/3/16.
 */

public class JsInfo {

    private String extVer;
    private String extName;
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getExtVer() {
        return extVer;
    }

    public void setExtVer(String extVer) {
        this.extVer = extVer;
    }

    public String getExtName() {
        return extName;
    }

    public void setExtName(String extName) {
        this.extName = extName;
    }

    public String getHook() {
        return hook;
    }

    public void setHook(String hook) {
        this.hook = hook;
    }

    public String getInjectTiming() {
        return injectTiming;
    }

    public void setInjectTiming(String injectTiming) {
        this.injectTiming = injectTiming;
    }

    private String hook;//xxxx.js
    private String injectTiming;//注入时机

}
