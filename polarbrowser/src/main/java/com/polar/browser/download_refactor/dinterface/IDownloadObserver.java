package com.polar.browser.download_refactor.dinterface;

import com.polar.browser.download_refactor.DownloadItemInfo;

import java.util.ArrayList;


public interface IDownloadObserver extends IScanVirus{
    public void handleDownloadLists(ArrayList<DownloadItemInfo> lists);
    public void handleDownloadItemAdded(boolean ret, long id, DownloadItemInfo info);
    public void handleDownloadItemRemoved(boolean ret, long[] id);
    public void handleDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes);
    public void handleDownloadStatus(long id, int status, int reason);
}