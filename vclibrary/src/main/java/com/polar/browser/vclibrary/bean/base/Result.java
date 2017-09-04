package com.polar.browser.vclibrary.bean.base;

/**
 * Created by James on 2016/9/14.
 */

public class Result<T> extends BaseResult {
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    private int code;
    private String message;
    private T data;
    private String cv;

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
