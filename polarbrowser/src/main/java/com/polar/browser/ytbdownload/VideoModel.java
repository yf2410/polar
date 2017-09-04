package com.polar.browser.ytbdownload;

import android.graphics.Bitmap;
import android.text.TextUtils;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.polar.browser.JuziApp;
import com.polar.browser.common.api.RequestAPI;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.vclibrary.bean.YouTubeVidVo;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.ImageLoadUtils;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by FKQ on 2017/6/28.
 */

public class VideoModel {

    public void getYtbVideoData(String url, final OnYtbVideoParserListener onYtbVideoParserListener) {
        if (ConfigManager.getInstance().getVideoIsCustomState()) {
            Api.getInstance().parserYouTubeVideoByServer(url).enqueue(new Callback<YouTubeVidVo>() {
                @Override
                public void onResponse(Call<YouTubeVidVo> call, Response<YouTubeVidVo> response) {
                    YouTubeVidVo body = response.body();
                    if (body != null) {
                        String bodyError = body.getError();
                        if (TextUtils.isEmpty(bodyError)) {
                            List<YouTubeVidVo.DownloadInfo> download_links = body.getDownload_links();
                            YouTubeVidVo.VideoInfo info = body.getInfo();
                            onYtbVideoParserListener.onParserSuccess(download_links, info);
                        } else {
                            onYtbVideoParserListener.onParserFail(bodyError);
                        }
                    } else {
                        onYtbVideoParserListener.onParserFail("body==null");
                    }

                }

                @Override
                public void onFailure(Call<YouTubeVidVo> call, Throwable t) {
                    if (onYtbVideoParserListener != null) {
                        onYtbVideoParserListener.onParserError(t);
                    }
                }
            });
        } else {
            String decoderUrl = RequestAPI.getDecoderUTF8(url);
            String md5Sign = SecurityUtil.getMD5(url + YtbVideoActivity.YOUTUBE_VC_FLAG);
            Api.getInstance().parserYouTubeVideo(decoderUrl,md5Sign).enqueue(new Callback<YouTubeVidVo>() {
                @Override
                public void onResponse(Call<YouTubeVidVo> call, Response<YouTubeVidVo> response) {
                    YouTubeVidVo body = response.body();
                    if (body != null) {
                        String bodyError = body.getError();
                        if (TextUtils.isEmpty(bodyError)) {
                            List<YouTubeVidVo.DownloadInfo> download_links = body.getDownload_links();
                            YouTubeVidVo.VideoInfo info = body.getInfo();
                            onYtbVideoParserListener.onParserSuccess(download_links, info);
                        } else {
                            onYtbVideoParserListener.onParserFail("parser error!");
                        }
                    } else {
                        onYtbVideoParserListener.onParserFail("parser error!");
                    }

                }

                @Override
                public void onFailure(Call<YouTubeVidVo> call, Throwable t) {
                    if (onYtbVideoParserListener != null) {
                        onYtbVideoParserListener.onParserError(t);
                    }
                }
            });
        }

    }

    public void loadVideoBanner(String url, final OnVideoBannerListener onVideoBannerListenner) {
        ImageLoadUtils.loadImageWithListener(JuziApp.getAppContext(), url, new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                onVideoBannerListenner.onImageLoadSuccess(resource);
            }
        });
    }
}
