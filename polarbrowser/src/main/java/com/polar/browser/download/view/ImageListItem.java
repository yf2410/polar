package com.polar.browser.download.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.polar.browser.R;
import com.polar.browser.bean.ImageFolderInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox;
import com.polar.browser.download.FileClassifyDetailActivity;
import com.polar.browser.utils.DensityUtil;

import java.io.File;

/**
 * Created by saifei on 17/1/3.
 * 图片列表 item
 */

public class ImageListItem extends RelativeLayout{
    private ImageFolderInfo imageFolderInfo;
    private CommonCheckBox1 check_box;
    private TextView image_count;
    private TextView image_dir;
    private ImageView image_icon;

    public ImageListItem(Context context) {
        this(context, null);
    }

    public ImageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(),R.layout.view_image_list_item, this);
        setPadding(36, DensityUtil.dip2px(getContext(), 4), 0, DensityUtil.dip2px(getContext(), 4));
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 69));
        setLayoutParams(params);

        this.check_box = (CommonCheckBox1)findViewById(R.id.check_box);
        this.image_icon = (ImageView) findViewById(R.id.image_icon);
        this.image_dir = (TextView) findViewById(R.id.image_dir);
        this.image_count = (TextView) findViewById(R.id.image_count);
        setListeners();
    }

    private void setListeners() {
        check_box.setOnCheckedChangedListener(new ICustomCheckBox.OnCheckChangedListener() {
            @Override
            public void onCheckChanged(View v, boolean isChecked) {
                imageFolderInfo.isChecked = isChecked;
                if (getContext() instanceof FileClassifyDetailActivity) {
                    ((FileClassifyDetailActivity)getContext()).checkCheckAllButton();
                }

            }
        });
    }

    public void bind(ImageFolderInfo imageFolderInfo) {
        this.imageFolderInfo = imageFolderInfo;
        Glide.with(getContext()).load(new File(imageFolderInfo.getImagePath())).centerCrop().into(image_icon);
        image_dir.setText(imageFolderInfo.getName());
        image_count.setText(getResources().getString(R.string.file_image_count, imageFolderInfo.getImageCount()));

        if (imageFolderInfo.isEditing()) {
            check_box.setVisibility(View.VISIBLE);
            if (imageFolderInfo.isChecked) {
                check_box.setChecked(true);
            } else {
                check_box.setChecked(false);
            }
        } else {
            check_box.setVisibility(View.GONE);
        }
    }
}
