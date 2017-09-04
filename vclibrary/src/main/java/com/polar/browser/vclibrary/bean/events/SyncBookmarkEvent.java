package com.polar.browser.vclibrary.bean.events;

/**
 * Created by saifei on 17/4/6.
 */

public class SyncBookmarkEvent {
    public int type;

    public SyncBookmarkEvent(int type) {
        this.type = type;
    }

    //    public static final int TYPE_UPLOAD_SUCCESS = 0;
    public static final int TYPE_SYNC_FAILED = 0;
    //    public static final int TYPE_DOWNLOAD_SUCCESS = 2;
    public static final int TYPE_MANUAL_SYNC_SUCCESS = 1;//手动同步成功
    public static final int TYPE_AUTO_SYNC_SUCCESS = 2;//手动同步成功
    public static final int TYPE_LOGIN_SUCCESS = 3;
    public static final int TYPE_LOGOUT_SYNC_FAILED = 5;


}
