package com.polar.browser.download.savedpage;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.base.JZBaseAdapter;

public class SavedPageAdapter extends JZBaseAdapter<SavedPageNode> {

	private boolean isEditing;

	public SavedPageAdapter(Context context) {
		super(context);
	}

	@Override
	public View newView(Context context, SavedPageNode data, ViewGroup parent,
						int type) {
		return new SavedPageItem(context);
	}

	@Override
	public void bindView(View view, int position, SavedPageNode data) {
		SavedPageItem item = (SavedPageItem) view;
		item.isEditing = isEditing;
		item.bind(data);
	}

	/**
	 * 更改条目编辑状态
	 *
	 * @param isEditing true 编辑状态，显示checkBox
	 */
	public void changeEditeState(boolean isEditing) {
		this.isEditing = isEditing;
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
}
