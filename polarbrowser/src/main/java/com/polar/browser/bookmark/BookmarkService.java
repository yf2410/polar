package com.polar.browser.bookmark;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.polar.browser.R;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.library.rx.RetryWhenProcess;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.SyncBookmarkResult;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.bean.events.SyncBookmarkEvent;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.rx.ResultObserver;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
 * Created by saifei on 17/4/6.
 * 加入一些rxjava 操作。
 */

public class BookmarkService extends IntentService {
    private static final String TAG = "BookmarkService";
    private static final String ACTION_SYNC = "com.polar.browser.service.syncbookmark";
    private static final String ACTION_LOGOUT_SYNC = "com.polar.browser.service.logout.syncbookmark";
    private static final String IS_MANUAL_SYNC = "isManualSync";
    private static final String TOKEN = "user_token";
    public static final String SERVER_SYNC_TIME = "serverSyncTime";

    private Disposable uploadDisposable;
    private Disposable downloadDisposable;
    private Disposable syncDisposable;

    public BookmarkService() {
        super("BookmarkService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            boolean isManualSync = intent.getBooleanExtra(IS_MANUAL_SYNC, false);
            String userToken = intent.getStringExtra(TOKEN);
            switch (intent.getAction()) {

                case ACTION_SYNC:
                    syncBookmark(isManualSync,intent.getStringExtra(SERVER_SYNC_TIME),userToken);
                    break;
                case ACTION_LOGOUT_SYNC:
                    logOutAndSync(userToken,intent.getStringExtra(SERVER_SYNC_TIME));
                    break;
            }

        }
    }

    /**
     *
     * @param isManualSync 是否手动同步，手动同步需要提示 成功或者失败。
     * @param isFirstUpLoad 是否是第一次上传书签，服务端如果没有书签，则上传本地书签。
     */
    private void uploadBookmark(final boolean isManualSync, boolean isFirstUpLoad, String userToken, final boolean isLogout) {
        if(!isManualSync && !isAllowSync()){
            //ConfigManager.getInstance().setSyncBookmarkTimeStamp("");
            if(isLogout){
                ConfigManager.getInstance().setSyncBookmarkTimeStamp("");
            }
            //这行代码有待考究
            ConfigManager.getInstance().setLoginSyncBookmarkFinished("true");
            return;
        }
        BookmarkManager.getInstance().prepareOnlineFile();
        if (isFirstUpLoad){//如果是用户第一次上传书签，则把本地书签拷贝到网络书签 然后上传网络书签。
            copyLocalToOnlineFile();
            BookmarkManager.getInstance().changeToOnlineFile();
        }
        final RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), new File(BookmarkManager.getInstance().onlineFilePath()));

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("myfile", BookmarkManager.getInstance().fileName(), requestFile);

        Api.getInstance().uploadBookmark(userToken, body)
                .filter(new Predicate<Result<SyncBookmarkResult>>() {
                    @Override
                    public boolean test(Result<SyncBookmarkResult> syncBookmarkResultResult) throws Exception {
                        if (syncBookmarkResultResult.getData() == null && isManualSync) {
                            CustomToastUtils.getInstance().showTextToast(R.string.sync_bookmark_failed);
                            sendStatis(GoogleConfigDefine.BOOKMARK_SYNC_FAILED,GoogleConfigDefine.BOOKMARK_MANUAL_SYNC_FAILED);
                        }else{
                            sendStatis(GoogleConfigDefine.BOOKMARK_SYNC_FAILED,GoogleConfigDefine.BOOKMARK_AUTO_SYNC_FAILED);
                        }
                        return syncBookmarkResultResult.getData()!=null;
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        uploadDisposable = disposable;
                    }
                }).retryWhen(new RetryWhenProcess(5))
               .subscribe(new ResultObserver<SyncBookmarkResult>() {
                @Override
                public void onError(Throwable e) {
                    SimpleLog.d(TAG, "=====上传书签失败======="+e.getMessage());
                    if(isManualSync){
                        RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_SYNC_FAILED));
                        sendStatis(GoogleConfigDefine.BOOKMARK_SYNC_FAILED,GoogleConfigDefine.BOOKMARK_MANUAL_SYNC_FAILED);
                    }else{
                        sendStatis(GoogleConfigDefine.BOOKMARK_SYNC_FAILED,GoogleConfigDefine.BOOKMARK_AUTO_SYNC_FAILED);
                    }

                    if(isLogout)
                        RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_LOGOUT_SYNC_FAILED));
                }

                @Override
                public void success(SyncBookmarkResult data) throws Exception {
                    if(!isLogout) {
                        ConfigManager.getInstance().setSyncBookmarkTimeStamp(data.getTimeStamp());
                    }
                    else {
                        ConfigManager.getInstance().setSyncBookmarkTimeStamp("");
                    }
                    SimpleLog.d(TAG, "=====上传书签成功=======");
                    sendStatis(GoogleConfigDefine.BOOKMARK_SYNC_SUCCESS,isManualSync?GoogleConfigDefine.BOOKMARK_MANUAL_SYNC_SUCCESS:GoogleConfigDefine.BOOKMARK_AUTO_SYNC_SUCCESS);
                    RxBus.get().post(new SyncBookmarkEvent(isManualSync?SyncBookmarkEvent.TYPE_MANUAL_SYNC_SUCCESS:SyncBookmarkEvent.TYPE_AUTO_SYNC_SUCCESS));

                    BookmarkManager.getInstance().changeToOnlineFile();
                }
        });

//        new Consumer<SyncBookmarkResult>() {
//            @Override
//            public void accept(SyncBookmarkResult syncBookmarkResult) throws Exception {
//                ConfigManager.getInstance().setSyncBookmarkTimeStamp(syncBookmarkResult.getTimeStamp());
//                SimpleLog.d(TAG, "=====上传书签成功=======");
//                if (isManualSync) {
//                    RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_UPLOAD_SUCCESS));
//                    CustomToastUtils.getInstance().showTextToast(R.string.sync_bookmark_success);
//                }
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                SimpleLog.d(TAG, "=====上传书签失败======="+throwable.getMessage());
//                if(isManualSync)
//                    CustomToastUtils.getInstance().showTextToast(R.string.sync_bookmark_failed);
//            }
//        }

    }

    private void copyLocalToOnlineFile() {
        InputStream input;
        try {
            input = new FileInputStream(BookmarkManager.getInstance().localFilePath());
            File dest = new File(BookmarkManager.getInstance().onlineFilePath());
            FileUtils.copyFile(input, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadBookmark(final SyncBookmarkResult result, final boolean isManualSync,final boolean isLogOut) {
         downloadDisposable = Api.getInstance().downloadFile(result.getUrl())
                .filter(new Predicate<ResponseBody>() {
                    @Override
                    public boolean test(ResponseBody responseBody) throws Exception {
                        if(isResponseValid(responseBody)){
                            return true;
                        }else{
                            if(isManualSync)
                                RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_SYNC_FAILED));
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
                        FileUtils.writeFile(inputStream, new File(BookmarkManager.getInstance().onlineFilePath()));
                        BookmarkManager.getInstance().changeToOnlineFile(); //切换文件，为网络书签文件
                        SimpleLog.d(TAG, "=====书签保存到本地成功=====");
                    }
                }).retryWhen(new RetryWhenProcess(5))
                .subscribe(new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream inputStream) throws Exception {
                        if(!isLogOut)
                            ConfigManager.getInstance().setSyncBookmarkTimeStamp(result.getTimeStamp());
                        BookmarkManager.getInstance().notifyChanged(false,false);
                        RxBus.get().post(new SyncBookmarkEvent(isManualSync?SyncBookmarkEvent.TYPE_MANUAL_SYNC_SUCCESS:SyncBookmarkEvent.TYPE_AUTO_SYNC_SUCCESS));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        SimpleLog.d(TAG, "=====书签下载失败====="+throwable.getMessage());
                        if(isManualSync)
                            RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_SYNC_FAILED));
                    }
                });

    }

    private boolean isResponseValid(ResponseBody responseBody) {
        return responseBody!=null&&responseBody.byteStream()!=null;
    }

    @Override
    public void onDestroy() {
        if (downloadDisposable != null)
            downloadDisposable.dispose();
        if (uploadDisposable != null)
            uploadDisposable.dispose();
        if(syncDisposable!=null)
            syncDisposable.dispose();
        SimpleLog.d(TAG,"BookmarkService onDestroy");
        BookmarkManager.getInstance().setIsSyncing(false);
        super.onDestroy();
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
                        syncDisposable = disposable;
                    }
                }).retryWhen(new RetryWhenProcess(2))//当网络错误时，2秒进行一次重试，一共进行三次重试
                .throttleFirst(500, TimeUnit.MILLISECONDS);//在每次事件触发后的一定时间间隔内丢弃新的事件。常用作去抖动过滤
    }

    private void logOutAndSync(String userToken, String serverSyncTime) {
        sendStatis(GoogleConfigDefine.BOOKMARK_AUTO_SYNC,GoogleConfigDefine.BOOKMARK_LOGOUT_SYNC);
        normalSync(userToken).subscribe(mResultObserver(false,serverSyncTime,userToken,true));
    }

    private synchronized void syncBookmark(final boolean isManualSync,String timeStamp,String userToken) {
        normalSync(userToken).subscribe(mResultObserver(isManualSync,timeStamp,userToken,false));
    }

    @NonNull
    private ResultObserver<SyncBookmarkResult> mResultObserver(final boolean isManualSync,final String localSaveSyncTime,final String userToken,final boolean isLogOut) {
        return new ResultObserver<SyncBookmarkResult>() {

            @Override
            @MainThread
            public void onError(Throwable e) {
                SimpleLog.e(TAG, "syncBookmark error==" + e.toString());
                if(isManualSync){
                    RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_SYNC_FAILED));
                    sendStatis(GoogleConfigDefine.BOOKMARK_SYNC_FAILED,GoogleConfigDefine.BOOKMARK_MANUAL_SYNC_FAILED);
                }

                if(isLogOut)
                    RxBus.get().post(new SyncBookmarkEvent(SyncBookmarkEvent.TYPE_LOGOUT_SYNC_FAILED));
            }

            @Override
            public void success(SyncBookmarkResult data) throws Exception {
                SimpleLog.d(TAG, "syncBookmark success==" + data.toString());
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

    public static void startSync(RxFragmentActivity context, boolean isManualSync,String userToken,String syncTime) {
        Intent intent = new Intent(context, BookmarkService.class);
        intent.setAction(BookmarkService.ACTION_SYNC);
        intent.putExtra(IS_MANUAL_SYNC, isManualSync);
        intent.putExtra(SERVER_SYNC_TIME, syncTime);
        intent.putExtra(TOKEN, userToken);
        context.startService(intent);
    }

    public static void startLogOutAndSync(RxFragmentActivity context, String token, String syncTime) {
        Intent intent = new Intent(context, BookmarkService.class);
        intent.setAction(BookmarkService.ACTION_LOGOUT_SYNC);
        intent.putExtra(TOKEN, token);
        intent.putExtra(SERVER_SYNC_TIME, syncTime);
        context.startService(intent);
    }

    public void sendStatis(String parentType,String type) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC,parentType,type);
    }

    private boolean isAllowSync() {
        boolean autoSyncSetting = ConfigWrapper.get(ConfigDefine.PERSONAL_CENTER_BOOKMARK, true);
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
