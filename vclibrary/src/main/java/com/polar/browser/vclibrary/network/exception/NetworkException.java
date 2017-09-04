package com.polar.browser.vclibrary.network.exception;

/**
 * Created by James on 2016/9/18.
 * 所有网络异常的基类
 */

public class NetworkException extends Exception {
    public NetworkException() {
    }

    public NetworkException(String detailMessage) {
        super(detailMessage);
    }

    public NetworkException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NetworkException(Throwable throwable) {
        super(throwable);
    }
}
