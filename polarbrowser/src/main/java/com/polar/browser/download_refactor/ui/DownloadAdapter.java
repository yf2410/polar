package com.polar.browser.download_refactor.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.download.download.DownloadItem;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.utils.SimpleLog;

import java.util.HashMap;
import java.util.Map;

public class DownloadAdapter extends JZBaseAdapter<DownloadItemInfo>{
	
	private Map<Long, DownloadItem> mChildMap = new HashMap<Long, DownloadItem>();
	
	public DownloadAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(Context context, DownloadItemInfo data, ViewGroup parent, int type) {
		SimpleLog.e("DownloadAdapter", "-- ---- --- newView");
		return new DownloadItem(context);
	}

	@Override
	public void bindView(View view, int position, DownloadItemInfo data) {
		DownloadItem item = (DownloadItem) view;
		item.bind(data);
		SimpleLog.e("DownloadAdapter", "--> bindView");
		mChildMap.put(data.mId, item);
	}

	/**
	 * 根据id拿到DownloadItem
	 * @param id
	 * @return
	 */
	public DownloadItem getDownloadItem(long id) {
		return mChildMap.get(id);
	}
	
	/**
	 * 根据id拿到DownloadItemInfo
	 * @param id
	 * @return
	 */
	public DownloadItemInfo getDownloadItemInfo(long id) {
		if (getCount() <= 0) {
			return null;
		}
		DownloadItemInfo info;
		for (int i = 0; i < getCount(); i++) {
			info = getData().get(i);
			if(info.mId == id) {
				return info;
			}
		}
		return null;
	}
	
	/**
	 * 更改条目编辑状态
	 * @param isEditing true 编辑状态，显示checkBox
	 */
	public void changeEditeState(boolean isEditing){
		if (getCount() <= 0) {
			return;
		}
		DownloadItemInfo info;
		for (int i = 0; i < getCount(); i++) {
			info = getData().get(i);
			info.isEditing = isEditing;
		}
		notifyDataSetChanged();
	}
	
	/**
	 * 全选or反选
	 * @param checked
	 */
	public void setAllChecked(boolean isChecked){
		if (getCount() <= 0) {
			return;
		}
		DownloadItemInfo info;
		for (int i = 0; i < getCount(); i++) {
			info = getData().get(i);
			info.isChecked = isChecked;
		}
		notifyDataSetChanged();
	}

}
