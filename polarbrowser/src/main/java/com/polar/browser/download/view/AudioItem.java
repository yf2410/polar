package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.bean.MusicInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;

/**
 * Created by saifei on 17/1/5.
 */

public class AudioItem extends RelativeLayout {
    public AudioItem(Context context) {
        this(context, null);
    }

    public AudioItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private CommonCheckBox1 audio_checkbox;
    private ImageView audio_icon;
    private TextView audio_title;
    private TextView audio_size;
    private TextView audio_download_time;
    private MusicInfo audioInfo;


    private void init() {
        inflate(getContext(), R.layout.view_video_audio_list_item, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 8), 0, DensityUtil.dip2px(getContext(), 8));

        audio_checkbox = (CommonCheckBox1) findViewById(R.id.video_audio_checkbox);
        audio_icon = (ImageView) findViewById(R.id.video_audio_icon);
        int iconWidthHeight = (int) getResources().getDimension(R.dimen.file_list_item_other_iv_width);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iconWidthHeight, iconWidthHeight);
        layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.video_audio_checkbox);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        audio_icon.setLayoutParams(layoutParams);
        audio_title = (TextView) findViewById(R.id.video_audio_title);
        audio_size = (TextView) findViewById(R.id.video_audio_size);
        audio_download_time = (TextView) findViewById(R.id.video_audio_download_time);
        setListeners();

    }

    private void setListeners() {
        audio_checkbox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
                audioInfo.isChecked = isChecked;
                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity)getContext()).checkCheckAllButton();
                }

            }
        });
    }

    public void bind(MusicInfo info) {
        this.audioInfo = info;


        audio_icon.setImageResource(R.drawable.file_icon_music);
        audio_title.setText(info.getName());
        audio_size.setText(FileUtils.formatFileSize(info.getSize()));
        audio_download_time.setText(DateUtils.formatFileDate(info.getDate()*1000));
        if (info.isEditing()) {
            audio_checkbox.setVisibility(View.VISIBLE);
            if (audioInfo.isChecked) {
                audio_checkbox.setChecked(true);
            } else {
                audio_checkbox.setChecked(false);
            }
        } else {
            audio_checkbox.setVisibility(View.GONE);
        }

    }
}
