package com.polar.browser.push.topic;

import com.google.firebase.messaging.FirebaseMessaging;
import com.polar.browser.env.AppEnv;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.util.AdapterConvertor;

/**
 * Created by James on 2016/8/15.
 */
public class TopicManager {
	public static final String TAG = "TopicManager";
	private String languageTopic;
	private String nationTopic;
	private String versionTopic;
	private static TopicManager mInstance;

	private TopicManager(){

	}
	public static TopicManager getInstance() {
	    if (mInstance == null) {
	        synchronized (TopicManager.class) {
	            if (mInstance == null) {
	                mInstance = new TopicManager();
	            }
	        }
	    }
	    return mInstance;
	}


	/**
	 * locale信息更改时回调
	 *
	 * @param currentLanguage
	 */
	public void onLocaleChanged(String currentLanguage) {
		if (!equals(languageTopic, currentLanguage)) {
			subscribeTopic(currentLanguage);
			unsubscribeTopic(languageTopic);
			languageTopic = currentLanguage;
		}
	}

	/**
	 * 应用启动时注册主题
	 */
	public void subscribeTopics(String preMCC,String preLan) {
		String languageTopic = AdapterConvertor.getLanguageTopic(preLan);
		String nationTopic = AdapterConvertor.getNationTopic(preMCC);
		String versionTopic = AppEnv.getVersionTopic();
		subscribeTopic(languageTopic);
		subscribeTopic(nationTopic);
		subscribeTopic(versionTopic);
		this.languageTopic = languageTopic;
		this.nationTopic = nationTopic;
		this.versionTopic = versionTopic;
	}

	private void subscribeTopic(String topic) {
		if (topic != null) {
			try {
				FirebaseMessaging.getInstance().subscribeToTopic(topic);
				SimpleLog.d(TAG, "subscribe " + topic);
			} catch (Exception e) {
				//部分机型会出现异常
				//java.lang.IllegalStateException: FirebaseApp with name [DEFAULT] doesn't exist.
				e.printStackTrace();
			}
		}
	}

	private void unsubscribeTopic(String topic) {
		if (topic != null) {
			try {
				FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
				SimpleLog.d(TAG, "unsubscribe " + topic);
			} catch (Exception e) {
				//部分机型会出现异常
				//java.lang.IllegalStateException: FirebaseApp with name [DEFAULT] doesn't exist.
				e.printStackTrace();
			}
		}
	}

	private boolean equals(Object a, Object b) {
		return (a == null) ? (b == null) : a.equals(b);
	}
}
