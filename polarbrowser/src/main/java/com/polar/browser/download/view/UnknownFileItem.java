package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.bean.UnknownInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;

/**
 * Created by saifei on 17/1/3.
 */

public class UnknownFileItem extends RelativeLayout{
    private CommonCheckBox1 unknown_checkbox;
    private ImageView unknown_icon;
    private TextView unknown_title;
    private TextView unknown_size;
    private TextView unknown_download_time;
    private UnknownInfo unknownInfo;

    public UnknownFileItem(Context context) {
        this(context, null);
    }

    public UnknownFileItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_video_audio_list_item, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 8), 0, DensityUtil.dip2px(getContext(), 8));

        unknown_checkbox = (CommonCheckBox1) findViewById(R.id.video_audio_checkbox);
        unknown_icon = (ImageView) findViewById(R.id.video_audio_icon);
        unknown_title = (TextView) findViewById(R.id.video_audio_title);
        unknown_size = (TextView) findViewById(R.id.video_audio_size);
        unknown_download_time = (TextView) findViewById(R.id.video_audio_download_time);
        setListeners();

    }

    private void setListeners() {
        unknown_checkbox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
                unknownInfo.isChecked = isChecked;
                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity)getContext()).checkCheckAllButton();
                }

            }
        });
    }

    public void bind(UnknownInfo info) {
        this.unknownInfo = info;


        unknown_icon.setImageResource(R.drawable.file_icon_other);
        unknown_title.setText(info.getName());
        unknown_size.setText(FileUtils.formatFileSize(info.getSize()));
        unknown_download_time.setText(DateUtils.formatFileDate(info.getDate()*1000));
        if (info.isEditing()) {
            unknown_checkbox.setVisibility(View.VISIBLE);
            if (unknownInfo.isChecked) {
                unknown_checkbox.setChecked(true);
            } else {
                unknown_checkbox.setChecked(false);
            }
        } else {
            unknown_checkbox.setVisibility(View.GONE);
        }

    }
}
