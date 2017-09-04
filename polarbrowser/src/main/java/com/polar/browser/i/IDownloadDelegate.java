package com.polar.browser.i;

public interface IDownloadDelegate {

	public void onDownloadStart(String pageUrl, String url, String cookies, String userAgent, String contentDisposition,
								String mimetype, long contentLength);
}
