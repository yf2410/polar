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

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_AD_VERSION;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_APP_NAME;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_APP_VERSION_CODE_OLD;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_CONTENT_DISPOSITION;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_CONTENT_LENGTH;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_COOKIES;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_AVAILMEMORY;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_CONTACT;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_CONTENT;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_CPURATE;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_NETTYPE;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_PIXEL;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_TOTALMEMORY;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_FEEDBACK_USEMEMORY;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_MIMETYPE;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_REFERER;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_SEARCH_ENGINE_V;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_SITE_LIST_VERSION;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_SITE_TYPE;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_TYPE;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_URL;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_USER_AGENT;
import static com.polar.browser.vclibrary.network.api.ApiConstants.PARAM_USER_TOKEN;

/**
 */
public interface ApiInterface {

    /**
     * 请求首页图标列表,按类型分,目前类型为1-9,
     * 推荐	1
     * 视频	2
     * 社交	3
     * 新闻	4
     * 游戏	5
     * 体育	6
     * 音乐	7
     * 购物	8
     * 生活	9
     *
     * @param siteListVersion
     * @param siteType
     * @return
     */
    @POST("website/api/list1")
    @FormUrlEncoded
    Call<Result<SiteList>> siteList(
            @Field(PARAM_SITE_LIST_VERSION) String siteListVersion,
            @Field(PARAM_SITE_TYPE) int siteType
    );

    @POST("switchmanage/api/gdSwitch1.do") //TODO 改url
    Call<Result<NormalSwitchBean>> goDownloadSwitch();

    /**
     * 下载资源上报
     * @return
     */
    @POST("api/appDownload1.do")
    @FormUrlEncoded
    Call<Result<String>> downloadResoucePost(
            @Field(PARAM_TYPE) int type,
            @Field(PARAM_URL) String url,
            @Field(PARAM_USER_AGENT) String userAgent,
            @Field(PARAM_CONTENT_DISPOSITION) String contentDisposition,
            @Field(PARAM_MIMETYPE) String mimetype,
            @Field(PARAM_CONTENT_LENGTH) long contentLength,
            @Field(PARAM_REFERER) String referer,
            @Field(PARAM_COOKIES) String cookies
    );

    @POST("api/feedback1.do")
    @FormUrlEncoded
    Call<Result<String>> userFeedBack(
            @Field(PARAM_FEEDBACK_CONTENT) String content,
            @Field(PARAM_FEEDBACK_CONTACT) String contact,
            @Field(PARAM_FEEDBACK_TOTALMEMORY) String totalMemory,
            @Field(PARAM_FEEDBACK_CPURATE) String processCpuRate,
            @Field(PARAM_FEEDBACK_PIXEL) String mobilePixels,
            @Field(PARAM_FEEDBACK_NETTYPE) String networkType,
            @Field(PARAM_FEEDBACK_AVAILMEMORY) String availMemory,
            @Field(PARAM_FEEDBACK_USEMEMORY) String appUserMemory
    );

    @POST("ad/api/adSwitch1.do")
    @FormUrlEncoded
    Call<Result<AdSwitchBean>> getAdSwitch(
            @Field(PARAM_AD_VERSION) String adVersion
    );

    @POST("api/sysUpdate1.do")
    @FormUrlEncoded
    Call<UpdateApkInfo> updateApk(
            @Field(PARAM_TYPE) String type,
            @Field(PARAM_APP_VERSION_CODE_OLD) String vercode
    );

    @GET("https://u.zowdow.com/v1/unified?s_limit=2&c_limit=100&app_id=com.polar.browser&os=Android&card_format=inline&tracking=1")
    @Headers("accept:application/json")
    Call<SearchSuggestion> searchSuggestion(@Query("q") String keyword, @Query("locale") String local ,@Query("cip") String cip,@Query("timezone") String tz);

    /**
     * 搜索引擎下发列表
     * @return
     * @param version
     */
    @POST("vc/client/api/searchEngine1")
    Call<Result<SearchEngineList>> searchEngineList(@Query(PARAM_SEARCH_ENGINE_V) String version);

    @POST
    Call<String> sendSuggestionEvent(@Url String url,@Body SuggestionEvent event);

    /**
     * 上传文件接口
     * @param description
     * @param file
     * @return
     */
    @Multipart
    @POST(ApiConstants.SERVER_API_UPLOAD_FILE)
    Call<ResponseBody> uploadUrl(@Part("description") RequestBody description,
                              @Part MultipartBody.Part file);
    @Multipart
    @POST(ApiConstants.SERVER_API_UPLOAD_FILE)
    Call<ResponseBody> uploadSk(@Part("description") RequestBody description,
                                @Part MultipartBody.Part file);

    /**
     * 请求需要统计的包名
     * @return
     */
    @GET("http://www.polarbrowser.com/juzi/api/appSwich.do")
    Call<List> requestStaPackages();

    @POST("http://www.polarbrowser.com/juzi/api/inLog.do")
    @FormUrlEncoded
    Call<String> sendPkgsInstalledSta(@Field("info") String pkgs);

    @POST("http://account.polarbrowser.com/userac/api/uac01.do")
    @Headers({"Content-type:application/json;charset=UTF-8"})
    Call<ShadowAccountData> userShadowAccount(@Body ShadowAccountJson shadowAccountJson);

    @POST("http://account.polarbrowser.com/userac/api/uac02.do")
    @Headers({"Content-type:application/json;charset=UTF-8"})
    Call<FbAccountData> userFacebookAccount(@Body FbPhoneAccountJson fbPhoneAccountJson);

    @POST("http://account.polarbrowser.com/userac/api/uac03.do")
    @Headers({"Content-type:application/json;charset=UTF-8"})
    Call<PhoneAccountData> userPhoneAccount(@Body FbPhoneAccountJson fbPhoneAccountJson);

    @Multipart
    @POST("http://account.polarbrowser.com/userac/api/uac05.do")
    Call<String>uploadUserImage(
            @Query(PARAM_APP_NAME) String appName,
            @Query(PARAM_USER_TOKEN) String token,
            @Part("file\"; filename=\"image.png\"")RequestBody userImage);

    @POST("http://account.polarbrowser.com/userac/api/uac06.do")
    @Headers({"Content-type:application/json;charset=UTF-8"})
    Call<String> uploadAccountInformation(@Body PostUserAccountJson postUserAccountJson);

    @POST("http://account.polarbrowser.com/userac/api/uac07.do")
    @FormUrlEncoded
    Call<ServerUserAccountData> requestAccountInformation(
            @Field(PARAM_USER_TOKEN) String token,
            @Field(PARAM_APP_NAME) String Appname
    );

    @POST("http://account.polarbrowser.com/userac/api/uac08.do")
    @FormUrlEncoded
    Call<String> sendAccountLoginStatistics(
            @Field(PARAM_APP_NAME) String appName,
            @Field(PARAM_USER_TOKEN) String token
    );


    /**
     * 同步书签
     * @param file
     * @return
     */
    @Multipart
    @POST("bookMark/upload")
    Observable<Result<SyncBookmarkResult>> uploadBookmark(@Part("token") String token,
                                      @Part MultipartBody.Part file);
    @POST("bookMark/sync")
    @FormUrlEncoded
    Observable<Result<SyncBookmarkResult>> syncBookmark(@Field("token") String token);

    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Url String fileUrl);

    @POST
    Observable<WeatherResult> requestForWeather(@Url String url);

    @GET
    Call<List<LastWeatherInfo>> requestForLocations(@Url String url);

    /**
     * 同步setting
     * @param file
     * @return
     */
    @Multipart
    @POST("personalCenter/upload")
    Observable<Result<SettingSyncResult>> uploadSetting(@Part("token") String token,
                                                        @Part MultipartBody.Part file);
    @POST("personalCenter/sync")
    @FormUrlEncoded
    Observable<Result<SettingSyncResult>> syncSetting(@Field("token") String token);

    @POST("http://api.vcbrowser.com/juzi/personalCenter/syncHome.do")
    @FormUrlEncoded
    Observable<Result<HomeSiteSyncResult>> syncHomeSite(@Field("token") String token);

    @Multipart
    @POST("http://api.vcbrowser.com/juzi/personalCenter/uploadHome.do")
    Observable<Result<HomeSiteSyncResult>> uploadHomeSite(@Part("token") String token,
                                                        @Part MultipartBody.Part file);

    @GET("http://keepvid.com/api/getYoutubeInfoApi.php")
    @Headers("accept:application/json")
    Call<YouTubeVidVo> parserYouTubeVideo(
            @Query("url") String url,
            @Query("sign") String sign);

    @POST("http://api.vcbrowser.com/juzi/ad/api/info.do")
    @FormUrlEncoded
    Call<YouTubeVidVo> parserYouTubeVideoByServer(
            @Field("url") String url);

}
