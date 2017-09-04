package com.polar.browser.download.download;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.database.MediaDBProvider;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.UiStatusDefine;
import com.polar.browser.download_refactor.dinterface.IDownloadObserver;
import com.polar.browser.download_refactor.dinterface.IDownloadUIDelegate;
import com.polar.browser.download_refactor.ui.DownloadAdapter;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SimpleLog;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.DownloadManager.STATUS_SUCCESSFUL;

public class DownloadView extends RelativeLayout implements IDownloadObserver {

	/** 下载列表 **/
	private ListView mLVDownload;

	/** 列表为空，显示的默认图 **/
	private View mEmptyView;

	/** 下载列表的Adapter **/
	private DownloadAdapter mAdapter;

	private ArrayList<DownloadItemInfo> mItemInfos = new ArrayList<>();

	private IDownloadUIDelegate mDownloadUIDelegate;

	private QueryHandler mQueryHandler;

	private DownloadActivity mActivity;
	private static final String TAG = "DownloadView";

	public DownloadView(Context context) {
		this(context, null);
	}

	public DownloadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		if (getContext() instanceof DownloadActivity)
			mActivity = (DownloadActivity) getContext();
		SoftReference<DownloadView> viewWeakReference = new SoftReference<>(this);
		mQueryHandler = new QueryHandler(mActivity, viewWeakReference);
		inflate(getContext(), R.layout.view_download, this);
		initView();
	}

	private void initView() {
		mLVDownload = (ListView) findViewById(R.id.lv_downloading);
		mEmptyView = findViewById(R.id.view_empty);
	}

	public void setDownloadUIDelegate(IDownloadUIDelegate downlaodUIDelegate) {
		mDownloadUIDelegate = downlaodUIDelegate;
	}

	public void initData() {
		mAdapter = new DownloadAdapter(getContext());
		mLVDownload.setAdapter(mAdapter);
		DownloadManager.getInstance().addObserver(this);
		DownloadManager.getInstance().getDownloadItemList();
	}

	/**
	 * 刷新列表
	 */
	public void notifyDataSetChanged(){
		ThreadManager.postTaskToUIHandler(    new Runnable() {
			@Override
			public void run() {
				mAdapter.updateData(mItemInfos);
				checkShowEmptyView();
				if (mDownloadUIDelegate != null) {
					mDownloadUIDelegate.updateEditButtonState();
				}
			}
		});
	}

	/**
	 * 检查是否显示列表为空页面
	 */
	public void checkShowEmptyView(){
		if (mItemInfos.isEmpty()) {
			mEmptyView.setVisibility(View.VISIBLE);
			mLVDownload.setVisibility(View.GONE);
		} else {
			mEmptyView.setVisibility(View.GONE);
			mLVDownload.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置全选or反全选状态
	 * @param isCheckAll
	 */
	public void checkAll(boolean isCheckAll){
		mAdapter.setAllChecked(isCheckAll);
	}

	/**
	 * 切换编辑or非编辑状态
	 * @param isEditing
	 */
	public void change2editState(boolean isEditing){
		mAdapter.changeEditeState(isEditing);
	}

	/**
	 * 是否处于全选状态； 处于全选状态，应显示“取消全选”；
	 * @return
	 */
	public boolean isCheckAllState(){
		if (mItemInfos.isEmpty()) {
			return false;
		}
		for (int i = 0; i < mItemInfos.size(); i++) {
			if (!mItemInfos.get(i).isChecked) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获得选中项的个数
	 * @return
	 */
	public int getCheckItemCount(){
		int count = 0;
		for (int i = 0; i < mItemInfos.size(); i++) {
			if (mItemInfos.get(i).isChecked) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 删除长按项
	 */
	public void deleteLongClick(DownloadItemInfo downloadItemInfo, boolean isDeleteFile){


		ArrayList<Long> alIds = new ArrayList<>();
		ArrayList<DownloadItemInfo> infoList = new ArrayList<>();
		if (mItemInfos.isEmpty() || downloadItemInfo == null) {
			return;
		}

		alIds.add(downloadItemInfo.mId);
		infoList.add(downloadItemInfo);

		long[] ids = new long[alIds.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = alIds.get(i);
		}
		if(isDeleteFile){
			delete(infoList);
			Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,infoList.size()==mAdapter.getCount()?
					GoogleConfigDefine.DOWNLOAD_EMPTY_LIST_SRC:GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_SRC);
		}

		com.polar.browser.download_refactor.DownloadManager.getInstance().deleteDownload(ids, isDeleteFile);
	}

	/**
	 * 删除选中项
	 */
	public void deleteChecked(boolean isDeleteFile){
		
		ArrayList<Long> alIds = new ArrayList<>();
		ArrayList<DownloadItemInfo> infoList = new ArrayList<>();
		if (mItemInfos.isEmpty()) {
			return;
		}
		for (int i = 0; i < mItemInfos.size(); i++) {
			if (mItemInfos.get(i).isChecked) {
				alIds.add(mItemInfos.get(i).mId);
				infoList.add(mItemInfos.get(i));
			}
		}
		long[] ids = new long[alIds.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = alIds.get(i);
		}
		if(isDeleteFile){
			delete(infoList);
			Statistics.sendOnceStatistics(GoogleConfigDefine.DOWNLOAD_MANAGER,infoList.size()==mAdapter.getCount()?
					GoogleConfigDefine.DOWNLOAD_EMPTY_LIST_SRC:GoogleConfigDefine.DOWNLOAD_EDIT_DELETE_SRC);
		}

		com.polar.browser.download_refactor.DownloadManager.getInstance().deleteDownload(ids, isDeleteFile);
	}


	private void delete(List<DownloadItemInfo> list) {
		if(list == null || list.size()<1) return;
		String where = buildWhere(list);
		mQueryHandler.startDelete(MediaDBProvider.TOKEN_DELETE,
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

	public void deleteAll(boolean isDeleteFile) {
		ArrayList<Long> alIds = new ArrayList<>();
		if (mItemInfos.isEmpty()) {
			return;
		}
		for (int i = 0; i < mItemInfos.size(); i++) {
			alIds.add(mItemInfos.get(i).mId);
		}
		long[] ids = new long[alIds.size()];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = alIds.get(i);
		}
		if(isDeleteFile)
			delete(mItemInfos);
		com.polar.browser.download_refactor.DownloadManager.getInstance().deleteDownload(ids, isDeleteFile);
	}

	/**
	 * 列表中是否还有下载条目项（是否被删除完了）
	 * @return
	 */
	public boolean hasDownloadItem(){
		return mItemInfos != null && !mItemInfos.isEmpty();
	}

	/**
	 * 获取选中条目列表Info
	 * @return
     */
	public List<DownloadItemInfo> getCheckedItemList() {
		List<DownloadItemInfo> list = new ArrayList<>();
		for (int i = 0; i < mItemInfos.size(); i++) {
			if (mItemInfos.get(i).isChecked) {
				list.add(mItemInfos.get(i));
			}
		}
		return list;
	}

	public void destroy() {
		com.polar.browser.download_refactor.DownloadManager.getInstance().removeObserver(this);
		if(mQueryHandler != null){
			mQueryHandler.removeCallbacksAndMessages(null);
		}

	}

	@Override
	public void handleDownloadLists(ArrayList<DownloadItemInfo> lists) {
		if (lists != null) {
			mItemInfos = lists;
			Collections.sort(mItemInfos, new Comparator<DownloadItemInfo>() {
				@Override
				public int compare(DownloadItemInfo lhs, DownloadItemInfo rhs) {
					return Long.valueOf(rhs.mId).compareTo(lhs.mId);
				}
			});
		}

		// TODO 刷新列表
		notifyDataSetChanged();
	}

	@Override
	public void handleDownloadItemAdded(boolean ret, long id, DownloadItemInfo info) {
		SimpleLog.d(TAG,"添加了下载任务");
		DownloadManager.getInstance().getDownloadItemList();
	}

	@Override
	public void handleDownloadItemRemoved(boolean ret, long[] ids) {
		DownloadManager.getInstance().getDownloadItemList();
	}

	@Override
	public void handleDownloadStatus(long id, int status, int reason) {
		SimpleLog.d(TAG,"下载status ="+status);
		if (reason == UiStatusDefine.ERROR_URL_FAILURE) {
			CustomToastUtils.getInstance().showTextToast(R.string.download_url_error);
		}
		final DownloadItemInfo info = mAdapter.getDownloadItemInfo(id);
//		if (info == null) {
//			return;
//		}
//		if (!new File(info.mFilePath).exists()) {
//			info.mCurrentBytes = 0;
//		} else {
//			long size = 0;
//			try {
//				size = new FileInputStream(new File(info.mFilePath)).available();
//			} catch (IOException ignored) {
//			}
//			if (size == 0) {
//				info.mCurrentBytes = 0;
//			}
//		}
//		DownloadManager.getInstance().getDownloadItemList();
		final DownloadItem item = mAdapter.getDownloadItem(id);
		if (item != null) {
			ThreadManager.postTaskToUIHandler(new Runnable() {
				@Override
				public void run() {
					item.bind(info);
				}
			});

			if(status == STATUS_SUCCESSFUL){

				mQueryHandler.insertFile(info.mFilePath);
				ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
						intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, false);
						JuziApp.getInstance().sendBroadcast(intent);
					}
				},50);
			}
		}
	}


	@Override
	public void handleDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes) {
		SimpleLog.d(TAG,"currentBytes="+currentBytes);
		DownloadItem item = mAdapter.getDownloadItem(id);
		if (item != null) {
			item.refreshDownloadProgress(id, currentBytes, totalBytes, speedBytes);
		}
	}

	@Override
	public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval) {

	}

	private static class QueryHandler extends MediaDBProvider {

		private SoftReference<DownloadView> viewWeakReference ;

		QueryHandler(Context context, SoftReference<DownloadView> viewWeakReference) {
			super(context);
			this.viewWeakReference = viewWeakReference;
		}

		@Override
		protected void onInsertComplete(int token, Object cookie, Uri uri) {
			SimpleLog.e(TAG,"onInsertComplete");
			DownloadView downloadView = viewWeakReference.get();
			if(downloadView!=null){
				downloadView.mActivity.queryCount();
			}
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			DownloadView downloadView = viewWeakReference.get();
			if(downloadView!=null){
				downloadView.mActivity.queryCount();
			}
		}

		@Override
		protected void onUpdateComplete(int token, Object cookie, int result) {
			DownloadView downloadView = viewWeakReference.get();
			if(downloadView!=null){
				downloadView.mActivity.queryCount();
			}
		}

		@Override
		protected void onDeleteComplete(int token, Object cookie, int result) {
			SimpleLog.d(TAG,"onDeleteComplete");
			DownloadView downloadView = viewWeakReference.get();
			if(downloadView!=null){
				downloadView.mActivity.queryCount();
			}
		}
	}

	public List<DownloadItemInfo> getDownloadList() {
		return mItemInfos;
	}


}
