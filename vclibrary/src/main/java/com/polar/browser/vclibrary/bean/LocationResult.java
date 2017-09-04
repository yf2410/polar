package com.polar.browser.vclibrary.bean;

/**
 * Created by yxx on 2017/4/13.
 */

public class LocationResult {

    private double lat;
    private double lng;
    private double accuracy;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
}
