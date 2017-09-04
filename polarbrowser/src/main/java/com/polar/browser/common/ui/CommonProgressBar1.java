package com.polar.browser.common.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.polar.browser.R;

public class CommonProgressBar1 extends LinearLayout {

	private final Context mContext;
	private int mMax = 100;
	private int mProcess = 0;
	private ImageView mProgressBarBg = null;
	private ImageView mProgressBar = null;

	public CommonProgressBar1(Context context) {
		super(context);
		this.mContext = context;
		initial();
	}

	public CommonProgressBar1(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initial();
	}

	private final void initial() {
		inflate(mContext, R.layout.common_progress_bar1, this);
		mProgressBar = (ImageView) findViewById(R.id.common_progress_bar);
		mProgressBarBg = (ImageView) findViewById(R.id.common_progress_bar_bg);
		mProgressBar.setVisibility(View.GONE);
	}

	public void setBarBgColor(int color) {
		mProgressBarBg.setBackgroundColor(color);
	}

	public void setBarColor(int color) {
		mProgressBar.setBackgroundColor(color);
	}

	public void setMax(int max) {
		mMax = max;
	}

	private int getCountLength() {
		if (mMax == 0) {
			return mProgressBarBg.getWidth();
		}
		return ((mProgressBarBg.getWidth()) * mProcess / mMax);
	}

	public int getProgress() {
		return mProcess;
	}

	public void setProgress(int process) {
		if (process <= mMax) {
			if (process == 0) {
				mProcess = 0;
				mProgressBar.setVisibility(View.INVISIBLE);
			} else {
				mProgressBar.setVisibility(View.VISIBLE);
				mProcess = process;
			}
			ViewGroup.LayoutParams params = mProgressBar.getLayoutParams();
			if (params != null) {
				params.width = getCountLength();
				mProgressBar.setLayoutParams(params);
			}
		}
	}

	public void fadeOut(boolean isFadeOut, boolean isEnableNightMode) {
		if (isFadeOut) {
			mProgressBar.setBackgroundResource(R.drawable.progress_fade_out);
		} else {
			mProgressBar.setBackgroundResource(R.drawable.progress);
		}
	}
}
