package com.polar.browser.impl;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.PopupWindow;

import com.polar.browser.i.IShareClick;
import com.polar.browser.i.ITouchListener;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.ViewUtils;
import com.polar.browser.view.TabLongClickImgView;
import com.polar.browser.view.TabLongClickLinkView;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class TabViewOnLongClickListener implements OnLongClickListener,
		ITouchListener {
	private static final String TAG = "TabViewOnLongClickListener";
	private float mTouchX;
	private float mTouchY;
	private float mWidth;
	private float mHeight;
	private View mAnchorView;
	private Activity mActivity;
	private static Handler mHandler;
	private TabLongClickWebNewsItemView mCurMenuNewsItemView;
	private static final int FOCUS_NODE_HREF = 102;
	private TabLongClickLinkView mCurMenuView;
	private IShareClick mShare;

	public TabViewOnLongClickListener(Activity activity, float width,
									  float height, View anchorView, IShareClick share) {
		mWidth = width;
		mHeight = height;
		mAnchorView = anchorView;
		mActivity = activity;
		mShare = share;
		startHandler();
	}

	private void startHandler() {
		mHandler = new MyHandler(new WeakReference<>(this));
	}

	private static class MyHandler extends Handler{
		WeakReference<TabViewOnLongClickListener> reference ;

		MyHandler(WeakReference<TabViewOnLongClickListener> reference) {
			this.reference = reference;
		}

		@Override
		public void handleMessage(Message msg) {
			TabViewOnLongClickListener outView = reference.get();
			switch (msg.what) {

				case FOCUS_NODE_HREF: {
					if(outView!=null){
						Bitmap bitmap = ViewUtils.takeScreenShot(outView.mActivity, true, true);
						String url = (String) msg.getData().get("url");
						String title = (String) msg.getData().get("title");
						String src = (String) msg.getData().get("src");
						WebView webview = (WebView) msg.obj;
						String webTitle = webview.getTitle();
						String webUrl = webview.getOriginalUrl();
						if (TextUtils.equals(url,src)){
							TabLongClickImgView menuView = new TabLongClickImgView(outView.mActivity, url, webUrl, title, bitmap);
							int menuWidth = DensityUtil.dip2px(outView.mActivity, TabLongClickImgView.WIDTH_DP);
							int menuHeight = DensityUtil.dip2px(outView.mActivity, TabLongClickImgView.HEIGHT_DP);
							outView.showMenu(menuView, menuWidth, menuHeight);
						}else{
							TabLongClickWebNewsItemView newsItemMenuView = new TabLongClickWebNewsItemView(outView.mActivity,src,url,webUrl,webTitle,title,bitmap);
							outView.mCurMenuNewsItemView = newsItemMenuView;
							int menuWidth = DensityUtil.dip2px(outView.mActivity, TabLongClickImgView.WIDTH_DP);
							int menuHeight = DensityUtil.dip2px(outView.mActivity, TabLongClickImgView.HEIGHT_DP);
							outView.showMenu(newsItemMenuView, menuWidth, menuHeight);
						}
					}

				}
			}
		}


	}

	@JavascriptInterface
	public void getString(String paramFromJS) {
		SimpleLog.d(TAG, paramFromJS);
		if (mCurMenuView != null) {
			paramFromJS = paramFromJS.trim();
			mCurMenuView.setLinkText(paramFromJS);
			if(mCurMenuNewsItemView!=null)
				mCurMenuNewsItemView.setLinkText(paramFromJS);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		HitTestResult result = ((WebView) v).getHitTestResult();
		if (null == result) {
			return false;
		}
		int type = result.getType();
		if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
			String extra = result.getExtra();
			SimpleLog.d(TAG, "extra:" + extra);
//			WebView webview = (WebView) v;
//			final String url = webview.getUrl();
//			final String title = webview.getTitle();
//			File file = new File(mActivity.getFilesDir() + File.separator + "a.mht");
//			String data = new String(FileUtils.readFile(file));
//			((WebView) v).loadDataWithBaseURL(null, data, "application/x-webarchive-xml", "UTF-8", null);
			// 需要先截屏
//			long start = System.currentTimeMillis();
//			final Bitmap bitmap = ViewUtils.takeScreenShot(mActivity, true, true);
//			SimpleLog.d(TAG, "time:" + String.valueOf(System.currentTimeMillis() - start));
//			
//			ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
//				@Override
//				public void run() {
//					TabLongClickBlankView menuView = new TabLongClickBlankView(mActivity, url, title, bitmap, mShare);
//					int menuWidth = DensityUtil.dip2px(mActivity, TabLongClickBlankView.WIDTH_DP);
//					int menuHeight = DensityUtil.dip2px(mActivity, TabLongClickBlankView.HEIGHT_DP);
//					showMenu(menuView, menuWidth, menuHeight);
//					MobclickAgent.onEvent(mActivity, ConfigDefine.UM_MENU_SHOW);
//				}
//			}, 50);
			// 因为对复制文字有干扰，暂时关掉本菜单，待找到更好的办法再恢复
			return false;
		}
		switch (type) {
			case WebView.HitTestResult.PHONE_TYPE:
				// 处理拨号
				break;
			case WebView.HitTestResult.EMAIL_TYPE:
				// 处理Email
				break;
			case WebView.HitTestResult.GEO_TYPE:
				// TODO
				break;
			case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:{

				WebView webview = (WebView) v;
				final HashMap<String, WebView> hrefMap =
						new HashMap<String, WebView>();
				hrefMap.put("webview", webview);

				final Message msg = mHandler.obtainMessage(
						FOCUS_NODE_HREF, hrefMap);
				msg.obj = webview;
				webview.requestFocusNodeHref(msg);

				return true;
			}
			case WebView.HitTestResult.SRC_ANCHOR_TYPE: {
				Bitmap bitmap = ViewUtils.takeScreenShot(mActivity, true, true);
				// 超链接
				WebView webview = (WebView) v;
				String title = webview.getTitle();
				String webUrl = webview.getOriginalUrl();
				String linkUrl = result.getExtra();
				// fix bug #2113 空链接 不做处理
				if (TextUtils.equals("javascript:void(0);", linkUrl)) {
					return false;
				}
				TabLongClickLinkView menuView = new TabLongClickLinkView(mActivity, linkUrl, webUrl, title, bitmap, mShare);
				mCurMenuView = menuView;
				getLinkText(v);
				int menuWidth = DensityUtil.dip2px(mActivity, TabLongClickLinkView.WIDTH_DP);
				int menuHeight = DensityUtil.dip2px(mActivity, TabLongClickLinkView.HEIGHT_DP);
				showMenu(menuView, menuWidth, menuHeight);
				return true;
			}

			case WebView.HitTestResult.IMAGE_TYPE: {
				Bitmap bitmap = ViewUtils.takeScreenShot(mActivity, true, true);
				String url = result.getExtra();
				WebView webview = (WebView) v;
				String title = webview.getTitle();
				String webUrl = webview.getOriginalUrl();
				TabLongClickImgView menuView = new TabLongClickImgView(mActivity, url, webUrl, title, bitmap);
				int menuWidth = DensityUtil.dip2px(mActivity, TabLongClickImgView.WIDTH_DP);
				int menuHeight = DensityUtil.dip2px(mActivity, TabLongClickImgView.HEIGHT_DP);
				showMenu(menuView, menuWidth, menuHeight);
				return true;
			}
			default:
				return false;
		}
		return false;
	}

	private void getLinkText(View v) {
		String js = "function delHtmlTag(str){return str.replace(/<[^>]+>/g,\"\");}  function MyAppGetLinkTITLEAtPoint(x,y) {var tags = \"\";var e = document.elementFromPoint(x,y);while (e) {if (e.href) {tags += delHtmlTag(e.innerHTML);break;}e = e.parentNode;}window.js.getString(tags);}";
		WebView webview = (WebView) v;
		int[] location = new int[2];
		webview.getLocationOnScreen(location);// 获取在整个屏幕内的绝对坐标
		webview.loadUrl("javascript:" + js);
		float density = mActivity.getResources().getDisplayMetrics().density;
		float x = mTouchX / density;
		float y = (mTouchY - location[1]) / density;
		String getTitle = String.format("javascript:MyAppGetLinkTITLEAtPoint(%d, %d);", (int) x, (int) y);
		webview.loadUrl(getTitle);
	}

	/**
	 * 计算菜单弹出的位置，弹出菜单
	 *
	 * @param menuView
	 * @param menuWidth
	 * @param menuHeight
	 */
	private void showMenu(PopupWindow menuView, int menuWidth, int menuHeight) {
		if (mTouchX + menuWidth >= mWidth && mTouchY + menuHeight >= mHeight) {
			menuView.showAtLocation(mAnchorView, Gravity.TOP | Gravity.START,
					(int) mTouchX - menuWidth, (int) mTouchY - menuHeight);
		} else if (mTouchX + menuWidth >= mWidth
				&& mTouchY + menuHeight <= mHeight) {
			menuView.showAtLocation(mAnchorView, Gravity.TOP | Gravity.START,
					(int) mTouchX - menuWidth, (int) mTouchY);
		} else if (mTouchX + menuWidth <= mWidth
				&& mTouchY + menuHeight >= mHeight) {
			menuView.showAtLocation(mAnchorView, Gravity.TOP | Gravity.START,
					(int) mTouchX, (int) mTouchY - menuHeight);
		} else {
			menuView.showAtLocation(mAnchorView, Gravity.TOP | Gravity.START,
					(int) mTouchX, (int) mTouchY);
		}
	}

	@Override
	public void onTouch(float x, float y) {
		mTouchX = x;
		mTouchY = y;
	}
}
