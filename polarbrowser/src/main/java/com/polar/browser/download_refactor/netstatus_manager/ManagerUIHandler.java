package com.polar.browser.download_refactor.netstatus_manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.polar.browser.utils.CustomToastUtils;

public class ManagerUIHandler extends Handler {
    public static final int MSG_NO_AVAILABLE_NETWORK        = 100;
    public static final int MSG_FROM_MOBILE_TO_WIFI         = 101;
    public static final int MSG_FROM_NULL_TO_WIFI           = 102;
    public static final int MSG_FROM_WIFI_TO_MOBILE         = 103;
    public static final int MSG_FROM_NULL_TO_MOBILE         = 104;
    /** 因网络变化导致不支持续传的任务中断 */
    public static final int MSG_ALERT_INTERRUPTED = 107;

    /** 创建新任务 */
    public static final int MSG_NEW_TASK                    = 200;
    /** 用来发送消息，获取当前的task队列中有多少任务处于下载完成没有点击过 */
    public static final int MSG_TASK_FINISHED               = 201;
    /** 用来发送空间不足的消息 */
    public static final int MSG_USABLESPACE                 = 202;
    /** wifi下自动下载 add by tanglong */
    public static final int MSG_WIFI_RESTART_TASK           = 203;

    private Context     mAppContext;
    private IDelegate   mDelegate;

    public ManagerUIHandler(Looper looper, Context context,
            IDelegate delegate) {
        super(looper);
        this.mAppContext = context;
        this.mDelegate = delegate;
    }

    @Override
    public void handleMessage(Message msg) {
        if (null == mDelegate)
            return;

        switch (msg.what) {
            case MSG_NO_AVAILABLE_NETWORK:
                mDelegate.onDidNoAvailableNetwork();
                break;
            case MSG_FROM_MOBILE_TO_WIFI:
                mDelegate.onDidFromMobileToWifi();
                break;
            case MSG_FROM_NULL_TO_WIFI:
                mDelegate.onDidFromNullToWifi();
                break;
            case MSG_FROM_WIFI_TO_MOBILE:
                mDelegate.onDidFromWifiToMobile();
                break;
            case MSG_FROM_NULL_TO_MOBILE:
                mDelegate.onDidFromNullToMobile();
                break;
            case MSG_NEW_TASK:
//                mDelegate.onDidNewTask((IDownloadTask) msg.obj);
                break;
            case MSG_TASK_FINISHED:
                break;
            case MSG_USABLESPACE:
                CustomToastUtils.getInstance().showTextToast("R.string.error_available_space");
                break;
            case MSG_WIFI_RESTART_TASK:
                CustomToastUtils.getInstance().showTextToast("R.string.s_download_text_auto_start_hint");
                break;
            case MSG_ALERT_INTERRUPTED:
                CustomToastUtils.getInstance().showTextToast("R.string.s_download_text_interrupted");
                break;
        }
    }

    public void release() {
        mAppContext = null;
        mDelegate = null;

        removeMessages(MSG_NO_AVAILABLE_NETWORK);
        removeMessages(MSG_FROM_MOBILE_TO_WIFI);
        removeMessages(MSG_FROM_NULL_TO_WIFI);
        removeMessages(MSG_FROM_WIFI_TO_MOBILE);
        removeMessages(MSG_FROM_NULL_TO_MOBILE);
        removeMessages(MSG_NEW_TASK);
        removeMessages(MSG_TASK_FINISHED);
        removeMessages(MSG_USABLESPACE);
        removeMessages(MSG_WIFI_RESTART_TASK);
        removeMessages(MSG_ALERT_INTERRUPTED);
    }

    /**
     * <p>NonThreadSafe.</p>
     * <p>TODO: 现在无线程管理，但有强引用在，所以暂时也就这样了</p>
     */
    public static interface IDelegate {
        public void onDidFromWifiToMobile();
        public void onDidFromNullToMobile();
        public void onDidFromNullToWifi();
        public void onDidFromMobileToWifi();
        public void onDidNoAvailableNetwork();
    }
}
