package com.polar.browser.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.manager.VCStoragerManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public final class SDCardUtils {

	/**
	 * SD卡是否存在
	 *
	 * @return
	 */
	public static boolean ExistSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * SD卡剩余空间
	 *
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getSDFreeSize() {
		long total = 0;
		String[] paths = VCStoragerManager.getInstance().getStorageDirectorys();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				File f = new File(paths[i]);
				boolean canWrite = f.canWrite();
				SimpleLog.e("", "paths[" + i + "] == " + paths[i]);
				if (canWrite) {
					total += f.getFreeSpace();
				}
			}
		}
		return total;
//		// 取得SD卡文件路径
//		File path = Environment.getExternalStorageDirectory();
//		StatFs sf = new StatFs(path.getPath());
//		// 获取单个数据块的大小(Byte)
//		long blockSize = sf.getBlockSize();
//		// 空闲的数据块的数量
//		long freeBlocks = sf.getAvailableBlocks();
//		// 返回SD卡空闲大小
//		return freeBlocks * blockSize; // 单位Byte
	}

	public static String[] getStorageDirectorys() {
		StorageManager sm = (StorageManager) JuziApp.getAppContext().getSystemService(Context.STORAGE_SERVICE);
		// 获取sdcard的路径：外置和内置
		try {
			//3.0以上可以通过反射获取
			String[] paths = (String[]) sm.getClass()
					.getMethod("getVolumePaths", new Class<?>[]{}).invoke(sm, new Object[]{});
			return paths;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * SD卡总容量
	 *
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long getSDAllSize() {
		long total = 0;
		String[] paths = getStorageDirectorys();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				File f = new File(paths[i]);
				boolean canWrite = f.canWrite();
				SimpleLog.e("", "paths[" + i + "] == " + paths[i]);
				if (canWrite) {
					total += f.getTotalSpace();
				}
			}
		}
		return total;
//		// 取得SD卡文件路径
//		File path = Environment.getExternalStorageDirectory();
//		StatFs sf = new StatFs(path.getPath());
//		// 获取单个数据块的大小(Byte)
//		long blockSize = sf.getBlockSize();
//		// 获取所有数据块数
//		long allBlocks = sf.getBlockCount();
//		// 返回SD卡大小
//		return allBlocks * blockSize; // 单位Byte
	}


	@SuppressWarnings("deprecation")
	public static long getCurrentStorageFreeSize() {
		long total = 0;
		String[] paths = VCStoragerManager.getInstance().getStorageDirectorys();
		String downloadDirPath = VCStoragerManager.getInstance().getDownloadDirPath();
		if (paths != null) {
			for (int i = 0; i < paths.length; i++) {
				File f = new File(paths[i]);
				boolean canWrite = f.canWrite();
				SimpleLog.e("", "paths[" + i + "] == " + paths[i]);
				if (canWrite) {
					if (!TextUtils.isEmpty(downloadDirPath) && downloadDirPath.startsWith(paths[i])) {
						return f.getFreeSpace();
					}
				}
			}
		}
		return total;
	}
}
