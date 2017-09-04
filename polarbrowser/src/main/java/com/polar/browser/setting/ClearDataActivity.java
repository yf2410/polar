package com.polar.browser.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.DialogContentView;
import com.polar.browser.database.MediaDBProvider;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.db.DownloadProvider;
import com.polar.browser.download_refactor.ui.DownloadNotify;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.CookieUtil;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.db.SearchRecordApi;
import com.polar.browser.view.switchbutton.SwitchButton;

import java.io.File;
import java.lang.ref.SoftReference;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClearDataActivity extends LemonBaseActivity implements
		OnClickListener, OnCheckedChangeListener {

	public static final String TAG = "ClearDataActivity";
	private CommonDialog mCommonDialog;
	private int mClickItemId;
	/**
	 * 是否在退出时清除全部浏览记录
	 **/
	private SwitchButton mSbExitClear;
	private TextView cache_size;
	private File[] listFiles;




	// private byte[] totalSizes;

	private File small_dir;

	private File bigNews_dir;

	private File logo_dir;

	private long size_bigs;
	// private long size_ACaches;

	private long size_logos;

	private long size_smalls;

	private long welcomePicSize;

	private long aCacheDirVolleySize;

	private File welcomePath;

	QueryHandler queryHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clear_data);
		// 初始化展示缓存文件大小的textview
		cache_size = (TextView) findViewById(R.id.cache_size);
		queryHandler = new QueryHandler(this, new SoftReference<>(this));
		initTextView();
		initDialog();
		initListener();
	}

	private void initTextView() {
		// TODO Auto-generated method stub
		showCacheSize();
	}

	private void initDialog() {
		mCommonDialog = new CommonDialog(this, R.string.tips,
				R.string.recover_setting_content);
		mCommonDialog.setBtnCancel(getString(R.string.cancel),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						mCommonDialog.dismiss();
					}
				});
		mCommonDialog.setBtnOk(getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
				// 清除
				doClear();
			}
		});
		boolean enableExitClear = ConfigManager.getInstance()
				.isEnableExitClear();
		mSbExitClear = (SwitchButton) findViewById(R.id.sb_clear_data_exit_browser);
		mSbExitClear.setChecked(enableExitClear);
		mSbExitClear.setOnCheckedChangeListener(this);
	}

	private void initListener() {
		findViewById(R.id.line_clear_cookie).setOnClickListener(this);
		findViewById(R.id.line_clear_cache).setOnClickListener(this);
		findViewById(R.id.line_clear_history).setOnClickListener(this);
		findViewById(R.id.line_clear_download).setOnClickListener(this);
		findViewById(R.id.line_clear_all).setOnClickListener(this);
		findViewById(R.id.line_exit_clear).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.line_clear_cookie:// 清除cookie
				mClickItemId = v.getId();
				mCommonDialog.setContentTxt(getString(R.string.clear_cookie));
				mCommonDialog.show();
				break;
			case R.id.line_clear_cache:// 清除缓存
				mClickItemId = v.getId();
				mCommonDialog.setContentTxt(getString(R.string.clear_cache));
				mCommonDialog.show();
				break;
			case R.id.line_clear_history:// 清除历史记录
				mClickItemId = v.getId();
				mCommonDialog.setContentTxt(getString(R.string.clear_history));
				mCommonDialog.show();
				break;
			case R.id.line_clear_download:// 清除下载记录(单独对话框)
				final CommonDialog dialog = new CommonDialog(this,
						getString(R.string.tips), getString(R.string.clear_download));
				final DialogContentView contentView = new DialogContentView(this);
				dialog.addView(contentView);
				dialog.setBtnCancel(getString(R.string.cancel),
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
				dialog.setBtnOk(getString(R.string.ok), new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						notifyDotGone();
						clearDownload(contentView.isChecked());
					}
				});
				dialog.show();
				break;
			case R.id.line_clear_all:// 清除全部
				mClickItemId = v.getId();
				mCommonDialog.setContentTxt(getString(R.string.clear_all_2));
				mCommonDialog.show();
				break;
			case R.id.line_exit_clear:
				// 退出时是否清除历史记录
				if (mSbExitClear.isShown()) {
					mSbExitClear.slideToChecked(!mSbExitClear.isChecked());
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean state) {
		switch (v.getId()) {
			case R.id.sb_clear_data_exit_browser:
				if (state != ConfigManager.getInstance().isEnableExitClear()) {
					ConfigManager.getInstance().setEnableExitClear(state);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 清除下载记录
	 *
	 * @param isDelFile
	 */
	private void clearDownload(final boolean isDelFile) {
		// TODO 清除下载记录
		CustomToastUtils.getInstance().showTextToast(R.string.clear_download);
		ThreadManager.postTaskToIOHandler(new Runnable() {
			@Override
			public void run() {
				SimpleLog.d(TAG, "isDelFile=="+isDelFile);
				DownloadProvider.getInstance().init(JuziApp.getAppContext());
				if (isDelFile) {
					// 删除下载文件
					ArrayList<DownloadItemInfo> list = DownloadProvider.getInstance().getDownloadListSyn();
					ArrayList<String> names = new ArrayList<String>();
					delete(list);
					ArrayList<Long> ids = new ArrayList<Long>();
					for (int i = 0; i < list.size(); i++) {
						ids.add(list.get(i).mId);
						File file = new File(list.get(i).mFilePath);
						String name = file.getName();
						if (file.exists()) {
							names.add(file.getAbsolutePath());
							SimpleLog.e("", "删除 : "
									+ file.getAbsoluteFile().toString());
							if (file.delete()) {
								SimpleLog.e("", "删除文件成功....");
							}
						}
						String fileDataPath = VCStoragerManager.getInstance().getDownloadDataDirPath() + name + ".obj";
						File fileObj = new File(fileDataPath);
						if (fileObj.exists()) {
							SimpleLog.e("", "删除 : "
									+ file.getAbsoluteFile().toString());
							if (fileObj.delete()) {
								SimpleLog.e("", "删除.obj文件成功....");
							}
						}
					}
					long[] idsArray = new long[ids.size()];
					for (int i = 0; i < ids.size(); i++) {
						idsArray[i] = ids.get(i);
					}
					DownloadNotify.cancelNotify(idsArray);
					String[] filePaths = (String[]) names
							.toArray(new String[names.size()]);
					SysUtils.refreshMediaMountedAfterDelete(filePaths);
				}
				FileUtils.deleteFileOrDirectory(new File(VCStoragerManager.getInstance().getDownloadDataDirPath()));
				// 清除数据库中下载完成条目
				DownloadProvider.getInstance().deleteSyn();
			}
		});
	}


	private void delete(List<DownloadItemInfo> list) {
		if(list == null || list.size()<1) return;
		String where = buildWhere(list);
		queryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
				null,
				MediaStore.Files.getContentUri("external"),
				MediaStore.Files.FileColumns.DATA + " in " + where,
				null);
	}

	private String buildWhere(List<DownloadItemInfo> list) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (DownloadItemInfo info : list) {
			if(info.mFilePath == null) continue;
			String path = info.mFilePath.replace("'", "''");
			sb.append("'").append(path).append("'").append(",");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		sb.append(")");
		return sb.toString();
	}


	private static class QueryHandler extends MediaDBProvider {

		private SoftReference<ClearDataActivity> viewWeakReference ;

		QueryHandler(Context context, SoftReference<ClearDataActivity> viewWeakReference) {
			super(context);
			this.viewWeakReference = viewWeakReference;
		}


		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			SimpleLog.d(TAG,"onDeleteComplete");
			ClearDataActivity outView = viewWeakReference.get();
			if(outView!=null){
				Intent broadIntent = new Intent(CommonData.ACTION_FILE_COUNT_CHANGED);
				broadIntent.putExtra(CommonData.KEY_CHANGED_FILE_TYPE, Constants.TYPE_ALL);
				outView.sendBroadcast(broadIntent);
			}
		}
	}

	/**
	 * 清除记录
	 */
	private void doClear() {
		switch (mClickItemId) {
			case R.id.line_clear_cookie:
				// TODO 清除cookie
				CustomToastUtils.getInstance().showTextToast(R.string.clear_cookie);
				CookieUtil.clearCookie(this.getApplicationContext());
				break;
			case R.id.line_clear_cache:
				// TODO 清除缓存
				CustomToastUtils.getInstance().showTextToast(R.string.clear_cache);
				// 清除webview缓存
				TabViewManager.getInstance().clearCache();
				// 清除缓存文件
				clearCacheFiles();
				break;
			case R.id.line_clear_history:
				// TODO 清除历史记录
				CustomToastUtils.getInstance().showTextToast(R.string.clear_history);
//				HistoryManager.getInstance().deleteAllHistory();
				ThreadManager.getIOHandler().post(new Runnable() {
					@Override
					public void run() {
						try {
							SearchRecordApi.getInstance(CustomOpenHelper.getInstance(ClearDataActivity.this)).clearAllSearchRecord();
							HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(ClearDataActivity.this)).clearAllHistoryRecord();
							SiteManager.getInstance().updateHistoryRecords(null);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});

//				HistoryManager.getInstance().deleteAllOftenHistory();

				break;
			case R.id.line_clear_all:
				// TODO 清除全部
				CustomToastUtils.getInstance().showTextToast(R.string.clear_all_2);
				CookieUtil.clearCookie(this.getApplicationContext());
				TabViewManager.getInstance().clearCache();
				// 增加清除缓存文件
				clearCacheFiles();
//				HistoryManager.getInstance().deleteAllHistory();
//				HistoryManager.getInstance().deleteAllSearchHistory();
//				HistoryManager.getInstance().deleteAllOftenHistory();
				ThreadManager.getIOHandler().post(new Runnable() {
					@Override
					public void run() {
						try {
							SearchRecordApi.getInstance(CustomOpenHelper.getInstance(ClearDataActivity.this)).clearAllSearchRecord();
							HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(ClearDataActivity.this)).clearAllHistoryRecord();
							SiteManager.getInstance().updateHistoryRecords(null);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				});
				notifyDotGone();
				break;
			default:
				break;
		}
	}

	private void notifyDotGone() {
		Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
		intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, false);
		JuziApp.getInstance().sendBroadcast(intent);
	}

	// 清除缓存文件，对方法进行了集中一下
	private void clearCacheFiles() {
		// 清除logo_dir（推荐和分类列表logo的icon）
		clearHomeSite();
		// 清除ACache dir（推荐和分类列表的json数据）
		clearACacheVolley();
		// clear hot_news cache big_news small_news
		clearSmallAndBig_News();
		// 清理完毕，更新UI
		cache_size.setText("0B");
	}

	// TODO: 2016/12/12  需要注意清除Picasso缓存
	private void clearHomeSite() {
		ThreadManager.postTaskToIOHandler(new Runnable() {
			@Override
			public void run() {
//				Glide.get(getApplicationContext()).clearDiskCache();
			}
		});
	}

	// 删除ACache和Volley
	private void clearACacheVolley() {
		File[] cacheDir = getACacheDirVolley();
		if (cacheDir != null) {
			// 清除文件夹
			for (int i = 0; i < cacheDir.length; i++) {
				FileUtils.deleteFileOrDirectory(cacheDir[i]);
			}
		}
	}

	/**
	 * 清除small big_news
	 */
	private void clearSmallAndBig_News() {
		// 删除文件夹 big_news small_news
		deleteSmall_news(getSmall_News());
		deleteBig_news(getBig_News());
	}

	/**
	 * 清除small_news big_news 文件夹
	 * @param small
	 */
	private void deleteSmall_news(File small) {
		FileUtils.deleteFileOrDirectory(small);
	}

	private void deleteBig_news(File big) {
		FileUtils.deleteFileOrDirectory(big);
	}








	/**
	 * 获取ACache和Volley文件夹
	 * @return
	 */
	private File[] getACacheDirVolley() {
		File cacheDir = this.getCacheDir();
		listFiles = cacheDir.listFiles();
		// 要确认ACache文件夹是否存在
		if (listFiles.length > 0) {
			return listFiles;
		}
		return null;
	}


	/**
	 * 获取big_news文件夹
	 * @return
	 */
	private File getBig_News() {
		bigNews_dir = new File(this.getFilesDir() + File.separator + "big_news");
		return bigNews_dir;
	}

	/**
	 * 获取small_news文件夹
	 * @return
	 */
	private File getSmall_News() {
		small_dir = new File(this.getFilesDir() + File.separator + "small_news");
		return small_dir;
	}

	/**
	 * 获取缓存文件大小
	 * 包括ACache logo_dir small_news big_news
	 */
	private void showCacheSize() {
		// 获取总大小
		long totalSize = getTotalSize();
		String formatFileSize = FileUtils.formatFileSize(totalSize);
		cache_size.setText(formatFileSize);
	}

	/**
	 * 获取每个缓存文件大小
	 * @return
	 */
	private long getTotalSize() {
		getVolleyAndACacheSize();
		getBig_NewSize();
		getLogoDirSize();
		getSmallNewsSize();
		long total_size = aCacheDirVolleySize + size_bigs + size_logos
				+ size_smalls ;
		return total_size;
	}

	private void getSmallNewsSize() {
		File small_News = getSmall_News();
		size_smalls = FileUtils.getDirSize(small_News);
	}

	private void getLogoDirSize() {
		// TODO: 2016/10/17 homesite 缓存大小
//		size_logos = FileUtils.getDirSize(logoDir);
	}

	private void getBig_NewSize() {
		File big_News = getBig_News();
		size_bigs = FileUtils.getDirSize(big_News);
	}

	private void getVolleyAndACacheSize() {
		File[] aCacheDirVolley = getACacheDirVolley();
		if (aCacheDirVolley != null) {
			for (int i = 0; i < aCacheDirVolley.length; i++) {
				long dirSize = FileUtils.getDirSize(aCacheDirVolley[i]);
				aCacheDirVolleySize += dirSize;
			}
		}
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slid_out_to_right);
	}
}
