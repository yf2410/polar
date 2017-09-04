package com.polar.browser.cropedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.DensityUtil;

import org.sprite2d.apps.pp.MosaicsCanvas;

public class MosaicsView extends RelativeLayout implements android.view.View.OnClickListener {

	private MosaicsCanvas mCanvas;

	private View mMosaicsMask;
	private View mMosaicsToolbarMask;

	private com.polar.browser.cropedit.IFinishedMosaics iFinishedMosaics;

	private com.polar.browser.cropedit.MosaicsBrushPoint mBtnMosaicsWidth;

	private SeekBar mSBBrushWidth;

	private View btnMosaicsRevert;

	private View mMosaicsWidthView;
	private View mMosaicsArrow;

	private ImageView mMosaicsSizeMin;
	private ImageView mMosaicsSizeMid;
	private ImageView mMosaicsSizeMax;

	private float mDensity;

	public MosaicsView(Context context) {
		this(context, null);
	}

	public MosaicsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_mosaiics, this);
		initView();
		initListener();
		initPoitWidth();
		initBrushWidthSeerkBar();
	}

	private void initView() {
		mCanvas = (MosaicsCanvas) findViewById(R.id.mosaics_canvas);
		mBtnMosaicsWidth = (com.polar.browser.cropedit.MosaicsBrushPoint) findViewById(R.id.btn_mosaics_width);
		btnMosaicsRevert = findViewById(R.id.btn_mosaics_revert);
		mMosaicsWidthView = findViewById(R.id.rl_mosaics_width);
		mMosaicsArrow = findViewById(R.id.mosaics_arrow);
		mMosaicsMask = findViewById(R.id.mosaics_mask);
		mMosaicsToolbarMask = findViewById(R.id.mosaics_toolbar_mask);
		mMosaicsSizeMin = (ImageView) findViewById(R.id.size_min);
		mMosaicsSizeMid = (ImageView) findViewById(R.id.size_mid);
		mMosaicsSizeMax = (ImageView) findViewById(R.id.size_max);
		mDensity = getResources().getDisplayMetrics().density;
		mCanvas.setPresetSize(30);
		setRevertEnabled(false);
	}

	private void initBrushWidthSeerkBar() {
		// progress 0 ~ 100
		mSBBrushWidth = (SeekBar) findViewById(R.id.seekbar_mosaics_width);
		mSBBrushWidth
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar seekBar) {
						if (seekBar.getProgress() > 3) {
							float p = seekBar.getProgress() * 0.9f / 3 * mDensity;
							if (p < 6) {
								p = 6;
							}
							mCanvas.setPresetSize(p);
							mBtnMosaicsWidth.setSize(p);
							mBtnMosaicsWidth.postInvalidate();
						} else {
							seekBar.setProgress(3);
							mBtnMosaicsWidth.setSize(6);
							mBtnMosaicsWidth.postInvalidate();
							mCanvas.setPresetSize(6);
						}
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onProgressChanged(SeekBar seekBar,
												  int progress, boolean fromUser) {
						if (progress > 3) {
							if (fromUser) {
								float p = seekBar.getProgress() * 0.9f / 3 * mDensity;
								if (p < 6) {
									p = 6;
								}
								mCanvas.setPresetSize(p);
								mBtnMosaicsWidth.setSize(p);
								mBtnMosaicsWidth.postInvalidate();
							}
						} else {
							seekBar.setProgress(3);
							mBtnMosaicsWidth.setSize(6);
							mBtnMosaicsWidth.postInvalidate();
							mCanvas.setPresetSize(6);
						}
					}
				});
	}

	private void initListener() {
		findViewById(R.id.mosaics_cancel).setOnClickListener(this);
		findViewById(R.id.mosaics_ok).setOnClickListener(this);
		mBtnMosaicsWidth.setOnClickListener(this);
		btnMosaicsRevert.setOnClickListener(this);
		mMosaicsMask.setOnClickListener(this);
		mMosaicsToolbarMask.setOnClickListener(this);
		mMosaicsSizeMin.setOnClickListener(this);
		mMosaicsSizeMid.setOnClickListener(this);
		mMosaicsSizeMax.setOnClickListener(this);
	}

	private void initPoitWidth() {
		ViewGroup.LayoutParams params = mBtnMosaicsWidth.getLayoutParams();
		int width = DensityUtil.dip2px(JuziApp.getInstance(), 34);
		params.width = width;
		params.height = width;
		mBtnMosaicsWidth.setLayoutParams(params);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.mosaics_cancel:
				cancelMosaics();
				break;
			case R.id.mosaics_ok:
				handleMosaics();
				CropEditActivity.sSavedPic = false;
				break;
			case R.id.btn_mosaics_width:
				changeMosaicsWidth();
				break;
			case R.id.btn_mosaics_revert:
				handleRevert();
				break;
			case R.id.mosaics_mask:
			case R.id.mosaics_toolbar_mask:
				hideToolbar();
				break;
			case R.id.size_min:
				handleMosaicsSelect(com.polar.browser.cropedit.MosaicsBrushPoint.TYPE_MIN);
				break;
			case R.id.size_mid:
				handleMosaicsSelect(com.polar.browser.cropedit.MosaicsBrushPoint.TYPE_MID);
				break;
			case R.id.size_max:
				handleMosaicsSelect(com.polar.browser.cropedit.MosaicsBrushPoint.TYPE_MAX);
				break;
			default:
				break;
		}
	}

	private void handleMosaicsSelect(int size) {
//		String typeValue = null;
		switch (size) {
			case com.polar.browser.cropedit.MosaicsBrushPoint.TYPE_MIN:
				mMosaicsSizeMin.setBackgroundResource(R.drawable.icon_mosaics_selected);
				mMosaicsSizeMid.setBackgroundResource(R.drawable.icon_mosaics_un_select);
				mMosaicsSizeMax.setBackgroundResource(R.drawable.icon_mosaics_un_select);
				mBtnMosaicsWidth.setType(size);
				mCanvas.getThread().changeMosaicsSize(DensityUtil.dip2px(getContext(), 4));
//			typeValue = ConfigDefine.UM_SCEENSHOT_PAINT_MOSAICS_TYPE1;
				break;
			case com.polar.browser.cropedit.MosaicsBrushPoint.TYPE_MID:
				mMosaicsSizeMid.setBackgroundResource(R.drawable.icon_mosaics_selected);
				mMosaicsSizeMin.setBackgroundResource(R.drawable.icon_mosaics_un_select);
				mMosaicsSizeMax.setBackgroundResource(R.drawable.icon_mosaics_un_select);
				mBtnMosaicsWidth.setType(size);
				mCanvas.getThread().changeMosaicsSize(DensityUtil.dip2px(getContext(), 6));
//			typeValue = ConfigDefine.UM_SCEENSHOT_PAINT_MOSAICS_TYPE2;
				break;
			case com.polar.browser.cropedit.MosaicsBrushPoint.TYPE_MAX:
				mMosaicsSizeMax.setBackgroundResource(R.drawable.icon_mosaics_selected);
				mMosaicsSizeMin.setBackgroundResource(R.drawable.icon_mosaics_un_select);
				mMosaicsSizeMid.setBackgroundResource(R.drawable.icon_mosaics_un_select);
				mBtnMosaicsWidth.setType(size);
				mCanvas.getThread().changeMosaicsSize(DensityUtil.dip2px(getContext(), 9));
//			typeValue = ConfigDefine.UM_SCEENSHOT_PAINT_MOSAICS_TYPE3;
				break;
			default:
				break;
		}
		Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_MOSAICS_SELECT);
	}

	private void hideToolbar() {
		mMosaicsWidthView.setVisibility(GONE);
		mMosaicsArrow.setVisibility(GONE);
		mMosaicsMask.setVisibility(GONE);
		mMosaicsToolbarMask.setVisibility(GONE);
	}

	private void changeMosaicsWidth() {
		mMosaicsWidthView.setVisibility(VISIBLE);
		mMosaicsArrow.setVisibility(VISIBLE);
		mMosaicsMask.setVisibility(VISIBLE);
		mMosaicsToolbarMask.setVisibility(VISIBLE);
		Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_MOSAICS_WIDTH);
	}

	private void handleRevert() {
		if (MosaicsCanvas.mCurrentPaintIndex > 0) {
			MosaicsCanvas.mCurrentPaintIndex--;
			if (MosaicsCanvas.mCurrentPaintIndex == 0) {
				setRevertEnabled(false);
			}
		}
		Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_MOSAICS_REVERT);
	}

	public void setRevertEnabled(boolean enabled) {
		btnMosaicsRevert.setEnabled(enabled);
	}

	public void setBitmap(Bitmap bitmap) {
		mCanvas.getThread().setBitmap(bitmap, false);
		mCanvas.setBitmap(bitmap);
	}

	public void resetCanvasSize(int width, int height, int topMargin) {
		LayoutParams params = (LayoutParams) mCanvas.getLayoutParams();
		params.width = width;
		params.height = height;
		params.topMargin = topMargin;
		mCanvas.setLayoutParams(params);
		mMosaicsMask.setLayoutParams(params);
	}

	public void registerListener(com.polar.browser.cropedit.IFinishedMosaics finishedMosaics) {
		this.iFinishedMosaics = finishedMosaics;
	}

	public void cancelMosaics() {
		hideToolbar();
		MosaicsCanvas.clearAllActions();
		setVisibility(GONE);
		if (iFinishedMosaics != null) {
			iFinishedMosaics.cancelMosaics();
		}
		setRevertEnabled(false);
		mCanvas.getThread().pauseDrawing();
	}

	public void handleMosaics() {
		hideToolbar();
		Bitmap bitmap = mCanvas.getThread().getBitmap();
		MosaicsCanvas.clearAllActions();
		setVisibility(GONE);
		if (iFinishedMosaics != null) {
			iFinishedMosaics.finishedMosaics(bitmap);
		}
		setRevertEnabled(false);
		mCanvas.getThread().pauseDrawing();
	}

	public void showMosaicsView() {
		setVisibility(VISIBLE);
		mCanvas.post(new Runnable() {
			@Override
			public void run() {
				mCanvas.getThread().startDrawing();
			}
		});
	}

	public boolean onBackPress() {
		if (mMosaicsMask.isShown()) {
			hideToolbar();
			return true;
		} else if (this.isShown()) {
			cancelMosaics();
			return true;
		}
		return false;
	}
}
