package com.polar.browser.download;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.CommonTabViewPager;
import com.polar.browser.common.ui.DialogContentView;
import com.polar.browser.common.ui.DownloadTitleBar;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.database.MediaDBProvider;
import com.polar.browser.download.download.DownloadMoreClickView;
import com.polar.browser.download.download.DownloadView;
import com.polar.browser.download.download.FileClassifyView;
import com.polar.browser.download.download.OperateView;
import com.polar.browser.download.savedpage.SavedPageActivity;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.UiStatusDefine;
import com.polar.browser.download_refactor.db.DownloadProvider;
import com.polar.browser.download_refactor.dinterface.IDownloadUIDelegate;
import com.polar.browser.download_refactor.handler.DownloadHandler;
import com.polar.browser.download_refactor.netstatus_manager.MoblieAllowDownloads;
import com.polar.browser.download_refactor.util.KSystemUtils;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.i.IDownloadViewOperateDelegate;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.setting.SettingDownloadActivity;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.OpenFileUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SDCardUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.NormalSwitchBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static com.polar.browser.common.data.ConfigDefine.FILE_COUNT_CHANGED;
import static com.polar.browser.common.data.GoogleConfigDefine.DOWNLOAD_EDIT_COMPLETE;
import static com.polar.browser.common.data.GoogleConfigDefine.DOWNLOAD_EDIT_DELETE;
import static com.polar.browser.common.data.GoogleConfigDefine.DOWNLOAD_EDIT_MORE;
import static com.polar.browser.common.data.GoogleConfigDefine.DOWNLOAD_RENAME;
import static com.polar.browser.common.data.GoogleConfigDefine.DOWNLOAD_TASK_EDIT;
import static com.polar.browser.download_refactor.Constants.TYPE_WEB_PAGE;
import static com.polar.browser.download_refactor.DownloadFileUtils.checkDownloadDirectoryCanWrite;
import static com.polar.browser.utils.QueryUtils.queryCountByFileType;

public class DownloadActivity extends LemonBaseActivity implements OnClickListener, IDownloadUIDelegate, IDownloadViewOperateDelegate, IConfigObserver {

	public static final int TAB_DOWNLOAD = 0;
	private TextView mOfflineWeb;
	private TextView mOfflineSize;
	private View mLineSavedPage;

	/**
	 * 下载列表
	 **/
	private DownloadView mDownloadView;

	/**
	 * 右上角更多按钮
	 **/
	private ImageView mImgMore;

	private String type;

	/**
	 * titleBar
	 **/
	//private DownloadTitleBar mTitleBar;

	/**
	 * 显示可用空间and已用空间view
	 **/
	private View mDiskSpaceView;
	/**
	 * 可用空间
	 **/
	private TextView mTvFreeSpace;
	/**
	 * 已用空间
	 **/
	private TextView mTvUsedSpace;

	private View mUsed;
	private View mFree;

	/**
	 * 点击删除，弹对话框
	 */
	private DialogContentView dialogContentView;

	/**
	 * 底部操作按钮 (两个状态)
	 **/
	private OperateView mViewOperate;
	private FileClassifyView mFileClassifyView;
	private CommonTabViewPager mViewPager;
	private int mPageSelected;
	private QueryHandler queryHandler;
	private ArrayList<String> changedFileTypes;
	private DownloadMoreClickView downloadMoreClickView;

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.CustomTheme);
		setContentView(R.layout.activity_download2);
		initIntentData();
		initView();
		initData();
		initListener();
	}

	private void initIntentData() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD)) {
			DownloadManager.getInstance().isOnlyWifiDownload = intent.getBooleanExtra(CommonData.KEY_ONLY_WIFI_DOWNLOAD, true);
			SimpleLog.e("DownloadActivity", "DownloadActivity -- isOnlyWifiDownload --- " + DownloadManager.getInstance().isOnlyWifiDownload);
		}
	}

	private void initView() {
		//mTitleBar = (DownloadTitleBar) findViewById(R.id.title_bar);
		NormalSwitchBean godownloadFlag = ConfigManager.getInstance().getGoDownloadFlag();
		//mTitleBar.setDownloadImgVisibile(godownloadFlag != null && godownloadFlag.isSwitchStatus() && !goDownloadInstalled());
//		mTitleBar.setSettingImg(R.drawable.icon_setting);

//		mImgMore = (ImageView) mTitleBar.findViewById(R.id.common_img_setting);
//		mDownloadView = (DownloadView) findViewById(R.id.download_view);
		mDiskSpaceView = findViewById(R.id.rl_disk_space);
		mTvFreeSpace = (TextView) findViewById(R.id.tv_free_space);
		mTvUsedSpace = (TextView) findViewById(R.id.tv_used_space);
		mUsed = findViewById(R.id.view_used);
		mFree = findViewById(R.id.view_free);
		mOfflineWeb = (TextView) findViewById(R.id.offline_web);
		mOfflineSize = (TextView) findViewById(R.id.offline_web_size);
		mLineSavedPage = findViewById(R.id.line_saved_archive);
		mViewOperate = (OperateView) findViewById(R.id.rl_operate);

		initTabviewPager();
	}

	private void initTabviewPager() {
		mDownloadView = new DownloadView(this);
		mFileClassifyView = new FileClassifyView(this);
		List<View> listView = new ArrayList<>();
		listView.add(mDownloadView);
		ScrollView scrollView = new ScrollView(this);
		scrollView.setBackgroundColor(getResources().getColor(R.color.common_bg_color_5));
		scrollView.addView(mFileClassifyView);
		listView.add(scrollView);

		mViewPager = (CommonTabViewPager) findViewById(R.id.view_pager);
		mViewPager.setStyle(CommonTabViewPager.STYLE_GREY);
		mViewPager.setPageViews(listView);
		mViewPager.setTitles(Arrays.asList(getString(R.string.download),
				getString(R.string.bookmark_file)));

		mViewPager.setOnPageChangedListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int item) {
				mPageSelected = item;
				updateEditButtonState();
				// 退出编辑状态
				exitEditingMode();
				mViewOperate.initAllStart(mPageSelected);//TODO
			}

			// arg0:当前页面，及你点击滑动的页面
			// arg1:当前页面偏移的百分比
			// arg2:当前页面偏移的像素位置
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			// arg0 ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});

	}

	private void initData() {
		EventBus.getDefault().register(this);
		changedFileTypes = getIntent().getStringArrayListExtra(CommonData.CHANGED_FILE_LIST);
		if (changedFileTypes == null) changedFileTypes = new ArrayList<>();
		queryHandler = new QueryHandler(this, new SoftReference<>(this));
		if (isNeedReQuery())
			queryCount();
		else
			mFileClassifyView.refreshUi();
		mDownloadView.initData();
		mDownloadView.setDownloadUIDelegate(this);

		// 初始化离线网页数据 //TODO
//		updateOfflineWeb();
		// 初始化底部显示存储大小的view
		initStorageView();
		// 进入下载页面后,重置下载指示状态(消除小红点)
		resetDownloadMark();

		ConfigManager.getInstance().registerObserver(this);

	}

	private void resetDownloadMark() {
		Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
		intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, false);
		JuziApp.getInstance().sendBroadcast(intent);
	}

	private void initStorageView() {
		ThreadManager.postTaskToIOHandler(new Runnable() {
			@Override
			public void run() {
				final long freeSize = SDCardUtils.getSDFreeSize();
				final long allSize = SDCardUtils.getSDAllSize();
				ThreadManager.postTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						mTvFreeSpace.setText(getString(R.string.disk_free_size, FileUtils.formatFileSize(freeSize)));
						mTvUsedSpace.setText(getString(R.string.disk_used_size, FileUtils.formatFileSize(allSize - freeSize)));
						LinearLayout.LayoutParams freeParams = (LayoutParams) mFree.getLayoutParams();
						freeParams.weight = freeSize;
						mFree.setLayoutParams(freeParams);
						LinearLayout.LayoutParams usedParams = (LayoutParams) mUsed.getLayoutParams();
						usedParams.weight = allSize - freeSize;
						mUsed.setLayoutParams(usedParams);
					}
				});

			}
		});

	}

	private void initListener() {
//		mImgMore.setClickListener(this);
		//mTitleBar.setClickListener(this);
		mLineSavedPage.setOnClickListener(this);
		mViewOperate.init(this);
		mViewOperate.getCheckAllView().setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// 20160824下载Activity退出后,移除监听,防止内存泄露
		if (mDownloadView != null) {
			mDownloadView.destroy();
		}

		ConfigManager.getInstance().unregisterObserver(this);
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	private final String GO_DOWNLOAD_PACK_NAME = "com.go.downloads";

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.imageview_setting:    // 右上角设置按钮
				startActivity(new Intent(this, SettingDownloadActivity.class));
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				type = GoogleConfigDefine.DOWNLOAD_MANAGER_POP_SETTING;
//				if(mViewOperate.isEditing()){
//					exitEditingMode();
//				}else{
//					intoEditingMode();
//				}
				break;
			case R.id.btn_delete:        // 删除
				onDeleteButtonClick();
				break;
			case R.id.line_saved_archive:    // 离线网页
				startActivity(new Intent(this, SavedPageActivity.class));
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				type = GoogleConfigDefine.DOWNLOAD_MANAGER_SAVED_ARCHIVE;
				break;
			case R.id.imageview_open_goDownloader://打开godownload
				if (goDownloadInstalled()) {
					type = GoogleConfigDefine.DOWNLOAD_OPEN_GDDOWNLD;
					openGoDownload();
				} else {
					type = GoogleConfigDefine.DOWNLOAD_GD_DOWNLD;
					downloadGoDownload();
				}
				break;
			case R.id.tv_check_all: //全选
				boolean isCheckedAll = mDownloadView.isCheckAllState();
				mDownloadView.checkAll(!isCheckedAll);
				mViewOperate.setCheckedAll(!isCheckedAll);
				if (downloadMoreClickView != null)
					downloadMoreClickView.setIsShowing(false);
				break;
			default:
				break;
		}
		sendGoogleStatistics(type);
	}

	private void openGoDownload() {
		Intent resolveIntent = getPackageManager().getLaunchIntentForPackage(GO_DOWNLOAD_PACK_NAME);
		if (resolveIntent == null) return;
		resolveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(resolveIntent);
	}


	private boolean goDownloadInstalled() {
		return KSystemUtils.isAPPInstalled(this, GO_DOWNLOAD_PACK_NAME);
	}

	private static final String GO_DOWNLOAD_APK_URL = "http://igodownload.com/apk/GODownloader.apk";

	/**
	 * 下载 我们的 GoDownloader
	 */
	private void downloadGoDownload() {//TODO
		DownloadItemInfo mInfo = DownloadManager.getInstance().getDownloadItem(GO_DOWNLOAD_APK_URL);
		boolean isNeedRedownload = true;
		if (mInfo != null) {
			switch (mInfo.mStatus) {
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
					if (apkFile.isFile()) {
						type = GoogleConfigDefine.DOWNLOAD_INSTALL_GDDOWNLD;
						OpenFileUtils.openFile(apkFile, this);
					} else {
						if (apkFile.exists()) {
							apkFile.delete();
						}
						type = GoogleConfigDefine.DOWNLOAD_GD_DOWNLD;
						DownloadManager.getInstance().restartDownload(mInfo.mId);

					}
					break;
				case UiStatusDefine.STATUS_RUNNING:
					isNeedRedownload = false;
					break;
			}
		}
		if (isNeedRedownload) {
			DownloadHandler.getInstance().onDownloadStart(JuziApp.getInstance(), GO_DOWNLOAD_APK_URL, "",
					"", "application/vnd.android", 0L, "", "", false, false);
//			mTitleBar.setEditBtnEnabled(true);
		}

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

	@Override
	public void onBackPressed() {
		if (mViewOperate.isEditing()) {
			exitEditingMode();
			if (downloadMoreClickView != null)
				downloadMoreClickView.setIsShowing(false);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void finish() {
		saveDownloadState();
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
	}

	/**
	 * 是否有正在下载or是否查看过，(保存状态)
	 */
	private void saveDownloadState() {
//		if (mDownloadView.hasDownloadingTask()) {
//			Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
//			intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, true);
//			JuziApp.getInstance().sendBroadcast(intent);
//		} else {
//			Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
//			intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, false);
//			JuziApp.getInstance().sendBroadcast(intent);
//		}
	}

	/**
	 * 退出编辑模式
	 */
	public void exitEditingMode() {
		if (downloadMoreClickView != null)
			downloadMoreClickView.setIsShowing(false);

		// 取消选中项的状态
		mDownloadView.checkAll(false);
		mDownloadView.change2editState(false);
		mViewOperate.exitEditingMode();
		checkCheckAllButton();
	}

	/**
	 * 进入编辑模式
	 */
	public void intoEditingMode() {
		Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, DOWNLOAD_TASK_EDIT);
		// 刚进入编辑模式，删除按钮处于不可用状态
		mDownloadView.change2editState(true);
		mViewOperate.intoEditingMode();
		checkCheckAllButton();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ThreadManager.getIOHandler().post(new Runnable() {
			@Override
			public void run() {
				updateOfflineWeb();
				changedFileTypes = ConfigManager.getInstance().getChangedFileTypes();
				checkChangedFile();
			}
		});

	}

	private void checkChangedFile() {
		for (String changedFileType : changedFileTypes) {
			QueryUtils.queryCountByFileType(changedFileType);
		}

		ThreadManager.getUIHandler().post(new Runnable() {
			@Override
			public void run() {
				mFileClassifyView.refreshUi(changedFileTypes);
			}
		});
		ConfigManager.getInstance().setAllFileTypeUnChanged();
	}

	private void onDeleteButtonClick() {
		final boolean isDelAll = mDownloadView.getCheckItemCount() == mDownloadView.getDownloadList().size();
		int checkedCount = 0;
		checkedCount = mDownloadView.getCheckItemCount();
		final CommonDialog dialog = new CommonDialog(this, "", getString(R.string.delete_checked_task, checkedCount));
		dialog.hideTitle();
		dialogContentView = new DialogContentView(this);
		dialog.addView(dialogContentView);
		dialog.setBtnCancel(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(View v) {
				Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, isDelAll ? GoogleConfigDefine.DOWNLOAD_EMPTY_LIST_CANCEL :
						GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_CANCEL);
				dialog.dismiss();
			}
		});
		dialog.setBtnOk(getString(R.string.delete), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER, isDelAll ? GoogleConfigDefine.DOWNLOAD_EMPTY_LIST_CONFIRM :
						GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_CONFIRM);
				onConfirmDelete();
			}
		});
		dialog.show();
	}

	/**
	 * 点击确定删除
	 */
	private void onConfirmDelete() {
		boolean isDeleteFile = false;
		if (null != dialogContentView) {
			isDeleteFile = dialogContentView.isChecked();
		}
		SimpleLog.e("DownloadActivity", "isDeleteFile = " + isDeleteFile);
		//根据isDeleteFile 判断是否删除文件
		mDownloadView.deleteChecked(isDeleteFile);
		// 刷新列表
		JuziApp.getInstance().sendBroadcast(new Intent(CommonData.DOWNLOAD_UPDATE_LIST));
		// 退出编辑状态
		exitEditingMode();
	}

	/**
	 * 点击确定删除
	 */
	private void onConfirmLongClickDelete(DownloadItemInfo downloadItemInfo) {
		boolean isDeleteFile = false;
		if (null != dialogContentView) {
			isDeleteFile = dialogContentView.isChecked();
		}
		//根据isDeleteFile 判断是否删除文件
		mDownloadView.deleteLongClick(downloadItemInfo, isDeleteFile);
		// 刷新列表
		JuziApp.getInstance().sendBroadcast(new Intent(CommonData.DOWNLOAD_UPDATE_LIST));
		// 退出编辑状态
		exitEditingMode();
	}

	/**
	 * 长按某一Item进行删除
	 *
	 * @param data
	 */
	protected void showLongClickDeleteDialog(final DownloadItemInfo data) {

		int checkedCount = 1;  //长按只能选中一个
		final CommonDialog dialog = new CommonDialog(this, "", getString(R.string.delete_checked_task, checkedCount));
		dialog.hideTitle();
		dialogContentView = new DialogContentView(this);
		dialog.addView(dialogContentView);
		dialog.setBtnCancel(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(View v) {
				Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,
						GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_CANCEL);
				dialog.dismiss();
			}
		});
		dialog.setBtnOk(getString(R.string.delete), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,
						GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_CONFIRM);
				onConfirmLongClickDelete(data);
			}
		});
		dialog.show();
	}

	/**
	 * 点击清空按钮
	 */
	private void onClearButtonClick() {
		final CommonDialog dialog = new CommonDialog(this, "", getString(R.string.clear_all_2));
		dialog.hideTitle();
		dialogContentView = new DialogContentView(this);
		dialog.addView(dialogContentView);
		dialog.setBtnCancel(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setBtnOk(getString(R.string.delete), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				onConfirmClear();
			}
		});
		dialog.show();
	}

	/**
	 * 点击确认清空
	 */
	private void onConfirmClear() {
		boolean isDeleteFile = false;
		if (null != dialogContentView) {
			isDeleteFile = dialogContentView.isChecked();
		}
		SimpleLog.e("DownloadActivity", "isDeleteFile = " + isDeleteFile);
		//根据isDeleteFile 判断是否删除文件
		mDownloadView.deleteAll(isDeleteFile);
		// 刷新列表
		JuziApp.getInstance().sendBroadcast(new Intent(CommonData.DOWNLOAD_UPDATE_LIST));
		// 退出编辑状态
		exitEditingMode();
	}

	/**
	 * 点击每个checkBox后，刷新底部操作按钮
	 */
	public void checkCheckAllButton() {
		// 正在下载页面
		int checkedCount = mDownloadView.getCheckItemCount();
		mViewOperate.notifyEnabled(checkedCount);
	}

	/**
	 * 检查并设置编辑按钮状态（可用or不可用）
	 */
	public void updateEditButtonState() {
//		mTitleBar.setEditBtnEnabled(mPageSelected == DownloadActivity.TAB_DOWNLOAD&&mDownloadView.hasDownloadItem());
		if (!mDownloadView.hasDownloadItem()) {
//			mBtnEdit.setEnabled(false);
			exitEditingMode();
			mViewOperate.updateEditEnabled(false);
		} else {
//			mBtnEdit.setEnabled(true);
			mViewOperate.updateEditEnabled(true);
		}
	}

	private void updateOfflineWeb() {
//		File file = new File(getFilesDir() + SavedPageUtil.SAVED_FOLDER);
//		if (file.exists() && file.isDirectory()) {
//			mOfflineWeb.setText(getString(R.string.offline_web, String.valueOf(file.listFiles().length / 2)));
//			File[] childFiles = file.listFiles();
//			long size = 0L;
//			for (File childFile : childFiles) {
//				if (!childFile.getAbsolutePath().endsWith(SavedPageUtil.DESC_SUFFIX)) {
//					size += childFile.length();
//				}
//			}
//			if (size == 0L) {
//				mOfflineWeb.setText(getString(R.string.offline_web,String.valueOf(0)));
//				mOfflineSize.setText("0K");
//			} else {
//				String result = Formatter.formatFileSize(this, size);
//				mOfflineSize.setText(result);
//			}
//		} else {
//			mOfflineWeb.setText(getString(R.string.offline_web, String.valueOf(0)));
//			mOfflineSize.setText("0K");
//		}
		queryCountByFileType(TYPE_WEB_PAGE);
		mFileClassifyView.refreshUi(TYPE_WEB_PAGE);
	}

	/**
	 * 点击分享
	 */
	private void onShareButtonClick() {
		List<DownloadItemInfo> list = mDownloadView.getCheckedItemList();
		if (!list.isEmpty()) {
			String url = list.get(0).mUrl;
			// TODO 吊起分享
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, getFormatedShareContent(url));
			startActivity(Intent.createChooser(intent, getString(R.string.share_download_url)));
		}
	}

	/**
	 * 长按list item 弹出对话框分享点击事件
	 */
	private void longClickShare(DownloadItemInfo info) {
		if (info != null) {
			String url = info.mUrl;
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, getFormatedShareContent(url));
			startActivity(Intent.createChooser(intent, getString(R.string.share_download_url)));
		}
	}

	private String getFormatedShareContent(String url) {
		if (!TextUtils.isEmpty(url)) {
			return url + " " + getString(R.string.download_share_from);
		}
		return getString(R.string.download_share_from);
	}

	/**
	 * 点击更多
	 */
	private void onMoreButtonClick() {
		List<DownloadItemInfo> list = mDownloadView.getCheckedItemList();
		if (!list.isEmpty()) {
			if (list.size() == 1) {
				// 选择了一条
//				showMoreDialogSingle(list.get(0));
				if (downloadMoreClickView == null)
					downloadMoreClickView = new DownloadMoreClickView(this, list.get(0), mViewOperate.getViewMore());
				downloadMoreClickView.setMulti(false);
				downloadMoreClickView.showMenu();
			} else {
				// 选择了多条
//				showMoreDialogMulti(list);
				if (downloadMoreClickView == null)
					downloadMoreClickView = new DownloadMoreClickView(this, list.get(0), mViewOperate.getViewMore());
				downloadMoreClickView.setMulti(true);
				downloadMoreClickView.showMenu();
			}
		}
	}

	private void showMoreDialogSingle(DownloadItemInfo info) {
		switch (info.mStatus) {
			case UiStatusDefine.STATUS_SUCCESSFUL:
				// 成功
				showDownloadSuccessfulDialog(info);
				break;
			case UiStatusDefine.STATUS_FAILED:
			case UiStatusDefine.STATUS_PENDING:
			case UiStatusDefine.STATUS_PAUSED:
			case UiStatusDefine.STATUS_RUNNING:
			case UiStatusDefine.ERROR_UNKNOWN:
				// 失败,下载中,暂停,等待下载等状态
				showDownloadFailedDialog(info);
				break;
			default:
				break;
		}
	}

	private void showMoreDialogMulti(final List<DownloadItemInfo> list) {
		String[] items = {
				getString(R.string.download_redownload)
		};
		AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				switch (position) {
					case 0:// 重新下载
						redownload(list);
						break;
					default:
						break;
				}
				exitEditingMode();
			}
		};
		showContextDialog(items, listener);
	}

	private void showDownloadSuccessfulDialog(final DownloadItemInfo info) {
		String[] items = {
				getString(R.string.download_rename),
				getString(R.string.download_redownload),
				getString(R.string.download_detail)
		};
		AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				switch (position) {
					case 0:// 重命名
						rename(info);
						break;
					case 1:// 重新下载
						List<DownloadItemInfo> list = new ArrayList<DownloadItemInfo>();
						list.add(info);
						redownload(list);
						break;
					case 2:// 详细信息
						sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_TASK_DETAIL);
						Intent intent = new Intent(DownloadActivity.this, DownloadDetailActivity.class);
						intent.putExtra("DownloadItemInfo", info);
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
						break;
					default:
						break;
				}
				exitEditingMode();
			}
		};
		showContextDialog(items, listener);
	}

	private void showDownloadFailedDialog(final DownloadItemInfo info) {
		String[] items = {
				getString(R.string.download_redownload),
				getString(R.string.download_detail)
		};
		AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				switch (position) {
					case 0:// 重新下载
						List<DownloadItemInfo> list = new ArrayList<DownloadItemInfo>();
						list.add(info);
						redownload(list);
						break;
					case 1:// 详细信息
						Intent intent = new Intent(DownloadActivity.this, DownloadDetailActivity.class);
						intent.putExtra("DownloadItemInfo", info);
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
						break;
					default:
						break;
				}
				exitEditingMode();
			}
		};
		showContextDialog(items, listener);
	}

	public void showLongClickMenu(final DownloadItemInfo info) {
		final int MENU_DELETE = 0;
		final int MENU_SHARE = 1;
		final int MENU_REDOWNLOAD = 2;
		final int MENU_RENAME = 3;
		final int MENU_TASK_DETAIL = 4;
		//删除、分享、重新下载、重命名、任务详情
		String[] items = {
				getString(R.string.delete),
				getString(R.string.share),
				getString(R.string.download_redownload)
				, getString(R.string.download_rename)
				, getString(R.string.download_detail)
		};
		AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				switch (position) {
					case MENU_DELETE:// 删除
						showLongClickDeleteDialog(info);
						break;
					case MENU_SHARE: // 分享
						longClickShare(info);
						break;
					case MENU_REDOWNLOAD://重新下载
						redownload(Collections.singletonList(info));
						break;
					case MENU_RENAME://重命名
						rename(info);
						break;
					case MENU_TASK_DETAIL://任务详情
						Intent intent = new Intent(DownloadActivity.this, DownloadDetailActivity.class);
						intent.putExtra("DownloadItemInfo", info);
						startActivity(intent);
						overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
						break;
					default:

						break;
				}
			}
		};
		showContextDialog(items, listener);
	}


	private void showContextDialog(String[] items, AdapterView.OnItemClickListener listener) {
		ListDialog dialog = new ListDialog(this);
		dialog.setItems(items, -1);
		dialog.setOnItemClickListener(listener);
		dialog.show();
	}

	public void redownload() {
		redownload(mDownloadView.getCheckedItemList());
	}

	public void redownload(List<DownloadItemInfo> list) {
		sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_EDIT_REDOWNLOAD);
		if (list == null || list.isEmpty()) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			DownloadItemInfo info = list.get(i);
			File file = new File(info.mFilePath);
			if (file.exists()) {
				file.delete();
				queryHandler.deleteFile(file.getAbsolutePath());
			}
			DownloadManager.getInstance().restartDownload(info.mId);
		}
	}

	public void rename(final DownloadItemInfo info) {
		sendGoogleStatistics(DOWNLOAD_RENAME);
		if (TextUtils.isEmpty(info.mFilePath)) {
			return;
		}
		// 弹框
		final CommonDialog dialog = new CommonDialog(this);
		dialog.setTitle(R.string.download_rename);
		dialog.setCenterView(R.layout.dialog_rename);
		final EditText etName = (EditText) dialog.findViewById(R.id.et_name);
		final String name = info.mFilePath.substring(info.mFilePath.lastIndexOf("/") + 1);
		final String folder = info.mFilePath.substring(0, info.mFilePath.lastIndexOf("/") + 1);
		etName.setText(name);
		dialog.setBtnOkListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String newName = etName.getText().toString();
				if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newName.replace(" ", ""))) {
					CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
					hideSoftInputFromWindow();
					return;
				}
				// 	处理文件名为 ".xxx" 情况
				if (newName.lastIndexOf(".") == 0) {
					CustomToastUtils.getInstance().showDurationToast(R.string.empty_file_name, 3000);
					hideSoftInputFromWindow();
					return;
				}
				// 判断是否有重名文件
				if (new File(folder + newName).exists() || TextUtils.equals(newName, name)) {
					CustomToastUtils.getInstance().showDurationToast(R.string.download_file_name_exists, 3000);
					hideSoftInputFromWindow();
					return;
				}
				final String fName = newName;
				new File(info.mFilePath).renameTo(new File(folder + fName));
				DownloadManager.getInstance().renameDownloadFilePath(info.mId, folder + newName, new DownloadProvider.UpdateDownloadCallback() {
					@Override
					public void onUpdateDownload(boolean ret, long id) {
						// 重命名磁盘上的文件
						String name = new File(info.mFilePath).getName();
						String downloadDataDirPath = VCStoragerManager.getInstance().getDownloadDataDirPath();

						if (!TextUtils.isEmpty(downloadDataDirPath)) {
							new File(downloadDataDirPath + name + ".obj").renameTo(new File(downloadDataDirPath + fName + ".obj"));
						}

						DownloadManager.getInstance().getDownloadItemList();
					}
				});
				hideSoftInputFromWindow();
				dialog.dismiss();
			}
		});
		dialog.setBtnCancelListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideSoftInputFromWindow();
				dialog.dismiss();
			}
		});
		dialog.show();
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				etName.setFocusable(true);
				int dot = name.lastIndexOf(".");
				if (dot > 0) {
					etName.setSelection(dot);
				}
				InputMethodManager inputManager =
						(InputMethodManager) etName.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(etName, 0);
			}
		}, 200);
	}

	/***---------- IDownloadViewOperateDelegate start ------------***/
	@Override
	public void edit() {
		intoEditingMode();
	}

	@Override
	public void complete() {
		sendGoogleStatistics(DOWNLOAD_EDIT_COMPLETE);
		exitEditingMode();
	}

	@Override
	public void clear() {
		onClearButtonClick();
		sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_EMPTY_LIST);
	}

	@Override
	public void share() {
		sendGoogleStatistics(GoogleConfigDefine.DOWNLOAD_EDIT_SHARE);
		onShareButtonClick();
	}


	@Override
	public void delete() {
		if (downloadMoreClickView != null)
			downloadMoreClickView.setIsShowing(false);
		sendGoogleStatistics(DOWNLOAD_EDIT_DELETE);
		onDeleteButtonClick();
	}

	@Override
	public void more() {
		sendGoogleStatistics(DOWNLOAD_EDIT_MORE);
		onMoreButtonClick();
	}

	public void queryCount() {
		ThreadManager.postTaskToIOHandler(new Runnable() {
			@Override
			public void run() {
				final Map<String, Integer> map = QueryUtils.queryCount(DownloadActivity.this);
				ThreadManager.postTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						if (map != null && !map.isEmpty()) {
							mFileClassifyView.refreshUi();
						}
					}
				});
			}
		});
	}

	private boolean isNeedReQuery() {
		return QueryUtils.queryMap == null || QueryUtils.queryMap.isEmpty() || !changedFileTypes.isEmpty();
	}

	@Override
	public void notifyChanged(String key, boolean value) {

	}

	@Override
	public void notifyChanged(String key, final String type) {
		if (FILE_COUNT_CHANGED.equals(key)) {
			if (Constants.TYPE_ALL.equals(type)) {  //判断是否查询全部类型
				queryCount();
				return;
			}
			ThreadManager.getIOHandler().post(new Runnable() {
				@Override
				public void run() {
					QueryUtils.queryCountByFileType(type);
					ThreadManager.postTaskToUIHandler(new Runnable() {
						@Override
						public void run() {
							mFileClassifyView.refreshUi(type);
							ConfigWrapper.put(type, false);
							ConfigWrapper.commit();

						}
					});
				}
			});
		}
	}

	@Override
	public void notifyChanged(String key, int value) {

	}


	private static class QueryHandler extends MediaDBProvider {

		private SoftReference<DownloadActivity> viewWeakReference;

		QueryHandler(Context context, SoftReference<DownloadActivity> viewWeakReference) {
			super(context);
			this.viewWeakReference = viewWeakReference;
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			DownloadActivity outView = viewWeakReference.get();
			if (outView != null) {
				outView.queryCount();
			}
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			DownloadActivity outView = viewWeakReference.get();
			if (outView != null) {
				outView.queryCount();
			}
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			DownloadActivity outView = viewWeakReference.get();
			if (outView != null) {
				outView.queryCount();
			}
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			DownloadActivity outView = viewWeakReference.get();
			if (outView != null) {
				outView.queryCount();
			}
		}
	}


	private void hideSoftInputFromWindow() {

		InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		View view = getCurrentFocus();
		if (view == null) {
			view = new View(this);
		}
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	public int itemCount() {
		return mDownloadView.getDownloadList().size();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onHidePopup(OnHidePopup onHidePopup) {
		if (downloadMoreClickView != null) {
			downloadMoreClickView.setIsShowing(false);
		}
	}

	public static class OnHidePopup{

	}


	/***---------- IDownloadViewOperateDelegate end ------------***/
}
