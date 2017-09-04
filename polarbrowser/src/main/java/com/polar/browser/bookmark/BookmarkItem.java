package com.polar.browser.bookmark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.ui.CommonCheckBox1;
import com.polar.browser.common.ui.ICustomCheckBox.OnCheckChangedListener;
import com.polar.browser.i.IEditStateObserver;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.UrlUtils;

public class BookmarkItem extends RelativeLayout implements Checkable {

	private static final String TAG = "BookmarkItem";


	private TextView mTvUrl;

	private TextView mTvTitle;

	private ImageView mIvIcon;

	private BookmarkInfo mInfo;

	private CommonCheckBox1 mCheckBox;

	private View mOperate;

	private ImageView mEdit;
	private ImageView mDrag;

	private boolean mIsEditing = false;

	private ListView mList;

	private int mPos;

	private IBookmarkItemClick mItemClick;
	private IEditStateObserver mEditObserver;
	private Checkable child;

	public BookmarkItem(Context context) {
		this(context, null);
	}

	public BookmarkItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void bindList(ListView list, int pos) {
		mList = list;
		mPos = pos;
	}

	public void setClickDelegate(IBookmarkItemClick itemClick) {
		mItemClick = itemClick;
	}

	public void setEditStateObserver(IEditStateObserver editObserver) {
		mEditObserver = editObserver;
	}

	private void initView() {
		mTvTitle = (TextView) findViewById(R.id.common_tv_title);
		mTvUrl = (TextView) findViewById(R.id.common_tv_summary);
		mIvIcon = (ImageView) findViewById(R.id.icon);
		mCheckBox = (CommonCheckBox1) findViewById(R.id.common_check);
		mOperate = findViewById(R.id.ll_operate);
		mEdit = (ImageView) findViewById(R.id.edit_handle);
		mDrag = (ImageView) findViewById(R.id.drag_handle);
		mCheckBox.setOnCheckedChangedListener(new OnCheckChangedListener() {
			@Override
			public void onCheckChanged(View v, boolean isChecked) {
				SimpleLog.d(TAG, "onCheckChanged:" + isChecked);
				if (mInfo.isChecked != isChecked) {
					mInfo.isChecked = isChecked;
					mList.setItemChecked(mPos, isChecked);
					mItemClick.onCheckedChange();
				}
			}
		});
		mEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), EditBookmarkActivity.class);
				intent.putExtra(BookmarkInfo.KEY_ID, mInfo.id);
				intent.putExtra(BookmarkInfo.KEY_NAME, mInfo.name);
				intent.putExtra(BookmarkInfo.KEY_TYPE, mInfo.type);
				intent.putExtra(BookmarkInfo.KEY_URL, mInfo.url);
				getContext().startActivity(intent);
				((Activity) getContext()).overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				if (mEditObserver != null) {
					mEditObserver.onEditStateChanged(false);
				}
			}
		});
	}

	public void bind(BookmarkInfo info) {
		mInfo = info;
		String mUrl = mInfo.url;
		mTvUrl.setText(mUrl);
		mTvTitle.setText(mInfo.name);
		String iconPath = String.format("%s/%s/%s", getContext().getFilesDir().toString(),
				CommonData.ICON_DIR_NAME, UrlUtils.getHost(mInfo.url));
		Bitmap icon = FileUtils.getBitmapFromFile(iconPath);
		if (icon != null) {
			mIvIcon.setImageBitmap(icon);
		} else {
			mIvIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_default));
		}
		mCheckBox.setChecked(mInfo.isChecked);
		findViewById(R.id.layout).setBackgroundResource(R.drawable.common_list_row1);
		mTvTitle.setTextColor(getResources().getColor(R.color.common_font_color_selector_2));
		mTvUrl.setTextColor(getResources().getColor(R.color.common_font_color_10));
	}

	public void changeEditState(boolean isEdit) {
		mIsEditing = isEdit;
		if (mIsEditing) {
			mCheckBox.setVisibility(View.VISIBLE);
			mOperate.setVisibility(View.VISIBLE);
			mDrag.setOnTouchListener(null);
			postInvalidate();
		} else {
			mCheckBox.setVisibility(View.GONE);
			mOperate.setVisibility(View.GONE);
			postInvalidate();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		child = (Checkable) findViewById(R.id.common_check);
		initView();
	}

	@Override
	public boolean isChecked() {
		return child.isChecked();
	}

	@Override
	public void setChecked(boolean checked) {
		if (mIsEditing) {
			child.setChecked(checked);
		}
	}

	@Override
	public void toggle() {
		if (mIsEditing) {
			child.toggle();
		}
	}
}
