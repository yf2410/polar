package com.polar.browser.webview;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IFullScreenDelegate;
import com.polar.browser.i.IScrollChanged;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.i.ITouchListener;
import com.polar.browser.i.IWebView;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;

import java.util.Map;

public class JuziWebView implements IWebView {

	public static final int NET_2G = 2;
	private static final String TAG = "JuziWebView";
	private static final String TAG2 = "JuziWebView2";
	private static final int CHECK_SCROLL_MILLISECONDS = 10;
	private static final int CHECK_TOP_MILLISECONDS = 200;
	private static final int CHECK_SCROLL_DIRECTION = 30;
	private static final int MESSAGE_SCROLL = 1000;
	private static final int MESSAGE_CHECK_TOP = 1001;
	private static String mLastUrl;
	private  WebView mWebView;
	private int mTextSize = 100;
	private OnLongClickListener mOnLongClickListener;
	private ISlideDelegate mSlideDelegate;
	private IFullScreenDelegate mFullScreenDelegate;
	private int mlastScrollY;
	private float mActionDownY;
	private IScrollChanged mScrollChanged;
	private boolean mIsDestroy = false;
	private ITouchListener mTouchListener;
	/**
	 * 用于用户手指离开webView的时候获取webView滚动的Y距离，然后回调给onScroll方法中
	 */
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if (mWebView == null || mIsDestroy) {
				return;
			}
			// 检测滚动事件的消息
			if (msg.what == MESSAGE_SCROLL) {
				int scrollY = mWebView.getScrollY();
				//此时的距离和记录下的距离不相等，在隔5毫秒给handler发送消息
				if (mlastScrollY != scrollY) {
					mlastScrollY = scrollY;
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_SCROLL), CHECK_SCROLL_MILLISECONDS);
				} else {
					SimpleLog.d(TAG, "onScrollChanged()");
					mScrollChanged.onScrollChanged();
				}
			} // 检测webview是否在头部的消息
			else if (msg.what == MESSAGE_CHECK_TOP) {
				if (mWebView.getScrollY() == 0) {
					mScrollChanged.onScrollShow();
				}
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_CHECK_TOP), CHECK_TOP_MILLISECONDS);
			}
		}

		;
	};

	public JuziWebView() {
	}

	public JuziWebView(Context c, final IScrollChanged scrollChanged,
					   OnLongClickListener onLongClickListener, final ITouchListener touchListener,
					   ISlideDelegate slideDelegate, IFullScreenDelegate fullScreenDelegate) {
		mScrollChanged = scrollChanged;
		mOnLongClickListener = onLongClickListener;
		mTouchListener=touchListener;
		mSlideDelegate = slideDelegate;
		mFullScreenDelegate = fullScreenDelegate;
		mIsDestroy = false;
		mWebView = new WebView(c) {

			private float downX;
			private float downY;

			private float mTouchX;
			private float mTouchY;
			private boolean mIsLongClick;
			private long mClickTime;

			@Override
			public void destroy() {
				super.destroy();
				mIsDestroy = true;
			}

			/**
			 * 重写onTouchEvent， 当用户的手在MyScrollView上面的时候，
			 * 直接将MyScrollView滑动的Y方向距离回调给onScroll方法中，当用户抬起手的时候，
			 * MyScrollView可能还在滑动，所以当用户抬起手我们隔5毫秒给handler发送消息，在handler处理
			 * MyScrollView滑动的距离
			 */
			@Override
			public boolean onTouchEvent(MotionEvent ev) {
//		    	SimpleLog.d(TAG, "ev." + ev.get)
				if (mWebView == null || mIsDestroy) {
					return false;
				}
				switch (ev.getAction()) {
					case MotionEvent.ACTION_UP:
						return handleActionUp(ev);
					case MotionEvent.ACTION_DOWN:
						return handleActionDown(ev);
					case MotionEvent.ACTION_MOVE:
						return handleActionMove(ev);
				}
				return super.onTouchEvent(ev);
			}

			private boolean handleActionUp(MotionEvent ev) {
				SimpleLog.i(TAG, "action up");
				long now = System.currentTimeMillis();
				Runnable r = new Runnable() {
					@Override
					public void run() {
						canGetScrollDirection();
					}
				};
				ThreadManager.postDelayedTaskToUIHandler(r, CHECK_SCROLL_DIRECTION);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_SCROLL),
						CHECK_SCROLL_MILLISECONDS);
				mSlideDelegate.touchUp(ev.getX(), ev.getY());
				downX = 0;
				// 判断出是长按，需要禁用掉clickable
				SimpleLog.d(TAG, "mClickTime:" + String.valueOf(mClickTime));
				SimpleLog.d(TAG, "mIsLongClick:" + String.valueOf(mIsLongClick));
				SimpleLog.d(TAG, "now:" + String.valueOf(now));
				if (mIsLongClick && now - mClickTime >= 1000) {
//					setClickable(false);
					mIsLongClick = false;
					return true;
				}
				mIsLongClick = false;
				return super.onTouchEvent(ev);
			}

			@Override
			protected void onScrollChanged(int l, int t, int oldl, int oldt) {
				super.onScrollChanged(l, t, oldl, oldt);
				if (AppEnv.DEBUG) {
					SimpleLog.d("*******************************", "left is : " + l + "  top is : " + t);
				}
				if (t <= 20) {
					TabViewManager.getInstance().getCurrentTabView().setCurrentWebviewTop(true);
					SimpleLog.d("*******************************", "true");
				}else if (t > 20) {
					SimpleLog.d("*******************************", "false");
					TabViewManager.getInstance().getCurrentTabView().setCurrentWebviewTop(false);
				}
			}

			private boolean handleActionDown(MotionEvent ev) {
				mFullScreenDelegate.check2showUI();
				mTouchX = ev.getX();
				mTouchY = ev.getY();
				mIsLongClick = true;
				mClickTime = System.currentTimeMillis();
				SimpleLog.i(TAG, "action down");
				mActionDownY = mWebView.getScrollY();
				int[] location = new int[2];
				mWebView.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
				mTouchListener.onTouch(ev.getX(), ev.getY() + location[1]);
				downX = ev.getX();
				downY = ev.getY();
				mSlideDelegate.touchDown(downX, downY);
				ConfigManager.getInstance().notifyHideIm();
				return super.onTouchEvent(ev);
			}

			private void handleLeftSliding(MotionEvent ev) {
				mSlideDelegate.leftSlide(Math.abs(ev.getX() - downX));
				MotionEvent cancelEvent = MotionEvent.obtain(ev);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
						(ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				onTouchEvent(cancelEvent);
				cancelEvent.recycle();
			}

			private void handleRightSliding(MotionEvent ev) {
				mSlideDelegate.rightSlide(Math.abs(ev.getX() - downX));
				MotionEvent cancelEvent = MotionEvent.obtain(ev);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
						(ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				onTouchEvent(cancelEvent);
				cancelEvent.recycle();
			}

			private boolean handleActionMove(MotionEvent ev) {
				SimpleLog.i(TAG, "action move");
				float curX = ev.getX();
				float curY = ev.getY();
				if (mIsLongClick
						&& Math.abs(mTouchX - curX) < DensityUtil.dip2px(
						mWebView.getContext(), 5)
						&& Math.abs(mTouchY - curY) < DensityUtil.dip2px(
						mWebView.getContext(), 5)) {
					// 认为没有移动过范围，属于点击或者长按
				} else {
					mIsLongClick = false;
				}
				mTouchX = curX;
				mTouchY = curY;
				float deltaX = curX - downX;
				float deltaY = curY - downY;
				int type = ConfigManager.getInstance().getSlidingScreenMode();
				if (type == ConfigDefine.SLIDING_BACK_FORWARD_fullscreen) {
					if (deltaX > 0 && Math.abs(deltaX) > AppEnv.MIN_SLIDING && Math.abs(deltaX) > Math.abs(deltaY)) {
						handleLeftSliding(ev);
						return true;
					} else if (deltaX < 0 && Math.abs(deltaX) > AppEnv.MIN_SLIDING && Math.abs(deltaX) > Math.abs(deltaY)) {
						if (TabViewManager.getInstance().getCurrentTabView().canGoForward()) {
							handleRightSliding(ev);
							return true;
						}
					}
				} else if (type == ConfigDefine.SLIDING_BACK_FORWARD_border) {
					if (downX < AppEnv.MIN_SLIDE_BORDER && Math.abs(deltaX) > AppEnv.MIN_SLIDING) {
						// 左侧
						handleLeftSliding(ev);
						return true;
					} else if (mWebView.getWidth() - downX < AppEnv.MIN_SLIDE_BORDER && Math.abs(deltaX) > AppEnv.MIN_SLIDING) {
						// 右侧
						if (TabViewManager.getInstance().getCurrentTabView().canGoForward()) {
							handleRightSliding(ev);
							return true;
						}
					}
				}
				return super.onTouchEvent(ev);
			}
		};
		if (AppEnv.DEBUG) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				mWebView.setWebContentsDebuggingEnabled(true);
			}
		}
		mWebView.setOnLongClickListener(mOnLongClickListener);
		mWebView.setAlwaysDrawnWithCacheEnabled(true);
		mWebView.setAnimationCacheEnabled(true);
		mWebView.setScrollbarFadingEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
		}
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_CHECK_TOP), CHECK_TOP_MILLISECONDS);
	}

	public static String appendUrl(String urlprefix, String url) {
		return urlprefix += url;
	}

	/**
	 * 计算滚动方向，如果计算出来，返回true，否则返回false
	 *
	 * @return
	 */
	private boolean canGetScrollDirection() {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		int y = mWebView.getScrollY();
		SimpleLog.i(TAG, "y=" + String.valueOf(y));
		boolean isCalculated = false;
		if (y > mActionDownY) {
			SimpleLog.d(TAG, "onScrollUp()");
			mScrollChanged.onScrollUp();
			isCalculated = true;
		} else if (y < mActionDownY) {
			SimpleLog.d(TAG, "onScrollDown()");
			mScrollChanged.onScrollDown();
			isCalculated = true;
		}
		return isCalculated;
	}

	@Override
	public void addJavascriptInterface(Object object, String name) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.addJavascriptInterface(object, name);
	}

	@Override
	public boolean canGoBack() {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.canGoBack();
	}

	@Override
	public boolean canGoBackOrForward(int steps) {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.canGoBackOrForward(steps);
	}

	@Override
	public boolean canGoForward() {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.canGoForward();
	}

	@Override
	public void clearCache(boolean includeDiskFiles) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.clearCache(includeDiskFiles);
	}

	@Override
	public void clearFormData() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.clearFormData();
	}

	@Override
	public void clearHistory() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.clearHistory();
	}

	@Override
	public void clearMatches() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.clearMatches();
	}

	@Override
	public WebBackForwardList copyBackForwardList() {
		if (mWebView == null || mIsDestroy) {
			return null;
		}
		return mWebView.copyBackForwardList();
	}

	@Override
	public void destroy() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mScrollChanged = null;
		mHandler.removeCallbacksAndMessages(null);
		mWebView.removeAllViews();
		mWebView.destroy();
		mTouchListener=null;
		mFullScreenDelegate=null;
		mSlideDelegate=null;
		mWebView = null;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.dispatchKeyEvent(event);
	}

	@Override
	public void documentHasImages(Message response) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.documentHasImages(response);
	}

	@Override
	public String findAddress(String addr) {
		return WebView.findAddress(addr);
	}

	@Override
	public void findAllAsync(String find) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.findAllAsync(find);
	}

	@Override
	public String getOriginalUrl() {
		if (mWebView == null || mIsDestroy) {
			return "";
		}
		return mWebView.getOriginalUrl();
	}

	@Override
	public WebSettings getSettings() {
		if (mWebView == null || mIsDestroy) {
			return null;
		}
		return mWebView.getSettings();
	}

	@Override
	public String getTitle() {
		if (mWebView == null || mIsDestroy) {
			return "";
		}
		return mWebView.getTitle();
	}

	@Override
	public String getUrl() {
		if (mWebView == null || mIsDestroy) {
			return "";
		}
		return mWebView.getUrl();
	}

	@Override
	public void goBack() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.goBack();
	}

	@Override
	public void goBackOrForward(int steps) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.goBackOrForward(steps);
	}

	@Override
	public void goForward() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.goForward();
	}

	@Override
	public void loadData(String data, String mimeType, String encoding) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.loadData(data, mimeType, encoding);
	}

	@Override
	public void loadDataWithBaseURL(String baseUrl, String data,
									String mimeType, String encoding, String historyUrl) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
	}

	@Override
	public void loadUrl(String url) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		SimpleLog.d(TAG2, "StartLoadUrl=" + url);
		mWebView.loadUrl(url);
		final int textSize = mTextSize;
		SimpleLog.d(TAG, "textSize:" + String.valueOf(textSize));
		mWebView.getSettings().setTextZoom(textSize);
		SimpleLog.d(TAG, "mTextSize:" + String.valueOf(mTextSize));
	}

	@Override
	public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		SimpleLog.d(TAG2, "url_2:" + url);
		mWebView.loadUrl(url, additionalHttpHeaders);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.onKeyUp(keyCode, event);
	}

	@Override
	public void onPause() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		SimpleLog.d(TAG, "onPause()");
		mWebView.onPause();
	}

	@Override
	public void onResume() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		SimpleLog.d(TAG, "onResume()");
		mWebView.onResume();
	}

	@Override
	public void postUrl(String url, byte[] postData) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.postUrl(url, postData);
	}

	@Override
	public void reload() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.reload();
	}

	@Override
	public void removeJavascriptInterface(String name) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.removeJavascriptInterface(name);
	}

	@Override
	public void setDownloadListener(DownloadListener listener) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.setDownloadListener(listener);
	}

	@Override
	public void setWebChromeClient(WebChromeClient client) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.setWebChromeClient(client);
	}

	@Override
	public void setWebViewClient(WebViewClient client) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.setWebViewClient(client);
	}

	@Override
	public void stopLoading() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.stopLoading();
	}

	@Override
	public boolean zoomIn() {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.zoomIn();
	}

	@Override
	public boolean zoomOut() {
		if (mWebView == null || mIsDestroy) {
			return false;
		}
		return mWebView.zoomOut();
	}

	@Override
	public View getView() {
		return mWebView;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void clearView() {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.clearView();
	}

	@Override
	public void setFontSize(int size) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mTextSize = size;
		mWebView.getSettings().setTextZoom(mTextSize);
		SimpleLog.d(TAG, "setFontSize:" + mTextSize);
	}

	@Override
	public boolean isInvalid() {
		return !mIsDestroy;
	}

	@Override
	public void saveWebArchive(String fileName) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.saveWebArchive(fileName);
	}

	@Override
	public void saveWebArchive(String basename, boolean autoname,
							   ValueCallback<String> callback) {
		if (mWebView == null || mIsDestroy) {
			return;
		}
		mWebView.saveWebArchive(basename, autoname, callback);
	}

	@Override
	public int getContentHeight() {
		if (mWebView == null || mIsDestroy) {
			return 0;
		}
		return mWebView.getContentHeight();
	}

	@Override
	public float getScale() {
		if (mWebView == null || mIsDestroy) {
			return 0;
		}
		return mWebView.getScale();
	}
}
