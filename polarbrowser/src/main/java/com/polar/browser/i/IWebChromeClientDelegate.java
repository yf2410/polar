package com.polar.browser.i;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient.CustomViewCallback;

public interface IWebChromeClientDelegate {
    public void onShowCustomView(View view, CustomViewCallback callback);

    public void onHideCustomView();

    public View getVideoLoadingProgressView();

    public Bitmap getDefaultVideoPoster();

    void release(); //释放内存
}
