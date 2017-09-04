package com.polar.browser.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.env.AppEnv;
import com.polar.browser.view.ToastClickListener;

/**
 * Created by lzk-pc on 2017/3/28.
 */

public class CustomToastActivity extends Activity {

    private static final String TAG = CustomToastActivity.class.getSimpleName();

    private LinearLayout ll_root;
    private TextView textTV;
    private TextView clickTV;
    public static final int DEFAULT_SHOW_TIME_LONG = 3000;
    public static final int DEFAULT_SHOW_TIME_SHORT = 1500;
    private int showTime = DEFAULT_SHOW_TIME_LONG;
    private View arrowIv;
    private View verticalLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_toast_dialog);
        initView();
        Intent intent = getIntent();
        if(intent != null){
            setShowTime(intent.getIntExtra(CustomToastUtils.TOAST_SHOW_TIME,showTime));
        }
        countDown(); //显示之前、首先获取显示时间
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if(intent != null){
            //Note: showtime只在onCreate中设置
            setMessage(intent.getStringExtra(CustomToastUtils.TOAST_MAIN_MESSAGE));
            setClickMessage(intent.getStringExtra(CustomToastUtils.TOAST_CLICK_MESSAGE));
            setShowArrow(intent.getBooleanExtra(CustomToastUtils.TOAST_IS_SHOW_BOTTOM_ARROW,false));
            int eventCode = intent.getIntExtra(CustomToastUtils.TOAST_CLICK_LISTENER,-1);
            String filePath = intent.getStringExtra(CustomToastUtils.TOAST_FILE_PATH);
            if (!TextUtils.isEmpty(filePath)) {
                setOnClickListener(eventCode, filePath);
            } else {
                setOnClickListener(eventCode);
            }
        }
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
            clickTV.setVisibility(View.VISIBLE);
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

    /**
     * 是否显示底部向下箭头
     * @param showArrow
     */
    void setShowArrow(boolean showArrow) {
        arrowIv.setVisibility(showArrow ? View.VISIBLE : View.GONE);
    }

    public void setOnClickListener(final int eventCode){
        clickTV.setVisibility(View.VISIBLE);
        clickTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastClickListener event = new ToastClickListener(eventCode);
                event.onClick(v);
                dismiss();
            }
        });
    }

    public void setOnClickListener(final int eventCode, final String filePath){
        clickTV.setVisibility(View.VISIBLE);
        clickTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastClickListener event = new ToastClickListener(eventCode, filePath);
                event.onClick(v);
                dismiss();
            }
        });
    }

    /**
     * 倒计时、显示设置时间后关闭
     */
    public void countDown() {
        clickTV.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        },showTime);
    }

    public void dismiss() {
        if(!CustomToastActivity.this.isFinishing()){
            CustomToastActivity.this.finish();
        }
    }

}
