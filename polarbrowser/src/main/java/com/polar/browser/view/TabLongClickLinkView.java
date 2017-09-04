package com.polar.browser.view;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.cropedit.CropStorageUtil;
import com.polar.browser.download.savedpage.SavedPageUtil;
import com.polar.browser.i.IShareClick;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;

public class TabLongClickLinkView extends PopupWindow implements
		OnClickListener {

	public static final int WIDTH_DP = 150;
	public static final int HEIGHT_DP = 180;

	private View mMenuView;
	private LayoutInflater mInflater;

	private String mUrl;
	private String mWebUrl;
	private String mTitle;

	private Context mContext;

	private String mLinkText;

	public TabLongClickLinkView(Context c, String url, String webUrl,
								String title, Bitmap bitmap, IShareClick share) {
		mContext = c;
		mUrl = url;
		mWebUrl = webUrl;
		mTitle = title;
		CropStorageUtil.setBitmap(bitmap);
		initView(c);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.item_open_back_ll:
				dismiss();
				TabViewManager.getInstance().addTabView(mUrl, false, false, false);
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.LONGTOUCHCLICKMENU,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_LINK,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_OPEN_BACK);
				disableAdBlockToast();
				break;
			case R.id.item_open_new_ll:
				dismiss();
				TabViewManager.getInstance().addTabView(mUrl, false, true, false);
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.LONGTOUCHCLICKMENU,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_LINK,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_OPEN_NEW);
				break;
			case R.id.item_save_page_ll:
				dismiss();
				SavedPageUtil.savePage(mContext, mTitle, mWebUrl);
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.LONGTOUCHCLICKMENU,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_LINK,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_SAVE_PAGE);
				break;
			case R.id.item_copy_link_text_ll:
				dismiss();
				CustomToastUtils.getInstance().showTextToast(
						R.string.toast_copy_link_text);
				copyText(mLinkText);
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.LONGTOUCHCLICKMENU,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_LINK,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_COPY_TEXT);
				break;
			case R.id.item_copy_link_ll:
				dismiss();
				CustomToastUtils.getInstance().showTextToast(
						R.string.toast_copy_link);
				copyText(mUrl);
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.LONGTOUCHCLICKMENU,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_LINK,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_COPY_LINK);
				break;
			default:
				break;
		}
	}

	public void setLinkText(String text) {
		mLinkText = text;
	}

	private void initView(Context c) {
		mInflater = LayoutInflater.from(c);
		mMenuView = mInflater.inflate(R.layout.menu_longclick_link, null);
		mMenuView.findViewById(R.id.item_open_back_ll).setOnClickListener(this);
		mMenuView.findViewById(R.id.item_open_new_ll).setOnClickListener(this);
		mMenuView.findViewById(R.id.item_save_page_ll).setOnClickListener(this);
		mMenuView.findViewById(R.id.item_copy_link_text_ll).setOnClickListener(
				this);
		mMenuView.findViewById(R.id.item_copy_link_ll).setOnClickListener(
				this);
		// mMenuBg.setBackgroundColor(mMenuView.getResources().getColor(android.R.color.white));
//		setWidth(DensityUtil.dip2px(c, WIDTH_DP));
//		setHeight(DensityUtil.dip2px(c, HEIGHT_DP));
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
//		setBackgroundDrawable(new ColorDrawable(c.getResources().getColor(android.R.color.white)));
		setBackgroundDrawable(new ColorDrawable(android.R.color.white));
		setContentView(mMenuView);
	}

	private void copyText(String copyText) {
		if (copyText != null && copyText.length() > 0) {
			// 得到剪贴板管理器
			android.content.ClipboardManager cmb = (android.content.ClipboardManager) JuziApp
					.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
			if (!TextUtils.isEmpty(copyText)) {
				cmb.setPrimaryClip(ClipData.newPlainText(null, copyText));
			}
		}
	}

	private void disableAdBlockToast() {
		ConfigManager.getInstance().setAdBlockToast(false);
	}
}
