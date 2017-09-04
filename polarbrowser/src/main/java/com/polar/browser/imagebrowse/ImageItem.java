package com.polar.browser.imagebrowse;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.polar.browser.R;

/**
 * Created by duan on 17/3/14.
 */

public class ImageItem extends RelativeLayout {

    private ImageInfo mData;

    private ImageView mImageView;

    public ImageItem(Context context) {
        this(context, null);
    }

    public ImageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.item_image_browse, this);
        mImageView = (ImageView) findViewById(R.id.iv_image);
    }

    public void bind(ImageInfo data) {
        mData = data;
        Glide.with(getContext()).load(mData.url).centerCrop().into(mImageView);
    }

}
