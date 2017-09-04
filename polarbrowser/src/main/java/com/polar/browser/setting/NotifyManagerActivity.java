package com.polar.browser.setting;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.view.switchbutton.SwitchButton;

/**
 * Created by FKQ on 2017/3/22.
 */

public class NotifyManagerActivity extends LemonBaseActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SwitchButton mSbNotifyNews;
    private SwitchButton mSbNotifySystem;
    private SwitchButton mSbNotifyFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_setting);
        initView();
        initActivityConfigure();
    }

    private void initActivityConfigure() {
        boolean isScreenLock = ConfigManager.getInstance().isScreenLock();
        boolean isFullScreen = ConfigManager.getInstance().isFullScreen();
        if (isScreenLock) {
            // 竖屏锁定开启，若不是竖屏，强制切换为竖屏
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            // 设置跟随系统
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        SysUtils.setFullScreen(this, isFullScreen);
    }

    private void initView() {
        mSbNotifyNews = (SwitchButton) findViewById(R.id.sb_notify_news);
        mSbNotifySystem = (SwitchButton) findViewById(R.id.sb_notify_system);
        mSbNotifyFacebook = (SwitchButton) findViewById(R.id.sb_notify_facebook);

        mSbNotifyNews.setChecked(ConfigManager.getInstance().getNotifyNewsEngine());
        mSbNotifySystem.setChecked(ConfigManager.getInstance().getNotifySystemEngine());
        mSbNotifyFacebook.setChecked(ConfigManager.getInstance().getFbMessageNotificationEngine());

        findViewById(R.id.line_notify_news).setOnClickListener(this);
        findViewById(R.id.line_notify_system).setOnClickListener(this);
        findViewById(R.id.line_notify_facebook).setOnClickListener(this);
        mSbNotifyNews.setOnCheckedChangeListener(this);
        mSbNotifySystem.setOnCheckedChangeListener(this);
        mSbNotifyFacebook.setOnCheckedChangeListener(this);

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.line_notify_news:
                if (mSbNotifyNews.isShown()) {
                    mSbNotifyNews.slideToChecked(!mSbNotifyNews.isChecked());
                }
                break;
            case R.id.line_notify_system:
                if (mSbNotifySystem.isShown()) {
                    mSbNotifySystem.slideToChecked(!mSbNotifySystem.isChecked());
               }
                break;
            case R.id.line_notify_facebook:
                if (mSbNotifyFacebook.isShown()) {
                    mSbNotifyFacebook.slideToChecked(!mSbNotifyFacebook.isChecked());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sb_notify_news:
                if (isChecked != ConfigManager.getInstance().getNotifyNewsEngine()) {
                    ConfigManager.getInstance().setNotifyNewsEngine(isChecked);
                }
                break;
            case R.id.sb_notify_system:
                if (isChecked != ConfigManager.getInstance().getNotifySystemEngine()) {
                    ConfigManager.getInstance().setNotifySystemEngine(isChecked);
                }
                break;
            case R.id.sb_notify_facebook:
                if (isChecked != ConfigManager.getInstance().getFbMessageNotificationEngine()) {
                    ConfigManager.getInstance().setFbMessageNotificationEngine(isChecked);
                }
                if (isChecked) {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.SETTING, GoogleConfigDefine.SETTING_NOTICE, GoogleConfigDefine.FB_NOTIFY_SWITCH_ON);
                } else {
                    Statistics.sendOnceStatistics(GoogleConfigDefine.SETTING, GoogleConfigDefine.SETTING_NOTICE, GoogleConfigDefine.FB_NOTIFY_SWITCH_OFF);
                }
                break;
            default:
                break;
        }
    }
}
