package com.polar.browser.download;

import android.content.Intent;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.bean.DownloadInfo;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.ConfigWrapper;

public class DownloadHelper {

	public static void downloadImg(String pageUrl, String url,boolean isNeedConfirm) {
		download(pageUrl, url, null, null, null, CommonData.MIME_TYPE_JPEG, 0L,isNeedConfirm);
	}

	public static void download(String pageUrl, String url, String cookies, String userAgent,
								String contentDisposition, String mimetype,
								long contentLength) {
		download(pageUrl,url,cookies,userAgent,contentDisposition,mimetype,contentLength,true);
	}

	public static void download(String pageUrl, String url, String cookies, String userAgent,
								String contentDisposition, String mimetype,
								long contentLength,boolean isNeedConfirm) {
		if (TextUtils.isEmpty(userAgent)) {
			userAgent = ConfigWrapper.get(CommonData.KEY_UA, null);
		}
		DownloadInfo info = new DownloadInfo();
		info.setUrl(url);
		info.setPageUrl(pageUrl);
		info.setCookies(cookies);
		info.setUserAgent(userAgent);
		info.setContentDisposition(contentDisposition);
		info.setMimetype(mimetype);
		info.setContentLength(contentLength);
		info.isNeedConfirm = isNeedConfirm;

		Intent intent = new Intent(JuziApp.getInstance(), DownloadService.class);
		intent.putExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, ConfigManager.getInstance().isEnableOnlyWifiDownload());
		intent.putExtra("DownloadInfo", info);
		if (TextUtils.equals(CommonData.MIME_TYPE_JPEG, mimetype)) {
			intent.putExtra("type", "image");
		}
		JuziApp.getInstance().startService(intent);
	}
}
