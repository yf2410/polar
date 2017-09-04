package com.polar.browser.common.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.DensityUtil;

/**
 * dialog分成上中下三部分，都可以定制
 */
public class CommonDialog extends CommonBaseDialog {

	// 兼容老的DialogFactory-------------------------------------------<
	public static final int ID_BTN_OK = R.id.common_btn_middle;
	public static final int ID_BTN_CANCEL = R.id.common_btn_left;
	public static final int ID_BTN_DEFAULT = R.id.common_btn_right;
	private final View.OnClickListener mDefaultDismiss = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			dismiss();
		}
	};
	protected TextView mContentTxt;
	protected LinearLayout mLLContent;

	protected LinearLayout mLLBottom;

	protected CommonBottomBar2 mBtnBar;
	private TextView mTitleTxt;
	private ImageButton mTitleBtnLeft;
	private ImageButton mTitleBtnRight;
	private RelativeLayout mLLTitle;
	/**
	 * 提供给调用者分清是哪个dialog
	 */
	private int mId;

	public CommonDialog(Context context) {
		super(context, R.style.common_dialog);
		setContentView(R.layout.common_dialog);
		setCanceledOnTouchOutside(false);
		initView();
	}

	public CommonDialog(Context context, CharSequence title, CharSequence content) {
		this(context);
		mTitleTxt.setText(title);
		mContentTxt.setText(content);
	}

	public CommonDialog(Context context, int title, int content) {
		this(context);
		if (title != 0) {
			mTitleTxt.setText(context.getString(title));
		}
		if (content != 0) {
			mContentTxt.setText(context.getString(content));
		}
	}

	public int getId() {
		return mId;
	}
	// 中间的按钮

	public void setId(int id) {
		mId = id;
	}

	public CommonDialog setBtnOkText(int text) {
		return setBtnOkText(getContext().getString(text));
	}

	public CommonDialog setBtnOkText(CharSequence text) {
		mBtnBar.getButtonOK().setText(text);
		return this;
	}
	// 右边的按钮

	public CommonDialog setBtnOkListener(View.OnClickListener listener) {
		mBtnBar.getButtonOK().setOnClickListener(listener);
		return this;
	}

	public CommonDialog setBtnCancelText(int text) {
		return setBtnCancelText(getContext().getString(text));
	}

	public CommonDialog setBtnCancelText(CharSequence text) {
		mBtnBar.getButtonCancel().setText(text);
		return this;
	}

	public CommonDialog setBtnCancelListener(View.OnClickListener listener) {
		mBtnBar.getButtonCancel().setOnClickListener(listener);
		return this;
	}

	public CommonDialog setBtnOptionText(int text) {
		return setBtnOptionText(getContext().getString(text));
	}

	public CommonDialog setBtnOptionText(CharSequence text) {
		mBtnBar.getButtonOption().setVisibility(View.VISIBLE);
		mBtnBar.getButtonOption().setText(text);
		return this;
	}

	public CommonDialog setBtnRightListener(View.OnClickListener listener) {
		mBtnBar.getButtonOption().setVisibility(View.VISIBLE);
		mBtnBar.getButtonOption().setOnClickListener(listener);
		return this;
	}

	public CommonDialog setBtnOk(String txt, View.OnClickListener listener) {
		mBtnBar.getButtonOK().setText(txt);
		mBtnBar.getButtonOK().setOnClickListener(listener);
		return this;
	}

	public CommonDialog setBtnCancel(String txt, View.OnClickListener listener) {
		mBtnBar.getButtonCancel().setText(txt);
		mBtnBar.getButtonCancel().setOnClickListener(listener);
		return this;
	}

	public CommonDialog setBtnOption(String txt, View.OnClickListener listener) {
		mBtnBar.getButtonOption().setVisibility(View.VISIBLE);
		mBtnBar.getButtonOption().setText(txt);
		mBtnBar.getButtonOption().setOnClickListener(listener);
		return this;
	}

	public CommonBottomBar2 getBtnBar() {
		return mBtnBar;
	}

	public ImageButton getTitleImgLeft() {
		return mTitleBtnLeft;
	}

	public ImageButton getTitleImgRight() {
		return mTitleBtnRight;
	}

	@Override
	public void setTitle(CharSequence txt) {
		super.setTitle(txt);
		mTitleTxt.setText(txt);
	}

	@Override
	public void setTitle(int txt) {
		setTitle(getContext().getText(txt));
	}

	public void setContentAutoLinkMask(int mask) {
		mContentTxt.setAutoLinkMask(mask);
	}

	public CommonDialog setContentTxt(int txt) {
		return setContentTxt(getContext().getString(txt));
	}

	public CommonDialog setContentTxt(CharSequence txt) {
		mContentTxt.setText(txt);
		return this;
	}

	public void setButtonText(int buttonId, int resId) {
		if (buttonId == ID_BTN_OK) {
			mBtnBar.getButtonOK().setText(resId);
		} else if (buttonId == ID_BTN_CANCEL) {
			mBtnBar.getButtonCancel().setText(resId);
		} else if (buttonId == ID_BTN_DEFAULT) {
			mBtnBar.getButtonOption().setText(resId);
		}
	}

	public void setButtonVisibility(int buttonId, boolean visible) {
		if (buttonId == ID_BTN_OK) {
			mBtnBar.getButtonOK().setVisibility(visible ? View.VISIBLE : View.GONE);
		} else if (buttonId == ID_BTN_CANCEL) {
			mBtnBar.getButtonCancel().setVisibility(visible ? View.VISIBLE : View.GONE);
		} else if (buttonId == ID_BTN_DEFAULT) {
			mBtnBar.getButtonOption().setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	public void setButtonOnClickListener(int buttonId, View.OnClickListener listener) {
		if (buttonId == ID_BTN_OK) {
			mBtnBar.getButtonOK().setOnClickListener(listener);
		} else if (buttonId == ID_BTN_CANCEL) {
			mBtnBar.getButtonCancel().setOnClickListener(listener);
		} else if (buttonId == ID_BTN_DEFAULT) {
			mBtnBar.getButtonOption().setOnClickListener(listener);
		}
	}

	public void hideMsgView() {
		mContentTxt.setVisibility(View.GONE);
	}

	public View addView(int layoutId) {
		View view = getLayoutInflater().inflate(layoutId, null);
		mLLContent.addView(view);
		return view;
	}
	// 兼容老的DialogFactory------------------------------------------->

	public void addView(View view) {
		mLLContent.addView(view);
	}

	private void initView() {
		mTitleTxt = (TextView) findViewById(R.id.common_txt_title);
		mContentTxt = (TextView) findViewById(R.id.common_txt_content);
		mTitleBtnLeft = (ImageButton) findViewById(R.id.common_img_title_left);
		mTitleBtnRight = (ImageButton) findViewById(R.id.common_img_title_right);
		mLLTitle = (RelativeLayout) findViewById(R.id.common_ll_title_bar);
		mLLContent = (LinearLayout) findViewById(R.id.common_ll_content);
		mLLBottom = (LinearLayout) findViewById(R.id.common_ll_bottom);
		mBtnBar = (CommonBottomBar2) findViewById(R.id.common_btn_bar);
		mTitleBtnRight.setOnClickListener(mDefaultDismiss);
		mBtnBar.getButtonOK().setOnClickListener(mDefaultDismiss);
		mBtnBar.getButtonCancel().setOnClickListener(mDefaultDismiss);
		mBtnBar.getButtonOption().setOnClickListener(mDefaultDismiss);
	}

	private void setView(ViewGroup root, View v) {
		root.removeAllViews();
		root.addView(v);
	}

	private void setView(ViewGroup root, int layoutId) {
		root.removeAllViews();
		getLayoutInflater().inflate(layoutId, root);
	}

	public void hideTitle() {
		LayoutParams lp = (LayoutParams) mLLTitle.getLayoutParams();
		lp.height = DensityUtil.dip2px(getContext(), 20);
		mLLTitle.setLayoutParams(lp);
	}

	public void hideBottom() {
		mLLBottom.setVisibility(View.GONE);
	}

	public void setTitleView(int layoutId) {
		setView(mLLTitle, layoutId);
	}

	public void setCenterView(int layoutId) {
		setView(mLLContent, layoutId);
	}

	public void setBottomView(int layoutId) {
		setView(mLLBottom, layoutId);
	}

	public void setTitleView(View v) {
		setView(mLLTitle, v);
	}

	public void setCenterView(View v) {
		setView(mLLContent, v);
	}

	public void setBottomView(View v) {
		setView(mLLBottom, v);
	}

	/**
	 * 设置顶部区域，纯色背景加入图片
	 */
	public void setTopAreaImg(int color, int imgRes) {
		ViewGroup parent = (ViewGroup) findViewById(R.id.common_ll_content_parent);
		parent.removeViewAt(0);
		ImageView img = new ImageView(getContext());
		img.setImageResource(imgRes);
		img.setScaleType(ScaleType.CENTER);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 154));
		img.setLayoutParams(lp);
		lp.setMargins(0, 0, 0, DensityUtil.dip2px(getContext(), 10));
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		float rad = getContext().getResources().getDimension(R.dimen.common_dialog_bg_radius);
		gd.setCornerRadii(new float[]{
				rad, rad, rad, rad, 0, 0, 0, 0
		});
		img.setBackgroundDrawable(gd);
		parent.addView(img, 0);
	}
}
