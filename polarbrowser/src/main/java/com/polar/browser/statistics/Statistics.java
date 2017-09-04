package com.polar.browser.statistics;

import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.polar.browser.JuziApp;
import com.polar.browser.common.api.RequestAPI;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.network.ResultCallback;
import com.polar.browser.vclibrary.network.api.Api;

import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 统计类，用于处理与统计有关的事情
 *
 * @author dpk
 */
public class Statistics {
	private static final String APP_VERSION = "ver";
	private static final String OS_VERSION = "os";
	private static final String MACHINE_MODEL = "mmod";
	private static final String MACHINE_ID = "mid";
	private static final String CPU = "cpu";
	private static final String MEMORY = "mem";
	public static final String SD = "sd";
	private static final String OTHER_BROWSERS = "bro";
	private static final String SAFE_APP = "safe";
	private static final String MARKET_APP = "mk";
	private static final String SCREEN_RESOLUTION = "sr";
	private static final String DEFAULT_BROWSER = "dbr";
	public static final String CACHE_CHECK = "cv";
	public static final String COMPRESS_SWITCHER = "cs";
	public static final String LANGUAGE = "lan";
	private static final String AREA = "area";
	private static final String VERSION_CODE = "vercode";
	private static final String TYPE = "type";
	private static final String MP = "mp";
	private static final String APPUPDATE = "appupdate";
	private static final String ANDROID = "android";

	//首页加载more定义字段
	private static final String MORE_ANDROID = "a";
	private static final String MORE_MCC = "c";

	//首页加载Web版卡片字段
	private static final String HOME_CARD_MCC = "mcc";
	private static final String HOME_CARD_LOCAL_SWITCH = "ncs";
	private static final String TAG = "Statistics";


	/**
	 * url添加最基本的统计字段
	 *
	 * @param baseUrl 其中cid默认使用首次安装渠道号
	 * @return
	 */
	public static String appendBasicStat(String baseUrl) {
		return appendBasicStat(baseUrl, false);
	}

	/**
	 * 升级APP-url添加最基本的字段
	 *
	 * @param baseUrl 其中cid默认使用首次安装渠道号
	 * @return
	 */
	public static String appendAppUpdateUrl(String baseUrl) {
		return appendAppUpdateUrl(baseUrl, false);
	}

	/**
	 * url添加最基本的统计字段
	 *
	 * @param baseUrl
	 * @param isCurrentCid:是否使用当前渠道号。如果不使用当前渠道号，默认使用首次安装渠道号
	 * @return
	 */
	public static String appendBasicStat(String baseUrl, boolean isCurrentCid) {
		String url = baseUrl;
		String ver = getArgEncode(SystemUtils.getVersionName(JuziApp.getAppContext()));
		String os = getArgEncode(SystemUtils.getOSVersion());
		String mmod = getArgEncode(SystemUtils.getModel());
		String mid = getArgEncode(SystemUtils.getMid(JuziApp.getAppContext()));

		url = appendArg(url, APP_VERSION, ver);
		url = appendArg(url, OS_VERSION, os);
		url = appendArg(url, MACHINE_MODEL, mmod);
		url = appendArg(url, MACHINE_ID, mid);
		return url;
	}

	/**
	 * 升级App—url添加最基本字段
	 *
	 * @param baseUrl
	 * @param isCurrentCid:是否使用当前渠道号。如果不使用当前渠道号，默认使用首次安装渠道号
	 * @return
	 */
	public static String appendAppUpdateUrl(String baseUrl, boolean isCurrentCid) {
		String url = baseUrl;
		String type = getArgEncode(APPUPDATE);
		String mp = getArgEncode(ANDROID);
		String ver = getArgEncode(SystemUtils.getVersionName(JuziApp.getAppContext()));
		String os = getArgEncode(SystemUtils.getOSVersion());
		String mmod = getArgEncode(SystemUtils.getModel());
		String mid = getArgEncode(SystemUtils.getMid(JuziApp.getAppContext()));
		String lan = getArgEncode(SystemUtils.getLan());
		String area = getArgEncode(SystemUtils.getArea());
		String vercode = getArgEncode(String.valueOf(SysUtils.getAppVersionCode()));
		url = appendArg(url, MP, mp);
		url = appendArg(url, TYPE, type);
		url = appendArg(url, APP_VERSION, ver);
		url = appendArg(url, VERSION_CODE, vercode);
		url = appendArg(url, LANGUAGE, lan);
		url = appendArg(url, AREA, area);
		url = appendArg(url, OS_VERSION, os);
		url = appendArg(url, MACHINE_MODEL, mmod);
		url = appendArg(url, MACHINE_ID, mid);
		return url;
	}

	/**
	 * url增加某个参数
	 *
	 * @param url
	 * @param argName
	 * @param argValue
	 * @return
	 */
	public static String appendArg(String url, String argName, String argValue) {
		return url += "&" + argName + "=" + argValue;
	}

	public static String appendFirstArg(String url, String argName, String argValue) {
		return url += argName + "=" + argValue;
	}

	public static String appendSpecialArg(String url, String argName, String argValue) {
		return url += "#" + argName + "=" + argValue;
	}

	/**
	 * @author FKQ
	 * @time 2016/11/7 14:59
	 *
	 * facebook deepLink
	 *
	 * @param url
	 * @param argName
	 * @param argValue
     * @return
     */
	public static String appendFbDpLinkArg(String url, String argName, String argValue) {
		return url += argName + "=" + argValue;
	}

	/**
	 * 获得编码后的参数
	 *
	 * @param argValue
	 * @return
	 */
	public static String getArgEncode(String argValue) {
		String argEncoded = argValue;
		try {
			argEncoded = URLEncoder.encode(argValue, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		return argEncoded;
	}

	/**
	 * url增加高级统计字段信息
	 *
	 * @param baseUrl
	 * @return
	 */
	public static String appendAdvancedStat(String baseUrl) {
		String url = baseUrl;
		String cpu = getArgEncode(String.valueOf(SysUtils.getNumCores()));
		String mem = getArgEncode(SysUtils.getTotalMemoryType(JuziApp
				.getAppContext()));
		String sd = getArgEncode(SysUtils.getSDSizeType());
		String sr = getArgEncode(String.valueOf(SysUtils
				.getScreenResolutionType()));
		String bro = getArgEncode(String.valueOf(SysUtils
				.getBrowserInstalledValue(JuziApp.getAppContext())));
		String mk = getArgEncode(String.valueOf(SysUtils
				.getMarketInstalledValue(JuziApp.getAppContext())));
		String safe = getArgEncode(String.valueOf(SysUtils
				.getSafeAppInstalledValue(JuziApp.getAppContext())));
		String dbr = getArgEncode(String.valueOf(SysUtils
				.getDefaultBrowserValue(JuziApp.getAppContext())));
		url = appendArg(url, CPU, cpu);
		url = appendArg(url, MEMORY, mem);
		url = appendArg(url, SD, sd);
		url = appendArg(url, SCREEN_RESOLUTION, sr);
		url = appendArg(url, OTHER_BROWSERS, bro);
		url = appendArg(url, MARKET_APP, mk);
		url = appendArg(url, SAFE_APP, safe);
		url = appendArg(url, DEFAULT_BROWSER, dbr);
		return url;
	}

	public static void sendOnceStatistics(String key, String value) {
		try {
		    HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
		    eventBuilder.setCategory(key);
		    eventBuilder.setAction(value);
			JuziApp.getInstance().getDefaultTracker().send(eventBuilder.build());
			if (AppEnv.DEBUG) {
				SimpleLog.d("GoogleStatistics", "key=="+key+",value="+value);
			}
		} catch (Throwable e) {
		}

	}

	public static void sendOnceStatistics(String key, String value, String tag) {
		try {
			HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
			eventBuilder.setCategory(key);
			eventBuilder.setAction(value);
			eventBuilder.setLabel(tag);
			JuziApp.getInstance().getDefaultTracker().send(eventBuilder.build());
			if (AppEnv.DEBUG) {
				SimpleLog.d("GoogleStatistics", "key=="+key+",value="+value+",tag="+tag);
			}
		} catch (Throwable e) {
		}
	}

	/**
	 * 发送AppsFlyer打点统计
	 * 实时发送（尽量少用）
	 *
	 * @param tag
	 * @author FKQ
	 * @time 2016/11/5 0:17
	 */
//	public static void sendAppsFlyerStatistics(String tag) {
//		AppsFlyerLib.getInstance().trackEvent(JuziApp.getAppContext(), tag, null);
//	}

	/**
	 * 发送强制推出log
	 */
	public static void sendForceExitStatistics() {
		// log force exit
		boolean forceExit = ConfigManager.getInstance().isForceExit();
		if (forceExit) {
			Statistics.sendOnceStatistics(GoogleConfigDefine.BROWSER_EXIT, GoogleConfigDefine.BROWSER_EXIT_FORCE);
		}
		ConfigManager.getInstance().setForceExit(true);
	}

	/**
	 * 首页More按钮加载网址导航页
	 * @return
     */
	public static String getLoadMoreUrl() {
		String url = RequestAPI.LOAD_HOME_MORE;
		String mp = getArgEncode(MORE_ANDROID);
		String mcc = getArgEncode(SystemUtils.getMCC(JuziApp.getAppContext()));
		String lan = getArgEncode(SystemUtils.getLan());

		url = appendFirstArg(url, MP, mp);
		url = appendArg(url, MORE_MCC, mcc);
		url = appendArg(url, LANGUAGE, lan);
		return url;
	}

	/**
	 * 设置页产品关于介绍页
	 * @return
	 */
	public static String getLoadHelpUrl() {
		String url = RequestAPI.LOAD_PRODUCT_HELP;
		String mp = getArgEncode(MORE_ANDROID);
		String mcc = getArgEncode(SystemUtils.getMCC(JuziApp.getAppContext()));
		String lan = getArgEncode(SystemUtils.getLan());

		url = appendFirstArg(url, MP, mp);
		url = appendArg(url, MORE_MCC, mcc);
		url = appendArg(url, LANGUAGE, lan);
		return url;
	}

	/**
	 * @return
	 */
	public static String getHomeWebCradUrl() {
		String url = AppEnv.DEBUG ? RequestAPI.LOAD_HOME_WEB_CARD_TEST : RequestAPI.LOAD_HOME_WEB_CARD;
		String mp = getArgEncode(ANDROID);
		String ver = getArgEncode(SystemUtils.getVersionName(JuziApp.getAppContext()));
		String mmod = getArgEncode(SystemUtils.getModel());
		String os = getArgEncode(SystemUtils.getOSVersion());
		String mid = getArgEncode(SystemUtils.getMid(JuziApp.getAppContext()));
		String lan = getArgEncode(SystemUtils.getLan());
		String area = getArgEncode(SystemUtils.getArea());
		String mcc = getArgEncode(SystemUtils.getMCC(JuziApp.getAppContext()));
		String vercode = getArgEncode(String.valueOf(SysUtils.getAppVersionCode()));
		String cv = getArgEncode(String.valueOf(new Random().nextInt(50000)));

		url = appendFirstArg(url, MP, mp);
		url = appendArg(url, APP_VERSION, ver);
		url = appendArg(url, MACHINE_MODEL, mmod);
		url = appendArg(url, OS_VERSION, os);
		url = appendArg(url, MACHINE_ID, mid);
		url = appendArg(url, LANGUAGE, lan);
		url = appendArg(url, AREA, area);
		url = appendArg(url, HOME_CARD_MCC, mcc);
		url = appendArg(url, VERSION_CODE, vercode);
		url = appendArg(url, CACHE_CHECK, cv);
		return url;
	}

	/**
	 * 发送下载资源统计
	 * @param type 1.下载;2.播放;
	 * @param url
	 * @param userAgent
	 * @param contentDisposition
	 * @param mimetype
	 * @param contentLength
	 * @param referer
     * @param cookies
     */
	public static void sendDownloadResourceStatistics(int type, String url, String userAgent,
													  String contentDisposition, String mimetype,
													  long contentLength, String referer, String cookies) {
		// 上报下载数据
		Api.getInstance().downloadResoucePost(type, url, userAgent, contentDisposition, mimetype, contentLength, referer, cookies)
				.enqueue(new ResultCallback<String>() {
					@Override
					public void success(String data, Call<Result<String>> call, Response<Result<String>> response) {
						if (AppEnv.DEBUG) {
							SimpleLog.d("Statistics", "Statistics - sendDownloadResourceStatistics - success");
						}
					}
					@Override
					public void error(Call<Result<String>> call, Throwable t) {
						if (AppEnv.DEBUG) {
							SimpleLog.d("Statistics", "Statistics - sendDownloadResourceStatistics - error");
						}
					}
				});
		if (AppEnv.DEBUG) {
			SimpleLog.d("Statistics", "type==" + type + ", url=" + url + ", contentDisposition=" + contentDisposition);
		}
	}

	public static void procStaJobs() {
		staAppInstalled();
		uploadStaFiles();
	}

	private static void uploadStaFiles() {
		File url_dir = new File(VCStoragerManager.getInstance().getTmpPath());
		File[] files = url_dir.listFiles();
		if (files == null || files.length < 1) {
			return;
		}
		for (final File file : files) {
			if (ConfigManager.getInstance().isUploadUrl() && TextUtils.equals(file.getName(), Constants.URLS_FILE_NAME)) {
//                NetworkUtils.uploadUrlFileWithDel(file, true);
				FileUtils.uploadFile(file, CommonData.ADDR_URL, JuziApp.getAppContext());
			}else if (ConfigManager.getInstance().isUploadSk() && TextUtils.equals(file.getName(), Constants.SK_FILE_NAME)) {
//                NetworkUtils.uploadSkFileWithDel(file, true);
				FileUtils.uploadFile(file, CommonData.ADDR_SK, JuziApp.getAppContext());
			}
		}
	}

	private static void staAppInstalled() {
		long currentTime = System.currentTimeMillis();
		if (!SysUtils.isSameDay(currentTime, ConfigManager.getInstance().getLastStaTime())) {
			ConfigManager.getInstance().setStaPkgsTime(currentTime);
			Api.getInstance().requestStaPackages().enqueue(new Callback<List>() {

				private Object[] pkgs;

				@Override
				public void onResponse(Call<List> call, Response<List> response) {
					if (response.isSuccessful()) {
						pkgs = response.body().toArray();
						List<String> pkgList = new ArrayList<>();
						if (pkgs.length > 0) {
							for (Object pkg : pkgs) {
								String pkgStr = (String)pkg;
								if (SysUtils.isPackageInstalled(JuziApp.getAppContext(), pkgStr)) {
									pkgList.add(pkgStr);
								}
							}
						}
						if (pkgList.size() > 0) {
							postResult(pkgList);
						}
					}
				}

				@Override
				public void onFailure(Call<List> call, Throwable t) {

				}
			});
		}
	}

	/**
	 *
	 * @param pkgList
	 */
	private static void postResult(List<String> pkgList) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < pkgList.size(); i++) {
			stringBuilder.append(pkgList.get(i));
			if (i < pkgList.size()-1) {
				stringBuilder.append(";");
			}
		}
		Api.getInstance().sendPkgsInstalledSta(stringBuilder.toString()).enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				SimpleLog.d(TAG, "post pkg sta data suc!!!");
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				SimpleLog.d(TAG, "post pkg sta data error!!!");

			}
		});
	}
}
