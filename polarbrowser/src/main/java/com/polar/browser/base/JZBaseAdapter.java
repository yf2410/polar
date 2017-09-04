package com.polar.browser.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Adapter
 *
 * @param <T>
 */

public abstract class JZBaseAdapter<T> extends BaseAdapter {
	protected Context mContext;
	protected List<T> mData;
	protected boolean mDataValid;

	public JZBaseAdapter(Context context) {
		mContext = context;
		mDataValid = false;
	}

	public void addAll(List<T> data) {
		if (data != null) {
			mDataValid = true;
			if (mData != null) {
				mData.clear();
				mData.addAll(data);
			} else {
				mData = data;
			}
			notifyDataSetChanged();
		} else {
			mDataValid = false;
			notifyDataSetInvalidated();
		}
	}

	/**
	 * 根据索引移除对应数据，并刷新列表数据
	 * 
	 * @author FKQ
	 * @time 2016/11/3 17:32
	 */
	public void remove(int position) {
		if (mData != null) {
			mData.remove(position);
			notifyDataSetChanged();
		}
	}

	public void updateData(List<T> data) {
		if (data != null) {
			mDataValid = true;
			mData = data;
			notifyDataSetChanged();
		} else {
			mDataValid = false;
			notifyDataSetInvalidated();
		}
	}

	public List<T> getData() {
		return mData;
	}

	@Override
	public int getCount() {
		if (mDataValid && mData != null) {
			return mData.size();
		} else {
			return 0;
		}
	}

	@Override
	public T getItem(int position) {
		if (mDataValid && mData != null) {
			return mData.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		if (mDataValid && mData != null) {
			return position;
		} else {
			return 0;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (!mDataValid) {
			throw new IllegalStateException("this should only be called when the data is valid");
		}
		if (position < 0 || position >= mData.size()) {
			throw new IllegalStateException("couldn't get view at this position " + position);
		}
		T data = mData.get(position);
		View v;
		if (convertView == null) {
			v = newView(mContext, data, parent, getItemViewType(position));
		} else {
			v = convertView;
		}
		bindView(v, position, data);
		return v;
	}

	public abstract View newView(Context context, T data, ViewGroup parent, int type);

	public abstract void bindView(View view, int position, T data);
}
