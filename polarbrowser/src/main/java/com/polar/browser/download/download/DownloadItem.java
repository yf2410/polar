package com.polar.browser.download.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.ICustomCheckBox.OnCheckChangedListener;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.download_refactor.DownloadFileUtils;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.UiStatusDefine;
import com.polar.browser.download_refactor.netstatus_manager.MoblieAllowDownloads;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.OpenFileUtils;
import com.polar.browser.utils.SimpleLog;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class DownloadItem extends LinearLayout implements android.view.View.OnClickListener, android.view.View.OnLongClickListener {

	private CommonCheckBox1 mCheckBox;

	private TextView mTvFileName;
	private TextView mTvLeftTime;

	private ImageView mIvFile;
	private ImageView mIvStatus;

	private TextView mTvSpeed;
	private TextView mTvProgress;

	private TextView mTvFileSize;
	private TextView mTvDownloadTime;
	private TextView mTvSuccessFileName;

	private ProgressBar mPbProgress;

	private CommonDialog mCommonDialog;

	private DownloadItemInfo mInfo;

	private boolean mPaused;

	public DownloadItem(Context context) {
		this(context, null);
	}

	public DownloadItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		initListener();
		initDialog();
	}

	private void initView() {
		setOrientation(LinearLayout.VERTICAL);
		LayoutInflater.from(getContext()).inflate(R.layout.item_download, this);
		setBackgroundResource(R.drawable.common_list_row1);
		AbsListView.LayoutParams params = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 76));
		setLayoutParams(params);

		mCheckBox = (CommonCheckBox1) findViewById(R.id.check_box);
		mTvFileName = (TextView) findViewById(R.id.tv_file_name);
		mTvLeftTime = (TextView) findViewById(R.id.tv_left_time);
		mTvProgress = (TextView) findViewById(R.id.tv_progress);
		mTvSpeed = (TextView) findViewById(R.id.tv_speed);
		mPbProgress = (ProgressBar) findViewById(R.id.pb_progress);
		mIvFile = (ImageView) findViewById(R.id.iv_file);
		mIvStatus = (ImageView) findViewById(R.id.iv_status);
		mTvFileSize = (TextView) findViewById(R.id.tv_file_size);
		mTvDownloadTime = (TextView) findViewById(R.id.tv_download_time);
		mTvSuccessFileName = (TextView) findViewById(R.id.tv_success_file_name);
	}

	private void initListener() {
		setOnClickListener(this);
		setOnLongClickListener(this);
		mCheckBox.setOnCheckedChangedListener(new OnCheckChangedListener() {
			@Override
			public void onCheckChanged(View v, boolean isChecked) {
//				mTask.setChecked(isChecked);
				mInfo.isChecked = isChecked;
				SimpleLog.e("DownloadingItem", "isChecked == "+isChecked);

				if (getContext() instanceof DownloadActivity) {
					((DownloadActivity)getContext()).checkCheckAllButton();
				}

			}
		});
	}

	public void bind(DownloadItemInfo info) {
		mInfo = info;
		int status = info.mStatus;
		if (status == UiStatusDefine.STATUS_SUCCESSFUL) {
			// 下载完成
			mTvFileSize.setVisibility(View.VISIBLE);
			mTvDownloadTime.setVisibility(View.VISIBLE);
			mTvSuccessFileName.setVisibility(View.VISIBLE);
			mTvFileName.setVisibility(View.GONE);
			mPbProgress.setVisibility(View.GONE);
			mTvProgress.setVisibility(View.GONE);
			mTvSpeed.setVisibility(View.GONE);
			mTvLeftTime.setVisibility(View.GONE);
			mIvStatus.setVisibility(View.GONE);

//			mIvFile.setImageResource(getFileIconByFileName(mTask.getDownloadInfo().fileName));
			mIvFile.setImageResource(OpenFileUtils.getFileIconByFileName(mInfo.getFilename()));
			mTvSuccessFileName.setText(mInfo.getFilename());
			mTvFileSize.setText(FileUtils.formatFileSize(mInfo.mTotalBytes));
			mTvDownloadTime.setText(DateUtils.formatDate(mInfo.mDate));
		} else {
			// 正在下载
			mTvFileSize.setVisibility(View.GONE);
			mTvDownloadTime.setVisibility(View.GONE);
			mTvSuccessFileName.setVisibility(View.GONE);
			mTvFileName.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.VISIBLE);
			mTvProgress.setVisibility(View.VISIBLE);
			mTvSpeed.setVisibility(View.VISIBLE);
			mIvStatus.setVisibility(View.VISIBLE);

			String fileName = mInfo.getFilename();
			long currentBytes = mInfo.mCurrentBytes;
			if (currentBytes <= 0) {
				File file = new File(mInfo.mFilePath);
				if (file.exists()) {
					currentBytes = file.length();
				}
			}
			String progressStr = "";
			int progress = 0;
			if (mInfo.mTotalBytes <= 0) {
				progressStr = FileUtils.formatFileSize(currentBytes);
				progress = 0;
			} else {
				if (currentBytes < 0 ) {
					currentBytes = 0;
				}
				progressStr = FileUtils.formatFileSize(currentBytes) + "/" + FileUtils.formatFileSize(mInfo.mTotalBytes);
				progress = (int)((currentBytes*1.0)/(mInfo.mTotalBytes*1.0)*100);
			}
			mTvFileName.setText(fileName);
			mTvProgress.setText(progressStr);
			mIvFile.setImageResource(OpenFileUtils.getFileIconByFileName(mInfo.getFilename()));

			mPbProgress.setProgress(progress);
			refreshProgressStatus(progressStr);
			mTvProgress.setText(progressStr);

			if (status == UiStatusDefine.STATUS_PENDING) {
				mTvLeftTime.setVisibility(View.GONE);
				mPbProgress.setProgressDrawable(getResources().getDrawable(R.drawable.downloading_progress_bg));
				mTvSpeed.setText(R.string.download_status_waiting);
				mTvSpeed.setTextColor(getResources().getColor(R.color.set_about));
				mIvStatus.setVisibility(View.GONE);
			} else if (status == UiStatusDefine.STATUS_RUNNING) {
				mTvLeftTime.setVisibility(View.VISIBLE);
				mTvSpeed.setText("0KB/s");
				mTvSpeed.setTextColor(getResources().getColor(R.color.set_about));
				mPbProgress.setProgressDrawable(getResources().getDrawable(R.drawable.downloading_progress_bg));
				mIvStatus.setImageResource(R.drawable.download_pause);
			} else if (status == UiStatusDefine.STATUS_PAUSED) {
				mTvLeftTime.setVisibility(View.GONE);
				mTvSpeed.setText(R.string.download_status_pause);
				mTvSpeed.setTextColor(getResources().getColor(R.color.black54));
				mPbProgress.setProgressDrawable(getResources().getDrawable(R.drawable.download_progress_bg));
				mIvStatus.setImageResource(R.drawable.download_start);
			} else if (status == UiStatusDefine.STATUS_FAILED) {
				mTvLeftTime.setVisibility(View.GONE);
				mTvSpeed.setText(R.string.download_status_failure);
				mTvSpeed.setTextColor(getResources().getColor(R.color.failure_text));
				mPbProgress.setProgressDrawable(getResources().getDrawable(R.drawable.download_progress_bg));
//				mIvStatus.setVisibility(View.INVISIBLE);
				mIvStatus.setImageResource(R.drawable.download_failed);
			}
		}

		if (mInfo.isEditing) {
			mCheckBox.setVisibility(View.VISIBLE);
			if (mInfo.isChecked) {
				mCheckBox.setChecked(true);
			} else {
				mCheckBox.setChecked(false);
			}
		} else {
			mCheckBox.setVisibility(View.GONE);
		}
	}

	public void refreshDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes) {
		if (mInfo == null || id != mInfo.mId) {
			return;
		}
		String leftTimeStr = "";
		String progressStr = "";
		int progress = 0;
		if (totalBytes <= 0) {
			progressStr = FileUtils.formatFileSize(currentBytes);
			progress = 0;
		} else {
			if (currentBytes < 0 ) {
				currentBytes = 0;
			}
			progressStr = FileUtils.formatFileSize(currentBytes) + "/" + FileUtils.formatFileSize(mInfo.mTotalBytes);
			progress = (int)((currentBytes*1.0)/(mInfo.mTotalBytes*1.0)*100);
			long diff = totalBytes - currentBytes;
			if (diff > 0) {
				if (speedBytes > 0) {
					int diffSecond = (int)Math.ceil(diff * 1.0 / speedBytes);
					leftTimeStr = formatLeftTime(diffSecond);
				}
			}
		}
		final String fProgressStr = progressStr;
		final int fProgress = progress;
		final long fSpeed = speedBytes;
		final String fLeftTimeStr = leftTimeStr;
		ThreadManager.postTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				mTvProgress.setText(fProgressStr);
				mPbProgress.setProgress(fProgress);
				mPbProgress.setProgressDrawable(getResources().getDrawable(R.drawable.downloading_progress_bg));
				mTvSpeed.setText(FileUtils.formatFileSize(fSpeed) + "/s");
				mTvSpeed.setTextColor(getResources().getColor(R.color.set_about));
				mTvLeftTime.setText(fLeftTimeStr);
			}
		});
	}

	private String formatLeftTime(int totalSecond) {
		int hour = totalSecond / (60 * 60);
		int min = totalSecond / 60 % 60;
		int second = totalSecond % 60;
		if (hour == 0) {
			return String.format("%02d", min) + ":" + String.format("%02d", second);
		}
		return String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", second);
	}

	private void initDialog() {
		mCommonDialog = new CommonDialog(this.getContext(), R.string.tips, R.string.net_changed_when_downloading);
		mCommonDialog.setBtnCancel(getContext().getString(R.string.cancel),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCommonDialog.dismiss();
					}
				});
		mCommonDialog.setBtnOk(getContext().getString(R.string.download), new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
				mPaused = false;
				ThreadManager.postTaskToNetworkHandler(new Runnable() {
					@Override
					public void run() {
//						mTask.start();
						DownloadManager.getInstance().resumeDownload(mInfo.mId);
					}
				});
			}
		});
	}

	private void refreshProgressStatus(String progressStr) {

	}

	@Override
	public void onClick(View v) {
		Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, GoogleConfigDefine.DOWNLOAD_ITEM_CLICK);
		if (mInfo.isEditing) {
			if (mCheckBox.isChecked()) {
				mCheckBox.setChecked(false);
			} else {
				mCheckBox.setChecked(true);
			}
			EventBus.getDefault().post(new DownloadActivity.OnHidePopup());
			return;
		}

		if (CommonUtils.isFastDoubleClick()) {
			return;
		}

		boolean disRsume = DownloadManager.getInstance().isContuningDownloadSupported(mInfo.mId);
		switch (mInfo.mStatus) {
			case UiStatusDefine.STATUS_PENDING:
				if (disRsume) {// 判断是否支持断点续传
					DownloadManager.getInstance().pauseDownload(mInfo.mId);
				} else {
					// toast：不支持断点续传时，提示暂停恢复时需要重新下载+
//                    mManagerView.showResumeDilogClickPauseWhileWaitOrRunning(info);
					DownloadManager.getInstance().pauseDownload(mInfo.mId);
				}
				break;
			case UiStatusDefine.STATUS_RUNNING:
				if (mInfo.mReason == UiStatusDefine.RUN_DETAIL_PAUSING) {
					return;
				}
				if (disRsume) {//toast：不支持断点续传时，提示暂停恢复时需要重新下载
					DownloadManager.getInstance().pauseDownload(mInfo.mId);
				} else {
//                    mManagerView.showResumeDilogClickPauseWhileWaitOrRunning(mInfo);
					DownloadManager.getInstance().pauseDownload(mInfo.mId);
				}
				break;
			case UiStatusDefine.STATUS_PAUSED:
				if (mInfo.mReason == UiStatusDefine.PAUSED_WAITING_FOR_NETWORK ||
						mInfo.mReason == UiStatusDefine.PAUSED_WAITING_TO_RETRY ||
						mInfo.mReason == UiStatusDefine.PAUSED_QUEUED_FOR_WIFI) {
					DownloadManager.getInstance().pauseDownload(mInfo.mId);
					return;
				}
				if (!DownloadFileUtils.checkDownloadDirectoryCanWrite(mInfo)) {
					CustomToastUtils.getInstance().showTextToast(R.string.download_no_available_space);
					return;
				}
				//判断是否有网，没网 通知栏提示没网络 detail显示 net error return
				if (!NetWorkUtils.isNetworkConnected(getContext())) {
					DownloadManager.getInstance().resumeDownload(mInfo.mId);
					CustomToastUtils.getInstance().showTextToast(R.string.net_no_connect);
					return;
				}
                resumeDownloadFetchWifiStatusWhilePause(mInfo);
				break;
			case UiStatusDefine.STATUS_SUCCESSFUL:
				// 正常文件打开
//                NotifyManagerVirusOnCheck.checkVirusStatusFile(mInfo);
				OpenFileUtils.openFile(new File(mInfo.mFilePath), getContext());
				break;
			case UiStatusDefine.STATUS_FAILED:
                if (!DownloadFileUtils.checkDownloadDirectoryCanWrite(mInfo)) {
					CustomToastUtils.getInstance().showTextToast(R.string.download_no_available_space);
					return;
				}

                if (mInfo.mReason == UiStatusDefine.ERROR_CANNOT_RESUME) {
                    //判断手否有网，没网 通知栏提示没网络 detail显示 net error return
                    if (!NetWorkUtils.isNetworkConnected(getContext())) {
//                        mManagerView.showResumeDilogClickRestartWhileFailed(mInfo);
						CustomToastUtils.getInstance().showTextToast(R.string.net_no_connect);
                        return;
                    }
                    restartDownloadFetchWifiStatusWhileFailed(mInfo);
                    return;
                }
                //判断是否有网，没网 通知栏提示没网络 detail显示 net error return
                if (!NetWorkUtils.isNetworkConnected(getContext())) {
                  	DownloadManager.getInstance().resumeDownload(mInfo.mId);
                    return;
                }
                if (mInfo.mReason == UiStatusDefine.ERROR_URL_FAILURE) {
					CustomToastUtils.getInstance().showTextToast(R.string.download_url_error);
                    //下载链接失效，网站链接（refer）存在，访问referer
                    String referer = mInfo.mReferer;
                    if (!TextUtils.isEmpty(referer)) {
//                        mManagerView.showLoadRefererClickRestartWhileUrlInvalid(mInfo.mId, referer);
						// TODO 下载链接失效
                        return;
                    }
                }
                if (mInfo.mReason == UiStatusDefine.ERROR_FILE_ERROR) {
                    restartDownloadFetchWifiStatusWhileFailed(mInfo);
                    return;
                }
                resumeDownloadFetchWifiStatusWhilePause(mInfo);
				break;
			default:
				break;
		}
	}



	public void restartDownloadFetchWifiStatusWhileFailed(final DownloadItemInfo info) {
		if (!NetWorkUtils.isWifiConnected(getContext())) {
			// 移动网络
			// 2、有移动网络，没有wifi，判断 only download in wifi 是：1、提示移动网络下是否继续下载
			// 2、此任务被允许移动网络系在则直接下载 否：直接下载
			if (DownloadManager.getInstance().isOnlyWifiDownload &&
							!MoblieAllowDownloads.getInstance().isAllowMoblieNetDownload(info.mUrl)) {
				// 2、only download in wifi,在移动网络环境回复下载时，提示：非wifi
				// 环境，注意流量
				// （1）pause：新建一个暂停任务 （2）continue:新建一个开始下载的任务
				// （3）物理返回键：新建一个暂停任务
//				mManagerView.showOnlyWifiDilogClickRestartWhileFailed(info);
				//继续下载，移动网下载
				MoblieAllowDownloads.getInstance().addAllowMoblieNetDownload(info.mUrl);
//				showResumeDilogClickRestartWhileFailed(info);
				DownloadManager.getInstance().restartDownload(info.mId);
				//TODO:下载任务适配网络授权

			} else {
//				mManagerView.showResumeDilogClickRestartWhileFailed(info);
				DownloadManager.getInstance().restartDownload(info.mId);
			}
		} else {
//			mManagerView.showResumeDilogClickRestartWhileFailed(info);
			DownloadManager.getInstance().restartDownload(info.mId);
		}

	}

	public void resumeDownloadFetchNotResumeWhilePause(final DownloadItemInfo info) {
		boolean disRsume = DownloadManager.getInstance().isContuningDownloadSupported(info.mId);// 支持断点续传继续下载，否则，继续
		if (disRsume) {
			DownloadManager.getInstance().resumeDownload(info.mId);
		} else {
//			mManagerView.showResumeDilogClickResumeWhilePause(info);
			// 不支持断点续传，重新下载
			DownloadManager.getInstance().restartDownload(info.mId);
		}
	}

	public void resumeDownloadFetchWifiStatusWhilePause(final DownloadItemInfo info) {
		// 判断wifi网络 与 only download in wifi
		if (!NetWorkUtils.isWifiConnected(getContext())) {
			// 2、有移动网络，没有wifi，判断 only download in wifi 是：提示移动网络下是否继续下载 否：直接下载
			if (DownloadManager.getInstance().isOnlyWifiDownload &&
							!MoblieAllowDownloads.getInstance().isAllowMoblieNetDownload(info.mUrl)) {// 是否only download in wifi
				//是：判断是否继续
				// 2、only download in wifi,在移动网络环境回复下载时，提示：非wifi
				// 环境，注意流量
				// （1）pause：新建一个暂停任务 （2）continue:新建一个开始下载的任务
				// （3）物理返回键：新建一个暂停任务
//				mManagerView.showOnlyWifiDilogClickResumeWhilePause(info);

				// TODO 弹框，提示非wifi，是否下载
				if (mCommonDialog != null) {
					mCommonDialog.show();
				}
//				DownloadManager.getInstance().resumeDownload(info.mId);
			} else {
				resumeDownloadFetchNotResumeWhilePause(info);
			}
		} else {
			resumeDownloadFetchNotResumeWhilePause(info);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (!mInfo.isEditing && getContext() instanceof DownloadActivity) {
//			mTask.setChecked(true);
//			mInfo.isChecked = true;
			((DownloadActivity)getContext()).showLongClickMenu(mInfo);
			return true;
		}
		return false;
	}

}
