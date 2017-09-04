package com.polar.browser.custom_logo;

import java.io.File;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.StringUtils;
import com.polar.browser.utils.UrlUtils;


public final class LogoBitmapUtils {
	
	/**
	 * 获取单个logo的路径
	 * @param host
	 * @return
	 */
	public static String getLogoPath(String host){
		return JuziApp.getInstance().getFilesDir().toString() + File.separator + CommonData.LOGO_DIR_NAME + File.separator + host;
	}

	public static String getLogoPathByName (String name) {
		return JuziApp.getInstance().getFilesDir().toString() + File.separator + CommonData.LOGO_DIR_NAME + File.separator + name;
	}
	
	/**
	 * 获取存放logo文件的目录路径
	 * @return
	 */
	public static String getLogoDirPath(){
		return JuziApp.getInstance().getFilesDir().toString() + File.separator + CommonData.LOGO_DIR_NAME + File.separator;
	}
	
	/**
	 * 根据传入的url和bm对象   存储bm到本地   ICON_DIR_NAME文件夹
	 * @param url
	 * @param bm
	 */
	public static void saveLogoBitmapToFile(final String dirName,final String url ,final String text, final Bitmap bm){
		
		final String host = UrlUtils.getHost(url);
    	final String path = String.format("%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
    			dirName);
    	
    	Runnable r = new Runnable() {

			@Override
			public void run() {
				//host为空的时候要为文件设置一个文件名，现在是计算url的MD5，针对用户自定义添加的情况，可能获取不到host。
				if (TextUtils.isEmpty(host)) {
//					host = SecurityUtil.getMD5(url);
					FileUtils.saveBitmapToFile(bm, path, SecurityUtil.getMD5(url)+text);
				}else {
					FileUtils.saveBitmapToFile(bm, path, host+text);
				}
			}
    	};
    	
    	ThreadManager.postTaskToIOHandler(r);
	}
	
	public static void deleteDrawedBitmapByUrl(String dirName,String url ,String title) {
		final String name = UrlUtils.getHost(url)+ StringUtils.getFirstChar(title);
    	final String path = String.format("%s/%s", JuziApp.getAppContext().getFilesDir().toString(),
    			dirName);
    	
    	Runnable r = new Runnable() {

			@Override
			public void run() {
				
				if (TextUtils.isEmpty(name)) {
					File file = new File(path, name);
					FileUtils.deleteOnlyFile(file);
				}
			}
    	};
    	
    	ThreadManager.postTaskToIOHandler(r);
	}
	
}
