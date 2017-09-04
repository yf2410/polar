package com.polar.browser.impl;

import com.polar.browser.download.DownloadHelper;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IDownloadDelegate;
import com.polar.browser.utils.SimpleLog;

public class DownloadDelegateImpl implements IDownloadDelegate {

	@Override
	public void onDownloadStart(String pageUrl, String url, String cookies, String userAgent,
								String contentDisposition, String mimetype,
								long contentLength) {
		if (AppEnv.DEBUG) {
			SimpleLog.d("DownloadDelegateImpl", "pageUrl = " + pageUrl);
			SimpleLog.d("DownloadDelegateImpl", "url = " + url);
			SimpleLog.d("DownloadDelegateImpl", "userAgent = " + userAgent);
			SimpleLog.d("DownloadDelegateImpl", "contentDisposition = " + contentDisposition);
			SimpleLog.d("DownloadDelegateImpl", "mimetype = " + mimetype);
			SimpleLog.d("DownloadDelegateImpl", "contentLength = " + contentLength);
			SimpleLog.d("DownloadDelegateImpl", "cookies = " + cookies);
		}
		// url = http://appsearchcdn.baidu.com/appsearch-pkg/highdownload/Appsearch_16784199_1000364r?response-content-disposition=attachment;filename=AppSearch_7044921_180.apk
		// userAgent = Mozilla/5.0 (Linux; Android 4.4.2; SM-G9006V Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36
		// contentDisposition = attachment;filename="AppSearch_7044921_180.apk"
		// mimetype = application/vnd.android.package-archive
		// 封装在DonwloadHelper.download中，方便其他地方调用下载
		DownloadHelper.download(pageUrl, url, cookies, userAgent, contentDisposition, mimetype, contentLength);
	}
}
