package com.polar.browser.cropedit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.DensityUtil;

import org.sprite2d.apps.pp.PainterCanvas;

public class ColorBoxView extends RelativeLayout implements View.OnClickListener {

	private SeekBar mSBBrushSize;

	private com.polar.browser.cropedit.BrushPoit mBrushPoit;

	private PainterCanvas mCanvas;

	private View mColorView1;
	private View mColorView2;
	private View mColorView3;
	private View mColorView4;
	private View mColorView5;
	private View mColorView6;
	private View mColorView7;
	private View mColorView8;
	private View mColorView9;
	private View mColorView10;

	private com.polar.browser.cropedit.IColorBoxChange mColorBoxChange;

	private int mLastSelectIndex;

	private float mDensity;

	public ColorBoxView(Context context) {
		this(context, null);
	}

	public ColorBoxView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public void setIColorBoxChange(com.polar.browser.cropedit.IColorBoxChange colorBoxChange) {
		this.mColorBoxChange = colorBoxChange;
	}

	private void init() {
		inflate(getContext(), R.layout.view_color_box, this);
		initView();
		mDensity = getResources().getDisplayMetrics().density;
	}

	private void initView() {
		mBrushPoit = (com.polar.browser.cropedit.BrushPoit) findViewById(R.id.view_poit);
		mBrushPoit.setType(com.polar.browser.cropedit.BrushPoit.TYPE_BIG);
		initColors();
		initBrushWidthSeerkBar();
		initDefult();
	}

	private void initDefult() {
		mLastSelectIndex = 7;
		mColorView7.setSelected(true);
	}

	public void setCanvas(PainterCanvas canvas) {
		this.mCanvas = canvas;
	}

	private void initColors() {
		mColorView1 = findViewById(R.id.color_box1);
		mColorView2 = findViewById(R.id.color_box2);
		mColorView3 = findViewById(R.id.color_box3);
		mColorView4 = findViewById(R.id.color_box4);
		mColorView5 = findViewById(R.id.color_box5);
		mColorView6 = findViewById(R.id.color_box6);
		mColorView7 = findViewById(R.id.color_box7);
		mColorView8 = findViewById(R.id.color_box8);
		mColorView9 = findViewById(R.id.color_box9);
		mColorView10 = findViewById(R.id.color_box10);
		mColorView1.setOnClickListener(this);
		mColorView2.setOnClickListener(this);
		mColorView3.setOnClickListener(this);
		mColorView4.setOnClickListener(this);
		mColorView5.setOnClickListener(this);
		mColorView6.setOnClickListener(this);
		mColorView7.setOnClickListener(this);
		mColorView8.setOnClickListener(this);
		mColorView9.setOnClickListener(this);
		mColorView10.setOnClickListener(this);
		findViewById(R.id.root_color_box).setOnClickListener(this);
	}

	private void initBrushWidthSeerkBar() {
		// progress 0 ~ 100
		mSBBrushSize = (SeekBar) findViewById(R.id.brush_size);
		mSBBrushSize
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar seekBar) {
						if (seekBar.getProgress() > 3) {
							float p = seekBar.getProgress() * 0.9f / 3 * mDensity;
							if (p < 6) {
								p = 6;
							}
							mCanvas.setPresetSize(p);
							mBrushPoit.setSize(p);
							mBrushPoit.postInvalidate();
							if (mColorBoxChange != null) {
								mColorBoxChange.changedSize(seekBar.getProgress() * 0.72f / 3 * mDensity);
							}
						} else {
							seekBar.setProgress(3);
							mBrushPoit.setSize(6);
							mBrushPoit.postInvalidate();
							mCanvas.setPresetSize(6);
							if (mColorBoxChange != null) {
								mColorBoxChange.changedSize(6);
							}
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
								mBrushPoit.setSize(p);
								mBrushPoit.postInvalidate();
								if (mColorBoxChange != null) {
									mColorBoxChange.changedSize(seekBar.getProgress() * 0.72f / 3 * mDensity);
								}
							}
						} else {
							seekBar.setProgress(3);
							mBrushPoit.setSize(6);
							mBrushPoit.postInvalidate();
							mCanvas.setPresetSize(6);
							if (mColorBoxChange != null) {
								mColorBoxChange.changedSize(6);
							}
						}
					}
				});
		LayoutParams params = (LayoutParams) mBrushPoit.getLayoutParams();
		int width = DensityUtil.dip2px(JuziApp.getInstance(), 34);
		params.width = width;
		params.height = width;
		mBrushPoit.setLayoutParams(params);
		mBrushPoit.postInvalidate();
	}

	@Override
	public void onClick(View v) {
		int color = 0;
//		String colorValue = null;
		switch (v.getId()) {
			case R.id.color_box1:
				color = getResources().getColor(R.color.color_box_item_1);
//			colorValue = ConfigDefine.UM_SCEENSHOT_PAINT_COLOR_1;
				clearSelect();
				mLastSelectIndex = 1;
				mColorView1.setSelected(true);
				break;
			case R.id.color_box2:
				color = getResources().getColor(R.color.color_box_item_2);
				clearSelect();
				mLastSelectIndex = 2;
				mColorView2.setSelected(true);
				break;
			case R.id.color_box3:
				color = getResources().getColor(R.color.color_box_item_3);
				clearSelect();
				mLastSelectIndex = 3;
				mColorView3.setSelected(true);
				break;
			case R.id.color_box4:
				color = getResources().getColor(R.color.color_box_item_4);
				clearSelect();
				mLastSelectIndex = 4;
				mColorView4.setSelected(true);
				break;
			case R.id.color_box5:
				color = getResources().getColor(R.color.color_box_item_5);
				clearSelect();
				mLastSelectIndex = 5;
				mColorView5.setSelected(true);
				break;
			case R.id.color_box6:
				color = getResources().getColor(R.color.color_box_item_6);
				clearSelect();
				mLastSelectIndex = 6;
				mColorView6.setSelected(true);
				break;
			case R.id.color_box7:
				color = getResources().getColor(R.color.color_box_item_7);
				clearSelect();
				mLastSelectIndex = 7;
				mColorView7.setSelected(true);
				break;
			case R.id.color_box8:
				color = getResources().getColor(R.color.color_box_item_8);
				clearSelect();
				mLastSelectIndex = 8;
				mColorView8.setSelected(true);
				break;
			case R.id.color_box9:
				color = getResources().getColor(R.color.color_box_item_9);
				clearSelect();
				mLastSelectIndex = 9;
				mColorView9.setSelected(true);
				break;
			case R.id.color_box10:
				color = getResources().getColor(R.color.color_box_item_10);
				clearSelect();
				mLastSelectIndex = 10;
				mColorView10.setSelected(true);
				break;
			case R.id.root_color_box:
				return;
			default:
				break;
		}
		if (color != 0) {
			mCanvas.setPresetColor(color);
			mBrushPoit.setColor(color);
			mBrushPoit.postInvalidate();
			if (mColorBoxChange != null) {
				mColorBoxChange.changedColor(color);
			}
		}
//		if (colorValue != null) {
//			Map<String,String> map_value = new HashMap<String, String>();
//			map_value.put(ConfigDefine.UM_SCEENSHOT_PAINT_COLOR_TYPE, colorValue);
//		}
		Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_COLOR);
	}

	private void clearSelect() {
		switch (mLastSelectIndex) {
			case 1:
				mColorView1.setSelected(false);
				break;
			case 2:
				mColorView2.setSelected(false);
				break;
			case 3:
				mColorView3.setSelected(false);
				break;
			case 4:
				mColorView4.setSelected(false);
				break;
			case 5:
				mColorView5.setSelected(false);
				break;
			case 6:
				mColorView6.setSelected(false);
				break;
			case 7:
				mColorView7.setSelected(false);
				break;
			case 8:
				mColorView8.setSelected(false);
				break;
			case 9:
				mColorView9.setSelected(false);
				break;
			case 10:
				mColorView10.setSelected(false);
				break;
			default:
				break;
		}
	}
}
