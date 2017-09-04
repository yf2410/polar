package com.polar.browser.bookmark;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTabViewPager;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.history.HistoryView;
import com.polar.browser.history.IHistoryDeleteCallback;
import com.polar.browser.history.IHistoryItemClick;
import com.polar.browser.i.IEditStateObserver;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.loginassistant.login.LoginActivity;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.vclibrary.bean.events.SyncBookmarkEvent;
import com.polar.browser.vclibrary.common.Constants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

public class BookmarkActivity extends LemonBaseActivity implements OnClickListener {
	private static final int ITEM_HISTORY = 0;
	private static final int ITEM_FAVORITE = 1;
	private CommonTitleBar mTitleBar;
	private HistoryView mHistoryView;
	private BookmarkView mBookmarkView;
	private CommonTabViewPager mViewPager;
	private boolean mIsEditing = false;
	private int mPageSelected;
	private boolean isSlideClose;
	private int mInitItem = ITEM_HISTORY;
	private IHistoryItemClick mHistoryItemClickImpl = new IHistoryItemClick() {

		@Override
		public void onClick(String url) {
			Intent intent = new Intent(BookmarkActivity.this, BrowserActivity.class);
			intent.setAction(CommonData.OPEN_HISTORY_OR_BOOKMARK_ITEM);
			intent.putExtra(CommonData.ACTION_GOTO_URL, url);
			intent.putExtra(CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_HISTORY);
			startActivity(intent);
			overridePendingTransition(0, 0);
		}

		@Override
		public void onCheckedChange() {
			mHistoryView.updateCheckState();
		}
	};

	private IBookmarkItemClick mBookmarkItemClickImpl = new IBookmarkItemClick() {

		@Override
		public void onClick(String url) {
			Intent intent = new Intent(BookmarkActivity.this, BrowserActivity.class);
			intent.setAction(CommonData.OPEN_HISTORY_OR_BOOKMARK_ITEM);
			intent.putExtra(CommonData.ACTION_GOTO_URL, url);
			intent.putExtra(CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_BOOKMARK);
			startActivity(intent);
			overridePendingTransition(0, 0);
		}

		@Override
		public void onCheckedChange() {
			mBookmarkView.updateCheckState();
		}
	};

	private IEditStateObserver mEditStateObserver = new IEditStateObserver() {

		@Override
		public void onEditStateChanged(boolean isEdit) {
			mIsEditing = isEdit;
			if (mPageSelected == ITEM_HISTORY) {
				mHistoryView.changeEditState(mIsEditing);
			} else if (mPageSelected == ITEM_FAVORITE) {
				mBookmarkView.changeEditState(mIsEditing);
			}
		}
	};

	private IHistoryDeleteCallback mHistoryDeleteCallbackImpl = new IHistoryDeleteCallback() {
		@Override
		public void notifyDelete() {
			mIsEditing = false;
			if (mPageSelected == ITEM_HISTORY) {
				if (mHistoryView.isNoHistory()) {
					//mBtnEdit.setEnabled(false);
				} else {
					//mBtnEdit.setEnabled(true);
				}
			}
		}
	};

	private IBookmarkDeleteCallback mBookmarkDeleteCallbackImpl = new IBookmarkDeleteCallback() {
		@Override
		public void notifyDelete() {
			mIsEditing = false;
//			mTitleBar.setSettingTxt(R.string.edit);
			if (mBookmarkView.isNoBookmark() && mPageSelected == ITEM_FAVORITE) {
				//mBtnEdit.setEnabled(false);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bookmark);
		Intent intent = getIntent();
		int selectedPage = intent.getIntExtra("item", 0);
		mInitItem = selectedPage;
		List<String> listStr = new ArrayList<>();
		listStr.add(getString(R.string.history));
		listStr.add(getString(R.string.favorite));
		mHistoryView = new HistoryView(this);
		mHistoryView.init(mHistoryItemClickImpl, mHistoryDeleteCallbackImpl,
				mEditStateObserver, HistoryView.TYPE_HISTORY);
		mBookmarkView = new BookmarkView(this);
		mBookmarkView.init(mBookmarkItemClickImpl, mBookmarkDeleteCallbackImpl,
				mEditStateObserver);
		List<View> listView = new ArrayList<>();
		listView.add(mHistoryView);
		listView.add(mBookmarkView);
//		initSlidingView();
		mViewPager = (CommonTabViewPager) findViewById(R.id.view_pager);
		mViewPager.setRightMenu(this);
		mViewPager.setStyle(CommonTabViewPager.STYLE_GREY);
		mViewPager.setPageViews(listView);
		mViewPager.setTitles(listStr);
		mViewPager.setOnPageChangedListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int item) {
				if (mInitItem == ITEM_FAVORITE) {
					mInitItem = ITEM_HISTORY;
				} else {
					if (item == ITEM_FAVORITE)
						Statistics.sendOnceStatistics(GoogleConfigDefine.HISTORY_BOOKMARK_SWITCH, GoogleConfigDefine.HISTORY_TO_BOOKMARK);
					else
						Statistics.sendOnceStatistics(GoogleConfigDefine.HISTORY_BOOKMARK_SWITCH, GoogleConfigDefine.BOOKMARK_TO_HISTORY);
				}
				if (item == ITEM_FAVORITE) {
					mPageSelected = ITEM_FAVORITE;
					mViewPager.setRightMenu(BookmarkActivity.this);
				} else if (item == ITEM_HISTORY) {
					if (mIsEditing) {
						BookmarkManager.getInstance().restoreBookmark();
					}
					mPageSelected = ITEM_HISTORY;
					mViewPager.hideRightMenu();
				}
				mIsEditing = false;
				changeEditState(mPageSelected);
			}

			// arg0:当前页面，及你点击滑动的页面
			// arg1:当前页面偏移的百分比
			// arg2:当前页面偏移的像素位置
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			// arg0 ==1的时辰默示正在滑动，arg0==2的时辰默示滑动完毕了，arg0==0的时辰默示什么都没做。
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
		mViewPager.setSelectedPage(selectedPage);
		mViewPager.hideRightMenu();
		initEventReceiver();
	}

	private void initEventReceiver() {
		RxBus.get().safetySubscribe(SyncBookmarkEvent.class,this)
				.subscribe(new Consumer<SyncBookmarkEvent>() {
					@Override
					public void accept(SyncBookmarkEvent syncBookmarkEvent) throws Exception {
						switch(syncBookmarkEvent.type){

							case SyncBookmarkEvent.TYPE_MANUAL_SYNC_SUCCESS:
							case SyncBookmarkEvent.TYPE_AUTO_SYNC_SUCCESS:
								refreshLastSyncTime();
								break;
						}
					}
				});
	}

	private void refreshLastSyncTime() {
		mBookmarkView.setSyncTime();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (BookmarkManager.getInstance().isBookMarkChanged) {
			BookmarkManager.getInstance().isBookMarkChanged = false;
			mBookmarkView.changeEditState(mIsEditing);
			mBookmarkView.notifyBookmarkChanged(true,true);
		}
	}

	void syncBookmark() {
		if(!AccountLoginManager.getInstance().isUserLogined()){
			//进入登录页面
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			return;
		}
		Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC,GoogleConfigDefine.BOOKMARK_SYNC_CLICK);
		CustomToastUtils.getInstance().showTextToast(R.string.account_login_sync_bookmarks);
		BookmarkManager.getInstance().syncBookmark(this,true);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (isSlideClose) {
			overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		} else {
			overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
		}
	}

	public void changeEditState(int page) {
		mHistoryView.changeEditState(mIsEditing);
		if (page == ITEM_HISTORY) {
			if (mHistoryView.isNoHistory()) {
				//mBtnEdit.setEnabled(false);
			} else {
				//mBtnEdit.setEnabled(true);
			}
		} else {
			mBookmarkView.changeEditState(mIsEditing);
			if (mBookmarkView.isNoBookmark()) {
				//mBtnEdit.setEnabled(false);
			} else {
				//mBtnEdit.setEnabled(true);
			}
		}
	}

	/**
	 * 退出编辑状态
	 */
//	public void exitEdit() {
//		mIsEditing = false;
//		changeEditState(mPageSelected);
//	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.common_tv_setting:
				mIsEditing = !mIsEditing;
				changeEditState(mPageSelected);
				mBookmarkView.updateCheckState();
				if (mIsEditing) {
					if (mPageSelected == ITEM_FAVORITE) {
						BookmarkManager.getInstance().backupBookmark();
					}
				}
				break;
			case R.id.back:
				onBackPressed();
				break;
			case R.id.common_tab_viewpager_right_menu_iv:
				showImportBookmarkPopWindow();
				break;
			default:
				break;
		}
	}

	private void showImportBookmarkPopWindow() {
		ImportBookmarkPpWindow importBookmarkPpWindow = new ImportBookmarkPpWindow(this,mViewPager.getTopBar());
		importBookmarkPpWindow.showMenu();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		BookmarkManager.getInstance().syncBookmark(this,false);
		Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC,GoogleConfigDefine.BOOKMARK_AUTO_SYNC,
				GoogleConfigDefine.BOOKMARK_EXIT_BOOKMARKACT_SYNC);
		if (mIsEditing && mPageSelected == ITEM_FAVORITE) {
			BookmarkManager.getInstance().restoreBookmark();
		}
		ConfigManager.getInstance().setBookmarkDefaultSelected(mPageSelected);
		mHistoryView.destory();
		mBookmarkView.destory();

	}
}
