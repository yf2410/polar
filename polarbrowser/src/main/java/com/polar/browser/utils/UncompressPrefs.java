package com.polar.browser.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.util.Map;

public class UncompressPrefs {
	private static final String PREF_NAME = "uncompress";

	private static SharedPreferences pref;

	private static SharedPreferences.Editor editor;

	public static void initialize(Context context) {
//        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		// 跨进程实时读取~
		pref = context.getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
		editor = pref.edit();
	}

	public synchronized static boolean contains(String key) {
		return pref.contains(key);
	}

	public synchronized static boolean get(String key, boolean defValue) {
		return pref.getBoolean(key, defValue);
	}

	public synchronized static float get(String key, float defValue) {
		return pref.getFloat(key, defValue);
	}

	public synchronized static int get(String key, int defValue) {
		return pref.getInt(key, defValue);
	}

	public synchronized static long get(String key, long defValue) {
		return pref.getLong(key, defValue);
	}

	public synchronized static String get(String key, String defValue) {
		return pref.getString(key, defValue);
	}

	public synchronized static Map<String,?> getAll(){
		return pref.getAll();
	}

	public synchronized static int getAllCount(){
		try{
			return pref.getAll().size();
		}catch (NullPointerException e){
			return 0;
		}
	}

	public synchronized static void put(String filePath, boolean value) {
		//key就是保存的文件路径
		File originFile = new File(filePath);
		if(originFile.isDirectory()){  //解压目录
			editor.putBoolean(filePath, value);
			Log.d("prefs","dir put key = "+filePath);
		}else{  //解压源文件
			String uncompressDirPath =  filePath.contains(".") ? filePath.substring(0,filePath.lastIndexOf('.')) : filePath;  // 解压之后的目录名
			editor.putBoolean(uncompressDirPath, value);
			Log.d("prefs","file put key = "+uncompressDirPath);
		}
	}

	public synchronized static boolean commit() {
		return editor.commit();
	}

	public synchronized static void apply() {
		editor.apply();
	}
	public synchronized static void clear() {
		editor.clear();
	}

	public synchronized static void remove(String key) {
		if(key == null || key.isEmpty()) return;
		editor.remove(key);
	}
}
