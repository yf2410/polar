package com.polar.browser.video;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;

public class VideoRateView extends RelativeLayout {

	private TextView mTvRate;

	private ImageView mIvIcon;
	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			setVisibility(View.GONE);
		}
	};

	public VideoRateView(Context context) {
		this(context, null);
	}

	public VideoRateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_video_rate, this);
		initView();
	}

	private void initView() {
		mTvRate = (TextView) findViewById(R.id.tv_rate);
		mIvIcon = (ImageView) findViewById(R.id.iv_rate);
	}

	public void setProgreess(int progress) {
		if (progress >= 0 && progress <= 100) {
			removeCallbacks(mHideRunnable);
			setVisibility(View.VISIBLE);
			mTvRate.setText(progress + "%");
			postDelayed(mHideRunnable, 1000);
		}
	}

	public void setProgreess(boolean isBack, String brightness) {
		if (TextUtils.isEmpty(brightness)) {
			return;
		}
		if (isBack) {
			mIvIcon.setImageResource(R.drawable.video_back);
		} else {
			mIvIcon.setImageResource(R.drawable.video_forward);
		}
		removeCallbacks(mHideRunnable);
		setVisibility(View.VISIBLE);
		mTvRate.setText(brightness);
		postDelayed(mHideRunnable, 1000);
	}
}
