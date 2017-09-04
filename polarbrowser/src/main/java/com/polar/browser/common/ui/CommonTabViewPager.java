package com.polar.browser.common.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 顶部的tab切换控件，截断了ViewPager的OnPageChangeListener下的事件，如果需要监听这些事件，请调用setOnPageChangedListener
 */
public class CommonTabViewPager extends LinearLayout implements OnPageChangeListener {

	public static final int STYLE_COMMON = 0;
	public static final int STYLE_GREY = 1;

	private static final int DEFAULT_LINE_WIDTH_DP = 30;

	private LinearLayout mSliderBar;
	private View mSliderLine;
	//    private View mSliderBottomLine;
	private ViewPager mPager;

	private List<View> mTitleViews;
	private OnPageChangeListener mOnPageChangedListener;

	private int mLineWidth = DensityUtil.dip2px(getContext(), DEFAULT_LINE_WIDTH_DP);

	private int mStyle;
	private LinearLayout mIvBack;
	private ImageView rightMenuIv;
	private View topBar;

	public CommonTabViewPager(Context context) {
		super(context);
		init();
	}

	public CommonTabViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public OnPageChangeListener getOnPageChangedListener() {
		return mOnPageChangedListener;
	}

	public void setOnPageChangedListener(OnPageChangeListener onPageChangedListener) {
		mOnPageChangedListener = onPageChangedListener;
	}

	public void setStyle(int style) {
		mStyle = style;
		if (mStyle == STYLE_GREY) {
//			findViewById(R.id.common_slider_bar).setBackgroundColor(getResources().getColor(R.color.common_bg_color_grey));
			findViewById(R.id.common_slider_bar).setBackgroundResource(R.color.common_bg_color_grey);
		} else {
//			findViewById(R.id.common_slider_bar).setBackgroundColor(getResources().getColor(R.color.day_viewpager_bg));
			findViewById(R.id.common_slider_bar).setBackgroundResource(R.color.day_viewpager_bg);
		}
	}

	/**
	 * 注意：width为dp
	 *
	 * @param width
	 */
	public void setTabLineWidth(int width) {
		mLineWidth = DensityUtil.dip2px(getContext(), width);
	}

	public int getCount() {
		if (mPager != null && mPager.getAdapter() != null) {
			return mPager.getAdapter().getCount();
		}
		return 0;
	}

	public void setViewPager(ViewPager pager) {
		mPager = pager;
		if (mPager != null) {
			mPager.setOnPageChangeListener(this);
			refreshSlider(0, 0);
		}
	}

	private void init() {
		inflate(getContext(), R.layout.common_tab_viewpager, this);
		topBar = findViewById(R.id.slider_bar_frame);
		mSliderBar = (LinearLayout) findViewById(R.id.common_slider_bar);
		mSliderLine = findViewById(R.id.common_slider_line);
		rightMenuIv = (ImageView)findViewById(R.id.common_tab_viewpager_right_menu_iv);
		mTitleViews = new ArrayList<View>();
		mIvBack = (LinearLayout) findViewById(R.id.back);
		ViewPager vp = (ViewPager) findViewById(R.id.common_viewpager);
		setViewPager(vp);
		final Context context = getContext();
		if (context instanceof Activity) {
			setOnBackListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					((Activity) context).finish();
				}
			});
		}
	}

	public void setOnBackListener(OnClickListener l) {
		//mImgBack.setOnClickListener(l);
		//mTitleArea.setOnClickListener(l);
		mIvBack.setOnClickListener(l);
	}

	public void setSelectedPage(int idx) {
		if (mTitleViews != null && idx < mTitleViews.size() && idx >= 0) {
			mPager.setCurrentItem(idx);
		}
	}
	/** 细的绿线 */
//    public View getSliderBottomLine() {
//        return mSliderBottomLine;
//    }

	/**
	 * 粗的绿线
	 */
	public View getSliderLine() {
		return mSliderLine;
	}

	public void hideSliderBar() {
//        mSliderBottomLine.setVisibility(View.GONE);
		mSliderLine.setVisibility(View.GONE);
		mSliderBar.setVisibility(View.GONE);
	}

	public void showSliderBar() {
//        mSliderBottomLine.setVisibility(View.VISIBLE);
		mSliderLine.setVisibility(View.VISIBLE);
		mSliderBar.setVisibility(View.VISIBLE);
	}

	public void setPageViews(List<View> views) {
		if (views == null) {
			return;
		}
		mPager.setAdapter(new DefaultViewPagerAdapter(views));
	}

	public void setPageViewsByResId(List<Integer> layoutResIds) {
		if (layoutResIds == null) {
			return;
		}
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < layoutResIds.size(); i++) {
			views.add(inflate(getContext(), layoutResIds.get(i), null));
		}
		setPageViews(views);
	}

	public void setTitleViewsByResId(List<Integer> layoutResIds) {
		if (layoutResIds == null) {
			return;
		}
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < layoutResIds.size(); i++) {
			views.add(inflate(getContext(), layoutResIds.get(i), null));
		}
		setTitleViews(views);
	}

	private void initTitleEvent() {
		if (mTitleViews == null) {
			return;
		}
		for (int i = 0; i < mTitleViews.size(); i++) {
			final int idx = i;
			mTitleViews.get(i).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mPager != null) {
						mPager.setCurrentItem(idx);
					}
				}
			});
		}
	}

	public void setTitleViews(List<View> views) {
		if (views == null) {
			return;
		}
		mTitleViews.clear();
		mSliderBar.removeAllViews();

		LayoutParams lpSB = (LayoutParams) mSliderBar.getLayoutParams();
		lpSB.height = LayoutParams.WRAP_CONTENT;
		mSliderBar.setLayoutParams(lpSB);
		LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT);
		//        lp.gravity = Gravity.CENTER;
		for (int i = 0; i < views.size(); i++) {
			View v = views.get(i);
			lp.weight = 1;
			v.setLayoutParams(lp);
			mSliderBar.addView(v);
			mTitleViews.add(v);
			v.setSelected(i == 0);
		}
		initTitleEvent();
		refreshSlider(0, 0);
		setTextView(mTitleViews);
	}

	public void setTitles(List<String> titles) {
		if (titles == null) {
			return;
		}
		mTitleViews.clear();
		mSliderBar.removeAllViews();
		LinearLayout.LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT , 1);
		for (int i = 0; i < titles.size(); i++) {
			TextView tv = new TextView(getContext());
			tv.setGravity(Gravity.CENTER);
			tv.setTextSize(17);
			tv.setTextColor(getResources().getColorStateList(R.drawable.common_tab_text_color));
			tv.setBackgroundResource(R.color.whitebg);
			tv.setText(titles.get(i));
			tv.setLayoutParams(lp);
			mSliderBar.addView(tv);
			mTitleViews.add(tv);
			tv.setSelected(i == 0);
		}
		initTitleEvent();
		refreshSlider(0, 0);
		setTextView(mTitleViews);
	}

	public List<View> getTitleViewList() {
		return mTitleViews;
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			int pos = 0;
			if (mPager != null) {
				pos = mPager.getCurrentItem();
			}
			refreshSlider(pos, 0);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			int pos = 0;
			if (mPager != null) {
				pos = mPager.getCurrentItem();
			}
			refreshSlider(pos, 0);
		}
	}

	private void refreshSlider(int pos, float offset) {
		if (getCount() > 0 && mTitleViews != null && mTitleViews.size() == getCount()) {
			final RelativeLayout.LayoutParams lpSlider = (RelativeLayout.LayoutParams) mSliderLine.getLayoutParams();
			lpSlider.width = mLineWidth;
			int tabWidth = mSliderBar.getWidth() / getCount();
			lpSlider.leftMargin = (int) (pos * tabWidth + tabWidth * offset + tabWidth / 2 - lpSlider.width / 2);
			postDelayed(new Runnable() {
				@Override
				public void run() {
					mSliderLine.setLayoutParams(lpSlider);
				}
			}, 30);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if (mOnPageChangedListener != null) {
			mOnPageChangedListener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		refreshSlider(position, positionOffset);
		if (mOnPageChangedListener != null) {
			mOnPageChangedListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageSelected(int position) {
		for (int i = 0; i < mTitleViews.size(); i++) {
			mTitleViews.get(i).setSelected(i == position);
		}
		if (mOnPageChangedListener != null) {
			mOnPageChangedListener.onPageSelected(position);
		}
	}

	private void setTextView(List<View> views) {
		for (int i = 0; i < views.size(); i++) {
			View view = views.get(i);
			if (view instanceof TextView) {
				if (mStyle == STYLE_GREY) {
					((TextView) view).setTextColor(getResources().getColorStateList(R.drawable.common_tab_text_color));
				} else {
					((TextView) view).setTextColor(getResources().getColor(R.color.white));
				}
			}
		}
	}

	class DefaultViewPagerAdapter extends PagerAdapter {
		private final List<View> mListViews;

		public DefaultViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	public void setRightMenu(View.OnClickListener listener){
		rightMenuIv.setVisibility(View.VISIBLE);
		rightMenuIv.setOnClickListener(listener);
	}

	public void hideRightMenu() {
		rightMenuIv.setVisibility(View.GONE);
	}

	public View getTopBar() {
		return topBar;
	}
}
