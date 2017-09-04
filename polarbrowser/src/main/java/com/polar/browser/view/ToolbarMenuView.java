package com.polar.browser.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.ConfigData;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.IConfigObserver;
import com.polar.browser.i.IToolbarMenuDelegate;
import com.polar.browser.i.IViewShownListener;
import com.polar.browser.loginassistant.login.LoginActivity;
import com.polar.browser.loginassistant.login.LogoutActivity;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.push.api.ISystemNewsNumberCallback;
import com.polar.browser.push.api.SystemNewsAsyncApi;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.AnimListenerAdapter;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.GlideUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.vclibrary.bean.login.UserAccountData;
import com.polar.browser.view.clipview.view.CircleImageView;

import java.util.Arrays;
import java.util.List;


public class ToolbarMenuView extends RelativeLayout implements OnClickListener,
		OnTouchListener, IConfigObserver,ViewPager.OnPageChangeListener {

	private static final String TAG = "MenuView";

	private IToolbarMenuDelegate mMenuDelegate;

//	private TextView mBtnHistory;
//	private TextView mBtnFavorite;
//	private View mBtnDownload;
//	private TextView mBtnSetting;
//
//	private TextView mBtnNightMode;
//	private TextView mBtnNoImg;
//	private TextView mBtnWeb;
//	private TextView mBtnGoPlug;



    //推送消息提醒
    private ImageView mImgPush;

	private View mBackground;
	private View mMenu;
	private CircleImageView userAvatar;

	private boolean mIsInit = false;

	private Bitmap mBitmapBackground;

//	private ConfigData mConfig;

	/**
	 * 设置此变量，以解决hide动画在触发过程中再次触发的问题
	 */
	private boolean mIsHideAnimation = false;

	private IViewShownListener mViewShownListener;

	private ViewPager viewPager;
	private LinearLayout dotsLayout;
	private ViewPagerAdapter pagerAdater;
	private ToolbarMenuFirstPage firstPageView;
	private ToolbarMenuSecondPage secondPageView;
    private TextView msgDot;
	private TextView mUserName;


	public ToolbarMenuView(Context context) {
		this(context, null);
	}

	public ToolbarMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void unInit() {
		if (mBitmapBackground != null && !mBitmapBackground.isRecycled()) {
			mBitmapBackground.recycle();
		}
		mMenuDelegate = null;
		ConfigManager.getInstance().unregisterObserver(this);
	}

	public View getBackgroundView() {
		return mBackground;
	}

	/**
	 * @param listener
	 */
	public void setShownListener(IViewShownListener listener) {
		mViewShownListener = listener;
	}

	public boolean isInitView() {
		return mIsInit;
	}

	public void init(IToolbarMenuDelegate delegate, ConfigData config) {
		mMenuDelegate = delegate;

	}

	public void initView() {
		inflate(getContext(),R.layout.view_menu, this);
		this.setBackgroundColor(this.getResources().getColor(android.R.color.transparent));
		mMenu = findViewById(R.id.menu);
		mBackground = findViewById(R.id.menu_background);
		findViewById(R.id.menu_touch).setOnTouchListener(this);
        mImgPush = (ImageView) findViewById(R.id.img_push_tip);
        msgDot = (TextView)findViewById(R.id.menu_tv_msg_dot);
		userAvatar = (CircleImageView) findViewById(R.id.menu_avatar_iv);
		mUserName = (TextView) findViewById(R.id.menu_user_name);
		mImgPush.setOnClickListener(this);
		userAvatar.setOnClickListener(this);
		initViewPager();

		mIsInit = true;
		ConfigManager.getInstance().registerObserver(this);
	}

	private void initViewPager() {
		viewPager = (ViewPager) findViewById(R.id.menu_viewpager);
		dotsLayout = (LinearLayout) findViewById(R.id.dots_layout);
		viewPager.addOnPageChangeListener(this);
		firstPageView = new ToolbarMenuFirstPage(getContext());
		secondPageView = new ToolbarMenuSecondPage(getContext());
		pagerAdater = new ViewPagerAdapter(Arrays.<View>asList(firstPageView,secondPageView));
		viewPager.setAdapter(pagerAdater);
		initDots();
	}



	/**
     * 刷新消息推送信息提醒
	 * @param hint
	 */
	public void refreshImgPushUI(boolean hint) {
			SystemNewsAsyncApi.getInstance().getUnreadSystemNewsCountAsync(new ISystemNewsNumberCallback() {
                @Override
                public void notifyQueryResult(final long unreadSystemNewsCount) {
					ThreadManager.postTaskToUIHandler(new Runnable() {
						@Override
						public void run() {
							SimpleLog.d(TAG, "FCM_unreadSystemNewsCount=="+unreadSystemNewsCount);
							if (0L != unreadSystemNewsCount && 99L >= unreadSystemNewsCount){
                                msgDot.setVisibility(View.VISIBLE);
								msgDot.setText(String.valueOf(unreadSystemNewsCount));
							}else if (0L != unreadSystemNewsCount && 99L < unreadSystemNewsCount){
								msgDot.setVisibility(View.VISIBLE);
                                msgDot.setText("99+");
							}else {
                                msgDot.setVisibility(View.GONE);
							}
						}
					});
                }
            });
	}

    private void refreshBtnWebUI() {
		firstPageView.refreshBtnWebUI();

	}

	public void refreshBtnNightModeUI(boolean isNightMode) {
		secondPageView.refreshBtnNightModeUI(isNightMode);

	}

	public void refreshBtnAdBlockUI(boolean isAdBlock) {
		secondPageView.refreshBtnAdBlockUI(isAdBlock);

	}

	public void refreshBtnNoImgUI(boolean isEnabled) {
		firstPageView.refreshBtnNoImgUI(isEnabled);

	}

	public void refreshDownloadUI() {
		if (!isInitView()) {
			return;
		}
		firstPageView.refreshDownloadUI();
	}

	public void refreshUserInformationUI() {

		if(ConfigManager.getInstance().isLoginStatus()){ //登录状态才显示用户信息
			UserAccountData userAccountData = JuziApp.getUserAccountData();
			SimpleLog.d("--Login--", "userAccountData==null");
			if (userAccountData != null) {
				SimpleLog.d("ToolbarMenuView", "userAccountData="+userAccountData.toString());
				String avatar = userAccountData.getAvatar();
				String username = userAccountData.getUsername();
				if (!TextUtils.isEmpty(username)) {
					mUserName.setText(username);
				} else {
					mUserName.setText(getResources().getString(R.string.account_login_success));
				}
				GlideUtils.loadCircleImage(getContext(),avatar,userAccountData.getAvatarLastModified(),
						userAvatar,
						Drawable.createFromPath(ConfigManager.getInstance().getLocalUserAvatarPath()),
						R.drawable.menu_default_head);

			}
		}else{
			userAvatar.setImageResource(R.drawable.menu_default_head);
			mUserName.setText(getResources().getString(R.string.account_login));
		}

	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		hide();
		switch (v.getId()) {
            case R.id.img_push_tip:
				Runnable runSystemNewsList = new Runnable() {

					@Override
					public void run() {
						mMenuDelegate.openSystemNewsListActivity();
					}
				};
				ThreadManager.postDelayedTaskToUIHandler(runSystemNewsList, 10);
                break;
			case R.id.menu_avatar_iv:
				onAvatarClicked();
				Statistics.sendOnceStatistics(GoogleConfigDefine.ACCOUNT,GoogleConfigDefine.ACCOUNT_FROM_MAIN_MENU_LOGIN);
				break;
			default:
				break;
		}
	}

	private void onAvatarClicked() {
		if(!ConfigManager.getInstance().isLoginStatus()){ //未登录状态
			Intent intent = new Intent(getContext(), LoginActivity.class);
			getContext().startActivity(intent);
		}else{
			Intent intent = new Intent(getContext(), LogoutActivity.class);
			getContext().startActivity(intent);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsHideAnimation && this.getVisibility() == View.VISIBLE) {
			hide();
		}
		return true;
	}

	public void show() {
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.menu_slide_in_from_bottom);
		mMenu.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_in);
		getBackgroundView().startAnimation(backgroundAnim);
		backgroundAnim.setAnimationListener(new AnimListenerAdapter() {

			@Override
			public void onAnimationEnd(Animation animation) {
				if (ConfigManager.getInstance().isShowBottomMenuNavigate()) {
					ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
						@Override
						public void run() {
							viewPager.setCurrentItem(1,true);
							ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
								@Override
								public void run() {
									viewPager.setCurrentItem(0,true);
									ConfigManager.getInstance().setNotShowBottomNavigate();
								}
							},1000);
						}
					},500);


				}
			}
		});
		setVisibility(View.VISIBLE);
		if (mViewShownListener != null) {
			mViewShownListener.onViewShown();
		}
		refreshUI();
	}

	private void refreshUI() {
		refreshBtnWebUI();
		refreshAddBookmarkUI();
		refreshSaveWebUI();
		refreshBtnNightModeUI(ConfigManager.getInstance().isEnableNightMode());
		refreshImgPushUI(true);
		refreshFullScreenUI(ConfigManager.getInstance().isFullScreen());
		refreshShareUI();
		refreshFindUI();
		refreshDownloadUI();
		refreshUserInformationUI();
		refreshFontSizeUI();
		refreshBtnNoImgUI(ConfigManager.getInstance().isEnableImg());
		refreshBtnAdBlockUI(ConfigManager.getInstance().isAdBlock());
	}

	private void refreshFontSizeUI() {
		secondPageView.refreshFontSizeUI();
	}

	private void refreshFindUI() {
		secondPageView.refreshFindInPageUI();
	}

	private void refreshShareUI() {
		secondPageView.refreshShareUI();
	}

	public void refreshFullScreenUI(boolean fullScreen) {
		if(secondPageView!=null)
			secondPageView.refreshFullScreenUI(fullScreen);
	}

	private void refreshSaveWebUI() {
		firstPageView.refreshSaveWebUI();
	}

	private void refreshAddBookmarkUI() {
		firstPageView.refreshAddBookmarkUI();
	}



	public void hide() {
		SimpleLog.d(TAG, "hide()");
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.menu_slide_out_to_bottom);
		mMenu.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_out);
		getBackgroundView().startAnimation(backgroundAnim);
		backgroundAnim.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				setVisibility(View.GONE);
				mIsHideAnimation = false;
				viewPager.setCurrentItem(0);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
				mIsHideAnimation = true;
			}
		});
		if (mViewShownListener != null) {
			mViewShownListener.onViewHide();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		return false;
	}

	@Override
	public void notifyChanged(String key, String value) {
	}

	@Override
	public void notifyChanged(String key, final int value) {
		if (key.equals(ConfigDefine.UA_TYPE)) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					//切换PC模式刷新页面
					TabViewManager.getInstance().getCurrentTabView().reload();
					changeBtnWebUaType(value);
				}
			};
			ThreadManager.postTaskToUIHandler(r);
		}
	}

	private void changeBtnWebUaType(int value) {
//		if (value == ConfigDefine.UA_TYPE_PC) {
//			mBtnWeb.setCompoundDrawablesWithIntrinsicBounds(0,
//					R.drawable.web_on, 0, 0);
//		} else {
//			mBtnWeb.setCompoundDrawablesWithIntrinsicBounds(0,
//					R.drawable.web, 0, 0);
//		}
		firstPageView.changeBtnWebUaType(value);

	}

	public void onOrientationChanged() {
	}

	@Override
	public void notifyChanged(String key, boolean value) {
		// TODO Auto-generated method stub
	}

	private void initDots() {
		if (pagerAdater == null) return;
		int count = pagerAdater.getCount();
		int dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
		int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
		for (int i = 0; i < count; i++) {
			ImageView imageView = new ImageView(getContext());
			if (i == 0) {
				imageView.setImageResource(R.drawable.dot_select);
			} else {
				imageView.setImageResource(R.drawable.dot_normal);
			}
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp, dp);
			params.setMargins(margin, 0, margin, 0);

			// 加载到布局容器
			dotsLayout.addView(imageView, params);
		}

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {


		for (int i = 0; i < dotsLayout.getChildCount(); i++) {
			ImageView img = (ImageView) dotsLayout.getChildAt(i);
			if (i == (position % dotsLayout.getChildCount())) {
				img.setImageResource(R.drawable.dot_select);
			} else {
				img.setImageResource(R.drawable.dot_normal);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}


	private class ViewPagerAdapter extends PagerAdapter {
		private List<View> views;

		ViewPagerAdapter(List<View> views) {
			this.views = views;
		}

		@Override
		public int getCount() {
			return views == null ? 0 : views.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(views.get(position), 0);
			return views.get(position);
		}
	}

}
