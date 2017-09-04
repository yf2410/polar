package com.polar.browser.setting;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.video.share.CustomShareDialog;

import java.text.DecimalFormat;

/**
 * Created by saifei on 17/3/21.
 */

public class AdBlockSettingTopView extends RelativeLayout {

    private View adBlockShareIv;
    private int adBlockCount;

    public AdBlockSettingTopView(Context context) {
        this(context, null);
    }

    public AdBlockSettingTopView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initListeners();
    }

    private void initListeners() {
        adBlockShareIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String prefix = String.format(getResources().getString(R.string.ad_block_share_content),adBlockCount);
                prefix += "--";
                String shareUrl = getResources().getString(R.string.vc_domain);
                CustomShareDialog shareDialog = new CustomShareDialog((Activity) getContext(),prefix,shareUrl,CustomShareDialog.CHANNEL_AD_BLOCK_SHARE);
                shareDialog.show();
            }
        });
    }

    private void initView(Context context) {
        inflate(context, R.layout.adblock_setting_topview, this);
        setBackgroundResource(R.drawable.adblock_setting_top_bg);
        TextView topAdCountTv = (TextView) findViewById(R.id.top_ad_count_tv);
        TextView bottomAdCountTv = (TextView) findViewById(R.id.bottom_ad_count_tv);
        TextView reductTrafficTv = (TextView) findViewById(R.id.tv_reduce_traffic);
        TextView reduceTimeTv = (TextView) findViewById(R.id.tv_reduce_time);
        TextView trafficUnitTv = (TextView) findViewById(R.id.tv_reduce_traffic_unit);
        adBlockShareIv = findViewById(R.id.adblock_share_iv);

        topAdCountTv.setText(String.valueOf(ConfigManager.getInstance().getAdBlockedCount()));
        bottomAdCountTv.setText(String.valueOf(ConfigManager.getInstance().getAdBlockedCount()));

        adBlockCount =  ConfigManager.getInstance().getAdBlockedCount();
        long saveTraffic = ConfigManager.getInstance().getSaveTraffic();
        TrafficBean bean = trafficBean(saveTraffic);

        float saveTime = ConfigManager.getInstance().getSaveTime()/1000.0f;
        DecimalFormat df = new DecimalFormat("0.0");
        String formatAdCount = String.format(getResources().getString(R.string.ad_block_count_format),adBlockCount);
        String formatTraffic = String.format(getResources().getString(R.string.ad_block_save_traffic), df.format(bean.size));
        String formatTime = String.format(getResources().getString(R.string.ad_block_save_time), df.format(saveTime));
        trafficUnitTv.setText(bean.unit);
        reductTrafficTv.setText(Html.fromHtml(formatTraffic));
        reduceTimeTv.setText(Html.fromHtml(formatTime));
        bottomAdCountTv.setText(Html.fromHtml(formatAdCount));

    }




    private TrafficBean trafficBean(long fileSize_B) {
        TrafficBean bean = new TrafficBean();
        if (fileSize_B <= 0) {
            bean.size = 0;
            bean.unit = "B";
            return bean;
        }
        double G = fileSize_B * 1.0 / 1024 / 1024 / 1024;
        if (G > 1) {
            bean.size = G;
            bean.unit = "G";
            return bean;
        }
        double M = fileSize_B * 1.0 / 1024 / 1024;
        if (M > 1) {
            bean.size = M;
            bean.unit = "M";
            return bean;
        }
        double KB = fileSize_B * 1.0 / 1024;
        if (KB > 1) {
            bean.size = KB;
            bean.unit = "KB";
            return bean;
        }

        bean.size = fileSize_B;
        bean.unit = "B";
        return bean;
    }

    private class TrafficBean {
        double size;
        String unit;
    }

}