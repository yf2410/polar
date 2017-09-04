package com.polar.browser.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBaseDialog;
import com.polar.browser.env.AppEnv;


/**
 * Created by yd_lzk on 2016/11/22.
 */

public class ToastDialog extends CommonBaseDialog {
    private static final String TAG = ToastDialog.class.getSimpleName();
    private  Activity activity;
    private LinearLayout ll_root;
    private TextView textTV;
    private TextView clickTV;
    public static final int DEFAULT_SHOW_TIME_LONG = 3000;
    public static final int DEFAULT_SHOW_TIME_SHORT = 1500;
    private int showTime = DEFAULT_SHOW_TIME_LONG;
    private View arrowIv;
    private View verticalLine;


    public ToastDialog(Context context) {
        super(context, R.style.custom_toast_dialog);
        setContentView(R.layout.custom_toast_dialog);
        setCanceledOnTouchOutside(false);
        this.activity = (Activity)context;
        initView();
    }

    public ToastDialog(Context context, int theme) {
        super(context, theme);
    }

    public void initView(){

        ll_root = (LinearLayout) this.findViewById(R.id.ll_root);

        textTV = (TextView) this.findViewById(R.id.text_tip);
        clickTV = (TextView)this.findViewById(R.id.tv_click);
        clickTV.setVisibility(View.GONE);

        verticalLine = findViewById(R.id.tv_middle_vertical_line);

//        clickTV.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG ); //下划线
//        clickTV.getPaint().setAntiAlias(true);//抗锯齿

        arrowIv = findViewById(R.id.bottom_arrow_iv);

        //显示位置
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL);
        int marginBottom = (int) JuziApp.getAppContext().getResources().getDimension(R.dimen.dialog_loading_margin_bottom_height);
        lp.x = 0; // 新位置X坐标
        lp.y = AppEnv.SCREEN_HEIGHT/2 - marginBottom; // 新位置Y坐标
        dialogWindow.setAttributes(lp);
    }

    /**
     * 显示内容
     * @param message
     */
    public void setMessage(String message){
        if(message != null){
            textTV.setText(message);
        }
    }

    /**
     * 可点击部分文本
     * @param message
     */
    public void setClickMessage(String message){
        if(message != null){
            clickTV.setText(message);
            verticalLine.setVisibility(View.VISIBLE);
        }
    }

    public void setBackgroundResource(int resource){
        ll_root.setBackgroundResource(resource);
    }


    /**
     * 显示时间 unit毫秒
     */
    public void setShowTime(int showTime){
        this.showTime = showTime;
    }

    public void setOnClickListener(View.OnClickListener listener){
        if(listener!=null){
            clickTV.setVisibility(View.VISIBLE);
            clickTV.setOnClickListener(listener);
        }
    }

    @Override
    public void show() {
        super.show();
        clickTV.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();

       /*         if(activity instanceof DialogToastActivity){
                    activity.finish();
                }*/

            }
        },showTime);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }


    void setShowArrow(boolean showArrow) {
        arrowIv.setVisibility(showArrow ? View.VISIBLE : View.GONE);
    }
}
