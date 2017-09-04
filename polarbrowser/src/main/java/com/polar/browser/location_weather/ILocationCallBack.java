package com.polar.browser.location_weather;

/**
 * Created by yxx on 2017/4/13.
 */

public interface ILocationCallBack {
    void callback(double lat, double lng);

    void onFailed(int code, String msg);
}
