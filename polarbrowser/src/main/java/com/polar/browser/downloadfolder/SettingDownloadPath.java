package com.polar.browser.downloadfolder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;

import java.io.File;

public class SettingDownloadPath extends LemonBaseActivity implements OnClickListener {

	private View mDestView1;
	private View mDestView2;

	private ImageView mIvDest1;
	private ImageView mIvDest2;

	private TextView mSpaceText1;
	private TextView mSpaceText2;

	private TextView mCurrentDest;

	private ProgressBar mProgressBar1;
	private ProgressBar mProgressBar2;

	/**
	 * 存储路径1根路径
	 **/
	private String mDest1Root;
	/**
	 * 存储路径2根路径
	 **/
	private String mDest2Root;

	private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_download_path);
		initView();
		initListener();
		initData();
		initReceiver();
	}

	private void initView() {
		mSpaceText1 = (TextView) findViewById(R.id.tv_space1);
		mSpaceText2 = (TextView) findViewById(R.id.tv_space2);
		mDestView1 = findViewById(R.id.downlaod_dest1);
		mDestView2 = findViewById(R.id.downlaod_dest2);
		mIvDest1 = (ImageView) findViewById(R.id.iv_dest1);
		mIvDest2 = (ImageView) findViewById(R.id.iv_dest2);
		mProgressBar1 = (ProgressBar) findViewById(R.id.progress1);
		mProgressBar2 = (ProgressBar) findViewById(R.id.progress2);
		mCurrentDest = (TextView) findViewById(R.id.current_dest);
	}

	private void initListener() {
		mDestView1.setOnClickListener(this);
		mDestView2.setOnClickListener(this);
		findViewById(R.id.common_img_back).setOnClickListener(this);
	}

	private void initData() {
		// TODO 存储卡1，存储卡2
		getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
		mDest1Root = VCStoragerManager.getInstance().getPhoneStorage();
		mDest2Root = VCStoragerManager.getInstance().getSDCardStorage();
		if (!TextUtils.isEmpty(mDest1Root)) {
			mDestView1.setVisibility(View.VISIBLE);
			File f = new File(mDest1Root);
			int total = (int) (f.getTotalSpace() / 1000 / 1000);
			int used = total - (int) (f.getUsableSpace() / 1000 / 1000);
			mSpaceText1.setText(getString(R.string.download_folder_space, FileUtils.formatFileSize(f.getUsableSpace()), FileUtils.formatFileSize(f.getTotalSpace())));
			mProgressBar1.setMax(total);
			mProgressBar1.setProgress(used);
		} else {
			mDestView1.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(mDest2Root)) {
			mDestView2.setVisibility(View.VISIBLE);
			File f = new File(mDest2Root);
			int total = (int) (f.getTotalSpace() / 1000 / 1000);
			int used = total - (int) (f.getUsableSpace() / 1000 / 1000);
			mSpaceText2.setText(getString(R.string.download_folder_space, FileUtils.formatFileSize(f.getUsableSpace()), FileUtils.formatFileSize(f.getTotalSpace())));
			mProgressBar2.setMax(total);
			mProgressBar2.setProgress(used);
		} else {
			mDestView2.setVisibility(View.GONE);
		}
		// 当前存储位置
		String currentFolder = ConfigWrapper.get(CommonData.KEY_DOWN_ROOT, VCStoragerManager.getInstance().getDefaultDownloadDirPath());
		updateCurrentDest(currentFolder);
	}

	private void initReceiver() {
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (TextUtils.equals(action, CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED)) {
					// 自定义了下载文件夹 改变
					SimpleLog.e("APP", "ACTION_DOWNLOAD_FOLDER_CHANGED");
					String currentFolder = intent.getStringExtra(CommonData.KEY_DOWN_ROOT);
					if (!TextUtils.isEmpty(currentFolder)) {
						if (mCurrentDest != null) {
							updateCurrentDest(currentFolder);
						}
					}
				}
			}
		};
		// 注册Receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED);
		registerReceiver(mReceiver, filter);
	}

	protected void updateCurrentDest(String currentFolder) {
		if (currentFolder == null) {
			return;
		}
		if (!TextUtils.isEmpty(mDest1Root) && currentFolder.startsWith(mDest1Root)) {
			String path = currentFolder.replace(mDest1Root, getString(R.string.download_folder_phone));
			mCurrentDest.setText(getString(R.string.current_download_folder, path));
			ConfigWrapper.put(CommonData.KEY_CURRENT_DOWN_FOLDER, path);
			ConfigWrapper.apply();
			mIvDest1.setImageResource(R.drawable.folder_checked);
			mIvDest2.setImageResource(R.drawable.folder_uncheck);
		}
		if (!TextUtils.isEmpty(mDest2Root) && currentFolder.startsWith(mDest2Root)) {
			String path = currentFolder.replace(mDest2Root, getString(R.string.download_folder_sd));
			mCurrentDest.setText(getString(R.string.current_download_folder, path));
			ConfigWrapper.put(CommonData.KEY_CURRENT_DOWN_FOLDER, path);
			ConfigWrapper.apply();
			mIvDest1.setImageResource(R.drawable.folder_uncheck);
			mIvDest2.setImageResource(R.drawable.folder_checked);
		}
		if (TextUtils.isEmpty(mDest2Root) && !TextUtils.isEmpty(mDest1Root)) {
			String path = ConfigWrapper.get(CommonData.KEY_CURRENT_DOWN_FOLDER, "");
			mCurrentDest.setText(getString(R.string.current_download_folder, path));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.downlaod_dest1:
				Intent intent = new Intent(this, SelectDownloadDir.class);
				intent.putExtra(CommonData.KEY_DOWN_ROOT, mDest1Root);
				intent.putExtra(CommonData.KEY_DOWN_TYPE, CommonData.DOWN_TYPE_PHONE);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				break;
			case R.id.downlaod_dest2:
				onDownlaodDest2Click();
				break;
			case R.id.common_img_back:
				onBackPressed();
				break;
			default:
				break;
		}
	}

	private void onDownlaodDest2Click() {
		// TODO SD卡可能有被卸载的情况
		String[] paths = VCStoragerManager.getInstance().getStorageDirectorys();
		if (paths == null || (paths != null && paths.length == 1)) {
			CustomToastUtils.getInstance().showTextToast(R.string.download_folder_sd_removed);
			return;
		}
		// TODO 判断如果4.4以上，不给跳转，指定死目录
		if (android.os.Build.VERSION.SDK_INT > 18) {
			showDialog();
			return;
		}
		Intent intent2 = new Intent(this, SelectDownloadDir.class);
		intent2.putExtra(CommonData.KEY_DOWN_ROOT, mDest2Root);
		intent2.putExtra(CommonData.KEY_DOWN_TYPE, CommonData.DOWN_TYPE_SD_CARD);
		startActivity(intent2);
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	/**
	 * 显示提示框, 4.4以上版本SD卡只支持读写自己的目录
	 */
	private void showDialog() {
		// 需要重新下载，弹提示框
		final CommonDialog mCommonDialog;
		mCommonDialog = new CommonDialog(this, getString(R.string.tips), getString(R.string.download_folder_alert_19));
		mCommonDialog.setBtnCancel(getString(R.string.cancel), new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
			}
		});
		mCommonDialog.setBtnOk(getString(R.string.ok), new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
				onSDCardClicked();
			}
		});
		mCommonDialog.show();
	}

	/**
	 * 弹框，点击确定
	 */
	private void onSDCardClicked() {
		// TODO SD卡可能有被卸载的情况
		String[] paths = VCStoragerManager.getInstance().getStorageDirectorys();
		if (paths == null || (paths != null && paths.length == 1)) {
			CustomToastUtils.getInstance().showTextToast(R.string.download_folder_sd_removed);
			return;
		}
		// TODO 下载路径更改为浏览器自己的目录
		String path = "";
		File fileDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
		if (fileDir != null && fileDir.exists()) {
			path = fileDir.getAbsolutePath();
			path = path.replace(mDest1Root, mDest2Root);
		} else {
			String pName = getPackageName();
			path = mDest2Root + java.io.File.separator + "Android/data"
					+ java.io.File.separator + pName + java.io.File.separator
					+ "files" + java.io.File.separator + "Download";
		}

		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		// 下载目录不存在的话，创建目录
		File f = new File(path);
		if (!f.exists()) {
			try {
				f.mkdirs();
			} catch (Exception e) {
				SimpleLog.e(e);
				CustomToastUtils.getInstance().showTextToast(R.string.add_shortcut_failed);
				return;
			}
		}
		// 保存选择的路径
		ConfigWrapper.put(CommonData.KEY_DOWN_ROOT, path);
		ConfigWrapper.apply();
		// 发广播通知
		Intent intent = new Intent(CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED);
		intent.putExtra(CommonData.KEY_DOWN_ROOT, path);
		JuziApp.getInstance().sendBroadcast(intent);
		// toast~
		CustomToastUtils.getInstance().showTextToast(R.string.download_folder_selected_sd);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onDestroy() {
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}
}
