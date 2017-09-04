package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.bean.DocInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;

/**
 * Created by saifei on 17/1/3.
 */

public class DocumentFileItem extends RelativeLayout{
    private CommonCheckBox1 doc_checkbox;
    private ImageView doc_icon;
    private TextView doc_title;
    private TextView doc_size;
    private TextView doc_download_time;
    private DocInfo docInfo;

    public DocumentFileItem(Context context) {
        this(context, null);
    }

    public DocumentFileItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_video_audio_list_item, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 8), 0, DensityUtil.dip2px(getContext(), 8));

        doc_checkbox = (CommonCheckBox1) findViewById(R.id.video_audio_checkbox);
        doc_icon = (ImageView) findViewById(R.id.video_audio_icon);
        int iconWidthHeight = (int) getResources().getDimension(R.dimen.file_list_item_other_iv_width);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iconWidthHeight, iconWidthHeight);
        /*int marginLeft = (int) getResources().getDimension(R.dimen.file_list_item_cb_marginLeft);
        layoutParams.setMargins(marginLeft, 0, 0, 0);*/
        layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.video_audio_checkbox);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        doc_icon.setLayoutParams(layoutParams);

        doc_title = (TextView) findViewById(R.id.video_audio_title);
        doc_size = (TextView) findViewById(R.id.video_audio_size);
        doc_download_time = (TextView) findViewById(R.id.video_audio_download_time);
        setListeners();

    }

    private void setListeners() {
        doc_checkbox.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
                docInfo.isChecked = isChecked;
                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity)getContext()).checkCheckAllButton();
                }

            }
        });
    }

    public void bind(DocInfo info) {
        this.docInfo = info;


        doc_icon.setImageResource(R.drawable.file_icon_doc);
        doc_title.setText(info.getName());
        doc_size.setText(FileUtils.formatFileSize(info.getSize()));
        doc_download_time.setText(DateUtils.formatFileDate(info.getDate()*1000));
        if (info.isEditing()) {
            doc_checkbox.setVisibility(View.VISIBLE);
            if (docInfo.isChecked) {
                doc_checkbox.setChecked(true);
            } else {
                doc_checkbox.setChecked(false);
            }
        } else {
            doc_checkbox.setVisibility(View.GONE);
        }

    }
}
