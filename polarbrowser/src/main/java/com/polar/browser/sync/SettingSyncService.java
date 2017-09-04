package com.polar.browser.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.library.rx.RetryWhenProcess;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.vclibrary.bean.SettingSyncResult;
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

public class SettingSyncService extends IntentService {

    private Disposable mSyncDisposable;
    private Disposable mDownloadDisposable;
    private Disposable mUploadDisposable;

    public SettingSyncService() {
        super("SettingSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /*if (!isAllowSync()) {
            return;
        }*/
        final int syncType = intent.getIntExtra(SettingSyncManager.KEY_SYNC_TYPE, SettingSyncManager.SYNC_TYPE_EXIT);
        final String userToken = intent.getStringExtra(SettingSyncManager.KEY_USER_TOKEN);
        final String userID = intent.getStringExtra(SettingSyncManager.KEY_USER_ID);

        switch (syncType) {
            case SettingSyncManager.SYNC_TYPE_LOGIN:
                //保存本地文件
                System.out.println("--settting-- 保存本地文件");
                SettingSyncManager.getInstance().saveLocalSettingFile();
                syncSetting(userToken, userID);
                break;
            case SettingSyncManager.SYNC_TYPE_LOGOUT:
                syncSettingOthers(userToken, userID);
                break;
            case SettingSyncManager.SYNC_TYPE_EXIT:
                syncSettingOthers(userToken, userID);
                break;
            case SettingSyncManager.SYNC_TYPE_HOME:
                syncSetting(userToken, userID);
                break;
            default:
                break;
        }
    }

    private boolean isAllowSync() {
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

    private void uploadSetting(final String userID, String userToken) {
        if (!isAllowSync()) {
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
                                mUploadDisposable = disposable;
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
                            mUploadDisposable = disposable;
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

    private void downloadSetting(final SettingSyncResult data, final String userID) {
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

    private void syncSetting(final String userToken, final String userID) {
        if (TextUtils.isEmpty(userToken)) {
            return;
        }
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
                        mSyncDisposable = disposable;
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
                        System.out.println("--settting-- 用户数据serverTimeStamp==" + serverTimeStamp + "本地数据localSaveSyncTime==" + localSaveSyncTime);
                        if (!TextUtils.isEmpty(localSaveSyncTime)) {
                            localTimeLong = Long.parseLong(localSaveSyncTime);
                        }
                        if (TextUtils.isEmpty(data.url) || serverTimeStamp == localTimeLong) { //服务器url为空 或者时间戳相同，覆盖服务器
                            uploadSetting(userID, userToken);
                        } else if (serverTimeStamp > localTimeLong) {
                            System.out.println("--setting--   下载用户数据  ");
                            downloadSetting(data, userID);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void syncSettingOthers(final String userToken, final String userID) {
        if (TextUtils.isEmpty(userToken)) {
            return;
        }
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
                        mSyncDisposable = disposable;
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

    public static void startSyncService(Context context, int type, String userToken, String userID) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, SettingSyncService.class);
        intent.putExtra(SettingSyncManager.KEY_SYNC_TYPE, type);
        intent.putExtra(SettingSyncManager.KEY_USER_TOKEN, userToken);
        intent.putExtra(SettingSyncManager.KEY_USER_ID, userID);
        context.startService(intent);
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
}
