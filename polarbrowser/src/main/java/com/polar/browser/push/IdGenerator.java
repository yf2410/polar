package com.polar.browser.push;

import com.polar.browser.common.data.CommonData;
import com.polar.browser.utils.SimpleLog;

/**
 * Created by James on 2016/8/18.
 */
public class IdGenerator {
	public static final String TAG = "IdGenerator";
	private static final int MAX_NEWS_NOTIFICATION = 6;
	private static final int MAX_FB_MSG_NOTIFICATION = 100;
	private static IdGenerator instance;
	private int idCounter;
	private int idCounterFbNotify;
	private int requestCodeCounter;

	public static IdGenerator getInstance() {
		if (instance == null) {
			synchronized (IdGenerator.class) {
				if (instance == null) {
					instance = new IdGenerator();
				}
			}
		}
		return instance;
	}

	/**
	 * 生成从100~105,共6条新闻id
	 *
	 * @return
	 */
	public int generateNewsId() {
		idCounter = (idCounter + 1) % MAX_NEWS_NOTIFICATION;
		int id = CommonData.NOTIFICATION_ID_NEWS + idCounter;
		SimpleLog.d(TAG, "news notification id: " + id);
		return id;
	}

	public int generateFbNotifyMsgId() {
		idCounterFbNotify = (idCounterFbNotify + 1) % MAX_FB_MSG_NOTIFICATION;
		int id = CommonData.NOTIFICATION_ID_FB_MSG + idCounterFbNotify;
		SimpleLog.d(TAG, "news notification id: " + id);
		return id;
	}

	/**
	 * 生成自增的requestCode,确保不会溢出
	 *
	 * @return
	 */
	public int generateNewsRequestCode() {
		if (requestCodeCounter < Integer.MAX_VALUE - 1) {
			requestCodeCounter = (requestCodeCounter + 1) % Integer.MAX_VALUE;
		} else {
			requestCodeCounter = 0;
		}
		SimpleLog.d(TAG, "news request code: " + requestCodeCounter);
		return requestCodeCounter;
	}
}
