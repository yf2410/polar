package com.polar.browser.download_refactor.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.DownloadManagerCheck;
import com.polar.browser.download_refactor.UiStatusDefine;
import com.polar.browser.download_refactor.handler.DownloadHandler;
import com.polar.browser.download_refactor.netstatus_manager.MoblieAllowDownloads;
import com.polar.browser.download_refactor.util.KSystemUtils;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.download_refactor.util.URLUtilities;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.OpenFileUtils;
import com.polar.browser.vclibrary.bean.NormalSwitchBean;
import com.polar.browser.view.ToastClickListener;

import java.io.File;

import static com.polar.browser.download_refactor.DownloadFileUtils.checkDownloadDirectoryCanWrite;

public class DownloadDialog extends Activity implements OnClickListener {
	private static final String GO_DOWNLOAD_APK_URL = "http://igodownload.com/apk/GODownloader.apk";
	private EditText mEtName;

	private TextView mTvSize;

	// 文件名后缀
	private String mSuffix;

	// 下载文件保存路径
	private String mCustomFolder;

	private View layout_title;
	private final String GO_DOWNLOAD_PACK_NAME = "com.go.downloads";
	private TextView tvGodownloadTip;
	private NormalSwitchBean godownloadFlag;
	private RelativeLayout download_dialog;

	private String mUserAgent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_download);
		initView();
		initData();
		initListener();
		if (TextUtils.equals("youtube.com",mUserAgent)) {
			Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_DIALOG_SHOW);
		}
	}

	private void initView() {
		download_dialog = (RelativeLayout) findViewById(R.id.download_dialog);
		mEtName = (EditText) findViewById(R.id.et_name);
		mTvSize = (TextView) findViewById(R.id.tv_size);

		layout_title = findViewById(R.id.layout_title);
		tvGodownloadTip = (TextView)findViewById(R.id.tv_godownload_tip);
		godownloadFlag = ConfigManager.getInstance().getGoDownloadFlag();
		tvGodownloadTip.setVisibility((godownloadFlag!=null&&godownloadFlag.isSwitchStatus()&&!goDownloadInstalled())?View.VISIBLE: View.GONE);
		tvGodownloadTip.setText(godownloadFlag==null?getString(R.string.download_with_godownloader_tip):godownloadFlag.getDes());
		if(godownloadFlag!=null){
			Glide.with(this).load(godownloadFlag.getIcon()).asBitmap().into(new SimpleTarget<Bitmap>() {
				@Override
				public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
					tvGodownloadTip.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(),bitmap),null,null,null);
				}
			});
//			Picasso.with(this).load(godownloadFlag.getIcon()).into(target);
		}
	}

//	Target target = new Target() {
//		@Override
//		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//			tvGodownloadTip.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(getResources(),bitmap),null,null,null);
//		}
//
//		@Override
//		public void onBitmapFailed(Drawable errorDrawable) {
//		}
//
//		@Override
//		public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//		}
//	};

	private void initData() {
		Intent intent = getIntent();
		if (intent != null) {
			final String filename = intent.getStringExtra("filename");
			mCustomFolder = intent.getStringExtra("customFolder");
			long contentLength = intent.getLongExtra("contentLength", 0);
			mUserAgent = intent.getStringExtra("userAgent");
			if (!TextUtils.isEmpty(filename)) {
				// TODO 传过来的消息
				mEtName.setText(filename);
				mEtName.post(new Runnable() {
					@Override
					public void run() {
						// handle cursor
						int dot = filename.lastIndexOf(".");
						if (dot > 0) {
							mEtName.setSelection(dot);
							mSuffix = filename.substring(dot);
						} else {
							mSuffix = "";
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
		if(tvGodownloadTip.getVisibility() == View.VISIBLE)
			layout_title.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_ok:
				if (TextUtils.equals("youtube.com",mUserAgent)) {
					Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_DWN_OK);
				}
				// 修改的文件名
				String newName = mEtName.getText().toString();
				if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newName.replace(" ", ""))) {
					CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
					return;
				}

				// 	处理文件名过长 待优化
				if (newName.length() > URLUtilities.FILENAME_MAX_LEN) {
					newName = newName.substring(0, URLUtilities.FILENAME_MAX_LEN - 1);
					mEtName.setText(newName);
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
				if (PathResolver.isDownloadFileExists(newName, mCustomFolder)) {
					CustomToastUtils.getInstance().showDurationToast(R.string.download_file_name_exists, 3000);
					return;
				}

				if (mSuffix.equalsIgnoreCase(".mp4") || mSuffix.equalsIgnoreCase(".avi")
						|| mSuffix.equalsIgnoreCase(".3gp") || mSuffix.equalsIgnoreCase(".mpg")
						|| mSuffix.equalsIgnoreCase(".mov") || mSuffix.equalsIgnoreCase(".swf")
						|| mSuffix.equalsIgnoreCase(".wmv") || mSuffix.equalsIgnoreCase(".flv")
						|| mSuffix.equalsIgnoreCase(".mkv") || mSuffix.equalsIgnoreCase(".rmvb")
						|| mSuffix.equalsIgnoreCase(".mpeg") || mSuffix.equalsIgnoreCase(".m4v")
						|| mSuffix.equalsIgnoreCase(".asf") || mSuffix.equalsIgnoreCase(".ac3")
						|| mSuffix.equalsIgnoreCase(".rm")) {	// 视频 mp4,avi,3gp,mpg,mov,swf,wmv,flv,mkv,rmvb mpeg,m4v,asf,ac3,rm
					Statistics.sendOnceStatistics(
							GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_VIDEO);
				} else if (mSuffix.equalsIgnoreCase(".mp3") || mSuffix.equalsIgnoreCase(".wav")
						|| mSuffix.equalsIgnoreCase(".aif") || mSuffix.equalsIgnoreCase(".au")
						|| mSuffix.equalsIgnoreCase(".ram") || mSuffix.equalsIgnoreCase(".wma")
						|| mSuffix.equalsIgnoreCase(".aac") || mSuffix.equalsIgnoreCase(".ogg")
						|| mSuffix.equalsIgnoreCase(".ape") || mSuffix.equalsIgnoreCase(".acg")
						|| mSuffix.equalsIgnoreCase(".aiff") || mSuffix.equalsIgnoreCase(".mid")
						|| mSuffix.equalsIgnoreCase(".ra")) {	// 音乐 wav,aif,au,mp3,ram,wma,aac,ogg,ape,acg,aiff,mid,ra
					Statistics.sendOnceStatistics(
							GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_MUSIC);
				} else if (mSuffix.equalsIgnoreCase(".bmp") || mSuffix.equalsIgnoreCase(".gif")
						|| mSuffix.equalsIgnoreCase(".jpg") || mSuffix.equalsIgnoreCase(".jpeg")
						|| mSuffix.equalsIgnoreCase(".pic") || mSuffix.equalsIgnoreCase(".png")
						|| mSuffix.equalsIgnoreCase(".tiff") || mSuffix.equalsIgnoreCase(".raw")
						|| mSuffix.equalsIgnoreCase(".svg") || mSuffix.equalsIgnoreCase(".ai")
						|| mSuffix.equalsIgnoreCase(".tga") || mSuffix.equalsIgnoreCase(".exif")
						|| mSuffix.equalsIgnoreCase(".fpx") || mSuffix.equalsIgnoreCase(".psd")
						|| mSuffix.equalsIgnoreCase(".cdr") || mSuffix.equalsIgnoreCase(".pcd")
						|| mSuffix.equalsIgnoreCase(".dxf") || mSuffix.equalsIgnoreCase(".ufo")
						|| mSuffix.equalsIgnoreCase(".eps") || mSuffix.equalsIgnoreCase(".hdri")) {
					// bmp,gif,jpg,jpeg,pic,png,tiff,raw,svg,ai,tga,exif,fpx,psd,cdr,pcd,dxf,ufo,eps,hdri
					Statistics.sendOnceStatistics(
							GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_PIC);
				} else if (mSuffix.equalsIgnoreCase(".zip") || mSuffix.equalsIgnoreCase(".rar")
						|| mSuffix.equalsIgnoreCase(".arj") || mSuffix.equalsIgnoreCase(".gz")
						|| mSuffix.equalsIgnoreCase(".z") || mSuffix.equalsIgnoreCase(".cab")
						|| mSuffix.equalsIgnoreCase(".7z") || mSuffix.equalsIgnoreCase(".iso")) {
					// 压缩文件 rar,zip,arj,gz,z,cab,7z,iso
					Statistics.sendOnceStatistics(
							GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_ZIP);
				} else if (mSuffix.equalsIgnoreCase(".apk")) {	// apk文件
					Statistics.sendOnceStatistics(
							GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_APK);
				} else {	//其他下载文件统计
					Statistics.sendOnceStatistics(
							GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_OTHER);
				}

				// 20160823 下载检测对象为空,导致接下来无法下载,需要用户再次尝试下载,重新初始化对象
				if (DownloadManagerCheck.getInstance() == null) {
					CustomToastUtils.getInstance().showTextToast(R.string.download_error);
					return;
				}

				DownloadManagerCheck.getInstance().setNeedConfirm(false);
				DownloadManagerCheck.getInstance().setFileName(newName);
				DownloadManagerCheck.getInstance().confirmDownload();

		/*		//DialogToastActivity为DialogToast临时依附的activity
				Intent intent = new Intent(DownloadDialog.this , DialogToastActivity.class);
				startActivity(intent);*/
				CustomToastUtils.getInstance().showClickActivityToast(
						getApplicationContext(),
						getString(R.string.download_start),
						getString(R.string.click_to_see),
						3000,
						ToastClickListener.EVENT_CLICK_DOWNLOAD,
						false
				);

				setHasDownloadStatus();
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_OK);
				break;
			case R.id.btn_cancel:
				// 取消
				if (DownloadManagerCheck.getInstance() != null) {
					DownloadManagerCheck.getInstance().destory();
				}
				finish();
				overridePendingTransition(0, android.R.anim.fade_out);
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_CANCEL);
				if (TextUtils.equals("youtube.com",mUserAgent)) {
					Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_DWN_CANCLE);
				}
				break;
			case R.id.layout_title:
				handleTitleClick();
				break;
			default:
				break;
		}
	}

	private static class CustomerClickListener implements  OnClickListener{

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(JuziApp.getInstance(), DownloadActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			JuziApp.getInstance().startActivity(intent);
		}
	}


	private void handleTitleClick() {
		if(goDownloadInstalled()){
			openGoDownload();
			sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_OPEN_GDDOWNLD);
		} else{
			sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_GD_DOWNLD);
			downloadGoDownload();
		}




	}

	private boolean goDownloadInstalled() {
		return KSystemUtils.isAPPInstalled(this,GO_DOWNLOAD_PACK_NAME);

	}
	public static final String KEY_CLIPBOARD_URL = "key_clipboard_url";
	private void openGoDownload() {
		Intent resolveIntent = getPackageManager().getLaunchIntentForPackage(GO_DOWNLOAD_PACK_NAME);
		resolveIntent.putExtra(KEY_CLIPBOARD_URL, DownloadManagerCheck.getInstance().getmUri());
		resolveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(resolveIntent);
	}


	/**
	 * 通知有下载任务,更新主页底部菜单状态
	 */
	private void setHasDownloadStatus() {
		Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
		intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, true);
		JuziApp.getInstance().sendBroadcast(intent);
	}

	/**
	 * 下载 我们的 GoDownloader
	 */
	private void downloadGoDownload() {//TODO
		DownloadItemInfo mInfo = DownloadManager.getInstance().getDownloadItem(godownloadFlag==null?GO_DOWNLOAD_APK_URL:godownloadFlag.getUrl());
		boolean isNeedRedownload = true;
		if(mInfo!=null){
			switch (mInfo.mStatus){
				case UiStatusDefine.STATUS_PAUSED:
					isNeedRedownload = false;
					if (mInfo.mReason == UiStatusDefine.PAUSED_WAITING_FOR_NETWORK ||
							mInfo.mReason == UiStatusDefine.PAUSED_WAITING_TO_RETRY ||
							mInfo.mReason == UiStatusDefine.PAUSED_QUEUED_FOR_WIFI) {
						DownloadManager.getInstance().pauseDownload(mInfo.mId);
						return;
					}
					if (!checkDownloadDirectoryCanWrite(mInfo)) {
						CustomToastUtils.getInstance().showTextToast(R.string.download_no_available_space);
						return;
					}
					//判断是否有网，没网 通知栏提示没网络 detail显示 net error return
					if (!NetworkUtils.isNetWorkConnected(this)) {
						DownloadManager.getInstance().resumeDownload(mInfo.mId);
						CustomToastUtils.getInstance().showTextToast(R.string.net_no_connect);
						return;
					}
					resumeDownloadFetchWifiStatusWhilePause(mInfo);
					break;
				case UiStatusDefine.STATUS_SUCCESSFUL:
					isNeedRedownload = false;
					File apkFile = new File(mInfo.mFilePath);
					if(apkFile.isFile()){
						OpenFileUtils.openFile(apkFile, this);
						sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_INSTALL_GDDOWNLD);
						finish();
						return;
					}else{
						if (apkFile.exists()) {
							apkFile.delete();
						}
						sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_GD_DOWNLD);
						DownloadManager.getInstance().restartDownload(mInfo.mId);
					}
					break;
				case UiStatusDefine.STATUS_RUNNING:
					isNeedRedownload = false;
					break;
			}
		}
		if(isNeedRedownload){
			DownloadHandler.getInstance().onDownloadStart(JuziApp.getInstance(), godownloadFlag==null?GO_DOWNLOAD_APK_URL:godownloadFlag.getUrl(), "",
					"","application/vnd.android", 0L, "", "", false,false);
		}

		boolean isWifiDownloadEnable = ConfigManager.getInstance().isEnableOnlyWifiDownload();
		Intent intent = new Intent(this, DownloadActivity.class);
		intent.putExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, isWifiDownloadEnable);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_right,
				R.anim.slide_out_to_left);
		finish();
	}

	public void resumeDownloadFetchWifiStatusWhilePause(final DownloadItemInfo info) {
		// 判断wifi网络 与 only download in wifi
		if (!NetworkUtils.isWifiConnected(this)) {
			// 2、有移动网络，没有wifi，判断 only download in wifi 是：提示移动网络下是否继续下载 否：直接下载
			if (DownloadManager.getInstance().isOnlyWifiDownload &&
					!MoblieAllowDownloads.getInstance().isAllowMoblieNetDownload(info.mUrl)) {// 是否only download in wifi
				final CommonDialog mCommonDialog = new CommonDialog(this, R.string.tips, R.string.net_changed_when_downloading);
				mCommonDialog.setBtnCancel(getString(R.string.cancel),
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								mCommonDialog.dismiss();
							}
						});
				mCommonDialog.setBtnOk(getString(R.string.download), new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCommonDialog.dismiss();
						ThreadManager.postTaskToNetworkHandler(new Runnable() {
							@Override
							public void run() {
//						mTask.start();
								DownloadManager.getInstance().resumeDownload(info.mId);
							}
						});
					}
				});


				mCommonDialog.show();
			} else {
				resumeDownloadFetchNotResumeWhilePause(info);
			}
		} else {
			resumeDownloadFetchNotResumeWhilePause(info);
		}
	}
	private void resumeDownloadFetchNotResumeWhilePause(final DownloadItemInfo info) {
		boolean disRsume = DownloadManager.getInstance().isContuningDownloadSupported(info.mId);// 支持断点续传继续下载，否则，继续
		if (disRsume) {
			DownloadManager.getInstance().resumeDownload(info.mId);
		} else {
//			mManagerView.showResumeDilogClickResumeWhilePause(info);
			// 不支持断点续传，重新下载
			DownloadManager.getInstance().restartDownload(info.mId);
		}
	}

	private void sendGoogleStatistics(String type) {
		Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, type);
	}

}
