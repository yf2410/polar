package com.polar.browser.update;

import android.text.TextUtils;
import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.UpdateApkInfo;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.ApiUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 应用升级辅助类，请求服务端接口，并返回升级信息
 */
public class UpdateUtils {
    private static final String TAG = "UpdateUtils";
    private static final String UPDATE_APK_TYPE = "appupdate";
    public static void checkAppUpdate() {
        Api.getInstance().updateApk(UPDATE_APK_TYPE, ApiUtil.getVersionCode(JuziApp.getAppContext())).enqueue(new Callback<UpdateApkInfo>() {
            @Override
            public void onResponse(Call<UpdateApkInfo> call, Response<UpdateApkInfo> response) {
                try {
                    UpdateApkInfo mUpdateApkIfo = response.body();
                    if (mUpdateApkIfo == null || mUpdateApkIfo.getData() == null) {
                        return;
                    }
                    if (AppEnv.DEBUG) {
                        SimpleLog.d(TAG, "mUpdateApkIfo=="+mUpdateApkIfo.toString());
                    }
                    int updateStatus = mUpdateApkIfo.getUpdateStatus();
                    switch (updateStatus){
                        case CommonData.APP_NO_UPDATE:
                            return;
                        case CommonData.APP_NEED_UPDATE:
                            ConfigWrapper.put(ConfigDefine.APP_UPDATE_STATUS, updateStatus);
                            ConfigWrapper.apply();
                            break;
                        case CommonData.APP_FORCE_UPDATE:
                            break;
                        default:
                            break;
                    }
                    String desc = mUpdateApkIfo.getData().getDesc();
                    if (TextUtils.isEmpty(desc)){
                        return;
                    }else {
                        ConfigWrapper.put(ConfigDefine.APP_UPDATE_DESC, desc);
                        ConfigWrapper.apply();
                    }
                    String url = mUpdateApkIfo.getData().getUrl();
                    if (TextUtils.isEmpty(url)){
                        return;
                    }else {
                        ConfigWrapper.put(ConfigDefine.APP_UPDATE_URL, url);
                        ConfigWrapper.apply();
                    }
                    String md5 = mUpdateApkIfo.getData().getMd5();
                    if (TextUtils.isEmpty(md5)){
                        return;
                    }else {
                        ConfigWrapper.put(ConfigDefine.APP_UPDATE_MD5, md5);
                        ConfigWrapper.apply();
                    }
                    long updateTime = mUpdateApkIfo.getData().getUpdateTime();
                    if (updateTime == 0L){
                        return;
                    }else {
                        long updateLastTime = ConfigWrapper.get(ConfigDefine.APP_UPDATE_LAST_TIME, 0L);
                        if (AppEnv.DEBUG) {
                            SimpleLog.d(TAG, "UP_updateLastTime="+updateLastTime + "updateTime ="+updateTime);
                        }
                        if (updateTime > updateLastTime) {
                            ConfigWrapper.put(ConfigDefine.APP_UPDATE_LAST_TIME, updateTime);
                            ConfigWrapper.apply();
                            ConfigManager.getInstance().setShownNOUpdateApkTip();
                            AppEnv.sIsShownUpdateApkTip = false;
                        }
                    }
                } catch (Exception e) {
                }
            }

            @Override
            public void onFailure(Call<UpdateApkInfo> call, Throwable t) {
            }
        });
    }
}
