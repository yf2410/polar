package com.polar.browser.tabview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.ValueCallback;

import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigData;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.ICaptureScreenCallback;
import com.polar.browser.i.IDownloadDelegate;
import com.polar.browser.i.IFullScreenDelegate;
import com.polar.browser.i.IProgressStart;
import com.polar.browser.i.ISearchFrame;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.i.ITouchListener;
import com.polar.browser.i.IWebChromeClientDelegate;
import com.polar.browser.i.IWebViewClientDelegate;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewCallbackManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.tabview.ContentView.IReceivedTitleCallback;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.common.Constants;

import java.io.File;
import java.util.Map;
import java.util.Random;

/**
 * 用于表示每个Tab，内部有两个对象：homeView以及contentView
 * 其中homeView只是一个对于主页的引用，并不真正创建对象。HomeView在各个tabView中是复用的。
 *
 * @author dpk
 */
public class TabView {

	private static final String TAG = "TabView";

	// 实际是单实例的，此处仅为一个引用，不产生新实例
	private HomeFrame mHomeRef;

	private ContentView mContent;

	private boolean mIsShowHome;

	private IDownloadDelegate mDownloadDelegate;

	private IWebViewClientDelegate mWebViewClientDelegate;

	private IWebChromeClientDelegate mWebChromeClientDelegate;

	private OnLongClickListener mOnLongClickListener;

	private ITouchListener mTouchListener;

	private ISlideDelegate mSlideDelegate;

	private IFullScreenDelegate mFullScreenDelegate;

	private IReceivedTitleCallback mReceivedTitleCallback;

	private TabViewManager mTabMgrRef;

	private Activity mActivity;

	private IProgressStart mProgressStart;

	private int mId;

	private Bitmap mBitmap;

	private String mSubImagePath = new String();

	private ConfigData mConfig;

	private int mOldProgress;

	/**
	 * 判断当前tabview的webview是否在顶部
	 */
	private boolean mIsCurrentWebviewTop = true;

	/** 恢复页面，初始化的title和url **/
	private String mInitialUrl;
	private String mInitialTitle;

	/**是否是从MultiWindowView中新建的标签
	 * true 是
	 * false 否
	 * */
	private boolean isFromBottomMenu = true;

	/**
	 * 异步截屏后的回调接口
	 */
	private ICaptureScreenCallback mCaptureScreenCallback = new ICaptureScreenCallback() {

		@Override
		public void notifyCapture(Bitmap bitmap) {
			mBitmap = bitmap;
			notifyChangeScreenShot();
		}
	};

	public TabView(TabViewManager manager, TabViewCallbackManager callbackMgr,
				   Activity activity, ConfigData config, boolean isHome, int id,
				   ISearchFrame searchDelegate, ISlideDelegate slideDelegate,
				   IReceivedTitleCallback callback,
				   IFullScreenDelegate fullScreenDelegate, IProgressStart progressStart) {
		mTabMgrRef = manager;
		mDownloadDelegate = callbackMgr.getDownloadDelegate();
		mWebViewClientDelegate = callbackMgr.getWebViewClientDelegate();
		mWebChromeClientDelegate = callbackMgr.getWebChromeClientDelegate();
		mActivity = activity;
		mId = id;
		mConfig = config;
		mOnLongClickListener = callbackMgr.getOnLongClickListener();
		mTouchListener = callbackMgr.getTouchListener();
		mReceivedTitleCallback = callback;
		mSlideDelegate = slideDelegate;
		mFullScreenDelegate = fullScreenDelegate;
		mProgressStart = progressStart;
		init(isHome);
	}

	public int getId() {
		return mId;
	}

	public void initRestoreData(String title, String url) {
		this.mInitialTitle = title;
		this.mInitialUrl = url;
	}

	public void resetRestoreData() {
		this.mInitialTitle = null;
		this.mInitialUrl = null;
	}

	/**
	 * 是否恢复的标签页
	 * @return
	 */
	public boolean isFirstRestoredTab() {
		if (!TextUtils.isEmpty(mInitialUrl)) {
			return true;
		}
		return false;
	}

	/**
	 * 首次加载恢复的标签页内容
	 */
	public void loadInitialUrl() {
		if (!TextUtils.isEmpty(mInitialUrl)) {
			mContent.loadUrl(mInitialUrl, Constants.NAVIGATESOURCE_NORMAL);
			mInitialUrl = null;
		} else {
			SimpleLog.e("", "mInitialUrl is empty");
		}
	}

	private void clearSubImage() {
		String subImagePath = makeSubImagePath();
		FileUtils.deleteFileOrDirectory(new File(subImagePath));
	}

	public void clearAllSubImages() {
		String subImagePath = makeSubImageDir();
		FileUtils.deleteFileOrDirectory(new File(subImagePath));
	}

	public void destroy() {
		mContent.destroy();
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
		}
		boolean isForceExit = ConfigManager.getInstance().isForceExit();
		boolean isSaveTab = ConfigManager.getInstance().isSaveTab();
		if (!isForceExit && !isSaveTab) {
			clearSubImage();
		}
		mActivity = null;
		mDownloadDelegate = null;
		mWebViewClientDelegate = null;
		mWebChromeClientDelegate = null;
		mTabMgrRef = null;
		mHomeRef = null;
	}

	public void setPrivacyMode() {
	}

	private void initContentView(ConfigData config) {
		mContent = new ContentView(this);
		mContent.init(mWebViewClientDelegate, mDownloadDelegate, mActivity,
				config, mOnLongClickListener, mTouchListener, mSlideDelegate,
				mReceivedTitleCallback, mFullScreenDelegate, mProgressStart, mWebChromeClientDelegate);
	}

	private void init(boolean isHome) {
		mHomeRef = mTabMgrRef.getHomeView();
		initContentView(mConfig);
		if (isHome) {
			mTabMgrRef.setHomeVisible(View.VISIBLE);
			mTabMgrRef.setContentVisible(View.GONE);
		} else {
			mTabMgrRef.setHomeVisible(View.GONE);
			mTabMgrRef.setContentVisible(View.VISIBLE);
		}
		mIsShowHome = isHome;
	}

	public void loadUrl(String url, int src) {
		mContent.loadUrl(url, src);
	}

	public void loadUrl(String url, int src, Map<String, String> headers) {
		mContent.loadUrl(url, src, headers);
	}

	public String getTitle() {
		if (mIsShowHome) {
			return mActivity.getString(R.string.home_page);
		}
		if (isFirstRestoredTab()) {
			return mInitialTitle;
		}
		return mContent.getTitle();
	}

	public String getUrl() {
		if (mIsShowHome) {
			return "";
		}
		if (isFirstRestoredTab()) {
			return mInitialUrl;
		}
		return mContent.getUrl();
	}

	public String getSubImagePath() {
		return mSubImagePath;
	}

	public void setSubImagePath(String imagePath) {
		mSubImagePath = imagePath;
		ThreadManager.postTaskToIOHandler(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mBitmap = FileUtils.getBitmapFromFile(makeSubImagePath());
			}
		});
	}

	public void setLoadsImages(boolean isLoad) {
		mContent.setLoadsImages(isLoad);
	}

	public void enableNightMode(boolean isEnabled, String js) {
		mContent.enableNightMode(isEnabled, js);
	}

	public void setVisibility(boolean isVisible) {
		if (isVisible) {
			if (mIsShowHome) {
				mTabMgrRef.setHomeVisible(View.VISIBLE);
				mTabMgrRef.setContentVisible(View.GONE);
			} else {
				mTabMgrRef.setContentVisible(View.VISIBLE);
				mTabMgrRef.setHomeVisible(View.GONE);
				if (mWebViewClientDelegate != null) {
					mWebViewClientDelegate.switchCurrentWebView(mContent.getWebView().getView());
				}
			}
		} else {
			mTabMgrRef.setHomeVisible(View.GONE);
			mTabMgrRef.setContentVisible(View.GONE);
		}
	}

	public void pause() {
		SimpleLog.d(TAG, "onPause()");
		mContent.pause();
	}

	public void resume() {
		SimpleLog.d(TAG, "onResume()");
		mContent.resume();
	}

	public void goBack() {
		if (mIsShowHome) {
			showContent();
			String url = mContent.getUrl();
			if (!TextUtils.isEmpty(url) && url.equals(TabViewManager.HOME_URL)) {
				mContent.goBack();
			}
		} else {
			mContent.goBack();
		}
	}

	public void goForward() {
		if (mIsShowHome) {
			showContent();
			String url = mContent.getUrl();
			if (!TextUtils.isEmpty(url) && url.equals(TabViewManager.HOME_URL)) {
				mContent.goForward();
			}
			mContent.resume();
		} else {
			mContent.goForward();
		}
	}

	public boolean canGoBack() {
		return mContent.canGoBack();
	}

	public boolean canGoForward() {
		if (mIsShowHome) {
			String url = mContent.getUrl();
			if (url != null) {
				if (!url.equals(TabViewManager.HOME_URL)) {
					return true;
				} else {
					return mContent.canGoForward();
				}
			} else {
				return false;
			}
		}
		return mContent.canGoForward();
	}

	public void reload() {
		mContent.reload();
	}

	public void stopLoading() {
		mContent.stopLoading();
	}

	public void goHome() {
		SimpleLog.d(TAG, "showContent");
		mIsShowHome = true;
		mContent.pause();
	}

	public void showContent() {
		SimpleLog.d(TAG, "showContent");
		mIsShowHome = false;
		mTabMgrRef.setContentVisible(View.VISIBLE);
		mTabMgrRef.setHomeVisible(View.GONE);
	}

	public boolean isShowHome() {
		return mIsShowHome;
	}

	public void switchHomePager() {
		// mHomeRef.switchViewPager();
	}

	/**
	 * 主页通过js调用打开url
	 *
	 * @param url
	 */
	public void showContent(String url) {
		mContent.loadUrl(url, Constants.NAVIGATESOURCE_NORMAL);
		showContent();
	}

	public void clearTabNavigate() {
		mContent.clearHistory();
	}

	public void clearCache() {
		mContent.clearCache();
	}

	public void reset(ConfigData config) {
		mContent.destroy();
		mConfig = config;
		initContentView(mConfig);
		mTabMgrRef.resetCurrentTab(mId);
	}

	public ContentView getContentView() {
		return mContent;
	}

	public void setFontSize(int size) {
		mContent.setFontSize(size);
	}

	public void notifyProgressChanged(int progress, String url) {
//		mTabMgrRef.notifyProgressChanged(progress, mId, url);
		notifyScreenShotChanged(progress);
	}

	private void notifyScreenShotChanged(int progress) {
		boolean isNeedCapture = false;
		if (progress < mOldProgress) {
			mOldProgress = 0;
		}
		// 截图策略，需要优化，达到性能和截图效果的平衡
		if (progress != 100 && progress >= 50) {
			if (progress > mOldProgress + 20) {
				isNeedCapture = true;
				mOldProgress = progress;
			}
		} else if (progress == 100) {
			isNeedCapture = true;
		}
		if (isNeedCapture) {
			Runnable captureScreenTask = new Runnable() {
				@Override
				public void run() {
					SimpleLog.i(TAG, "captureScreenTask execute!");
					long start = System.currentTimeMillis();
					getScreenShotInner();
					long end = System.currentTimeMillis();
					SimpleLog.i(TAG, "captureScreenTask finished!");
					SimpleLog.i(TAG, "time:" + String.valueOf(end - start));
				}
			};
			ThreadManager.postTaskToUIHandler(captureScreenTask);
		}
	}

	/**
	 * 通知Webview加载页面置顶
	 */
	public void webViewJumpToTop() {
		mContent.getWebView().getView().scrollTo(0,0);
	}

	/**
	 * 通知滚动条上滑
	 */
	public void notifyScrollUp() {
		if (mTabMgrRef == null) {
			return;
		}
		if (mTabMgrRef.getScrollInterface() != null
				&& mTabMgrRef.getCurrentTabId() == mId) {
			// 20160919 添加地址栏消失判断条件,如果webview内容高度小于一屏幕时,就不消失(1.2为一个系数。google页面大于1屏但仍不允许滚动)
			int height = mContent.getWebView().getContentHeight();
			float scale = mContent.getWebView().getScale();
			if (height * scale > AppEnv.SCREEN_HEIGHT * 1.2) {
				mTabMgrRef.getScrollInterface().onScrollUp();
			}
		}
	}

	/**
	 * 通知滚动条下滑
	 */
	public void notifyScrollDown() {
		if (mTabMgrRef == null) {
			return;
		}
		if (mTabMgrRef.getScrollInterface() != null
				&& mTabMgrRef.getCurrentTabId() == mId) {
			mTabMgrRef.getScrollInterface().onScrollDown();
		}
	}

	public void notifyScrollShow() {
		if (mTabMgrRef == null) {
			return;
		}
		if (mTabMgrRef.getScrollInterface() != null
				&& mTabMgrRef.getCurrentTabId() == mId) {
			mTabMgrRef.getScrollInterface().onScrollShow();
		}
	}

	public int getProgress() {
		return mContent.getProgress();
	}

	public void setProgress(int progress) {
		mContent.setProgress(progress);
	}

	public int getRealProgress() {
		return mContent.getRealProgress();
	}

	public void setRealProgress(int realprogress) {
		mContent.setRealProgress(realprogress);
	}

	private Bitmap getScreenShotSync() {
		if (mIsShowHome) {
			mBitmap = mHomeRef.getScreenShotSync();
		} else {
			mBitmap = mContent.getScreenShotSync();
		}
		notifyChangeScreenShot();
		return mBitmap;
	}

	public Bitmap getScreenShot() {
		if (mIsShowHome) {
			if (mBitmap == null) {
				mBitmap = mHomeRef.getScreenShotSync();
				notifyChangeScreenShot();
			}
		} else if (mBitmap == null) {
			getScreenShotInner();
		}
		return mBitmap;
	}

	public void forceCaptureScreenInNightMode() {
		SimpleLog.d(TAG, "forceCaptureScreenInNightMode");
		Runnable r = new Runnable() {
			public void run() {
				getScreenShotSync();
//				TabView.this.pause();
			}
		};
		ThreadManager.postDelayedTaskToUIHandler(r, 500);
	}

	/**
	 * 外部调用，让当前tabview截屏
	 */
	public Bitmap captureScreen() {
		return getScreenShotSync();
	}

	private String makeSubImageDir() {
		return String.format("%s/%s", mActivity.getFilesDir().toString(),
				CommonData.SUB_IMAGE_PATH) + File.separator;
	}

	private String makeSubImagePath() {
		return makeSubImageDir() + mSubImagePath;
	}

	private void notifyChangeScreenShot() {
		if (mBitmap != null) {
			if (mSubImagePath.isEmpty()) {
				String md5 = SecurityUtil.getMD5(mContent.getUrl());
				mSubImagePath = md5 + String.valueOf(new Random().nextInt(99999999)) + ".png";
			}
			final String subImageDir = makeSubImageDir();
			Runnable r = new Runnable() {
				@Override
				public void run() {
					FileUtils.saveBitmapToFile(mBitmap, subImageDir, mSubImagePath);
				}
			};
			ThreadManager.postTaskToIOHandler(r);
		}
	}

	/**
	 * 获取截屏，异步操作
	 */
	private void getScreenShotInner() {
		if (mIsShowHome) {
			mBitmap = mHomeRef.getScreenShotBitmap();
			notifyChangeScreenShot();
		} else {
			mContent.getScreenShotAsync(mCaptureScreenCallback);
		}
	}

	/**
	 * 获取横屏截图
	 */
	public void getScreenShotLandScape() {
		mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
				(int) (mBitmap.getWidth() * 0.3));
	}

	/**
	 * 获取contentView的url
	 */
	public String getContentUrl() {
		String url = null;
		if (mContent != null) {
			url = mContent.getUrl();
		}
		return url;
	}

	/**
	 * 获取contentView的originalUrl
	 */
	public String getContentOrigUrl() {
		String url = null;
		if (mContent != null) {
			url = mContent.getOrigUrl();
		}
		return url;
	}

	/**
	 * 获取contentView的title
	 */
	public String getContentTitle() {
		String title = null;
		if (mContent != null) {
			title = mContent.getTitle();
		}
		return title;
	}

	public String getUa() {
		String ua = "";
		if (mContent != null) {
			ua = mContent.getUa();
		}
		return ua;
	}

	public void setUa(String ua) {
		if (mContent != null) {
			mContent.setUa(ua);
		}
	}

	public void saveWebArchive(String fileName) {
		if (mContent != null) {
			mContent.saveWebArchive(fileName);
		}
	}

	public void saveWebArchive(String fileName, ValueCallback<String> callback) {
		if (mContent != null) {
			mContent.saveWebArchive(fileName, false, callback);
		}
	}

	public void setSavePassword(boolean isSave) {
		if (mContent != null) {
			mContent.setSavePassword(isSave);
		}
	}

	public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
		if (mContent != null) {
			mContent.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		}
	}

	public boolean isFromBottomMenu() {
		return isFromBottomMenu;
	}

	public void setFromBottomMenu(boolean isFromBottomMenu) {
		this.isFromBottomMenu = isFromBottomMenu;
	}

	public void setCurrentWebviewTop(boolean isTop) {
		mIsCurrentWebviewTop = isTop;
	}

	public boolean isCurrentWebviewTop() {
		return mIsCurrentWebviewTop;
	}
}
