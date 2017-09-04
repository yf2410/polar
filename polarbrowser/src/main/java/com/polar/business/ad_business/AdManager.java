package com.polar.business.ad_business;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.NativeAd;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.AdSwitchBean;
import com.polar.browser.vclibrary.bean.ApplicationTagBean;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.bean.events.GoBrowserActivityEvent;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.network.ResultCallback;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.ImageLoadUtils;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by FKQ on 2017/2/16.
 */

public final class AdManager {

    private static final String TAG = "AdManager";

    private static final String AD_VERSION = "1";

    /** Facebook */
    private static final String FB_WELCOME_PLACEMENT_ID = "1223010714479910_1225699767544338";
    private static final String FB_HOME_PLACEMENT_ID = "1223010714479910_1225699994210982";
    private static final long AD_HOME_SHOW_TIME = 15000L;
    private static final int WELCOME_AD_SHOW_TIME = 5;//单位秒

    private static AdManager mInstance;
    private NativeAd mNativeAd;
    private ApplicationTagBean mApplicationTagBean;

    private AdManager(){
        if(AppEnv.DEBUG) {
            //6P
            AdSettings.addTestDevice("8a20c15ad480d313fe031f7f6a84c31c");
        }
        mApplicationTagBean = new ApplicationTagBean();
    }
    public static AdManager getInstance() {
        if (mInstance == null) {
            synchronized (AdManager.class) {
                if (mInstance == null) {
                    mInstance = new AdManager();
                }
            }
        }
        return mInstance;
    }

    public ApplicationTagBean getApplicationTagBean() {
        return mApplicationTagBean;
    }

    /**
     * facebook-启动广告
     * @param mNativeAdContainer
     */
    public void loadWelcomeAd(final RelativeLayout mNativeAdContainer) {
        final ImageView nativeAdIcon = (ImageView) mNativeAdContainer.findViewById(R.id.fb_welcome_icon);
        final TextView nativeAdTitle = (TextView) mNativeAdContainer.findViewById(R.id.fb_welcome_title);
        final TextView nativeAdBody = (TextView) mNativeAdContainer.findViewById(R.id.fb_welcome_dec);
        final ImageView nativeAdPic = (ImageView) mNativeAdContainer.findViewById(R.id.fb_welcome_pic);
        final TextView nativeAdAction = (TextView) mNativeAdContainer.findViewById(R.id.fb_welcome_action);
        final LinearLayout adChoicesView_ll = (LinearLayout) mNativeAdContainer.findViewById(R.id.adChoicesView_ll);
        final LinearLayout nativeAdCallToAction = (LinearLayout) mNativeAdContainer.findViewById(R.id.fb_welcome_ad_call_to_action);
        ThreadManager.postDelayedTaskToIOHandler(new Runnable() {
            @Override
            public void run() {
                if(mNativeAdContainer.getVisibility()== View.GONE){
                    RxBus.get().post(new GoBrowserActivityEvent(Constants.GOBROWSERTYPE_AD,0));
                }
            }
        },2000);
        mNativeAd = new NativeAd(JuziApp.getAppContext(), FB_WELCOME_PLACEMENT_ID);
        mNativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                RxBus.get().post(new GoBrowserActivityEvent(Constants.GOBROWSERTYPE_AD,0));
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (mNativeAd != ad) {
                    // Race condition, load() called again before last ad was displayed
                    return;
                }
                mNativeAdContainer.setVisibility(View.VISIBLE);
                // Unregister last ad
                mNativeAd.unregisterView();

                AdChoicesView adChoicesView = new AdChoicesView(JuziApp.getAppContext(), mNativeAd, true);
                adChoicesView_ll.addView(adChoicesView);
                // Setting the Text
                nativeAdAction.setText(mNativeAd.getAdCallToAction());
                nativeAdTitle.setText(mNativeAd.getAdTitle());
                nativeAdBody.setText(mNativeAd.getAdBody());

//                NativeAd.Image adIcon = fbNativeAd.getAdIcon();
//                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);
                ImageLoadUtils.loadRoundImage(JuziApp.getAppContext(), mNativeAd.getAdIcon().getUrl(), nativeAdIcon);

                // Downloading and setting the cover image.
                NativeAd.Image adCoverImage = mNativeAd.getAdCoverImage();
                NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdPic);

                mNativeAd.registerViewForInteraction(nativeAdCallToAction);
                RxBus.get().post(new GoBrowserActivityEvent(Constants.GOBROWSERTYPE_AD,WELCOME_AD_SHOW_TIME));
            }

            @Override
            public void onAdClicked(Ad ad) {

            }
        });

        mNativeAd.loadAd();
    }

    public void loadHomePage(final LinearLayout mNativeAdContainer) {
        final LinearLayout adChoicesView_ll = (LinearLayout) mNativeAdContainer.findViewById(R.id.adChoicesView_ll);
        final ImageView nativeAdIcon = (ImageView) mNativeAdContainer.findViewById(R.id.native_ad_icon);
        final TextView nativeAdTitle = (TextView) mNativeAdContainer.findViewById(R.id.native_ad_title);
        final TextView nativeAdBody = (TextView) mNativeAdContainer.findViewById(R.id.native_ad_body);
        final ImageView nativeAdPic = (ImageView) mNativeAdContainer.findViewById(R.id.ad_pic);
        final Button nativeAdCallToAction = (Button) mNativeAdContainer.findViewById(R.id.native_ad_call_to_action);

        mNativeAd = new NativeAd(JuziApp.getAppContext(), FB_HOME_PLACEMENT_ID);
        mNativeAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                SimpleLog.i(TAG, "FB_onError_Error: " + adError.getErrorMessage());
                SimpleLog.i(TAG, "FB_onError_Error: " + adError.getErrorCode());

            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (mNativeAd != ad) {
                    // Race condition, load() called again before last ad was displayed
                    return;
                }
                // Unregister last ad
                mNativeAd.unregisterView();
                mNativeAdContainer.setVisibility(View.VISIBLE);
                ThreadManager.postDelayedTaskToLogicHandler(new Runnable() {
                    @Override
                    public void run() {
                        ThreadManager.postTaskToUIHandler(new Runnable() {
                            @Override
                            public void run() {
                                mNativeAdContainer.setVisibility(View.GONE);
                            }
                        });
                    }
                },AD_HOME_SHOW_TIME);

                AdChoicesView adChoicesView = new AdChoicesView(JuziApp.getAppContext(), mNativeAd, true);
                adChoicesView_ll.addView(adChoicesView);

                nativeAdCallToAction.setText(mNativeAd.getAdCallToAction());
                nativeAdTitle.setText(mNativeAd.getAdTitle());
                nativeAdBody.setText(mNativeAd.getAdBody());

                NativeAd.Image adIcon = mNativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

                // Downloading and setting the cover image.
                NativeAd.Image adCoverImage = mNativeAd.getAdCoverImage();
                NativeAd.downloadAndDisplayImage(adCoverImage, nativeAdPic);

                // Wire up the View with the native ad, the whole nativeAdContainer will be clickable.
                mNativeAd.registerViewForInteraction(mNativeAdContainer);

            }

            @Override
            public void onAdClicked(Ad ad) {
                mNativeAdContainer.setVisibility(View.GONE);
            }
        });
        mNativeAd.loadAd();
    }

    public void initServiceAdSwitch() {
        Api.getInstance().getAdSwitch(AD_VERSION).enqueue(new ResultCallback<AdSwitchBean>() {
            @Override
            public void success(AdSwitchBean data, Call<Result<AdSwitchBean>> call, Response<Result<AdSwitchBean>> response) throws Exception {
                SimpleLog.d(TAG, "initServiceAdSwitch-success");
                String adVersion = data.getAdVersion();
                if (TextUtils.isEmpty(adVersion)) {
                    return;
                }
                switch (adVersion) {
                    case AD_VERSION:
                        ConfigManager.getInstance().saveAdSwitchBySP(data);
                        break;
                }
            }

            @Override
            public void error(Call<Result<AdSwitchBean>> call, Throwable t) {

            }
        });
    }

    public void destroy() {
        if(mNativeAd != null){
            mNativeAd.unregisterView();
            mNativeAd.destroy();
            mNativeAd = null;
        }
    }

}
