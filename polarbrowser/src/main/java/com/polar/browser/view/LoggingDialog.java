package com.polar.browser.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;

/**
 * Created by yd_lzk on 2017/4/14.
 * 登录加载对话框
 */

public class LoggingDialog extends CommonBaseDialog{

    private ObjectAnimator objectAnimator;
    private TextView dialogDescription;

    private OnShowTimeoutListener timeoutListener;
    private int timeout = -1;
    private static final int TIMEOUT_MIN_TIME = 5000;

    public LoggingDialog(Context context, int theme) {
        super(context, R.style.common_dialog);
    }

    public LoggingDialog(Context context,int textStr,int image) {
        super(context, R.style.common_dialog);
        setContentView(R.layout.dialog_feed_back);
        dialogDescription = (TextView) findViewById(R.id.description);
        dialogDescription.setText(context.getResources().getString(textStr));

        setCanceledOnTouchOutside(false);
        final ImageView dialogIcon = (ImageView) findViewById(R.id.icon_dialog);
        dialogIcon.setImageDrawable(context.getResources().getDrawable(image));
        objectAnimator = ObjectAnimator.ofFloat(dialogIcon, "rotation", 0,360);
        objectAnimator.setDuration(700);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.setRepeatMode(ObjectAnimator.RESTART);
    }

    public interface OnShowTimeoutListener{
        void showTimeout();
    }

    public void setTimeoutListener(OnShowTimeoutListener timeoutListener, int time){
        if(timeoutListener != null){
            this.timeoutListener = timeoutListener;
            if(time < TIMEOUT_MIN_TIME){
                time = TIMEOUT_MIN_TIME;
            }
            this.timeout = time;
        }
    }

    @Override
    public void show() {
        super.show();
        if(objectAnimator!=null){
            objectAnimator.start();
        }
        if(timeoutListener != null){
            dialogDescription.postDelayed(new Runnable() {
                @Override
                public void run() {
                    timeoutListener.showTimeout();
                }
            },timeout);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(objectAnimator!=null){
            objectAnimator.cancel();
        }
    }

}
