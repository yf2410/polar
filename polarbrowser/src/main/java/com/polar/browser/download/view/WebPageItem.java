package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.download.savedpage.SavedPageNode;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;

/**
 * Created by saifei on 17/1/3.
 */

public class WebPageItem extends RelativeLayout{
    private CommonCheckBox1 webPage_checkbox;
    private ImageView webPage_icon;
    private TextView webPage_title;
    private TextView webPage_size;
    private TextView webPage_download_time;
    private SavedPageNode webPageInfo;

    public WebPageItem(Context context) {
        this(context, null);
    }

    public WebPageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_video_audio_list_item, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 8), 0, DensityUtil.dip2px(getContext(), 8));

        webPage_checkbox = (CommonCheckBox1) findViewById(R.id.video_audio_checkbox);
        webPage_icon = (ImageView) findViewById(R.id.video_audio_icon);
        webPage_title = (TextView) findViewById(R.id.video_audio_title);
        webPage_size = (TextView) findViewById(R.id.video_audio_size);
        webPage_download_time = (TextView) findViewById(R.id.video_audio_download_time);
        setListeners();

    }

    private void setListeners() {
        webPage_checkbox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
                webPageInfo.isChecked = isChecked;
                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity)getContext()).checkCheckAllButton();
                }

            }
        });
    }

    public void bind(SavedPageNode info) {
        this.webPageInfo = info;


        webPage_icon.setImageResource(R.drawable.offline_files);
        webPage_title.setText(info.fileName);
        webPage_size.setText(FileUtils.formatFileSize(info.fileSize));
        webPage_download_time.setText(DateUtils.formatFileDate(info.getDate()));
        if (info.isEditing()) {
            webPage_checkbox.setVisibility(View.VISIBLE);
            if (webPageInfo.isChecked) {
                webPage_checkbox.setChecked(true);
            } else {
                webPage_checkbox.setChecked(false);
            }
        } else {
            webPage_checkbox.setVisibility(View.GONE);
        }

    }
}
