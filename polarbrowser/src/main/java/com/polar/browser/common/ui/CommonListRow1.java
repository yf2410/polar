package com.polar.browser.common.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.UIUtils;

/**
 * 左边是一个icon（默认隐藏），中间两行文字，第二行为补充说明啥的（默认隐藏），右边是一个图标
 * 自定义时分成左中右三部分，使用相应的set方法设定自定义View或layout 可以设置背景和高度
 */
public class CommonListRow1 extends LinearLayout {
	protected ImageView mImgIcon;

	protected TextView mTxtTitle;

	protected TextView mTxtSummary;

	protected TextView mTxtStatus;

	protected ImageView mImgRight;

	protected LinearLayout mLLRoot;

	private boolean mDefineHeight;

	private String mDefineTitle;

	private boolean mDefineHeaderDivider;

	private boolean mDefineFootDivider;

	private Drawable mDefineSrc;

	public CommonListRow1(Context context, Drawable imgIcon, String title, String summary, Drawable imgRight) {
		super(context);
		initView(context);
		setImageIcon(imgIcon);
		setTitleText(title);
		setSummaryText(summary);
		setImageRight(imgRight);
	}

	public CommonListRow1(Context context) {
		super(context);
		initView(context);
	}

	public CommonListRow1(Context context, AttributeSet attrs) {
		super(context, attrs);
		String h = UIUtils.getValueFromAttrs(context, attrs, "layout_height");
		if (!TextUtils.isEmpty(h) && !h.equals(String.valueOf(LayoutParams.WRAP_CONTENT)) && !h.equals(String.valueOf(LayoutParams.MATCH_PARENT))) {
			mDefineHeight = true;
		}
		String text = UIUtils.getTextFromAttrs(context, attrs);
		if (!TextUtils.isEmpty(text)) {
			mDefineTitle = text;
		}
		text = UIUtils.getValueFromAttrs(context, attrs, "headerDividersEnabled");
		if (!TextUtils.isEmpty(text) && text.equals("true")) {
			mDefineHeaderDivider = true;
		}
		text = UIUtils.getValueFromAttrs(context, attrs, "footerDividersEnabled");
		if (!TextUtils.isEmpty(text) && text.equals("true")) {
			mDefineFootDivider = true;
		}
		mDefineSrc = UIUtils.getDrawableFromAttrs(context, attrs, "src");
		initView(context);
	}

	private void initView(Context context) {
		inflate(context, R.layout.common_list_row1, this);
		mImgIcon = (ImageView) findViewById(R.id.common_img_icon);
		mTxtTitle = (TextView) findViewById(R.id.common_tv_title);
		mTxtSummary = (TextView) findViewById(R.id.common_tv_summary);
		mTxtStatus = (TextView) findViewById(R.id.common_tv_status);
		mImgRight = (ImageView) findViewById(R.id.common_img_right);
		mLLRoot = (LinearLayout) findViewById(R.id.common_ll_root);
		int bgRes = 0;
		if (mDefineHeaderDivider && !mDefineFootDivider) { // 只有上边框
			bgRes = R.drawable.common_list_row1_frame_t;
		} else if (!mDefineHeaderDivider && mDefineFootDivider) { // 只有下边框
			bgRes = R.drawable.common_list_row1_frame_b;
		} else if (mDefineHeaderDivider && mDefineFootDivider) { // 上下边框都有
			bgRes = R.drawable.common_list_row1_frame_tb;
		}
		if (bgRes != 0) {
			int paddingLeft = mLLRoot.getPaddingLeft();
			int paddingTop = mLLRoot.getPaddingTop();
			int paddingRight = mLLRoot.getPaddingRight();
			int paddingBottom = mLLRoot.getPaddingBottom();
			mLLRoot.setBackgroundResource(bgRes);
			mLLRoot.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
		}
		if (!mDefineHeight) {
			setHeight((int) getResources().getDimension(R.dimen.common_list_row_height_3));
		}
		if (mDefineSrc != null) {
			setImageIcon(mDefineSrc);
		}
		if (!TextUtils.isEmpty(mDefineTitle)) {
			setTitleText(mDefineTitle);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		UIUtils.setViewGroupEnabled(this, isEnabled());
	}

	public void setMarginLeftMiddle(int px) {
		setMarginRight(R.id.common_img_icon, px);
	}

	public void setMarginMiddleRight(int px) {
		setMarginRight(R.id.common_ll_middle, px);
	}

	private void setMarginRight(int viewId, int px) {
		View v = findViewById(viewId);
		LayoutParams lp = (LayoutParams) v.getLayoutParams();
		lp.rightMargin = px;
		v.setLayoutParams(lp);
	}

	public void setRowPadding(int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
		findViewById(R.id.common_ll_root).setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
	}

	public void setHeight(int pix) {
		LayoutParams lp = (LayoutParams) mLLRoot.getLayoutParams();
		lp.height = pix;
		mLLRoot.setLayoutParams(lp);
	}

	@Override
	public void setBackgroundColor(int color) {
		super.setBackgroundColor(color);
		if (mLLRoot != null) {
			mLLRoot.setBackgroundColor(color);
		}
	}

	@Override
	public void setBackgroundDrawable(Drawable background) {
		super.setBackgroundDrawable(background);
		if (mLLRoot != null) {
			mLLRoot.setBackgroundDrawable(background);
		}
	}

	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(resid);
		if (mLLRoot != null) {
			mLLRoot.setBackgroundResource(resid);
		}
	}

	public void setMiddleView(View v) {
		setView(R.id.common_ll_middle, v);
	}

	public void setMiddleView(int layoutResId) {
		setView(R.id.common_ll_middle, layoutResId);
	}

	public void setLeftView(View v) {
		setView(R.id.common_ll_left, v);
	}

	public void setLeftView(int layoutResId) {
		setView(R.id.common_ll_left, layoutResId);
	}

	public void setRightView(View v) {
		setView(R.id.common_ll_right, v);
	}

	public void setRightView(int layoutResId) {
		setView(R.id.common_ll_right, layoutResId);
	}

	private void setView(int rootId, View v) {
		LinearLayout ll = (LinearLayout) findViewById(rootId);
		ll.removeAllViews();
		ll.addView(v);
	}

	private void setView(int rootId, int layoutId) {
		LinearLayout ll = (LinearLayout) findViewById(rootId);
		ll.removeAllViews();
		inflate(getContext(), layoutId, ll);
	}

	public ImageView getImageIcon() {
		return mImgIcon;
	}

	public void setImageIcon(int img) {
		setImageIcon(getResources().getDrawable(img));
	}

	public void setImageIcon(Drawable img) {
		if (img != null) {
			mImgIcon.setVisibility(View.VISIBLE);
			mImgIcon.setImageDrawable(img);
		} else {
			mImgIcon.setVisibility(View.GONE);
			mImgIcon.setImageDrawable(null);
		}
	}

	public ImageView getImageRight() {
		return mImgRight;
	}

	public void setImageRight(Drawable imgRight) {
		if (imgRight != null) {
			if (mImgRight != null) {
				mImgRight.setVisibility(View.VISIBLE);
				mImgRight.setImageDrawable(imgRight);
			}
		} else {
			if (mImgRight != null) {
				mImgRight.setVisibility(View.GONE);
				mImgRight.setImageDrawable(null);
			}
		}
	}

	public TextView getTitleView() {
		return mTxtTitle;
	}

	public void setTitleText(CharSequence txt) {
		mTxtTitle.setText(txt);
	}

	public void setTitleText(int txt) {
		mTxtTitle.setText(txt);
	}

	public TextView getSummaryView() {
		return mTxtSummary;
	}

	public TextView getStatusView() {
		return mTxtStatus;
	}

	public void setSummaryText(CharSequence txt) {
		if (TextUtils.isEmpty(txt)) {
			return;
		}
		if (mTxtSummary.getVisibility() != View.VISIBLE) {
			mTxtSummary.setVisibility(View.VISIBLE);
			setHeight((int) getResources().getDimension(R.dimen.common_list_row_height_1));
		}
		mTxtSummary.setText(txt);
	}

	public void setSummaryText(int txt) {
		String str = getResources().getString(txt);
		setSummaryText(str);
	}

	public void setStatusText(CharSequence txt) {
		if (TextUtils.isEmpty(txt)) {
			return;
		}
		if (mTxtStatus.getVisibility() != View.VISIBLE) {
			mTxtStatus.setVisibility(View.VISIBLE);
		}
		mTxtStatus.setText(txt);
	}

	public void setStatusText(int txt) {
		String str = getResources().getString(txt);
		setStatusText(str);
	}
}

