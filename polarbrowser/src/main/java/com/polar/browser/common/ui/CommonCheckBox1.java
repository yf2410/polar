package com.polar.browser.common.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.UIUtils;

/**
 * 开启绿色，关闭灰色样式checkbox
 */
public class CommonCheckBox1 extends LinearLayout implements ICustomCheckBox, OnClickListener {
	private ImageView mImgButton;

	private TextView mTxtContent;

	private ViewGroup mCheckRoot;

	private boolean mChecked;

	private boolean mHalfChecked;

	private boolean mDefineEnabled = true;

	private String mDefineText;

	private OnCheckChangedListener mListener;

	private int[] mButtonDrawables;

	private int[] mHalfCheckDrawables;

	public CommonCheckBox1(Context context) {
		super(context);
		init();
	}

	public CommonCheckBox1(Context context, AttributeSet attrs) {
		super(context, attrs);
		String checked = UIUtils.getValueFromAttrs(context, attrs, "checked");
		if (!TextUtils.isEmpty(checked) && checked.equals("true")) {
			mChecked = true;
		}
		String enabled = UIUtils.getValueFromAttrs(context, attrs, "enabled");
		if (!TextUtils.isEmpty(enabled) && enabled.equals("true")) {
			mDefineEnabled = true;
		}
		String text = UIUtils.getTextFromAttrs(context, attrs);
		if (!TextUtils.isEmpty(text)) {
			mDefineText = text;
		}
		init();
	}

	private final void init() {
		inflate(getContext(), R.layout.common_checkbox1, this);
		mImgButton = (ImageView) findViewById(R.id.common_img_button);
		mCheckRoot = (ViewGroup) findViewById(R.id.common_check_root);
		mButtonDrawables = new int[]{
				R.drawable.common_checkbox1_checked, R.drawable.common_checkbox1_unchecked, R.drawable.common_checkbox1_checked_disabled, R.drawable.common_checkbox1_unchecked_disabled,
				R.drawable.common_checkbox1_checked, R.drawable.common_checkbox1_unchecked
		};
		mHalfCheckDrawables = new int[]{
				R.drawable.common_checkbox1_halfchecked, R.drawable.common_checkbox1_halfchecked_disabled
		};
		mTxtContent = (TextView) findViewById(R.id.common_tv_content);
		if (!TextUtils.isEmpty(mDefineText)) {
			mTxtContent.setText(mDefineText);
		}
		setOnClickListener(this);
		setEnabled(mDefineEnabled);
		refreshView();
	}

	@Override
	public void setBackgroundResource(int resid) {
		if (mCheckRoot == null) {
			super.setBackgroundResource(resid);
		} else {
			mCheckRoot.setBackgroundResource(resid);
		}
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		if (mCheckRoot == null) {
			super.setBackgroundDrawable(d);
		} else {
			mCheckRoot.setBackgroundDrawable(d);
		}
	}

	/**
	 * 0 enabled checked 1 enabled unchecked 2 unenabled checked 3 unenabled
	 * unchecked 4 enabled checked pressed 5 enabled unchecked pressed
	 */
	protected void setButtonDrawables(int[] drawables) {
		if (drawables == null || drawables.length != mButtonDrawables.length) {
			return;
		}
		mButtonDrawables = drawables;
		refreshView();
	}

	/**
	 * 半选按钮 0 enabled halfchecked 1 unenabled halfchecked
	 */
	protected void setHalfCheckButtonDrawables(int[] drawables) {
		if (drawables == null || drawables.length != mHalfCheckDrawables.length) {
			return;
		}
		mHalfCheckDrawables = drawables;
		refreshView();
	}

	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed) {
			mImgButton.setBackgroundResource(mChecked ? mButtonDrawables[4] : mButtonDrawables[5]);
		} else {
			mImgButton.setBackgroundResource(mChecked ? mButtonDrawables[0] : mButtonDrawables[1]);
		}
	}

	@Override
	public void setOnCheckedChangedListener(OnCheckChangedListener listener) {
		mListener = listener;
	}

	public void setText(CharSequence txt) {
		mTxtContent.setText(txt);
	}

	public void setText(int resId) {
		mTxtContent.setText(resId);
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

	public void setTextColor(int color) {
		mTxtContent.setTextColor(color);
	}

	public void setTextColor(ColorStateList colors) {
		mTxtContent.setTextColor(colors);
	}

	public void setTextSize(int sp) {
		mTxtContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
	}

	@Override
	public void refreshDrawableState() {
		super.refreshDrawableState();
		refreshView();
	}

	protected void refreshView() {
		if (isEnabled()) {
			if (mHalfChecked) {
				mImgButton.setBackgroundResource(mHalfCheckDrawables[0]);
			} else {
				mImgButton.setBackgroundResource(mChecked ? mButtonDrawables[0] : mButtonDrawables[1]);
			}
		} else {
			if (mHalfChecked) {
				mImgButton.setBackgroundResource(mHalfCheckDrawables[1]);
			} else {
				mImgButton.setBackgroundResource(mChecked ? mButtonDrawables[2] : mButtonDrawables[3]);
			}
		}
	}

	public void setCheckedWithoutNotify(boolean checked) {
		mHalfChecked = false;
		if (mChecked == checked) {
			return;
		}
		mChecked = checked;
		refreshView();
	}

	public boolean isHalfChecked() {
		return mHalfChecked;
	}

	public void setHalfChecked(boolean checked) {
		mHalfChecked = checked;
		refreshView();
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		mHalfChecked = false;
		if (mChecked == checked) {
			return;
		}
		mChecked = checked;
		refreshView();
		if (mListener != null) {
			mListener.onCheckChanged(this, mChecked);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == this) {
			toggle();
		}
	}
}
