package com.polar.browser.vclibrary.network.exception;

/**
 * Created by James on 2016/9/18.
 * cv校验不一致异常
 */
public class CVNotConsistentException extends NetworkException {
    public CVNotConsistentException() {
    }

    public CVNotConsistentException(String detailMessage) {
        super(detailMessage);
    }

    public CVNotConsistentException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CVNotConsistentException(Throwable throwable) {
        super(throwable);
    }

    public CVNotConsistentException(String requestCV, String responseCV) {
    }
}
