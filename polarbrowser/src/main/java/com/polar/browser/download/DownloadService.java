package com.polar.browser.download;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.bean.DownloadInfo;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.handler.DownloadHandler;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SimpleLog;

import static com.polar.browser.common.data.CommonData.ACTION_FILE_COUNT_CHANGED;

public class DownloadService extends Service {

    String TAG = "DownloadService";
    private static final int GRAY_SERVICE_ID = 1001;
    private FileCountChangedReceiver fileCountChangedReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        fileCountChangedReceiver = new FileCountChangedReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION_FILE_COUNT_CHANGED);
        registerReceiver(fileCountChangedReceiver, intentFilter);
        SimpleLog.e(TAG, "DownloadService onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SimpleLog.e(TAG, "DownloadService onStartCommand");
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
        }
        if (intent != null) {
            // 仅wifi下载开关是否开启
            if (intent.hasExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD)) {
                DownloadManager.getInstance().isOnlyWifiDownload = intent.getBooleanExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, true);
                SimpleLog.e("DownloadService", "DownloadService -- isOnlyWifiDownload --- " + DownloadManager.getInstance().isOnlyWifiDownload);
            }
            boolean isFirstRun = intent.getBooleanExtra("isFirstRun", false);
            // 20160823 延后1秒刷新磁盘上下载数据,为数据表的创建留出时间
            if (isFirstRun) {
                ThreadManager.postDelayedTaskToIOHandler(new Runnable() {
                    @Override
                    public void run() {
                        FileUtils.getVideoFileName();
                    }
                }, 1000);
                return START_NOT_STICKY;
            }

            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getSerializable("DownloadInfo") != null) {
                DownloadInfo info = (DownloadInfo) bundle.getSerializable("DownloadInfo");
                String type = bundle.getString("type");
                if (info != null) {
                    if (TextUtils.equals("image", type)) {
                        DownloadHandler.getInstance().onDownloadImage(JuziApp.getInstance(), info.url, info.userAgent, info.pageUrl, info.mimetype, info.cookies, false,info.isNeedConfirm);
                    } else {
                        DownloadHandler.getInstance().onDownloadStart(JuziApp.getInstance(), info.url, info.userAgent,
                                info.contentDisposition, info.mimetype, info.contentLength, info.pageUrl, info.cookies, false);
                    }
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        SimpleLog.e(TAG, "DownloadService onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        SimpleLog.e(TAG, "DownloadService onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        SimpleLog.e(TAG, "DownloadService onDestroy");
        unregisterReceiver(fileCountChangedReceiver);
        super.onDestroy();
    }

    class FileCountChangedReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_FILE_COUNT_CHANGED)) {
                String changedFileType = intent.getStringExtra(CommonData.KEY_CHANGED_FILE_TYPE);
                if(Constants.TYPE_ALL.equals(changedFileType)){
                    QueryUtils.queryCount(DownloadService.this);
                    return;
                }
                ConfigManager.getInstance().notifyFileCountChanged(changedFileType);
            }
        }
    }

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

    }

}
