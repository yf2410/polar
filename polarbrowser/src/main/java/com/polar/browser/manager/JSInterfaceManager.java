package com.polar.browser.manager;

import android.app.Activity;
import android.content.Context;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.download.DownloadHelper;
import com.polar.browser.env.AppEnv;

import com.polar.browser.imagebrowse.ImageListData;
import com.polar.browser.library.utils.SystemUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.events.FbNotifyMsgEvent;
import com.polar.browser.vclibrary.bean.events.IntoImageBrowseEvent;
import com.polar.browser.video.share.CustomShareDialog;
import com.polar.browser.video.share.VideoShareFSDialog;

import org.greenrobot.eventbus.EventBus;

/**
 * java和js交互类
 * Created by yxx on 2017/3/8.
 */

public class JSInterfaceManager {

    private Context mContext;
    private boolean load;

    public JSInterfaceManager(Context context) {
        this.mContext = context;
    }

    /**
     * facebook 网站视频分享
     * @param shareUrl 视频url*/
    @JavascriptInterface
    public void shareVideo(String shareUrl) {
        if (mContext != null && mContext instanceof Activity) {
            CustomShareDialog customShareDialog = new CustomShareDialog((Activity) mContext, shareUrl, CustomShareDialog.CHANNEL_VIDEO_SHARE);
            customShareDialog.init();
            customShareDialog.show();
        }
    }
    /**
     * facebook 网站视频全屏分享
     * @param shareUrl 视频url*/
    @JavascriptInterface
    public void fullscreenShareVideo(String shareUrl) {
        VideoShareFSDialog customShareDialog = new VideoShareFSDialog((Activity) mContext, shareUrl);
        customShareDialog.show();
    }

    /**
     * 进入看图模式
     * @param imgs
     */
    @JavascriptInterface
    public void intoImgBrowseMode(String imgs, boolean isLoadMore) {
        ImageListData data = null;
        try {
            data = new Gson().fromJson(imgs, ImageListData.class);
        } catch (Exception e) {
        }
        // 添加过滤，大于3张图片的图集才显示
        if (data != null && data.imgs != null && data.imgs.size() >= 3) {
            EventBus.getDefault().post(new IntoImageBrowseEvent(imgs, isLoadMore));
        } else {
            CustomToastUtils.getInstance().showTextToast(R.string.unusable_full_figure_mode);
        }
    }

    @JavascriptInterface
    public void albumAvailable(String availavle) {
        if (availavle != null) {
            ConfigManager.getInstance().setAlbumAvailable(Boolean.valueOf(availavle));
        }
    }

    @JavascriptInterface
    public void receiveFbNoti(String type, String url, String count) {
        if (AppEnv.DEBUG) {
            SimpleLog.d("--MyLog--", "fbNotify--type=" + type+",count =" +count+",url="+url);
        }
        EventBus.getDefault().post(new FbNotifyMsgEvent(type, url, count));
    }

    /**
     * facebook 网站视频下载
     * @param url 视频url
     * @param mimeType 视频mime type
     * */
    @JavascriptInterface
    public void downloadVideo(String url, String mimeType) {
        DownloadHelper.download(url, url, null, null, null, mimeType, 0);
    }
    /**
     * facebook 视频js中获取apk版本号
     * */
    @JavascriptInterface
    public int getAppVersionCode() {
        return SystemUtils.getVersionCode(JuziApp.getInstance());
    }


}
