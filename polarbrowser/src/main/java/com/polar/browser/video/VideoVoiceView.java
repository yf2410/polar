package com.polar.browser.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;

public class VideoVoiceView extends RelativeLayout {

	private TextView mTvVoice;
	private ImageView mIvVoice;
	private Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			setVisibility(View.GONE);
		}
	};

	public VideoVoiceView(Context context) {
		this(context, null);
	}

	public VideoVoiceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_video_voice, this);
		initView();
	}

	private void initView() {
		mTvVoice = (TextView) findViewById(R.id.tv_voice);
		mIvVoice = (ImageView) findViewById(R.id.icon_voice);
	}

	public void setProgreess(int brightness) {
		if (brightness >= 0 && brightness <= 100) {
			if (brightness == 0) {
				mIvVoice.setImageResource(R.drawable.video_no_voice);
			} else {
				mIvVoice.setImageResource(R.drawable.video_voice);
			}
			removeCallbacks(mHideRunnable);
			setVisibility(View.VISIBLE);
			mTvVoice.setText(brightness + "%");
			postDelayed(mHideRunnable, 600);
		}
	}
}
