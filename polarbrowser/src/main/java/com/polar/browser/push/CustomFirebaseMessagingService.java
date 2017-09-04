package com.polar.browser.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.homepage.customlogo.JsonParser;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.db.News;
import com.polar.browser.vclibrary.bean.db.PushedSystemNews;
import com.polar.browser.vclibrary.bean.db.SystemNews;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SystemNewsApi;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by Administrator on 2016/7/12.
 */
public class CustomFirebaseMessagingService extends FirebaseMessagingService {
	private static final String TAG = "CustomFirebaseMsgService";

	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, "receive remoteMessage" + remoteMessage.toString());
		}
		sendFCMArriveStatistics(GoogleConfigDefine.FCM, GoogleConfigDefine.FCM_ARRIVE);
		try {
			Map<String, String> data = remoteMessage.getData();
			if (AppEnv.DEBUG) {
				SimpleLog.d(TAG, "receive remoteMessage data: " + data);
			}
			String versionString = data.get(CommonData.DATA_VERSION);
			String typeString = data.get(CommonData.DATA_TYPE);
			String content = data.get(CommonData.DATA_CONTENT);
			sendFCMArriveStatistics(GoogleConfigDefine.FCM, "version="+versionString+",type="+typeString);
			if (TextUtils.isEmpty(versionString) || TextUtils.isEmpty(typeString) || TextUtils.isEmpty(content)) {
				return;
			}
			switch (typeString) {
				case CommonData.TYPE_NEWS:
					if (!ConfigManager.getInstance().getNotifyNewsEngine()) {
						return;
					}
					sendFCMArriveStatistics(GoogleConfigDefine.FCM, "推送新闻到达");
					News news = JsonParser.fromJson(content, News.class);
					sendNotification(news);
					sendFCMArriveStatistics(GoogleConfigDefine.FCM_SYSTEM_ARRIVE, news.getUrl());
					break;
				case CommonData.TYPE_SYSTEM_NEWS:
					if (!ConfigManager.getInstance().getNotifySystemEngine()) {
						return;
					}
					sendFCMArriveStatistics(GoogleConfigDefine.FCM, "推送系统消息到达");
					PushedSystemNews pushedSystemNews = JsonParser.fromJson(content, PushedSystemNews.class);
					SystemNews systemNews = Converter.convert(versionString, pushedSystemNews);
					sendNotification(systemNews);
					try {
						SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).insert(systemNews);
						ConfigManager.getInstance().updateSystemNewsHintState(true);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					sendFCMArriveStatistics(GoogleConfigDefine.FCM_NEWS_ARRIVE, systemNews.getContentURL());
					break;
				default:
					break;
			}
		}
		//防止解析错误
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendFCMArriveStatistics(String key, String value) {
		if (TextUtils.isEmpty(value)) {
			return;
		}
		Statistics.sendOnceStatistics(key, value);
	}

	private void sendNotifiation(int notificationId, Intent intent, int requestCode, String title, String contentText, String ticker) {
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);
		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setSmallIcon(R.drawable.icon_notifications);
		builder.setContentTitle(title);
		builder.setContentText(contentText);
		builder.setColor(getResources().getColor(R.color.color_box_item_7));
		builder.setTicker(ticker);
		builder.setAutoCancel(true);
		builder.setSound(defaultSoundUri);
		builder.setContentIntent(pendingIntent);
		Notification notification = builder.build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notificationId, notification);
	}

	public void sendNotification(News news) {
		Intent intent = new Intent(this, BrowserActivity.class);
		intent.setAction(CommonData.ACTION_CLICK_NOTIFICATION);
		intent.putExtra(CommonData.DATA_TYPE, CommonData.TYPE_NEWS);
		intent.putExtra(CommonData.EXTRA_NEWS, news);
		sendNotifiation(IdGenerator.getInstance().generateNewsId(), intent, IdGenerator.getInstance().generateNewsRequestCode(), getResources().getString(R.string.app_name), news.getTitle(), null);
		sendFCMArriveStatistics(GoogleConfigDefine.FCM, "推送新闻发送通知栏");
	}

	public void sendNotification(SystemNews systemNews) {
		Intent intent = new Intent(this, BrowserActivity.class);
		intent.setAction(CommonData.ACTION_CLICK_NOTIFICATION);
		intent.putExtra(CommonData.DATA_TYPE, CommonData.TYPE_SYSTEM_NEWS);
		intent.putExtra(CommonData.EXTRA_SYSTEM_NEWS, systemNews);
		sendNotifiation(CommonData.NOTIFICATION_ID_SYSTEM_NEWS, intent, IdGenerator.getInstance().generateNewsRequestCode(), getResources().getString(R.string.app_name), systemNews.getTitle(), null);
		sendFCMArriveStatistics(GoogleConfigDefine.FCM, "推送系统消息发送通知栏");
	}
}
