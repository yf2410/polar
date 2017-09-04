package com.polar.browser.setting;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.view.switchbutton.SwitchButton;

public class SettingPlugActivity extends LemonBaseActivity implements
		OnClickListener, IConfigObserver,
		android.widget.CompoundButton.OnCheckedChangeListener {

	/**
	 * 视频下载
	 **/
	private SwitchButton mSbVideoDownload;
	/**
	 * 广告拦截
	 **/
	private SwitchButton mSbAdBlock;

	private CommonTitleBar mCommonTitleBar;
	private SwitchButton mSbSuggestion;
	private SwitchButton mSbPriceComparison;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plug_setting);
		initView();
		initPriceComparisonData();
		initData();
		initListener();
	}

	private void initPriceComparisonData() {
		RelativeLayout priceComparison = (RelativeLayout) findViewById(R.id.line_price_comparison);
		if (ConfigManager.getInstance().getServerHasofferEnabled()) {
			mSbPriceComparison = (SwitchButton) findViewById(R.id.sb_price_comparison);
			boolean hasofferEnabled = ConfigManager.getInstance().getHasofferEnabled();
			mSbPriceComparison.setChecked(hasofferEnabled);
			priceComparison.setOnClickListener(this);
			mSbPriceComparison.setOnCheckedChangeListener(this);
			priceComparison.setVisibility(View.VISIBLE);
		} else {
			priceComparison.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ConfigManager.getInstance().unregisterObserver(this);
	}

	private void initView() {
		mCommonTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
		mSbVideoDownload = (SwitchButton) findViewById(R.id.sb_video_download);
		mSbAdBlock = (SwitchButton) findViewById(R.id.sb_ad_block);
		mSbSuggestion = (SwitchButton) findViewById(R.id.sb_suggestion);
	}

	private void initData() {
		// 读取配置，设置初始状态
		boolean isVedioDownload = ConfigManager.getInstance().isVedioDownload();
		boolean isAdBlock = ConfigManager.getInstance().isAdBlock();
		boolean isShowSuggestion = ConfigManager.getInstance().isShowSuggestion();

		mSbVideoDownload.setChecked(isVedioDownload);
		mSbAdBlock.setChecked(isAdBlock);
		mSbSuggestion.setChecked(isShowSuggestion);

		ConfigManager.getInstance().registerObserver(this);
	}

	private void initListener() {
		findViewById(R.id.line_video_download).setOnClickListener(this);
		findViewById(R.id.line_ad_block).setOnClickListener(this);
		findViewById(R.id.line_suggestion_layout).setOnClickListener(this);
		mSbAdBlock.setOnCheckedChangeListener(this);
		mSbSuggestion.setOnCheckedChangeListener(this);
		mSbVideoDownload.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.line_video_download:
				// 视频下载
				onVedioDownloadLineClick();
				break;
			case R.id.line_ad_block:
				// 广告拦截
				onAdBlockLineClick();
				break;
				//比价插件
			case R.id.line_price_comparison:
				onPriceComparisonLineClick();
				break;
			case R.id.line_suggestion_layout:
				onSuggestionLineClick();
			default:
				break;
		}
	}

	private void onSuggestionLineClick() {
		if (mSbSuggestion.isShown()) {
			mSbSuggestion.slideToChecked(!mSbSuggestion.isChecked());
			Statistics.sendOnceStatistics(GoogleConfigDefine.ZOWDOW_SUGGESTION,mSbSuggestion.isChecked()?
					GoogleConfigDefine.SUGGESTION_SWITCH_OFF:GoogleConfigDefine.SUGGESTION_SWITCH_OPEN);
		}
	}

	/**
	 * 视频下载
	 */
	private void onVedioDownloadLineClick() {
		if (mSbVideoDownload.isShown()) {
			mSbVideoDownload.slideToChecked(!mSbVideoDownload.isChecked());
		}
	}

	/**
	 * 广告拦截
	 */
	private void onAdBlockLineClick() {
		if (mSbAdBlock.isShown()) {
			mSbAdBlock.slideToChecked(!mSbAdBlock.isChecked());
		}
	}

	/**
	 * 比价插件
	 */
	private void onPriceComparisonLineClick() {
		if (mSbPriceComparison.isShown()) {
			mSbPriceComparison.slideToChecked(!mSbPriceComparison.isChecked());
		}
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
			Runnable r = new Runnable() {
				@Override
				public void run() {
					if (value) {
					}
					SysUtils.setFullScreen(SettingPlugActivity.this, value);
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		}
	}

	@Override
	public void notifyChanged(String key, String value) {
	}

	@Override
	public void notifyChanged(String key, final int value) {
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean state) {
		switch (v.getId()) {
			case R.id.sb_video_download:
				if (state != ConfigManager.getInstance().isVedioDownload()) {
					ConfigManager.getInstance().setEnableVedioDownload(state);
				}
				break;
			case R.id.sb_ad_block:
				if (state != ConfigManager.getInstance().isAdBlock()) {
					ConfigManager.getInstance().setEnableAdBlock(state);
				}
				break;
			case R.id.sb_price_comparison:
				if(state!=ConfigManager.getInstance().getHasofferEnabled()){
					ConfigManager.getInstance().setHasofferEnabled(state);
				}
				break;
			case R.id.sb_suggestion:
				if(state!=ConfigManager.getInstance().isShowSuggestion()){
					ConfigManager.getInstance().setShowSuggestion(state);
				}
				break;

			default:
				break;
		}
	}
}
