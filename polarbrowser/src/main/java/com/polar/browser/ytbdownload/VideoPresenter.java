package com.polar.browser.ytbdownload;

import android.graphics.Bitmap;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.bean.YouTubeVidVo;

import java.util.List;

/**
 * Created by FKQ on 2017/6/28.
 */

public class VideoPresenter {

    private IVideoView mIVideoView;
    private VideoModel mVideoModel;
    private Bitmap mBitmap = null;

    public VideoPresenter(IVideoView videoView) {
        this.mIVideoView = videoView;
        mVideoModel = new VideoModel();
    }

    public void initVideoData(String bannerUrl, String url) {
        mIVideoView.hindVideoBanner();
        mIVideoView.hindVideoDownload();
        mIVideoView.showParsingView(YtbVideoActivity.YOUTUBE_PARSERING, JuziApp.getAppContext().getResources().getString(R.string.dialog_video_download_parsing));//正在解析

        mVideoModel.loadVideoBanner(bannerUrl, new OnVideoBannerListener() {
            @Override
            public void onImageLoadSuccess(Bitmap resource) {
                mBitmap = resource;
            }
        });

        mVideoModel.getYtbVideoData(url, new OnYtbVideoParserListener() {
            @Override
            public void onParserSuccess(List<YouTubeVidVo.DownloadInfo> download_links, YouTubeVidVo.VideoInfo info) {
                if (mIVideoView == null) return;
                mIVideoView.hindParsingView();
                if (mBitmap != null) {
                    mIVideoView.showVideoBanner(mBitmap);
                } else {
                    mIVideoView.hindVideoBanner();
                }
                mIVideoView.showVideoDownload();
                mIVideoView.bindVideoData(download_links, info);
            }

            @Override
            public void onParserFail(String message) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_PARSER_ERROR_CODE, message);
                if (mIVideoView == null) return;
                mIVideoView.hindVideoBanner();
                mIVideoView.hindVideoDownload();
                mIVideoView.showParsingView(YtbVideoActivity.YOUTUBE_PARSER_FAIL,JuziApp.getAppContext().getResources().getString(R.string.dialog_video_download_parseerror));//解析错误
            }

            @Override
            public void onParserError(Throwable t) {
                Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_PARSER_ERROR_CODE, t.getMessage());
                if (mIVideoView == null) return;
                mIVideoView.hindVideoBanner();
                mIVideoView.hindVideoDownload();
                mIVideoView.showParsingView(YtbVideoActivity.YOUTUBE_PARSER_FAIL,JuziApp.getAppContext().getResources().getString(R.string.dialog_video_download_parseerror));//解析错误
            }
        });
    }

    public void onPresenterDestroy() {
        if (mVideoModel != null) {
            mVideoModel = null;
        }
        if (mIVideoView != null) {
            mIVideoView = null;
        }
    }
}
