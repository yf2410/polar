package com.polar.browser.push.fbnotify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.push.IdGenerator;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SimpleLog;

/**
 * Created by FKQ on 2017/3/23.
 */

public class FbNotifyManager {

    public static final String FB_URL_PREFIX = "facebook.com";
    public static final String FB_URL_WEBSITE = "http://m.facebook.com";
    public static final String FB_URL_HOME = "http://m.facebook.com/home.php";

    public static final String FB_NOTIFY_MEG_NUMBER = "FB_NOTIFY_MEG_NUMBER";

    public static final String FB_NOTIFY_TYPE_MEG = "messages";
    public static final String FB_NOTIFY_TYPE_REQUEST = "requests";
    public static final String FB_NOTIFY_TYPE_NOTIFY = "notifications";

    private long mAnimationTime = 300;
    public static int DISMISS_DURATION = 4000;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;
    private float mDownX;
    private float mDownY;
    private int mViewWidth;
    private View rootView;
    private WindowManager mWindowManager;
    private boolean mIsAdded;

    private static FbNotifyManager mInstance;
    private TextView mFbNotify;

    private FbNotifyManager(){

    }
    public static FbNotifyManager getInstance() {
        if (mInstance == null) {
            synchronized (FbNotifyManager.class) {
                if (mInstance == null) {
                    mInstance = new FbNotifyManager();
                }
            }
        }
        return mInstance;
    }

    public void showFbNoticeDialog(Activity activity) {
        final CommonDialog dialog = new CommonDialog(activity, activity.getString(R.string.new_function_tip),
                activity.getString(R.string.fb_notify_push_tip));
        dialog.setBtnCancel(activity.getString(R.string.next_time), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_NOTICE, GoogleConfigDefine.FB_NOTIFY_NOTICE_NEXT);
            }
        });
        dialog.setBtnOk(activity.getString(R.string.setting_editlogo_type_ok), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigManager.getInstance().setFbMessageNotificationEngine(true);
                dialog.dismiss();
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_NOTICE, GoogleConfigDefine.FB_NOTIFY_NOTICE_OK);
            }
        });
        dialog.show();
    }

    public void showFbNotifyMeg(final Context context, final boolean isAppFg,
                                final String fbNotifyType, final String fbNotifyMsgUrl) {
        final String notifyMes = getFbToastMsgByType(fbNotifyType);

        if (mWindowManager != null && rootView != null && mIsAdded) {
            mWindowManager.removeView(rootView);
            mIsAdded = false;
        }
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        rootView =  View.inflate(context, R.layout.notify_fb_msg, null);
        mFbNotify = (TextView) rootView.findViewById(R.id.tv_fb_notify_msg);
        mFbNotify.setText(notifyMes);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handleActionDown(event);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        return handleActionMove(event);
                    case MotionEvent.ACTION_UP:
                        handleActionUp(event,context,fbNotifyType,fbNotifyMsgUrl);
                        break;
                }
                return true;
            }
        });

//        rootView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mWindowManager != null && rootView != null && mIsAdded) {
//                    mWindowManager.removeView(rootView);
//                    mIsAdded = false;
//                    SimpleLog.d("--MyLog--", "FB点击弹窗移除");
//                }
//                SimpleLog.d("--MyLog--", "FB点击弹窗");
//                Intent intent = new Intent(context, BrowserActivity.class);
//                intent.setAction(CommonData.ACTION_CLICK_FB_NOTIFICATION);
//                intent.putExtra(CommonData.FB_NOTIFY_DATA_LINK, fbNotifyMsgUrl);
//                intent.putExtra(FB_NOTIFY_MEG_NUMBER,"");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//                sendFbNotifiationClickStatistics(fbNotifyType);
//                SimpleLog.d("--MyLog--", "rootView--onClick");
//            }
//        });

        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;

        int flags = 0;
        int type = 0;
        //api版本大于19的时候 TYPE_TOAST用这个参数 可以绕过绝大多数对悬浮窗权限的限制，比如miui
        //在小于19的时候 其实也是可以绕过的，只不过小于19你绕过了以后 点击事件就无效了 所以小于19的时候
        //还是用TYPE_PHONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSLUCENT);

//        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
//                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                        | WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP;
        mWindowManager.addView(rootView, layoutParams);
        mIsAdded = true;
        sendFbNotifiationShowStatistics(fbNotifyType);
        ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                if (mWindowManager != null && rootView != null && mIsAdded) {
                    mWindowManager.removeView(rootView);
                    mIsAdded = false;
                    SimpleLog.d("--MyLog--", "FB弹窗消失");
                    sendFbNotifiation(context,notifyMes,fbNotifyMsgUrl);
                    int fbNotifyMsgNumber = ConfigManager.getInstance().getFbNotifyMsgNumber();
                    ConfigManager.getInstance().setFbNotifyMsgNumber(++fbNotifyMsgNumber);
                }
//                if (!isAppFg) {
//                    sendFbNotifiation(context,notifyMes,fbNotifyMsgUrl);
//                    ConfigManager.getInstance().setAddFbNotifyFlag(true);
//                    int fbNotifyMsgNumber = ConfigManager.getInstance().getFbNotifyMsgNumber();
//                    ConfigManager.getInstance().setFbNotifyMsgNumber(++fbNotifyMsgNumber);
//                }
            }
        }, DISMISS_DURATION);
    }

    private void handleActionDown(MotionEvent ev) {
        mDownX = ev.getX();
        mDownY = ev.getY();
        mViewWidth = rootView.getWidth();
        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);
    }

    private boolean handleActionMove(MotionEvent ev) {
        if (mVelocityTracker == null) {
            return true;
        }
        float deltaX = ev.getX() - mDownX;
        float deltaY = ev.getY() - mDownY;
        if (Math.abs(deltaX) > AppEnv.MIN_SLIDING && Math.abs(deltaY) < AppEnv.MIN_SLIDING) {
            mSwiping = true;
            MotionEvent cancelEvent = MotionEvent.obtain(ev);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                    (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
//            onTouchEvent(cancelEvent);
        }
        if (mSwiping) {
            rootView.setTranslationX(deltaX);
            rootView.setAlpha(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
            return true;
        } else {
            rootView.setTranslationX(0);
            rootView.setAlpha(1);
            return true;
        }
    }

    private void handleActionUp(MotionEvent ev,final Context context, final String fbNotifyType, final String fbNotifyMsgUrl) {
        float deltaX = ev.getX() - mDownX;
        float deltaY = ev.getY() - mDownY;
        if (mVelocityTracker == null || (Math.abs(deltaX) < AppEnv.MIN_SLIDING && Math.abs(deltaY) < AppEnv.MIN_SLIDING)) {
            if (mWindowManager != null && rootView != null && mIsAdded) {
                mWindowManager.removeView(rootView);
                mIsAdded = false;
                SimpleLog.d("--MyLog--", "FB点击弹窗移除");
            }
            SimpleLog.d("--MyLog--", "FB点击弹窗");
            Intent intent = new Intent(context, BrowserActivity.class);
            intent.setAction(CommonData.ACTION_CLICK_FB_NOTIFICATION);
            intent.putExtra(CommonData.FB_NOTIFY_DATA_LINK, fbNotifyMsgUrl);
            intent.putExtra(FB_NOTIFY_MEG_NUMBER,"");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            sendFbNotifiationClickStatistics(fbNotifyType);
            return;
        }
        mVelocityTracker.computeCurrentVelocity(1000);
        float velocityX = Math.abs(mVelocityTracker.getXVelocity());
        float velocityY = Math.abs(mVelocityTracker.getYVelocity());
        boolean dismiss = false;
        boolean dismissRight = false;
        if (Math.abs(deltaX) > mViewWidth / 3) {
            dismiss = true;
            dismissRight = deltaX > 0;
        } else if (mMinFlingVelocity <= velocityX
                && velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
            dismiss = true;
            dismissRight = mVelocityTracker.getXVelocity() > 0;
        }
        if (dismiss) {
//            mAnimationTime = (int) ((getWidth() - Math.abs(deltaX)) / getWidth() * mAnimationTime);
            rootView.animate()
                    .translationX(dismissRight ? mViewWidth : -mViewWidth)//X轴方向的移动距离
                    .alpha(0)
                    .setDuration(mAnimationTime)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
//                            mWindowManager.removeView(rootView);
                            if (mWindowManager != null && rootView != null && mIsAdded) {
                                mWindowManager.removeView(rootView);
                                mIsAdded = false;
                                SimpleLog.d("--MyLog--", "FB弹窗滑动消失移除");
                            }
                            SimpleLog.d("--MyLog--", "FB弹窗滑动消失");
                            sendFbNotifiationDelStatistics(fbNotifyType);
                        }
                    });
        } else {
            rootView.animate()
                    .translationX(0)
                    .alpha(1)
                    .setDuration(mAnimationTime).setListener(null);
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        mSwiping = false;
    }

    private void sendFbNotifiation(Context context, String notifyMes, String url) {
        Intent intent = new Intent(context, BrowserActivity.class);
        intent.setAction(CommonData.ACTION_CLICK_FB_NOTIFICATION);
        intent.putExtra(CommonData.FB_NOTIFY_DATA_LINK, url);
        intent.putExtra(FB_NOTIFY_MEG_NUMBER,FB_NOTIFY_MEG_NUMBER);
        PendingIntent pendingIntent = PendingIntent.getActivity(JuziApp.getInstance().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.fb_notify_logo_vc);
        builder.setContentTitle(context.getString(R.string.fb_notify_title));
        builder.setContentText(notifyMes);
//        builder.setColor(context.getResources().getColor(R.color.theme_green));
        builder.setTicker(null);
        builder.setAutoCancel(true);
        builder.setSound(defaultSoundUri);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) JuziApp.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(IdGenerator.getInstance().generateFbNotifyMsgId(), notification);
    }

    public void sendFbNotifiationPushStatistics(String fbNotifyType) {
        switch (fbNotifyType) {
            case FB_NOTIFY_TYPE_MEG:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_TYPE, GoogleConfigDefine.FB_NOTIFY_TYPE_MSG);
                break;
            case FB_NOTIFY_TYPE_REQUEST:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_TYPE, GoogleConfigDefine.FB_NOTIFY_TYPE_REQUEST);
                break;
            case FB_NOTIFY_TYPE_NOTIFY:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_TYPE, GoogleConfigDefine.FB_NOTIFY_TYPE_NOTICE);
                break;
            default:
                break;
        }
    }

    private void sendFbNotifiationClickStatistics(String fbNotifyType) {
        switch (fbNotifyType) {
            case FB_NOTIFY_TYPE_MEG:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_CLICK, GoogleConfigDefine.FB_NOTIFY_CLICK_MSG);
                break;
            case FB_NOTIFY_TYPE_REQUEST:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_CLICK, GoogleConfigDefine.FB_NOTIFY_CLICK_REQUEST);
                break;
            case FB_NOTIFY_TYPE_NOTIFY:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_CLICK, GoogleConfigDefine.FB_NOTIFY_CLICK_NOTICE);
                break;
        }
    }

    private void sendFbNotifiationDelStatistics(String fbNotifyType) {
        switch (fbNotifyType) {
            case FB_NOTIFY_TYPE_MEG:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_DEL, GoogleConfigDefine.FB_NOTIFY_DEL_MSG);
                break;
            case FB_NOTIFY_TYPE_REQUEST:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_DEL, GoogleConfigDefine.FB_NOTIFY_DEL_REQUEST);
                break;
            case FB_NOTIFY_TYPE_NOTIFY:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_DEL, GoogleConfigDefine.FB_NOTIFY_DEL_NOTICE);
                break;
        }
    }

    private void sendFbNotifiationShowStatistics(String fbNotifyType) {
        switch (fbNotifyType) {
            case FB_NOTIFY_TYPE_MEG:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_SHOW, GoogleConfigDefine.FB_NOTIFY_SHOW_MSG);
                break;
            case FB_NOTIFY_TYPE_REQUEST:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_SHOW, GoogleConfigDefine.FB_NOTIFY_SHOW_REQUEST);
                break;
            case FB_NOTIFY_TYPE_NOTIFY:
                Statistics.sendOnceStatistics(GoogleConfigDefine.FB_NOTIFY, GoogleConfigDefine.FB_NOTIFY_SHOW, GoogleConfigDefine.FB_NOTIFY_SHOW_NOTICE);
                break;
        }
    }

    private String getFbToastMsgByType(String fbNotifyType) {
        switch (fbNotifyType) {
            case FB_NOTIFY_TYPE_MEG:
                return JuziApp.getAppContext().getString(R.string.fb_notify_msg_tip);
            case FB_NOTIFY_TYPE_REQUEST:
                return JuziApp.getAppContext().getString(R.string.fb_notify_request_tip);
            case FB_NOTIFY_TYPE_NOTIFY:
                return JuziApp.getAppContext().getString(R.string.fb_notify_notice_tip);
            default:
                return JuziApp.getAppContext().getString(R.string.fb_notify_msg_tip);
        }
    }

}
