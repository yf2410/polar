package com.polar.browser.common.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.env.AppEnv;

/**
 * Created by yd_lzk on 2016/11/22.
 */

public class LoadingDialog extends CommonBaseDialog {

    private static final String TAG = LoadingDialog.class.getSimpleName();
    private Context mContext;
    private ValueAnimator valueAnimator;
    private TextView loadingMsg;

    public LoadingDialog(Context context) {
        super(context, R.style.loading_dialog);
        setContentView(R.layout.custom_loading_dialog);
        mContext = context;
        setCanceledOnTouchOutside(false);
        initView();
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
    }

    public void initView(){

        loadingMsg = (TextView) findViewById(R.id.loading_text);

        final ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);
        valueAnimator = ValueAnimator.ofInt(1,13);
        LinearInterpolator ll = new LinearInterpolator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int i = (Integer) animation.getAnimatedValue();
                ObjectAnimator.ofFloat(loadingIcon, "rotation", 30*(i-1),30*i).start();
            }
        });
        valueAnimator.setDuration(1200);
        valueAnimator.setInterpolator(ll);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);


        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL);

        int marginBottom = (int)mContext.getResources().getDimension(R.dimen.dialog_loading_margin_bottom_height);

        lp.x = 0; // 新位置X坐标
        lp.y = AppEnv.SCREEN_HEIGHT/2 - marginBottom; // 新位置Y坐标
        dialogWindow.setAttributes(lp);
    }

    public void setMessage(String message){
        if(message == null) return;
        loadingMsg.setText(message);
    }

    @Override
    public void show() {
        super.show();
        if(valueAnimator!=null){
            valueAnimator.start();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(valueAnimator!=null){
            valueAnimator.cancel();
        }
    }
}
