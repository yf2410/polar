package com.polar.browser.setting;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.bookmark.BookmarkExportActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.common.ui.ListDialog;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.library.utils.ListUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.vclibrary.bean.SearchEngineList;
import com.polar.browser.vclibrary.bean.events.SPConfigChangedEvent;
import com.polar.browser.view.switchbutton.SwitchButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SettingActivity extends LemonBaseActivity implements
		OnClickListener, IConfigObserver,
		android.widget.CompoundButton.OnCheckedChangeListener {

	public static final String ACTION_EMPHASIZE_SLIDING = "com.polar.browser.ACTION_EMPHASIZE_SLIDING";

	/**
	 * 显示字号的TextView
	 **/
	//private TextView mTvFontSize;

	private TextView mTvCustomUa;

	private TextView mTvSlidingType;
	private TextView mTvSearchEngine;

	private CommonTitleBar mCommonTitleBar;

	/**
	 * 竖屏锁定
	 **/
	private SwitchButton mSbScreenLock;
	/**
	 * 全屏模式
	 **/
	//private SwitchButton mSbFullScreen;
	/**
	 * 快捷搜索
	 **/
	private SwitchButton mSbQuickSearch;
	/**
	 * 安全警告
	 **/
	private SwitchButton mSbSafetyTip;
	/**
	 * 退出时保留标签
	 **/
	private SwitchButton mSbSaveTab;
	/**
	 * 长访问记录
	 **/
	private SwitchButton mSbHistoryVisited;

	private String type;
	/**
	 * Notification管理
	 */
	private NotificationManager mNotificationManager;

	/**
	 * 首页卡片管理
	 */
	private RelativeLayout mCardManage;
	private SearchEngineList engineList;
	/**
	 * 字体大小条目点击监听
	 */
	/*private OnItemClickListener mFontListItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			switch (position) {
				case 0:
					SimpleLog.d("FONT_SIZE", "小");
					mTvFontSize.setText(R.string.setting_font_size_min);
					ConfigManager.getInstance().setFontSize(
							ConfigDefine.FONT_SIZE_MIN);
					break;
				case 1:
					SimpleLog.d("FONT_SIZE", "中");
					mTvFontSize.setText(R.string.setting_font_size_mid);
					ConfigManager.getInstance().setFontSize(
							ConfigDefine.FONT_SIZE_MID);
					break;
				case 2:
					SimpleLog.d("FONT_SIZE", "大");
					mTvFontSize.setText(R.string.setting_font_size_big);
					ConfigManager.getInstance().setFontSize(
							ConfigDefine.FONT_SIZE_BIG);
					break;
				case 3:
					SimpleLog.d("FONT_SIZE", "特大");
					mTvFontSize.setText(R.string.setting_font_size_large);
					ConfigManager.getInstance().setFontSize(
							ConfigDefine.FONT_SIZE_LARGE);
					break;
				default:
					break;
			}
		}
	};*/
	private OnItemClickListener mCustomUaItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			switch (position) {
				case 0:
					mTvCustomUa.setText(R.string.custom_ua_default);
					TabViewManager.getInstance().setUa(ConfigManager.getInstance().getDefaultUa());
					break;
				case 1:
					mTvCustomUa.setText(R.string.custom_ua_pc);
					TabViewManager.getInstance().setUa(CommonData.UA_PC);
					break;
				case 2:
					mTvCustomUa.setText(R.string.custom_ua_ios);
					TabViewManager.getInstance().setUa(CommonData.UA_IPHONE6);
					break;
				case 3:
					mTvCustomUa.setText(R.string.custom_ua_custom);
					showCustomUaDialog();
					break;
				default:
					break;
			}
			ConfigManager.getInstance().setUaType(position);
		}
	};
	/**
	 * 滑屏前进后退条目点击监听
	 */
	private OnItemClickListener mSlidingTypeClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			switch (position) {
				case ConfigDefine.SLIDING_BACK_FORWARD_close:
					mTvSlidingType.setText(R.string.setting_sliding_type_close);
					break;
				case ConfigDefine.SLIDING_BACK_FORWARD_border:
					mTvSlidingType.setText(R.string.setting_sliding_type_border);
					break;
				case ConfigDefine.SLIDING_BACK_FORWARD_fullscreen:
					mTvSlidingType.setText(R.string.setting_sliding_type_fullscreen);
					break;
				default:
					break;
			}
			ConfigManager.getInstance().setSlidingScreenMode(position);
		}
	};
	/**
	 * 搜索引擎条目点击监听
	 */
	private OnItemClickListener mSearchEngineClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			ConfigManager.getInstance().setDefaultSearchEngineModified();
			if(engineList != null && !ListUtils.isEmpty(engineList.getDataList()) ){
				try {
					mTvSearchEngine.setText(
							SearchUtils.getMultiLanByEn(engineList.getDataList().get(position).getEngineName()));
					ConfigManager.getInstance().setSearchEngine(position,true);
				}catch (NullPointerException e){
					e.printStackTrace();
				}
			}
		/*	else{
				switch (position) {
					case ConfigDefine.SEARCH_ENGINE_GOOGLE:
						mTvSearchEngine.setText(R.string.setting_search_engine_google);
						ConfigManager.getInstance().setSearchEngine(ConfigDefine.SEARCH_ENGINE_GOOGLE,true);
						break;
					case ConfigDefine.SEARCH_ENGINE_BING:
						mTvSearchEngine.setText(R.string.setting_search_engine_bing);
						ConfigManager.getInstance().setSearchEngine(ConfigDefine.SEARCH_ENGINE_BING,true);
						break;
					case ConfigDefine.SEARCH_ENGINE_YAHOO:
						mTvSearchEngine.setText(R.string.setting_search_engine_yahoo);
						ConfigManager.getInstance().setSearchEngine(ConfigDefine.SEARCH_ENGINE_YAHOO,true);
						break;
					case ConfigDefine.SEARCH_ENGINE_YANDEX:
						mTvSearchEngine.setText(R.string.setting_search_engine_yandex);
						ConfigManager.getInstance().setSearchEngine(ConfigDefine.SEARCH_ENGINE_YANDEX,true);
						break;
					case ConfigDefine.SEARCH_ENGINE_DUCKGO:
						mTvSearchEngine.setText(R.string.setting_search_engine_duckgo);
						ConfigManager.getInstance().setSearchEngine(ConfigDefine.SEARCH_ENGINE_DUCKGO,true);
						break;
					case ConfigDefine.SEARCH_ENGINE_YOUTUBE:
						mTvSearchEngine.setText(R.string.setting_search_engine_youtube);
						ConfigManager.getInstance().setSearchEngine(ConfigDefine.SEARCH_ENGINE_YOUTUBE,true);
						break;
					case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
						mTvSearchEngine.setText(R.string.setting_search_engine_google_quick);
						ConfigManager.getInstance().setSearchEngine(ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK,true);
						break;
					default:
						break;
				}
			}*/

		}
	};

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		initView();
		initData();
		initListener();
		//若从首页弹窗跳转设置划屏手势，应将划屏条目划至屏幕内，再显示动画
		// （暂时将scroll划至一半高度显示屏幕，后期可能设置条目增加需要测量view高度计算显示高度）
		if (ACTION_EMPHASIZE_SLIDING.equals(getIntent().getAction())) {
			ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
				@Override
				public void run() {
					final TextView tvSliding = (TextView) findViewById(R.id.sliding_type);
					final ScrollView scrollView = (ScrollView) findViewById(R.id.setting_scrollview);
					scrollView.scrollTo(0, scrollView.getHeight()/2);
					emphasizeSliding(tvSliding);
//					RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.line_sliding_type);
//					int top = relativeLayout.getTop();
//					new RelativeLayout((Context) relativeLayout.getParent()).getTop();
//					int screenHeight = AppEnv.SCREEN_HEIGHT;
//					int measuredHeight = scrollView.getMeasuredHeight();
				}
			},800);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ConfigManager.getInstance().unregisterObserver(this);
		EventBus.getDefault().unregister(this);
	}

	private void initView() {
		mCommonTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
		//mTvFontSize = (TextView) findViewById(R.id.tv_font_size);
		mTvCustomUa = (TextView) findViewById(R.id.tv_ua_type);
		mTvSlidingType = (TextView) findViewById(R.id.tv_sliding_type);
		mTvSearchEngine = (TextView) findViewById(R.id.tv_search_engine);
		mSbScreenLock = (SwitchButton) findViewById(R.id.sb_screen_lock);
		//mSbFullScreen = (SwitchButton) findViewById(R.id.sb_full_screen);
		mSbSaveTab = (SwitchButton) findViewById(R.id.sb_save_tab);
		mSbQuickSearch = (SwitchButton) findViewById(R.id.sb_quick_search);
		mSbSafetyTip = (SwitchButton) findViewById(R.id.sb_safety_tip);
		mSbHistoryVisited = (SwitchButton) findViewById(R.id.sb_history_visited);
	}

	private void initData() {
		initEngineData();
		// 读取配置，设置初始状态
		int fontSize = ConfigManager.getInstance().getFontSize();
		int type = ConfigManager.getInstance().getSlidingScreenMode();
		int searchEngine = ConfigManager.getInstance().getSearchEngine();
		int uaType = ConfigManager.getInstance().getUaType();
		boolean isScreenLock = ConfigManager.getInstance().isScreenLock();
		boolean isSaveTab = ConfigManager.getInstance().isSaveTab();
		boolean isFullScreen = ConfigManager.getInstance().isFullScreen();
		boolean isQuickSearch = ConfigManager.getInstance().isQuickSearch();
		boolean safetyWarningEnabled = ConfigManager.getInstance().isSafetyWarningEnabled();
		boolean historyVisitedEnabled = ConfigManager.getInstance().isHistoryVisitedEnabled();
		//changeFontSizeText(fontSize);
		changeSLidingType(type);
		changeSearchEngine(searchEngine);
		changeUaType(uaType);
		mSbScreenLock.setChecked(isScreenLock);
		//mSbFullScreen.setChecked(isFullScreen);
		mSbQuickSearch.setChecked(isQuickSearch);
		mSbSaveTab.setChecked(isSaveTab);
		mSbSafetyTip.setChecked(safetyWarningEnabled);
		mSbHistoryVisited.setChecked(historyVisitedEnabled);
		ConfigManager.getInstance().registerObserver(this);
		EventBus.getDefault().register(this);
	}

	private void initEngineData() {
		String engineListJson = ConfigManager.getInstance().getLastEngineList();
		Gson gson = new Gson();
		engineList = gson.fromJson(engineListJson, SearchEngineList.class);
	}

	private void initListener() {
		findViewById(R.id.line_plug_center).setOnClickListener(this);
		findViewById(R.id.line_search_engine).setOnClickListener(this);
		findViewById(R.id.line_saved_account).setOnClickListener(this);
		//findViewById(R.id.line_ad_block).setOnClickListener(this);
		findViewById(R.id.line_download_setting).setOnClickListener(this);
		//findViewById(R.id.line_font_size).setOnClickListener(this);
		findViewById(R.id.line_sliding_type).setOnClickListener(this);
		findViewById(R.id.line_bookmark_backup).setOnClickListener(this);
		findViewById(R.id.line_screen_lock).setOnClickListener(this);
		findViewById(R.id.line_clear_data).setOnClickListener(this);
		findViewById(R.id.line_recover_setting).setOnClickListener(this);
		findViewById(R.id.line_set_default_browser).setOnClickListener(this);
		findViewById(R.id.line_feedback).setOnClickListener(this);
		//findViewById(R.id.line_full_screen).setOnClickListener(this);
		findViewById(R.id.line_quick_search).setOnClickListener(this);
		findViewById(R.id.line_set_user_agent).setOnClickListener(this);
		findViewById(R.id.line_save_tab).setOnClickListener(this);
		findViewById(R.id.line_product_about).setOnClickListener(this);
		findViewById(R.id.line_safety_tip).setOnClickListener(this);
		findViewById(R.id.line_app_about).setOnClickListener(this);
		findViewById(R.id.line_notify).setOnClickListener(this);
		findViewById(R.id.line_history_visited).setOnClickListener(this);
		mSbScreenLock.setOnCheckedChangeListener(this);
		//mSbFullScreen.setOnCheckedChangeListener(this);
		mSbSaveTab.setOnCheckedChangeListener(this);
		mSbQuickSearch.setOnCheckedChangeListener(this);
		mSbSafetyTip.setOnCheckedChangeListener(this);
		mSbHistoryVisited.setOnCheckedChangeListener(this);
	}

	/**
	 * 根据字体大小，设置mTvFontSize显示的字号
	 *
	 * @param fontSize
	 */
	/*private void changeFontSizeText(int fontSize) {
		switch (fontSize) {
			case ConfigDefine.FONT_SIZE_MIN:
				mTvFontSize.setText(R.string.setting_font_size_min);
				break;
			case ConfigDefine.FONT_SIZE_MID:
				mTvFontSize.setText(R.string.setting_font_size_mid);
				break;
			case ConfigDefine.FONT_SIZE_BIG:
				mTvFontSize.setText(R.string.setting_font_size_large);
				break;
			case ConfigDefine.FONT_SIZE_LARGE:
				mTvFontSize.setText(R.string.setting_font_size_large);
				break;
			default:
				break;
		}
	}*/

	/**
	 * 根据滑屏前进后退类型，进行设置
	 *
	 * @param type
	 */
	private void changeSLidingType(int type) {
		switch (type) {
			case ConfigDefine.SLIDING_BACK_FORWARD_close:
				mTvSlidingType.setText(R.string.setting_sliding_type_close);
				break;
			case ConfigDefine.SLIDING_BACK_FORWARD_border:
				mTvSlidingType.setText(R.string.setting_sliding_type_border);
				break;
			case ConfigDefine.SLIDING_BACK_FORWARD_fullscreen:
				mTvSlidingType.setText(R.string.setting_sliding_type_fullscreen);
				break;
			default:
				break;
		}
	}

	private void changeUaType(int type) {
		switch (type) {
			case ConfigDefine.UA_TYPE_DEFAULT:
				mTvCustomUa.setText(R.string.custom_ua_default);
				break;
			case ConfigDefine.UA_TYPE_PC:
				mTvCustomUa.setText(R.string.custom_ua_pc);
				break;
			case ConfigDefine.UA_TYPE_IOS:
				mTvCustomUa.setText(R.string.custom_ua_ios);
				break;
			case ConfigDefine.UA_TYPE_CUSTOM:
				mTvCustomUa.setText(R.string.custom_ua_custom);
				break;
			default:
				break;
		}
	}

	/**
	 * 根据搜索引擎设置条目上文字
	 *
	 * @param searchEngine
	 */
	private void changeSearchEngine(int searchEngine) {

		if(engineList!=null&&!ListUtils.isEmpty(engineList.getDataList())){
			try{
				mTvSearchEngine.setText(
						SearchUtils.getMultiLanByEn(engineList.getDataList().get(searchEngine).getEngineName()));
			}catch (NullPointerException e){
				e.printStackTrace();
			}
		}
/*		else{
			switch (searchEngine) {
				case ConfigDefine.SEARCH_ENGINE_GOOGLE:
					mTvSearchEngine.setText(R.string.setting_search_engine_google);
					break;
				case ConfigDefine.SEARCH_ENGINE_BING:
					mTvSearchEngine.setText(R.string.setting_search_engine_bing);
					break;
				case ConfigDefine.SEARCH_ENGINE_YAHOO:
					mTvSearchEngine.setText(R.string.setting_search_engine_yahoo);
					break;
				case ConfigDefine.SEARCH_ENGINE_YANDEX:
					mTvSearchEngine.setText(R.string.setting_search_engine_yandex);
					break;
				case ConfigDefine.SEARCH_ENGINE_DUCKGO:
					mTvSearchEngine.setText(R.string.setting_search_engine_duckgo);
					break;
				case ConfigDefine.SEARCH_ENGINE_YOUTUBE:
					mTvSearchEngine.setText(R.string.setting_search_engine_youtube);
					break;
				case ConfigDefine.SEARCH_ENGINE_GOOGLE_QUICK:
					mTvSearchEngine.setText(R.string.setting_search_engine_google_quick);
					break;
				default:
					break;
			}
		}*/

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.line_plug_center:	// 去往插件中心
				startActivity(new Intent(this, SettingPlugActivity.class));
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				type = GoogleConfigDefine.SETTING_TYPE_GOPLUG;
				break;
			/*case R.id.line_font_size: // 更改字体大小
				onFontSizeLineClick();
				type = GoogleConfigDefine.SETTING_TYPE_FONTSIZE;
				break;*/
			case R.id.line_sliding_type: // 更改滑屏前进后退方式
				onSlidingTypeClick();
				type = GoogleConfigDefine.SETTING_TYPE_SLIDINGTYPE;
				break;
			case R.id.line_bookmark_backup: //导出收藏夹
				onBookmarkExportClick();
				type = GoogleConfigDefine.SETTING_TYPE_BOOKMARKBACKUP;
				break;
			case R.id.line_screen_lock: // 竖屏锁定
				onScreenLockLineClick();
				type = GoogleConfigDefine.SETTING_TYPE_SCREENLOCK;
				break;
			case R.id.line_save_tab: // 退出时保存标签
				onSaveTabLineClick();
				type = GoogleConfigDefine.SETTING_TYPE_SAVETAB;
				break;
			/*case R.id.line_full_screen: // 全屏模式
				onFullScreenClick();
				type = GoogleConfigDefine.SETTING_TYPE_FULLSCREEN;
				break;*/
			case R.id.line_quick_search: // 快捷搜索
				onQuickSearchClick();
				type = GoogleConfigDefine.SETTING_TYPE_QUICKSEARCH;
				break;
			case R.id.line_safety_tip: // 安全警告
				onSafetyTipClick();
				type = GoogleConfigDefine.SETTING_TYPE_SAFETY_TIP;
				break;
			case R.id.line_history_visited: // 最长访问记录
				onHistoryVisitedClick();
				type = GoogleConfigDefine.SETTING_TYPE_HISTORY_VISITED;
				break;
			case R.id.line_clear_data: // 清除个人数据
				onClearDataLineClick();
				type = GoogleConfigDefine.SETTING_TYPE_CLEARDATA;
				break;
			case R.id.line_set_default_browser: // 设置默认浏览器
				onSetDefaultBrowserLineClick();
				type = GoogleConfigDefine.SETTING_TYPE_SETDEFAULT;
				break;
			case R.id.line_download_setting: // 下载设置
				onDownloadSettingLineClick();
				type = GoogleConfigDefine.SETTING_TYPE_DOWNLOAD_SETTING;
				break;
			case R.id.line_set_user_agent:
				onSetCustomUa();
				type = GoogleConfigDefine.SETTING_TYPE_CUSTOMUA;
				break;
			case R.id.line_recover_setting: // 恢复默认设置
				onRecoverSettingLineClick();
				type = GoogleConfigDefine.SETTING_TYPE_RECOVERSETTING;
				break;
			case R.id.line_feedback: // 反馈
				onFeedback();
				type = GoogleConfigDefine.SETTING_TYPE_FEEDBACK;
				break;
			case R.id.line_search_engine: // 自定义搜索引擎
				onSetSearchEngine();
				type = GoogleConfigDefine.SETTING_TYPE_SEARCHENGINE;
				break;
			case R.id.line_saved_account: // 保存的账号和密码
				startActivity(new Intent(this, SavedAccountActivity.class));
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				type = GoogleConfigDefine.SETTING_TYPE_GOSAVEDACCOUNT;
				break;
			case R.id.line_product_about: // 产品介绍页
				String loadHelpUrl = Statistics.getLoadHelpUrl();
				Intent intent = new Intent(this, BrowserActivity.class);
				intent.setAction(CommonData.ACTION_OPEN_PRODUCT_ABOUT);
				intent.putExtra(CommonData.SYSTEM_CONTENT_URL, loadHelpUrl);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				type = GoogleConfigDefine.SETTING_TYPE_PRODUCT_ABOUT;
//				TabViewManager tabViewManager = TabViewManager.getInstance();
//				if (tabViewManager == null) {
//					return;
//				}
//				if (!tabViewManager.isCurrentHome()) {
//					tabViewManager.addTabView(true);
//					tabViewManager.showContentHideHome();
//				}
//				String loadHelpUrl = Statistics.getLoadHelpUrl();
//				tabViewManager.loadUrl(loadHelpUrl, Constants.NAVIGATESOURCE_PRODUCT_ABOUT);
//				Statistics.sendOnceStatistics(
//						GoogleConfigDefine.SETTING, GoogleConfigDefine.SETTING_TYPE_PRODUCT_ABOUT);
//				finish();
				break;
			case R.id.line_app_about:
				onAppAboutClick();
				type = GoogleConfigDefine.SETTING_TYPE_APP_ABOUT;
				break;
			/*case R.id.line_ad_block:
				onAdSettingClick();
				type = GoogleConfigDefine.AB_ADBLOCK_MENU_CLICK;
				break;*/
			case R.id.line_notify:
				openNotifyManager();
				type = GoogleConfigDefine.SETTING_TYPE_NOTIFY;
			default:

				break;
		}
		sendGoogleStatistics(type);
	}

	private void openNotifyManager() {
		startActivity(new Intent(this, NotifyManagerActivity.class));
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}
	/*private void onAdSettingClick() {
		startActivity(new Intent(this,AdBlockSettingActivity.class));
	}*/

	private void sendGoogleStatistics(String type) {
		Statistics.sendOnceStatistics(GoogleConfigDefine.SETTING, type);
	}

	private void onSetCustomUa() {
		ListDialog dialog = new ListDialog(this);
		String[] items = {getString(R.string.custom_ua_default),
				getString(R.string.custom_ua_pc),
				getString(R.string.custom_ua_ios),
				getString(R.string.custom_ua_custom)};
		int selectItem = ConfigManager.getInstance().getUaType();
		dialog.setItems(items, selectItem);
		dialog.setOnItemClickListener(mCustomUaItemClickListener);
		dialog.show();
	}

	private void showCustomUaDialog() {
		final CommonDialog dialog = new CommonDialog(this);
		dialog.setBottomView(R.layout.view_bottom_bar);
		dialog.setTitleView(R.layout.view_title);
		dialog.setCenterView(R.layout.view_custom_ua_center);
		final EditText et = (EditText) dialog.findViewById(R.id.et_title);
		et.requestFocus();
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_edit);
		TextView confirm = (TextView) dialog.findViewById(R.id.tv_add);
		TextView cancel = (TextView) dialog.findViewById(R.id.tv_cancel);
		confirm.setText(getString(R.string.ok));
		cancel.setText(getString(R.string.cancel));
		tvTitle.setText(getString(R.string.custom_ua_title));
		String ua = ConfigManager.getInstance().getCustomUa();
		et.setText(ua);
		et.setSelection(ua.length());
		ConfigManager.getInstance().setUa(ua);
		TabViewManager.getInstance().setUa(ua);
		et.setHint(getString(R.string.custom_ua_input_hint));
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				String ua = et.getText().toString();
				ConfigManager.getInstance().setUa(ua);
				ConfigManager.getInstance().setCustomUa(ua);
				TabViewManager.getInstance().setUa(ua);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void onFeedback() {
		startActivity(new Intent(this, FeedBackActivity.class));
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	/**
	 * 更改字体大小
	 */
	/*private void onFontSizeLineClick() {
		ListDialog dialog = new ListDialog(this);
		String[] items = {getString(R.string.setting_font_size_min),
				getString(R.string.setting_font_size_mid),
				getString(R.string.setting_font_size_big),
				getString(R.string.setting_font_size_large)
		};
		int selectItem = getFontSelectedIndex(ConfigManager.getInstance()
				.getFontSize());
		dialog.setItems(items, selectItem);
		dialog.setOnItemClickListener(mFontListItemClickListener);
		dialog.show();
	}*/

	/**
	 * 更改滑屏前进后退的类型
	 */
	private void onSlidingTypeClick() {
		ListDialog dialog = new ListDialog(this);
		String[] items = {getString(R.string.setting_sliding_type_close),
				getString(R.string.setting_sliding_type_border),
				getString(R.string.setting_sliding_type_fullscreen)};
		int selectItem = ConfigManager.getInstance().getSlidingScreenMode();
		dialog.setItems(items, selectItem);
		dialog.setOnItemClickListener(mSlidingTypeClickListener);
		dialog.show();
	}

	private void onBookmarkExportClick() {
		startActivity(new Intent(this, BookmarkExportActivity.class));
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	private void onAppAboutClick() {
		startActivity(new Intent(this, SettingAboutActivity.class));
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	private int getFontSelectedIndex(int fontSize) {
		if (fontSize == ConfigDefine.FONT_SIZE_MIN) {
			return 0;
		}
		if (fontSize == ConfigDefine.FONT_SIZE_MID) {
			return 1;
		}
		if (fontSize == ConfigDefine.FONT_SIZE_BIG) {
			return 2;
		}
		if (fontSize == ConfigDefine.FONT_SIZE_LARGE) {
			return 3;
		}
		return 0;
	}

	/**
	 * 退出保存标签
	 */
	private void onSaveTabLineClick() {
		if (mSbSaveTab.isShown()) {
			mSbSaveTab.slideToChecked(!mSbSaveTab.isChecked());
		}
	}

	/**
	 * 竖屏锁定
	 */
	private void onScreenLockLineClick() {
		if (mSbScreenLock.isShown()) {
			mSbScreenLock.slideToChecked(!mSbScreenLock.isChecked());
		}
	}

	/**
	 * 全屏模式
	 */
	/*private void onFullScreenClick() {
		if (mSbFullScreen.isShown()) {
			mSbFullScreen.slideToChecked(!mSbFullScreen.isChecked());
		}
	}*/

	/**
	 * 快捷搜索
	 */
	private void onQuickSearchClick() {
		if (mSbQuickSearch.isShown()) {
			mSbQuickSearch.slideToChecked(!mSbQuickSearch.isChecked());
		}
}

	/**
	 * 安全警告
	 */
	private void onSafetyTipClick() {
		if (mSbSafetyTip.isShown()) {
			mSbSafetyTip.slideToChecked(!mSbSafetyTip.isChecked());
		}
	}

	/**
	 * 长访问记录
	 */
	private void onHistoryVisitedClick() {
		if (mSbHistoryVisited.isShown()) {
			mSbHistoryVisited.slideToChecked(!mSbHistoryVisited.isChecked());
			Statistics.sendOnceStatistics(GoogleConfigDefine.OFTEN_HISTORY_VISITED,mSbHistoryVisited.isChecked()?
					GoogleConfigDefine.VISITED_SETTING_CLOSE:GoogleConfigDefine.VISITED_SETTING_OPEN);
		}
	}

	/**
	 * 清除个人数据
	 */
	private void onClearDataLineClick() {
		startActivity(new Intent(this, ClearDataActivity.class));
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	/**
	 * 清除个人数据
	 */
	private void onDownloadSettingLineClick() {
		startActivity(new Intent(this, SettingDownloadActivity.class));
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	/**
	 * 设置默认浏览器
	 */
	private void onSetDefaultBrowserLineClick() {
		startActivity(new Intent(this, SetDefaultBrowserActivity.class));
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
	}

	/**
	 * 恢复默认设置
	 */
	private void onRecoverSettingLineClick() {
		final CommonDialog dialog = new CommonDialog(this, R.string.tips,
				R.string.recover_setting_content);
		dialog.setBtnCancel(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setBtnOk(getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				mTvSlidingType.setText(R.string.setting_sliding_type_border);
				ConfigManager.getInstance().resetToDefault();
				setBrightness(-1);
				switch2nightMode(false);
			}
		});
		dialog.show();
	}

	private void switch2nightMode(boolean nightMode) {
		TabViewManager.getInstance().enableNightMode(nightMode);
		TabViewManager.getInstance().forceCaptureScreen();
		//mToolbarMenuView.refreshBtnNightModeUI(nightMode);
	}

	/**
	 * 设置搜索引擎
	 */
	private void onSetSearchEngine() {
		ListDialog dialog = new ListDialog(this);
		String[] items = createItems();
		int selectItem = ConfigManager.getInstance().getSearchEngine();
		dialog.setItems(items, selectItem);
		dialog.setOnItemClickListener(mSearchEngineClickListener);
		dialog.show();
	}

	private String[] createItems() {

		ArrayList<String> list = new ArrayList<>();
		if (engineList != null && !ListUtils.isEmpty(engineList.getDataList())) {

			Collections.sort(engineList.getDataList());
			for (SearchEngineList.DataListBean dataListBean : engineList.getDataList()) {
				if(dataListBean == null || dataListBean.getEngineName() == null) continue;
				list.add(SearchUtils.getMultiLanByEn(dataListBean.getEngineName()));
			}
		}else{
			return createDefaultItems();
		}
		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}

	private String[] createDefaultItems() {

		List<String> list = new ArrayList<>();
		for(Map.Entry<String,int[]> entry : ConfigDefine.SEARCH_ENGINE_NAME_MAP.entrySet()){
			if(entry == null || entry.getValue() == null || entry.getValue().length < 2) continue;
			list.add(getResources().getString(entry.getValue()[1]));
		}
		String[] array = new String[list.size()];
		list.toArray(array);

		return array;
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
	}

	@Override
	public void notifyChanged(String key, final boolean value) {
		if (key.equals(ConfigDefine.ENABLE_SCREEN_LOCK)) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					mSbScreenLock.setChecked(value);
					if (value) {
						// 竖屏锁定开启，若不是竖屏，强制切换为竖屏
						if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
						}
					} else {
						// 设置跟随系统
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					}
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		} else if (key.equals(ConfigDefine.ENABLE_FULL_SCREEN)) {
			/*Runnable r = new Runnable() {
				@Override
				public void run() {
					mSbFullScreen.setChecked(value);
					if (value) {
					}
					SysUtils.setFullScreen(SettingActivity.this, value);
				}
			};
			ThreadManager.postTaskToUIHandler(r);*/
		} else if (key.equals(ConfigDefine.ENABLE_SAVE_TAB)) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					mSbSaveTab.setChecked(value);
					if (value) {
					}
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		} else if (key.equals(ConfigDefine.ENABLE_QUICK_SEARCH)) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					mSbQuickSearch.setChecked(value);
					if (value) {
						shwoNotify();
					} else {
						clearNotify();
					}
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		} else if (key.equals(ConfigDefine.SERVER_CARD_NEWS_ENABLE)) {
			if (value) {
				mCardManage.setVisibility(View.VISIBLE);
			} else {
				mCardManage.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void notifyChanged(String key, String value) {
	}

	@Override
	public void notifyChanged(String key, final int value) {
		if (key.equals(ConfigDefine.FONT_SIZE)) {
			/*Runnable r = new Runnable() {
				@Override
				public void run() {
					changeFontSizeText(value);
				}
			};
			ThreadManager.postTaskToUIHandler(r);*/
		} else if (key.equals(ConfigDefine.SLIDING_BACK_FORWARD)) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					changeSLidingType(value);
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		} else if (key.equals(ConfigDefine.UA_TYPE)) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					changeUaType(value);
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		} else if (key.equals(ConfigDefine.SEARCH_ENGINE)) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					changeSearchEngine(value);
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean state) {
		switch (v.getId()) {
			case R.id.sb_screen_lock:
				if (state != ConfigManager.getInstance().isScreenLock()) {
					ConfigManager.getInstance().setEnableScreenLock(state);
				}
				break;
			/*case R.id.sb_full_screen:
				if (state != ConfigManager.getInstance().isFullScreen()) {
					ConfigManager.getInstance().setEnableFullScreen(state);
				}
				break;*/
			case R.id.sb_quick_search:
				if (state != ConfigManager.getInstance().isQuickSearch()) {
					ConfigManager.getInstance().setEnableQuickSearch(state);
				}
				break;
			case R.id.sb_safety_tip:
				if (state != ConfigManager.getInstance().isSafetyWarningEnabled()) {
					ConfigManager.getInstance().setSafetyWarningEnabled(state);
				}
				break;
			case R.id.sb_save_tab:
				if (state != ConfigManager.getInstance().isSaveTab()) {
					ConfigManager.getInstance().setEnableSaveTab(state);
				}
				break;
			case R.id.sb_history_visited:
				if (state != ConfigManager.getInstance().isHistoryVisitedEnabled()) {
					ConfigManager.getInstance().setHistoryVisitedEnabled(state);
				}
				break;
			default:
				break;
		}
	}
//	private boolean isSlideClose;
//	@SuppressWarnings("deprecation")
//	private void initSlidingView() {
//		BitmapDrawable drawable = new BitmapDrawable(
//				BrowserActivity.getScreenBitmap());
//		SlidingViewHelper helper = new SlidingViewHelper(drawable);
//		mSlidingMenu = helper.getSlidingView(this);
//
//		mSlidingMenu.setOnOpenedListener(new OnOpenedListener() {
//			@Override
//			public void onOpened() {
//				isSlideClose = true;
//				onBackPressed();
//			}
//		});
//	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	private void emphasizeSliding(final View view) {
		//初始化
		Animation scaleAnimation = new ScaleAnimation(1f, 1.5f, 1f, 1.5f);
		//设置动画时间
		scaleAnimation.setDuration(1000);
		scaleAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Animation scaleAnimation = new ScaleAnimation(1.5f, 1f, 1.5f, 1f);
				//设置动画时间
				scaleAnimation.setDuration(1000);
				view.startAnimation(scaleAnimation);
			}
		});
		view.startAnimation(scaleAnimation);
	}

	private void shwoNotify() {
		NotificationCompat.Builder mBuilder = new Builder(this);
		RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.notifi_search);
		Intent buttonIntent1 = new Intent(getApplicationContext(), BrowserActivity.class);
		buttonIntent1.setAction(CommonData.QUICK_SEARCH_SEARCH);
		PendingIntent pendingIntent1 = PendingIntent
				.getActivity(this, 1, buttonIntent1, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.search_area, pendingIntent1);
		Intent buttonIntent2 = new Intent(getApplicationContext(), BrowserActivity.class);
		buttonIntent2.setAction(CommonData.QUICK_SEARCH_SETTING);
		PendingIntent pendingIntent2 = PendingIntent
				.getActivity(this, 2, buttonIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
		mRemoteViews.setOnClickPendingIntent(R.id.btn_gosetting, pendingIntent2);
		mBuilder.setContent(mRemoteViews)
				.setContentIntent(pendingIntent1)
				.setContentIntent(pendingIntent2)
				.setSmallIcon(R.drawable.notifi_icon);
		Notification notify = mBuilder.build();
		notify.flags = Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(CommonData.QUICK_SEARCH_ID, notify);
	}

	/**
	 * 清除当前创建的通知栏
	 */
	public void clearNotify() {
		mNotificationManager.cancel(CommonData.QUICK_SEARCH_ID);//删除一个特定的通知ID对应的通知
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSPConfigChanged(SPConfigChangedEvent spConfigChangedEvent) {
		String configDefineValue = spConfigChangedEvent.getConfigDefineValue();
		boolean value = spConfigChangedEvent.isValue();
		if (TextUtils.isEmpty(configDefineValue)) {
			return;
		}
		switch (configDefineValue) {
			case ConfigDefine.SAFETY_WARNING:
				mSbSafetyTip.setChecked(value);
				break;
			default:
				break;
		}
	}
}
