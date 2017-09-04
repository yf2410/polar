package com.polar.browser.downloadfolder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.folder.FileInfo;
import com.polar.browser.folder.FileListAdapter;
import com.polar.browser.folder.IRefresh;
import com.polar.browser.manager.ConfigManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectDownloadDir extends LemonBaseActivity implements
		OnClickListener {

	private ListView mLvFolders;
	private List<FileInfo> list;
	private FileListAdapter mAdapter;

	private TextView mBtnConfirm;
	private TextView mTvCurrentFolder;

	private String mCurrentPath;

	/**
	 * 手机存储orSD卡
	 **/
	private int mDownType;

	private String mRootPath;
	private IRefresh mRefresh = new IRefresh() {

		@Override
		public void refreshListItems(String path, boolean isFolder) {
			refresh(path);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_download_dir);
		initView();
		initListener();
		initData();
	}

	private void initView() {
		mLvFolders = (ListView) findViewById(R.id.lv_folders);
		mBtnConfirm = (TextView) findViewById(R.id.btn_confirm);
		mTvCurrentFolder = (TextView) findViewById(R.id.tv_current_folder);
	}

	private void initListener() {
		mBtnConfirm.setOnClickListener(this);
		findViewById(R.id.common_img_back).setOnClickListener(this);
	}

	private void initData() {
		mAdapter = new FileListAdapter(this, mRefresh);
		mCurrentPath = getIntent().getStringExtra(CommonData.KEY_DOWN_ROOT);
		mDownType = getIntent().getIntExtra(CommonData.KEY_DOWN_TYPE, CommonData.DOWN_TYPE_PHONE);
		if (!mCurrentPath.endsWith(File.separator)) {
			mCurrentPath = mCurrentPath + File.separator;
		}
		mRootPath = mCurrentPath;
		if (!TextUtils.isEmpty(mCurrentPath)) {
			refresh(mCurrentPath);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_confirm:
				// 保存选择的路径
				if (!mCurrentPath.endsWith(File.separator)) {
					mCurrentPath = mCurrentPath + File.separator;
				}
				ConfigManager.getInstance().saveCurrentPath(mCurrentPath);
				// 发广播通知
				Intent intent = new Intent(CommonData.ACTION_DOWNLOAD_FOLDER_CHANGED);
				intent.putExtra(CommonData.KEY_DOWN_ROOT, mCurrentPath);
				JuziApp.getInstance().sendBroadcast(intent);
				finish();
				overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
				break;
			case R.id.common_img_back:
				onBackPressed();
				break;
			default:
				break;
		}
	}

	private void refresh(String path) {
		if (!path.endsWith(File.separator)) {
			path = path + File.separator;
		}
		mCurrentPath = path;
		list = buildListForSimpleAdapter(path);
		orderByName(list);
		mAdapter.updateData(list);
		mLvFolders.setAdapter(mAdapter);
		mLvFolders.setSelection(0);
		updateCurrentFolder();
	}

	private void updateCurrentFolder() {
		String path = mCurrentPath;
		String path2 = mRootPath;
		if (mRootPath.endsWith(File.separator)) {
			path2 = path2.substring(0, path2.length() - 1);
		}
		if (mDownType == CommonData.DOWN_TYPE_PHONE) {
			path = path.replace(path2, getString(R.string.download_folder_phone));
		} else if (mDownType == CommonData.DOWN_TYPE_SD_CARD) {
			path = path.replace(path2, getString(R.string.download_folder_sd));
		}
		mTvCurrentFolder.setText(path);
	}

	/* 根据路径生成一个包含路径的列表 */
	private List<FileInfo> buildListForSimpleAdapter(String path) {
		File[] files = new File(path).listFiles();
		List<FileInfo> list = new ArrayList<FileInfo>();
		if (files == null) {
			return list;
		}
		for (File file : files) {
			FileInfo info = new FileInfo();
			info.directory = file.isDirectory();
			info.name = file.getName();
			// 只添加目录 & 非隐藏文件
			if (info.directory && !info.name.startsWith(".")) {
				info.path = file.getPath();
				// 获取该目录下，有多少个文件夹
				info.children = getChildFolderCount(file);
				list.add(info);
			}
		}
		return list;
	}

	/**
	 * 按照文件名称排序
	 *
	 * @param list
	 */
	private void orderByName(List<FileInfo> list) {
		Collections.sort(list, new Comparator<FileInfo>() {
			@Override
			public int compare(FileInfo o1, FileInfo o2) {
				return o1.name.compareTo(o2.name);
			}
		});
	}

	/**
	 * 获取目录下子文件夹个数
	 *
	 * @param file
	 * @return
	 */
	private int getChildFolderCount(File file) {
		File[] files = file.listFiles();
		int folderCount = 0;
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					folderCount++;
				}
			}
		}
		return folderCount;
	}

	/**
	 * 跳转到上一层
	 *
	 * @return true，有上级； false，没上级
	 */
	private boolean goToParent() {
		File file = new File(mCurrentPath);
		File str_pa = file.getParentFile();
		if (str_pa == null) {
			return false;
		} else if (TextUtils.equals(mRootPath, mCurrentPath)) {
			return false;
		} else {
			mCurrentPath = str_pa.getAbsolutePath();
			refresh(mCurrentPath);
			return true;
		}
	}

	@Override
	public void onBackPressed() {
		if (!goToParent()) {
			super.onBackPressed();
			overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
		}
	}
}
