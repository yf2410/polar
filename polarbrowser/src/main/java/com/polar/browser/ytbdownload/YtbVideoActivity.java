package com.polar.browser.ytbdownload;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.bean.YouTubeVidVo;
import com.polar.browser.vclibrary.network.api.ApiConstants;
import java.util.List;

/**
 * Created by FKQ on 2017/6/28.
 */

public class YtbVideoActivity extends Activity implements IVideoView, View.OnClickListener {

    public static final String YOUTUBE_URL = "youtube_url";
    public static final String YOUTUBE_VC_FLAG = "_vcBrowser";

    public static final int YOUTUBE_PARSERING = 1;
    public static final int YOUTUBE_PARSER_FAIL = 2;

    private ImageView mIvVideoAdBanner;
    private LinearLayout mVideoDownloadTitle;
    private YtbVideoItem mYtbVideoItem1;
    private YtbVideoItem mYtbVideoItem2;
    private YtbVideoItem mYtbVideoItem3;
    private YtbVideoItem mYtbVideoItem4;
    private YtbVideoItem mYtbVideoItem5;
    private RelativeLayout mVideoParsingArea;
    private TextView mTvVideoParse;
    private ScrollView mScrollViewVideo;

    private VideoPresenter mVideoPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.download_video_activity);
        initView();
        initData();
    }

    private void initView() {

        mIvVideoAdBanner = (ImageView) findViewById(R.id.video_ad_banner);
        mVideoDownloadTitle = (LinearLayout) findViewById(R.id.video_download_ll);
        mScrollViewVideo = (ScrollView) findViewById(R.id.video_content_scrollview);

        mVideoParsingArea = (RelativeLayout) findViewById(R.id.rl_parsing_area);
        mTvVideoParse = (TextView) findViewById(R.id.tv_parsing);

        mYtbVideoItem1 = (YtbVideoItem) findViewById(R.id.video1);
        mYtbVideoItem2 = (YtbVideoItem) findViewById(R.id.video2);
        mYtbVideoItem3 = (YtbVideoItem) findViewById(R.id.video3);
        mYtbVideoItem4 = (YtbVideoItem) findViewById(R.id.video4);
        mYtbVideoItem5 = (YtbVideoItem) findViewById(R.id.video5);

        mIvVideoAdBanner.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String stringExtra = intent.getStringExtra(YOUTUBE_URL);
            if (!TextUtils.isEmpty(stringExtra)) {
                mVideoPresenter = new VideoPresenter(this);
                mVideoPresenter.initVideoData(ApiConstants.YTB_VIDEO_BANNER, stringExtra);
            }
        }
    }

    @Override
    public void showParsingView(int type, String parsMessage) {
        mVideoParsingArea.setVisibility(View.VISIBLE);
        mTvVideoParse.setText(parsMessage);
        if (YOUTUBE_PARSERING==type) {
            Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_DIALOG_PARSING);
        } else if (YOUTUBE_PARSER_FAIL==type) {
            Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_PARSER_FAIL);
        }
    }

    @Override
    public void hindParsingView() {
        mVideoParsingArea.setVisibility(View.GONE);
    }

    @Override
    public void showVideoBanner(Bitmap bitmap) {
        mIvVideoAdBanner.setVisibility(View.VISIBLE);
        mIvVideoAdBanner.setImageBitmap(bitmap);
    }

    @Override
    public void hindVideoBanner() {
        mIvVideoAdBanner.setVisibility(View.GONE);
    }

    @Override
    public void showVideoDownload() {
        mVideoDownloadTitle.setVisibility(View.VISIBLE);
        mScrollViewVideo.setVisibility(View.VISIBLE);
        Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_PARSER_OK);
    }

    @Override
    public void hindVideoDownload() {
        mVideoDownloadTitle.setVisibility(View.GONE);
        mScrollViewVideo.setVisibility(View.GONE);
    }

    @Override
    public void bindVideoData(List<YouTubeVidVo.DownloadInfo> download_links, YouTubeVidVo.VideoInfo info) {
        if (!ListUtils.isEmpty(download_links)) {
            if (1==download_links.size()) {
                YouTubeVidVo.DownloadInfo downloadInfo1 = download_links.get(0);
                mYtbVideoItem1.bind(downloadInfo1,info,mOnVideoDialogDismissListener);
            } else if (2==download_links.size()) {
                YouTubeVidVo.DownloadInfo downloadInfo1 = download_links.get(0);
                mYtbVideoItem1.bind(downloadInfo1,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo2 = download_links.get(1);
                mYtbVideoItem2.bind(downloadInfo2,info,mOnVideoDialogDismissListener);
            } else if (3==download_links.size()) {
                YouTubeVidVo.DownloadInfo downloadInfo1 = download_links.get(0);
                mYtbVideoItem1.bind(downloadInfo1,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo2 = download_links.get(1);
                mYtbVideoItem2.bind(downloadInfo2,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo3 = download_links.get(2);
                mYtbVideoItem3.bind(downloadInfo3,info,mOnVideoDialogDismissListener);
            } else if (4==download_links.size()) {
                YouTubeVidVo.DownloadInfo downloadInfo1 = download_links.get(0);
                mYtbVideoItem1.bind(downloadInfo1,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo2 = download_links.get(1);
                mYtbVideoItem2.bind(downloadInfo2,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo3 = download_links.get(2);
                mYtbVideoItem3.bind(downloadInfo3,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo4 = download_links.get(3);
                mYtbVideoItem4.bind(downloadInfo4,info,mOnVideoDialogDismissListener);
            } else if (5<=download_links.size()) {
                YouTubeVidVo.DownloadInfo downloadInfo1 = download_links.get(0);
                mYtbVideoItem1.bind(downloadInfo1,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo2 = download_links.get(1);
                mYtbVideoItem2.bind(downloadInfo2,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo3 = download_links.get(2);
                mYtbVideoItem3.bind(downloadInfo3,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo4 = download_links.get(3);
                mYtbVideoItem4.bind(downloadInfo4,info,mOnVideoDialogDismissListener);
                YouTubeVidVo.DownloadInfo downloadInfo5 = download_links.get(4);
                mYtbVideoItem5.bind(downloadInfo5,info,mOnVideoDialogDismissListener);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_ad_banner:
                Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_BANNER_CLICK);
                Intent intent = new Intent(this, BrowserActivity.class);
                intent.setAction(CommonData.ACTION_OPEN_PRODUCT_ABOUT);
                intent.putExtra(CommonData.SYSTEM_CONTENT_URL, ApiConstants.YTB_VIDEO_BANNER_ACTION);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(0, android.R.anim.fade_out);
                break;
            default:
                break;
        }
    }

    public void onFinshActivity() {
        finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    private OnVideoDialogDismissListener mOnVideoDialogDismissListener = new OnVideoDialogDismissListener() {
        @Override
        public void onDismissVideoDialog() {
            onFinshActivity();
        }
    };

    @Override
    protected void onDestroy() {
        if (mVideoPresenter != null) {
            mVideoPresenter.onPresenterDestroy();
            mVideoPresenter = null;
        }
        if (mOnVideoDialogDismissListener != null) {
            mOnVideoDialogDismissListener = null;
        }
        super.onDestroy();
    }

}
