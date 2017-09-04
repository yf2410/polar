package com.polar.browser.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.downloadfolder.SettingDownloadPath;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.view.switchbutton.SwitchButton;

/**
 * Created by FKQ on 2016/8/22.
 */

public class SettingDownloadActivity
        extends LemonBaseActivity implements IConfigObserver,
        View.OnClickListener,android.widget.CompoundButton.OnCheckedChangeListener{

    /**
     * 仅wifi开启时下载
     **/
    private SwitchButton mSbWifiDownload;
    private TextView mTvDownloadPath;
    private CommonTitleBar mCommonTitleBar;
    private BroadcastReceiver mReceiver;
    /**
     * 存储路径1根路径
     **/
    private String mDest1Root;
    /**
     * 存储路径2根路径
     **/
    private String mDest2Root;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_setting);
        initView();
        initData();
        initListener();
        initReceiver();
    }

    private void initView() {
        mCommonTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
        mTvDownloadPath = (TextView) findViewById(R.id.tv_download_path);
        mSbWifiDownload = (SwitchButton) findViewById(R.id.sb_download_wifi_lock);
    }

    private void initData() {
        // 读取配置，设置初始状态
        boolean isWifiDownloadEnable = ConfigManager.getInstance().isEnableOnlyWifiDownload();
        mSbWifiDownload.setChecked(isWifiDownloadEnable);
        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        mDest1Root = VCStoragerManager.getInstance().getPhoneStorage();
        mDest2Root = VCStoragerManager.getInstance().getSDCardStorage();
        // 当前存储位置
        String currentFolder = ConfigWrapper.get(CommonData.KEY_DOWN_ROOT, VCStoragerManager.getInstance().getDefaultDownloadDirPath());
        updateCurrentDest(currentFolder);
        ConfigManager.getInstance().registerObserver(this);
    }

    private void initListener() {
        findViewById(R.id.line_download_wifi).setOnClickListener(this);
        findViewById(R.id.line_download_path).setOnClickListener(this);
        mSbWifiDownload.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        ConfigManager.getInstance().unregisterObserver(this);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.line_download_wifi:
                // 切换是否开启仅wifi下载
                onWifiDownload();
                break;
            case R.id.line_download_path:
                // 存储路径
                onDownloadPathLineClick();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sb_download_wifi_lock:
                if (isChecked != ConfigManager.getInstance().isEnableOnlyWifiDownload()) {
                    ConfigManager.getInstance().setEnableOnlyWifiDownload(isChecked);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void notifyChanged(String key, final boolean value) {

        if (key.equals(ConfigDefine.ENABLE_ONLY_WIFI_DOWNLOAD)) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mSbWifiDownload.setChecked(value);
                    sendOnlyWifiDownloadSwitchChanged(value);
                }
            };
            ThreadManager.postTaskToUIHandler(r);
        }
    }

    @Override
    public void notifyChanged(String key, String value) {

    }

    @Override
    public void notifyChanged(String key, int value) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
    }

    private void onWifiDownload() {
        mSbWifiDownload.slideToChecked(!mSbWifiDownload.isChecked());
    }

    private void onDownloadPathLineClick() {
        startActivity(new Intent(this, SettingDownloadPath.class));
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    private void sendOnlyWifiDownloadSwitchChanged(boolean isOnlyWifiDownload) {
        Intent intent = new Intent(CommonData.ACTION_DOWNLOAD_ONLY_WIFI);
        intent.putExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, isOnlyWifiDownload);
        JuziApp.getInstance().sendBroadcast(intent);
    }

    private void initReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TextUtils.equals(action, CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED)) {
                    // 自定义了下载文件夹 改变
                    SimpleLog.e("APP", "ACTION_DOWNLOAD_FOLDER_CHANGED");
                    String currentFolder = intent.getStringExtra(CommonData.KEY_DOWN_ROOT);
                    if (!TextUtils.isEmpty(currentFolder)) {
                        if (mTvDownloadPath != null) {
                            updateCurrentDest(currentFolder);
                        }
                    }
                }
            }
        };
        // 注册Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    protected void updateCurrentDest(String currentFolder) {
        if (currentFolder == null) {
            return;
        }
        if (!TextUtils.isEmpty(mDest1Root) && currentFolder.startsWith(mDest1Root)) {
            String path = currentFolder.replace(mDest1Root, getString(R.string.download_folder_phone));
            mTvDownloadPath.setText(path);
            ConfigWrapper.put(CommonData.KEY_CURRENT_DOWN_FOLDER, path);
            ConfigWrapper.apply();
        }
        if (!TextUtils.isEmpty(mDest2Root) && currentFolder.startsWith(mDest2Root)) {
            String path = currentFolder.replace(mDest2Root, getString(R.string.download_folder_sd));
            mTvDownloadPath.setText(path);
            ConfigWrapper.put(CommonData.KEY_CURRENT_DOWN_FOLDER, path);
            ConfigWrapper.apply();
        }
        if (TextUtils.isEmpty(mDest2Root) && !TextUtils.isEmpty(mDest1Root)) {
            String path = ConfigWrapper.get(CommonData.KEY_CURRENT_DOWN_FOLDER, "");
            mTvDownloadPath.setText(path);
        }
    }

}
