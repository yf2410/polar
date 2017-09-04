package com.polar.browser.vclibrary.bean.events;

/**
 * Created by FKQ on 2017/3/23.
 */

public class FbNotifyMsgEvent {

    private String fbNotifyType;
    private String url;
    private String fbMsgCount;

    public FbNotifyMsgEvent(String fbNotifyType, String url, String fbMsgCount) {
        this.fbNotifyType = fbNotifyType;
        this.url = url;
        this.fbMsgCount = fbMsgCount;
    }

    public String getFbNotifyType() {
        return fbNotifyType;
    }

    public String getUrl() {
        return url;
    }

    public String getFbMsgCount() {
        return fbMsgCount;
    }
}
