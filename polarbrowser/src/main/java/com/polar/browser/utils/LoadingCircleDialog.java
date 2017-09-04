package com.polar.browser.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;
import com.polar.browser.env.AppEnv;


/**
 * Created by yd_lzk on 2016/11/22.
 */

public class LoadingCircleDialog extends CommonBaseDialog {

    private static final String TAG = LoadingCircleDialog.class.getSimpleName();
    private Context mContext;
    private ObjectAnimator objectAnimator;

    public LoadingCircleDialog(Context context) {
        super(context, R.style.custom_toast_dialog);
        setContentView(R.layout.custom_loading_circle_dialog);
        mContext = context;
        setCanceledOnTouchOutside(false);
        initView();
    }

    public LoadingCircleDialog(Context context, int theme) {
        super(context, theme);
    }

    public void initView(){

        final ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);
        objectAnimator = ObjectAnimator.ofFloat(loadingIcon, "rotation", 0,360);
        objectAnimator.setDuration(1000);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        objectAnimator.setRepeatMode(ObjectAnimator.RESTART);

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL);

        int marginBottom = (int) JuziApp.getAppContext().getResources().getDimension(R.dimen.dialog_loading_margin_bottom_height);

        lp.x = 0; // 新位置X坐标
        lp.y = AppEnv.SCREEN_HEIGHT/2 - marginBottom; // 新位置Y坐标
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void show() {
        super.show();
        if(objectAnimator!=null){
            objectAnimator.start();
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
