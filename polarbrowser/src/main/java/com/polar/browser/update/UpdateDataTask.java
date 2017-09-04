package com.polar.browser.update;

import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.bean.UpdateConfigDataInfo;
import com.polar.browser.bean.UpdateConfigInfo;
import com.polar.browser.common.api.RequestAPI;
import com.polar.browser.env.AppEnv;
import com.polar.browser.homepage.customlogo.JsonParser;
import com.polar.browser.i.IUpdateFileCallback;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.shortcut.ParseConfig;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.thread.NetworkHandler;
import com.polar.browser.thread.NetworkHandler.IDownloadCallback;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.network.api.ApiConstants;

import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;

/**
 * 用于处理数据文件升级的task，工作在network线程上
 * @author dpk
 * 每次BrowserActivity启动时，延时执行本task进行数据文件升级
 */
public class UpdateDataTask implements Runnable {

	private static final String TAG = "UpdateDataTask";

//	private static final String UPDATE_DATA_URL_NEW = (AppEnv.DEBUG ? RequestAPI.SERVER_API_ADDRESS_TEST : RequestAPI.SERVER_API_ADDRESS) + RequestAPI.CONFIG_DATA;
	private static final String UPDATE_DATA_URL_NEW = AppEnv.DEBUG ? "http://172.17.228.226/news/server/api/adblock.do?mp=android&type=dfupdate": (ApiConstants.SERVER_API_ADDRESS + RequestAPI.CONFIG_DATA);
//	private static final String UPDATE_DATA_URL_NEW = (RequestAPI.SERVER_API_ADDRESS + RequestAPI.CONFIG_DATA);
	private String mCv;
	private IUpdateFileCallback mCallback;

	public UpdateDataTask(IUpdateFileCallback callback) {
		mCallback = callback;
	}

	@Override
	public void run() {
		String result = downloadJsons();
		if (TextUtils.isEmpty(result)) {
			// 结果为空 返回
			return;
		} else {
			downloadFile(result);
		}
	}


	// 更换新接口下载json文件
	private String downloadJsons() {
		String check_version_url = Statistics.appendBasicStat(UPDATE_DATA_URL_NEW);
		check_version_url = Statistics.appendArg(check_version_url,
				Statistics.LANGUAGE, SystemUtils.getLan());
		mCv = String.valueOf(new Random().nextInt(50000));
		check_version_url = Statistics.appendArg(check_version_url,
				Statistics.CACHE_CHECK, mCv);
		BufferedReader in = null;
		String result = "";
		try {
			if (AppEnv.DEBUG) {
				SimpleLog.e(TAG, "updataDataTask=check_version_url==" + check_version_url);
			}
			URL theURL = new URL(check_version_url);
			URLConnection conn = theURL.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Charset", HTTP.UTF_8);
			conn.setConnectTimeout(10000);
			conn.connect();
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (MalformedURLException e) {
			SimpleLog.e(e);
		} catch (IOException e) {
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					SimpleLog.e(e);
				}
			}
		}
		return result;
	}

	/**
	 * 解析下发配置接口返回的Json
	 * @param result
     */
	private void downloadFile(String result) {
		try {
			UpdateConfigInfo updateConfigInfo = JsonParser.fromJson(result, UpdateConfigInfo.class);
			if (updateConfigInfo == null || TextUtils.isEmpty(updateConfigInfo.getCv()) || !updateConfigInfo.getCv().equals(mCv)) {
				return;
			}

			List<UpdateConfigDataInfo> updateConfigDataInfoList = updateConfigInfo.getData();
			if (updateConfigDataInfoList == null || 0 == updateConfigDataInfoList.size()) {
				return;
			}

			for (int i = 0; i < updateConfigDataInfoList.size(); i++) {
				UpdateConfigDataInfo updateConfigDataInfo = updateConfigDataInfoList.get(i);

				String file = updateConfigDataInfo.getFile();
				if (!TextUtils.isEmpty(file)) {
					String realPath = getRealPath(file);
					String md5 = SecurityUtil.getFileMD5(realPath);
					File realFile = new File(realPath);
					if (realFile.exists() && updateConfigDataInfo.getMd5().equals(md5)) {
						// 无需下载
					} else {
						String url = updateConfigDataInfo.getUrl();
						final String path = VCStoragerManager.getInstance().getDataPath() + "tmp" + String.valueOf(i);
						File files = new File(path);
						if (files.exists()) {
							FileUtils.deleteFileOrDirectory(files);
						}
						DownloadCallbackImpl callback = new DownloadCallbackImpl(updateConfigDataInfo, path);
						// 延迟10s下载图标
						if (realPath.contains(ParseConfig.SHORTCUT_ZIP_FILE)) {
							NetworkHandler.getInstance().downloadFileDelay(
									JuziApp.getAppContext(), url, files,
									callback, 10000);
						} else {
							NetworkHandler.getInstance().downloadFile(
									JuziApp.getAppContext(), url, files,
									callback);
						}
					}
				}
			}
		} catch (Exception e) {
			SimpleLog.e(e);
		}
	}

	// 根据相对path，获取文件的真实路经
	private String getRealPath(String path) {
		String realPath = JuziApp.getAppContext().getFilesDir().toString()
				+ File.separator + path;
		return realPath;
	}

	// 下载完成后的处理
	private class DownloadCallbackImpl implements IDownloadCallback {

		private UpdateConfigDataInfo mInfo;
		private String mPath;

		public DownloadCallbackImpl(UpdateConfigDataInfo updateConfigDataInfo, String path) {
			mInfo = updateConfigDataInfo;
			mPath = path;
		}

		@Override
		public void notifyResult(boolean result) {
			if (result) {
				String md5 = SecurityUtil.getFileMD5(mPath);
				try {
					if (md5.equals(mInfo.getMd5())) {
						InputStream inStream = new FileInputStream(mPath);
						String fileName = mInfo.getFile();
						String realPath = getRealPath(fileName);
						File file = new File(realPath);
						FileUtils.copyFile(inStream, file);
						FileUtils.deleteFileOrDirectory(new File(mPath));
						if (mCallback != null) {
							mCallback.notifyFileDownload(fileName, file);
						}
					}
				} catch (IOException e) {
					SimpleLog.e(e);
				} catch (Exception e) {
					SimpleLog.e(e);
				}
			}
		}
	}
}
