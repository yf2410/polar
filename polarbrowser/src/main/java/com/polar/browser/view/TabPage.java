package com.polar.browser.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.tabview.TabView;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;

/**
 * 多标签页中单个标签View
 *
 * @author duan
 */

public class TabPage extends RelativeLayout implements android.view.View.OnClickListener {

	private static final int ANIM_DURATION = 300;
	private static final int SCREENSHOT_WIDTH_DP = 125;
	private static final String TAG = "TabPage";
	/**
	 * 执行动画的时间
	 */
	protected int mAnimationTime = ANIM_DURATION;
	private ImageView mIvScreenShot;
	private TextView mTvDescribe;
	private View mBtnClose;
	private View mRoot;
	/**
	 * 滑动的最小速度
	 */
	private int mMinFlingVelocity;
	/**
	 * 滑动的最大速度
	 */
	private int mMaxFlingVelocity;
	/**
	 * 用来标记用户是否正在滑动中
	 */
	private boolean mSwiping;
	/**
	 * 滑动速度检测类
	 */
	private VelocityTracker mVelocityTracker;

	private float mDownX;
	private float mDownY;
	/**
	 * item的宽度
	 */
	private int mViewWidth;

	/**
	 * Item滑出界面回调的接口
	 */
	private IOnDismissListener mOnDismissListener;

	private TabView mTabView;

	public TabPage(Context context) {
		this(context, null);
	}

	public TabPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.item_multi_window, this);
		mIvScreenShot = (ImageView) findViewById(R.id.screen_shot);
		mTvDescribe = (TextView) findViewById(R.id.tv_describe);
		mBtnClose = findViewById(R.id.close);
		mRoot = findViewById(R.id.multiwindow_root);
		mBtnClose.setOnClickListener(this);
	}

	public void changeBitmapSize(float heightWidthRatio) {
		Bitmap bitmap = mTabView.getScreenShot();
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
			mIvScreenShot.setImageBitmap(bitmap);
			RelativeLayout.LayoutParams params;
			params = (LayoutParams) mIvScreenShot.getLayoutParams();
			int width = DensityUtil.dip2px(this.getContext(), SCREENSHOT_WIDTH_DP);
			params.width = width;
			params.height = (int) (width * heightWidthRatio);
			mIvScreenShot.setLayoutParams(params);
		} else {
			mIvScreenShot.setImageResource(R.drawable.ic_launcher);
		}
	}

	public void init(TabView tabView, IOnDismissListener onDismissListener, float heightWidthRatio, boolean isEnableNightMode) {
		this.mTabView = tabView;
		this.mOnDismissListener = onDismissListener;
		String data = null;
		Bitmap bitmap = null;
		if (tabView != null) {
			data = tabView.getTitle();
			bitmap = tabView.getScreenShot();
		}
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
			mIvScreenShot.setImageBitmap(bitmap);
			RelativeLayout.LayoutParams params;
			params = (LayoutParams) mIvScreenShot.getLayoutParams();
			int width = DensityUtil.dip2px(this.getContext(), SCREENSHOT_WIDTH_DP);
			params.width = width;
			params.height = (int) (width * heightWidthRatio);
			mIvScreenShot.setLayoutParams(params);
		} else {
			mIvScreenShot.setImageResource(R.drawable.ic_launcher);
		}
		if (!TextUtils.isEmpty(data)) {
			SimpleLog.d(TAG, "title is : " + data);
			if (TextUtils.equals(data, "home.html")) {//mIsHome  状态存在更新不及时，导致直接获取home.html。
				data = JuziApp.getAppContext().getString(R.string.home_page);
			}
			mTvDescribe.setText(data);
		}
		refreshCheckState();
	}

	/**
	 * 刷新选中状态
	 */
	public void refreshCheckState() {
		if (getTabId() == TabViewManager.getInstance().getCurrentTabId()) {
			mIvScreenShot.setBackgroundResource(R.drawable.screen_shot_bg_pre);
			mTvDescribe.setTextColor(getResources().getColor(R.color.set_about));
		} else {
			mIvScreenShot.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
			mTvDescribe.setTextColor(getResources().getColor(R.color.white));
		}
	}

	public Integer getTabId() {
		if (mTabView != null) {
			return mTabView.getId();
		}
		return 0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handleActionDown(ev);
				return true;
			case MotionEvent.ACTION_MOVE:
				return handleActionMove(ev);
			case MotionEvent.ACTION_UP:
				handleActionUp(ev);
				break;
		}
		return super.onTouchEvent(ev);
	}


	/**
	 * 按下事件处理
	 *
	 * @param ev
	 * @return
	 */
	private void handleActionDown(MotionEvent ev) {
		mDownX = ev.getX();
		mDownY = ev.getY();
		mViewWidth = getWidth();
		//加入速度检测
		mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(ev);
	}


	/**
	 * 处理手指滑动的方法
	 *
	 * @param ev
	 * @return
	 */
	private boolean handleActionMove(MotionEvent ev) {
		if (mVelocityTracker == null) {
			return super.onTouchEvent(ev);
		}
		float deltaX = ev.getX() - mDownX;
		float deltaY = ev.getY() - mDownY;
		// X方向滑动的距离大于mSlop并且Y方向滑动的距离小于mSlop，表示可以滑动
		if (Math.abs(deltaX) > AppEnv.MIN_SLIDING && Math.abs(deltaY) < AppEnv.MIN_SLIDING) {
			mSwiping = true;
			//当手指滑动item,取消item的点击事件，不然我们滑动Item也伴随着item点击事件的发生
			MotionEvent cancelEvent = MotionEvent.obtain(ev);
			cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
					(ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
			onTouchEvent(cancelEvent);
		}
		if (mSwiping) {
			// -------ScrollView不拦截事件--------- //
			getParent().requestDisallowInterceptTouchEvent(true);
			// 跟谁手指移动item
//			ViewHelper.setTranslationX(mRoot, deltaX);
			mRoot.setTranslationX(deltaX);
			// 透明度渐变
//			ViewHelper.setAlpha(mRoot, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX)/ mViewWidth)));
			mRoot.setAlpha(Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
			// 手指滑动的时候,返回true，表示SwipeDismissListView自己处理onTouchEvent,其他的就交给父类来处理
			return true;
		} else {
			getParent().requestDisallowInterceptTouchEvent(false);
//			ViewHelper.setTranslationX(mRoot, 0);
//			ViewHelper.setAlpha(mRoot, 1);
			mRoot.setTranslationX(0);
			mRoot.setAlpha(1);
			return super.onTouchEvent(ev);
		}
	}

	/**
	 * 手指抬起的事件处理
	 *
	 * @param ev
	 */
	private void handleActionUp(MotionEvent ev) {
		float deltaX = ev.getX() - mDownX;
		float deltaY = ev.getY() - mDownY;
		if (mVelocityTracker == null || (Math.abs(deltaX) < AppEnv.MIN_SLIDING && Math.abs(deltaY) < AppEnv.MIN_SLIDING)) {
			// 点击事件，点击后条目切换
			mOnDismissListener.onSwitch(this);
			return;
		}
		//通过滑动的距离计算出X,Y方向的速度
		mVelocityTracker.computeCurrentVelocity(1000);
		float velocityX = Math.abs(mVelocityTracker.getXVelocity());
		float velocityY = Math.abs(mVelocityTracker.getYVelocity());
		boolean dismiss = false; //item是否要滑出屏幕
		boolean dismissRight = false;//是否往右边删除
		//当拖动item的距离大于item的1/3，item滑出屏幕
		if (Math.abs(deltaX) > mViewWidth / 3) {
			dismiss = true;
			dismissRight = deltaX > 0;
			//手指在屏幕滑动的速度在某个范围内，也使得item滑出屏幕
		} else if (mMinFlingVelocity <= velocityX
				&& velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
			dismiss = true;
			dismissRight = mVelocityTracker.getXVelocity() > 0;
		}
		if (dismiss) {
			mAnimationTime = (int) ((getWidth() - Math.abs(deltaX)) / getWidth() * mAnimationTime);
			mRoot.animate()
					.translationX(dismissRight ? mViewWidth : -mViewWidth)//X轴方向的移动距离
					.alpha(0)
					.setDuration(mAnimationTime)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							//Item滑出界面之后执行删除
							if (mOnDismissListener != null) {
								mOnDismissListener.onDismiss(TabPage.this);
							}
						}
					});
		} else {
			mAnimationTime = ANIM_DURATION;
			//将item滑动至开始位置
			mRoot.animate()
					.translationX(0)
					.alpha(1)
					.setDuration(mAnimationTime).setListener(null);
		}
		//移除速度检测
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
		mSwiping = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.close:
				mRoot.animate()
						.translationX(getWidth())//X轴方向的移动距离
						.alpha(0)
						.setDuration(mAnimationTime)
						.setListener(new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								//Item滑出界面之后执行删除
								if (mOnDismissListener != null) {
									mOnDismissListener.onDismiss(TabPage.this);
								}
							}
						});
				break;
			default:
				break;
		}
	}
}
