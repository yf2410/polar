package com.polar.browser.ytbdownload;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download.DownloadHelper;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.vclibrary.bean.YouTubeVidVo;

/**
 * Created by FKQ on 2017/6/28.
 */

public class YtbVideoItem extends LinearLayout implements View.OnClickListener{

    private TextView mTvQuality;
    private TextView mTvFormat;
    private TextView mTvSize;
    private TextView mTvDownload;

    private YouTubeVidVo.DownloadInfo mDownloadInfo;
    private YouTubeVidVo.VideoInfo mVideoInfo;

    private OnVideoDialogDismissListener mOnVideoDialogDismissListener;


    public YtbVideoItem(Context context) {
        this(context,null);
    }

    public YtbVideoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.ytb_video_item, this);
        mTvQuality = (TextView) findViewById(R.id.video_quality);
        mTvFormat = (TextView) findViewById(R.id.video_format);
        mTvSize = (TextView) findViewById(R.id.video_size);
        mTvDownload = (TextView) findViewById(R.id.video_download_click);
        mTvDownload.setOnClickListener(this);
    }

    public void bind(YouTubeVidVo.DownloadInfo downloadInfo, YouTubeVidVo.VideoInfo info, OnVideoDialogDismissListener onVideoDialogDismissListener) {
        this.mDownloadInfo = downloadInfo;
        this.mVideoInfo = info;
        mOnVideoDialogDismissListener = onVideoDialogDismissListener;
        if (mDownloadInfo == null) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        String quality = downloadInfo.getQuality();
        if (!TextUtils.isEmpty(quality)) {
            mTvQuality.setText(quality);
        }
        String type = downloadInfo.getType();
        if (!TextUtils.isEmpty(type)) {
            mTvFormat.setText(type);
        }
        String size = downloadInfo.getSize();
        if (TextUtils.isEmpty(size)) {
            mTvSize.setText("UnKnown");
        } else {
            mTvSize.setText(FileUtils.formatFileSize(Long.parseLong(size)));
        }
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(mDownloadInfo.getUrl())) return;
        Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_DWN_FORMAT_BTN, mDownloadInfo.getQuality());
        if (mOnVideoDialogDismissListener != null) {
            mOnVideoDialogDismissListener.onDismissVideoDialog();
        }
        DownloadHelper.download(mVideoInfo.getUrl(), mDownloadInfo.getUrl(), null,
                "youtube.com", "attachment;filename=\"" + mDownloadInfo.getTitle()+"."+mDownloadInfo.getType().toLowerCase()
                        + "\"", "video/" + mDownloadInfo.getType(), Long.parseLong(TextUtils.isEmpty(mDownloadInfo.getSize())?"0":mDownloadInfo.getSize()));
    }
}
