package com.polar.browser.bookmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.polar.browser.R;
import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.i.IEditStateObserver;

public class BookmarkListAdapter extends JZBaseAdapter<BookmarkInfo> {

	private boolean mIsEditing;
	private ListView mList;
	private IBookmarkItemClick mItemClick;
	private IEditStateObserver mEditObserver;

	public BookmarkListAdapter(Context context, ListView list, IBookmarkItemClick itemClick, IEditStateObserver editObserver) {
		super(context);
		mIsEditing = false;
		mList = list;
		mItemClick = itemClick;
		mEditObserver = editObserver;
	}

	@Override
	public View newView(Context context, BookmarkInfo data, ViewGroup parent,
						int type) {
		BookmarkItem itemView = (BookmarkItem) LayoutInflater.from(context).inflate(R.layout.item_relative_bookmark, null);
		return itemView;
	}

	@Override
	public void bindView(View view, int position, BookmarkInfo data) {
		BookmarkItem itemView = ((BookmarkItem) view);
		itemView.bindList(mList, position);
		itemView.setClickDelegate(mItemClick);
		itemView.setEditStateObserver(mEditObserver);
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
		for (int i = 0; i < mData.size(); i++) {
			mData.get(i).isChecked = checked;
		}
		notifyDataSetChanged();
	}

	/**
	 * 选中某一个条目
	 *
	 * @param position
	 */
	public void setChecked(int position) {
		mData.get(position).isChecked = true;
		notifyDataSetChanged();
	}

	/**
	 * 是否处于全选状态
	 *
	 * @return
	 */
	public boolean isCheckedAll() {
		for (int i = 0; i < mData.size(); i++) {
			if (!mData.get(i).isChecked) {
				return false;
			}
		}
		return true;
	}
}
