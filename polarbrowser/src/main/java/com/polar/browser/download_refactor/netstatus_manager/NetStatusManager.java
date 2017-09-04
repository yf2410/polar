package com.polar.browser.download_refactor.netstatus_manager;

import android.content.Context;
import android.os.Handler;

import com.polar.browser.download_refactor.netstatus_manager.ManagerUIHandler.IDelegate;
import com.polar.browser.download_refactor.util.ListenerList;

import java.util.Iterator;

public class NetStatusManager implements IDelegate {
    private static NetStatusManager sObj;
    private ListenerList<IDelegate> mObservers = new ListenerList<IDelegate>();  
    public static NetStatusManager getInstance(){
        if( sObj == null )
            sObj = new NetStatusManager();
        return sObj;
    }
    
    private  ConnectivityMonitor mConnectivityMonitor;
    public void init(Context context, Handler handler){
        mConnectivityMonitor = new ConnectivityMonitor(context, 
                new ManagerUIHandler(handler.getLooper(),
                        context,
                        this));
        mConnectivityMonitor.initialize();
    }
    
    public void unInit(){
        if(mConnectivityMonitor!=null)
            mConnectivityMonitor.release();
    }
 
    public void addObserver(IDelegate ob){
        if(ob == null)
            return;  
        mObservers.add(ob);
    }
    
    public void removeObserver(IDelegate ob){
        if(ob == null)
            return;
        mObservers.remove(ob);
    }

    public String getNetworkType() {
        if (null == mConnectivityMonitor)
            return "";
        return mConnectivityMonitor.getTypeString();
    }

    public String getNetworkSubtype() {
        if (null == mConnectivityMonitor)
            return "";
        return mConnectivityMonitor.getSubtypeString();
    }

    public boolean isConnectivityMobileActive() {
        if (null == mConnectivityMonitor)
            return false;
        return mConnectivityMonitor.isMobileActive();
    }

    public boolean isConnectivityWifiActive() {
        if (null == mConnectivityMonitor)
            return false;
        return mConnectivityMonitor.isWifiActive();
    }

    public boolean isConnectivityActive() {
        if (null == mConnectivityMonitor)
            return false;
        return mConnectivityMonitor.isWifiActive()
                || mConnectivityMonitor.isMobileActive();
    }

    @Override
    public void onDidFromWifiToMobile() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onDidFromNullToMobile() {
        mObservers.begin();
        try {
          Iterator<IDelegate> it = mObservers.iterator();
          while(it.hasNext()) {
              IDelegate listener = it.next();
             if(listener != null) {
                listener.onDidFromNullToMobile();
              }
          }
        } finally {
            mObservers.end();
        }
        
    }

    @Override
    public void onDidFromNullToWifi() {
        mObservers.begin();
        try {
          Iterator<IDelegate> it = mObservers.iterator();
          while(it.hasNext()) {
              IDelegate listener = it.next();
             if(listener != null) {
                listener.onDidFromNullToWifi();
              }
          }
        } finally {
            mObservers.end();
        }
        
    }

    @Override
    public void onDidFromMobileToWifi() {
        mObservers.begin();
        try {
          Iterator<IDelegate> it = mObservers.iterator();
          while(it.hasNext()) {
              IDelegate listener = it.next();
             if(listener != null) {
                listener.onDidFromMobileToWifi();
              }
          }
        } finally {
            mObservers.end();
        }
        
    }

    @Override
    public void onDidNoAvailableNetwork() {
        mObservers.begin();
        try {
          Iterator<IDelegate> it = mObservers.iterator();
          while(it.hasNext()) {
              IDelegate listener = it.next();
             if(listener != null) {
                listener.onDidNoAvailableNetwork();
              }
          }
        } finally {
            mObservers.end();
        }
    }
}
