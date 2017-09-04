package com.polar.business.search.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.history.IHistoryItemClick;
import com.polar.browser.history.ISearchResultNotifyHindeIM;
import com.polar.browser.i.IHideIMListener;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.business.search.ISuggestCallBack;
import com.polar.business.search.ISuggestUrl;
import com.polar.business.search.SuggestTask2;
import com.polar.business.search.adapter.RecommendAdapter;
import com.polar.business.search.dao.RecommendInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchResultView extends RelativeLayout {

	private static final String TAG = "SearchResultView";

	/**
	 * 下半部分搜索结果，ListView --》 推荐网址
	 **/
	private ListView mLvPart2;

	private LayoutInflater mInflater;

	private RecommendAdapter mAdapter;

	private IHistoryItemClick mClickDelegate;

	private ISuggestUrl mSuggestUrlDelegate;

	private ISearchResultNotifyHindeIM mSearchResultNotifyHindeIM;

	/**
	 * 输入框输入要查询的关键字
	 **/
	private String mKey;
	/**
	 * 2、搜索推荐网址列表(C++)
	 **/
	private Runnable mSuggestTask2;
	/**
	 * 推荐网址列表结果回调
	 **/
	private ISuggestCallBack callBack2 = new ISuggestCallBack() {
		@Override
		public void callBack(final String result) {
			ThreadManager.getUIHandler().post(new Runnable() {
				@Override
				public void run() {
					if (!TextUtils.isEmpty(result) && !TextUtils.isEmpty(mKey)) {
						handleSuggestResult2(result);
					} else {
						SearchResultView.this.initDataPart2(null);
					}
				}
			});
		}
	};

	private boolean headerIsAdded;

	public SearchResultView(Context context) {
		this(context, null);
	}

	public SearchResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mInflater = LayoutInflater.from(context);
		initView();
		initData();
	}

	@SuppressLint("InflateParams")
	private void initView() {
		mInflater.inflate(R.layout.view_search_result, this);
		mLvPart2 = (ListView) findViewById(R.id.lv_result_part2);
		mLvPart2.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mSearchResultNotifyHindeIM.onNotifyHindeIM();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}

	private void initData() {
		mAdapter = new RecommendAdapter(getContext());

	}

	/**
	 * 注册下半部分数据中条目点的击事件处理
	 *
	 * @param delegate
	 */
	public void registerDelegate(IHistoryItemClick delegate, ISuggestUrl urlDelegate, ISearchResultNotifyHindeIM searchResultNotifyHindeIM) {
		mClickDelegate = delegate;
		mSuggestUrlDelegate = urlDelegate;
		this.mSearchResultNotifyHindeIM = searchResultNotifyHindeIM;
		mAdapter.registerDelegate(mClickDelegate, mSuggestUrlDelegate);
	}

	/**
	 * 设置点击键盘消失的监听
	 *
	 * @param hideIMListener
	 */
	public void setHideImListener(IHideIMListener hideIMListener) {
//		if (mLvPart2 instanceof HideIMListView) {
//			((HideIMListView) mLvPart2).setHideImListener(hideIMListener);
//		}
	}

	/**
	 * 初始化下半部分数据 网址。。
	 */
	private void initDataPart2(List<RecommendInfo> datasPart2) {
		if (datasPart2 == null) {
			datasPart2 = new ArrayList<RecommendInfo>();
//			mLvPart2.setVisibility(View.GONE);
		}
		mAdapter.setKey(mKey);
		mAdapter.updateData(datasPart2);
	}

	public void clearResults() {
		mKey = null;
		if (mSuggestTask2 != null) {
			ThreadManager.getLogicHandler().removeCallbacks(mSuggestTask2);
		}
		this.initDataPart2(null);
	}

	/**
	 * 输入框文字更改时，
	 * 1、请求搜索推荐词
	 * 2、访问C++ 推荐列表
	 *
	 * @param key
	 */
	public void handleTextChange(String key, boolean isUrl) {
		mKey = key;
		if (TextUtils.isEmpty(mKey)) {
			return;
		}
		if (mKey.length() > 2048) {
			mKey = mKey.substring(0, 50);
		}
		// 第二部分数据
		if (mSuggestTask2 != null) {
			ThreadManager.getLogicHandler().removeCallbacks(mSuggestTask2);
		}
		mSuggestTask2 = new SuggestTask2(mKey, callBack2);
		ThreadManager.getLogicHandler().postDelayed(mSuggestTask2, 200);
	}


	/**
	 * data part 2
	 *
	 * @param result
	 */
	private void handleSuggestResult2(String result) {
		// 推荐列表2
		//[{"title":"tieba.baidu.com","url":"http%3A%2F%2Ftieba.baidu.com%2F"},
		//{"title":"爱奇艺-爱奇艺视频,高清影视剧,网络视频在线观看","url":"http://m.iqiyi.com/"},
		//{"title":"手机环球网","url":"http://wap.huanqiu.com/"},
		//{"title":"手机网易网","url":"http://3g.163.com/touch/"}]
		SimpleLog.d(TAG, "Result2 = " + result);
		JSONArray jsonArray;
		List<RecommendInfo> infos = new ArrayList<RecommendInfo>();
		try {
			jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject item = jsonArray.getJSONObject(i);
				String title = item.getString("title");
				String url = item.getString("url");
				RecommendInfo info = new RecommendInfo();
				info.title = title;
				info.url = url;
				infos.add(info);
			}
		} catch (JSONException e) {
			SimpleLog.e(e);
		}
		this.initDataPart2(infos);
		this.setVisibility(View.VISIBLE);
	}

	/**
	 * 把搜索建议 添加到历史记录 listView header
	 */
	public void addSearchRecommendHeader(View view ) {
		if(!headerIsAdded){
			mLvPart2.addHeaderView(view);
			headerIsAdded = true;
		}

		mLvPart2.setAdapter(mAdapter);
	}

	public void removeSearchRecommendHeader(View view ) {
		mLvPart2.removeHeaderView(view);
		headerIsAdded = false;
	}

}
