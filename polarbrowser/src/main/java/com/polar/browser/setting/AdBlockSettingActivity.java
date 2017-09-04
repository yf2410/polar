package com.polar.browser.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.BitmapUtils;
import com.polar.browser.view.switchbutton.SwitchButton;

/**
 * Created by saifei on 17/3/21.
 */

public class AdBlockSettingActivity extends LemonBaseActivity implements CompoundButton.OnCheckedChangeListener,View.OnClickListener {
    private View adBlockLayout;
    private SwitchButton adBlockSwitch;
    private SwitchButton adBlockTipsSwitch;
    private View adBlockTipsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adblock_setting);
        initData();
        initViews();
        initListeners();
    }

    private void initData() {
        //把广告分享图片 资源文件保存到本地
        ThreadManager.postTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                BitmapUtils.saveResourceToFile(AdBlockSettingActivity.this,R.drawable.adblock_share_img,false);
            }
        });

    }

    private void initListeners() {
        adBlockSwitch.setOnCheckedChangeListener(this);
        adBlockTipsSwitch.setOnCheckedChangeListener(this);
        adBlockLayout.setOnClickListener(this);
        adBlockTipsLayout.setOnClickListener(this);

    }

    private void initViews() {
        adBlockSwitch = (SwitchButton) findViewById(R.id.sb_ad_block_switch);
        adBlockLayout = findViewById(R.id.ad_block_layout);
        adBlockTipsSwitch = (SwitchButton) findViewById(R.id.sb_ad_block_tips_switch);
        adBlockTipsLayout = findViewById(R.id.ad_block_tips_layout);
        boolean isAdBlock = ConfigManager.getInstance().isAdBlock();
        adBlockSwitch.setChecked(isAdBlock);
        adBlockTipsLayout.setVisibility(isAdBlock ? View.VISIBLE : View.GONE);
        adBlockTipsSwitch.setChecked(ConfigManager.getInstance().isAdBlockTip());
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        String type = "";
        switch (compoundButton.getId()) {
            case R.id.sb_ad_block_switch:
                adBlockTipsLayout.setVisibility(checked?View.VISIBLE:View.GONE);
                ConfigManager.getInstance().setEnableAdBlock(checked);
                type = checked ? GoogleConfigDefine.AB_ADBLOCK_SWITCH_OPEN : GoogleConfigDefine.AB_ADBLOCK_SWTICH_OFF;
                break;
            case R.id.sb_ad_block_tips_switch:
                ConfigManager.getInstance().setEnableAdBlockTip(checked);
                type = checked ? GoogleConfigDefine.AB_ADBLOCK_TIP_SWITCH_OPEN : GoogleConfigDefine.AB_ADBLOCK_TIP_SWITCH_OFF;
                break;
            default:
                break;
        }

        Statistics.sendOnceStatistics(GoogleConfigDefine.SETTING,GoogleConfigDefine.AB_ADBLOCK_MENU_CLICK, type);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ad_block_layout:
                adBlockSwitch.setChecked(!adBlockSwitch.isChecked());
                break;
            case R.id.ad_block_tips_layout:
                adBlockTipsSwitch.setChecked(!adBlockTipsSwitch.isChecked());
                break;
        }
    }
}
