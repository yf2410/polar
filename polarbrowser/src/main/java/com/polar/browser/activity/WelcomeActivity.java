package com.polar.browser.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.vclibrary.bean.events.GoBrowserActivityEvent;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.business.ad_business.AdManager;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by saifei on 17/1/22.
 */

public class WelcomeActivity extends RxFragmentActivity {

    private static final String TAG = "WelcomeActivity";

    private boolean mIsShowWelcomeAd;
    private boolean mIsSkip;
    private static final long WELCOME_WAIT_TIME = 100L;
    private RelativeLayout mNativeAdContainer;
    private TextView nativeAdSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_welcome_layout);
        mIsShowWelcomeAd = ConfigManager.getInstance().getFbWelcomeEngine();
        SimpleLog.d(TAG,"mIsShowWelcomeAd="+mIsShowWelcomeAd);
        onGoBrowserActivityEvent();
        if(JuziApp.getInstance().isInitialized) {
            gotoBrowserActivity(WELCOME_WAIT_TIME);
        }else {
            initAdView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SysUtils.setFullScreen(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AdManager.getInstance().destroy();
    }

    @Override
    protected void onDestroy() {
        ThreadManager.getUIHandler().removeCallbacksAndMessages(null);
        super.onDestroy();
    }

	public void onGoBrowserActivityEvent() {

        RxBus.get().safetySubscribe(GoBrowserActivityEvent.class,this)
                .subscribe(new Consumer<GoBrowserActivityEvent>() {
            @Override
            public void accept(GoBrowserActivityEvent goBrowserActivityEvent) throws Exception {
                switch (goBrowserActivityEvent.getType()) {
                    case Constants.GOBROWSERTYPE_INIT:
                        if (!mIsShowWelcomeAd) {

                            gotoBrowserActivity(0);
                        }
                        break;
                    case Constants.GOBROWSERTYPE_AD:
                        mIsSkip = true;
                        showCountDown(goBrowserActivityEvent.getDelay());//显示广告倒计时
                        break;
                    default:
                        break;
                }
            }
        });

	}

    private void gotoBrowserActivity(long delay) {
        JuziApp.getInstance().isInitialized = true;
        ThreadManager.postDelayedTaskToUIHandler(new DelayRunnable(new WeakReference<>(this)),delay);
    }

    private static class DelayRunnable implements Runnable{
        WeakReference<WelcomeActivity> reference;

        DelayRunnable(WeakReference<WelcomeActivity> reference) {
            this.reference = reference;
        }

        @Override
        public void run() {
            WelcomeActivity outActivity = reference.get();
            if (ConfigManager.getInstance().isQuickSearch() && outActivity != null) {
                outActivity.showNotify(BrowserActivity.class);
            }

            Intent intent = (Intent) outActivity.getIntent().clone();
            intent.setClass(outActivity, BrowserActivity.class);
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && action.equals(Intent.ACTION_WEB_SEARCH)) {
                // 是系统发出的搜索请求，可以不再对intent进行处理
            } else {
                Uri uri = intent.getData();
                if (uri != null) {
                    try {
                        intent.putExtra(CommonData.ACTION_GOTO_URL, uri.toString());
                    } catch (Throwable e) {
                    }
                }
            }
            outActivity.startActivity(intent);
            outActivity.overridePendingTransition(0, 0);
            outActivity.finish();
        }
    }

    /**
     * 显示通知栏快捷搜索常驻通知
     */
    public void showNotify(Class<? extends Activity> activityClazz) {
        NotificationManager mNotificationManager = (NotificationManager) JuziApp.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notifi_search);
        Intent buttonIntent1 = new Intent(getApplicationContext(), activityClazz);
        buttonIntent1.setAction(CommonData.QUICK_SEARCH_SEARCH);
        PendingIntent pendingIntent1 = PendingIntent
                .getActivity(this, 1, buttonIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.search_area, pendingIntent1);
        Intent buttonIntent2 = new Intent(getApplicationContext(), activityClazz);
        buttonIntent2.setAction(CommonData.QUICK_SEARCH_SETTING);
        PendingIntent pendingIntent2 = PendingIntent
                .getActivity(this, 2, buttonIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.btn_gosetting, pendingIntent2);
        mBuilder.setContent(mRemoteViews)
                .setContentIntent(pendingIntent1)
                .setContentIntent(pendingIntent2)
                .setSmallIcon(R.drawable.notifi_icon);
        Notification notify = mBuilder.build();
        notify.flags = Notification.FLAG_ONGOING_EVENT;
        try {
            mNotificationManager.notify(CommonData.QUICK_SEARCH_ID, notify);
        } catch (Throwable e) {
        }
    }

    private void initAdView() {
        if (mIsShowWelcomeAd) {
            initFbNativeAd();
        }
    }


    /**
     * 一秒钟后加载广告
     */
    private void loadAdAfter1Second() {
        Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindToLifecycle())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        AdManager.getInstance().loadWelcomeAd(mNativeAdContainer);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        SimpleLog.e(TAG,"loadAdAfter1Second error"+throwable.getMessage());
                    }
                });

    }

    private void initFbNativeAd() {
        mNativeAdContainer = (RelativeLayout)findViewById(R.id.fa_welcome_area);

        this.nativeAdSkip = (TextView) mNativeAdContainer.findViewById(R.id.fb_welcome_skip);

        nativeAdSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSkip) {
                    gotoBrowserActivity(0);
                }
            }
        });

        loadAdAfter1Second();

    }

//    @Override
//    public Object getSystemService(@NonNull String name) {
//        return JuziApp.getInstance().getSystemService(name);
//    }


    private void showCountDown(final int delay) {
        if(delay==0){
            gotoBrowserActivity(0);
            return;
        }
        final String skipText = nativeAdSkip.getText().toString();
        Observable.interval(0,1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<Long>bindToLifecycle())
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(Long aLong) throws Exception {
                        return delay - aLong.intValue();
                    }
                })
                .take(delay + 1)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {//计时完成
                        gotoBrowserActivity(0);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        String text = skipText+integer;
                        nativeAdSkip.setText(text);
                    }
                });
    }

}
