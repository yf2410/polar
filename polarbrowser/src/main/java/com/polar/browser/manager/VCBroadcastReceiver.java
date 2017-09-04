package com.polar.browser.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.adjust.AdjustService;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.push.topic.TopicManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.common.Constants;

/**
 * Created by FKQ on 2016/10/9.
 */

public class VCBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case Intent.ACTION_LOCALE_CHANGED:  //系统语言切换
                TopicManager.getInstance().onLocaleChanged(SystemUtils.getLan());
                ConfigManager.getInstance().setLanChangedRestart(true);
                if (AppEnv.DEBUG) {
                    String lan = SystemUtils.getLan();
                    String lastRunLan = ConfigManager.getInstance().getLastRunLan();
                    SimpleLog.d("VCBroadcastReceiver", "语言切换！！！！！！！！！");
                    SimpleLog.d("VCBroadcastReceiver", "上次语言=="+lastRunLan + "当前语言==" + lan);
                }
                break;
            case Constants.ADJUST_LIFE_ACTION:
                context.startService(new Intent(context, AdjustService.class));
                break;

            case Intent.ACTION_USER_PRESENT:    //屏幕解锁的时候触发这个广播
                context.startService(new Intent(context, AdjustService.class));
                break;
            case Intent.ACTION_BOOT_COMPLETED:  //在系统启动完成后，这个动作被广播一次（只有一次）
                if (ConfigManager.getInstance().isQuickSearch()){
                    shwoNotify(context);
                }
                context.startService(new Intent(context, AdjustService.class));
                break;
            default:
                break;
        }
    }

   /**
     * 显示通知栏快捷搜索常驻通知
     */
    private void shwoNotify(Context context) {

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        RemoteViews mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notifi_search);
        Intent buttonIntent1 = new Intent(context, BrowserActivity.class);
        buttonIntent1.setAction(CommonData.QUICK_SEARCH_SEARCH);
        PendingIntent pendingIntent1 = PendingIntent
                .getActivity(context, 1, buttonIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.search_area, pendingIntent1);
        Intent buttonIntent2 = new Intent(context, BrowserActivity.class);
        buttonIntent2.setAction(CommonData.QUICK_SEARCH_SETTING);
        PendingIntent pendingIntent2 = PendingIntent
                .getActivity(context, 2, buttonIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_gosetting, pendingIntent2);
        mBuilder.setContent(mRemoteViews)
                .setContentIntent(pendingIntent1)
                .setContentIntent(pendingIntent2)
                .setSmallIcon(R.drawable.notifi_icon);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        mNotificationManager.notify(CommonData.QUICK_SEARCH_ID, notify);
    }
}
