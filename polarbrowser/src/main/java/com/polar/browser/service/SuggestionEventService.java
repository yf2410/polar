package com.polar.browser.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.gson.Gson;
import com.polar.browser.BuildConfig;
import com.polar.browser.JuziApp;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.SuggestionEvent;
import com.polar.browser.vclibrary.network.api.Api;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by saifei on 16/12/21.
 */

public class SuggestionEventService extends IntentService {

    private static final int SLEEP_TIME = 60 * 1000 * 5;
    private static final int TEST_SLEEP_TIME =  1000 * 20;

    private static final String TAG = "SuggestionEventService";
    private static final String LOGGING_API = "http://l.zowdow.com/v1/log";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public SuggestionEventService() {
        super("SuggestionEventService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Gson gson = new Gson();
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(JuziApp.getAppContext());
        } catch (IOException | GooglePlayServicesNotAvailableException ignored) {
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
        String device_id = null;
        if (adInfo != null) {
             device_id = adInfo.getId();
        }
        try {
            Thread.sleep(BuildConfig.DEBUG?TEST_SLEEP_TIME:SLEEP_TIME);//睡眠五分钟
            String eventJson = ConfigManager.getInstance().getSuggestionEvent();
            if(TextUtils.isEmpty(eventJson)) return;
            ConfigManager.getInstance().saveSuggestionEvent("",true);

            SuggestionEvent event = gson.fromJson(eventJson, SuggestionEvent.class);
            if(event==null) return;
            event.setApp_id(JuziApp.getAppContext().getPackageName());
            event.setDevice_id(device_id);
            Api.getInstance().sendSuggestionEvent(LOGGING_API,event).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    SimpleLog.e(TAG,"SuggestionEventService onResponse = "+response.body());
                    ConfigManager.getInstance().setNeedSendSuggesitonEvent(true);

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    SimpleLog.e(TAG,"SuggestionEventService onFailure = "+t.toString());
                    ConfigManager.getInstance().setNeedSendSuggesitonEvent(true);
                }
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
            SimpleLog.e(TAG,"SuggestionEventService onFailure = "+e.toString());
            ConfigManager.getInstance().setNeedSendSuggesitonEvent(true);
        }


    }
}
