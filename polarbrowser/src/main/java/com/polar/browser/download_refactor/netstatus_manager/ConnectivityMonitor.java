package com.polar.browser.download_refactor.netstatus_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.polar.browser.utils.SimpleLog;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>ThreadSafe.</p>
 * <p>网络连接状态监控器。</p>
 * <p>产品层面上，关于各种网络类型的定义和称呼：</br>
 * 当前认为Ethernet和wifi网络连接都是免费连接，因此都定义为产品需求意义上的wifi连接；
 * 当前认为所有mobile网络连接都是收费连接，因此都定义为产品需求意义上的mobile连接；
 * 当前认为除了Ethernet、wifi和mobile之外的网络连接，都无法访问外网，因此都定义为产品需求意义上
 * 的无连接。</p>
 */
class ConnectivityMonitor {
    private static final String TAG = "download.ConnectivityMonitor";

    private IntentFilter mIntentFilter;
    private BroadcastReceiver mBroadcastReceiver;
    private Handler mHandlerRef;
    private Context mContextRef;
    private AtomicBoolean mEthernetConnected;
    private AtomicBoolean mWifiConnected;
    private AtomicBoolean mMobileConnected;

    // need protected by mNetworkInfoGuard -------------------------------------
    private String mNetworkType;
    private String mNetworkSubtype;
    private String mNetworkSpeed;
    private Object mNetworkInfoGuard;

    public ConnectivityMonitor(Context context, Handler handler) {
        this.mContextRef = context;
        this.mHandlerRef = handler;
        this.mNetworkInfoGuard = new Object();
        this.mEthernetConnected = new AtomicBoolean(false);
        this.mWifiConnected     = new AtomicBoolean(false);
        this.mMobileConnected   = new AtomicBoolean(false);
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    /**
     * 当前流量是否走wifi
     * @return
     */
    public boolean isWifiActive() {
        return mEthernetConnected.get() || mWifiConnected.get();
    }

    /**
     * 当前流量是否走mobile
     * @return
     */
    public boolean isMobileActive() {
        if (mEthernetConnected.get() || mWifiConnected.get())
            return false;
        return mMobileConnected.get();
    }

    public String getTypeString() {
        synchronized(mNetworkInfoGuard) {
            return mNetworkType;
        }
    }

    public String getSubtypeString() {
        synchronized(mNetworkInfoGuard) {
            return mNetworkSubtype;
        }
    }

    public void release() {
        if (null == mContextRef || null == mBroadcastReceiver)
            return;

        try {
            mContextRef.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            SimpleLog.e(e);
        }

        mContextRef         = null;
        mBroadcastReceiver  = null;
        mHandlerRef         = null;
        mIntentFilter       = null;
    }

    public void initialize() {
        updateNetworkInfo();
        mBroadcastReceiver = new MyBroadcastReceiver();
        mContextRef.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    private void sendMessage(int msg) {
        Message.obtain(mHandlerRef, msg).sendToTarget();
    }

    private void updateNetworkInfo() {
        if (null == mContextRef) {
            resetNetworkInfo();
            return;
        }

        fillNetworkInfo();
        synchronized(mNetworkInfoGuard) {
            String messageInfo = "ConnectivityMonitor.onReceive("
                    + mNetworkType + ", "
                    + mNetworkSubtype + ", "
                    + mNetworkSpeed + "), ethernet: "
                    + mEthernetConnected.get() + ", wifi: "
                    + mWifiConnected.get() + ", mobile: "
                    + mMobileConnected.get();
                SimpleLog.d(TAG, messageInfo);
        }
    }

    private void resetNetworkInfo() {
        mEthernetConnected.set(false);
        mWifiConnected.set(false);
        mMobileConnected.set(false);
        synchronized(mNetworkInfoGuard) {
            mNetworkType = "";
            mNetworkSubtype = "";
            mNetworkSpeed = "";
        }
    }

    private void setNetworkInfo(NetworkInfo info) {
        String type     = null;
        String subtype  = null;
        String speed    = null;
        if (null != info) {
            type    = info.getTypeName();
            subtype = info.getSubtypeName();
            speed   = Connectivity.getNetworkSpeed(info.getType(),
                    info.getSubtype());
        }
        synchronized(mNetworkInfoGuard) {
            if (TextUtils.isEmpty(type))
                mNetworkType = "";
            else
                mNetworkType = type;

            if (TextUtils.isEmpty(subtype))
                mNetworkSubtype = "";
            else
                mNetworkSubtype = subtype;

            if (TextUtils.isEmpty(speed))
                mNetworkSpeed = "";
            else
                mNetworkSpeed = speed;
        }
    }

    private void fillNetworkInfo() {
        NetworkInfo ethernetInfo =
                Connectivity.getEthernetNetworkInfo(mContextRef);
        mEthernetConnected.set(Connectivity.isConnected(ethernetInfo));

        NetworkInfo wifiInfo = Connectivity.getWifiNetworkInfo(mContextRef);
        mWifiConnected.set(Connectivity.isConnected(wifiInfo));

        NetworkInfo mobileInfo = Connectivity.getMobileNetworkInfo(mContextRef);
        mMobileConnected.set(Connectivity.isConnected(mobileInfo));

        // 假定三者同时连接时，优先级 Ethernet > wifi > mobile
        if (mEthernetConnected.get()) {
            setNetworkInfo(ethernetInfo);
            return;
        }

        if (mWifiConnected.get()) {
            setNetworkInfo(wifiInfo);
            return;
        }

        if (mMobileConnected.get()) {
            setNetworkInfo(mobileInfo);
            return;
        }

        setNetworkInfo(null);
    }


    private class MyBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (null == mHandlerRef)
                return;

            String action = intent.getAction();
            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
                return;

            boolean lastEthernetConnected = mEthernetConnected.get();
            boolean lastWifiConnected     = mWifiConnected.get();
            boolean lastMobileConnected   = mMobileConnected.get();
            updateNetworkInfo();

            // TODO: 以后可能会做ip是否切换的case event
            if (mEthernetConnected.get() || mWifiConnected.get()) {
                if (lastEthernetConnected || lastWifiConnected) {
                    // event: 依然是有线或wifi网络，不需要通知
                    return;
                } else if (lastMobileConnected) {
                    // event: 从mobile切换到有线或者wifi
                    sendMessage(ManagerUIHandler.MSG_FROM_MOBILE_TO_WIFI);
                    return;
                } else {
                    // event: 从无网络切换到有线或者wifi
                    sendMessage(ManagerUIHandler.MSG_FROM_NULL_TO_WIFI);
                    return;
                }
            }

            if (mMobileConnected.get()) {
                if (lastEthernetConnected || lastWifiConnected) {
                    // event: 从有线或wifi网络切换到mobile
                    sendMessage(ManagerUIHandler.MSG_FROM_WIFI_TO_MOBILE);
                    return;
                } else if (lastMobileConnected) {
                    // event: 依然是mobile，不需要通知
                    return;
                } else {
                    // event: 从无网络切换到有线或者mobile
                    sendMessage(ManagerUIHandler.MSG_FROM_NULL_TO_MOBILE);
                    return;
                }
            }

            sendMessage(ManagerUIHandler.MSG_NO_AVAILABLE_NETWORK);
        }
    }
}
