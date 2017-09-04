package com.polar.browser.video;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.polar.browser.R;
import com.polar.browser.utils.SysUtils;

public class VideoActivity extends Activity {

	private int mStopPosition;

	private CustomVideoView mCustomVideoView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_video);
		Intent intent = getIntent();
		String fileName = intent.getStringExtra("fileName");
		String filePath = intent.getStringExtra("filePath");
		mCustomVideoView = (CustomVideoView) findViewById(R.id.custom_videoview);
		playVideo(filePath, fileName);
	}

	private void playVideo(String filePath, String fileName) {
		mCustomVideoView.init();
		SysUtils.setFullScreen(this, true);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		mCustomVideoView.playCustomVideo(filePath, fileName);
		mCustomVideoView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCustomVideoView.isShown()) {
			SysUtils.setFullScreen(this, true);
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			mCustomVideoView.resumePosition(mStopPosition);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mCustomVideoView.onPause();
		if (mCustomVideoView != null) {
			mStopPosition = mCustomVideoView.getCurrentPosition();
		}
	}

	@Override
	public void finish() {
		if (mCustomVideoView != null) {
			mCustomVideoView.finish();
		}
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
