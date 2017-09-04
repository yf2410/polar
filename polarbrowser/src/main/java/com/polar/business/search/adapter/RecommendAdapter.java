package com.polar.business.search.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.polar.browser.base.JZBaseAdapter;
import com.polar.browser.history.IHistoryItemClick;
import com.polar.business.search.ISuggestUrl;
import com.polar.business.search.dao.RecommendInfo;

public class RecommendAdapter extends JZBaseAdapter<RecommendInfo> {

	private IHistoryItemClick mClickDelegate;

	private ISuggestUrl mSuggestUrlDelegate;

	private String mKey;

	public RecommendAdapter(Context context) {
		super(context);
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String key) {
		mKey = key;
	}

	public void registerDelegate(IHistoryItemClick delegate, ISuggestUrl urlDelegate) {
		mClickDelegate = delegate;
		mSuggestUrlDelegate = urlDelegate;
	}

	@Override
	public View newView(Context context, RecommendInfo data, ViewGroup parent, int type) {
		RecommendItem item = new RecommendItem(context);
		return item;
	}

	@Override
	public void bindView(View view, int position, RecommendInfo data) {
		RecommendItem item = (RecommendItem) view;
		item.init(mClickDelegate, mSuggestUrlDelegate, mKey);
		item.bind(data);
	}
}
