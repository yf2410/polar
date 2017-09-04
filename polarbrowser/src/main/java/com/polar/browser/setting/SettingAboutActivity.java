package com.polar.browser.setting;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.api.RequestAPI;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.vclibrary.util.GooglePlayUtil;
/**
 * Created by FKQ on 2016/12/12.
 */

public class SettingAboutActivity extends LemonBaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_about);

        initView();
        initData();
    }

    private void initData() {
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
        findViewById(R.id.line_update).setOnClickListener(this);
        findViewById(R.id.line_face_book).setOnClickListener(this);
        findViewById(R.id.line_website).setOnClickListener(this);
        findViewById(R.id.line_user_group).setOnClickListener(this);
        findViewById(R.id.line_good_evaluation).setOnClickListener(this);
        findViewById(R.id.agreement).setOnClickListener(this);
        findViewById(R.id.terms).setOnClickListener(this);
        findViewById(R.id.title_bar).setOnClickListener(this);
        findViewById(R.id.app_icon).setOnClickListener(this);
        TextView appVersionName = (TextView) findViewById(R.id.app_version_name);
        appVersionName.setText("V"+SystemUtils.getVersionName(this));

    }


    @Override
    public void onClick(View v) {
        String url = null;
        switch (v.getId()) {
            case R.id.line_update:
            case R.id.line_good_evaluation:
                onCheckedUpdate();
                return;
            case R.id.line_face_book:
                url = RequestAPI.LOAD_VC_FACEBOOK;
                break;
            case R.id.line_website:
                url = RequestAPI.LOAD_OFFICIAL_WEBSITE;
                break;
            case R.id.line_user_group:
                url = RequestAPI.LOAD_USER_GROUP;
                break;
            case R.id.agreement:
                url = RequestAPI.LOAD_AGREEMENT;
                break;
            case R.id.terms:
                url = RequestAPI.LOAD_TERMS;
                break;
            case R.id.title_bar:
                mClickTitleBarTimes++;
                break;
            case R.id.app_icon:
                mClickIconTimes++;
                if (mClickTitleBarTimes >= 10 && mClickIconTimes >= 10) {
                    popSetMccDialog();
                }
            default:
                break;
        }
        if (!TextUtils.isEmpty(url)) {
            Intent intent = new Intent(this, BrowserActivity.class);
            intent.setAction(CommonData.ACTION_OPEN_PRODUCT_ABOUT);
            intent.putExtra(CommonData.SYSTEM_CONTENT_URL, url);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

    private void onCheckedUpdate() {
        try {
            GooglePlayUtil.launchAppDetail(this, AppEnv.PACKAGE_NAME, GooglePlayUtil.GOOGLE_PLAY_APP_PKGNAME);
        } catch (ActivityNotFoundException e) {
            CustomToastUtils.getInstance().showTextToast(getString(R.string.check_update_tip));
        }
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

    int mClickTitleBarTimes = 0;
    int mClickIconTimes = 0;
    private void popSetMccDialog() {
        ListDialog dialog = new ListDialog(this);
        String currentMcc = "Current mcc:" + SystemUtils.getMCC(JuziApp.getAppContext());
        String[] items = {currentMcc, "India(404)", "Brazil(724)", "Chile(730)", "China(460)"};
        dialog.setItems(items, 0);
        dialog.setOnItemClickListener(mSearchEngineClickListener);
        dialog.show();
    }
    private OnItemClickListener mSearchEngineClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    //no need to change mcc.
                    break;
                case 1:
                    ConfigManager.getInstance().setMCC("404"); // India
                    break;
                case 2:
                    ConfigManager.getInstance().setMCC("724"); // Brazil
                    break;
                case 3:
                    ConfigManager.getInstance().setMCC("730"); // Chile
                    break;
                case 4:
                    ConfigManager.getInstance().setMCC("460"); // China
                    break;
                default:
                    break;
            }
        }
    };
}
