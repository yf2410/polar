package com.polar.browser.folder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.base.JZBaseAdapter;

public class FileListAdapter extends JZBaseAdapter<FileInfo> {

	private IRefresh mRefresh;

	public FileListAdapter(Context context, IRefresh refresh) {
		super(context);
		mRefresh = refresh;
	}

	@Override
	public View newView(Context context, FileInfo data, ViewGroup parent, int type) {
		return new FileItem(context, mRefresh);
	}

	@Override
	public void bindView(View view, int position, FileInfo data) {
		FileItem item = (FileItem) view;
		item.bind(data);
	}
}
