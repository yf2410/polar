package com.polar.browser.download_refactor;

import java.util.ArrayList;

public interface DownloadObserver{
    public void handleDownloadLists(ArrayList<DownloadItemInfo> lists);
    public void handleDownloadItemAdded(boolean ret, long id);
    public void handleDownloadItemRemoved(boolean ret, long id);
    public void handleDownloadStatus(long id, int status, int reason);
    public void handleDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes);
    public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval);
}