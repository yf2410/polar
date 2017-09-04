package com.polar.browser.common.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.polar.browser.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 底部buttonbar，默认灰绿红加checkbox 可以通过setButtons定制按钮
 */
public class CommonBottomBar2 extends CommonBottomBar1 {

	private LinearLayout mLLBtns;

	public CommonBottomBar2(Context context) {
		super(context);
		init();
	}

	public CommonBottomBar2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@Override
	protected void init() {
		inflate(getContext(), R.layout.common_bottom_btns_bar2, this);
		mLLBtns = (LinearLayout) findViewById(R.id.common_ll_btns);
		Button btnL = (Button) findViewById(R.id.common_btn_left);
		Button btnM = (Button) findViewById(R.id.common_btn_middle);
		Button btnR = (Button) findViewById(R.id.common_btn_right);
		mBtns = new Button[]{
				btnL, btnM, btnR
		};
	}

	/**
	 * 举个例子，如果想要左红右绿的buttonbar，这样调 setButtons(new
	 * int[]{CommonBottomBar2.TYPE_BTN_RED, CommonBottomBar2.TYPE_BTN_GREEN})
	 *
	 * @param buttons 定制类型数组
	 * @return 定制的button数组，可以用来设置文本或ClickListener
	 */
	@Override
	public Button[] setButtons(int... buttons) {
		if (buttons == null) {
			return null;
		}
		Button[] btns = new Button[buttons.length];
		mLLBtns.removeAllViews();
		for (int i = 0; i < buttons.length; i++) {
			Button btn = new Button(getContext());
			btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.common_font_size_d));
			switch (buttons[i]) {
				case TYPE_BTN_GREEN:
					btn.setTextColor(getResources().getColor(R.color.common_font_color_9));
					break;
				case TYPE_BTN_GREY:
					btn.setTextColor(getResources().getColor(R.color.common_font_color_10));
					break;
				case TYPE_BTN_RED:
					btn.setTextColor(getResources().getColor(R.color.common_font_color_6));
					break;
				default:
					throw new IllegalArgumentException("wrong type");
			}
			LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
			mLLBtns.addView(btn, lp);
			btns[i] = btn;
			if (i != buttons.length - 1) {
				View divider = new View(getContext());
				divider.setBackgroundColor(getResources().getColor(R.color.common_dlg_bar_divider));
				lp = new LayoutParams(1, LayoutParams.MATCH_PARENT);
				mLLBtns.addView(divider, lp);
			}
		}
		mBtns = btns;
		mLLBtns.invalidate();
		return btns;
	}

	/**
	 * 刷新各个按钮的背景和分隔线
	 */
	private void refreshButtons() {
		if (mBtns == null) {
			return;
		}
		List<Button> listVisible = new ArrayList<Button>();
		for (Button btn : mBtns) {
			int idx = mLLBtns.indexOfChild(btn) + 1;
			View divider = mLLBtns.getChildAt(idx);
			if (divider != null) {
				divider.setVisibility(View.GONE);
			}
			if (btn.getVisibility() == View.VISIBLE) {
				listVisible.add(btn);
			}
		}
		if (listVisible.size() == 1) {
			listVisible.get(0).setBackgroundResource(R.drawable.common_bar_btn_container);
		} else {
			for (int i = 0; i < listVisible.size(); i++) {
				Button btn = listVisible.get(i);
				if (i == listVisible.size() - 1) {
					btn.setBackgroundResource(R.drawable.common_bar_btn_rb);
					btn.setTextColor(getResources().getColor(R.color.set_about));
				} else {
					if (i == 0) {
						btn.setBackgroundResource(R.drawable.common_bar_btn_lb);
						btn.setTextColor(getResources().getColor(R.color.gray));
					} else {
						btn.setBackgroundResource(R.drawable.common_bar_btn_mb);
						btn.setTextColor(getResources().getColor(R.color.gray));
					}
					int idx = mLLBtns.indexOfChild(btn) + 1;
					View divider = mLLBtns.getChildAt(idx);
					if (divider != null) {
						divider.setVisibility(View.VISIBLE);
					}
				}
			}
		}
		/*findViewById(R.id.common_ll_top_line).setBackgroundColor(getResources().getColor(R.color.common_dlg_bar_divider));
		findViewById(R.id.common_ll_mid_line1).setBackgroundColor(getResources().getColor(R.color.common_dlg_bar_divider));
		findViewById(R.id.common_ll_mid_line2).setBackgroundColor(getResources().getColor(R.color.common_dlg_bar_divider));*/
	}

	@Override
	protected void drawableStateChanged() {
		refreshButtons();
		super.drawableStateChanged();
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		refreshButtons();
		return super.drawChild(canvas, child, drawingTime);
	}
}
