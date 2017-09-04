package com.polar.browser.tabview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IEditLogo;
import com.polar.browser.i.IFullScreenDelegate;
import com.polar.browser.i.ISearchFrame;
import com.polar.browser.i.ISlideDelegate;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.ViewUtils;
import com.polar.browser.view.ObservableScrollView;
import com.polar.browser.view.ToolbarBottomController;

/**
 * HomeFrame，是BrowserActivity布局中的一块区域，用于主页的布局 本质上是主页的容器
 * TODO:未来将把主页容器与主页的view进行分离
 *
 * @author duanpeikun
 */
public class HomeFrame {

	private static final float SCREEN_SHOT_SCALE = 0.25f;
	private static final int TOOLBAR_BOTTOM_HEIGHT_DP = 40;
	private SearchView mHomeSearchView;
	private TabViewManager mTabViewMgrRef;
	private Bitmap mBitmap;
	private ISearchFrame mSearchFrame;
	private ISlideDelegate mSlideDelegate;
	private IFullScreenDelegate mFullScreenDelegate;
	private ViewGroup mRoot;

	public HomeFrame(ViewGroup root) {
		mRoot = root;
		initView();
	}

	public View getView() {
		return mHomeSearchView.getView();
	}

	@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
	public void init(TabViewManager tabManager, ISearchFrame searchFrame,
					 ISlideDelegate slideDelegate, IFullScreenDelegate fullScreenDelegate,
					 IEditLogo editLogoDelegate, ToolbarBottomController toolbarController) {
		mTabViewMgrRef = tabManager;
		mSearchFrame = searchFrame;
		this.mSlideDelegate = slideDelegate;
		mFullScreenDelegate = fullScreenDelegate;
		mHomeSearchView.init(toolbarController, mSearchFrame, mSlideDelegate, mFullScreenDelegate, editLogoDelegate);
	}

	private void initView() {
		mHomeSearchView = new SearchView(mRoot);
	}

	public void destroy() {
		mTabViewMgrRef = null;
		if (mHomeSearchView != null) {
			mHomeSearchView.destroy();
		}
	}

	public Bitmap getScreenShotBitmap() {
		if (mBitmap == null) {
			updateScreenShot();
		}
		return Bitmap.createBitmap(mBitmap);
	}

	private void updateScreenShot() {
		Activity activity = (Activity) mRoot.getContext();
		long begin = java.lang.System.currentTimeMillis();
		int px = 0;
		if (AppEnv.SCREEN_HEIGHT > AppEnv.SCREEN_WIDTH) {
			mBitmap = ViewUtils.getScreenShotSync(getView(), SCREEN_SHOT_SCALE, SCREEN_SHOT_SCALE, 0.9f); //ViewUtils.takeScreenShot(activity, SCREEN_SHOT_SCALE, SCREEN_SHOT_SCALE, 0.9f);
		} else {
			mBitmap = ViewUtils.getScreenShotSync(getView(), SCREEN_SHOT_SCALE, SCREEN_SHOT_SCALE, 0.8f);
			px = DensityUtil.dip2px(activity, TOOLBAR_BOTTOM_HEIGHT_DP);
		}
		long end = java.lang.System.currentTimeMillis();
		SimpleLog.d("ViewUtils", "takeScreenShot:" + String.valueOf(end - begin));
		Rect frame = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;
		px = (int) (px * SCREEN_SHOT_SCALE);
		begin = java.lang.System.currentTimeMillis();
		int searchbarHeight = 0;
		if (mHomeSearchView.isSearchbarTop()) {
			searchbarHeight = (int) ((mHomeSearchView.getSearchbarHeight() + statusBarHeight) * SCREEN_SHOT_SCALE);
		} else {
			searchbarHeight = (int) (statusBarHeight * SCREEN_SHOT_SCALE);
		}
		mBitmap = Bitmap.createBitmap(mBitmap, 0, searchbarHeight,
				mBitmap.getWidth(), mBitmap.getHeight() - searchbarHeight - px, null, false);
		end = java.lang.System.currentTimeMillis();
		SimpleLog.d("ViewUtils", "createBitmap:" + String.valueOf(end - begin));
	}

	public Bitmap getScreenShotSync() {
		updateScreenShot();
		return Bitmap.createBitmap(mBitmap);
	}

	public void onOrientationChanged() {
		mHomeSearchView.onOrientationChanged();
	}

	public void setVisibility(int visibility) {
		mHomeSearchView.getView().setVisibility(visibility);
	}

	public void scrollToTop() {
		ObservableScrollView view = mHomeSearchView.getScrollView();
		if (view!= null) {
			view.scrollTo(0, 0);
		}
	}
}
