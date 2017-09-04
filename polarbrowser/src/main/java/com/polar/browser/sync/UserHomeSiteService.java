package com.polar.browser.sync;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.library.rx.RetryWhenProcess;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.vclibrary.bean.HomeSiteSyncResult;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.rx.ResultObserver;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by yangfan on 2017/5/24.
 */

public class UserHomeSiteService extends IntentService {
    private Disposable mSyncDisposable;
    private Disposable mDownloadDisposable;
    private Disposable mUploadDisposable;

    public UserHomeSiteService() {
        super("UserHomeSiteService");
    }

    public static void startSyncService(Context context, int type, String userToken, String userID) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, UserHomeSiteService.class);
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
            case UserHomeSiteManager.SYNC_TYPE_LOGIN:
                syncHomeSiteData(userToken, userID);
                break;
            case UserHomeSiteManager.SYNC_TYPE_LOGOUT:
                syncUploadHomeSiteData(userToken, userID);
                break;
            case UserHomeSiteManager.SYNC_TYPE_EXIT:
                syncUploadHomeSiteData(userToken, userID);
                break;
            case UserHomeSiteManager.SYNC_TYPE_HOME:
                syncHomeSiteData(userToken, userID);
                break;
            default:
                break;
        }
    }

    private void syncUploadHomeSiteData(final String userToken, final String userID) {
        if (TextUtils.isEmpty(userToken) || !isAllowSync()) {
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
                        mSyncDisposable = disposable;
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

    private void syncHomeSiteData(final String userToken, final String userID) {
        if (TextUtils.isEmpty(userToken)) {
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
                        mSyncDisposable = disposable;
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

                        if (TextUtils.isEmpty(data.url) || serverTimeStamp == localTimeLong) { //服务器url为空 或者本地保存的时间戳为空，覆盖服务器
                            uploadHomeSiteData(userID, userToken);
                        } else if (serverTimeStamp > localTimeLong) {
                            downloadSetting(data, userID);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    private void downloadSetting(final HomeSiteSyncResult data, final String userID) {
        mDownloadDisposable = Api.getInstance().downloadFile(data.url)
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
                        System.out.println("--Mylog——" + "主页图标下载");
                        if (AccountLoginManager.getInstance().isUserLogined()) {
                            UserHomeSiteManager.getInstance().setHomeSiteSyncTime(userID, data.timeStamp);
                        }
                    }
                });
    }

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
                            mUploadDisposable = disposable;
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
                                System.out.println("--Mylog——" + "主页图标上传");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        if (mSyncDisposable != null) {
            mSyncDisposable.dispose();
        }
        if (mDownloadDisposable != null) {
            mDownloadDisposable.dispose();
        }
        if (mUploadDisposable != null) {
            mUploadDisposable.dispose();
        }
        super.onDestroy();
    }

    private boolean isAllowSync() {
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
}
