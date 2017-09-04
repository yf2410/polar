package com.polar.browser.i;

/**
 * Created by liuzikuo on 2017/3/14.
 */

public interface IDownloadCallBack<T> {
    void onDownloadSuccess(T source);
    void onDownloadFailed(String error);
}
