package com.polar.browser.video;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.VideoView;

/**
 * 自动全屏的VideoView
 */
public class FullScreenVideoView extends VideoView {

	private int videoWidth;
	private int videoHeight;

	private SurfaceHolder mHolder;

	public FullScreenVideoView(Context context) {
		this(context, null);
	}

	public FullScreenVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void init() {
		mHolder = getHolder();
	}

	public void clearScreen() {
		Canvas canvas = mHolder.lockCanvas(null);
		canvas.drawColor(Color.BLACK);// 清除画布
		mHolder.unlockCanvasAndPost(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = getDefaultSize(videoWidth, widthMeasureSpec);
		int height = getDefaultSize(videoHeight, heightMeasureSpec);
		if (videoWidth > 0 && videoHeight > 0) {
			if (videoWidth * height > width * videoHeight) {
				height = width * videoHeight / videoWidth;
			} else if (videoWidth * height < width * videoHeight) {
				width = height * videoWidth / videoHeight;
			}
		}
		setMeasuredDimension(width, height);
	}

	public int getVideoWidth() {
		return videoWidth;
	}

	public void setVideoWidth(int videoWidth) {
		this.videoWidth = videoWidth;
	}

	public int getVideoHeight() {
		return videoHeight;
	}

	public void setVideoHeight(int videoHeight) {
		this.videoHeight = videoHeight;
	}
}
