package com.polar.browser.download_refactor.dinterface;

public interface IProgressChange {
    public void onProgressChange(long id, long currentBytes, long totalBytes, long speedBytes);
}
