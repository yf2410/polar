package com.polar.browser.library.network;

import android.content.Context;
import android.support.annotation.NonNull;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by James on 2016/9/14.
 */

public abstract class AbstractNetworkManager {


    protected Retrofit retrofit;
    protected OkHttpClient client;
    protected Context context;

    /**
     * 调用一次
     *
     * @param context
     */
    public void init(@NonNull Context context, @NonNull String baseUrl) {
        this.context = context;
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        initOkHttpClient(okHttpBuilder);
        client = okHttpBuilder.build();
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl(baseUrl);
        retrofitBuilder.client(client);
        initRetrofit(retrofitBuilder);
        retrofit = retrofitBuilder.build();
    }


    protected abstract void initOkHttpClient(OkHttpClient.Builder builder);

    protected abstract void initRetrofit(Retrofit.Builder builder);
}
