package com.polar.browser.common.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.polar.browser.R;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.UIUtils;

/**
 * 底部buttonbar，默认灰绿红加checkbox 可以通过setButtons定制按钮
 */
public class CommonBottomBar1 extends LinearLayout {

	public static final int TYPE_BTN_NONE = 0;

	public static final int TYPE_BTN_GREEN = 1;

	public static final int TYPE_BTN_GREY = 2;

	public static final int TYPE_BTN_RED = 3;

	protected Button[] mBtns;

	private CommonCheckBox1 mCheckBox;

	private int mBtnMargin;
	// 给checkbox加边框
	private boolean mCheckBackgroundVisible;

	public CommonBottomBar1(Context context) {
		super(context);
		init();
	}

	public CommonBottomBar1(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void init() {
		inflate(getContext(), R.layout.common_bottom_btns_bar1, this);
		Button btnL = (Button) findViewById(R.id.common_btn_left);
		Button btnM = (Button) findViewById(R.id.common_btn_middle);
		Button btnR = (Button) findViewById(R.id.common_btn_right);
		mCheckBox = (CommonCheckBox1) findViewById(R.id.common_check);
		mBtns = new Button[]{
				btnL, btnM, btnR
		};
		mBtnMargin = DensityUtil.dip2px(getContext(), 8);
	}

	/**
	 * 举个例子，如果想要左红右绿的buttonbar，这样调 setButtons(new
	 * int[]{CommonBottomBar1.TYPE_BTN_RED, CommonBottomBar1.TYPE_BTN_GREEN})
	 *
	 * @param buttons 定制类型数组
	 * @return 定制的button数组，可以用来设置文本或ClickListener
	 */
	public Button[] setButtons(int... buttons) {
		if (buttons == null) {
			return null;
		}
		Button[] btns = new Button[buttons.length];
		LinearLayout llBtns = (LinearLayout) findViewById(R.id.common_ll_btns);
		llBtns.removeAllViews();
		for (int i = 0; i < buttons.length; i++) {
			Button btn = null;
			switch (buttons[i]) {
				case TYPE_BTN_GREEN:
					btn = new CommonBtnB(getContext());
					break;
				case TYPE_BTN_GREY:
					btn = new CommonBtnD(getContext());
					break;
				case TYPE_BTN_RED:
					btn = new CommonBtnE(getContext());
					break;
				default:
					throw new IllegalArgumentException("wrong type");
			}
			LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT);
			lp.weight = 1;
			if (i > 0) {
				lp.leftMargin = DensityUtil.dip2px(getContext(), 8);
			}
			btn.setLayoutParams(lp);
			llBtns.addView(btn);
			btns[i] = btn;
		}
		mBtns = btns;
		llBtns.invalidate();
		return btns;
	}

	public void setCheckBackgroundVisible() {
		mCheckBackgroundVisible = true;
		mCheckBox.setVisibility(View.VISIBLE);
		mCheckBox.setBackgroundResource(R.drawable.common_btn_d);
		mCheckBox.setTextColor(getResources().getColor(R.color.common_font_color_10));
		LayoutParams lp = (LayoutParams) mCheckBox.getLayoutParams();
		lp.setMargins(DensityUtil.dip2px(getContext(), 8), 0, 0, 0);
	}

	/**
	 * 解决隐藏某个button后后面的button不能居中显示的问题
	 */
	private void refreshButtonMargins() {
		if (mBtns != null && mBtns.length > 1) {
			for (int i = 1; i < mBtns.length; i++) {
				if (mBtns[i].getVisibility() == View.VISIBLE) {
					LayoutParams lp = (LayoutParams) mBtns[i].getLayoutParams();
					if (mBtns[i - 1].getVisibility() == View.GONE) {
						lp.leftMargin = 0;
					} else {
						lp.leftMargin = mBtnMargin;
					}
					mBtns[i].setLayoutParams(lp);
				}
			}
		}
	}

	/**
	 * 某些特殊情况需要显示不一样宽度的button，有点button会扩展，有点会被压缩
	 *
	 * @param extend 是否扩展或压缩
	 */
	public void setAutoAdjustButtonWidth(boolean auto) {
		if (mBtns != null) {
			for (Button btn : mBtns) {
				LayoutParams lp = (LayoutParams) btn.getLayoutParams();
				lp.width = auto ? LayoutParams.WRAP_CONTENT : 0;
				btn.setLayoutParams(lp);
			}
		}
	}

	public void setBtnMargin(int px) {
		if (mBtns == null || mBtns.length == 1) {
			return;
		}
		mBtnMargin = px;
		for (int i = 1; i < mBtns.length; i++) {
			LayoutParams lp = (LayoutParams) mBtns[i].getLayoutParams();
			lp.leftMargin = px;
			mBtns[i].setLayoutParams(lp);
		}
	}

	public Button getButtonOK() {
		return (Button) findViewById(R.id.common_btn_middle);
	}

	public Button getButtonCancel() {
		return (Button) findViewById(R.id.common_btn_left);
	}

	public Button getButtonOption() {
		return (Button) findViewById(R.id.common_btn_right);
	}

	public CommonCheckBox1 getCheckBox() {
		return mCheckBox;
	}

	@Override
	protected void drawableStateChanged() {
		refreshButtonMargins();
		super.drawableStateChanged();
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		refreshButtonMargins();
		return super.drawChild(canvas, child, drawingTime);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		UIUtils.setViewGroupEnabled(this, isEnabled());
	}

	public void clearBackground() {
		LinearLayout llRoot = (LinearLayout) findViewById(R.id.common_ll_root);
		llRoot.setBackgroundColor(getResources().getColor(R.color.common_transparent));
		llRoot.setPadding(0, 0, 0, 0);
		LayoutParams lp = (LayoutParams) llRoot.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		llRoot.setLayoutParams(lp);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mCheckBox != null && mCheckBox.getVisibility() == View.VISIBLE && !mCheckBackgroundVisible) {
			int space = 10 * DensityUtil.dip2px(getContext(), 10);
			float x = event.getX();
			float y = event.getY();
			if (x > mCheckBox.getLeft() - space && x < mCheckBox.getRight() + space && y > mCheckBox.getTop() - space && y < mCheckBox.getBottom() + space) {
				mCheckBox.performClick();
			}
		}
		return super.onTouchEvent(event);
	}
}
