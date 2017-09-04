package com.polar.browser.folder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.CommonUtils;

public class FileItem extends RelativeLayout implements android.view.View.OnClickListener {

	private TextView mTvFileName;
	private TextView mTvChild;
	private ImageView mIvIcon;

	private FileInfo mInfo;

	private IRefresh mRefresh;

	public FileItem(Context context, IRefresh refresh) {
		this(context, null, refresh);
	}

	public FileItem(Context context, AttributeSet attrs, IRefresh refresh) {
		super(context, attrs);
		mRefresh = refresh;
		init();
	}

	private void init() {
		initView();
	}

	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.item_file, this);
		mTvFileName = (TextView) findViewById(R.id.tv_file_name);
		mTvChild = (TextView) findViewById(R.id.tv_child);
		mIvIcon = (ImageView) findViewById(R.id.iv_folder);
		setOnClickListener(this);
	}

	public void bind(FileInfo info) {
		this.mInfo = info;
		mTvFileName.setText(mInfo.name);
		if (info.directory) {
			mTvChild.setText(getResources().getString(R.string.file_child_count, info.children));
			mIvIcon.setImageResource(R.drawable.icon_folder);
		} else {
			mTvChild.setText(getResources().getString(R.string.bookmark_file));
			mIvIcon.setImageResource(R.drawable.icon_file);
		}
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		mRefresh.refreshListItems(mInfo.path, mInfo.directory);
	}
}
