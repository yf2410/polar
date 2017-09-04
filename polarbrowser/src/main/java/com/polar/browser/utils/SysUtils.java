package com.polar.browser.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.Window;
import android.view.WindowManager;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.vclibrary.common.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 获取系统信息
 *
 * @author dpk
 */
public class SysUtils {

	// 竞品的包名
	public static final String UC_BROWSER_PACKAGE = "com.UCMobile";
	public static final String QQ_BROWSER_PACKAGE = "com.tencent.mtt";
	public static final String QQ_BROWSER_PACKAGE2 = "com.tencent.qbx";
	public static final String LIEBAO_BROWSER_PACKAGE = "com.ijinshan.browser_fast";
	public static final String SOGOU_BROWSER_PACKAGE = "sogou.mobile.explorer";
	public static final String SYSTEM_BROWSER_PACKAGE = "com.android.browser";
	// 应用市场的包名
	public static final String QIHU360_MARKET_PACKAGE = "com.qihoo.appstore";
	public static final String BAIDU_MARKET_PACKAGE = "com.baidu.appsearch";
	public static final String BAIDU91_MARKET_PACKAGE = "com.dragon.android.pandaspace";
	public static final String TENCENT_MARKET_PACKAGE = "com.tencent.android.qqdownloader";
	public static final String WANDOUJIA_MARKET_PACKAGE = "com.wandoujia.phoenix2";
	public static final String TAOBAO_MARKET_PACKAGE = "com.taobao.appcenter";
	// 安全软件的包名
	public static final String QIHU360_SAFE_PACKAGE = "com.qihoo360.mobilesafe";
	public static final String TENCENT_SAFE_PACKAGE = "com.tencent.qqpimsecure";
	public static final String BAIDU_SAFE_PACKAGE = "cn.opda.a.phonoalbumshoushou";
	public static final String LBE_SAFE_PACKAGE = "com.lbe.security";
	public static final String JINSHAN_SAFE_PACKAGE = "com.ijinshan.mguard";
	// 默认浏览器类型
	public static final int DEFAULT_BROWSER_NONE = 0;
	public static final int DEFAULT_BROWSER_UC = 1;
	public static final int DEFAULT_BROWSER_QQ = 2;
	public static final int DEFAULT_BROWSER_LIEBAO = 3;
	public static final int DEFAULT_BROWSER_SOGOU = 4;
	public static final int DEFAULT_BROWSER_SYSTEM = 5;
	public static final int DEFAULT_BROWSER_OTHER = 6;
	public static final int DEFAULT_BROWSER_MOMENG = 7;
	// 内存容量类型
	public static final int MEMORY_BELOW_512MB = 1;
	public static final int MEMORY_512_1024MB = 2;
	public static final int MEMORY_1024_2048MB = 3;
	public static final int MEMORY_2048_3072MB = 4;
	public static final int MEMORY_ABOVE_3072MB = 5;
	// sd卡容量类型
	public static final int SD_BELOW_512MB = 1;
	public static final int SD_512_1024MB = 2;
	public static final int SD_1024_4096MB = 3;
	public static final int SD_4096_8192MB = 4;
	public static final int SD_8192_16384MB = 5;
	public static final int SD_16384_32768MB = 6;
	public static final int SD_32768_65536MB = 7;
	public static final int SD_64GB_128GB = 8;
	public static final int SD_ABOVE_128GB = 9;
	// cpu核类型
	public static final int CPU_CORE_1 = 1;
	public static final int CPU_CORE_2 = 2;
	public static final int CPU_CORE_4 = 3;
	public static final int CPU_CORE_8 = 4;
	public static final int CPU_CORE_ABOVE_8 = 5;
	private static final String TAG = "SysUtils";
	private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
	/**
	 * 判断某台设备是否为低配机
	 *
	 * @param c
	 * @return
	 */
	private static boolean isInitDeviceType = false;
	private static boolean isLowLevelDevice = false;

	/**
	 * 返回当前的进程名
	 *
	 * @return
	 */
	public static String getCurrentProcessName() {
		FileInputStream in = null;
		try {
			String fn = "/proc/self/cmdline";
			in = new FileInputStream(fn);
			byte[] buffer = new byte[256];
			int len = 0;
			int b;
			while ((b = in.read()) > 0 && len < buffer.length) {
				buffer[len++] = (byte) b;
			}
			if (len > 0) {
				String s = new String(buffer, 0, len, "UTF-8");
				return s;
			}
		} catch (Throwable e) {
			if (AppEnv.DEBUG) {
				SimpleLog.e(TAG, e.getMessage() + e);
			}
		} finally {
			FileUtils.closeSilently(in);
		}
		return null;
	}

	/**
	 * 获得CPU名字
	 *
	 * @return
	 */
	public static String getCpuName() {
		try {
			FileReader fr = new FileReader("/proc/cpuinfo");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			br.close();
			return array[1];
		} catch (Exception e) {
		}
		return "";
	}

	/**
	 * 获得系统内存
	 *
	 * @param c
	 * @return
	 */
	public static String getTotalMemory(Context c) {
		long totalMem = getTotalMemoryByte(c);

		String result = Formatter.formatFileSize(c, totalMem);
		return result;
	}

	/**
	 * 获得内存容量的类型
	 *
	 * @param c
	 * @return
	 */
	public static String getTotalMemoryType(Context c) {
		long totalMem = getTotalMemoryByte(c) / 1024 / 1024;
		int type = 0;
		if (totalMem < 512) {
			type = MEMORY_BELOW_512MB;
		} else if (totalMem >= 512 && totalMem < 1024) {
			type = MEMORY_512_1024MB;
		} else if (totalMem >= 1024 && totalMem < 2048) {
			type = MEMORY_1024_2048MB;
		} else if (totalMem >= 2048 && totalMem < 3192) {
			type = MEMORY_2048_3072MB;
		} else if (totalMem >= 3192) {
			type = MEMORY_ABOVE_3072MB;
		}
		return String.valueOf(type);
	}

	/**
	 * 获得内存容量，以字节计算
	 *
	 * @param c
	 * @return
	 */
	private static long getTotalMemoryByte(Context c) {
		long totalMem = 0;
		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			arrayOfString = str2.split("\\s+");
			totalMem = Long.valueOf(arrayOfString[1]).longValue() * 1024;
			localBufferedReader.close();
		} catch (Exception e) {
		}
		return totalMem;
	}

	/**
	 * 获取sd卡总空间
	 *
	 * @return
	 */
	public static String getSDSize() {
		return FileUtils.formatFileSize(SDCardUtils.getSDAllSize());
	}

	/**
	 * 获得sd卡的容量类型
	 *
	 * @return
	 */
	public static String getSDSizeType() {
		long sdSize = SDCardUtils.getSDAllSize() / 1024 / 1024;
		int type = 0;
		if (sdSize < 512) {
			type = SD_BELOW_512MB;
		} else if (sdSize >= 512 && sdSize < 1024) {
			type = SD_512_1024MB;
		} else if (sdSize >= 1024 && sdSize < 4096) {
			type = SD_1024_4096MB;
		} else if (sdSize >= 4096 && sdSize < 8192) {
			type = SD_4096_8192MB;
		} else if (sdSize >= 8192 && sdSize < 16384) {
			type = SD_8192_16384MB;
		} else if (sdSize >= 16384 && sdSize < 32768) {
			type = SD_16384_32768MB;
		} else if (sdSize >= 32768 && sdSize < 65536) {
			type = SD_32768_65536MB;
		} else if (sdSize >= 65536 && sdSize < 131072) {
			type = SD_64GB_128GB;
		} else if (sdSize >= 131072) {
			type = SD_ABOVE_128GB;
		}
		return String.valueOf(type);
	}

	/**
	 * 获取应用版本号
	 *
	 * @return
	 */
	public static int getAppVersionCode() {
		int vercode = 0;
		PackageInfo info;
		try {
			info = JuziApp.getInstance().getPackageManager()
					.getPackageInfo(JuziApp.getInstance().getPackageName(), 0);
			vercode = info.versionCode;
		} catch (NameNotFoundException e) {
			SimpleLog.e(e);
		}
		return vercode;
	}

	/**
	 * 获得分辨率信息
	 *
	 * @return
	 */
	public static int getScreenResolutionType() {
		int max = AppEnv.SCREEN_HEIGHT > AppEnv.SCREEN_WIDTH ? AppEnv.SCREEN_HEIGHT
				: AppEnv.SCREEN_WIDTH;
		int min = AppEnv.SCREEN_HEIGHT < AppEnv.SCREEN_WIDTH ? AppEnv.SCREEN_HEIGHT
				: AppEnv.SCREEN_WIDTH;
		if (max == 480 && min == 320) {
			return CommonData.SCREEN_RESOLUTION_480_320;
		} else if (max == 800 && min == 480) {
			return CommonData.SCREEN_RESOLUTION_800_480;
		} else if (max == 854 && min == 480) {
			return CommonData.SCREEN_RESOLUTION_854_480;
		} else if (max == 960 && min == 540) {
			return CommonData.SCREEN_RESOLUTION_960_540;
		} else if (max == 1184 && min == 720) {
			return CommonData.SCREEN_RESOLUTION_1184_720;
		} else if (max == 1280 && min == 720) {
			return CommonData.SCREEN_RESOLUTION_1280_720;
		} else if (max == 1280 && min == 800) {
			return CommonData.SCREEN_RESOLUTION_1280_800;
		} else if (max == 1920 && min == 1080) {
			return CommonData.SCREEN_RESOLUTION_1920_1080;
		} else if (max >= 2000) {
			return CommonData.SCREEN_RESOLUTION_2K;
		} else {
			return CommonData.SCREEN_RESOLUTION_OTHER;
		}
	}

	/**
	 * 判断某个软件是否安装了
	 *
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isPackageInstalled(Context context, String packageName) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);
		} catch (Exception e) {
			packageInfo = null;
		}
		return packageInfo != null;
	}

	/**
	 * 获取竞品浏览器安装的情况
	 *
	 * @param c
	 * @return
	 */
	public static int getBrowserInstalledValue(Context c) {
		int value = 0;
		if (isPackageInstalled(c, UC_BROWSER_PACKAGE)) {
			value += 1;
		}
		if (isPackageInstalled(c, QQ_BROWSER_PACKAGE)
				|| isPackageInstalled(c, QQ_BROWSER_PACKAGE2)) {
			value += 1 << 1;
		}
		if (isPackageInstalled(c, LIEBAO_BROWSER_PACKAGE)) {
			value += 1 << 2;
		}
		if (isPackageInstalled(c, SOGOU_BROWSER_PACKAGE)) {
			value += 1 << 3;
		}
		return value;
	}

	/**
	 * 获取应用市场安装的情况
	 *
	 * @param c
	 * @return
	 */
	public static int getMarketInstalledValue(Context c) {
		int value = 0;
		if (isPackageInstalled(c, QIHU360_MARKET_PACKAGE)) {
			value += 1;
		}
		if (isPackageInstalled(c, BAIDU_MARKET_PACKAGE)) {
			value += 1 << 1;
		}
		if (isPackageInstalled(c, BAIDU91_MARKET_PACKAGE)) {
			value += 1 << 2;
		}
		if (isPackageInstalled(c, TENCENT_MARKET_PACKAGE)) {
			value += 1 << 3;
		}
		if (isPackageInstalled(c, WANDOUJIA_MARKET_PACKAGE)) {
			value += 1 << 4;
		}
		if (isPackageInstalled(c, TAOBAO_MARKET_PACKAGE)) {
			value += 1 << 5;
		}
		return value;
	}

	/**
	 * 获取安全软件安装的情况
	 *
	 * @param c
	 * @return
	 */
	public static int getSafeAppInstalledValue(Context c) {
		int value = 0;
		if (isPackageInstalled(c, QIHU360_SAFE_PACKAGE)) {
			value += 1;
		}
		if (isPackageInstalled(c, TENCENT_SAFE_PACKAGE)) {
			value += 1 << 1;
		}
		if (isPackageInstalled(c, BAIDU_SAFE_PACKAGE)) {
			value += 1 << 2;
		}
		if (isPackageInstalled(c, LBE_SAFE_PACKAGE)) {
			value += 1 << 3;
		}
		if (isPackageInstalled(c, JINSHAN_SAFE_PACKAGE)) {
			value += 1 << 4;
		}
		return value;
	}

	/**
	 * 获得默认浏览器
	 *
	 * @param c
	 * @return
	 */
	public static ResolveInfo getDefaultBrowser(Context c) {
		try {
			PackageManager pm = c.getPackageManager();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://www.google.com"));
			ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
			return info;
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		return null;
	}
	/**
	 * 检测小米V5. 方法来源：小米论坛，开发者板块。
	 *
	 * @return
	 */
	// public static boolean isMiuiV5() {
	// String miuiVersionName = "";
	// try {
	// miuiVersionName = SystemProperties.get("ro.miui.ui.version.name");
	// } catch (Exception e) {
	// miuiVersionName = "";
	// }
	// return "V5".equalsIgnoreCase(miuiVersionName);
	// }

	/**
	 * 获取默认浏览器的情况
	 *
	 * @param c
	 * @return
	 */
	public static int getDefaultBrowserValue(Context c) {
		ResolveInfo info = getDefaultBrowser(c);
		String packageName = "";
		if (info != null && info.activityInfo != null) {
			packageName = info.activityInfo.packageName;
		}
		if (packageName.equals("android")) {
			return DEFAULT_BROWSER_NONE;
		}
		if (packageName.equals(UC_BROWSER_PACKAGE)) {
			return DEFAULT_BROWSER_UC;
		}
		if (packageName.equals(QQ_BROWSER_PACKAGE)) {
			return DEFAULT_BROWSER_QQ;
		}
		if (packageName.equals(LIEBAO_BROWSER_PACKAGE)) {
			return DEFAULT_BROWSER_LIEBAO;
		}
		if (packageName.equals(SOGOU_BROWSER_PACKAGE)) {
			return DEFAULT_BROWSER_SOGOU;
		}
		if (packageName.equals(SYSTEM_BROWSER_PACKAGE)) {
			return DEFAULT_BROWSER_SYSTEM;
		}
		if (packageName.equals(c.getPackageName())) {
			return DEFAULT_BROWSER_MOMENG;
		}
		return DEFAULT_BROWSER_OTHER;
	}

	/**
	 * 获得cpu核心数
	 *
	 * @return
	 */
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}
		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			int cores = files.length;
			int type = CPU_CORE_1;
			if (cores == 1) {
				type = CPU_CORE_1;
			} else if (cores == 2) {
				type = CPU_CORE_2;
			} else if (cores == 4) {
				type = CPU_CORE_4;
			} else if (cores == 8) {
				type = CPU_CORE_8;
			} else if (cores > 8) {
				type = CPU_CORE_8;
			}
			return type;
		} catch (Exception e) {
			// Default to return 1 core
			return CPU_CORE_1;
		}
	}

	/**
	 * 判断是否为小米手机或者MIUI
	 */
	public static boolean isMiuiRom(Context context) {
		// 少部分厂商的手机刷入MIUI V5之后并不会修改FINGERPRINTER的值
		// 增加小米官方提供的V5判断方法一并使用
		String fingerPrint = Build.FINGERPRINT != null ? Build.FINGERPRINT
				.toLowerCase() : "";
		if (fingerPrint.contains("miui") || fingerPrint.contains("xiaomi")) {
			return true;
		}
		return false;
	}

	/**
	 * 小米Note
	 *
	 * @return
	 */
	public static boolean isMiNote() {
		if (TextUtils.equals("MI NOTE LTE", android.os.Build.MODEL)) {
			return true;
		}
		return false;
	}

	/**
	 * 设置Activity全屏显示
	 *
	 * @param activity
	 * @param isFullScreen
	 */
	public static void setFullScreen(Activity activity, boolean isFullScreen) {
		if (isFullScreen) {
			activity.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			final WindowManager.LayoutParams attrs = activity.getWindow()
					.getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			activity.getWindow().setAttributes(attrs);
			activity.getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		}
	}

	/**
	 * 部分机型刷新一下媒体数据库，这样图片才能在图库中显示
	 */
	public static void refreshMediaMounted(final String filePath) {
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				if (Build.VERSION.SDK_INT < 19) {
					try {
						StringBuilder sb = new StringBuilder();
						sb.append("file://");
						sb.append(VCStoragerManager.getInstance().getDownloadDirPath());
						JuziApp.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(sb.toString())));
						sb = new StringBuilder();
						sb.append("file://");
						sb.append(VCStoragerManager.getInstance().getImageDirPath());
						JuziApp.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(sb.toString())));
					} catch (Exception e) {
						SimpleLog.e(e);
					}
				} else {
					try {
						File f = new File(filePath);
						if (f.exists()) {
							File[] files = f.getParentFile().listFiles();
							String[] nameList = new String[files.length];
							for (int i = 0; i < files.length; i++) {
								nameList[i] = files[i].getAbsolutePath();
							}
							MediaScannerConnection.scanFile(JuziApp.getInstance(), nameList, null, null);
						}
					} catch (Exception e) {
						SimpleLog.e(e);
					}
				}
//				ConfigManager.getInstance().notifyFileCountChanged(com.polar.browser.download_refactor.Constants.TYPE_IMAGE);
			}
		}, 1000);
	}

	/**
	 * 部分机型刷新一下媒体数据库，这样图片才能在图库中显示
	 */
	public static void refreshMediaMountedAfterDelete(final String[] filePath) {
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				if (Build.VERSION.SDK_INT < 19) {
					try {
						StringBuilder sb = new StringBuilder();
						sb.append("file://");
						sb.append(VCStoragerManager.getInstance().getDownloadDirPath());
						JuziApp.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(sb.toString())));
						sb = new StringBuilder();
						sb.append("file://");
						sb.append(VCStoragerManager.getInstance().getImageDirPath());
						JuziApp.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(sb.toString())));
					} catch (Exception e) {
						SimpleLog.e(e);
					}
				} else {
					// 4.4以上
					deleteMediaFiles(filePath);
				}
			}
		}, 1000);
	}

	/**
	 * 删除媒体文件，（刷新媒体，适配下载的图片删除后，图库里仍然显示缩略图或者灰图的问题）
	 *
	 * @param filePath
	 */
	public static void deleteMediaFiles(String[] filePath) {
		try {
			for (int i = 0; i < filePath.length; i++) {
				JuziApp.getInstance().getContentResolver().delete(Media.EXTERNAL_CONTENT_URI, Media.DATA + "=?", new String[]{filePath[i]});
			}
		} catch (Exception e) {
			SimpleLog.e(e);
		}
	}

	public static void createShortcutForTop(Context context, String name, int ridIcon, ComponentName component,
											boolean duplicate) {
		createShortcut(context, name, ridIcon, component, duplicate);
	}

	public static void createShortcut(Context context, String name, int ridIcon, ComponentName component,
									  boolean duplicate) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(component);
		Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		shortcutIntent.putExtra("duplicate", duplicate);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(context, ridIcon));
		context.sendBroadcast(shortcutIntent);
	}
//    public static void createShortcut(Context context, String name, Bitmap icon, ComponentName component, boolean duplicate) {
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        intent.setComponent(component);
//        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
//        shortcutIntent.putExtra("duplicate", duplicate);
//        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
//        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
//        context.sendBroadcast(shortcutIntent);
//    }

	public static boolean isLowLevelDevice(Context c) {
		if (isInitDeviceType) {
			return isLowLevelDevice;
		}
		isLowLevelDevice = false;
		long mem = getTotalMemoryByte(c) / 1024 / 1024;
		// 系统版本低于4.2
		if (SystemUtils.getOSVersion().compareTo("4.2") < 0) {
			isLowLevelDevice = true;
		} else if (mem < 1000) {
			// 内存低于1000M
			isLowLevelDevice = true;
		}
		isInitDeviceType = true;
		return isLowLevelDevice;
	}
//	
//	private static String getAuthorityFromPermission(Context context, String permission){
//	    if (permission == null) return null;
//	    List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
//	    if (packs != null) {
//	        for (PackageInfo pack : packs) { 
//	            ProviderInfo[] providers = pack.providers; 
//	            if (providers != null) { 
//	                for (ProviderInfo provider : providers) { 
//	                    if (permission.equals(provider.readPermission)) return provider.authority;
//	                    if (permission.equals(provider.writePermission)) return provider.authority;
//	                } 
//	            }
//	        }
//	    }
//	    return null;
//	}
//	
//	public static boolean hasShortcut(Context cx, String name) {
//	    boolean isAdded = false;
//	    ContentResolver cr = cx.getContentResolver();
//	    String authority = getAuthorityFromPermission(cx.getApplicationContext(), "com.android.launcher.permission.READ_SETTINGS");
//	    Uri contentUri = Uri.parse("content://"+authority+"/favorites?notify=true");
//	    Cursor c = cr.query(contentUri, new String[] { "title", "iconResource" }, "title=?" , new String[] { name }, null);
//	    if(c!=null && c.getCount()>0){
//	        isAdded = true;
//	    }
//	    return isAdded;
//	}
//	private static final String URI_LAUNCER = "content://com.android.launcher.settings/favorites?notify=false";
//    private static final String URI_HTC_LAUNCER = "content://com.htc.launcher.settings/favorites?notify=true";
//
//    public static boolean isShortcutExist(Context context, String name) {
//        return isShortcutExist(context, name, URI_LAUNCER)
//                || (Build.MANUFACTURER.toLowerCase().equals("htc") ? isShortcutExist(context, name,
//                        URI_HTC_LAUNCER) : false);
//    }
//
//    private static final String[] PROJECTION = { "_id", "title", "intent" };
//
//    public static boolean isShortcutExist(Context context, String name, String lancherUri) {
//        Cursor cursor = null;
//        try {
//            Uri uri = Uri.parse(lancherUri);
//            String where = "title='" + name + "'";
//            cursor = context.getContentResolver().query(uri, PROJECTION, where, null, null);
//
//            if (cursor != null && cursor.moveToFirst()) {
//                return true;
//            }
//        } catch (Exception e) {
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return false;
//    }

	/**
	 * 创建桌面快捷方式
	 */
	public static void createShotcut(Context c, String url, String title, String path) {
//        createShortcutForTop(c, title,
//                R.drawable.ic_launcher, new ComponentName(c.getPackageName(),
//                		url), false);
//
//        return;
		try {
			Bitmap bitmap = null;
			// 获取快捷键的图标
			if (!TextUtils.isEmpty(path)) {
				bitmap = FileUtils.getBitmapFromFile(path);
			}
			int size = (int) c.getResources().getDimension(android.R.dimen.app_icon_size);
			Parcelable icon = Intent.ShortcutIconResource.fromContext(c, R.drawable.default_shortcut_desk);
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName("com.polar.browser", "com.polar.browser.activity.BrowserActivity");
			intent.putExtra(CommonData.ACTION_GOTO_URL, url);
			intent.putExtra(CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_SHORTCUT);
//			String packageName = c.getPackageName();
//	        intent.setComponent(new ComponentName("com.polar.browser", "com.polar.browser.JuziApp"));
			Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
			// 快捷方式的标题
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
			// 快捷方式的图标
			if (bitmap != null) {
				if (size < 128) {
					addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, Bitmap.createScaledBitmap(bitmap, size, size, false));
				} else {
					addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
				}
			} else {
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
			}
			// 快捷方式的动作
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
			// 是否允许重复添加
			addIntent.putExtra("duplicate", false);
			// 发送广播
			c.getApplicationContext().sendBroadcast(addIntent);
		} catch (Exception e) {
		}
	}

	/**
	 * 包被窜改，打开一个合适的浏览器下载
	 *
	 * @return
	 */
	public static ComponentName getDefaultBrowserComponentName() {
		// TODO 去官网
		Intent intent2 = new Intent();
		intent2.setAction(Intent.ACTION_VIEW);
		intent2.addCategory(Intent.CATEGORY_BROWSABLE);
		intent2.setData(Uri.parse(JuziApp.getInstance().getString(R.string.official_website)));
		List<ResolveInfo> list = JuziApp
				.getInstance()
				.getPackageManager()
				.queryIntentActivities(intent2,
						PackageManager.MATCH_DEFAULT_ONLY);
		String activityName = null;
		String packageName = null;
		for (int i = 0; i < list.size(); i++) {
			ResolveInfo info = list.get(i);
			packageName = info.activityInfo.packageName;
			activityName = info.activityInfo.name;
			break;
		}
		ResolveInfo info = SysUtils.getDefaultBrowser(JuziApp.getInstance());
		if (info != null) {
			String pName = info.activityInfo.packageName;// com.android.browser
			if (TextUtils.equals("android", pName)
					|| (TextUtils.equals("com.android.browser", pName) && !isInDefaultList(pName))) {
				// 没有设置默认
			} else if (TextUtils.equals(JuziApp.getInstance().getPackageName(), pName)) {
				// TODO 设置了默认，而且是Eva,显示
			} else {
				// TODO 设置了默认，但不是Eva
				SimpleLog.e(TAG, pName + " is default !!");
				packageName = pName;
				activityName = info.activityInfo.name;
			}
		}
		ComponentName componentName = null;
		if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty(activityName)) {
			componentName = new ComponentName(packageName, activityName);
		}
		return componentName;
	}

	/**
	 * 红米手机，判断是否设置了默认浏览器，没有设置默认浏览器 && 设置了默认浏览器 都会是 com.android.browser。。。
	 * 所以添加一个判断，看com.android.browser是否在系统的 所有默认程序列表中
	 *
	 * @return
	 */
	private static boolean isInDefaultList(String packageName) {
		PackageManager pm = JuziApp.getInstance().getPackageManager();
		// Get list of preferred activities
		List<ComponentName> prefActList = new ArrayList<ComponentName>();
		// Intent list cannot be null. so pass empty list
		List<IntentFilter> intentList = new ArrayList<IntentFilter>();
		pm.getPreferredActivities(intentList, prefActList, packageName);
		for (int i = 0; i < prefActList.size(); i++) {
			ComponentName pi = prefActList.get(i);
			SimpleLog.e(TAG, "default  ComponentName " + i + " == " + pi.toString());
			if (TextUtils.equals("com.android.browser", pi.getPackageName())) {
				// 在默认列表中
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取状态栏高度
	 *
	 * @param activity
	 * @return
	 */
	public static int getStatusHeight(Activity activity) {
		int statusHeight = 0;
		Rect rect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		statusHeight = rect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object object = localClass.newInstance();
				int height = Integer.parseInt(localClass
						.getField("status_bar_height").get(object).toString());
				statusHeight = activity.getResources().getDimensionPixelSize(
						height);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	/**
	 * 只支持MIUI V6 & V7
	 *
	 * @param context
	 * @param type    0--只需要状态栏透明 1-状态栏透明且黑色字体 2-清除黑色字体
	 */
	public static void setStatusBarTextColor(Activity context, int type) {
		if (!isMiUIV6orV7()) {
			SimpleLog.d("", "isMiUIV6orV7:" + false);
			return;
		}
		SimpleLog.d("", "isMiUIV6orV7:" + true);
		Window window = context.getWindow();
		Class clazz = window.getClass();
		try {
			int tranceFlag = 0;
			int darkModeFlag = 0;
			Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
			Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT");
			tranceFlag = field.getInt(layoutParams);
			field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
			darkModeFlag = field.getInt(layoutParams);
			Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
			if (type == 0) {
				extraFlagField.invoke(window, tranceFlag, tranceFlag);//只需要状态栏透明
			} else if (type == 1) {
				extraFlagField.invoke(window, tranceFlag | darkModeFlag, tranceFlag | darkModeFlag);//状态栏透明且黑色字体
			} else {
				extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
			}
		} catch (Exception e) {
		}
	}

	private static boolean isMiUIV6orV7() {
		try {
			String name = SystemPropertiesInvoke.getString(KEY_MIUI_VERSION_NAME, "");
			if ("V6".equals(name)) {
				return true;
			} else if ("V7".equals(name)) {
				return true;
			} else {
				return false;
			}
//            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
//                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
//                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * 获取剪切板里的网址
	 * @return
	 */
	public static String getContentByClipboar(Context context) throws Exception{
		String contentClipboar = null;
		ClipboardManager cm = (ClipboardManager) context.getSystemService(
				Context.CLIPBOARD_SERVICE);
		if (cm != null && cm.hasPrimaryClip() &&
				(cm.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
						cm.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) ||
						cm.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_URILIST))) {
			ClipData cd = cm.getPrimaryClip();
			ClipData.Item item = cd.getItemAt(0);
			contentClipboar = item.getText().toString();
		}

		return contentClipboar;
	}

    public static boolean isSameDay(long currentTime, long lastTime) {
        return (Math.abs(currentTime - lastTime) < Constants.ONE_DAY_TIMEMILLIS);
    }
}
