package com.polar.browser.thread;

import android.content.Context;
import android.os.Looper;
import android.os.Message;

import com.polar.browser.base.BaseHandler;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.SimpleLog;

import org.apache.http.client.HttpClient;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 用于处理网络请求的Handler，工作在ThreadNetwork中
 *
 * @author dpk
 *         <p/>
 *         不要直接创建对象，需要通过ThreadManager来进行调用
 */
public class NetworkHandler extends BaseHandler {

	// 最大下载4MB的文件
	public static final int MAX_DOWNLOAD_SIZE = 4096 * 1024;

	public static final String TAG = "NetworkHandler";

	private static final int MSG_DOWNLOAD_FILE = 1;

	private static final int MSG_DELAY_DOWNLOAD_FILE = 2;

	private static NetworkHandler sInstance;

	private DownloadInfo mDelayedDownloadInfo;

	private NetworkHandler(Looper looper) {
		super(looper);
	}

	public static NetworkHandler getInstance(Looper looper) {
		if (sInstance == null) {
			sInstance = new NetworkHandler(looper);
		}
		return sInstance;
	}

	public static NetworkHandler getInstance() {
		return sInstance;
	}

	public void downloadFile(Context c, String url, File file) {
		downloadFile(c, url, file, null);
	}

	public void downloadFile(Context c, String url, File file,
							 IDownloadCallback callback) {
		DownloadInfo info = new DownloadInfo(c, url, file, callback);
		this.sendMessage(Message.obtain(this, MSG_DOWNLOAD_FILE, info));
	}

	public void downloadFileDelay(Context c, String url, File file,
								  IDownloadCallback callback, int milliseconds) {
		DownloadInfo info = new DownloadInfo(c, url, file, callback);
		mDelayedDownloadInfo = info;
		this.sendMessageDelayed(Message.obtain(this, MSG_DELAY_DOWNLOAD_FILE, info), milliseconds);
	}

	public void downloadFileImmediately(Context c) {
		if (mDelayedDownloadInfo != null) {
			SimpleLog.d(TAG, "downloadFileImmediately");
			this.sendMessage(Message.obtain(this, MSG_DOWNLOAD_FILE, mDelayedDownloadInfo));
			this.removeMessages(MSG_DELAY_DOWNLOAD_FILE);
		}
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_DOWNLOAD_FILE:
			case MSG_DELAY_DOWNLOAD_FILE:
				if (msg.what == MSG_DELAY_DOWNLOAD_FILE) {
					SimpleLog.d(TAG, "MSG_DELAY_DOWNLOAD_FILE");
				}
				DownloadInfo info = (DownloadInfo) msg.obj;
				if (info != null) {
					boolean result = downloadFileInner(info);
					if (info.callback != null) {
						info.callback.notifyResult(result);
					}
				}
				break;
			default:
				break;
		}
	}

	public void update() {
	}

	public void upload() {
	}

	private boolean downloadFileInner(DownloadInfo info) {
		SimpleLog.d(TAG, "Download started");
		int returnCode = 0;
		HttpClient httpClient = NetworkUtils.createHttpClient(NetworkUtils
				.getCurrentProxy(info.context));
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(info.file, true);
			returnCode = NetworkUtils.UrlDownloadToStream(httpClient, info.url,
					fos, 0, null, MAX_DOWNLOAD_SIZE, 0);
		} catch (Exception e) {
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}
		SimpleLog.d(TAG, "Download finished");
		if (returnCode <= 0) {
			info.file.delete();
		}
		return returnCode > 0;
	}

	public interface IDownloadCallback {
		void notifyResult(boolean result);
	}

	private class DownloadInfo {
		public Context context;
		public String url;
		public File file;
		public IDownloadCallback callback;
		public DownloadInfo(Context c, String url, File file,
							IDownloadCallback callback) {
			this.context = c;
			this.url = url;
			this.file = file;
			this.callback = callback;
		}
	}
}
