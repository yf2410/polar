package com.polar.browser.common.ui;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.R;

/**
 * 文本列表对话框
 */
public class ListDialog extends CommonBaseDialog implements OnItemClickListener {

	protected String[] mItems;

	protected ListView mListView;

	protected DialogListAdatper mAdapter;

	protected OnItemClickListener mItemClicklistener;

	protected boolean mSingleLine = false;

	protected TruncateAt mEllipsize = null;

	protected int mSelectedIndex;

	public ListDialog(Context context) {
		super(context, R.style.common_dialog);
		initCenterView();
	}

	public void setSingleLine(boolean singleLine) {
		mSingleLine = singleLine;
	}

	public void setEllipsize(TruncateAt where) {
		mEllipsize = where;
	}

	protected void initCenterView() {
		mListView = new ListView(getContext());
//        mListView.setDivider(getContext().getResources().getDrawable(R.color.common_grey_color1));
//        mListView.setDividerHeight((int) getContext().getResources().getDimension(R.dimen.common_divider_width));
		// 12-05; fix bug 1834
		mListView.setDivider(null);
		mListView.setSelector(R.drawable.empty_selector);
		mAdapter = new DialogListAdatper();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		setContentView(mListView);
	}

	public ListView getListView() {
		return mListView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mItemClicklistener != null) {
			mItemClicklistener.onItemClick(parent, view, position, id);
		}
		mSelectedIndex = position;
		dismiss();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mItemClicklistener = listener;
	}

	public void setItems(String[] items, int selectedIndex) {
		mItems = items;
		mSelectedIndex = selectedIndex;
		mAdapter.notifyDataSetChanged();
	}

	protected View getListRowView(int position, View convertView, ViewGroup parent) {
		convertView = getLayoutInflater().inflate(R.layout.common_dialog_list_row, null);
		// 设置背景色，根据mItems.length
		View root = convertView.findViewById(R.id.root);
		View line = convertView.findViewById(R.id.line);
		if (mItems.length == 1) {
			// case 1 ,只有1条目
			root.setBackgroundResource(R.drawable.list_row_one_selector);
			line.setVisibility(View.GONE);
		} else if (mItems.length == 2) {
			// case 2 ,只有2条目
			if (position == 0) {
				root.setBackgroundResource(R.drawable.list_row_top_selector);
				line.setVisibility(View.VISIBLE);
			} else if (position == 1) {
				root.setBackgroundResource(R.drawable.list_row_bottom_selector);
				line.setVisibility(View.GONE);
			}
		} else {
			// 条目>=3
			if (position == 0) {
				root.setBackgroundResource(R.drawable.list_row_top_selector);
				line.setVisibility(View.VISIBLE);
			} else if (position == mItems.length - 1) {
				root.setBackgroundResource(R.drawable.list_row_bottom_selector);
				line.setVisibility(View.INVISIBLE);
			} else {
				root.setBackgroundResource(R.drawable.common_list_row1);
				line.setVisibility(View.VISIBLE);
			}
		}
		TextView title = (TextView) convertView.findViewById(R.id.tv_title);
		title.setText(mItems[position]);
		if (mSelectedIndex == position) {
			convertView.findViewById(R.id.iv_check).setVisibility(View.VISIBLE);
		} else {
			convertView.findViewById(R.id.iv_check).setVisibility(View.INVISIBLE);
		}
		((ImageView) convertView.findViewById(R.id.iv_check)).setImageResource(R.drawable.list_check);
		title.setTextColor(convertView.getResources().getColor(R.color.common_font_color_selector_2));
		return convertView;
	}

	class DialogListAdatper extends BaseAdapter {

		@Override
		public int getCount() {
			return mItems == null ? 0 : mItems.length;
		}

		@Override
		public Object getItem(int position) {
			return mItems == null ? null : mItems[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getListRowView(position, convertView, parent);
		}
	}
}
