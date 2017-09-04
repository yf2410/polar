package com.polar.browser.update;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.polar.browser.R;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.CustomToastUtils;

import java.io.File;

/**
 * Created by FKQ on 2016/8/10.
 */

public class DownloadApkService extends Service{

    private String downloadMd5;
    private long mEnqueueId;
    private DownloadCompleteReceiver mCompleteReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static final String DOWNLOAD_URL = "download_url";
    public static final String DOWNLOAD_MD5 = "download_md5";
    public static final String DOWNLOAD_FOLDER_NAME = "download";
    public  static final String DOWNLOAD_FILE_NAME = "vcbrowser.apk";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String downloadUrl = intent.getExtras().getString(DOWNLOAD_URL);
            downloadMd5 = intent.getExtras().getString(DOWNLOAD_MD5);

            if (downloadUrl != null) {
                // 注册下载完成广播
                mCompleteReceiver = new DownloadCompleteReceiver();
                registerReceiver(mCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                // 下载
//                new Thread(new DownloadRunable(downloadUrl)).start();
//                ThreadPoolUtils.execute(new DownloadRunable(downloadUrl));
                ThreadManager.postTaskToIOHandler(new DownloadRunable(downloadUrl));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 下载
    class DownloadRunable implements Runnable{
        private String mDownloadUrl;

        public DownloadRunable(String url) {
            mDownloadUrl = url;
        }

        @Override
        public void run() {
            startDownload();
        }

        // 开始下载
        private void startDownload(){

            ThreadManager.postTaskToUIHandler(new Runnable() {
                @Override
                public void run() {
                    CustomToastUtils.getInstance().showTextToast(getString(R.string.update_download_tip));
                }
            });
            try {
                File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!folder.exists() || !folder.isDirectory()) {
                    folder.mkdirs();
                }
            } catch (Exception e) {

            }

            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mDownloadUrl));
            request.setMimeType("application/vnd.android.package-archive");
            // 存储的目录
            request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, DOWNLOAD_FILE_NAME);
            // 设定在 WIFI 流量 情况都下载
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setTitle(getResources().getString(R.string.downloading));
            request.setVisibleInDownloadsUi(true);
            mEnqueueId = manager.enqueue(request);
        }
    }

    // 下载完成
    class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (intent.getAction() == DownloadManager.ACTION_DOWNLOAD_COMPLETE && completeDownloadId == mEnqueueId) {
//                checkStatus(context, completeDownloadId);
                String apkFilePath = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath())
                        .append(File.separator).append(Environment.DIRECTORY_DOWNLOADS).append(File.separator)
                        .append(DownloadApkService.DOWNLOAD_FILE_NAME).toString();

                String md5 = SecurityUtil.getFileMD5(apkFilePath);
                if (!TextUtils.isEmpty(md5) &&
                        !TextUtils.isEmpty(downloadMd5) && downloadMd5.equalsIgnoreCase(md5)){
                    installApk(context, apkFilePath);
                }

                // 停止下载Service
                DownloadApkService.this.stopSelf();
            }
        }

        // 安装APk
        private  void installApk(Context context, String file) {
            File apkFile = new File(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mCompleteReceiver);
        super.onDestroy();
    }
}
