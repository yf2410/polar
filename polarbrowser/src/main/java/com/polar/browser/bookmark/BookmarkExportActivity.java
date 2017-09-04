package com.polar.browser.bookmark;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.folder.FileInfo;
import com.polar.browser.folder.FileListAdapter;
import com.polar.browser.folder.IRefresh;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookmarkExportActivity extends LemonBaseActivity implements
		OnClickListener {

	private ListView mLvFolders;
	private List<FileInfo> list;
	private FileListAdapter mAdapter;

	private TextView mBtnConfirm;
	private TextView mTvCurrentFolder;

	private String mCurrentPath;
	private String mRootPath;

	private CommonTitleBar mTitleBar;
	private IRefresh mRefresh = new IRefresh() {

		@Override
		public void refreshListItems(String path, boolean isFolder) {
			refresh(path);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
		mTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
		mTitleBar.setTitle(getString(R.string.export_bookmarks));
	}

	private void initListener() {
		mBtnConfirm.setOnClickListener(this);
		findViewById(R.id.common_img_back).setOnClickListener(this);
	}

	private void initData() {
		mAdapter = new FileListAdapter(this, mRefresh);
		mCurrentPath = VCStoragerManager.getInstance().getPhoneStorage();
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
				exportBookmark();
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

	private void exportBookmark() {
		if (!mCurrentPath.endsWith(File.separator)) {
			mCurrentPath = mCurrentPath + File.separator;
		}
		String srcPath = BookmarkManager.getInstance().getPath();
		boolean isSuccess = true;
		String exportPath = mCurrentPath + "VC_bookmark_" + System.currentTimeMillis() + ".json";
		try {
			InputStream input = new FileInputStream(srcPath);
			File dest = new File(exportPath);
			FileUtils.copyFile(input, dest);
			input.close();
		} catch (FileNotFoundException e) {
			SimpleLog.e(e);
			isSuccess = false;
		} catch (IOException e) {
			SimpleLog.e(e);
			isSuccess = false;
		} finally {
		}
		if (isSuccess) {
			CustomToastUtils.getInstance().showDurationToast(getString(R.string.export_bookmarks_success) + "\n" + getString(R.string.file_path) + exportPath, 4000);
			try {
				Uri localUri = Uri.fromFile(new File(exportPath));
				Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
				sendBroadcast(localIntent);
			} catch (Exception e) {
				SimpleLog.e(e);
			}
		} else {
			CustomToastUtils.getInstance().showTextToast(getString(R.string.export_bookmarks_failed));
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
		path = path.replace(path2, getString(R.string.download_folder_phone));
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
