package com.polar.browser.location_weather;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;

/**
 * Created by yf on 2017/4/19.
 */

public class WeatherLocationUtils {
    /**
     * 天气数组
     */
    private static int[] weatherCodes = {R.string.tornado, R.string.tropical_storm, R.string.hurricane, R.string.severe_thunderstorms, R.string.thunderstorms, R.string.rain_snow, R.string.rain_sleet,
            R.string.snow_sleet, R.string.freezing_drizzle, R.string.drizzle, R.string.freezing_rain, R.string.showers, R.string.showers, R.string.snow_flurries, R.string.small_snow_flurries,
            R.string.blowing_snow, R.string.snow, R.string.hail, R.string.sleet, R.string.dust, R.string.foggy, R.string.haze, R.string.smoky, R.string.blustery, R.string.windy,
            R.string.cold, R.string.cloudy, R.string.mostly_cloudy_night, R.string.mostly_cloudy_day, R.string.partly_cloudy_night, R.string.partly_cloudy_day, R.string.clear_night,
            R.string.sunny, R.string.fair_night, R.string.fair_day, R.string.rain_hail, R.string.hot, R.string.isolated_thunderstorms, R.string.scattered_thunderstorms, R.string.scattered_thunderstorms,
            R.string.scattered_showers, R.string.heavy_snow, R.string.scattered_snow_showers, R.string.partly_cloudy, R.string.thundershowers, R.string.snow_showers, R.string.isolated_thundershowers, R.string.not_available};

    /**
     * 图标数组
     */
    private static int[] drawableCodes = {R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d , R.drawable.e , R.drawable.f , R.drawable.g , R.drawable.h ,
                                        R.drawable.i , R.drawable.j , R.drawable.k , R.drawable.l , R.drawable.m , R.drawable.n , R.drawable.o , R.drawable.p ,
                                        R.drawable.q , R.drawable.r , R.drawable.s , R.drawable.t , R.drawable.u , R.drawable.v , R.drawable.w , R.drawable.x ,
                                        R.drawable.y , R.drawable.z , R.drawable.aa , R.drawable.bb , R.drawable.cc , R.drawable.dd , R.drawable.ee , R.drawable.ff ,
                                        R.drawable.gg , R.drawable.hh , R.drawable.ii , R.drawable.jj , R.drawable.kk , R.drawable.ll , R.drawable.mm , R.drawable.nn ,
                                        R.drawable.oo , R.drawable.pp , R.drawable.qq , R.drawable.rr , R.drawable.ss , R.drawable.tt , R.drawable.uu , R.drawable.vv ,
                                        R.drawable.ww };

    public static String getWeatherByCode(String weatherCode) {
        if (TextUtils.isEmpty(weatherCode)) {
            return null;
        }
        int position = Integer.parseInt(weatherCode);
        if (position <= weatherCodes.length - 1) {
            return JuziApp.getAppContext().getString(weatherCodes[position]);
        }
        return null;
    }

    public static int getDrawableByCode(String weatherCode) {
        if (TextUtils.isEmpty(weatherCode)) {
            return 0;
        }
        int position = Integer.parseInt(weatherCode);
        if (position <= drawableCodes.length - 1) {
            return drawableCodes[position];
        }
        return 0;
    }

    /**
     * 根据摄氏温度转换成华氏温度
     * @param temp
     */
    public static float getFahrenheitByCentigrade(String temp) {
        if(TextUtils.isEmpty(temp)){
                return 0;
        }
        return 9*Integer.parseInt(temp)/5f+32;
    }
}
