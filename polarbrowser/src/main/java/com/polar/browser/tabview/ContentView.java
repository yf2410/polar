package com.polar.browser.tabview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.data.ConfigData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.ICaptureScreenCallback;
import com.polar.browser.i.IDownloadDelegate;
import com.polar.browser.i.IFullScreenDelegate;
import com.polar.browser.i.IProgressStart;
import com.polar.browser.i.IScrollChanged;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.i.ITouchListener;
import com.polar.browser.i.IWebChromeClientDelegate;
import com.polar.browser.i.IWebView;
import com.polar.browser.i.IWebViewClientDelegate;
import com.polar.browser.impl.WebViewClientImpl;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.JSInterfaceManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.setting.JavascriptInterfaceFeedBack;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.NetworkUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.ViewUtils;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.webview.JuziDownloadListener;
import com.polar.browser.webview.JuziWebChromeClient;
import com.polar.browser.webview.JuziWebChromeClient.IReceivedTitle;
import com.polar.browser.webview.JuziWebView;
import com.polar.browser.webview.JuziWebViewClient;

import java.lang.reflect.Method;
import java.nio.channels.GatheringByteChannel;
import java.util.Map;

public class ContentView {

	private static final String TAG = "ContentView";

	private IWebView mWebView;

	private IWebViewClientDelegate mWebViewClientDelegate;

	private IWebChromeClientDelegate mWebChromeClientDelegate;

	private String mTitle;
	private IReceivedTitleCallback mReceivedTitleCallback;
	private IDownloadDelegate mDownloadDelegate;
	private OnLongClickListener mOnLongClickListener;
	private ITouchListener mTouchListener;
	private ISlideDelegate mSlideDelegate;
	private IFullScreenDelegate mFullScreenDelegate;
	private Context mContext;
	private ViewGroup mView;
	private int mSource;
	private TabView mTab;
	private View mErrorView;
	private IReceivedTitle mReceivedTitle = new IReceivedTitle() {

		@Override
		public void onReceivedTitle(String title) {
			mTitle = title;
			SimpleLog.d(TAG, "onReceivedTitle  title is : " + title);
			mReceivedTitleCallback.onReceivedTitle(title, mTab.getId());
		}
	};
	private int mProgress = 0;

	private int mRealProgress = 0;

	private IProgressStart mProgressStart;
	private IScrollChanged mScrollChanged = new IScrollChanged() {

		@Override
		public void onScrollChanged() {
		}

		@Override
		public void onScrollUp() {
			mTab.notifyScrollUp();
		}

		@Override
		public void onScrollDown() {
			mTab.notifyScrollDown();
		}

		@Override
		public void onScrollShow() {
			mTab.notifyScrollShow();
		}
	};
	private TextView mErrorMsg;
	private TextView mErrorMsgDetail;
	private ImageView mIvErrorImg;

	public ContentView(TabView tab) {
		mTab = tab;
	}

	public TabView getTab() {
		return mTab;
	}

	public String getUa() {
		String ua = mWebView.getSettings().getUserAgentString();
		return ua;
	}

	public void setUa(String ua) {
		mWebView.getSettings().setUserAgentString(ua);
	}

	public void saveWebArchive(String fileName) {
		mWebView.saveWebArchive(fileName);
	}

	public void saveWebArchive(String basename, boolean autoname, ValueCallback<String> callback) {
		mWebView.saveWebArchive(basename, autoname, callback);
	}

	public void init(IWebViewClientDelegate webViewClientDelegate,
					 IDownloadDelegate downloadDelegate, Context c, ConfigData config,
					 OnLongClickListener onLongClickListener,
					 ITouchListener touchListener, ISlideDelegate slideDelegate,
					 IReceivedTitleCallback callback, IFullScreenDelegate fullScreenDelegate, IProgressStart progressStart,
					 IWebChromeClientDelegate webChromeClientDelegate) {
		mContext = c;
		mWebViewClientDelegate = webViewClientDelegate;
		mWebChromeClientDelegate = webChromeClientDelegate;
		mDownloadDelegate = downloadDelegate;
		mOnLongClickListener = onLongClickListener;
		mTouchListener = touchListener;
		mSlideDelegate = slideDelegate;
		mReceivedTitleCallback = callback;
		mFullScreenDelegate = fullScreenDelegate;
		mProgressStart = progressStart;
		initWebView(mContext, config);
		initErrorView(mContext);
		initView();
	}

	private void initErrorView(Context mContext) {
		if (mErrorView == null) {
			mErrorView = View.inflate(mContext, R.layout.view_custom_webpage_error, null);
			mErrorMsg = (TextView) mErrorView.findViewById(R.id.tv_webpage_error_msg);
			mErrorMsgDetail = (TextView) mErrorView.findViewById(R.id.tv_webpage_error_msg_detail);
			mIvErrorImg = (ImageView) mErrorView.findViewById(R.id.iv_webpage_error_img);
			mErrorView.findViewById(R.id.tv_webpage_error_retry).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mWebView.reload();
					setErrorPageVisibility(View.GONE);
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE_ERROR, GoogleConfigDefine.WEBPAGE_ERROR_REFRESH);
                }
			});
			mErrorView.findViewById(R.id.tv_webpage_error_back).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TabViewManager.getInstance().goBack();
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE_ERROR, GoogleConfigDefine.WEBPAGE_ERROR_BACK);
				}
			});
			setErrorPageVisibility(View.GONE);
			mErrorView.setOnClickListener(null);
		}
	}

	public void setErrorPageVisibility(int visibility) {
		mErrorView.setVisibility(visibility);
	}

	public void setErrorMsg(String errorCode, String description) {
		mErrorMsgDetail.setText(description);
		if (!NetworkUtils.isAllNetWorkConnected(mContext)) {
			mErrorMsg.setText(JuziApp.getInstance().getString(R.string.no_internet_connection));
			mIvErrorImg.setBackgroundDrawable(JuziApp.getInstance().getResources().getDrawable(R.drawable.webpage_error_disconnected));
            Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE_ERROR, GoogleConfigDefine.WEBPAGE_ERROR_DISCONNECTED);
		} else {
			mErrorMsg.setText(JuziApp.getInstance().getString(R.string.webpage_not_available));
			mIvErrorImg.setBackgroundDrawable(JuziApp.getInstance().getResources().getDrawable(R.drawable.webpage_error_not_available));
            Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE_ERROR, GoogleConfigDefine.WEBPAGE_ERROR_OTHERS);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(Context c, ConfigData config) {
		mWebView = new JuziWebView(c, mScrollChanged, mOnLongClickListener,
				mTouchListener, mSlideDelegate, mFullScreenDelegate);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mWebView.getView().setLayoutParams(layoutParams);
		mWebView.setDownloadListener(new JuziDownloadListener(mDownloadDelegate));
		mWebView.setWebViewClient(new JuziWebViewClient(mWebViewClientDelegate, this));
		mWebView.setWebChromeClient(new JuziWebChromeClient(mWebViewClientDelegate, mReceivedTitle, c instanceof BrowserActivity ? (BrowserActivity) c : null, mProgressStart, mWebChromeClientDelegate));
		// 必须在加载页面前先注入对象，否则执行脚本时，会找不到java对象
		mWebView.addJavascriptInterface(new JSInterfaceManager(mContext), "JSInterfaceManager");
		mWebView.addJavascriptInterface(mWebViewClientDelegate, "video");
		mWebView.addJavascriptInterface(mOnLongClickListener, "js");
		mWebView.addJavascriptInterface(new JavascriptInterfaceFeedBack(c), "feedback");

		WebSettings settings = mWebView.getSettings();
		setPageCache(settings);
		settings.setJavaScriptEnabled(true);
		if (Build.VERSION.SDK_INT < 18) {
			settings.setRenderPriority(RenderPriority.HIGH);
		}
		if (NetWorkUtils.isWifiConnected(mContext)) {
			settings.setLoadsImagesAutomatically(true);
		} else {
			settings.setLoadsImagesAutomatically(config.isEnableImg());
		}
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setGeolocationEnabled(true);
		settings.setUseWideViewPort(true);
		// 无痕模式
		try {
			boolean privacyMode = ConfigManager.getInstance().isPrivacyMode();
			settings.setSaveFormData(!privacyMode);
			settings.setSavePassword(!privacyMode);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		// 自定义ua
		// 1. 如果没有default ua，设置default ua
		String ua = settings.getUserAgentString();
		boolean isOriginalUa = true;
		if (!ua.contains("Mozilla")) {
			ua = "Mozilla/5.0 " + ua;
			isOriginalUa = false;
		}
		String defaultUa = ConfigManager.getInstance().getDefaultUa();
		if (TextUtils.isEmpty(defaultUa) || !isOriginalUa) {
			ConfigManager.getInstance().setDefaultUa(ua);
		}
		// 2. 根据ua type，选择合适的ua
		int uaType = ConfigManager.getInstance().getUaType();
		if (uaType == ConfigDefine.UA_TYPE_DEFAULT && !isOriginalUa) {
			settings.setUserAgentString(ua);
		} else if (uaType == ConfigDefine.UA_TYPE_PC) {
			settings.setUserAgentString(CommonData.UA_PC);
		} else if (uaType == ConfigDefine.UA_TYPE_IOS) {
			settings.setUserAgentString(CommonData.UA_IPHONE6);
		} else if (uaType == ConfigDefine.UA_TYPE_CUSTOM) {
			String configUa = ConfigManager.getInstance().getCustomUa();
			if (!TextUtils.isEmpty(configUa)) {
				settings.setUserAgentString(configUa);
			}
		}
		// 支持缩放
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		// 缓存相关
		settings.setDomStorageEnabled(true);
		String appCacheDir = mContext.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
		settings.setAppCachePath(appCacheDir);
		settings.setAllowFileAccess(true);
		settings.setAppCacheEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                       settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
               }

		// mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
		settings.setSupportMultipleWindows(true);
	}

	public int getProgress() {
		return mProgress;
	}

	public void setProgress(int progress) {
		mProgress = progress;
	}

	public int getRealProgress() {
		return mRealProgress;
	}

	public void setRealProgress(int realprogress) {
		mRealProgress = realprogress;
	}

	private void initView() {
		RelativeLayout rl = new RelativeLayout(mContext);
		android.widget.RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		rl.setLayoutParams(params);
		rl.addView(mWebView.getView());
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mErrorView.setLayoutParams(lp);
		rl.addView(mErrorView);
		mView = rl;
	}

	public View getView() {
		return mView;
	}

	public void loadUrl(String url, int src) {
		//无图模式时，新建标签时 应在加载链接时设置 不加载图片（临时解决）
		if (mWebView != null && mWebView.getSettings() != null) {
			mWebView.getSettings().setLoadsImagesAutomatically(ConfigManager.getInstance().isEnableImg());
		}
		if (mWebView != null) {
			SimpleLog.d(TAG, "loadUrl()");
			mSource = src;
			mWebView.loadUrl(url);
			mWebViewClientDelegate.setMainUrl(url);
		}
	}

	public void loadUrl(String url, int src, Map<String, String> headers) {
		if (mWebView != null) {
			SimpleLog.d(TAG, "loadUrl() with headers");
			mSource = src;
			mWebView.loadUrl(url, headers);
		}
	}

	public void resetNavigateSource() {
		mSource = Constants.NAVIGATESOURCE_NORMAL;
	}

	public void clearHistory() {
		if (mWebView != null) {
			mWebView.clearHistory();
		}
	}

	public void clearCache() {
		if (mWebView != null) {
			mWebView.clearCache(true);
		}
	}

	public int getSource() {
		return mSource;
	}

	public void destroy() {
		// 必须先remove掉，才能destroy！
		if (mWebView != null) {
			mView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
	}

	public void resume() {
		SimpleLog.d(TAG, "resume()");
		if (mWebView != null) {
			mWebView.onResume();
		}
	}

	public void pause() {
		SimpleLog.d(TAG, "pause()");
		if (mWebView != null) {
			mWebView.onPause();
		}
	}

	public void setLoadsImages(boolean isEnabled) {
		if (mWebView != null) {
			mWebView.getSettings().setLoadsImagesAutomatically(isEnabled);
		}
	}

	public void enableNightMode(boolean isEnabled, String js) {
		if (mWebView != null) {
			mWebView.loadUrl(CommonData.EXEC_JAVASCRIPT + js);
		}
	}

	public String getTitle() {
		if (mTitle != null) {
			return mTitle;
		}
		if (mWebView != null) {
			String title = mWebView.getTitle();
			if (TextUtils.isEmpty(title)) {
				title = mTitle;
			}
			if (TextUtils.isEmpty(title)) {
				title = mWebView.getUrl();
			}
			return title;
		} else {
			return "";
		}
	}

	public void reload() {
		if (mWebView != null) {
			if(mWebViewClientDelegate instanceof WebViewClientImpl)
				((WebViewClientImpl)mWebViewClientDelegate).setEnnableBlockTips(false);
			mWebView.reload();
		}
	}

	public void stopLoading() {
		if (mWebView != null) {
			mWebView.stopLoading();
		}
	}

	public String getUrl() {
		if (mWebView != null) {
			return mWebView.getUrl();
		} else {
			return "";
		}
	}

	public String getOrigUrl() {
		if (mWebView != null) {
			return mWebView.getOriginalUrl();
		} else {
			return "";
		}
	}

	public void goBack() {
		if (mWebView != null) {
			mWebView.goBack();
		}
	}

	public void goForward() {
		if (mWebView != null) {
			mWebView.goForward();
		}
	}

	public boolean canGoBack() {
		if (mWebView != null) {
			return mWebView.canGoBack();
		} else {
			return false;
		}
	}

	public boolean canGoForward() {
		if (mWebView != null) {
			return mWebView.canGoForward();
		} else {
			return false;
		}
	}

	public IWebView getWebView() {
		return mWebView;
	}

	public void setFontSize(int size) {
		if (mWebView != null) {
			mWebView.setFontSize(size);
		}
	}

	public void getScreenShotAsync(ICaptureScreenCallback callback) {
		long start = System.currentTimeMillis();
		ViewUtils.getScreenShotAsync(mWebView.getView(), callback, 0.3f, 0.3f,
				true, 0.8f);
		long end = System.currentTimeMillis();
		SimpleLog.i(TAG,
				"getScreenShotAsync! time:" + String.valueOf(end - start));
	}

	public Bitmap getScreenShotSync() {
		long start = System.currentTimeMillis();
		Bitmap bitmap = ViewUtils
				.getScreenShotSync(getView(), 0.3f, 0.3f, 0.8f);
		long end = System.currentTimeMillis();
		SimpleLog.i(TAG,
				"getScreenShotSync! time:" + String.valueOf(end - start));
		return bitmap;
	}

	private void setPageCache(WebSettings settings) {
		try {
			Object[] args = {Integer.valueOf(5)};
			Method m = WebSettings.class.getMethod("setPageCacheCapacity",
					new Class[]{int.class});
			m.invoke(settings, args); // wSettings是WebSettings对象
		} catch (Exception e) {
		}
	}

	public boolean isInvalid() {
		return mWebView.isInvalid();
	}

	public boolean isSameWebView(WebView webview) {
		WebView web = (WebView) mWebView.getView();
		if (web != null && web.equals(webview)) {
			return true;
		}
		return false;
	}

	public void setSavePassword(boolean isSave) {
		if (mWebView != null) {
			mWebView.getSettings().setSaveFormData(isSave);
			mWebView.getSettings().setSavePassword(isSave);
		}
	}

	public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
		if (mWebView != null) {
			mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		}
	}

	public interface IReceivedTitleCallback {
		public void onReceivedTitle(String title, int tabId);
	}
}
