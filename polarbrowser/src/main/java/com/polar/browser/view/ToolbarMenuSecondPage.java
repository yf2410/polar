package com.polar.browser.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.bean.events.BottomMenuNotifyBrowserEvent;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by saifei on 17/3/16.
 * 菜单栏第二页，目前两页
 * 当前页内容：
 * 添加收藏，历史/收藏夹,下载,保存网页，PC模式，无图模式，设置，退出
 *
 */

public class ToolbarMenuSecondPage extends LinearLayout implements View.OnClickListener{

    private TextView tvShare;
    private TextView tvScreenShot;
    private TextView tvFindInPage;
    private TextView tvClean;
    private TextView tvNightMode;
    private TextView tvFullScreen;
    private TextView tvAdBlock;
    private TextView tvFontSize;

    public ToolbarMenuSecondPage(Context context) {
        this(context, null);
    }

    public ToolbarMenuSecondPage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.toolbar_menu_second_page, this);
        setOrientation(LinearLayout.VERTICAL);
        setBackgroundColor(Color.WHITE);

        initViews();
        initListeners();

    }



    private void initViews() {

        tvScreenShot = (TextView)findViewById(R.id.btn_screen_shot);
        tvFindInPage = (TextView)findViewById(R.id.btn_find_in_page);
        tvShare = (TextView)findViewById(R.id.btn_share);
        tvNightMode = (TextView) findViewById(R.id.btn_night_mode);
        tvFullScreen =(TextView) findViewById(R.id.btn_fullscreen);
        tvClean = (TextView) findViewById(R.id.btn_clean_data);
        tvAdBlock = (TextView) findViewById(R.id.btn_ad_block);
        tvFontSize = (TextView) findViewById(R.id.btn_font_size);

    }

    private void initListeners() {
        tvScreenShot.setOnClickListener(this);
        tvFindInPage.setOnClickListener(this);
        tvShare.setOnClickListener(this);
        tvNightMode.setOnClickListener(this);
        tvFullScreen.setOnClickListener(this);
        tvClean.setOnClickListener(this);
        tvAdBlock.setOnClickListener(this);
        tvFontSize.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        boolean isCurrentHome = TabViewManager.getInstance().isCurrentHome();
        String type = "";
        switch (v.getId()) {
            case R.id.btn_screen_shot:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.SCREEN_SHOT));
                type = GoogleConfigDefine.MENU_SCREEN_SHOT;
                break;
            case R.id.btn_find_in_page:
                if(isCurrentHome) return;
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.FIND_IN_PAGE));
                type = GoogleConfigDefine.MENU_FIND_PAGE;
                break;
            case R.id.btn_share:
                if(isCurrentHome)return;
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.SHARE));
                type = GoogleConfigDefine.MENU_SHARE_PAGE;
                break;
            case R.id.btn_night_mode:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.NIGHT_MODE));
                break;
            case R.id.btn_fullscreen:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.FULL_SCREEN));
                break;
            case R.id.btn_clean_data:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.CLEAN_DATA));
                type = GoogleConfigDefine.MENU_CLEAR_DATA;
                break;
            case R.id.btn_ad_block:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.AD_BLOCK));
                type = GoogleConfigDefine.MENU_AD_BLOCL;
                break;
            case R.id.btn_font_size:
                EventBus.getDefault().post(new BottomMenuNotifyBrowserEvent(BottomMenuNotifyBrowserEvent.Menu.FONT_SIZE));
                type = GoogleConfigDefine.MENU_FONT_SIZE;
                break;
            default:
                break;
        }

        sendGoogleStatistics(type);
    }

    public void refreshBtnAdBlockUI(boolean isAdBlock) {
//        if (isAdBlock) {
//            tvAdBlock.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.menu_ad_block_on, 0, 0);
//        } else {
            tvAdBlock.setTextColor(getResources().getColor(R.color.black87));
            tvAdBlock.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.menu_ad_block_off, 0, 0);
//        }
    }

    public void refreshBtnNightModeUI(boolean isNightMode) {
        if (isNightMode) {
            //tvNightMode.setTextColor(getResources().getColor(R.color.theme_green));
            tvNightMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.menu_night_on, 0, 0);
        } else {
            tvNightMode.setTextColor(getResources().getColor(R.color.black87));
            tvNightMode.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.menu_night_off, 0, 0);
        }
    }

    public void refreshFullScreenUI(boolean fullScreen) {
        if (fullScreen) {
            // tvFullScreen.setTextColor(getResources().getColor(R.color.theme_green));
            tvFullScreen.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.menu_fullscreen_open, 0, 0);
            sendGoogleStatistics(GoogleConfigDefine.MENU_FULL_SCREEN);
        } else {
            tvFullScreen.setTextColor(getResources().getColor(R.color.black87));
            tvFullScreen.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.menu_fullscreen, 0, 0);
        }
    }

    public void refreshShareUI() {
        setTextViewBg(tvShare,R.drawable.menu_share,R.drawable.menu_share_disable);
    }

    public void refreshFontSizeUI() {
        setTextViewBg(tvFontSize,R.drawable.menu_font_size_enable,R.drawable.menu_font_size_disable);
    }

    public void refreshFindInPageUI() {
        setTextViewBg(tvFindInPage,R.drawable.menu_findinpage,R.drawable.menu_findinpage_disable);
    }

    private void setTextViewBg(TextView tv, int enableId, int disableId) {
        boolean isCurrentHome = TabViewManager.getInstance().isCurrentHome();
        tv.setCompoundDrawablesWithIntrinsicBounds(0, isCurrentHome ? disableId : enableId
                , 0, 0);
        tv.setBackgroundResource(isCurrentHome ? R.color.transparent : R.drawable.menu_item_selector);
        tv.setTextColor(isCurrentHome? Color.GRAY:getResources().getColor(R.color.black87));
    }

    private void sendGoogleStatistics(String type) {
        Statistics.sendOnceStatistics(GoogleConfigDefine.MENU_EVENTS, type);
    }
}
