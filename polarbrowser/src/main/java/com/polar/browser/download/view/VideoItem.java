package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.bean.VideoInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.cache.ApkIconLoader;

/**
 * Created by saifei on 17/1/3.
 */

public class VideoItem extends RelativeLayout{
    private CommonCheckBox1 video_checkbox;
    private ImageView video_icon;
    private TextView video_title;
    private TextView video_size;
    private TextView video_download_time;
    private VideoInfo videoInfo;

    public ImageView getVideo_icon() {
        return video_icon;
    }

    public VideoInfo getVideoInfo() {
        return videoInfo;
    }

    public VideoItem(Context context) {
        this(context, null);
    }

    public VideoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_video_audio_list_item, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 8), 0, DensityUtil.dip2px(getContext(), 8));

        video_checkbox = (CommonCheckBox1) findViewById(R.id.video_audio_checkbox);
        video_icon = (ImageView) findViewById(R.id.video_audio_icon);
        video_title = (TextView) findViewById(R.id.video_audio_title);
        video_size = (TextView) findViewById(R.id.video_audio_size);
        video_download_time = (TextView) findViewById(R.id.video_audio_download_time);
        setListeners();

    }

    private void setListeners() {
        video_checkbox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
                videoInfo.isChecked = isChecked;
                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity) getContext()).checkCheckAllButton();
                }

            }
        });
    }

    public void bind(VideoInfo info,boolean isScrollState) {

        SimpleLog.d("VideoItem","info = "+info.toString()+" isScrollState = "+isScrollState);
        this.videoInfo = info;
        // 给 ImageView 设置一个 tag
        video_icon.setTag(info.getPath());
        // 预设一个图片
        video_icon.setImageResource(R.drawable.file_icon_video);
        // 通过 tag 来防止图片错位

        ApkIconLoader.getInstance().loadLocalVideoThumbnails(info.getId(), info.getPath(), video_icon, R.drawable.file_icon_video,isScrollState);

        video_title.setText(info.getName());
        video_size.setText(FileUtils.formatFileSize(info.getSize()));
        video_download_time.setText(DateUtils.formatDate(info.getDate()));
        if (info.isEditing()) {
            video_checkbox.setVisibility(View.VISIBLE);
            if (videoInfo.isChecked) {
                video_checkbox.setChecked(true);
            } else {
                video_checkbox.setChecked(false);
            }
        } else {
            video_checkbox.setVisibility(View.GONE);
        }

    }

}
