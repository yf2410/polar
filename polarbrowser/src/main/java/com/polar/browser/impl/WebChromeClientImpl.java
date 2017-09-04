package com.polar.browser.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

import com.polar.browser.i.IWebChromeClientDelegate;
import com.polar.browser.video.H5PlayerFullScreenMgr;

public class WebChromeClientImpl implements IWebChromeClientDelegate {
    public WebChromeClientImpl(Context c) {
        H5PlayerFullScreenMgr.getInstance().registerInstance(c);
    }

    @Override
    public View getVideoLoadingProgressView() {
        return H5PlayerFullScreenMgr.getInstance().getVideoLoadingProgressView();
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        H5PlayerFullScreenMgr.getInstance().onShow(view, callback);
        return;
    }

    @Override
    public void onHideCustomView() {
        H5PlayerFullScreenMgr.getInstance().onHide();
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return H5PlayerFullScreenMgr.getInstance().getDefaultVideoPoster();
    }

    @Override
    public void release() {
        H5PlayerFullScreenMgr.getInstance().unRegisterInstance();
    }
}
