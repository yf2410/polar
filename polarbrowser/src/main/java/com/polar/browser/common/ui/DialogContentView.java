package com.polar.browser.common.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.ui.ICustomCheckBox.OnCheckChangedListener;

/**
 * 下载删除对话框，中间的View： checkBox + textView
 *
 * @author wenqiang
 */
public class DialogContentView extends RelativeLayout implements android.view.View.OnClickListener {

	private CommonCheckBox1 mCheckBox;
	private TextView mTvContent;

	private boolean isChecked;


	public DialogContentView(Context context) {
		this(context, null);
	}

	public DialogContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_dialog_content, this);
		mCheckBox = (CommonCheckBox1) findViewById(R.id.check_box);
		mTvContent = (TextView) findViewById(R.id.tv_content);
		mTvContent.setOnClickListener(this);
		isChecked = mCheckBox.isChecked();
		mCheckBox.setOnCheckedChangedListener(new OnCheckChangedListener() {
			@Override
			public void onCheckChanged(View v, boolean isChecked) {
				DialogContentView.this.isChecked = isChecked;
			}
		});
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setContentText(CharSequence txt) {
		mTvContent.setText(txt);
	}

	@Override
	public void onClick(View v) {
		isChecked = !isChecked;
		mCheckBox.setChecked(isChecked);
	}
}
