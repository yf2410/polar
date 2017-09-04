package com.polar.browser.download;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonDialogActivity;

public class DownloadAlertDialog extends CommonDialogActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			String msg = intent.getStringExtra("message");
			if (!TextUtils.isEmpty(msg)) {
				setMsg(msg, true);
			}
			setTitle(R.string.download);
			setButtonVisibility(CommonDialogActivity.ID_BTN_CANCEL, false);
		}
		setPositiveButtonOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取消
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
			}
		});
	}
}
