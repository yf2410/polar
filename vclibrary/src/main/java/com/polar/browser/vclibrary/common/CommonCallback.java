package com.polar.browser.vclibrary.common;

/**
 * Created by James on 2016/9/20.
 */

public abstract class CommonCallback<T> implements ICallback<T> {
    public abstract void onSuccess(T t) throws Exception;

    public void onError(Exception e) {
        e.printStackTrace();
    }
}
