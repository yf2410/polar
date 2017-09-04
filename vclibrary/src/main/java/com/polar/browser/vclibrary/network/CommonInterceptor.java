package com.polar.browser.vclibrary.network;

import android.content.Context;

import com.polar.browser.vclibrary.network.api.ApiConstants;
import com.polar.browser.vclibrary.util.ApiUtil;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by James on 2016/6/2.
 * <p>
 * 通用参数拦截器,处理
 */
public class CommonInterceptor implements Interceptor {

    private Context context;

    public CommonInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl originalUrl = request.url();

        HttpUrl newUrl = originalUrl.newBuilder()
                .addQueryParameter(ApiConstants.PARAM_MOBILE_PLATFORM, ApiUtil.getMobilePlatForm())
                .addQueryParameter(ApiConstants.PARAM_APP_NAME, ApiUtil.getAppName())
                .addQueryParameter(ApiConstants.PARAM_APP_VERSION, ApiUtil.getVersionName(context))
                .addQueryParameter(ApiConstants.PARAM_APP_VERSION_CODE, ApiUtil.getVersionCode(context))
                .addQueryParameter(ApiConstants.PARAM_LANGUAGE, ApiUtil.getLan())
                .addQueryParameter(ApiConstants.PARAM_OS_VERSION, ApiUtil.getOS())
                .addQueryParameter(ApiConstants.PARAM_MCC, ApiUtil.getMCC(context))
                .addQueryParameter(ApiConstants.PARAM_AREA, ApiUtil.getArea())
                .addQueryParameter(ApiConstants.PARAM_MMOD, ApiUtil.getMMOD())
                .addQueryParameter(ApiConstants.PARAM_MID, ApiUtil.getMID(context))
                .addQueryParameter(ApiConstants.PARAM_CV, ApiUtil.getCV())
                .build();
        Request newRequest = request.newBuilder().url(newUrl).build();
        return chain.proceed(newRequest);
    }
}
