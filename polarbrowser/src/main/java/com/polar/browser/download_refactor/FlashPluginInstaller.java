package com.polar.browser.download_refactor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.download_refactor.dinterface.IDownloadObserver;

import java.io.File;
import java.util.ArrayList;

public class FlashPluginInstaller implements IDownloadObserver {

    public static final String macromediaPubRepo = "http://download.macromedia.com/pub/";
    public static final String macromediaPubRepo2 = "http://fpdownload.macromedia.com/pub/flashplayer/";
    // http://fpdownload.macromedia.com/pub/flashplayer/installers/archive/android/11.1.115.69/install_flash_player_ics.apk?type=cmb_flash
    // 20141105 added by zhangyuanqing
    public static final String key = "?type=cmb_flash";

    public static boolean isFlashPluginInstallUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (!url.endsWith(FlashPluginInstaller.key)) {
            return false;
        }
        if (url.startsWith(FlashPluginInstaller.macromediaPubRepo)) {
            return true;
        }
        if (url.startsWith(FlashPluginInstaller.macromediaPubRepo2)) {
            return true;
        }
        return false;
    }

    @Override
    public void handleDownloadLists(ArrayList<DownloadItemInfo> lists){
        
    }

    @Override
    public void handleDownloadItemAdded(boolean ret,long id,DownloadItemInfo info){
        
    }

    @Override
    public void handleDownloadItemRemoved(boolean ret,long[] id){
        
    }

    @Override
    public void handleDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes){
        
    }

    @Override
    public void handleDownloadStatus(long id, int status, int reason){
        
    }

    @SuppressLint("DefaultLocale")
	@Override
    public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval) {

        DownloadItemInfo info = DownloadManager.getInstance().getDownloadItem(id);
        if (info == null)
            return;

        if (info.mStatus != UiStatusDefine.STATUS_SUCCESSFUL)
            return;

        if (!info.mFilePath.endsWith(".apk"))
            return;

        String url = info.mUrl.toLowerCase();
        if (!url.contains("flash") && url.contains("player"))
            return;

        PackageManager pm = JuziApp.getInstance().getPackageManager();
        if (pm == null)
            return;
        PackageInfo pInfo = pm.getPackageArchiveInfo(info.mFilePath, PackageManager.GET_ACTIVITIES);
        if (pInfo == null)
            return;
        ApplicationInfo appInfo = pInfo.applicationInfo;
        if (appInfo == null)
            return;
        String packageName = appInfo.packageName.toLowerCase();
        if (packageName == null)
            return;
        if (packageName.endsWith(".flashplayer")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(info.mFilePath)),
                    "application/vnd.android.package-archive");
            JuziApp.getInstance().startActivity(intent);
            // UserBehaviorLogManager.onClick(UserLogConstants.FLASH_PLUGIN_INSTALLER,
            // UserLogConstants.FLASH_PLUGIN_INSTALLATION_REQUESTED);
        }
    }

/*TODO:     @Override
    public void handleDownloadItemRemoved(long... ids) {
//        if (!ArrayUtils.contains(ids, mDownloadId))
//            return;
    }

    @Override
    public void handleDownloadStatus(long id, int status, int reason) {
        if (id != mDownloadId)
            return;

        DownloadManager.getInstance().removeObserver(this);

        if (status != DownloadManager.STATUS_SUCCESSFUL)
            return;

        DownloadManager.DownloadItemInfo info = DownloadManager.getInstance().getDownloadItemInfo(mDownloadId);
        if (info == null)
            return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(info.mFilePath)), "application/vnd.android.package-archive");

        KApplication.getInstance().getTopActivity().startActivity(intent);
//        UserBehaviorLogManager.onClick(UserLogConstants.FLASH_PLUGIN_INSTALLER, UserLogConstants.FLASH_PLUGIN_INSTALLATION_REQUESTED);
    }

    @Override
    public void handleDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes) {
//        if (id != mDownloadId)
//            return;
    }

    @Override
    public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval) {
        
    }*/

}
