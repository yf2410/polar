package com.polar.browser.common.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.UIUtils;


public class CommonTitleBar extends LinearLayout {

	private ImageView mImgBack;
	private TextView mTvTitle;
	private TextView mTvSetting;
	private ImageView mImgSetting;
	private ImageView mRedPoint;
//	private View mRoot;
	private View mShadow;
	private View mStatusBar;

	private String mTitleText;
	private SETTING_TYPE mSettingType = SETTING_TYPE.SETTING_TYPE_TEXT;
//	private LinearLayout mTitleArea;
    private LinearLayout mCommonLeft;

    public CommonTitleBar(Context context) {
		super(context);
		init();
	}

	public CommonTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTitleText = UIUtils.getTextFromAttrs(context, attrs);
		init();
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);
		setFitsSystemWindows(true);
		final Context context = getContext();
		inflate(context, R.layout.common_title_bar, this);
        mCommonLeft = (LinearLayout) findViewById(R.id.common_ll_left);
//      mTitleArea = (LinearLayout) findViewById(R.id.common_ll_middle);
		mImgBack = (ImageView) findViewById(R.id.common_img_back);
		mTvTitle = (TextView) findViewById(R.id.common_tv_title);
		mTvSetting = (TextView) findViewById(R.id.common_tv_setting);
		mImgSetting = (ImageView) findViewById(R.id.common_img_setting);
		mRedPoint = (ImageView) findViewById(R.id.common_red_point);
//		mRoot = findViewById(R.id.common_titlebar_root);
		mShadow = findViewById(R.id.common_title_bar_shadow);
		mStatusBar = findViewById(R.id.common_titlebar_status_bar);
//        mRoot.setBackgroundResource(R.drawable.navigation_top_bg);
//        if (isInEditMode()) {
//            if (mRoot != null) {
//                mRoot.setBackgroundColor(0xF5F5F5);
//            }
//        } else {
//            mRoot.setBackgroundColor(getResources().getColor(R.color.common_grey_color1));
//        }
		if (!TextUtils.isEmpty(mTitleText)) {
			setTitle(mTitleText);
		}
		if (context instanceof Activity) {
			setOnBackListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					((Activity) context).finish();
				}
			});
		}
	}

	public TextView getTitleView() {
		return mTvTitle;
	}

	/**
	 * 设置标题居中（默认跟随左边返回箭头）
	 */
	public void setTitleHorizontalCenter() {
		//mTitleArea.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
	}

	/**
	 * 设置背景为透明
	 */
	public void setBackgroundTransparent() {
//        mRoot.setBackgroundColor(0);
		mShadow.setVisibility(View.VISIBLE);
	}

	/**
	 * 设置背景颜色
	 * @param resource
	 */
	public void setTitleBarBackground(int resource){
		if(resource != 0){
			findViewById(R.id.common_rl_background).setBackgroundResource(resource);
			findViewById(R.id.common_divider_line).setVisibility(View.GONE);
		}
	}

	public void setRedPointVisibility(int visibility) {
		mRedPoint.setVisibility(visibility);
	}

	private void setSettingType(SETTING_TYPE type) {
		mSettingType = type;
	}

	public void setRedPointResource(int resId) {
		mRedPoint.setImageResource(resId);
	}

	public void setRedPointDrawable(Drawable drawable) {
		mRedPoint.setImageDrawable(drawable);
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
		ViewGroup ll = (ViewGroup) findViewById(rootId);
		ll.removeAllViews();
		ll.addView(v);
	}

	private void setView(int rootId, int layoutId) {
		ViewGroup ll = (ViewGroup) findViewById(rootId);
		ll.removeAllViews();
		inflate(getContext(), layoutId, ll);
	}

	private ImageView getBackImageView() {
		return mImgBack;
	}

	public View getRightButton() {
		switch (mSettingType) {
			case SETTING_TYPE_TEXT:
				return mTvSetting;
			case SETTING_TYPE_IMG:
				return mImgSetting;
		}
		return null;
	}

	public void setTitle(CharSequence title) {
		if (mTvTitle != null) {
			mTvTitle.setText(title);
		}
	}

	public void setTitle(int title) {
		mTvTitle.setText(title);
	}

	/**
	 * 设置标题颜色
	 * @param color
     */
	public void setTitleColor(int color){
		if(color != 0){
			if(mTvTitle != null){
				ColorStateList csl=(ColorStateList)getResources().getColorStateList(color);
//				mTvTitle.setTextColor(getResources().getColor(color));
				mTvTitle.setTextColor(csl);
			}
		}
	}

	public void setSettingVisible(boolean visible) {
		mImgSetting.setVisibility(View.GONE);
		mTvSetting.setVisibility(View.GONE);
		if (visible) {
			switch (mSettingType) {
				case SETTING_TYPE_TEXT:
					mTvSetting.setVisibility(View.VISIBLE);
					break;
				case SETTING_TYPE_IMG:
					mImgSetting.setVisibility(View.VISIBLE);
					break;
			}
		}
	}

	public void setBackVisible(boolean visible) {
		mImgBack.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	/**
	 * 设置返回箭头
	 * @param resource
     */
	public void setBackImg(int resource){
		if(resource != 0){
			mImgBack.setImageResource(resource);
			mImgBack.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		}
	}

	public void setSettingTxt(int resId) {
		setSettingType(SETTING_TYPE.SETTING_TYPE_TEXT);
		setSettingVisible(true);
		mTvSetting.setText(resId);
	}

	public void setSettingTxt(CharSequence title) {
		setSettingType(SETTING_TYPE.SETTING_TYPE_TEXT);
		setSettingVisible(true);
		mTvSetting.setText(title);
	}

	public void setSettingImg(int resId) {
		setSettingType(SETTING_TYPE.SETTING_TYPE_IMG);
		setSettingVisible(true);
		mImgSetting.setImageResource(resId);
	}

	public void setSettingImg(Drawable drawable) {
		setSettingType(SETTING_TYPE.SETTING_TYPE_IMG);
		setSettingVisible(true);
		mImgSetting.setImageDrawable(drawable);
	}

	public void setOnBackListener(OnClickListener l) {
		//mImgBack.setOnClickListener(l);
		//mTitleArea.setOnClickListener(l);
        mCommonLeft.setOnClickListener(l);
	}

	public void setOnSettingListener(OnClickListener l) {
		switch (mSettingType) {
			case SETTING_TYPE_TEXT:
				mTvSetting.setOnClickListener(l);
				break;
			case SETTING_TYPE_IMG:
				mImgSetting.setOnClickListener(l);
				break;
		}
	}

	public void setOnButtonListener(OnClickListener l) {
		setOnBackListener(l);
		setOnSettingListener(l);
	}
	public void setOnMoreImgListener(OnClickListener l) {
		setOnBackListener(l);
		mTvSetting.setVisibility(View.GONE);
		mImgSetting.setVisibility(View.VISIBLE);
		mImgSetting.setOnClickListener(l);
	}

	public void setOnDoneTxTListener(OnClickListener l) {
		setOnBackListener(l);
		mTvSetting.setVisibility(View.VISIBLE);
		mImgSetting.setVisibility(View.GONE);
		mTvSetting.setOnClickListener(l);
	}

	public int getLeftButtonId() {
		return getBackImageView().getId();
	}

	public int getRightButtonId() {
		View v = getRightButton();
		return v != null ? v.getId() : 0;
	}

	private enum SETTING_TYPE {
		/**
		 * 有文字的设置按钮
		 */
		SETTING_TYPE_TEXT,
		/**
		 * 图标设置按钮
		 */
		SETTING_TYPE_IMG
	}
}
