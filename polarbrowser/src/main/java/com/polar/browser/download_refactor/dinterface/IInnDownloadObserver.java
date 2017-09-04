package com.polar.browser.download_refactor.dinterface;

public interface IInnDownloadObserver extends IDownloadObserver,IContinuingStatusChange{
    public void startPreUnfinishedDownloads();
}
