package com.polar.browser.manager;

import android.view.View.OnLongClickListener;

import com.polar.browser.i.IDownloadDelegate;
import com.polar.browser.i.IJsCallbackDelegate;
import com.polar.browser.i.ITouchListener;
import com.polar.browser.i.IWebChromeClientDelegate;
import com.polar.browser.i.IWebViewClientDelegate;

public class TabViewCallbackManager {

    private static TabViewCallbackManager sInstance;
    private IJsCallbackDelegate mJsDelegate;
    private IDownloadDelegate mDownloadDelegate;
    private IWebViewClientDelegate mWebViewClientDelegate;
    private IWebChromeClientDelegate mWebChromeClientDelegate;
    private OnLongClickListener mOnLongClickListener;
    private ITouchListener mTouchListener;

    private TabViewCallbackManager() {
    }

    public static TabViewCallbackManager getInstance() {
        if (sInstance == null) {
            synchronized (TabViewCallbackManager.class) {
                if (sInstance == null) {
                    sInstance = new TabViewCallbackManager();
                }
            }
        }
        return sInstance;
    }

    public void registerJsCallBack(IJsCallbackDelegate delegate) {
        mJsDelegate = delegate;
    }

    public void registerDownloadDelegate(IDownloadDelegate delegate) {
        mDownloadDelegate = delegate;
    }

    public void registerWebViewClientDelegate(IWebViewClientDelegate delegate) {
        mWebViewClientDelegate = delegate;
    }

    public void registerWebChromeClientDelegate(IWebChromeClientDelegate delegate) {
        mWebChromeClientDelegate = delegate;
    }

    public void registerOnLongClickListener(OnLongClickListener onLongClickListener) {
        mOnLongClickListener = onLongClickListener;
    }

    public void registerTouchListener(ITouchListener touchListener) {
        mTouchListener = touchListener;
    }

    public IJsCallbackDelegate getJsCallBack() {
        return mJsDelegate;
    }

    public IDownloadDelegate getDownloadDelegate() {
        return mDownloadDelegate;
    }

    public IWebChromeClientDelegate getWebChromeClientDelegate() {
        return mWebChromeClientDelegate;
    }

    public IWebViewClientDelegate getWebViewClientDelegate() {
        return mWebViewClientDelegate;
    }

    public OnLongClickListener getOnLongClickListener() {
        return mOnLongClickListener;
    }

    public ITouchListener getTouchListener() {
        return mTouchListener;
    }

    public void destroy() {
        mJsDelegate = null;
        mDownloadDelegate = null;
        mWebViewClientDelegate = null;
        if (mWebChromeClientDelegate != null) {
            mWebChromeClientDelegate.release();
        }
        mWebChromeClientDelegate = null;
        sInstance = null;
    }
}
