package com.polar.browser.vclibrary.bean;

import java.io.Serializable;

/**
 * Created by yxx on 2017/4/19.
 */

public class LastWeatherInfo implements Serializable {

    /**
     * woeid : 160524
     * lat : 8.85522
     * lon : -80.24865
     * country : Panama
     * city : U
     * qualifiedName : U, Cocle, PA
     */

    private int woeid;
    private double lat;
    private double lon;
    private String country;
    private String city;
    private String qualifiedName;

    public int getWoeid() {
        return woeid;
    }

    public void setWoeid(int woeid) {
        this.woeid = woeid;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }
}
