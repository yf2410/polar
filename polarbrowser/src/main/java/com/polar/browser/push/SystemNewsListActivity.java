package com.polar.browser.push;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.bean.db.SystemNews;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SystemNewsApi;


import java.sql.SQLException;
import java.util.List;

public class SystemNewsListActivity extends LemonBaseActivity implements SystemNewsAdapter.AllItemCheckedListener, SystemNewsAdapter.ItemCheckedListener, SystemNewsAdapter.ItemLongClickListener, View.OnClickListener {
	private static final long LIMIT = 20;
	private static final int ADD = 1;
	private static final int Empty = 0;
	private static final int ERROR = -1;
	private SystemNewsAdapter mAdapter;
	private ListView mListView;
	private TextView mTvRead;
	//private CommonTitleBar mTitleBar;
	private boolean editable;
//	private SystemNewsApi api;
	private CommonCheckBox1 mCheckBox;
	private TextView mDelete;
	private TextView markAsRead;
	private TextView checkAllTV;
	//private TextView mBtnEdit;
	//private TextView mBottomEditLayout;
	private LinearLayout mBottomBar;
	private RelativeLayout mListEmptyView;
	private TextView editTv;
	private LinearLayout mBottom;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case ADD:
					mAdapter.add((List<SystemNews>) msg.obj);
					editTv.setEnabled(true);
					mTvRead.setEnabled(true);
					break;
				case ERROR:
					// TODO: 2016/7/14 异常
					editTv.setEnabled(false);
					mTvRead.setEnabled(false);
					mListView.setVisibility(View.GONE);
					mListEmptyView.setVisibility(View.VISIBLE);
					break;
				case Empty:
					editTv.setEnabled(false);
					mTvRead.setEnabled(false);
					mListView.setVisibility(View.GONE);
					mListEmptyView.setVisibility(View.VISIBLE);
					break;
				default:
					break;
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ConfigManager.getInstance().updateSystemNewsHintState(false);
		setContentView(R.layout.activity_system_news_list);
		Statistics.sendOnceStatistics(GoogleConfigDefine.FCM_SYSTEM, GoogleConfigDefine.FCM_SYSTEM_LIST);
		initView();
		initListener();
		initAdapter();
	}

	private void initAdapter() {
		mAdapter = new SystemNewsAdapter(getApplicationContext());
		mAdapter.setAllItemCheckedListener(this);
		mAdapter.setItemCheckedListener(this);
		mAdapter.setItemLongClickListener(this);
		mListView.setAdapter(mAdapter);
		ThreadManager.getIOHandler().post(new Runnable() {
			@Override
			public void run() {
				try {
					List<SystemNews> systemNewses = requestAllData();
					if (systemNewses != null && systemNewses.size() > 0) {
						Message message = handler.obtainMessage(ADD);
						message.obj = systemNewses;
						message.sendToTarget();
					} else {
						Message message = handler.obtainMessage(Empty);
						message.obj = systemNewses;
						message.sendToTarget();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					Message message = handler.obtainMessage(ERROR);
					message.obj = e;
					message.sendToTarget();
				}
			}
		});
	}

	private void initListener() {
		mTvRead.setOnClickListener(this);
		//mBottomEditLayout.setOnClickListener(this);
		editTv.setOnClickListener(this);
		checkAllTV.setOnClickListener(this);
		mCheckBox.setOnClickListener(this);
		markAsRead.setOnClickListener(this);
		mDelete.setOnClickListener(this);
	}

	private void initView() {
		mListView = ((ListView) findViewById(R.id.list));
		//mTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
		mBottomBar = (LinearLayout) findViewById(R.id.bottom_bar);
		mTvRead = (TextView) findViewById(R.id.tv_read);
		mListEmptyView = (RelativeLayout) findViewById(R.id.list_empty);
		mBottom = (LinearLayout) findViewById(R.id.bottom);
		//mBtnEdit = (TextView) mTitleBar.findViewById(R.id.common_tv_setting);
		//mBtnEdit.setEnabled(false);
		editTv = (TextView)findViewById(R.id.btn_edit);
		/*editTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mBottomEditLayout.setVisibility(View.GONE);
				mBottomBar.setVisibility(View.VISIBLE);
			}
		});*/

		//mTitleBar.setSettingTxt(R.string.edit);
		/*mTitleBar.setOnSettingListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleEditable();
			}
		});*/

		checkAllTV = ((TextView) findViewById(R.id.tv_check_all));
		/*checkAllTV.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCheckBox.performClick();
			}
		});*/
		mCheckBox = ((CommonCheckBox1) findViewById(R.id.common_check));
		/*mCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCheckBox.toggle();
				mAdapter.toggleAllSelection();
			}
		});*/
		markAsRead = (TextView) findViewById(R.id.mark_as_read);
		/*markAsRead.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				markAsRead.setEnabled(false);
				mAdapter.markSelectionAsRead(new SystemNewsAdapter.Callback() {
					@Override
					public void onSuccess() {
						asyncNotifyDataSetChanged();
					}

					@Override
					public void onError(SQLException e) {
						asyncNotifyDataSetChanged();
					}
				});
			}
		});*/
		mDelete = ((TextView) findViewById(R.id.btn_delete));
		mDelete.setEnabled(false);
		/*mDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDelete.setEnabled(false);
				if (editable) {
					mAdapter.deleteSelection(new SystemNewsAdapter.Callback() {
						@Override
						public void onSuccess() {
							if (mAdapter.getCount() == 0) {
								ThreadManager.postTaskToUIHandler(new Runnable() {
									@Override
									public void run() {
										toggleEditable();
										mAdapter.notifyDataSetChanged();
										markAsRead.setEnabled(false);
										mCheckBox.setChecked(false);
										mDelete.setEnabled(false);
									}
								});
							}
						}

						@Override
						public void onError(SQLException e) {
							asyncNotifyDataSetChanged();
						}
					});
				}
			}
		});*/
//		api = SystemNewsApi.getInstance();

	}

	private void asyncNotifyDataSetChanged() {
		ThreadManager.getUIHandler().post(new Runnable() {
			@Override
			public void run() {
				mAdapter.setEditable(false);
				mCheckBox.setChecked(false);
				mDelete.setEnabled(false);

			}
		});
	}

	private List<SystemNews>  requestData(int page) throws SQLException {
		List<SystemNews> systemNewsList = SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).querySystemNewsList(LIMIT, page);
		return systemNewsList;
	}

	private List<SystemNews> requestAllData() throws SQLException {
		List<SystemNews> systemNewsList = SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).queryAllSystemNews();
		return systemNewsList;
	}

	/**
	 * 更改可编辑性
	 */
	private void toggleEditable() {
		editable = !editable;
		//if (editable) {
			//mTitleBar.setSettingTxt(R.string.complete);
			mBottom.setVisibility(View.GONE);
			mBottomBar.setVisibility(View.VISIBLE);
		/*} else {
			//mTitleBar.setSettingTxt(R.string.edit);
			//mBtnEdit.setEnabled(mAdapter.getCount() != 0);
			mBottomBar.setVisibility(View.GONE);
			mAdapter.setChecked2All(false);
			mTvRead.setEnabled(false);
			mCheckBox.setChecked(false);
			mDelete.setEnabled(false);
		}*/
		mAdapter.setEditable(editable);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onAllItemChecked(boolean check) {
		if (editable) {
			mCheckBox.setChecked(check);
			markAsRead.setEnabled(check);
			mDelete.setEnabled(check);
		}
	}

	@Override
	public void onItemChecked(boolean anyItemChecked) {
		if (editable) {
			markAsRead.setEnabled(anyItemChecked);
			mDelete.setEnabled(anyItemChecked);
		}
	}

	@Override
	public void onItemLongClick(int position) {
		if (!editable) {
			toggleEditable();
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_edit:
				toggleEditable();
				break;
			case R.id.tv_read:
				mAdapter.markSelectionAsRead(new SystemNewsAdapter.Callback() {
					@Override
					public void onSuccess() {
						asyncNotifyDataSetChanged();
					}

					@Override
					public void onError(SQLException e) {
					}
				});
				break;

			case R.id.btn_delete:
				mDelete.setEnabled(false);
				if (editable) {
					mAdapter.deleteSelection(new SystemNewsAdapter.Callback() {
						@Override
						public void onSuccess() {
							if (mAdapter.getCount() == 0) {
								ThreadManager.postTaskToUIHandler(new Runnable() {
									@Override
									public void run() {
										mBottomBar.setVisibility(View.GONE);
										mBottom.setVisibility(View.VISIBLE);
										mCheckBox.setChecked(false);
										mDelete.setEnabled(false);
										editTv.setEnabled(false);
										mTvRead.setEnabled(false);
										mAdapter.setChecked2All(false);
										mAdapter.setEditable(editable);
									}
								});
							}
						}

						@Override
						public void onError(SQLException e) {
							asyncNotifyDataSetChanged();
						}
					});
				}
				break;
			case R.id.mark_as_read:
				if(!mDelete.isEnabled()){
					mBottomBar.setVisibility(View.GONE);
					mBottom.setVisibility(View.VISIBLE);
					mAdapter.setEditable(false);
					editable = false;
					return;
				}
				mAdapter.markSelectionAsRead(new SystemNewsAdapter.Callback() {
					@Override
					public void onSuccess() {
						asyncNotifyDataSetChanged();
						editable = false;
						mAdapter.setEditable(editable);
						mAdapter.setChecked2All(false);
					}

					@Override
					public void onError(SQLException e) {
						asyncNotifyDataSetChanged();
					}
				});
				mBottomBar.setVisibility(View.GONE);
				mBottom.setVisibility(View.VISIBLE);
				break;
			case R.id.tv_check_all:
				mCheckBox.performClick();
				break;
			case R.id.common_check:
				mCheckBox.toggle();
				mAdapter.toggleAllSelection();
				break;
			default:
				break;
		}
	}
}
