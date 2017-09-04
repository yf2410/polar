package com.polar.browser.download.savedpage;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonBottomBar3;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.utils.CustomToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 离线保存网页的Activity
 *
 * @author dpk
 */
public class SavedPageActivity extends LemonBaseActivity implements OnClickListener {

	/**
	 * 离线保存网页列表
	 **/
	private ListView mListSavedPage;

	private SavedPageAdapter mAdapter;

	private List<SavedPageNode> mDataList;

	/**
	 * titleBar
	 **/
	private CommonTitleBar mTitleBar;

	/**
	 * 编辑状态下的底部操作按钮
	 **/
	private CommonBottomBar3 mEditBar;

	/**
	 * 全选
	 **/
	private TextView mBtnCheckAll;
	/**
	 * 删除
	 **/
	private TextView mBtnDelete;

	private TextView mBtnEdit;

	private View mViewEmpty;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saved_page);
		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
	}


	private void init() {
		initView();
		initData();
		initListener();
	}

	private void initView() {
		mListSavedPage = (ListView) findViewById(R.id.lv_savedpage);
		mTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
		mTitleBar.setSettingVisible(true);
		mTitleBar.setSettingTxt(R.string.edit);
		mTitleBar.setOnButtonListener(this);
		mBtnEdit = (TextView) mTitleBar.findViewById(R.id.common_tv_setting);
		mEditBar = (CommonBottomBar3) findViewById(R.id.edit_bar);
		mBtnCheckAll = mEditBar.getCheckAllBtn();
		mBtnCheckAll.setText(R.string.check_all);
		mBtnDelete = mEditBar.getDeleteBtn();
		mBtnDelete.setText(R.string.delete);
		mViewEmpty = findViewById(R.id.view_empty);
	}

	private void initListener() {
		mBtnEdit.setOnClickListener(this);
		mBtnCheckAll.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);
	}

	private void initData() {
		mAdapter = new SavedPageAdapter(this);
		mListSavedPage.setAdapter(mAdapter);
		mDataList = SavedPageUtil.getSavedPageList(this);
		mAdapter.updateData(mDataList);
		if (mDataList.size() == 0) {
			mBtnEdit.setEnabled(false);
			mViewEmpty.setVisibility(View.VISIBLE);
		} else {
			mBtnEdit.setEnabled(true);
			mViewEmpty.setVisibility(View.GONE);
		}
		changeEditState(false);
	}

	/**
	 * 刷新列表
	 */
	public void notifyDataSetChanged() {
		mAdapter.updateData(mDataList);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.common_img_back:
				// 左上角返回
				finish();
				break;
			case R.id.common_tv_setting:
				// 右上角编辑
				changeEditState(!mEditBar.isShown());
				break;
			case R.id.tv_check_all:
				// 全选
				onCheckAllButtonClick();
				break;
			case R.id.btn_delete:
				// 删除
				onDeleteButtonClick();
				break;
			default:
				break;
		}
	}

	public void updateCheckAll() {
		boolean isCheckAll = isCheckedAll();
		if (isCheckAll) {
//			mBtnCheckAll.setText(R.string.check_all_cancel);
		} else {
//			mBtnCheckAll.setText(R.string.check_all);
		}
		mEditBar.setCheckAll(isCheckAll);
	}

	public void updateDeleteBtnState() {
		boolean hasItemChecked = hasItemChecked();
		if (hasItemChecked) {
			mBtnDelete.setEnabled(true);
		} else {
			mBtnDelete.setEnabled(false);
		}
	}

	public void onCheckAllButtonClick() {
		boolean isCheckAll = !isCheckedAll();
		if (isCheckAll) {
//			mBtnCheckAll.setText(R.string.check_all_cancel);
		} else {
			mBtnCheckAll.setText(R.string.check_all);
		}
		checkAll(isCheckAll);
	}

	private boolean isCheckedAll() {
		boolean isCheckedAll = true;
		for (int i = 0; i < mDataList.size(); ++i) {
			isCheckedAll = mDataList.get(i).isChecked;
			if (!isCheckedAll) {
				break;
			}
		}
		return isCheckedAll;
	}

	private void checkAll(boolean isCheckAll) {
		mAdapter.setAllChecked(isCheckAll);
		if (isCheckAll) {
			mBtnDelete.setEnabled(true);
		} else {
			mBtnDelete.setEnabled(false);
		}
	}

	private void onDeleteButtonClick() {
		List<File> fileList = new ArrayList<File>();
		if (!hasItemChecked()) {
			CustomToastUtils.getInstance().showTextToast(R.string.delete_not_select);
		} else {
			for (int i = 0; i < mAdapter.getData().size(); ++i) {
				if (mAdapter.getData().get(i).isChecked) {
					fileList.add(mAdapter.getData().get(i).file);
				}
			}
			showDeleteDialog(fileList);
		}
	}

	private boolean hasItemChecked() {
		boolean ret = false;
		for (int i = 0; i < mAdapter.getData().size(); ++i) {
			ret = mAdapter.getData().get(i).isChecked;
			if (ret) {
				return ret;
			}
		}
		return ret;
	}

	private void showDeleteDialog(final List<File> fileList) {
		final CommonDialog dialog = new CommonDialog(this, getString(R.string.tips), getString(
				R.string.offline_web_delete_content));
		dialog.setBtnCancel(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setBtnOk(getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				for (File file : fileList) {
					SavedPageUtil.deleteSavedPage(file);
				}
				initData();
			}
		});
		dialog.show();
	}

	public void changeEditState(boolean isEditState) {
		if (isEditState) {
			mEditBar.setVisibility(View.VISIBLE);
			mBtnEdit.setText(R.string.complete);
		} else {
			mEditBar.setVisibility(View.GONE);
			mBtnEdit.setText(R.string.edit);
		}
		mAdapter.changeEditeState(isEditState);
		checkAll(false);
	}
}
