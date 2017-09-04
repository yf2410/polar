package com.polar.browser.common.ui;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.UIUtils;


public class DownloadTitleBar extends LinearLayout {

	private ImageView mImgBack;
	private TextView mTvTitle;
	private String mTitleText;
	private ImageButton ivSetting;
	private ImageButton ivOpenGodownload;

	public DownloadTitleBar(Context context) {
		super(context);
		init();
	}

	public DownloadTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTitleText = UIUtils.getTextFromAttrs(context, attrs);
		init();
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);
		setFitsSystemWindows(true);
		final Context context = getContext();
		inflate(context, R.layout.activity_download_title_bar, this);
		mImgBack = (ImageView) findViewById(R.id.common_img_back);
		mTvTitle = (TextView) findViewById(R.id.common_tv_title);
		ivOpenGodownload = (ImageButton) findViewById(R.id.imageview_open_goDownloader);
		ivSetting = (ImageButton) findViewById(R.id.imageview_setting);

		if (!TextUtils.isEmpty(mTitleText)) {
			setTitle(mTitleText);
		}

		initListeners();
	}

	private void initListeners() {
		if (getContext() instanceof Activity) {
			mImgBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					((Activity) getContext()).finish();
				}
			});
		}
	}



	public void setTitle(CharSequence title) {
		if (mTvTitle != null) {
			mTvTitle.setText(title);
		}
	}

	public void setTitle(int title) {
		mTvTitle.setText(title);
	}

	public void setClickListener(View.OnClickListener listener) {
		ivSetting.setOnClickListener(listener);
		ivOpenGodownload.setOnClickListener(listener);
	}

//	public void setEditBtnEnabled(boolean isEnable) {
//		ivSetting.setEnabled(isEnable);
////		ivSetting.setImageResource(isEnable?R.drawable.icon_setting:R.drawable.icon_setting_disabled);
//	}

	public void setDownloadImgVisibile(boolean visible) {
		ivOpenGodownload.setVisibility(visible?VISIBLE:GONE);
	}
}
