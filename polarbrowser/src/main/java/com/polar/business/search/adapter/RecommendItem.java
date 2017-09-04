package com.polar.business.search.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.history.IHistoryItemClick;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.UrlUtils;
import com.polar.business.search.HighlightTask;
import com.polar.business.search.IHighlightCallBack;
import com.polar.business.search.ISuggestUrl;
import com.polar.business.search.dao.RecommendInfo;

public class RecommendItem extends LinearLayout implements OnClickListener {

	private TextView mTvUrl;

	private TextView mTvTitle;

	private ImageView mIvIcon;

	private ImageView mSelectIcon;

	private RecommendInfo mInfo;

	private IHistoryItemClick mClickDelegate;

	private ISuggestUrl mSuggestUrlDelegate;

	private String mKey;

	private IHighlightCallBack mTitleCallBack = new IHighlightCallBack() {
		@Override
		public void callBack(String key, final String resultText) {
			if (TextUtils.equals(key, mKey) && mTvTitle != null) {
				ThreadManager.postTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						mTvTitle.setText(Html.fromHtml(resultText));
					}
				});
			}
		}
	};

	private IHighlightCallBack mUrlCallBack = new IHighlightCallBack() {
		@Override
		public void callBack(String key, final String resultText) {
			if (TextUtils.equals(key, mKey) && mTvUrl != null) {
				ThreadManager.postTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						mTvUrl.setText(Html.fromHtml(resultText));
					}
				});
			}
		}
	};

	private HighlightTask mTitleHighlightTask;
	private HighlightTask mUrlHighlightTask;

	public RecommendItem(Context context) {
		this(context, null);
	}

	public RecommendItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();

	}

	public void init(IHistoryItemClick delegate, ISuggestUrl suggestUrlDelegate, String key) {
		mClickDelegate = delegate;
		mSuggestUrlDelegate = suggestUrlDelegate;
		mKey = key;
	}

	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.recommend_item, this);
		mTvTitle = (TextView) findViewById(R.id.common_tv_title);
		mTvUrl = (TextView) findViewById(R.id.common_tv_summary);
		mIvIcon = (ImageView) findViewById(R.id.common_img_icon);
		mSelectIcon = (ImageView) findViewById(R.id.common_img_right);
		mSelectIcon.setOnClickListener(this);
		setOnClickListener(this);
	}

	public void bind(RecommendInfo info) {
		mInfo = info;
		if (mInfo == null) {
			return;
		}
		if (!TextUtils.isEmpty(mInfo.title)) {
			String title = mInfo.title;
			if (mKey != null) {
				mTitleHighlightTask = new HighlightTask(title, mKey, mTitleCallBack);
				ThreadManager.postTaskToLogicHandler(mTitleHighlightTask);
			}
		}
		if (!TextUtils.isEmpty(mInfo.url)) {
			String Url = mInfo.url;
			if (mKey != null) {
				mUrlHighlightTask = new HighlightTask(Url, mKey, mUrlCallBack);
				ThreadManager.postTaskToLogicHandler(mUrlHighlightTask);
			}
		}
		String iconPath = String.format("%s/%s/%s", getContext().getFilesDir().toString(),
				CommonData.ICON_DIR_NAME, UrlUtils.getHost(mInfo.url));
		Bitmap icon = FileUtils.getBitmapFromFile(iconPath);
		if (icon != null) {
			mIvIcon.setImageBitmap(icon);
		} else {
			mIvIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon_default));
		}
		this.setBackgroundResource(R.drawable.common_list_row1);
		mTvTitle.setTextColor(getResources().getColor(R.color.common_font_color_selector_2));
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mSelectIcon)) {
			mSuggestUrlDelegate.addUrl(mInfo.url);
		} else if (mClickDelegate != null) {
			mClickDelegate.onClick(mInfo.url);
		}
	}
}
