package com.polar.browser.i;

/**
 * Created by yxx on 2017/3/8.
 */

public interface IDownloadImgCallBack {
    void onDownloadSuccess(byte[] bytes);
    void onDownloadFailed(String error);
}
