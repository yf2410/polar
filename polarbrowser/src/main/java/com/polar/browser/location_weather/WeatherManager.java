package com.polar.browser.location_weather;

import android.text.Editable;

import com.polar.browser.vclibrary.bean.LastWeatherInfo;
import com.polar.browser.vclibrary.bean.WeatherResult;
import com.polar.browser.vclibrary.network.api.Api;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

/**
 * Created by yxx on 2017/4/13.
 */

public class WeatherManager {

    public static Observable<WeatherResult> requestWeather(double lat, double lng) {
        String url =  "https://query.yahooapis.com/v1/public/yql?q= " +
                "select location, item.condition from weather.forecast where woeid in (select woeid from geo.places(1) where " +
                "text=\"(" + lat + "," + lng+")\")AND u=\"c\"&format=json";
       return  Api.getInstance().requestForWeather(url)
                .subscribeOn(Schedulers.io());
    }


    public static Call<List<LastWeatherInfo>> requestRecCities(Editable inputs) {
        String url = "https://www.yahoo.com/news/_td/api/resource/WeatherSearch;text=" + inputs + "?lang=en-US";
        return Api.getInstance().requestForLocations(url);
    }
}
