package com.polar.browser.utils;

import android.os.Process;

import com.polar.browser.JuziApp;
import com.polar.browser.manager.ThreadManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * dns工具类，用于预加载dns
 *
 * @author dpk
 */
public class DnsUtils {

	private static final String TAG = "DnsUtils";

	private static final String CONFIG_PATH = "dns_config";

	public static void preloadBookmarkDns(final List<String> bookmarkHostList) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				long start = 0;
				long end = 0;
				for (int i = 0; i < bookmarkHostList.size(); ++i) {
					start = System.currentTimeMillis();
					try {
						InetAddress.getByName(bookmarkHostList.get(i));
						end = System.currentTimeMillis();
						SimpleLog.d(
								TAG,
								"inetAddress.getByName("
										+ bookmarkHostList.get(i) + "): "
										+ String.valueOf(end - start));
					} catch (UnknownHostException e) {
						SimpleLog.e(e);
					}
				}
			}
		};
		ThreadManager.postTaskToLogicHandler(r);
	}

	public static void preloadPresetDns(final String hosts) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					String[] hostList = hosts.split("\n");
					for (int i = 1; i < hostList.length; i += 2) {
						InetAddress.getByName(hostList[i]);
					}
				} catch (Throwable e) {
				}
			}
		};
		ThreadManager.postTaskToLogicHandler(r);
	}

	public static void preloadDns() {
//		PreloadDnsThread t = new PreloadDnsThread();
//		t.start();
	  ThreadPoolUtils.execute(new PreloadDnsTask());   //采用线程池来代替直接new Thread().start()，节省内存开销
	}


	/**
	 *
	 */
	private static class PreloadDnsTask implements  Runnable{

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			java.security.Security.setProperty("networkaddress.cache.ttl",
					String.valueOf(300));
			String path = JuziApp.getAppContext().getFilesDir().toString();
			path = path + "/" + CONFIG_PATH;
			try {
				File file = new File(path);
				if (!file.exists()) {
					FileUtils.copyAssetsFile(JuziApp.getAppContext(),
							CONFIG_PATH, file);
				}
				if (file.exists()) {
					FileReader fr = new FileReader(file);
					BufferedReader br = new BufferedReader(fr);
					String line;
					long start = 0;
					long end = 0;
					while ((line = br.readLine()) != null) {
						start = System.currentTimeMillis();
						try {
							InetAddress.getByName(line);
							end = System.currentTimeMillis();
							SimpleLog.d(TAG, "inetAddress.getByName(" + line
									+ "): " + String.valueOf(end - start));
						} catch (Throwable e) {
						}
					}
					br.close();
				}
			} catch (IOException e) {
				SimpleLog.e(e);
			} catch (Exception e) {
			}
		}
	}

//	public static class PreloadDnsThread extends Thread {
//		@Override
//		public void run() {
//			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
//			java.security.Security.setProperty("networkaddress.cache.ttl",
//					String.valueOf(300));
//			String path = JuziApp.getAppContext().getFilesDir().toString();
//			path = path + "/" + CONFIG_PATH;
//			try {
//				File file = new File(path);
//				if (!file.exists()) {
//					FileUtils.copyAssetsFile(JuziApp.getAppContext(),
//							CONFIG_PATH, file);
//				}
//				if (file.exists()) {
//					FileReader fr = new FileReader(file);
//					BufferedReader br = new BufferedReader(fr);
//					String line;
//					long start = 0;
//					long end = 0;
//					while ((line = br.readLine()) != null) {
//						start = System.currentTimeMillis();
//						try {
//							InetAddress.getByName(line);
//							end = System.currentTimeMillis();
//							SimpleLog.d(TAG, "inetAddress.getByName(" + line
//									+ "): " + String.valueOf(end - start));
//						} catch (Throwable e) {
//						}
//					}
//					br.close();
//				}
//			} catch (IOException e) {
//				SimpleLog.e(e);
//			} catch (Exception e) {
//			}
//		}
//	}
	// public static void testPreloadDns() {
	// try {
	// java.security.Security.setProperty("networkaddress.cache.ttl",
	// String.valueOf(300));
	// long start = System.currentTimeMillis();
	// InetAddress inetAddress = InetAddress.getByName("m.baidu.com");
	// long end = System.currentTimeMillis();
	// SimpleLog.d(TAG, "InetAddress.getByName(m.baidu.com): " +
	// String.valueOf(end - start));
	//
	// start = System.currentTimeMillis();
	// String ip = inetAddress.getHostAddress();
	// end = System.currentTimeMillis();
	// SimpleLog.d(TAG, "ip: " + ip + " inetAddress.getHostAddress(): " +
	// String.valueOf(end - start));
	//
	// start = System.currentTimeMillis();
	// InetAddress inetAddress2 = InetAddress.getByName("m.baidu.com");
	// end = System.currentTimeMillis();
	// SimpleLog.d(TAG, "inetAddress2.getByName(m.baidu.com): " +
	// String.valueOf(end - start));
	//
	// start = System.currentTimeMillis();
	// String ip2 = inetAddress2.getHostAddress();
	// end = System.currentTimeMillis();
	// SimpleLog.d(TAG, "ip: " + ip2 + " inetAddress2.getHostAddress(): " +
	// String.valueOf(end - start));
	//
	// start = System.currentTimeMillis();
	// inetAddress = InetAddress.getByName("m.taobao.com");
	// end = System.currentTimeMillis();
	// SimpleLog.d(TAG, "inetAddress.getByName(m.taobao.com): " +
	// String.valueOf(end - start));
	//
	// start = System.currentTimeMillis();
	// inetAddress = InetAddress.getByName("m.taobao.com");
	// end = System.currentTimeMillis();
	// SimpleLog.d(TAG, "inetAddress2.getByName(m.taobao.com): " +
	// String.valueOf(end - start));
	//
	//
	// } catch (UnknownHostException e) {
	// SimpleLog.e(e);
	// }
	// }
}
