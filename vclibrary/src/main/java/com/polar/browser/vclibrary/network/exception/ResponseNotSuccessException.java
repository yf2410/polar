package com.polar.browser.vclibrary.network.exception;

import okhttp3.ResponseBody;

/**
 * Created by James on 2016/9/18.
 * 网络响应不成功异常
 */

public class ResponseNotSuccessException extends Exception {
    public ResponseNotSuccessException() {
        super();
    }

    public ResponseNotSuccessException(String detailMessage) {
        super(detailMessage);
    }

    public ResponseNotSuccessException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ResponseNotSuccessException(Throwable throwable) {
        super(throwable);
    }

    public ResponseNotSuccessException(int code, ResponseBody responseBody) {
    }
}
