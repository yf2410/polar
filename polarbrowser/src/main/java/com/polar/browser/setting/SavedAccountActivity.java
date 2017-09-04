package com.polar.browser.setting;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bean.LoginAccountInfo;
import com.polar.browser.common.ui.CommonBottomBar3;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.loginassistant.LoginDBHelper;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.setting.SaveAccountAdapter.AllItemCheckedListener;
import com.polar.browser.setting.SaveAccountAdapter.ItemCheckedListener;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.view.switchbutton.SwitchButton;

import java.util.List;

/**
 * 设置-保存账号和密码页面 功能:
 * <p/>
 * 1.设置开关<br/>
 * 2.显示已保存的网站/host和username列表 3.编辑:多选/全选后删除
 *
 * @author James
 */
public class SavedAccountActivity extends LemonBaseActivity implements
		AllItemCheckedListener, OnItemLongClickListener, ItemCheckedListener {

	private String TAG = "SavedAccountActivity";
	private boolean editable;
	private CommonTitleBar mTitleBar;
	private List<LoginAccountInfo> queryLoginAccountList;
	private SaveAccountAdapter mAdapter;
	private CommonBottomBar3 mBottomBar;
	private boolean checked;
	private LoginDBHelper loginDBHelper;
	private View mEditLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saved_account);
		// 标题
		mTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
//		mTitleBar.setSettingTxt(R.string.edit);
//		mTitleBar.setOnSettingListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//
//			}
//		});
		// 开关
		SwitchButton switchButton = (SwitchButton) findViewById(R.id.sb_save_account);
		boolean enableSaveAccount = ConfigManager.getInstance()
				.isEnableSaveAccount();
		SimpleLog.i(TAG, "[account]enableSaveAccount is " + enableSaveAccount);
		switchButton.setChecked(enableSaveAccount);
		switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				ConfigManager.getInstance().setEnableSaveAccount(isChecked);
			}
		});
		// 空白
		// RelativeLayout emptyImageView = (RelativeLayout)
		// findViewById(R.id.view_empty);
		// 底部操作栏 全选 删除
		mBottomBar = (CommonBottomBar3) findViewById(R.id.bottom_bar);
		mBottomBar.setDeleteBtnEnabled(false);
		mBottomBar.getDeleteBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editable) {
					mAdapter.deleteSelection();
					if (queryLoginAccountList != null
							&& queryLoginAccountList.size() == 0) {
						changeEditable();
//						mTitleBar.setSettingVisible(false);
					 	mEditLayout.setVisibility(View.GONE);
					}
				}
			}
		});
		mBottomBar.getCheckAllBtn().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!editable) {
					return;
				}
				// 默认不选中,checked=false
				checked = !checked;
				mBottomBar.setCheckAll(checked);
				mAdapter.applySelection2All(checked);
			}
		});
		mBottomBar.getTvComplete().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				changeEditable();
			}
		});

		mEditLayout =findViewById(R.id.edit_layout);
		mEditLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				changeEditable();
			}
		});

		loginDBHelper = LoginDBHelper.getInstance();
		loginDBHelper.init(getApplicationContext());
		queryLoginAccountList = loginDBHelper.queryLoginAccount(100, true);
		// adapter
		mAdapter = new SaveAccountAdapter(getApplicationContext());
		mAdapter.setAllItemCheckedListener(this);
		mAdapter.setItemCheckedListener(this);
		// 列表
		ListView listView = (ListView) findViewById(R.id.account_list);
		// TODO 被子view抢占焦点,不会被触发 mark
		listView.setOnItemLongClickListener(this);
		if (queryLoginAccountList != null && queryLoginAccountList.size() > 0) {
			mAdapter.addAll(queryLoginAccountList);
			listView.setAdapter(mAdapter);
			// emptyImageView.setVisibility(View.GONE);
//			mTitleBar.setSettingVisible(true);
			mEditLayout.setVisibility(View.VISIBLE);
		} else {
			// emptyImageView.setVisibility(View.VISIBLE);
//			mTitleBar.setSettingVisible(false);
			mEditLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 更改可编辑性
	 */
	private void changeEditable() {
		editable = !editable;
		if (editable) {
//			mTitleBar.setSettingTxt(R.string.complete);
			mBottomBar.setVisibility(View.VISIBLE);
			mEditLayout.setVisibility(View.GONE);
		} else {
//			mTitleBar.setSettingTxt(R.string.edit);
			mBottomBar.setVisibility(View.GONE);
			mEditLayout.setVisibility(View.VISIBLE);
		}
		mAdapter.applySelection2All(false);
		mAdapter.setEditable(editable);
	}

	/**
	 * 关闭页面时加入动画效果<br/>
	 * 返回键和返回按钮两处关闭都在此处统一处理
	 */
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slide_out_to_right);
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
	}

	@Override
	public void onAllItemChecked(boolean allItemChecked) {
		mBottomBar.setCheckAll(allItemChecked);
		checked = allItemChecked;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
								   int position, long id) {
		SimpleLog.d(TAG, "item long click");
		if (!editable) {
			changeEditable();
			view.performClick();
		}
		return false;
	}

	@Override
	public void onItemChecked(boolean anyItemChecked) {
		mBottomBar.setDeleteBtnEnabled(anyItemChecked);
	}
}
