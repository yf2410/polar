package com.polar.browser.common.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.polar.browser.R;

/**
 * 兼容DialogActivity
 */
public class CommonDialogActivity extends Activity {
	/**
	 * the view id of the right button, with default text "确定", default color
	 * green
	 */
	public static final int ID_BTN_OK = R.id.common_btn_middle;
	/**
	 * the view id of the middle button, with default text "取消", default color
	 * brown
	 */
	public static final int ID_BTN_CANCEL = R.id.common_btn_left;
	/**
	 * the view id of the right button, with default text "默认", default color
	 * brown
	 */
	public static final int ID_BTN_DEFAULT = R.id.common_btn_right;
	public TextView mTitle;
	/**
	 * title icon, is set {@link GONE} by default
	 */
	public ImageView mTitleIcon;
	/**
	 * Dialog message
	 */
	public TextView mMsg;
	/**
	 * Dialog main contents view container, is a {@link LinearLayout}, use
	 * {@code mContents.addView(customView) to add custom views}
	 */
	public LinearLayout mContents;
	public LayoutInflater mInflater;
	public View mTitleBar;
	public Button mBtnOK;
	public Button mBtnCancel;
	/**
	 * the right button on bottom of dialog, is set {@link GONE} by default
	 */
	public Button mBtnDefault;
	/**
	 * Parent view of mBtnOK, mBtnCancel, mBtnDefault
	 */
	public CommonBottomBar1 mBtnsBar;
	private ImageButton mTitleBtnRight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.common_dialog);
		mInflater = getLayoutInflater();
		mTitle = (TextView) findViewById(R.id.common_txt_title);
		mTitleBar = findViewById(R.id.common_ll_title_bar);
		mMsg = (TextView) findViewById(R.id.common_txt_content);
		mTitleIcon = (ImageView) findViewById(R.id.common_img_title_left);
		mContents = (LinearLayout) findViewById(R.id.common_ll_content);
		mBtnOK = (Button) findViewById(ID_BTN_OK);
		mBtnCancel = (Button) findViewById(ID_BTN_CANCEL);
		mBtnDefault = (Button) findViewById(ID_BTN_DEFAULT);
		mBtnsBar = (CommonBottomBar1) findViewById(R.id.common_btn_bar);
		mTitleBtnRight = (ImageButton) findViewById(R.id.common_img_title_right);
		mTitleBtnRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public CommonBottomBar1 getButtonBar() {
		return mBtnsBar;
	}

	@Override
	public void setTitle(CharSequence text) {
		mTitle.setText(text);
	}

	@Override
	public void setTitle(int resId) {
		mTitle.setText(resId);
	}

	public void setMsg(CharSequence text) {
		mMsg.setText(text);
	}

	public void setMsg(CharSequence text, boolean singleLine) {
		mMsg.setSingleLine();
		mMsg.setEllipsize(TruncateAt.MIDDLE);
		mMsg.setText(text);
	}

	public void setMsg(int resId) {
		mMsg.setText(resId);
	}

	public void setButtonText(int buttonId, CharSequence text) {
		if (buttonId == ID_BTN_OK) {
			mBtnOK.setText(text);
		} else if (buttonId == ID_BTN_CANCEL) {
			mBtnCancel.setText(text);
		} else if (buttonId == ID_BTN_DEFAULT) {
			mBtnDefault.setText(text);
		}
	}

	/**
	 * @param buttonId ID_BTN_OK and ID_BTN_CANCEL
	 */
	public void setButtonText(int buttonId, int resId) {
		if (buttonId == ID_BTN_OK) {
			mBtnOK.setText(resId);
		} else if (buttonId == ID_BTN_CANCEL) {
			mBtnCancel.setText(resId);
		} else if (buttonId == ID_BTN_DEFAULT) {
			mBtnDefault.setText(resId);
		}
	}

	/**
	 * @param buttonId ID_BTN_OK and ID_BTN_CANCEL
	 */
	public void setButtonVisibility(int buttonId, boolean visible) {
		if (buttonId == ID_BTN_OK) {
			mBtnOK.setVisibility(visible ? View.VISIBLE : View.GONE);
		} else if (buttonId == ID_BTN_CANCEL) {
			mBtnCancel.setVisibility(visible ? View.VISIBLE : View.GONE);
		} else if (buttonId == ID_BTN_DEFAULT) {
			mBtnDefault.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	public void setButtonOnClickListener(int buttonId, View.OnClickListener listener) {
		if (buttonId == ID_BTN_OK) {
			mBtnOK.setOnClickListener(listener);
		} else if (buttonId == ID_BTN_CANCEL) {
			mBtnCancel.setOnClickListener(listener);
		} else if (buttonId == ID_BTN_DEFAULT) {
			mBtnDefault.setOnClickListener(listener);
		}
	}

	public void setPositiveButtonOnClickListener(View.OnClickListener listener) {
		mBtnOK.setOnClickListener(listener);
	}

	public void setNegativeButtonOnClickListener(View.OnClickListener listener) {
		mBtnCancel.setOnClickListener(listener);
	}

	public void setNeutralButtonOnClickListener(View.OnClickListener listener) {
		mBtnDefault.setOnClickListener(listener);
	}

	public void hideTitle() {
		mTitleBar.setVisibility(View.GONE);
	}

	public void hideMsgView() {
		mMsg.setVisibility(View.GONE);
	}

	public View addView(int layoutId) {
		View view = mInflater.inflate(layoutId, null);
		mContents.addView(view);
		return view;
	}

	public void addView(View view) {
		mContents.addView(view);
	}

	public ImageButton getTitleImgRight() {
		return mTitleBtnRight;
	}

	public View getRootView() {
		return findViewById(R.id.common_dialog_root);
	}
}
