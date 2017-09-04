package com.polar.browser.setting;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.bean.LoginAccountInfo;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.loginassistant.LoginDBHelper;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 保存账号适配器
 *
 * @author James
 */
public class SaveAccountAdapter extends JZBaseAdapter<LoginAccountInfo> {
	private static final String TAG = "SaveAccountAdapter";
	private AllItemCheckedListener allItemCheckedListener;
	private ItemCheckedListener itemCheckedListener;
	private boolean editable;
	private ArrayList<Boolean> selections = new ArrayList<>();
	private LoginDBHelper loginDBHelper;
	public SaveAccountAdapter(Context context) {
		super(context);
		loginDBHelper = LoginDBHelper.getInstance();
		loginDBHelper.init(mContext);
		selections.clear();
	}

	@Override
	public View newView(Context context, LoginAccountInfo data,
						ViewGroup parent, int type) {
		View itemView = LayoutInflater.from(context).inflate(
				R.layout.item_save_account, null);
		return itemView;
	}

	@Override
	public void bindView(View view, final int position, LoginAccountInfo data) {
		final CommonCheckBox1 checkBox1 = (CommonCheckBox1) view
				.findViewById(R.id.common_check);
		TextView hostTextView = (TextView) view.findViewById(R.id.host);
		TextView usernameTextView = (TextView) view.findViewById(R.id.username);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				checkBox1.performClick();
			}
		});
		if (isEditable()) {
			checkBox1.setVisibility(View.VISIBLE);
			boolean checked = getBooleanValue(selections.get(position));
			checkBox1.setChecked(checked);
		} else {
			checkBox1.setVisibility(View.GONE);
		}
		checkBox1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editable) {
					boolean checked = !(getBooleanValue(selections
							.get(position)));
					selections.set(position, checked);
					checkBox1.setChecked(checked);
					if (allItemCheckedListener != null) {
						allItemCheckedListener
								.onAllItemChecked(areAllChecked());
					}
					if (itemCheckedListener != null) {
						itemCheckedListener.onItemChecked(isAnyChecked());
					}
				}
			}
		});
		hostTextView.setText(data.getUrl());
		String username = data.getUsername();
		usernameTextView.setText(TextUtils.isEmpty(username) ? "" : username);
	}

	protected boolean isAnyChecked() {
		for (int i = 0; i < mData.size(); i++) {
			if (getBooleanValue(selections.get(i))) {
				return true;
			}
		}
		return false;
	}

	private boolean getBooleanValue(Boolean value) {
		return value != null ? value : false;
	}

	/**
	 * 添加数据时,同步更新被选择的状态,初始状态为未选中,用false表示
	 */
	@Override
	public void addAll(List<LoginAccountInfo> data) {
		for (int i = 0; i < data.size(); i++) {
			selections.add(i, false);
		}
		SimpleLog.d(TAG, selections.toString());
		super.addAll(data);
	}

	/**
	 * 选择全部/取消全选
	 */
	public void applySelection2All(boolean select) {
		if (!isEditable()) {
			return;
		}
		for (int i = 0; i < mData.size(); i++) {
			selections.set(i, select);
		}
		itemCheckedListener.onItemChecked(select);
		allItemCheckedListener.onAllItemChecked(select);
		notifyDataSetChanged();
	}

	/**
	 * 查询可编辑状态
	 *
	 * @return
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * 设置可编辑状态
	 *
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
		notifyDataSetChanged();
	}

	public void deleteSelection() {
		int size = selections.size();
		for (int i = size - 1; i >= 0; i--) {
			if (getBooleanValue(selections.get(i))) {
				deleteData(mData.get(i).getUrl());
				mData.remove(i);
				selections.remove(i);
			}
		}
		notifyDataSetChanged();
	}

	private void deleteData(final String url) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				loginDBHelper.deleteAccountByUrl(url);
			}
		};
		ThreadManager.postTaskToIOHandler(runnable);
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

	public AllItemCheckedListener getAllItemCheckedListener() {
		return allItemCheckedListener;
	}

	public void setAllItemCheckedListener(
			AllItemCheckedListener allItemCheckedListener) {
		this.allItemCheckedListener = allItemCheckedListener;
	}

	public ItemCheckedListener getItemCheckedListener() {
		return itemCheckedListener;
	}

	public void setItemCheckedListener(ItemCheckedListener itemCheckedListener) {
		this.itemCheckedListener = itemCheckedListener;
	}

	public interface AllItemCheckedListener {
		void onAllItemChecked(boolean allItemChecked);
	}

	public interface ItemCheckedListener {
		void onItemChecked(boolean anyItemChecked);
	}

	public interface DeleteItemListener {
		void onItemDelete(LoginAccountInfo loginAccountInfo);
	}
}
