package com.polar.browser.download_refactor.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.download_refactor.Base64ImageDownloader;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;

import java.io.File;

public class DownloadBase64ImgDialog extends Activity implements OnClickListener {

	private static final int FILE_NAME_MAX_LENGTH = 50;

	private EditText mEtName;

	private TextView mTvSize;

	// 文件名后缀
	private String mSuffix;

	private String mFileName;
	private String mImgData;
	private String mMimeType;
	private String mReferer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_download);
		initView();
		initData();
		initListener();
	}

	private void initView() {
		mEtName = (EditText) findViewById(R.id.et_name);
		mTvSize = (TextView) findViewById(R.id.tv_size);
	}

	private void initData() {
		Intent intent = getIntent();
		if (intent != null) {
			mFileName = intent.getStringExtra("filename");
			mImgData = intent.getStringExtra("imgData");
			mMimeType = intent.getStringExtra("mimeType");
			mReferer = intent.getStringExtra("referer");
			long contentLength = intent.getLongExtra("contentLength", 0);
			if (!TextUtils.isEmpty(mFileName)) {
				// TODO 传过来的消息
				mEtName.setText(mFileName);
				mEtName.post(new Runnable() {
					@Override
					public void run() {
						// handle cursor
						int dot = mFileName.lastIndexOf(".");
						if (dot > 0) {
							mEtName.setSelection(dot);
							mSuffix = mFileName.substring(dot);
						}
					}
				});
				if (contentLength > 0) {
					mTvSize.setText(FileUtils.formatFileSize(contentLength));
				} else {
					mTvSize.setText(R.string.size_unknown);
				}
			}

		}
	}

	private void initListener() {
		findViewById(R.id.btn_ok).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_ok:
				// 修改的文件名
				String newName = mEtName.getText().toString();
				if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newName.replace(" ", ""))) {
					CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
					return;
				}

				// 	处理文件名过长 待优化
				if (newName.length() > FILE_NAME_MAX_LENGTH) {
					newName = newName.substring(0, FILE_NAME_MAX_LENGTH);
				}

				// 处理文件更改名称后无后缀
				if (!TextUtils.isEmpty(mSuffix) && !newName.endsWith(mSuffix)) {
					newName = newName + mSuffix;
				}

				// 	处理文件名为 ".xxx" 情况
				if (newName.lastIndexOf(".") == 0) {
					CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
					return;
				}

				// 判断是否有重名文件
				String imgDir = PathResolver.getDownloadFileDir(null);
				File temPathFile = new File(imgDir, newName);
				if (temPathFile != null && temPathFile.exists()) {
					CustomToastUtils.getInstance().showDurationToast(R.string.download_file_name_exists, 3000);
					return;
				}

				Base64ImageDownloader.generateBase64Image(mImgData, newName, mMimeType, mReferer);

				CustomToastUtils.getInstance().showClickToast(DownloadBase64ImgDialog.this , R.string.download_start, new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(JuziApp.getInstance(), DownloadActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						JuziApp.getInstance().startActivity(intent);
					}
				});
				//DialogToastActivity为DialogToast临时依附的activity
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
				break;
			case R.id.btn_cancel:
				// 取消
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
				break;
			default:
				break;
		}
	}
}
