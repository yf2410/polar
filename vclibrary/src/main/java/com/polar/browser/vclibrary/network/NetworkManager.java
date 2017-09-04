package com.polar.browser.vclibrary.network;

import android.content.Context;
import android.support.annotation.NonNull;
import com.polar.browser.library.network.AbstractNetworkManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by James on 2016/5/25.
 */
public class NetworkManager extends AbstractNetworkManager {
    /**
     * 设置缓存大小8M
     */
    private static final long CACHE_SIZE = 8 * 1024 * 1024;
    /**
     * 超时
     */
    private static final long TIME_OUT = 20 * 1000;
    private Context context;

    public NetworkManager(Context context) {
        this.context = context;
    }

    /**
     * 初始化retrofit
     *
     * @param builder
     * @return
     */

    @Override
    protected void initRetrofit(Retrofit.Builder builder) {
        builder.addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create()).
                addCallAdapterFactory(RxJava2CallAdapterFactory.create());
    }


    /**
     * 配置OkHttpClient
     * 包括 缓存 请求参数拦截器 请求头拦截器 Cookie
     *
     * @return
     */
    @Override
    @NonNull
    protected void initOkHttpClient(OkHttpClient.Builder builder) {
        CommonInterceptor commonInterceptor = new CommonInterceptor(context);
//
//		Cache cache = new Cache(FileUtil.getCacheDirectory(), CACHE_SIZE);
//
        NoCookieJar noCookieJar = new NoCookieJar();
//
        builder.addInterceptor(commonInterceptor)
//				.cache(cache)
                .cookieJar(noCookieJar)
                .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS);
//				.retryOnConnectionFailure(true)
//              .build();
        //
//        if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        builder.addInterceptor(loggingInterceptor);
//        }
//

    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}
