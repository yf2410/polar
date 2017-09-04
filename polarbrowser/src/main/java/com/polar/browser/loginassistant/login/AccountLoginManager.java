package com.polar.browser.loginassistant.login;

import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.LoadingDialog;
import com.polar.browser.i.IUploadCallBack;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.events.NotifLoginResultEvent;
import com.polar.browser.vclibrary.bean.login.FbAccountData;
import com.polar.browser.vclibrary.bean.login.FbPhoneAccountJson;
import com.polar.browser.vclibrary.bean.login.PhoneAccountData;
import com.polar.browser.vclibrary.bean.login.ServerUserAccountData;
import com.polar.browser.vclibrary.bean.login.ShadowAccountData;
import com.polar.browser.vclibrary.bean.login.ShadowAccountJson;
import com.polar.browser.vclibrary.bean.login.UserAccountData;
import com.polar.browser.vclibrary.network.api.Api;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by FKQ on 2017/3/30.
 */

public class AccountLoginManager {
    private static final String TAG = AccountLoginManager.class.getSimpleName();
    public static final String APP_NAME = "polar";
    private static final String CLIENT_PLATFORM = "android";

    public static final int NOTIFLOGINRESULT_SUCCESS = 0x1 << 1;
    public static final int NOTIFLOGINRESULT_ERRO = 0x1 << 2;

    private static AccountLoginManager mInstance;
    private AccountLoginManager(){
    }

    public static AccountLoginManager getInstance() {
        if (mInstance == null) {
            synchronized (AccountLoginManager.class) {
                if (mInstance == null) {
                    mInstance = new AccountLoginManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 程序初始化得到影子账号
     */
    public void getShadowAccount() {
        if(!TextUtils.isEmpty(ConfigManager.getInstance().getShadowToken())) {
            return;
        }
        ShadowAccountJson shadowAccountJson = new ShadowAccountJson();
        shadowAccountJson.setA(APP_NAME);
        shadowAccountJson.setB(null);
        shadowAccountJson.setC(SystemUtils.getImeiId(JuziApp.getAppContext()));
        shadowAccountJson.setP(CLIENT_PLATFORM);

        SimpleLog.d("--Login--", "shadowAccountJson="+shadowAccountJson.toString());
        Api.getInstance().userShadowAccount(shadowAccountJson).enqueue(new Callback<ShadowAccountData>() {
            @Override
            public void onResponse(Call<ShadowAccountData> call, Response<ShadowAccountData> response) {
                SimpleLog.d("--Login--", "userShadowAccount="+(response.body() != null ? response.body().toString() : ""));
                ShadowAccountData shadowAccountData = response.body();
                if(shadowAccountData != null && shadowAccountData.errorcode == 0){
                    if(shadowAccountData.ltoken != null){
                        ConfigManager.getInstance().setShadowToken(shadowAccountData.ltoken);
                    }
                }
            }

            @Override
            public void onFailure(Call<ShadowAccountData> call, Throwable t) {
                SimpleLog.d("--Login--", "userShadowAccount-onFailure="+t.toString());
            }
        });
    }

    /**
     * 通过facebook授权登录获取用户登录令牌
     *
     * @param input_token  facebook 返回给客户端的 token
     * @param token 服务端向客户端发放的登录令牌
     */
    public void getUserAccountByFacebook(String input_token, String token) {
        FbPhoneAccountJson fbPhoneAccountJson = new FbPhoneAccountJson();
        fbPhoneAccountJson.setAppName(APP_NAME);
        fbPhoneAccountJson.setType(CLIENT_PLATFORM);
        fbPhoneAccountJson.setInput_token(input_token);
        fbPhoneAccountJson.setToken(token);
        SimpleLog.d(TAG,"getUserAccountByFacebook params - "+fbPhoneAccountJson.toString());
        Api.getInstance().userFacebookAccount(fbPhoneAccountJson).enqueue(new Callback<FbAccountData>() {
            @Override
            public void onResponse(Call<FbAccountData> call, Response<FbAccountData> response) {

                FbAccountData fbData = response.body();
                SimpleLog.d("tag","getUserAccountByFacebook params - "+fbData.toString());
                System.out.print("FbAccountData"+fbData.toString());
                SimpleLog.d(TAG,(response.body() != null ? response.body().toString() : "null"));
                if(fbData != null && fbData.errorcode == 0){
                    //覆盖本地影子账号token
                    //登录信息
                    if(!TextUtils.isEmpty(fbData.ltoken)){
                        ConfigManager.getInstance().setShadowToken(fbData.ltoken);
                    }
                    //登录信息
                    ConfigManager.getInstance().setUserToken(fbData.token);  //登录成功授权Token
                    //用户信息
                    UserAccountData userAccountData = new UserAccountData();
                    userAccountData.setToken(fbData.token);
                    userAccountData.setsId(fbData.sid);
                    ConfigManager.getInstance().setUserId(fbData.sid);  //同步书签使用
                    userAccountData.setAccountType(ConfigDefine.TYPE_LOGIN_FACEBOOK); //Facebook登录
                    userAccountData.setEmail(fbData.email);
                    userAccountData.setGender(fbData.gender);
                    if(!TextUtils.isEmpty(fbData.name)){
                        userAccountData.setUsername(fbData.name);  //用户名
                    }else {
                        userAccountData.setUsername(fbData.fbname);
                    }
                    userAccountData.setBirthday(fbData.birthday);
                    userAccountData.setLocale(fbData.locale);
                    userAccountData.setTimeZone(fbData.timeZone);
                    userAccountData.setAvatar(fbData.picture);
                    userAccountData.setAvatarLastModified(System.currentTimeMillis());
                    SimpleLog.d("tag" , " userAccountData" + userAccountData.toString());
                    //保存用户信息
                    updateUserAccountData(userAccountData);
                    RxBus.get().post(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_SUCCESS));
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_SUCCESS, GoogleConfigDefine.ACCOUNT_LOGIN_SUCCESS_FACEBOOK);
                } else {
                    RxBus.get().post(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_FACEBOOK);
                }
            }

            @Override
            public void onFailure(Call<FbAccountData> call, Throwable t) {
                SimpleLog.d("tag","getUserAccountByFacebook params 请求Facebook失败");
                RxBus.get().post(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_FACEBOOK);
            }
        });

    }



    public void getUserData(String token , String appName){
        Api.getInstance().requestAccountInformation(token , appName).enqueue(new Callback<ServerUserAccountData>() {
            @Override
            public void onResponse(Call<ServerUserAccountData> call, Response<ServerUserAccountData> response) {
                ServerUserAccountData body = response.body();
                SimpleLog.d("tag" , "ServerUserAccountData" + body.toString());
                //UserAccountData userAccountData = new UserAccountData();
                UserAccountData userAccountData = JuziApp.getUserAccountData();
                if(body != null && body.errorcode == 0) {
                    String userName = body.name;
                    if(!TextUtils.isEmpty(userName)){
                        userAccountData.setUsername(body.name);
                    }
//                    userAccountData.setToken(body.token);
                    userAccountData.setNickName(body.nickname);
                    userAccountData.setAvatar(body.hPortrait);
                    userAccountData.setBirthday(body.birthday);
                    userAccountData.setGender(body.gender);
                    userAccountData.setAvatarLastModified(System.currentTimeMillis());
                    userAccountData.setAge(body.age);
                    userAccountData.setToken(ConfigManager.getInstance().getUserToken());
                    userAccountData.setEmail("");
                    JuziApp.updateUserAccountData(userAccountData);
                }
            }

            @Override
            public void onFailure(Call<ServerUserAccountData> call, Throwable t) {
                SimpleLog.d("tag" , "ServerUserAccountData" +"失败");
            }
        });
    }

    /**
     *
     * 通过手机号码验证登录获取用户登录令牌
     *
     * @param input_token facebook 返回给客户端的 token
     * @param token 服务端向客户端发放的登录令牌
     */
    public void getUserAccountByPhone(String input_token, String token) {
        FbPhoneAccountJson fbPhoneAccountJson = new FbPhoneAccountJson();
        fbPhoneAccountJson.setAppName(APP_NAME);
        fbPhoneAccountJson.setType(CLIENT_PLATFORM);
        fbPhoneAccountJson.setInput_token(input_token);
        fbPhoneAccountJson.setToken(token);

        Api.getInstance().userPhoneAccount(fbPhoneAccountJson).enqueue(new Callback<PhoneAccountData>() {
            @Override
            public void onResponse(Call<PhoneAccountData> call, Response<PhoneAccountData> response) {
                SimpleLog.d("--Login--", "userPhoneAccount=="+(response.body() != null ? response.body().toString() : "null"));
                PhoneAccountData phoneAccountData = response.body();
                if(phoneAccountData != null && phoneAccountData.errorcode == 0){
                    //登录信息
                    ConfigManager.getInstance().setUserToken(phoneAccountData.token);  //登录成功授权Token
                    //覆盖本地影子账号token
                    if(!TextUtils.isEmpty(phoneAccountData.ltoken)){
                        ConfigManager.getInstance().setShadowToken(phoneAccountData.ltoken);
                    }
                    //用户信息
                    UserAccountData userAccountData = new UserAccountData();
                    userAccountData.setToken(phoneAccountData.token);
                    userAccountData.setsId(phoneAccountData.sid);
                    ConfigManager.getInstance().setUserId(phoneAccountData.sid);  //同步书签使用
                    userAccountData.setAccountType(ConfigDefine.TYPE_LOGIN_PHONE_NUMBER);  //手机号登录
                    userAccountData.setPhoneNum(phoneAccountData.pn);
                    userAccountData.setUsername(phoneAccountData.pn);
                    userAccountData.setAvatar(phoneAccountData.picture);
                    userAccountData.setAvatarLastModified(System.currentTimeMillis());
                    JuziApp.updateUserAccountData(userAccountData);
                    RxBus.get().post(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_SUCCESS));
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_SUCCESS, GoogleConfigDefine.ACCOUNT_LOGIN_SUCCESS_PHONE_NUM);
                } else {
                    RxBus.get().post(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                    Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_PHONE_NUM);
                }
            }

            @Override
            public void onFailure(Call<PhoneAccountData> call, Throwable t) {
                SimpleLog.d("--Login--", "userPhoneAccount==onFailure"+t.getMessage());
                RxBus.get().post(new NotifLoginResultEvent(AccountLoginManager.NOTIFLOGINRESULT_ERRO));
                Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL, GoogleConfigDefine.ACCOUNT_LOGIN_FAIL_PHONE_NUM);
            }
        });
    }

    protected void uploadUserImage(String token, final File file, final IUploadCallBack<String> callBack) {

        RequestBody userImager = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        Api.getInstance().uploadUserImage(APP_NAME,token,userImager).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                SimpleLog.d("--Login--", "uploadUserImage-success");
                callBack.onUploadSuccess(file.getPath());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                SimpleLog.d("--Login--", "uploadUserImage-fail");
                callBack.onUploadFailed("");
            }
        });
    }

    public void sendAccountLoginStatistics(String appName, String token) {
        Api.getInstance().sendAccountLoginStatistics(appName,token).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                SimpleLog.d("--Login--", "sendAccountLoginStatistics-success");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                SimpleLog.d("--Login--", "sendAccountLoginStatistics-success");
            }
        });
    }


    /**
     * 用户是否已登录 TODO
     * @return
     */
    public boolean isUserLogined() {
        return ConfigManager.getInstance().getUserToken() != null;
    }

    private void updateUserAccountData(UserAccountData userInfo) {
        JuziApp.updateUserAccountData(userInfo);

//        Api.getInstance().requestAccountInformation(APP_NAME,mUserAccountData.getToken()).enqueue(new Callback<ServerUserAccountData>() {
//            @Override
//            public void onResponse(Call<ServerUserAccountData> call, Response<ServerUserAccountData> response) {
//                SimpleLog.d("--Login--", "requestAccountInformation="+response.body().toString());
//                ServerUserAccountData serverUserAccountData = response.body();
//                if (serverUserAccountData != null) {
//                    String hPortrait = serverUserAccountData.hPortrait;
//                    if (!TextUtils.isEmpty(hPortrait)) {
//                        mUserAccountData.setAvatar(hPortrait);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ServerUserAccountData> call, Throwable t) {
//
//            }
//        });
    }

}
