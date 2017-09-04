package com.polar.browser.push;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.api.RequestAPI;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.DateUtils;
import com.polar.browser.vclibrary.bean.db.SystemNews;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.SystemNewsApi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by James on 2016/7/13.
 */
public class SystemNewsAdapter extends BaseAdapter {
	private final Context context;
	private final List<SystemNews> mData = new ArrayList<>();
//	private SystemNewsApi api;
	private ArrayList<Boolean> selections = new ArrayList<>();
	private ItemCheckedListener itemCheckedListener;
	private AllItemCheckedListener allItemCheckedListener;
	private boolean editable;
	private ItemLongClickListener itemLongClickListener;

	public SystemNewsAdapter(Context context) {
		this.context = context;
//		this.api = api;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.item_system_news, null);
			holder.title = (TextView) convertView.findViewById(R.id.titleTV);
			holder.time = (TextView) convertView.findViewById(R.id.timeTV);
			holder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
			holder.checkBox = (CommonCheckBox1) convertView.findViewById(R.id.check_box);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final View finalConvertView = convertView;
		final SystemNews systemNews = mData.get(position);
//		convertView.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				if (!editable) {
//					selections.set(position, !getBooleanValue(selections.get(position)));
//					holder.checkBox.setChecked(selections.get(position));
//				}
//				itemLongClickListener.onItemLongClick(position);
//				return true;
//			}
//		});
		if (systemNews.isRead()) {
			holder.imageView.setImageResource(R.drawable.push_system_tip_open);
		} else {
			holder.imageView.setImageResource(R.drawable.push_system_tip);
		}
		if (editable) {
			holder.checkBox.setVisibility(View.VISIBLE);
		} else {
			holder.checkBox.setVisibility(View.GONE);
		}
		holder.checkBox.setChecked(selections.get(position));
		holder.checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finalConvertView.performClick();
			}
		});
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editable) {
					toggle(position);
				} else {
					if (systemNews.getContentURL().contains(RequestAPI.LOAD_PRODUCT_HELP)) {
						Intent intent = new Intent(context, BrowserActivity.class);
						intent.setAction(CommonData.ACTION_OPEN_PRODUCT_ABOUT);
						intent.putExtra(CommonData.SYSTEM_CONTENT_URL, systemNews.getContentURL());
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
//						TabViewManager tabViewManager = TabViewManager.getInstance();
//						if (tabViewManager == null) {
//							return;
//						}
//						if (!tabViewManager.isCurrentHome()) {
//							tabViewManager.addTabView(true);
//							tabViewManager.showContentHideHome();
//						}
//						String loadHelpUrl = Statistics.getLoadHelpUrl();
//						tabViewManager.loadUrl(loadHelpUrl, Constants.NAVIGATESOURCE_PRODUCT_ABOUT);

					} else {
						Intent intent = new Intent(context, loadWebDetailsActivity.class);
						intent.setAction(CommonData.ACTION_OPEN_SYSTEMNEWS_DATA);
						intent.putExtra(CommonData.SYSTEM_CONTENT_URL, systemNews.getContentURL());
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intent);
					}
					systemNews.setRead(true);
					Statistics.sendOnceStatistics(GoogleConfigDefine.FCM_SYSTEM_CLICK, systemNews.getContentURL());
					ThreadManager.postTaskToIOHandler(new Runnable() {
						@Override
						public void run() {
							try {
								SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).updateReadState(systemNews);
								ThreadManager.postTaskToUIHandler(new Runnable() {
									@Override
									public void run() {
										notifyDataSetChanged();
									}
								});
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
		holder.title.setText(systemNews.getTitle());
		Date receivedTime = systemNews.getReceivedTime();
		String dateDiff = DateUtils.getDateDiff(receivedTime, new Date());
		holder.time.setText(dateDiff);
		return convertView;
	}

	public void add(List<SystemNews> systemNewses) {
		int size = systemNewses.size();
		if (systemNewses != null && size > 0) {
			mData.addAll(systemNewses);
			for (int i = 0; i < size; i++) {
				selections.add(false);
			}
			notifyDataSetChanged();
		}
	}

	public void delete(int position) {
		mData.remove(position);
		selections.remove(position);
		notifyDataSetChanged();
	}

	/**
	 * 反选
	 *
	 * @param position
	 */
	public void toggle(int position) {
		selections.set(position, !getBooleanValue(selections.get(position)));
		if (allItemCheckedListener != null) {
			allItemCheckedListener.onAllItemChecked(areAllChecked());
		}
		if (itemCheckedListener != null) {
			itemCheckedListener.onItemChecked(isAnyChecked());
		}
		notifyDataSetChanged();
	}

	private boolean getBooleanValue(Boolean value) {
		return value != null ? value : false;
	}

	public void setChecked2All(boolean select) {
		int size = selections.size();
		for (int i = 0; i < size; i++) {
			selections.set(i, select);
		}
		if (allItemCheckedListener != null) {
			allItemCheckedListener.onAllItemChecked(select);
		}
		if (itemCheckedListener != null) {
			itemCheckedListener.onItemChecked(select);
		}
		notifyDataSetChanged();
	}

	protected boolean isAnyChecked() {
		for (int i = 0; i < mData.size(); i++) {
			if (getBooleanValue(selections.get(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否处于全选状态
	 *
	 * @return
	 */
	public boolean areAllChecked() {
		for (int i = 0; i < mData.size(); i++) {
			if (!getBooleanValue(selections.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * toggle全选状态
	 */
	public void toggleAllSelection() {
		setChecked2All(!areAllChecked());
	}

	public void setAllItemCheckedListener(AllItemCheckedListener allItemCheckedListener) {
		this.allItemCheckedListener = allItemCheckedListener;
	}

	public void setItemCheckedListener(ItemCheckedListener itemCheckedListener) {
		this.itemCheckedListener = itemCheckedListener;
	}

	public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
		this.itemLongClickListener = itemLongClickListener;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		notifyDataSetChanged();
	}

	public void deleteSelection(final Callback callback) {
		ThreadManager.getIOHandler().post(new Runnable() {
			@Override
			public void run() {
				int size = selections.size();
				for (int i = size - 1; i >= 0; i--) {
					if (getBooleanValue(selections.get(i))) {
						try {
							SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).delete(mData.get(i));
							mData.remove(i);
							selections.remove(i);
						} catch (SQLException e) {
							e.printStackTrace();
							callback.onError(e);
						}
					}
				}
				callback.onSuccess();
			}
		});
	}

	public void markSelectionAsRead(final Callback callback) {
		ThreadManager.getIOHandler().post(new Runnable() {
			@Override
			public void run() {
				int size = selections.size();
				for (int i = size - 1; i >= 0; i--) {
					if (!getBooleanValue(selections.get(i))) {
						try {
							SystemNews systemNews = mData.get(i);
							if (!systemNews.isRead()) {
								systemNews.setRead(true);
								SystemNewsApi.getInstance(CustomOpenHelper.getInstance(JuziApp.getAppContext())).updateReadState(systemNews);
							} else {
								//pass
							}
							selections.set(i, getBooleanValue(selections.get(i)));
						} catch (SQLException e) {
							e.printStackTrace();
							callback.onError(e);
						}
					}
				}
				callback.onSuccess();
			}
		});
	}

	public interface AllItemCheckedListener {
		void onAllItemChecked(boolean check);
	}

	public interface ItemCheckedListener {
		void onItemChecked(boolean check);
	}

	public interface ItemLongClickListener {
		void onItemLongClick(int position);
	}

	public interface Callback {
		void onSuccess();

		void onError(SQLException e);
	}

	class ViewHolder {
		public CommonCheckBox1 checkBox;
		public ImageView imageView;
		public TextView title;
		public TextView time;
	}
}
