package com.polar.browser.download_refactor.dinterface;

public interface IScanVirus {
    public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval);
}
