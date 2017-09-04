package com.polar.browser.download.savedpage;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.ValueCallback;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SavedPageUtil {
	public static final String SAVED_FOLDER = File.separator + "saved_pages" + File.separator;

	public static final String DESC_SUFFIX = ".desc";

	public static List<SavedPageNode> getSavedPageList(Context c) {
		List<SavedPageNode> list = new ArrayList<SavedPageNode>();
		File folder = new File(c.getFilesDir() + SAVED_FOLDER);
		if (folder.exists() && folder.isDirectory()) {
			File[] childFiles = folder.listFiles();
			List<File> listFile = new ArrayList<File>();
			if (childFiles != null && childFiles.length > 0) {
				listFile = Arrays.asList(childFiles);
				Collections.sort(listFile, new Comparator<File>() {
					@Override
					public int compare(File arg0, File arg1) {
						return ((Long)arg1.lastModified()).compareTo((Long)arg0.lastModified());
					}
				});
				for (File childFile : listFile) {
					if (childFile.getName().endsWith(SavedPageUtil.DESC_SUFFIX)) {
						continue;
					}
					SavedPageNode node = new SavedPageNode();
					node.file = childFile;
					node.fileName = childFile.getName();
					node.setName(childFile.getName());
					node.fileSize = childFile.length();
					node.setSize(childFile.length());
					node.setPath(childFile.getAbsolutePath());
					node.setDate(childFile.lastModified());
					list.add(node);
				}
			}
		}
		return list;
	}

	public static void savePage(Context c, String title, final String url) {
		File file = new File(c.getFilesDir() + SavedPageUtil.SAVED_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		if (TextUtils.isEmpty(url)) {
			return;
		}
		if (TextUtils.isEmpty(title)) {
			title = url;
		}
		String fileName = title.replaceAll("[\\?\\\\/:|<>\\*]", "_");
		// 重名的，找到不重复的名称，然后再存存储
		File webFile = new File(file.getAbsolutePath() + File.separator + fileName + ".mht");
		if (webFile.exists()) {
			int i = 1;
			while (webFile.exists() && i <= 20) {
				webFile = new File(file.getAbsolutePath() + File.separator + fileName + "(" + String.valueOf(i) + ")" + ".mht");
				++i;
			}
		}
		// 保存web archive
		final String webFilePath = webFile.getAbsolutePath();
		TabViewManager.getInstance().getCurrentTabView().saveWebArchive(webFilePath, new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				// 保存webarchive成功后，保存对应的描述文件
				CustomToastUtils.getInstance().showTextToast(R.string.offline_web_saved);
				String descPath = webFilePath + SavedPageUtil.DESC_SUFFIX;
				File descFile = new File(descPath);
				if (descFile.exists()) {
					FileUtils.deleteOnlyFile(descFile);
				}
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(descPath);
					fos.write(url.getBytes());
					fos.close();
				} catch (Exception e) {
					SimpleLog.e(e);
				}
				Intent intent = new Intent(CommonData.ACTION_HAS_DOWNLOADING_TASK);
				intent.putExtra(ConfigDefine.HAS_DOWNLOADING_TASK, true);
				JuziApp.getInstance().sendBroadcast(intent);
			}
		});
		CustomToastUtils.getInstance().showTextToast(R.string.offline_web_start_save);
	}

	public static void deleteSavedPage(File file) {
		File descFile = new File(file.getAbsolutePath() + SavedPageUtil.DESC_SUFFIX);
		FileUtils.deleteOnlyFile(file);
		FileUtils.deleteOnlyFile(descFile);
	}
}
