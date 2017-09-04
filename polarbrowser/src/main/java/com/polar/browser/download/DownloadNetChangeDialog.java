package com.polar.browser.download;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonDialogActivity;

/**
 * 正在下载时，WIFI切换到3G，弹出继续下载确认框
 */
public class DownloadNetChangeDialog extends CommonDialogActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			setTitle(R.string.tips);
			setMsg(R.string.net_changed_when_downloading);
			setButtonText(CommonDialogActivity.ID_BTN_OK, R.string.download);
		}
		setPositiveButtonOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 继续下载
//				DownloadManager.getInstance().continueDownload();
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
			}
		});
		setNegativeButtonOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取消
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
			}
		});
	}
}
