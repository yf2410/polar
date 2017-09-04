package com.polar.browser.vclibrary.common;

/**
 * Created by James on 2016/9/23.
 */

public interface ICallback<T> {
    void onSuccess(T t) throws Exception;

    void onError(Exception e);
}
