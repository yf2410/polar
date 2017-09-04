package com.polar.browser.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.cropedit.CropStorageUtil;
import com.polar.browser.download.DownloadHelper;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.video.share.CustomShareDialog;

public class TabLongClickImgView extends PopupWindow implements OnClickListener {

	public static final int WIDTH_DP = 120;
	public static final int HEIGHT_DP = 140;

	private View mMenuView;

	private LayoutInflater mInflater;

	private String mUrl;

	private Context mContext;

	public TabLongClickImgView(Context c, String url, String webUrl,
							   String title, Bitmap bitmap) {
		mContext = c;
		mUrl = url;
		CropStorageUtil.setBitmap(bitmap);
		initView(c);
	}

	@SuppressLint("InflateParams")
	private void initView(Context c) {
		mInflater = LayoutInflater.from(c);
		mMenuView = mInflater.inflate(R.layout.menu_longclick_img, null);
		mMenuView.findViewById(R.id.item_view_img_ll).setOnClickListener(this);
		mMenuView.findViewById(R.id.item_download_img_ll).setOnClickListener(
				this);
		mMenuView.findViewById(R.id.item_share_img_ll).setOnClickListener(this);
//		setWidth(DensityUtil.dip2px(c, WIDTH_DP));
//		setHeight(DensityUtil.dip2px(c, HEIGHT_DP));
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setFocusable(true);
//		setBackgroundDrawable(new ColorDrawable(c.getResources().getColor(android.R.color.white)));
		setBackgroundDrawable(new ColorDrawable(android.R.color.white));
		setContentView(mMenuView);
		// mMenuBg.setBackgroundColor(mMenuView.getResources().getColor(android.R.color.white));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.item_view_img_ll:
				dismiss();
				TabViewManager.getInstance().getCurrentTabView()
						.loadUrl(mUrl, Constants.NAVIGATESOURCE_NORMAL);
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.LONGTOUCHCLICKMENU,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_VIEW_IMAGE);
				break;
			case R.id.item_download_img_ll:
				dismiss();
				String pageUrl = TabViewManager.getInstance().getCurrentUrl();
				DownloadHelper.downloadImg(pageUrl, mUrl,false);
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
						GoogleConfigDefine.LONGTOUCHCLICKMENU,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE,
						GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_SAVE_IMAGE);
				break;
			case R.id.item_share_img_ll:
				dismiss();
				shareImg();
				CropStorageUtil.recycle();
				Statistics.sendOnceStatistics(
								GoogleConfigDefine.LONGTOUCHCLICKMENU,
								GoogleConfigDefine.LONGTOUCHCLICKMENU_TYPE_IMAGE,
								GoogleConfigDefine.LONGTOUCHCLICKMENU_LINK_TYPE_SHARE_IMAGE);
				break;
			default:
				break;
		}
	}

	private void shareImg() {
		CustomShareDialog dialog = new CustomShareDialog((Activity) mContext,mUrl, CustomShareDialog.CHANNEL_IMAGE_SHARE);
		dialog.show();
	}
}
