package com.polar.browser.i;

/**
 * Created by liuzikuo on 2017/4/14.
 */

public interface IUploadCallBack<T> {
    void onUploadSuccess(T source);
    void onUploadFailed(String error);
}
