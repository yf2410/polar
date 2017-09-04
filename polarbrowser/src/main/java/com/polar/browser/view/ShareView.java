package com.polar.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.i.IShareDelegate;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;

public class ShareView extends RelativeLayout implements OnClickListener, OnTouchListener {

	// 手指移动在多少以内的范围，认为是点击，目前是5dp
	private static final int CLICK_MOVE_RANGE_DP = 20;
	private static final int BUTTON_NUM = 3;
	private static final int INDEX_FACEBOOK = 0;
	private static final int INDEX_TWITTER = 1;
	private static final int INDEX_WHATSAPP = 2;
	private static final int INDEX_MAIL = 3;
	private static final int INDEX_MESSAGE = 4;
	private static final int INDEX_COPYLINK = 5;
	/**
	 * 设置此变量，以解决hide动画在触发过程中再次触发的问题
	 */
	private boolean mIsHideAnimation = false;
	private View mMenuArea;
	private View mBackground;
	private TextView mBtnFaceBook;
	private TextView mBtnTwitter;
	private TextView mBtnWhatsapp;
	private TextView mBtnMail;
	private TextView mBtnMessage;
	private TextView mBtnCopyLink;
	private TextView mBtnSystemShare;
	// 计算按下的坐标
	private float mTouchX;
	private float mTouchY;
	// 是否为按钮点击事件
	private boolean mToolbarBottomClick = false;
	// 当前按下的按钮
	private View mPressedBtn;
	private IShareDelegate mShareDelegate;

	public ShareView(Context context) {
		this(context, null);
	}

	public ShareView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.view_share, this);
		mMenuArea = findViewById(R.id.menu_area);
		mBackground = findViewById(R.id.menu_background);
		mBackground.setOnClickListener(this);
		mBtnFaceBook = (TextView) findViewById(R.id.share_facebook);
		mBtnFaceBook.setOnClickListener(this);
//		mBtnWechat = (TextView) findViewById(R.id.share_wechat);
//		mBtnWechat.setOnClickListener(this);
		mBtnTwitter = (TextView) findViewById(R.id.share_twitter);
		mBtnTwitter.setOnClickListener(this);
//		mBtnWeibo = (TextView) findViewById(R.id.share_sina_weibo);
//		mBtnWeibo.setOnClickListener(this);
		mBtnWhatsapp = (TextView) findViewById(R.id.share_whatsapp);
		mBtnWhatsapp.setOnClickListener(this);
		mBtnMail = (TextView) findViewById(R.id.share_email);
		mBtnMail.setOnClickListener(this);
		mBtnMessage = (TextView) findViewById(R.id.share_message);
		mBtnMessage.setOnClickListener(this);
		mBtnCopyLink = (TextView) findViewById(R.id.copy_link);
		mBtnCopyLink.setOnClickListener(this);
		mBtnSystemShare = (TextView) findViewById(R.id.vc_share_system);
		mBtnSystemShare.setOnClickListener(this);
		findViewById(R.id.menu_touch).setOnTouchListener(this);
	}

	public void destroy() {
	}

	/**
	 * 初始化
	 *
	 * @param shareDelegate 分享操作
	 */
	public void init(IShareDelegate shareDelegate) {
		this.mShareDelegate = shareDelegate;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.menu_background:
				// 点击背景
				if (!mIsHideAnimation && mBackground.isShown()) {
					hide();
				}
				break;
			case R.id.share_facebook:
				Statistics.sendOnceStatistics(GoogleConfigDefine.SHARE, "Facebook");
				hide(new CallBack() {
					@Override
					public void callBack() {
						if (mShareDelegate != null) {
							mShareDelegate.share2FaceBook();
						}
					}
				});
				break;
			case R.id.share_twitter:
				Statistics.sendOnceStatistics(GoogleConfigDefine.SHARE, "Twitter");
				hide(new CallBack() {
					@Override
					public void callBack() {
						if (mShareDelegate != null) {
							mShareDelegate.share2Twitter();
						}
					}
				});
				break;
			case R.id.share_whatsapp:
				Statistics.sendOnceStatistics(GoogleConfigDefine.SHARE, "WhatsApp");
				hide(new CallBack() {
					@Override
					public void callBack() {
						if (mShareDelegate != null) {
							mShareDelegate.share2Whatsapp();
						}
					}
				});
				break;
			case R.id.share_email:
				// 邮件
				// Toast.makeText(getContext(), "分享 - 邮件",
				// Toast.LENGTH_SHORT).show();
				Statistics.sendOnceStatistics(GoogleConfigDefine.SHARE, "email");
				hide(new CallBack() {
					@Override
					public void callBack() {
						if (mShareDelegate != null) {
							mShareDelegate.share2Email();
						}
					}
				});
				break;
			case R.id.share_message:
				// 短信
				// Toast.makeText(getContext(), "分享 - 短信",
				// Toast.LENGTH_SHORT).show();
				Statistics.sendOnceStatistics(GoogleConfigDefine.SHARE, "message");
				hide(new CallBack() {
					@Override
					public void callBack() {
						if (mShareDelegate != null) {
							mShareDelegate.share2ShortMessage();
						}
					}
				});
				break;
			case R.id.copy_link:
				// 复制链接
				Statistics.sendOnceStatistics(GoogleConfigDefine.SHARE, "copy_link");
				CustomToastUtils.getInstance().showTextToast(R.string.copy_link);
				hide(new CallBack() {
					@Override
					public void callBack() {
						if (mShareDelegate != null) {
							mShareDelegate.copyLink();
						}
					}
				});
				break;
			case R.id.vc_share_system:
				// 系统分享
				hide(new CallBack() {
					@Override
					public void callBack() {
						if (mShareDelegate != null) {
							mShareDelegate.systemShare();
						}
					}
				});
				break;
			default:
				break;
		}
	}

	private void hide(AnimationListener listener) {
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.slide_out_to_bottom);
		mMenuArea.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_out);
		backgroundAnim.setDuration(200);
		mBackground.startAnimation(backgroundAnim);
		backgroundAnim.setAnimationListener(listener);
	}

	public void hide() {
		hide(new HideAnimationListener());
	}

	public void hide(CallBack callBack) {
		hide(new HideAnimationListener(callBack));
	}

	public void show() {
		Animation menuAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.slide_in_from_bottom);
		mMenuArea.startAnimation(menuAnim);
		Animation backgroundAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.alpha_menu_in);
		mBackground.startAnimation(backgroundAnim);
		if (TabViewManager.getInstance().isCurrentHome()) {
			mBtnCopyLink.setVisibility(INVISIBLE);
			mBtnCopyLink.setOnClickListener(null);
		} else {
			mBtnCopyLink.setVisibility(VISIBLE);
			mBtnCopyLink.setOnClickListener(this);
		}
		setVisibility(View.VISIBLE);
	}

	private void handleActionDown(MotionEvent ev) {
		mTouchX = ev.getX();
		mTouchY = ev.getY();
		mToolbarBottomClick = true;
		mPressedBtn = getCurrentBottomController(mTouchX, mTouchY);
		if (mPressedBtn != null && mPressedBtn.isEnabled()) {
			mPressedBtn.setPressed(true);
		}
	}

	private boolean handleActionMove(MotionEvent ev) {
		float curX = ev.getX();
		float curY = ev.getY();
		// 如果手指移动的范围很小，认为是点击
		if (mToolbarBottomClick
				&& Math.abs(mTouchX - curX) < DensityUtil.dip2px(getContext(),
				CLICK_MOVE_RANGE_DP)
				&& Math.abs(mTouchY - curY) < DensityUtil.dip2px(getContext(),
				CLICK_MOVE_RANGE_DP)) {
			mToolbarBottomClick = true;
			return true;
		} else {
			mToolbarBottomClick = false;
			if (mPressedBtn != null && mPressedBtn.isEnabled()) {
				mPressedBtn.setPressed(false);
			}
		}
		mTouchX = curX;
		mTouchY = curY;
		boolean ret = false;
		return ret;
	}

	private boolean handleActionUp(MotionEvent ev) {
		// 如果是点击，则执行点击，否则执行滑动
		if (mToolbarBottomClick) {
			View v = getCurrentBottomController(ev.getX(), ev.getY());
			if (v != null && v.isEnabled()) {
				v.performClick();
				mToolbarBottomClick = false;
				if (mPressedBtn != null && mPressedBtn.isEnabled()) {
					mPressedBtn.setPressed(false);
				}
			} else {
				hide();
			}
			return true;
		}
		return false;
	}

	private View getCurrentBottomController(float x, float y) {
		View v = null;
		int[] location = new int[2];
		mBtnFaceBook.getLocationInWindow(location);
		float top = location[1];
		float left = location[0];
		if (x < left) {
			return v;
		}
		mBackground.getLocationInWindow(location);
		y = y + location[1];
		if (y < top) {
			return v;
		}
		mBtnWhatsapp.getLocationInWindow(location);
		float right = location[0];
		mBtnMail.getLocationInWindow(location);
		int bottom = location[1];
		float buttonWidth = (right - left) / (BUTTON_NUM - 1);
		// 坐标已经超过最右边的按钮，说明点击的不是按钮，直接返回吧
		if (x > right + buttonWidth) {
			return v;
		}
		mBackground.getLocationInWindow(location);
		y = y + location[1];
		int index = y < bottom ? (int) ((x - left) / buttonWidth)
				: (int) ((x - left) / buttonWidth) + BUTTON_NUM;
		switch (index) {
			case INDEX_FACEBOOK:
				return mBtnFaceBook;
			case INDEX_TWITTER:
				return mBtnTwitter;
			case INDEX_WHATSAPP:
				return mBtnWhatsapp;
			case INDEX_MAIL:
				return mBtnMail;
			case INDEX_MESSAGE:
				return mBtnMessage;
			case INDEX_COPYLINK:
				return mBtnCopyLink;
			default:
				break;
		}
		return v;
	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		/*switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handleActionDown(ev);
				return true;
			case MotionEvent.ACTION_MOVE:
				return handleActionMove(ev);
			case MotionEvent.ACTION_UP:
				return handleActionUp(ev);
		}*/
		return false;
	}

	public void onOrientationChanged() {
	}

	private interface CallBack {
		public void callBack();
	}

	private class HideAnimationListener implements AnimationListener {

		// 动画结束回调
		private CallBack callBack;

		public HideAnimationListener() {
		}

		public HideAnimationListener(CallBack callBack) {
			this.callBack = callBack;
		}

		public void onAnimationEnd(Animation animation) {
			setVisibility(View.GONE);
			mIsHideAnimation = false;
			if (callBack != null) {
				callBack.callBack();
			}
		}

		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
		}

		public void onAnimationStart(Animation animation) {
			mIsHideAnimation = true;
		}
	}
}
