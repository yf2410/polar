package com.polar.browser.crashhandler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.polar.browser.JuziApp;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理,当程序发生Uncaught异常的时,有该类来接管程序,并记录发送错误报.
 *
 * @author user
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = "CrashHandler";
	// 崩溃目录名
	public static String JAVA_CRASH_DIR = "/Polar_crash/java/";
	public static String ANR_DIR = "/Polar_crash/anr/";
	// 存储的crash文件名
	private static String mFileName;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 系统默认的UncaughtException处理
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// 程序的Context对象
	private Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	private CrashHandler() {
	}

	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 *
	 * @param context
	 */
	public void init(Context context) {
		SimpleLog.d(TAG, "init");
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				SimpleLog.e(TAG, "error : " + e.toString());
			}
			//只有主线程崩溃时才弹框
			try {
				String sRuntimeProcessName = SysUtils.getCurrentProcessName();
				if (sRuntimeProcessName.equals(mContext.getApplicationInfo().packageName)) {
					CrashUploadActivity.start(mContext, mFileName);
				}
			} catch (Exception e) {
				SimpleLog.e(e);
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 自定义错误处理
	 *
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(final Throwable ex) {
		if (ex == null) {
			return false;
		}
//		// 显示异常
//		new Thread() {
//			@Override
//			public void run() {
////                Looper.prepare();
////                Toast.makeText(mContext, mContext.getString(R.string.app_crash_toast), Toast.LENGTH_LONG).show();
//				//收集设备参数信息
//				collectDeviceInfo();
//				//保存日志文件
//				mFileName = saveCrashInfo2File(ex);
////                Looper.loop();
//			}
//		}.start();
		ThreadManager.postTaskToIOHandler(new ExceptionHandleTask(ex));
		return true;
	}

	private class ExceptionHandleTask implements  Runnable{

		private final Throwable mException;

		ExceptionHandleTask(Throwable ex){
			this.mException=ex;
		}

		@Override
		public void run() {
			//收集设备参数信息
			collectDeviceInfo();
			//保存日志文件
			mFileName = saveCrashInfo2File(mException);
		}
	}

	/**
	 * 收集设备参数信息
	 *
	 */
	public void collectDeviceInfo() {
		if (infos.size() > 1)
			return;
		try {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
				infos.put("osVersion", SystemUtils.getOSVersion());
				infos.put("mmod",SystemUtils.getModel());
				infos.put("mid", SystemUtils.getMid(JuziApp.getAppContext()));
			}
		} catch (Throwable e) {
			SimpleLog.e(TAG, "an error occured when collect package info:" + e.toString());
		}

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				SimpleLog.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				SimpleLog.e(TAG, "an error occured when collect crash info:" + e.toString());
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 *
	 * @param ex
	 * @return 返回文件名称, 便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		long timestamp = System.currentTimeMillis();
		String timevalue = "TIMESTAMP=" + String.valueOf(timestamp) + "\n";
		sb.append(timevalue);
		String result = "DUMP=" + writer.toString();
		sb.append(result);
		try {
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String path = Environment.getExternalStorageDirectory().getPath();
				path += JAVA_CRASH_DIR;
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				SimpleLog.e(TAG, sb.toString());
				FileOutputStream fos = new FileOutputStream(path + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
				return path + fileName;
			}
		} catch (Exception e) {
			SimpleLog.e(TAG, "an error occured while writing file..." + e);
		}
		return null;
	}

	public void writeDeviceInfo(BufferedWriter writer) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		try {
			writer.write("\n\n----VCBrowser dev Info----\n");
			writer.write(sb.toString());
		} catch (Throwable e) {
		}
	}
} 