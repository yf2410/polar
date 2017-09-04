package com.polar.browser.history;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox.OnCheckChangedListener;
import com.polar.browser.i.IEditStateObserver;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.UrlUtils;

/**
 * 单条历史记录
 *
 * @author dpk
 */
public class HistoryItem extends LinearLayout {

	private TextView mTvUrl;

	private TextView mTvTitle;

	private ImageView mIvIcon;

	private HistoryInfo mInfo;

	private IHistoryItemClick mClickDelegate;

	private CommonCheckBox1 mCheckBox;

	private IEditStateObserver mEditObserver;

	private boolean mIsEditing = false;

	public HistoryItem(Context context) {
		this(context, null);
	}

	public HistoryItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}


	public void init(IHistoryItemClick delegate, IEditStateObserver editObserver) {
		mClickDelegate = delegate;
		mEditObserver = editObserver;
	}

	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.history_list_row, this);
		mTvTitle = (TextView) findViewById(R.id.common_tv_title);
		mTvUrl = (TextView) findViewById(R.id.common_tv_summary);
		mIvIcon = (ImageView) findViewById(R.id.common_img_icon);
		mCheckBox = (CommonCheckBox1) findViewById(R.id.common_check);
		mCheckBox.setOnCheckedChangedListener(new OnCheckChangedListener() {
			@Override
			public void onCheckChanged(View v, boolean isChecked) {
				mInfo.isChecked = isChecked;
				mClickDelegate.onCheckedChange();
			}
		});
	}

	public void bind(HistoryInfo info) {
		mInfo = info;
		String mUrl = mInfo.url;
		mTvUrl.setText(mUrl);
		mTvTitle.setText(mInfo.title);
		String iconPath = String.format("%s/%s/%s", getContext().getFilesDir().toString(),
				CommonData.ICON_DIR_NAME, UrlUtils.getHost(mInfo.url));
		Bitmap icon = FileUtils.getBitmapFromFile(iconPath);
		if (icon != null) {
			mIvIcon.setImageBitmap(icon);
		} else {
			mIvIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_default));
		}
		mCheckBox.setChecked(mInfo.isChecked);
		findViewById(R.id.common_ll_root).setBackgroundResource(R.drawable.common_list_row1);
		mTvTitle.setTextColor(getResources().getColor(R.color.common_font_color_selector_2));
		mTvUrl.setTextColor(getResources().getColor(R.color.common_font_color_10));
	}

	public void onClick() {
		if (!mIsEditing) {
			if (mClickDelegate != null) {
				mClickDelegate.onClick(mInfo.url);
				Statistics.sendOnceStatistics(GoogleConfigDefine.FAVORITE_HISTORY, GoogleConfigDefine.FAVORITE_HISTORY_TYPE_HISTORY);
			}
		} else {
			boolean checked = mCheckBox.isChecked();
			mCheckBox.setChecked(!checked);
		}
	}

	public boolean onLongClick() {
		if (!mIsEditing && mEditObserver != null) {
			mEditObserver.onEditStateChanged(true);
			mCheckBox.setChecked(true);
			return true;
		}
		return false;
	}

	public void changeEditState(boolean isEdit) {
		mIsEditing = isEdit;
		if (mIsEditing) {
			mCheckBox.setVisibility(View.VISIBLE);
		} else {
			mCheckBox.setVisibility(View.GONE);
		}
	}
}
