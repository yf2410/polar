package com.polar.browser.homepage.sitelist.classify;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.homepage.sitelist.classify.details.ClassifyActivity;
import com.polar.browser.vclibrary.util.ImageLoadUtils;

public class ClassifyItem extends RelativeLayout implements android.view.View.OnClickListener {

    /**
     * 标题
     **/
    private TextView mTvTitle;
    /** 描述 **/
//	private TextView mTvDesc;
    /**
     * 网址icon
     **/
    private ImageView mImage;

    private ClassifyItemInfo classifyItemInfo;
    private int siteType;

    public ClassifyItem(Context context) {
        this(context, null);
    }

    public ClassifyItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.item_classify, this);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mImage = (ImageView) findViewById(R.id.iv_icon);
        setOnClickListener(this);
    }

    public void bind(@NonNull ClassifyItemInfo classifyItemInfo) {
        this.classifyItemInfo = classifyItemInfo;
        this.siteType = classifyItemInfo.getTypeId();
        String title = classifyItemInfo.getTitle();
        if (!TextUtils.isEmpty(title)) {
            this.mTvTitle.setText(title);
        }
//        Glide.with(getContext()).load(classifyItemInfo.getImgResource()).error(R.drawable.default_shortcut).placeholder(R.drawable.logo_click).into(mImage);
        ImageLoadUtils.loadImage(getContext(),classifyItemInfo.getImgResource(),mImage,R.drawable.logo_click,R.drawable.default_shortcut);
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getContext(), ClassifyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ClassifyActivity.SITE_TYPE, siteType);
        getContext().startActivity(intent);
    }
}

