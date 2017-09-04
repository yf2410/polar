package com.polar.browser.download_refactor.ui;

import android.text.TextUtils;

import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.dinterface.IDownloadObserver;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/5.
 */

public class DownloadDataPersistence implements IDownloadObserver {
    @Override
    public void handleDownloadLists(ArrayList<DownloadItemInfo> lists) {
        SimpleLog.d("Download", "handleDownloadLists-");
    }

    @Override
    public void handleDownloadItemAdded(boolean ret, long id, DownloadItemInfo info) {
        Statistics.sendOnceStatistics(
                GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_START);
        SimpleLog.d("Download", "handleDownloadItemAdded-ret=="+ret+",id="+id);
        SimpleLog.d("Download", "handleDownloadItemAdded-DownloadItemInfo=="+info.toString());
    }

    @Override
    public void handleDownloadItemRemoved(boolean ret, long[] id) {

        SimpleLog.d("Download", "handleDownloadItemRemoved-ret="+ret);
    }

    @Override
    public void handleDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes) {

        SimpleLog.d("Download", "handleDownloadProgress-id="+id+",currentBytes="+currentBytes+",totalBytes=="+totalBytes+",speedBytes="+speedBytes);
    }
    @Override
    public void handleDownloadStatus(long id, int status, int reason) {
        SimpleLog.d("Download", "status=="+status+",id="+id+",reason="+reason);
        if (16 == status) {
            Statistics.sendOnceStatistics(
                    GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_FAILURE);
        }
        DownloadItemInfo info = DownloadManager.getInstance().getDownloadItem(id);
        if (info == null || TextUtils.isEmpty(info.mFilePath)){
            return;
        }
        try {
            String downloadDataDirPath;
            String name = new File(info.mFilePath).getName();
            downloadDataDirPath = VCStoragerManager.getInstance().getDownloadDataDirPath();


            if (downloadDataDirPath != null) {
                File file = new File(downloadDataDirPath);
                if (file.exists()) {
                    try {
                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(downloadDataDirPath + name + ".obj"));
                        out.writeObject(info);
                        out.close();
                        if(status==8){
                            ConfigManager.getInstance().notifyFileCountChanged(FileUtils.getFileType(name));
                            Statistics.sendOnceStatistics(
                                    GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_SUCCEED);
                            if (UrlUtils.matchYoutubeVideoUrl(info.mReferer)) {
                                Statistics.sendOnceStatistics(
                                        GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_DWN_SUCCEED);
                            }
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            SimpleLog.e(e);
        }
    }

    @Override
    public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval) {

        SimpleLog.d("Download", "handleDownloadVirusStatus——virusStatus="+virusStatus+",interval="+interval+",id="+id);
    }
}
