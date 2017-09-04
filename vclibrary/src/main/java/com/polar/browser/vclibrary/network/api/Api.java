package com.polar.browser.vclibrary.network.api;

import com.polar.browser.vclibrary.bean.AdSwitchBean;
import com.polar.browser.vclibrary.bean.HomeSiteSyncResult;
import com.polar.browser.vclibrary.bean.LastWeatherInfo;
import com.polar.browser.vclibrary.bean.NormalSwitchBean;
import com.polar.browser.vclibrary.bean.SearchEngineList;
import com.polar.browser.vclibrary.bean.SearchSuggestion;
import com.polar.browser.vclibrary.bean.SettingSyncResult;
import com.polar.browser.vclibrary.bean.SiteList;
import com.polar.browser.vclibrary.bean.SuggestionEvent;
import com.polar.browser.vclibrary.bean.SyncBookmarkResult;
import com.polar.browser.vclibrary.bean.UpdateApkInfo;
import com.polar.browser.vclibrary.bean.WeatherResult;
import com.polar.browser.vclibrary.bean.YouTubeVidVo;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.bean.login.FbAccountData;
import com.polar.browser.vclibrary.bean.login.FbPhoneAccountJson;
import com.polar.browser.vclibrary.bean.login.PhoneAccountData;
import com.polar.browser.vclibrary.bean.login.PostUserAccountJson;
import com.polar.browser.vclibrary.bean.login.ServerUserAccountData;
import com.polar.browser.vclibrary.bean.login.ShadowAccountData;
import com.polar.browser.vclibrary.bean.login.ShadowAccountJson;
import com.polar.browser.vclibrary.network.NetworkManager;
import com.polar.browser.vclibrary.util.ApiUtil;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by James on 2016/9/14.
 * <p>
 * 举例
 * Call<Result<SiteList>> siteList = Api.getInstance().siteList("1", "1");
 * <p>
 * siteList.enqueue(new ResultCallback<SiteList>() {
 *
 * @Override <p>
 * public void success(SiteList data, Call<Result<SiteList>> call, Response<Result<SiteList>> response) {
 * }
 * @Override <p>
 * public void error(Call<Result<SiteList>> call, Throwable t) {
 * }
 * });
 */

public class Api implements ApiInterface {

    private static Api instance;

    private ApiInterface apiInterface;

    private Api() {
    }

    public static Api getInstance() {
        if (instance == null) {
            synchronized (Api.class) {
                if (instance == null) {
                    instance = new Api();
                }
            }
        }
        return instance;
    }

    public void init(NetworkManager networkManager) {
        Retrofit retrofit = networkManager.getRetrofit();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    @Override
    public Call<Result<SiteList>> siteList(String version, int siteType) {
        return apiInterface.siteList(version, siteType);
    }

    @Override
    public Call<Result<String>> downloadResoucePost(int type, String url, String userAgent,
                                                    String contentDisposition, String mimetype,
                                                    long contentLength, String referer, String cookies) {
        return apiInterface.downloadResoucePost(type, url, userAgent, contentDisposition, mimetype, contentLength, referer, cookies);
    }

    @Override
    public Call<Result<String>> userFeedBack(String content, String contact,
                                             String totalMemory, String processCpuRate,
                                             String mobilePixels, String networkType,
                                             String availMemory, String appUserMemory) {
        return apiInterface.userFeedBack(content, contact, totalMemory, processCpuRate, mobilePixels, networkType, availMemory, appUserMemory);
    }

    @Override
    public Call<Result<AdSwitchBean>> getAdSwitch(String adVersion) {
        return apiInterface.getAdSwitch(adVersion);
    }

    @Override
    public Call<UpdateApkInfo> updateApk(String type,String vercode) {
        return apiInterface.updateApk(type,vercode);
    }

    @Override
    public Call<SearchSuggestion> searchSuggestion(@Query("q") String keyword, @Query("locale") String local, @Query("cip") String cip, @Query("timezone") String tz) {
        return apiInterface.searchSuggestion(keyword, local, ApiUtil.getIPAddress(true),ApiUtil.getTimeZone());
    }

    @Override
    public Call<Result<SearchEngineList>> searchEngineList(String version) {
        return apiInterface.searchEngineList(version);
    }

    @Override
    public Call<String> sendSuggestionEvent(@Url String url, @Body SuggestionEvent event) {
        return apiInterface.sendSuggestionEvent(url, event);
    }

    @Override
    public Call<ResponseBody> uploadUrl(@Part("description") RequestBody description, @Part MultipartBody.Part file) {
        return apiInterface.uploadUrl(description, file);
    }

    @Override
    public Call<ResponseBody> uploadSk(@Part("description") RequestBody description, @Part MultipartBody.Part file) {
        return apiInterface.uploadSk(description, file);
    }

    @Override
    public Call<List> requestStaPackages() {
        return apiInterface.requestStaPackages();
    }

    @Override
    public Call<String> sendPkgsInstalledSta(String pkgs) {
        return apiInterface.sendPkgsInstalledSta(pkgs);
	}
	
    public Call<ShadowAccountData> userShadowAccount(@Body ShadowAccountJson shadowAccountJson) {
        return apiInterface.userShadowAccount(shadowAccountJson);
    }

    @Override
    public Call<FbAccountData> userFacebookAccount(@Body FbPhoneAccountJson fbPhoneAccountJson) {
        return apiInterface.userFacebookAccount(fbPhoneAccountJson);
    }

    @Override
    public Call<PhoneAccountData> userPhoneAccount(@Body FbPhoneAccountJson fbPhoneAccountJson) {
        return apiInterface.userPhoneAccount(fbPhoneAccountJson);
    }

    @Override
    public Call<String> uploadUserImage(String appName, String token, RequestBody userImage) {
        return apiInterface.uploadUserImage(appName,token,userImage);
    }

    @Override
    public Call<String> uploadAccountInformation(@Body PostUserAccountJson postUserAccountJson) {
        return apiInterface.uploadAccountInformation(postUserAccountJson);
    }

    @Override
    public Call<ServerUserAccountData> requestAccountInformation(String token, String appname) {
        return apiInterface.requestAccountInformation(token,appname);
    }

    @Override
    public Call<String> sendAccountLoginStatistics(String appName, String token) {
        return apiInterface.sendAccountLoginStatistics(appName,token);
    }


    @Override
    public Observable<Result<SyncBookmarkResult>> uploadBookmark(String userToken,  MultipartBody.Part file) {
        return apiInterface.uploadBookmark(userToken,file);
    }

    @Override
    public Observable<Result<SyncBookmarkResult>> syncBookmark(@Field("token") String token) {
        return apiInterface.syncBookmark(token);
    }

    @Override
    public Observable<ResponseBody> downloadFile(String fileUrl) {
        return apiInterface.downloadFile(fileUrl);
    }

    @Override
    public Observable<WeatherResult> requestForWeather(@Url String url) {
        return apiInterface.requestForWeather(url);
    }

    @Override
    public Call<List<LastWeatherInfo>> requestForLocations(String url) {
        return apiInterface.requestForLocations(url);
    }

    @Override
    public Observable<Result<SettingSyncResult>> uploadSetting(@Part("token") String token, @Part MultipartBody.Part file) {
        return apiInterface.uploadSetting(token, file);
    }

    @Override
    public Observable<Result<SettingSyncResult>> syncSetting(@Field("token") String token) {
        return apiInterface.syncSetting(token);
    }

    @Override
    public Observable<Result<HomeSiteSyncResult>> syncHomeSite(@Field("token") String token) {
        return apiInterface.syncHomeSite(token);
    }

    @Override
    public Observable<Result<HomeSiteSyncResult>> uploadHomeSite(@Part("token") String token, @Part MultipartBody.Part file) {
        return apiInterface.uploadHomeSite(token, file);
    }

    public Call<SearchSuggestion> searchSuggestion(@Query("q") String keyword) {
        return apiInterface.searchSuggestion(keyword, ApiUtil.getArea(), ApiUtil.getIPAddress(true),ApiUtil.getTimeZone());
    }

    public Call<Result<NormalSwitchBean>> goDownloadSwitch() {
        return apiInterface.goDownloadSwitch();
    }

    @Override
    public Call<YouTubeVidVo> parserYouTubeVideo(String url, String sign) {
        return apiInterface.parserYouTubeVideo(url, sign);
    }

    @Override
    public Call<YouTubeVidVo> parserYouTubeVideoByServer(String url) {
        return apiInterface.parserYouTubeVideoByServer(url);
    }

}
