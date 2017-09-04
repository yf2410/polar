package com.polar.browser.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.polar.browser.JuziApp;
import com.polar.browser.bookmark.BookmarkManager;
import com.polar.browser.bookmark.HomeListener;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.loginassistant.login.AccountLoginManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.sync.SettingSyncManager;
import com.polar.browser.sync.UserHomeSiteManager;
import com.polar.browser.utils.SimpleLog;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

public abstract class BaseActivity extends RxFragmentActivity {

	protected String mClassName;

	private String TAG = "lemon";

	private long mOrientationLandscapeStartTime = 0;
	private static final long LANDSCAPE_EXPIRING_TIME = 5000; // 5s.
	private HomeListener mHomeWatcher;

	public BaseActivity() {
		mClassName = getClass().getSimpleName();
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.%s()", mClassName, mClassName));
		}
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onAttachedToWindow()", mClassName));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onPostCreate(savedInstanceState=%s)", mClassName, savedInstanceState));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onCreate(savedInstanceState=%s)", mClassName, savedInstanceState));
		}
		//获取手机配置信息
		Configuration mConfiguration = this.getResources().getConfiguration();
		//判断横竖屏
		if (mConfiguration.orientation == mConfiguration.ORIENTATION_LANDSCAPE){
			mOrientationLandscapeStartTime = System.currentTimeMillis();
		} else {
			mOrientationLandscapeStartTime = 0;
		}
	}

	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onCreateView(name=%s)", mClassName, name));
		}
		return super.onCreateView(name, context, attrs);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onNewIntent()", mClassName));
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onRestart()", mClassName));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onStart()", mClassName));
		}
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onPostResume()", mClassName));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onResume(NativeHeapAllocatedSize:%d, NativeHeapFreeSize:%d)", mClassName,
					android.os.Debug.getNativeHeapAllocatedSize(), android.os.Debug.getNativeHeapFreeSize()));
		}
		registerHomeListener();
		Tracker tracker = JuziApp.getInstance().getDefaultTracker();
		tracker.setScreenName(mClassName);
		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onConfigurationChanged(newConfig=%s)", mClassName, newConfig));
		}
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mOrientationLandscapeStartTime = System.currentTimeMillis();
			SimpleLog.e(TAG, "当前屏幕为横屏");
		} else {
			if (mOrientationLandscapeStartTime > 0 && Math.abs(System.currentTimeMillis() - mOrientationLandscapeStartTime) >= LANDSCAPE_EXPIRING_TIME) {
				Statistics.sendOnceStatistics(GoogleConfigDefine.ORIENTATION_LANDSCAPE_OVER_FIVE, mClassName);
			}
			mOrientationLandscapeStartTime = 0;
			SimpleLog.e(TAG, "当前屏幕为竖屏");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onActivityResult(requestCode=%d, resultCode=%d, data=%s)", mClassName,
					requestCode, resultCode, data));
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onBackPressed()", mClassName));
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		try {
			super.onRestoreInstanceState(savedInstanceState);
		} catch (IllegalArgumentException e) {
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		savedInstanceState = null;
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onRestoreInstanceState()", mClassName));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onSaveInstanceState()", mClassName));
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onPause()", mClassName));
		}
		mHomeWatcher.stopWatch();
		super.onPause();
	}


	@Override
	protected void onStop() {
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onStop()", mClassName));
		}
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.onDestroy()", mClassName));
		}
		if (mOrientationLandscapeStartTime > 0 && Math.abs(System.currentTimeMillis() - mOrientationLandscapeStartTime) >= LANDSCAPE_EXPIRING_TIME) {
			Statistics.sendOnceStatistics(GoogleConfigDefine.ORIENTATION_LANDSCAPE_OVER_FIVE, GoogleConfigDefine.ORIENTATION_LANDSCAPE_OVER_FIVE);
		}
		mOrientationLandscapeStartTime = 0;
		super.onDestroy();
	}

	@Override
	public void finish() {
		if (AppEnv.DEBUG) {
			SimpleLog.d(TAG, String.format("%s.finish()", mClassName));
		}
		super.finish();
	}

	public String getString(Activity activity, int resId) {
		if (activity != null) {
			return activity.getString(resId);
		} else {
			return "";
		}
	}

	public String getString(FragmentActivity activity, int resId) {
		if (activity != null) {
			return activity.getString(resId);
		} else {
			return "";
		}
	}

	public String getString(Activity activity, int resId, Object object) {
		if (activity != null) {
			return activity.getString(resId, object);
		} else {
			return "";
		}
	}

	public String getString(FragmentActivity activity, int resId, Object object) {
		if (activity != null) {
			return activity.getString(resId, object);
		} else {
			return "";
		}
	}


	private void registerHomeListener() {
		mHomeWatcher = new HomeListener(this);
		mHomeWatcher.setOnHomePressedListener(new HomeListener.OnHomePressedListener() {

			@Override
			public void onHomePressed() {
				//TODO 进行点击Home键的处理
				if (!AccountLoginManager.getInstance().isUserLogined()) return;
				Statistics.sendOnceStatistics(GoogleConfigDefine.BOOKMARK_SYNC,GoogleConfigDefine.BOOKMARK_AUTO_SYNC,
						GoogleConfigDefine.BOOKMARK_BACK_HOME_SYNC);
				BookmarkManager.getInstance().syncBookmark(BaseActivity.this,false);
				SettingSyncManager.getInstance().syncSetting(SettingSyncManager.SYNC_TYPE_HOME);
				//UserHomeSiteManager.getInstance().syncHomeSite(UserHomeSiteManager.SYNC_TYPE_HOME);
			}

			@Override
			public void onHomeLongPressed() {
				//TODO 进行长按Home键的处理
			}
		});
		mHomeWatcher.startWatch();
	}
}
