package com.polar.browser.download.savedpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox.OnCheckChangedListener;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;

import java.io.BufferedReader;
import java.io.FileReader;

public class SavedPageItem extends RelativeLayout implements OnClickListener, OnLongClickListener {

	/**
	 * 标识该条目是否处于编辑状态
	 **/
	public boolean isEditing;

	private SavedPageNode mNode;

	private CommonCheckBox1 mCheckBox;

	private TextView mTvFileName;

	private TextView mTvFileSize;

	private ImageView mIvIcon;

	public SavedPageItem(Context context) {
		this(context, null);
	}

	public SavedPageItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
		initListener();
	}

	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.item_savedpage, this);
		mCheckBox = (CommonCheckBox1) findViewById(R.id.check_box);
		mTvFileName = (TextView) findViewById(R.id.tv_file_name);
		mTvFileSize = (TextView) findViewById(R.id.tv_file_size);
		mIvIcon = (ImageView) findViewById(R.id.iv_icon);
	}

	private void initListener() {
		setOnClickListener(this);
		setOnLongClickListener(this);
		mCheckBox.setOnCheckedChangedListener(new OnCheckChangedListener() {
			@Override
			public void onCheckChanged(View v, boolean isChecked) {
				mNode.isChecked = isChecked;
				if (getContext() instanceof SavedPageActivity) {
					((SavedPageActivity) getContext()).updateCheckAll();
					((SavedPageActivity) getContext()).updateDeleteBtnState();
				}
			}
		});
	}

	public void bind(SavedPageNode task) {
		mNode = task;
		mTvFileName.setText(mNode.fileName);
		mTvFileSize.setText(FileUtils.formatFileSize(mNode.fileSize));
		if (isEditing) {
			mCheckBox.setVisibility(View.VISIBLE);
			if (mNode.isChecked) {
				mCheckBox.setChecked(true);
			} else {
				mCheckBox.setChecked(false);
			}
		} else {
			mCheckBox.setVisibility(View.GONE);
		}
		String file = mNode.file.getAbsolutePath() + SavedPageUtil.DESC_SUFFIX;
		FileReader fr;
		try {
			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line;
			if ((line = br.readLine()) != null) {
				String iconPath = String.format("%s/%s/%s", getContext().getFilesDir().toString(),
						CommonData.ICON_DIR_NAME, UrlUtils.getHost(line));
				Bitmap icon = FileUtils.getBitmapFromFile(iconPath);
				if (icon != null) {
					mIvIcon.setImageBitmap(icon);
				} else {
					mIvIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_default));
				}
			}
			br.close();
		} catch (Exception e) {
			SimpleLog.e(e);
		}
	}

	@Override
	public void onClick(View v) {
		if (isEditing) {
			if (mCheckBox.isChecked()) {
				mCheckBox.setChecked(false);
			} else {
				mCheckBox.setChecked(true);
			}
			return;
		} else { // 打开.mht
			Intent intent = new Intent(this.getContext(), BrowserActivity.class);
			intent.setAction(CommonData.ACTION_LOAD_SAVED_PAGES);
			intent.putExtra(CommonData.EXTRA_LOAD_SAVED_PAGES_DATA, mNode.file.getAbsolutePath());
			getContext().startActivity(intent);
			((Activity) getContext()).overridePendingTransition(0, 0);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (!isEditing && getContext() instanceof SavedPageActivity) {
			((SavedPageActivity) getContext()).changeEditState(true);
			mCheckBox.setChecked(true);
			return true;
		}
		return false;
	}
}
