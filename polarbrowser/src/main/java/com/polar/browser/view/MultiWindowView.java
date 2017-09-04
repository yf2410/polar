package com.polar.browser.view;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.TabViewManager.TabData;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.tabview.TabView;
import com.polar.browser.utils.AnimListenerAdapter;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SimpleLog;

import java.util.List;

public class MultiWindowView extends RelativeLayout implements
		android.view.View.OnClickListener, IOnDismissListener {

	private static final String TAG = "MultiWindowView";

	private static final float HEIGHT_WIDTH_RATIO_LANDSCAPE = 0.55f;
	private static final float HEIGHT_WIDTH_RATIO_PORTRAIT = 0.8f;


	/**
	 * 设置此变量，以解决hide动画在触发过程中再次触发的问题
	 */
	private boolean mIsAniming = false;

	private TabViewManager mTabManager;

	private TextView mBtnPrivacyMode;
	private ImageView mIvBack;
	private ImageView mBtnNewTab;

	private LinearLayout mContainer;
	private LayoutTransition mTransition;

	private Bitmap mBitmapBackground;

	private List<TabData> mDataSet;

	private Animator mChangeDisappearingAnimator;

	private LayoutAnimationController mStartAnimController;

	private ScrollView mScrollView;

	private boolean mIsHide = false;

	// 浏览器view
	private View mBrowserView;

	private boolean mIsNeedShowBrowserView = true;

	private View mMask;
	private float downX;
	private float downY;

	public MultiWindowView(Context context) {
		this(context, null);
	}

	public MultiWindowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public void init(TabViewManager manager, Bitmap bitmap, boolean isLandscape, View browserView) {
		mTabManager = manager;
		mBitmapBackground = bitmap;
		mBrowserView = browserView;
		mIsNeedShowBrowserView = true;
		initData(manager.getTabViewList(), isLandscape);
		initListener();
	}

	public void destory() {
		if (mBitmapBackground != null && !mBitmapBackground.isRecycled()) {
			mBitmapBackground.recycle();
		}
	}

	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.view_muti_window, this);
		this.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		mContainer = (LinearLayout) findViewById(R.id.id_container);
		mBtnPrivacyMode = (TextView) findViewById(R.id.privacy_mode);
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mBtnNewTab = (ImageView) findViewById(R.id.new_tab);
		mScrollView = (ScrollView) findViewById(R.id.multiwindow_scroll_view);
		mMask = findViewById(R.id.multiwindow_menu_mask);
		initLayoutTransition();
		setPrivacyMode(ConfigManager.getInstance().isPrivacyMode());
	}

	private void initLayoutTransition() {
		// 默认动画全部开启
		mTransition = new LayoutTransition();
		mChangeDisappearingAnimator = mTransition
				.getAnimator(LayoutTransition.CHANGE_DISAPPEARING);
		mTransition.setAnimator(LayoutTransition.APPEARING, null);
		mTransition.setAnimator(LayoutTransition.CHANGE_APPEARING, null);
		mTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, null);
		mTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
		mContainer.setLayoutTransition(mTransition);
	}

	public void onOrientationChanged(boolean isLandscape, Bitmap bitmap) {
		// 截当前tab的屏
//		mTabManager.getCurrentTabView().captureScreen();
		if (mScrollView == null || mContainer == null || mDataSet == null) {
			return;
		}
		if (isShown()) {
			float heightWidthRatio = isLandscape ? HEIGHT_WIDTH_RATIO_LANDSCAPE : HEIGHT_WIDTH_RATIO_PORTRAIT;
			int currentId = TabViewManager.getInstance().getCurrentTabId();
			int position = 0;
			int size = mDataSet.size();
			for (int i = 0; i < size; i++) {
				TabView tabView = getItemByPosition(i);
				if (currentId == tabView.getId()) {
					position = size - i;
				}
				TabPage tabPage = (TabPage) mContainer.getChildAt(i);
				if (tabPage != null) {
					tabPage.changeBitmapSize(heightWidthRatio);
				}
			}
			measureView(mContainer);
			int height = mContainer.getMeasuredHeight();
			// 每次进入滚动到当前标签位置
			int y = height / size * position;
			mScrollView.scrollTo(0, y);
		}
	}

	/**
	 * 设置进入时的动画
	 */
	private void setShowAnim() {
		// 进入时的LayoutAnimation
		Animation animation = AnimationUtils.loadAnimation(getContext(),
				R.anim.tab_page_item_in);
		mStartAnimController = new LayoutAnimationController(animation);
		mStartAnimController.setDelay(0.07f);
		mStartAnimController.setOrder(LayoutAnimationController.ORDER_NORMAL);
		mContainer.setLayoutAnimation(mStartAnimController);
		animation.setAnimationListener(new AnimListenerAdapter() {
			@Override
			public void onAnimationEnd(Animation animation) {
				// 进入动画结束后，设置CHANGE_DISAPPEARING为默认动画，不然删除标签没有动画
				if (mStartAnimController.isDone()) {
					mTransition.setAnimator(
							LayoutTransition.CHANGE_DISAPPEARING,
							mChangeDisappearingAnimator);
					mIsAniming = false;
				}
			}

			@Override
			public void onAnimationStart(Animation animation) {
				mIsAniming = true;
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void initData(List<TabData> dataList, boolean isLandscape) {
		// 截当前tab的屏
		if (mTabManager.getCurrentTabView() != null)
		    mTabManager.getCurrentTabView().captureScreen();
		float heightWidthRatio = isLandscape ? HEIGHT_WIDTH_RATIO_LANDSCAPE : HEIGHT_WIDTH_RATIO_PORTRAIT;
		setShowAnim();
		// 设置CHANGE_DISAPPEARING为null，不然新增的标签会有弹出动画
		mTransition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, null);
		mDataSet = dataList;
		mContainer.removeAllViews();
		int currentId = TabViewManager.getInstance().getCurrentTabId();
		int position = 0;
		int size = mDataSet.size();
		for (int i = 0; i < size; i++) {
			TabView tabView = getItemByPosition(i);
			if (currentId == tabView.getId()) {
				position = size - i;
			}
			TabPage tabPage = new TabPage(getContext());
			tabPage.init(tabView, this, heightWidthRatio, ConfigManager.getInstance().isEnableNightMode());
			mContainer.addView(tabPage, 0);
		}
		if (mBitmapBackground != null) {
			BitmapDrawable backDrawable = new BitmapDrawable(getResources(), mBitmapBackground);
			this.setBackgroundDrawable(backDrawable);
		}
		// 新进入开启动画
		mContainer.startLayoutAnimation();
		measureView(mContainer);
		int height = mContainer.getMeasuredHeight();
		// 每次进入滚动到当前标签位置
		final int y = height / size * (position - 1);
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				mScrollView.scrollTo(0, y);
			}
		}, 30);
	}

	private TabView getItemByPosition(int position) {
		return mDataSet.get(position).tabView;
	}

	private void initListener() {
		mBtnNewTab.setOnClickListener(this);
		mBtnPrivacyMode.setOnClickListener(this);
		mIvBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		switch (v.getId()) {
			case R.id.new_tab:
				if (!mIsAniming) {
					hide(new HideAndAddAnimationListener());
				}
				break;
			case R.id.privacy_mode:
				boolean privacyMode = ConfigManager.getInstance().isPrivacyMode();
				ConfigManager.getInstance().setPrivacyMode(!privacyMode);
				break;
			case R.id.iv_back:
			case R.id.iv_multi:
				if (!mIsAniming) {
					hide();
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 删除条目后的回调
	 */
	@Override
	public void onDismiss(final TabPage tabPage) {
		if (tabPage != null && mContainer != null) {
			boolean isDeletedCurrent = false;
			if (tabPage.getTabId() == TabViewManager.getInstance()
					.getCurrentTabId()) {
				// 删除的是当前页面的话，刷新选中项
				isDeletedCurrent = true;
			}
			final boolean test = isDeletedCurrent;
			TabViewManager.getInstance().removeTabViewById(tabPage.getTabId());
			post(new Runnable() {
				@Override
				public void run() {
					mContainer.removeView(tabPage);
					if (mContainer.getChildCount() == 0) {
						hide();
					}
					if (test) {
						// 刷新
						for (int i = 0; i < mContainer.getChildCount(); i++) {
							TabPage tp = (TabPage) mContainer.getChildAt(i);
							tp.refreshCheckState();
						}
					}
				}
			});
		}
	}

	/**
	 * 切换条目
	 */
	@Override
	public void onSwitch(TabPage tabPage) {
		if (tabPage != null && mContainer != null) {
//			ConfigWrapper.put(ConfigDefine.TAB_CLICK_ID, tabPage.getTabId());
//			ConfigWrapper.commit();
			mIsNeedShowBrowserView = false;
			TabViewManager.getInstance().switchTabByKey(tabPage.getTabId());
			hide();
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 滑动多标签不消失，点击抬手多标签消失
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				downX = event.getX();
				downY = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				float upX = event.getX();
				float upY = event.getY();
				if (Math.abs(upY - downY) < AppEnv.MIN_SLIDING && Math.abs(upX - downX) < AppEnv.MIN_SLIDING) {
					if (!mIsAniming && this.getVisibility() == View.VISIBLE) {
						hide();
					}
				}
				break;
			default:
				break;
		}
		return true;
	}

	private void hide(AnimationListener listener) {
		SimpleLog.d(TAG, "hide");
		mIsHide = true;
		if (mIsNeedShowBrowserView) {
			SimpleLog.d(TAG, "mBrowserView.setVisibility(View.VISIBLE)");
			mBrowserView.setVisibility(View.VISIBLE);
		}
		for (int i = 0; i < mContainer.getChildCount(); i++) {
			View v = mContainer.getChildAt(i);
			Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.tab_page_item_out);
			anim.setStartOffset(i * 30);
			v.startAnimation(anim);
		}
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				android.R.anim.fade_out);
		this.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_mulit_window_out);
		this.startAnimation(backgroundAnim);
		backgroundAnim.setAnimationListener(listener);
	}

	public void hide() {
		if(mIsAniming)return;
		hide(new HideAnimationListener());
		mIsAniming = true;
	}

	public void show() {
		SimpleLog.d(TAG, "show");
		mIsHide = false;
		Runnable r = new Runnable() {

			@Override
			public void run() {
				if (!mIsHide) {
					SimpleLog.d(TAG, "mBrowserView.setVisibility(View.GONE)");
					mBrowserView.setVisibility(View.GONE);
				}
			}
		};
		ThreadManager.postDelayedTaskToUIHandler(r, 200);
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_in);
		menuAnim.setStartOffset(250);
		this.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_in);
		this.startAnimation(backgroundAnim);
		setVisibility(View.VISIBLE);
	}

	private void measureView(View childView) {
		android.view.ViewGroup.LayoutParams p = childView.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int height = p.height;
		int childHeightSpec;
		if (height > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(height,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		childView.measure(childWidthSpec, childHeightSpec);
	}

	public void setPrivacyMode(boolean privacyMode, boolean isNotifyChanged) {
		if (privacyMode) {
			mBtnPrivacyMode.setTextColor(getResources().getColor(R.color.set_about));
			mMask.setBackgroundColor(getResources().getColor(R.color.privacy_on_mask));
			if (isNotifyChanged) {
				CustomToastUtils.getInstance().showTextToast(R.string.tip_privacy_on);
				Statistics.sendOnceStatistics(GoogleConfigDefine.FUCTION_CLICK, GoogleConfigDefine.FUCTION_CLICK_PRIVACY_MODE);
			}
		} else {
			mBtnPrivacyMode.setTextColor(getResources().getColor(R.color.white));
			mMask.setBackgroundColor(getResources().getColor(R.color.privacy_off_mask));
			if (isNotifyChanged && mMask.isShown()) {
				CustomToastUtils.getInstance().showTextToast(R.string.tip_privacy_off);
			}
		}
	}

	public void setPrivacyMode(boolean privacyMode) {
		setPrivacyMode(privacyMode, false);
	}

	public void switch2fullScreen(boolean fullScreen) {
		int topMargin = 0;
		if (fullScreen) {
			topMargin = 0;
		} else {
			topMargin = AppEnv.STATUS_BAR_HEIGHT;
		}
		initImmersion(topMargin);
	}

	/**
	 * 初始化沉浸式
	 */
	private void initImmersion(int topMargin) {
		if (android.os.Build.VERSION.SDK_INT > 18) {
			if (mScrollView != null) {
				RelativeLayout.LayoutParams params = (LayoutParams) mScrollView.getLayoutParams();
				params.topMargin = topMargin;
				mScrollView.setLayoutParams(params);
			}
		}
	}

	private class HideAnimationListener extends AnimListenerAdapter {
		public void onAnimationEnd(Animation animation) {
			setVisibility(View.GONE);
			mIsAniming = false;
		}

		public void onAnimationStart(Animation animation) {
			mIsAniming = true;
		}
	}

	private class HideAndAddAnimationListener extends AnimListenerAdapter {
		public void onAnimationEnd(Animation animation) {
			setVisibility(View.GONE);
			mIsAniming = false;
		}

		public void onAnimationStart(Animation animation) {
			mIsAniming = true;
			mTabManager.addTabView(true,true,true);
		}
	}
}