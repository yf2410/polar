package com.polar.browser.common.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;

public class CommonBottomBar3 extends RelativeLayout {

	private CommonCheckBox1 mCheckBox;
	private TextView mTvCheckAll;
	private TextView mTvDelete;
	private TextView mTvComplete;

	public CommonBottomBar3(Context context) {
		this(context, null);
	}

	public CommonBottomBar3(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.common_bottom_bar3, this);
		mCheckBox = (CommonCheckBox1) findViewById(R.id.common_check);
		mTvCheckAll = (TextView) findViewById(R.id.tv_check_all);
		mTvDelete = (TextView) findViewById(R.id.btn_delete);
		mCheckBox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTvCheckAll.performClick();
			}
		});
		mTvComplete = (TextView)findViewById(R.id.btn_complete);
	}

	public void setCheckAll(boolean isCheckAll) {
		if (isCheckAll) {
//			mTvCheckAll.setText(R.string.check_all_cancel);
		} else {
			mTvCheckAll.setText(R.string.check_all);
		}
		mCheckBox.setChecked(isCheckAll);
	}

	public TextView getCheckAllBtn() {
		return mTvCheckAll;
	}

	public TextView getDeleteBtn() {
		return mTvDelete;
	}

	public void setDeleteBtnEnabled(boolean enabled) {
		mTvDelete.setEnabled(enabled);
	}

	public TextView getTvComplete() {
		return mTvComplete;
	}
}
