package com.polar.browser.history;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView.OnHeaderClickListener;
import com.emilsjolander.components.stickylistheaders.WrapperView;
import com.polar.browser.R;
import com.polar.browser.common.ui.CommonBottomBar3;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.homepage.sitelist.SiteManager;
import com.polar.browser.i.IEditStateObserver;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.bean.events.SyncDatabaseEvent;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.db.SearchRecordApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 历史记录页面
 *
 * @author dpk
 */
public class HistoryView extends LinearLayout {

	public static final int TYPE_HISTORY = 1;
	public static final int TYPE_VISIT_HISTORY = 2;

	private static final int LIMIT_HISTORY_COUNT = 2000;

	private static final String TAG = "HistoryView";

	private HistoryItemAdapter mAdapter;

	private StickyListHeadersListView mStickyList;

	private CommonBottomBar3 mBottomBar;

	private IHistoryItemClick mClickDelegate;

	private IHistoryDeleteCallback mDeleteCallback;

	private IEditStateObserver mEditObserver;

	private View mEmptyView;

	private View mBtnTrash;

	private View mNoVisitHistoryView;

	private View mNoVisitHistorySeparateLine;

	private int mType = TYPE_HISTORY;

	private RelativeLayout mBackground;
	private View mBottomEditLayout;
	private View editTv;

	public HistoryView(Context context) {
		this(context, null);
	}

	public HistoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(IHistoryItemClick clickDelegate,
					 IHistoryDeleteCallback deleteCallback,
					 IEditStateObserver editObserver, int type) {
		LayoutInflater.from(getContext()).inflate(R.layout.view_history, this);
		mClickDelegate = clickDelegate;
		mDeleteCallback = deleteCallback;
		mEditObserver = editObserver;
		mType = type;
		EventBus.getDefault().register(this);
		initView();
		initData();
	}

	public void destory() {
		EventBus.getDefault().unregister(this);
	}

	private void initView() {
		mBackground = (RelativeLayout) findViewById(R.id.rl_background);
		mNoVisitHistoryView = findViewById(R.id.title);
		mNoVisitHistorySeparateLine = findViewById(R.id.separate_line);
		mEmptyView = findViewById(R.id.view_empty);
		mBtnTrash = findViewById(R.id.trash);
		mStickyList = (StickyListHeadersListView) findViewById(R.id.list);
		mStickyList.setDividerHeight(0);
		mStickyList.setOnHeaderClickListener(new OnHeaderClickListener() {

			@Override
			public void onHeaderClick(StickyListHeadersListView arg0,
									  View arg1, int arg2, long arg3, boolean arg4) {
				// TODO Auto-generated method stub
				SimpleLog.d("onHeaderClick", "header");
			}
		});
		mStickyList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos,
									long id) {
				View item = v;
				if (v instanceof WrapperView) {
					item = ((WrapperView) v).getItem();
				}
				mAdapter.onItemClick(item);
				mBottomBar.setDeleteBtnEnabled(mAdapter.hasCheckedItem());
			}
		});
		mStickyList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
										   int pos, long id) {
				View item = v;
				if (v instanceof WrapperView) {
					item = ((WrapperView) v).getItem();
				}
				mAdapter.onItemLongClick(item);
				return true;
			}
		});
		mBottomBar = (CommonBottomBar3) findViewById(R.id.bottom_bar);
		mBottomBar.getDeleteBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (CommonUtils.isFastDoubleClick()) {
					return;
				}
				deleteItems();
			}
		});
		mBottomBar.getCheckAllBtn().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isCheckedAll = updateCheckState();
				mAdapter.setAllChecked(!isCheckedAll);
				updateCheckState();
			}
		});
		mBottomBar.setDeleteBtnEnabled(false);
		mBottomBar.getTvComplete().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mEditObserver.onEditStateChanged(false);
			}
		});
		mBottomEditLayout = findViewById(R.id.edit_layout);
		editTv = findViewById(R.id.btn_edit);
		editTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mEditObserver.onEditStateChanged(true);
			}
		});

		if (mType == TYPE_VISIT_HISTORY) {
			mBtnTrash.setVisibility(View.VISIBLE);
			mBtnTrash.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (CommonUtils.isFastDoubleClick()) {
						return;
					}
					showDeleteVisitedDialog();
				}
			});
		} else {
			mBtnTrash.setVisibility(View.GONE);
		}
	}

	private void deleteItems() {
		Iterator<HistoryInfo> iterator = mAdapter.getData().iterator();
		List<String> urlList = new ArrayList<String>();
		while (iterator.hasNext()) {
			HistoryInfo info = iterator.next();
			if (info.isChecked) {
				urlList.add(info.url);
			}
		}
		if (urlList.size() > 0) {
			showDeleteDialog(urlList);
		} else {
			CustomToastUtils.getInstance().showTextToast(R.string.delete_not_select);
		}
	}

	private void showDeleteDialog(final List<String> urlList) {
		final CommonDialog dialog = new CommonDialog(getContext(), getContext()
				.getString(R.string.tips), getContext().getString(
				R.string.history_clean_content));
		dialog.setBtnCancel(getContext().getString(R.string.cancel),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		dialog.setBtnOk(getContext().getString(R.string.ok),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();

						ThreadManager.getIOHandler().post(new Runnable() {
							@Override
							public void run() {
								if (!ListUtils.isEmpty(urlList)) {
									for (int i = 0; i < urlList.size(); i++) {
										String historyAddr = urlList.get(i);
										try {
											HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).deleteHistoryRecordByAddr(historyAddr);
											EventBus.getDefault().post(new SyncDatabaseEvent(SyncDatabaseEvent.TYPE_HISTORY_RECORD));
										} catch (SQLException e) {
											e.printStackTrace();
										}
									}
								}

							}
						});
						if (updateCheckState()) {
							ThreadManager.getIOHandler().post(new Runnable() {
								@Override
								public void run() {
									try {
										SearchRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).clearAllSearchRecord();
										SiteManager.getInstance().updateHistoryRecords(null);
									} catch (SQLException e) {
										e.printStackTrace();
									}
								}
							});
						}
						changeEditState(false);
					}
				});
		dialog.show();
	}

	private void showDeleteVisitedDialog() {
		final CommonDialog dialog = new CommonDialog(getContext(), getContext()
				.getString(R.string.tips), getContext().getString(
				R.string.history_clean_visited));
		dialog.setBtnCancel(getContext().getString(R.string.cancel),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		dialog.setBtnOk(getContext().getString(R.string.ok),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						ThreadManager.getIOHandler().post(new Runnable() {
							@Override
							public void run() {
								try {
									HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(getContext())).clearAllHistoryRecord();
									SiteManager.getInstance().updateHistoryRecords(null);
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
		dialog.show();
	}

	private void initData() {
		mAdapter = new HistoryItemAdapter(getContext(), mEditObserver);
		mAdapter.registerDelegate(mClickDelegate);
		mStickyList.setAdapter(mAdapter);
		SimpleLog.d(TAG, "queryHistoryAsync() begin time:" + String.valueOf(System.currentTimeMillis()));
		if (mType == TYPE_VISIT_HISTORY) {
//			HistoryManager.getInstance().queryAddressVisitedHistory(
//					LIMIT_HISTORY_COUNT, new HistoryCallbackImpl());
		} else {
//			HistoryManager.getInstance().queryHistoryAsync(LIMIT_HISTORY_COUNT,
//					new HistoryCallbackImpl());
			refreshHistoryView();

		}
	}

	private void refreshHistoryView() {
		ThreadManager.getIOHandler().post(new Runnable() {
			@Override
			public void run() {
				try {
					List<HistoryRecord> historyRecords = HistoryRecordApi.getInstance(
							CustomOpenHelper.getInstance(getContext())).queryAllHistoryRecordByTS(LIMIT_HISTORY_COUNT);

					final List<HistoryInfo> infoList = new ArrayList<>();
					if (!ListUtils.isEmpty(historyRecords)) {
						for (int i = 0; i < historyRecords.size(); i++) {
							HistoryInfo historyInfo = new HistoryInfo();
							HistoryRecord historyRecord = historyRecords.get(i);
							historyInfo.id = historyRecord.getId();
							historyInfo.count = historyRecord.getCount();
							historyInfo.src = historyRecord.getSource();
							historyInfo.timestamp = historyRecord.getTs();
							historyInfo.title = historyRecord.getHistoryTitle();
							historyInfo.url = historyRecord.getHistoryAddr();
							infoList.add(historyInfo);
						}
					}
					ThreadManager.getUIHandler().post(new Runnable() {
						@Override
						public void run() {
							mAdapter.initData(infoList);
							mAdapter.updateData(infoList);
							changeEditState(false);
							editTv.setEnabled(!ListUtils.isEmpty(infoList));
							if (mDeleteCallback != null) {
								mDeleteCallback.notifyDelete();
							}
							updateEmptyView();
						}
					});

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void changeEditState(boolean isEditing) {
		mAdapter.changeEditeState(isEditing);
		if (isEditing) {
			mBottomEditLayout.setVisibility(View.GONE);
			mBottomBar.setVisibility(View.VISIBLE);
		} else {
			mBottomBar.setVisibility(View.GONE);
			mBottomEditLayout.setVisibility(View.VISIBLE);
			updateCheckState();
		}
	}

	public boolean updateCheckState() {
		boolean isCheckedAll = mAdapter.isCheckedAll();
		mBottomBar.setCheckAll(isCheckedAll);
		boolean hasCheckedItem = mAdapter.hasCheckedItem();
		mBottomBar.setDeleteBtnEnabled(hasCheckedItem);
		return isCheckedAll;
	}

	private void updateEmptyView() {
		if (mAdapter.getCount() == 0) {
			mBtnTrash.setVisibility(View.GONE);
			if (mType == TYPE_HISTORY) {
				mEmptyView.setVisibility(View.VISIBLE);
				mBackground.setBackgroundColor(getResources().getColor(
						R.color.empty_view_background_color));
			} else {
				mNoVisitHistoryView.setVisibility(View.VISIBLE);
				mNoVisitHistorySeparateLine.setVisibility(View.VISIBLE);
				changeBgColor();
			}
		} else {
			mNoVisitHistoryView.setVisibility(View.GONE);
			mNoVisitHistorySeparateLine.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.GONE);
			if (mType == TYPE_VISIT_HISTORY) {
				mBtnTrash.setVisibility(View.VISIBLE);
			}
			changeBgColor();
		}
	}

	private void changeBgColor() {
		mBackground.setBackgroundColor(getResources().getColor(R.color.transparent));
	}

	public boolean isNoHistory() {
		return mAdapter.getCount() == 0;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSyncDatabaseEvent(SyncDatabaseEvent syncDatabaseEvent) {
		switch (syncDatabaseEvent.type) {
			case SyncDatabaseEvent.TYPE_HISTORY_RECORD:
				refreshHistoryView();
				break;
			default:
				break;
		}
	}
}
