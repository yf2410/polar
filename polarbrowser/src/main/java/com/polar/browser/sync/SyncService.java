package com.polar.browser.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.library.rx.RetryWhenProcess;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.HomeSiteSyncResult;
import com.polar.browser.vclibrary.bean.SettingSyncResult;
import com.polar.browser.vclibrary.bean.SyncBookmarkResult;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.bean.events.SyncBookmarkEvent;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.UserHomeSiteApi;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.rx.ResultObserver;

import org.w3c.dom.Text;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by yangfan on 2017/6/13.
 */

public class SyncService extends IntentService {

    private static final String TAG = "SyncService";
    /**
     * 用户登录帐号时
     */
    public static final int SYNC_TYPE_LOGIN = 0;
    /**
     * 用户退出帐号时
     */
    public static final int SYNC_TYPE_LOGOUT = 1;
    /**
     * 点退出按钮退出浏览器时
     */
    public static final int SYNC_TYPE_EXIT = 3;
    /**
     * 按home键退出浏览器
     */
    public static final int SYNC_TYPE_HOME = 4;

    /**
     * 浏览器设置Disposable
     */
    private Disposable mSettingSyncDisposable;
    private Disposable mSettingDownloadDisposable;
    private Disposable mSettingUploadDisposable;

    /**
     * 主页图标Disposable
     */
    private Disposable mHomeSiteSyncDisposable;
    private Disposable mHomeSiteDownloadDisposable;
    private Disposable mHomeSiteUploadDisposable;

    public SyncService() {
        super("SyncService");
    }

    public static void startSyncService(Context context, int type, String userToken, String userID) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(UserHomeSiteManager.KEY_SYNC_TYPE, type);
        intent.putExtra(UserHomeSiteManager.KEY_USER_TOKEN, userToken);
        intent.putExtra(UserHomeSiteManager.KEY_USER_ID, userID);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final int syncType = intent.getIntExtra(UserHomeSiteManager.KEY_SYNC_TYPE, UserHomeSiteManager.SYNC_TYPE_EXIT);
        final String userToken = intent.getStringExtra(UserHomeSiteManager.KEY_USER_TOKEN);
        final String userID = intent.getStringExtra(UserHomeSiteManager.KEY_USER_ID);
        switch (syncType) {
            case SYNC_TYPE_LOGIN:
                loginAndSync(userToken, userID);
                break;
            case SYNC_TYPE_LOGOUT:
                logoutAndSync(userToken, userID);
                break;
            case SYNC_TYPE_EXIT:
                exitAndSync(userToken, userID);
                break;
            case SYNC_TYPE_HOME:
                homeAndSync(userToken, userID);
                break;
            default:
                break;
        }
    }

    /**
     * home键的同步操作
     * @param userToken
     * @param userID
     */
    private void homeAndSync(String userToken, String userID) {
        //syncBookmark(userToken, userID);
        syncSetting(userToken, userID);
        syncHomeSite(userToken, userID);
    }

    /**
     * 退出浏览器的同步操作
     * @param userToken
     * @param userID
     */
    private void exitAndSync(String userToken, String userID) {
        if (TextUtils.isEmpty(userToken)) {
            return;
        }
        upDataHomeSite(userToken, userID);
        upDataSetting(userToken, userID);
        //UpDataBookmark(userToken, userID);
    }

    /**
     * 退出账号之后的同步操作
     * @param userToken
     * @param userID
     */
    private void logoutAndSync(String userToken, String userID) {
        if (TextUtils.isEmpty(userToken)) {
            return;
        }
        upDataHomeSite(userToken, userID);
        upDataSetting(userToken, userID);
        //UpDataBookmark(userToken, userID);
    }

    /**
     * 上传书签
     * @param userToken
     * @param userID
     */
    private void UpDataBookmark(String userToken, String userID) {

    }

    /**
     * 上传浏览器设置
     * @param userToken
     * @param userID
     */
    private void upDataSetting(final String userToken, final String userID) {
        Api.getInstance().syncSetting(userToken)
                .filter(new Predicate<Result<SettingSyncResult>>() {
                    @Override
                    public boolean test(Result<SettingSyncResult> settingSyncResultResult) throws Exception {
                        return settingSyncResultResult != null && settingSyncResultResult.getData() != null &&
                                !TextUtils.isEmpty(settingSyncResultResult.getData().timeStamp);
                    }
                })
                .doOnNext(new Consumer<Result<SettingSyncResult>>() {
                    @Override
                    public void accept(Result<SettingSyncResult> settingSyncResultResult) throws Exception {

                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mSettingSyncDisposable = disposable;
                    }
                })
                .retryWhen(new RetryWhenProcess(2))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new ResultObserver<SettingSyncResult>() {
                    @Override
                    public void success(SettingSyncResult data) throws Exception {
                        uploadSetting(userID, userToken);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    /**
     * 上传主页图标
     * @param userToken
     * @param userID
     */
    private void upDataHomeSite(final String userToken, final String userID) {
        if(!isAllowHomesiteSync()){
            UserHomeSiteManager.getInstance().setHomeSiteSyncTime(ConfigWrapper.get(ConfigDefine.USER_ID, ""), "");
            return;
        }
        Api.getInstance().syncHomeSite(userToken)
                .filter(new Predicate<Result<HomeSiteSyncResult>>() {
                    @Override
                    public boolean test(Result<HomeSiteSyncResult> homeSiteSyncResultResult) throws Exception {
                        return homeSiteSyncResultResult != null && homeSiteSyncResultResult.getData() != null &&
                                !TextUtils.isEmpty(homeSiteSyncResultResult.getData().timeStamp);
                    }
                })
                .doOnNext(new Consumer<Result<HomeSiteSyncResult>>() {
                    @Override
                    public void accept(Result<HomeSiteSyncResult> homeSiteSyncResultResult) throws Exception {
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mHomeSiteSyncDisposable = disposable;
                    }
                })
                .retryWhen(new RetryWhenProcess(2))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new ResultObserver<HomeSiteSyncResult>() {
                    @Override
                    public void success(HomeSiteSyncResult data) throws Exception {
                        uploadHomeSiteData(userID, userToken);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    /**
     * 登陆之后的同步操作
     * @param userToken
     * @param userID
     */
    private void loginAndSync(String userToken, String userID) {
        if (TextUtils.isEmpty(userToken)) {
            return;
        }
        //将本地浏览器设置以sp形式保存起来
        SettingSyncManager.getInstance().saveLocalSettingFile();
        //syncBookmark(userToken, userID);
        syncSetting(userToken, userID);
        syncHomeSite(userToken, userID);
    }

    /**
     * 主页图标同步
     * @param userToken
     * @param userID
     */
    private void syncHomeSite(final String userToken, final String userID) {
        Api.getInstance().syncHomeSite(userToken)
                .filter(new Predicate<Result<HomeSiteSyncResult>>() {
                    @Override
                    public boolean test(com.polar.browser.vclibrary.bean.base.Result<HomeSiteSyncResult> homeSiteSyncResultResult) throws Exception {
                        return homeSiteSyncResultResult != null && homeSiteSyncResultResult.getData() != null &&
                                !TextUtils.isEmpty(homeSiteSyncResultResult.getData().timeStamp);
                    }
                })
                .doOnNext(new Consumer<com.polar.browser.vclibrary.bean.base.Result<HomeSiteSyncResult>>() {
                    @Override
                    public void accept(com.polar.browser.vclibrary.bean.base.Result<HomeSiteSyncResult> homeSiteSyncResultResult) throws Exception {
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mHomeSiteSyncDisposable = disposable;
                    }
                })
                .retryWhen(new RetryWhenProcess(2))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new ResultObserver<HomeSiteSyncResult>() {
                    @Override
                    public void success(HomeSiteSyncResult data) throws Exception {
                        long serverTimeStamp = Long.parseLong(data.timeStamp);
                        long localTimeLong = 0;
                        String localSaveSyncTime = UserHomeSiteManager.getInstance().getHomeSiteSyncTime(userID);
                        if (!TextUtils.isEmpty(localSaveSyncTime)) {
                            localTimeLong = Long.parseLong(localSaveSyncTime);
                        }
                        // FIXME: 2017/6/3 当用户第一次登录时，会先上传本地文件，同时将本地文件中的数据插入用户表中
                        if(TextUtils.isEmpty(data.url)){
                            UserHomeSiteManager.getInstance().LocalToUserHomesite();
                        }
                        if (TextUtils.isEmpty(data.url) || serverTimeStamp == localTimeLong) { //服务器url为空 或者本地保存的时间戳相等，覆盖服务器
                            uploadHomeSiteData(userID, userToken);
                        } else if (serverTimeStamp > localTimeLong) {
                            downloadHomesiteData(data, userID);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    /**
     * 获取用户登录后的主页图标数据
     * @param data
     * @param userID
     */
    private void downloadHomesiteData(final HomeSiteSyncResult data, final String userID) {
        mHomeSiteDownloadDisposable = Api.getInstance().downloadFile(data.url)
                .filter(new Predicate<ResponseBody>() {
                    @Override
                    public boolean test(ResponseBody responseBody) throws Exception {
                        if (responseBody != null && responseBody.byteStream() != null) {
                            return true;
                        } else {
                            return false;
                        }

                    }
                })
                .map(new Function<ResponseBody, InputStream>() {
                    @Override
                    public InputStream apply(ResponseBody responseBody) throws Exception {
                        return responseBody.byteStream();
                    }
                })
                .doOnNext(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        FileUtils.writeFile(inputStream, new File(UserHomeSiteManager.getInstance().getOnlineHomesiteFile()));
                    }
                })
                .retryWhen(new RetryWhenProcess(5))
                .subscribe(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        UserHomeSiteManager.getInstance().applyOnlineSetting();
                        if (AccountLoginManager.getInstance().isUserLogined()) {
                            UserHomeSiteManager.getInstance().setHomeSiteSyncTime(userID, data.timeStamp);
                        }
                    }
                });
    }

    /**
     * 上传主页图标数据
     * @param userID
     * @param userToken
     */
    private void uploadHomeSiteData(final String userID, String userToken) {
        boolean success = UserHomeSiteManager.getInstance().createLocalHomeSite();
        if (success) {
            final RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), new File(UserHomeSiteManager.getInstance().getLocalHomesiteFile()));
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("myfile", UserHomeSiteManager.getInstance().getOnlineHomesiteFile(), requestFile);
            Api.getInstance().uploadHomeSite(userToken, body)
                    .filter(new Predicate<Result<HomeSiteSyncResult>>() {
                        @Override
                        public boolean test(Result<HomeSiteSyncResult> homeSiteSyncResultResult) throws Exception {
                            return homeSiteSyncResultResult.getData() != null;
                        }
                    })
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {
                            mHomeSiteUploadDisposable = disposable;
                        }
                    })
                    .retryWhen(new RetryWhenProcess(5))
                    .subscribe(new ResultObserver<HomeSiteSyncResult>() {
                        @Override
                        public void success(HomeSiteSyncResult data) throws Exception {
                            if (AccountLoginManager.getInstance().isUserLogined()) {
                                UserHomeSiteManager.getInstance().setHomeSiteSyncTime(userID, data.timeStamp);
                            } else {
                                UserHomeSiteManager.getInstance().setHomeSiteSyncTime(userID, "");
                                //将用户数据清空
                                UserHomeSiteApi userHomeSiteApi = UserHomeSiteApi.getInstance(CustomOpenHelper.getInstance(SyncService.this));
                                userHomeSiteApi.clear();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    /**
     * 浏览器设置同步
     * @param userToken
     * @param userID
     */
    private void syncSetting(final String userToken, final String userID) {
        Api.getInstance().syncSetting(userToken)
                .filter(new Predicate<Result<SettingSyncResult>>() {
                    @Override
                    public boolean test(Result<SettingSyncResult> settingSyncResultResult) throws Exception {
                        return settingSyncResultResult != null && settingSyncResultResult.getData() != null &&
                                !TextUtils.isEmpty(settingSyncResultResult.getData().timeStamp);
                    }
                })
                .doOnNext(new Consumer<Result<SettingSyncResult>>() {
                    @Override
                    public void accept(Result<SettingSyncResult> settingSyncResultResult) throws Exception {

                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mSettingSyncDisposable = disposable;
                    }
                })
                .retryWhen(new RetryWhenProcess(2))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new ResultObserver<SettingSyncResult>() {
                    @Override
                    public void success(SettingSyncResult data) throws Exception {
                        long serverTimeStamp = Long.parseLong(data.timeStamp);
                        long localTimeLong = 0;
                        String localSaveSyncTime = SettingSyncManager.getInstance().getSettingSyncTime(userID);
                        if (!TextUtils.isEmpty(localSaveSyncTime)) {
                            localTimeLong = Long.parseLong(localSaveSyncTime);
                        }
                        if (TextUtils.isEmpty(data.url) || serverTimeStamp == localTimeLong) { //服务器url为空 或者时间戳相同，覆盖服务器
                            uploadSetting(userID, userToken);
                        } else if (serverTimeStamp > localTimeLong) {
                            downloadSetting(data, userID);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    /**
     * 用户登录之后获取用户浏览器设置数据
     * @param data
     * @param userID
     */
    private void downloadSetting(final SettingSyncResult data, final String userID) {
        mSettingDownloadDisposable = Api.getInstance().downloadFile(data.url)
                .filter(new Predicate<ResponseBody>() {
                    @Override
                    public boolean test(ResponseBody responseBody) throws Exception {
                        if (responseBody != null && responseBody.byteStream() != null) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .map(new Function<ResponseBody, InputStream>() { //切换数据
                    @Override
                    public InputStream apply(ResponseBody responseBody) throws Exception {
                        return responseBody.byteStream();
                    }
                })
                .doOnNext(new Consumer<InputStream>() { //在通知给观察者之前 把文件保存到本地。
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        //把文件保存起来
                        FileUtils.writeFile(inputStream, new File(SettingSyncManager.getInstance().getOnlineSettingFile()));
                    }
                })
                .retryWhen(new RetryWhenProcess(5))
                .subscribe(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        SettingSyncManager.getInstance().applyOnlineSetting();
                        if (AccountLoginManager.getInstance().isUserLogined()) {
                            SettingSyncManager.getInstance().setSettingSyncTime(userID, data.timeStamp);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                });
    }

    /**
     * 书签同步
     * @param userToken
     * @param userID
     */
    private void syncBookmark(String userToken, String userID) {
        String syncBookmarkTimeStamp = ConfigManager.getInstance().getSyncBookmarkTimeStamp();
        normalSync(userToken).subscribe(mResultObserver(false,syncBookmarkTimeStamp ,userToken,false));
    }

    @NonNull
    private ResultObserver<SyncBookmarkResult> mResultObserver(final boolean isManualSync,final String localSaveSyncTime,final String userToken,final boolean isLogOut) {
        return new ResultObserver<SyncBookmarkResult>() {

            @Override
            @MainThread
            public void onError(Throwable e) {
                if(isManualSync){
                    RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_SYNC_FAILED));
                    sendStatis(GoogleConfigDefine.BOOKMARK_SYNC_FAILED,GoogleConfigDefine.BOOKMARK_MANUAL_SYNC_FAILED);
                }

                if(isLogOut)
                    RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_LOGOUT_SYNC_FAILED));
            }

            @Override
            public void success(SyncBookmarkResult data) throws Exception {
                long serverTimeStamp = Long.parseLong(data.getTimeStamp());
                long localTimeLong = 0;
                if (!TextUtils.isEmpty(localSaveSyncTime))
                    localTimeLong = Long.parseLong(localSaveSyncTime);

                if (TextUtils.isEmpty(data.getUrl()) || serverTimeStamp == localTimeLong) { //服务器url为空 或者时间戳相同，覆盖服务器书签
                    uploadBookmark(isManualSync, TextUtils.isEmpty(data.getUrl()),userToken,isLogOut);
                } else if (serverTimeStamp > localTimeLong) { //服务器书签为新，下载书签覆盖本地。
                    downloadBookmark(data, isManualSync,isLogOut);
                }

            }
        };
    }

    /**
     * 下载用户数据书签
     * @param data
     * @param isManualSync
     * @param isLogOut
     */
    private void downloadBookmark(SyncBookmarkResult data, boolean isManualSync, boolean isLogOut) {

    }

    /**
     * 上传书签
     * @param isManualSync
     * @param empty
     * @param userToken
     * @param isLogOut
     */
    private void uploadBookmark(boolean isManualSync, boolean empty, String userToken, boolean isLogOut) {

    }

    private synchronized Observable<Result<SyncBookmarkResult>> normalSync(String userToken) {
        if (TextUtils.isEmpty(userToken) ||
                BookmarkManager.getInstance().isSyncing()){
            SimpleLog.d(TAG,"书签正在同步，返回。。。");
            return Observable.empty();
        }
        BookmarkManager.getInstance().setIsSyncing(true);
        return Api.getInstance().syncBookmark(userToken)
                .filter(new Predicate<Result<SyncBookmarkResult>>() { //过滤空的
                    @Override
                    public boolean test(Result<SyncBookmarkResult> syncBookmarkResultResult) throws Exception {
                        return syncBookmarkResultResult != null && syncBookmarkResultResult.getData() != null &&
                                !TextUtils.isEmpty(syncBookmarkResultResult.getData().getTimeStamp());
                    }
                })
                .doOnNext(new Consumer<Result<SyncBookmarkResult>>() {
                    @Override
                    public void accept(Result<SyncBookmarkResult> syncBookmarkResultResult) throws Exception {
                        BookmarkManager.getInstance().prepareOnlineFile();//初始化网络书签文件。
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        //bookmarkSyncDisposable = disposable;
                    }
                }).retryWhen(new RetryWhenProcess(2))//当网络错误时，2秒进行一次重试，一共进行三次重试
                .throttleFirst(500, TimeUnit.MILLISECONDS);//在每次事件触发后的一定时间间隔内丢弃新的事件。常用作去抖动过滤



    }

    @Override
    public void onDestroy() {
        //浏览器设置
        if (mSettingSyncDisposable != null) {
            mSettingSyncDisposable.dispose();
        }
        if (mSettingDownloadDisposable != null) {
            mSettingDownloadDisposable.dispose();
        }
        if (mSettingUploadDisposable != null) {
            mSettingUploadDisposable.dispose();
        }

        //主页图标
        if (mHomeSiteSyncDisposable != null) {
            mHomeSiteSyncDisposable.dispose();
        }
        if (mHomeSiteDownloadDisposable != null) {
            mHomeSiteDownloadDisposable.dispose();
        }
        if (mHomeSiteUploadDisposable != null) {
            mHomeSiteUploadDisposable.dispose();
        }
        super.onDestroy();
    }

    /**
     * 是否同步主页图标
     * @return
     */
    private boolean isAllowHomesiteSync() {
        boolean autoSyncSetting = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_HOMEPAGE, true);
        if (!autoSyncSetting) {
            return false;
        }

        boolean onlyWifiSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_SYNC_IN_WIFI, true);
        int networkState = NetWorkUtils.getNetworkState(this);
        if (onlyWifiSync) {
            if (networkState == NetWorkUtils.NETWORN_WIFI) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 上传用户浏览器设置
     * @param userID
     * @param userToken
     */
    private void uploadSetting(final String userID, String userToken) {
        if (!isSyncSetting()) {
            boolean success = SettingSyncManager.getInstance().createNoUpdataSettingFile();
            if (success) {
                final RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), new File(SettingSyncManager.getInstance().getLocalSettingFile()));

                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("myfile", SettingSyncManager.getInstance().getOnlineSettingFile(), requestFile);
                Api.getInstance().uploadSetting(userToken, body)
                        .filter(new Predicate<Result<SettingSyncResult>>() {
                            @Override
                            public boolean test(Result<SettingSyncResult> settingSyncResultResult) throws Exception {
                                return settingSyncResultResult.getData() != null;
                            }
                        })
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                mSettingUploadDisposable = disposable;
                            }
                        })
                        .retryWhen(new RetryWhenProcess(5))
                        .subscribe(new ResultObserver<SettingSyncResult>() {

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void success(SettingSyncResult data) throws Exception {
                                if (AccountLoginManager.getInstance().isUserLogined()) {
                                    SettingSyncManager.getInstance().setSettingSyncTime(userID, data.timeStamp);
                                } else {
                                    SettingSyncManager.getInstance().setSettingSyncTime(userID, "");
                                    SettingSyncManager.getInstance().reflshLocalSetting();
                                }
                            }
                        });
            }
            return;
        }


        boolean success = SettingSyncManager.getInstance().createLocalSetting();
        if (success) {
            final RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), new File(SettingSyncManager.getInstance().getLocalSettingFile()));

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("myfile", SettingSyncManager.getInstance().getOnlineSettingFile(), requestFile);
            Api.getInstance().uploadSetting(userToken, body)
                    .filter(new Predicate<Result<SettingSyncResult>>() {
                        @Override
                        public boolean test(Result<SettingSyncResult> settingSyncResultResult) throws Exception {
                            return settingSyncResultResult.getData() != null;
                        }
                    })
                    .doOnSubscribe(new Consumer<Disposable>() {
                        @Override
                        public void accept(Disposable disposable) throws Exception {
                            mSettingUploadDisposable = disposable;
                        }
                    })
                    .retryWhen(new RetryWhenProcess(5))
                    .subscribe(new ResultObserver<SettingSyncResult>() {

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void success(SettingSyncResult data) throws Exception {
                            if (AccountLoginManager.getInstance().isUserLogined()) {
                                SettingSyncManager.getInstance().setSettingSyncTime(userID, data.timeStamp);
                            } else {
                                SettingSyncManager.getInstance().setSettingSyncTime(userID, "");
                                SettingSyncManager.getInstance().reflshLocalSetting();
                            }
                        }
                    });
        }
    }

    /**
     * 判断浏览器设置开关是否打开
     * @return
     */
    private boolean isSyncSetting() {
        boolean autoSyncSetting = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BROWSER_SETTING, true);
        if (!autoSyncSetting) {
            return false;
        }

        boolean onlyWifiSync = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_SYNC_IN_WIFI, true);
        int networkState = NetWorkUtils.getNetworkState(this);
        if (onlyWifiSync) {
            if (networkState == NetWorkUtils.NETWORN_WIFI) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public void sendStatis(String parentType,String type) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC,parentType,type);
    }
}
