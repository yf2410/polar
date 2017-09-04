package com.polar.browser.manager;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.polar.browser.thread.IOHandler;
import com.polar.browser.thread.LogicHandler;
import com.polar.browser.thread.NetworkHandler;
import com.polar.browser.thread.UIHandler;

/**
 * 用于管理线程，外部模块可以调用该类，执行各个线程的相关任务
 * 所有需要线程处理的事情，全部在此处调用。
 * <p/>
 * 如：需要NetworkHandler来进行下载，则需要调用ThreadManager.getNetworkHandler().Download();
 * 而不是自己试图创建NetworkHandler的实例（无法创建，构造函数被设成private）
 *
 * @author dpk
 */
public class ThreadManager {
	private static final String THREAD_IO_NAME = "threadIO";
	private static final String THREAD_NETWORK_NAME = "threadNetwork";
	private static final String THREAD_LOGIC_NAME = "threadLogic";
	private static IOHandler sIOHandler;
	private static NetworkHandler sNetworkHandler;
	private static UIHandler sUIHandler;
	private static LogicHandler sLogicHandler;


	private ThreadManager() {
	}

	public static void init() {
		sUIHandler = new UIHandler(Looper.getMainLooper());
		HandlerThread threadIO = new HandlerThread(THREAD_IO_NAME);
		threadIO.start();
		sIOHandler = IOHandler.getInstance(threadIO.getLooper());
		HandlerThread threadNetwork = new HandlerThread(THREAD_NETWORK_NAME);
		threadNetwork.start();
		sNetworkHandler = NetworkHandler.getInstance(threadNetwork.getLooper());
		HandlerThread threadLogic = new HandlerThread(THREAD_LOGIC_NAME);
		threadLogic.start();
		sLogicHandler = LogicHandler.getInstance(threadLogic.getLooper());
		// You should set the thread priority to "background" priority by calling Process.setThreadPriority()
		// and passing THREAD_PRIORITY_BACKGROUND. If you don't set the thread to a lower priority this way,
		// then the thread could still slow down your app
		// because it operates at the same priority as the UI thread by default.
		// http://developer.android.com/training/articles/perf-anr.html
		Process.setThreadPriority(threadIO.getThreadId(), Process.THREAD_PRIORITY_BACKGROUND);
		Process.setThreadPriority(threadNetwork.getThreadId(), Process.THREAD_PRIORITY_BACKGROUND);
		Process.setThreadPriority(threadLogic.getThreadId(), Process.THREAD_PRIORITY_BACKGROUND);
	}

	public static void destroy() {
		if (sIOHandler != null) {
			sIOHandler.removeCallbacksAndMessages(null);
		}
		if (sNetworkHandler != null) {
			sNetworkHandler.removeCallbacksAndMessages(null);
		}
		if (sLogicHandler != null) {
			sLogicHandler.removeCallbacksAndMessages(null);
		}
		if (sUIHandler != null) {
			sUIHandler.removeCallbacksAndMessages(null);
		}
	}

	public static void postTaskToIOHandler(Runnable r) {
		sIOHandler.post(r);
	}

	public static void postDelayedTaskToIOHandler(Runnable r, long delayMillis) {
		sIOHandler.postDelayed(r, delayMillis);
	}

	public static void sendMessageToIOHandler(Message msg) {
		sIOHandler.sendMessage(msg);
	}

	public static void sendDelayedMessageToIOHandler(Message msg, long delayMillis) {
		sIOHandler.sendMessageDelayed(msg, delayMillis);
	}

	public static void postTaskToNetworkHandler(Runnable r) {
		sNetworkHandler.post(r);
	}

	public static void postDelayedTaskToNetworkHandler(Runnable r, long delayMillis) {
		sNetworkHandler.postDelayed(r, delayMillis);
	}

	public static void sendMessageToNetworkHandler(Message msg) {
		sNetworkHandler.sendMessage(msg);
	}

	public static void sendDelayedMessageToNetworkHandler(Message msg, long delayMillis) {
		sNetworkHandler.sendMessageDelayed(msg, delayMillis);
	}

	public static void postTaskToUIHandler(Runnable r) {
		sUIHandler.post(r);
	}

	public static void postDelayedTaskToUIHandler(Runnable r, long delayMillis) {
		sUIHandler.postDelayed(r, delayMillis);
	}

	public static void sendMessageToUIHandler(Message msg) {
		sUIHandler.sendMessage(msg);
	}

	public static void sendDelayedMessageToUIHandler(Message msg, long delayMillis) {
		sUIHandler.sendMessageDelayed(msg, delayMillis);
	}

	public static void sendMessageToLogicHandler(Message msg) {
		sLogicHandler.sendMessage(msg);
	}

	public static void sendDelayedMessageToLogicHandler(Message msg, long delayMillis) {
		sLogicHandler.sendMessageDelayed(msg, delayMillis);
	}

	public static void postTaskToLogicHandler(Runnable r) {
		sLogicHandler.post(r);
	}

	public static void postDelayedTaskToLogicHandler(Runnable r, long delayMillis) {
		sLogicHandler.postDelayed(r, delayMillis);
	}

	public static IOHandler getIOHandler() {
		return sIOHandler;
	}

	public static NetworkHandler getNetworkHandler() {
		return sNetworkHandler;
	}

	public static UIHandler getUIHandler() {
		return sUIHandler;
	}

	public static LogicHandler getLogicHandler() {
		return sLogicHandler;
	}

	public static void removeAllUITask() {
		sUIHandler.removeCallbacksAndMessages(null);
	}
}
