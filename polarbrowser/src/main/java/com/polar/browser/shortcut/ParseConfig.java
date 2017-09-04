package com.polar.browser.shortcut;

import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;
import com.polar.browser.utils.ZipUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

/**
 * 用于处理shortcut的配置文件
 *
 * @author dpk
 */
public class ParseConfig {
	// 下载的压缩文件名
	public static final String SHORTCUT_ZIP_FILE = "shortcut.dat";

	public static final String HASOFFER_ZIP = "hasoffer.zip";//比较插件脚本

	//比价插件
	public static final String FACEBOOK_IMG = "vc-upImgFb";
	public static final String HASOFFER = "hasoffer";
	public static final String VC_FBNOTI = "vc-fbNoti";
	public static final String VC_FBNOTI_JS = "vc-fbNoti.js";
	public static final String HASOFFER_JSON = "hasoffer.json";

	public static final String VC_ALBUMINS = "vc-albumIns";
	public static final String VC_ALBUMINS_JS = "vc-albumIns.js";
	public static final String VC_ALBUMINS_AVAILABLE_JS = "vc-albumInsAvailable.js";
	public static final String VC_ALBUMINS_ADBLOCK_JS = "vc-instagramAdblock.js";
	//Facebook登录页
	public static final String VC_FBLOGIN = "vc-fbLogin";
	public static final String VC_FBLOGIN_JS = "vc-fbLogin.js";
	// 解压后的目录
	public static final String SHORTCUT_FOLDER_PATH = "shortcut";
	// 解压后的目录==rawAdblock
	public static final String RAWADBLOCK_FOLDER_PATH = "rawAdblock";

	// config文件路径
	public static final String SHORTCUT_CONFIG_FILE_PATH = "config";
	public static final String VC_UPIMGINS = "vc-upImgIns";

    public static String sFolderPath = JuziApp.getAppContext().getFilesDir() + File.separator + SHORTCUT_FOLDER_PATH;

	public static String sFolderPaths = JuziApp.getAppContext().getFilesDir() + File.separator + RAWADBLOCK_FOLDER_PATH;

	public static String sPluginPath = JuziApp.getAppContext().getFilesDir() + File.separator;//插件存放目录

	private static List<DataNode> sDataList = new ArrayList<DataNode>();

	public static void unzipFile(File file) {
		try {
			File folder = new File(sFolderPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			ZipUtil.unZipFile(file, sFolderPath);
		} catch (ZipException e) {
			SimpleLog.e(e);
		} catch (IOException e) {
			SimpleLog.e(e);
		}
	}

	public static void unzipPluginFile(File file) {
		try {
			File folder = new File(sPluginPath);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			ZipUtil.unZipFile(file, sPluginPath);
			if (TextUtils.equals(file.getName(), HASOFFER_ZIP)) {
				parseHasOffer();
			}
		} catch (ZipException e) {
			SimpleLog.e(e);
		} catch (IOException e) {
			SimpleLog.e(e);
		}
	}

	/**
	 * Config文件格式
	 * title=xx url=1@@2@@3
	 * title=yy url=2
	 */
	public static void parseData() {
		File config = new File(sFolderPath + File.separator + SHORTCUT_CONFIG_FILE_PATH);
		if (!config.exists()) {
			unzipFile(new File(JuziApp.getAppContext().getFilesDir() + File.separator + SHORTCUT_ZIP_FILE));
		}
		if (config.exists()) {
			sDataList.clear();
			FileReader fr;
			try {
				fr = new FileReader(config);
				BufferedReader br = new BufferedReader(fr);
				String line;
				while ((line = br.readLine()) != null) {
					String node[] = line.split("\t");
					if (node.length == 2) {
						DataNode data = new DataNode();
						data.title = node[0];
						data.urls = node[1].split("@@");
						sDataList.add(data);
					}
				}
				br.close();
			} catch (FileNotFoundException e) {
				SimpleLog.e(e);
			} catch (IOException e) {
				SimpleLog.e(e);
			}
		}
	}

	/**
	 * 给定url，得到匹配结果
	 *
	 * @param url
	 * @return
	 */
	public static MatchResult getMatchResult(String url) {
		MatchResult result = new MatchResult();
		String host = UrlUtils.getHost(url);
		for (int i = 0; i < sDataList.size(); ++i) {
			DataNode data = sDataList.get(i);
			for (int j = 0; j < data.urls.length; ++j) {
				String matchUrl = data.urls[j];
				if (!matchUrl.startsWith("http")) {
					matchUrl = "http://" + matchUrl;
				}
				String matchHost = UrlUtils.getHost(matchUrl);
				if (matchHost.equals(host)) {
					result.id = i;
					if (matchUrl.equals(url)) {
						result.title = data.title;
						return result;
					}
				}
			}
		}
		return result;
	}

	private static void parseHasOffer() {
		try {
			String jsonPath = VCStoragerManager.getInstance().getHasOfferJsPath()
				+ File.separator + HASOFFER_JSON;
			String jstr = FileUtils.readFile(jsonPath);
			JSONObject object = new JSONObject(jstr);
			String hasoffer = object.getString("hasoffer");
			if (hasoffer != null) {
				String host = new JSONObject(hasoffer).getString("host");
				ConfigManager.getInstance().setHasofferPlugSupport(host);
				String status = new JSONObject(hasoffer).getString("switch");
				if (TextUtils.equals(status, "0")) {
					ConfigManager.getInstance().setServerHasofferEnabled(false);
				}else if(TextUtils.equals(status, "1")){
					ConfigManager.getInstance().setServerHasofferEnabled(true);
				}
			}else {
					ConfigManager.getInstance().setServerHasofferEnabled(false);
			}

		} catch (IOException | JSONException e) {
			ConfigManager.getInstance().setServerHasofferEnabled(false);
			e.printStackTrace();
		}
	}
}
