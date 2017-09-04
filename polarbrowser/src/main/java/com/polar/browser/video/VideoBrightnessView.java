package com.polar.browser.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;

public class VideoBrightnessView extends RelativeLayout {

	private TextView mTvBrightness;
	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			setVisibility(View.GONE);
		}
	};

	public VideoBrightnessView(Context context) {
		this(context, null);
	}

	public VideoBrightnessView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_video_brightness, this);
		initView();
	}

	private void initView() {
		mTvBrightness = (TextView) findViewById(R.id.tv_brightness);
	}

	public void setProgreess(int brightness) {
		if (brightness < 0) {
			brightness = 0;
		} else if (brightness > 100) {
			brightness = 100;
		}
		if (brightness >= 0 && brightness <= 100) {
			removeCallbacks(mHideRunnable);
			setVisibility(View.VISIBLE);
			mTvBrightness.setText(brightness + "%");
			postDelayed(mHideRunnable, 1000);
		}
	}
}
