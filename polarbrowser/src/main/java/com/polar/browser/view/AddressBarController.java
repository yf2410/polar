package com.polar.browser.view;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.gson.Gson;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonProgressBar1;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IAddressBar;
import com.polar.browser.i.IOpenToolBarTopMore;
import com.polar.browser.i.IProgressCallback;
import com.polar.browser.i.IShowOrHideDelegate;
import com.polar.browser.impl.AddressBarImpl;
import com.polar.browser.impl.ProgressBarImpl;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.SearchEngineList;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.util.ImageLoadUtils;
import com.polar.browser.ytbdownload.YtbVideoActivity;

import java.sql.SQLException;

/**
 * AddressBarController 用于处理和地址栏有关的逻辑
 *
 * @author dpk
 */
public class AddressBarController implements OnClickListener, IShowOrHideDelegate {
	private static final String TAG = AddressBarController.class.getSimpleName();
	// 地址栏显示的地址
	public TextView mTextUrl;
	// 容纳地址栏的Activity
	private Activity mActivity;
	// 地址栏View
	private View mAddressBar;
	// 地址栏前方搜索图标
	private View mSearchIcon;
	// 刷新按钮
	private View mBtnRefresh;
	// 停止按钮
	private View mBtnStop;
	//地址栏右侧更多图标
//	private ImageView mIvMore;
	// 处理地址栏
	private IAddressBar mAddressBarHandler;
	// 处理进度条
	private IProgressCallback mProgressHandler;
	// 进度条，在地址栏中
	private CommonProgressBar1 mProgressBar;
	// 全屏模式进度条
	private CommonProgressBar1 mFullScreenProgress;
	// 是否需要显示地址栏，在处理滚动事件时使用
	private boolean mIsNeedAddressBarShow = true;
	// 地址栏动画是否正在进行中
	private boolean mIsAddressBarAnimate = false;
	// 容纳实际页面内容的引用
	private ViewGroup mContentFrameRef;
	// 搜索视图
	private SearchFrame mSearchFrameRef;
	private IOpenToolBarTopMore mOpenToolBarTopMore;
	private View mBtnDownloadVideo;
	private ImageView mSearchEngineImg;
	private View searchEngineLayout;
	private SearchEngineList engineList;

	public AddressBarController(IOpenToolBarTopMore toolBarTopMore, Activity activity, ViewGroup content,
								SearchFrame searchFrame) {
		mActivity = activity;
		mContentFrameRef = content;
		mSearchFrameRef = searchFrame;
		mOpenToolBarTopMore = toolBarTopMore;
	}
	private void initEngineData() {
		String json = ConfigManager.getInstance().getLastEngineList();
		this.engineList = new Gson().fromJson(json, SearchEngineList.class);
	}

	public void init() {
		initEngineData();

		mAddressBar = mActivity.findViewById(R.id.toolbar_top);
		mBtnRefresh = mActivity.findViewById(R.id.btn_refresh);
		mBtnDownloadVideo = mActivity.findViewById(R.id.btn_download_video);
		mBtnStop = mActivity.findViewById(R.id.btn_stop);
		mSearchIcon = mActivity.findViewById(R.id.search_icon);
		mSearchEngineImg = (ImageView) mActivity.findViewById(R.id.search_engine_icons);
		searchEngineLayout = mActivity.findViewById(R.id.icon_framelayout);
		refreshSearchEngineUI(ConfigManager.getInstance().getSearchEngine());
		mTextUrl = (TextView) mActivity.findViewById(R.id.text_url);
//		mIvMore = (ImageView) mActivity.findViewById(R.id.tool_bar_more);
		mProgressBar = (CommonProgressBar1) mActivity.findViewById(R.id.progress);
		mFullScreenProgress = (CommonProgressBar1) mActivity.findViewById(R.id.fullscreen_progress);
		mAddressBarHandler = new AddressBarImpl(mAddressBar, mProgressBar,
				mFullScreenProgress, mBtnRefresh, mBtnStop);
		mProgressHandler = new ProgressBarImpl(mAddressBarHandler);
		mBtnDownloadVideo.setOnClickListener(this);
		mBtnRefresh.setOnClickListener(this);
		mBtnStop.setOnClickListener(this);
		mSearchEngineImg.setOnClickListener(this);
//		mIvMore.setOnClickListener(this);
		mActivity.findViewById(R.id.rl_search).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_download_video: //点击下载youtube 视频
				if (TabViewManager.getInstance().getCurrentUrl() != null &&
						TabViewManager.getInstance().getCurrentTitle() != null) {
					Intent intent = new Intent(mActivity, YtbVideoActivity.class);
					intent.putExtra(YtbVideoActivity.YOUTUBE_URL,TabViewManager.getInstance().getCurrentUrl());
					mActivity.startActivity(intent);
				}
				Statistics.sendOnceStatistics(GoogleConfigDefine.PLUG_VIDEO, GoogleConfigDefine.VIDEO_DWN_BTN);
				break;

			case R.id.search_engine_icons:
				if (mSearchFrameRef != null) {
					mSearchFrameRef.onSetSearchEngin(true);
				}
				break;

			case R.id.btn_refresh:
				refreshTab();
				break;
			case R.id.btn_stop:
				stopLoading();
				break;
			case R.id.rl_search:
				jumpToSearch();
				break;
			case R.id.tool_bar_more:
				openMoreMenu();
				showAddressBar();
				break;
			default:
				break;
		}
	}

	private void openMoreMenu() {
		mOpenToolBarTopMore.openMoreMenu();
	}

	public void showRefreshAddressBar() {
		mBtnRefresh.setVisibility(View.VISIBLE);
		mBtnStop.setVisibility(View.GONE);
	}

	public void showStopAddressBar() {
		mBtnRefresh.setVisibility(View.GONE);
		mBtnStop.setVisibility(View.VISIBLE);
	}

	public void showDownloadVideoButton(boolean show) {
		mBtnDownloadVideo.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	public IAddressBar getAddressBarHandler() {
		return mAddressBarHandler;
	}

	public IProgressCallback getProgressCallback() {
		return mProgressHandler;
	}

	private void hideProgressBar() {
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	private void hideFullScreenProgress() {
		mFullScreenProgress.setVisibility(View.INVISIBLE);
	}

	public void showAddressBar() {
		mAddressBar.setVisibility(View.VISIBLE);
	}

	public void hideAddressBar() {
		mAddressBar.setVisibility(View.GONE);
	}

	public void setNeedShowAddressBar() {
		mIsNeedAddressBarShow = true;
	}

	@Override
	public void show() {
		forceShowAddress();
	}

	@Override
	public void hide() {
		boolean bLastShow = mIsNeedAddressBarShow;
		mIsNeedAddressBarShow = false;
		if (bLastShow != mIsNeedAddressBarShow) {
			hideAddressBarAnimation();
		}
	}

	public void showWithoutAnim() {
		mAddressBar.setVisibility(View.VISIBLE);
	}

	public void hideWithoutAnim() {
		mAddressBar.setVisibility(View.GONE);
	}

	private void refreshTab() {
		String currentUrl = TabViewManager.getInstance().getCurrentUrl();
		if (TextUtils.isEmpty(currentUrl)) {
			TabViewManager.getInstance().getCurrentTabView().loadUrl(getUrl(), TabViewManager.getInstance().getCurrentTabView().getContentView().getSource());
		} else {
			if (currentUrl.equals(TabViewManager.HOME_URL)) {
				TabViewManager.getInstance().getCurrentTabView().loadUrl(mTextUrl.getText().toString(), TabViewManager.getInstance().getCurrentTabView().getContentView().getSource());
			} else
				TabViewManager.getInstance().getCurrentTabView().reload();
		}
	}

	private void stopLoading() {
		TabViewManager.getInstance().getCurrentTabView().stopLoading();
	}

	public void needshowAddress(boolean isTop) {
		mIsNeedAddressBarShow = true;
		if (!mAddressBar.isShown()) {
			SimpleLog.i("AddressBarController", "needshowAddress: !mAddressBar.isShown()");
			showAddressBarAnimation(isTop);
		} else if (isTop && mAddressBar.isShown() && !mIsAddressBarAnimate) {
			modifyContentLayouMargin();
		}
	}

	public void forceShowAddress() {
		boolean bLastShow = mIsNeedAddressBarShow;
		mIsNeedAddressBarShow = true;
		if (!mAddressBar.isShown()) {
			showAddressBarAnimation(true);
		} else if (bLastShow != mIsNeedAddressBarShow) {
			showAddressBar();
		} else {
			modifyContentLayouMargin();
		}
	}

	public void updateTitle(String title) {
		if (title != null) {
			if (!TabViewManager.getInstance().isCurrentHome()) {
				mTextUrl.setText(title);
			}
		}
	}

	private void hideAddressBarAnimation() {
		Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.addressbar_out);
		mAddressBar.startAnimation(anim);
		if (!AppEnv.sIsFullScreen) {
			LayoutParams lp = (LayoutParams) mContentFrameRef.getLayoutParams();
			lp.setMargins(0, 0, 0, 0);
			mContentFrameRef.setLayoutParams(lp);
		}
		anim.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				if (AppEnv.sIsFullScreen) {
					mAddressBar.setVisibility(View.GONE);
				} else {
					mAddressBar.setVisibility(View.INVISIBLE);
				}
				mIsAddressBarAnimate = false;
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
				mIsAddressBarAnimate = true;
			}
		});
	}

	private void showAddressBarAnimation(final boolean isTop) {
		Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.addressbar_in);
		mAddressBar.startAnimation(anim);
		mAddressBar.setVisibility(View.VISIBLE);
		anim.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				if (isTop && !AppEnv.sIsFullScreen) {
					LayoutParams lp = (LayoutParams) mContentFrameRef.getLayoutParams();
					lp.setMargins(0, mAddressBar.getHeight() - DensityUtil.dip2px(mActivity, 4) - 1, 0, 0);
					mContentFrameRef.setLayoutParams(lp);
				}
				mAddressBar.setVisibility(View.VISIBLE);
				mIsAddressBarAnimate = false;
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
				mIsAddressBarAnimate = true;
			}
		});
	}

	private void modifyContentLayouMargin() {
		if (AppEnv.sIsFullScreen) {
			return;
		}
		LayoutParams lp = (LayoutParams) mContentFrameRef.getLayoutParams();
		if (lp.topMargin <= 0) {
			lp.setMargins(0, mAddressBar.getHeight() - DensityUtil.dip2px(mActivity, 4) - 1, 0, 0);
			mContentFrameRef.setLayoutParams(lp);
		}
	}

	/**
	 * 初始化地址栏
	 *
	 * @param url
	 */
	public void initAddressBar(String url) {
		if (TabViewManager.getInstance().isCurrentHome()) {
			mSearchIcon.setVisibility(View.VISIBLE);
			mTextUrl.setText(mActivity.getString(R.string.search_input_hint));
			hideProgressBar();
			hideFullScreenProgress();
			hideAddressBar();
		} else {
			mSearchIcon.setVisibility(View.GONE);
			mOpenToolBarTopMore.initMoreMenu();
			if (url != null) {
				if (!url.equals(TabViewManager.BLANK_TAB_URL)) {
//					String title = HistoryManager.getInstance().getUrlTitle(url);
					try {
						HistoryRecord historyRecord = HistoryRecordApi.getInstance(
								CustomOpenHelper.getInstance(mActivity)).queryForHistoryAddr(url);
						if (historyRecord != null) {
						String historyTitle = historyRecord.getHistoryTitle();
							if (!TextUtils.isEmpty(historyTitle)) {
								mTextUrl.setText(historyTitle);
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} else {
				if (mTextUrl.getText() == null || mTextUrl.getText().length() == 0) {
					mTextUrl.setText(mActivity.getString(R.string.search_input_hint));
				}
			}
		}
	}

	public void initAddressBar() {
		mSearchIcon.setVisibility(View.VISIBLE);
		hideProgressBar();
		hideFullScreenProgress();
		hideAddressBar();
	}

	public String getUrl() {
		String url = TabViewManager.getInstance().getCurrentUrl();
		if (TextUtils.isEmpty(url)) {
			String text = mTextUrl.getText().toString();
			if (!text.equals(mActivity.getResources().getString(
					R.string.search_input_hint))) {
				url = text;
			}
		}
		return url;
	}

	/**
	 * 跳转到search activity
	 */
	public void jumpToSearch() {
		String url = getUrl();
		if (TabViewManager.getInstance().isCurrentHome()) {
			Statistics.sendOnceStatistics(
					GoogleConfigDefine.SEARCH, GoogleConfigDefine.SEARCH_CLICK_HOME);
			if (mSearchFrameRef != null) {
//				initEngineData();
//				refreshSearchEngineUI(SearchUtils.getDefaultEngineIndex(engineList));
				mSearchFrameRef.show(CommonData.PAGE_EDIT_CLICK,url, mActivity.findViewById(R.id.home_frame));
			}
		} else {
			Statistics.sendOnceStatistics(
					GoogleConfigDefine.SEARCH, GoogleConfigDefine.SEARCH_CLICK_WEB);
			if (mSearchFrameRef != null) {
//				initEngineData();
//				refreshSearchEngineUI(SearchUtils.getDefaultEngineIndex(engineList));
				mSearchFrameRef.show(CommonData.WEBCONTENT_EDIT_CLICK,url, mActivity.findViewById(R.id.content_frame));
			}
		}
	}

	@Override
	public boolean isShown() {
		return mAddressBar.isShown();
	}

	public void refreshSearchEngineUI(int searchEngine) {
		initEngineData();
		if(engineList!=null&&!ListUtils.isEmpty(engineList.getDataList())){
			SimpleLog.d(TAG,"AddressBarController refreshSearchEngineUI engineList!=null --- ");
			if(searchEngine>=engineList.getDataList().size())
				searchEngine = engineList.getDataList().size()-1;
			try{
				int defaultRes = SearchUtils.getDefaultEngineIconByName(engineList.getDataList().get(searchEngine).getEngineName());
				ImageLoadUtils.loadImage(mActivity, engineList.getDataList().get(searchEngine).getEnginePic(), mSearchEngineImg,R.drawable.engin_default_bg,defaultRes);
			}catch (NullPointerException e){
				e.printStackTrace();
			}
		}
/*		else{
			SimpleLog.d(TAG,"AddressBarController refreshSearchEngineUI engineList!=null --- ");
			switch (searchEngine) {
				case ConfigDefine.SEARCH_ENGINE_GOOGLE:

					mSearchEngineImg.setImageResource(R.drawable.google_icon);
					break;
				case ConfigDefine.SEARCH_ENGINE_BING:
					mSearchEngineImg.setImageResource(R.drawable.bing);
					break;
				case ConfigDefine.SEARCH_ENGINE_YAHOO:
					mSearchEngineImg.setImageResource(R.drawable.yahoo);

					break;
				case ConfigDefine.SEARCH_ENGINE_YANDEX:
					mSearchEngineImg.setImageResource(R.drawable.yandex);
					break;
				case ConfigDefine.SEARCH_ENGINE_DUCKGO:
					mSearchEngineImg.setImageResource(R.drawable.duck_duck_go);
					break;
				case ConfigDefine.SEARCH_ENGINE_YOUTUBE:
					mSearchEngineImg.setImageResource(R.drawable.youtube);
					break;
				case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
					mSearchEngineImg.setImageResource(R.drawable.google_quick_search);
					break;
				default:
					break;
			}
		}*/
	}

	public void setSearchEngineImgVisible(int visible) {
		searchEngineLayout.setVisibility(visible);
	}
}
