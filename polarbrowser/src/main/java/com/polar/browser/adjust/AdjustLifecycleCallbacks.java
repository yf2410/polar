package com.polar.browser.adjust;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import com.adjust.sdk.Adjust;
import com.polar.browser.JuziApp;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.vclibrary.common.Constants;

import java.util.Random;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by FKQ on 2016/10/17.
 */

public class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Adjust.onResume();
        if (ConfigManager.getInstance().getAdjustLifeSwitchState()) {
            Intent intent = new Intent(Constants.ADJUST_LIFE_ACTION);
            PendingIntent sender=PendingIntent.getBroadcast(JuziApp.getAppContext(), 0, intent, 0);
            AlarmManager alarm=(AlarmManager) JuziApp.getInstance().getSystemService(ALARM_SERVICE);
            alarm.cancel(sender);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Adjust.onPause();
//        String adjustSwitchState = ConfigManager.getInstance().getAdjustSwitchState();
//        switch (adjustSwitchState) {
//            case Constants.ADJUST_CLOSE:
//                return;
//            case Constants.ADJUST_FOUR:
//                sendAdjustLifeByTime(Constants.ADJUST_FOUR_TIME);
//                break;
//            case Constants.ADJUST_EIGHT:
//                sendAdjustLifeByTime(Constants.ADJUST_EIGHT_TIME);
//                break;
//            case Constants.ADJUST_TWELVE:
//                sendAdjustLifeByTime(Constants.ADJUST_TWELVE_TIME);
//                break;
//            default:
//                break;
//        }

        if (ConfigManager.getInstance().getAdjustLifeSwitchState()) {
            sendAdjustLifeByTime(Constants.ADJUST_FOUR_TIME);
        }
    }

    private void sendAdjustLifeByTime(final long time) {
        long times = time + randomLongByInt();

        Intent intent = new Intent(Constants.ADJUST_LIFE_ACTION);
        PendingIntent sender=PendingIntent.getBroadcast(JuziApp.getAppContext(), 0, intent, 0);
        //开始时间
        long firstime= SystemClock.elapsedRealtime();
        AlarmManager am= (AlarmManager) JuziApp.getInstance().getSystemService(ALARM_SERVICE);
        //设置多少时间一个周期，不停的发送广播
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, times, sender);
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    /**
     * 设置间隔随机数（一小时内随机）
     * @return
     */
    private long randomLongByInt() {
        int nextInt = new Random().nextInt(3600000);
        return (long)nextInt;
    }

}
