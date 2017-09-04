package com.polar.browser.location_weather;

/**
 * Created by yd_lp on 2017/4/18.
 * 天气定位事件
 */

public class WeatherLocationEvent {
    public double lat; //经度
    public double lon; //纬度
    public String country;
    public String city;

    public WeatherLocationEvent() {

    }

    public WeatherLocationEvent(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

}
