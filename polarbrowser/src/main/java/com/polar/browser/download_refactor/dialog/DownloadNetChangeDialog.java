package com.polar.browser.download_refactor.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.polar.browser.R;
import com.polar.browser.common.ui.CommonDialogActivity;
import com.polar.browser.download_refactor.DownloadManagerCheck;
import com.polar.browser.utils.CustomToastUtils;


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
				// 20160823 下载检测对象为空,导致接下来无法下载,需要用户再次尝试下载,重新初始化对象
				if (DownloadManagerCheck.getInstance() == null) {
					CustomToastUtils.getInstance().showTextToast(R.string.download_error);
					return;
				}
				DownloadManagerCheck.getInstance().setMobileWork(true);
				DownloadManagerCheck.getInstance().confirmDownload();
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
			}
		});
		setNegativeButtonOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 取消
				if (DownloadManagerCheck.getInstance() != null) {
					DownloadManagerCheck.getInstance().destory();
				}
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
			}
		});
	}
}
