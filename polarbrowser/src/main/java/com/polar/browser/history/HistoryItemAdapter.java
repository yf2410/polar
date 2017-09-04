package com.polar.browser.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.polar.browser.R;
import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.i.IEditStateObserver;
import java.util.Date;
import java.util.List;

/**
 * 历史记录Adapter
 *
 * @author dpk
 */
public class HistoryItemAdapter extends JZBaseAdapter<HistoryInfo> implements
		StickyListHeadersAdapter {

	private static final long ID_HEADER_TODAY = 0;
	private static final long ID_HEADER_EARLIER = 2;
	private LayoutInflater mInflater;
	private List<HistoryInfo> mInfoList;
	private Context mContext;
	private IHistoryItemClick mClickDelegate;
	private IEditStateObserver mEditStateObserver;
	private boolean mIsEditing;

	public HistoryItemAdapter(Context context,
							  IEditStateObserver editStateObserver) {
		super(context);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mIsEditing = false;
		mEditStateObserver = editStateObserver;
	}

	public void registerDelegate(IHistoryItemClick delegate) {
		mClickDelegate = delegate;
	}

	public void initData(List<HistoryInfo> infoList) {
		mInfoList = infoList;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.view_history_header,
					parent, false);
			holder.tvHeader = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}
		int day = mInfoList.get(position).timestamp.getDay();
		if (new Date().getDay() == day) {
			holder.tvHeader.setText(mContext.getString(R.string.today));
		} else {
			holder.tvHeader.setText(mContext.getString(R.string.earlier));
		}
		convertView.findViewById(R.id.root_header).setBackgroundResource(R.drawable.history_header_view_item);
		holder.tvHeader.setTextColor(convertView.getResources().getColor(R.color.common_font_color_selector_2));
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		int day = mInfoList.get(position).timestamp.getDay();
		if (new Date().getDay() == day) {
			return ID_HEADER_TODAY;
		} else {
			return ID_HEADER_EARLIER;
		}
	}

	@Override
	public View newView(Context context, HistoryInfo data, ViewGroup parent,
						int type) {
		HistoryItem itemView = new HistoryItem(context);
		itemView.init(mClickDelegate, mEditStateObserver);
		return itemView;
	}

	@Override
	public void bindView(View view, int position, HistoryInfo data) {
		HistoryItem itemView = ((HistoryItem) view);
		itemView.bind(data);
		itemView.changeEditState(mIsEditing);
	}

	/**
	 * 更改条目编辑状态
	 *
	 * @param isEditing true 编辑状态，显示checkBox
	 */
	public void changeEditeState(boolean isEditing) {
		mIsEditing = isEditing;
		setAllChecked(false);
		notifyDataSetChanged();
	}

	/**
	 * 全选or反选
	 *
	 * @param checked
	 */
	public void setAllChecked(boolean checked) {
		if (mData != null) {
			for (int i = 0; i < mData.size(); i++) {
				mData.get(i).isChecked = checked;
			}
			notifyDataSetChanged();
		}
	}

	/**
	 * 是否处于全选状态
	 *
	 * @return
	 */
	public boolean isCheckedAll() {
		if(mData == null) return false;
		for (int i = 0; i < mData.size(); i++) {
			if (!mData.get(i).isChecked) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 是否处于全选状态
	 *
	 * @return
	 */
	public boolean hasCheckedItem() {
		if(mData == null) return false;
		for (int i = 0; i < mData.size(); i++) {
			if (mData.get(i).isChecked) {
				return true;
			}
		}
		return false;
	}

	public void onItemClick(View v) {
		HistoryItem item = (HistoryItem) v;
		item.onClick();
	}

	public void onItemLongClick(View v) {
		HistoryItem item = (HistoryItem) v;
		item.onLongClick();
	}

	class HeaderViewHolder {
		TextView tvHeader;
	}
}
