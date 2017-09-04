package com.polar.browser.tabview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;


/**
 * ContentFrame，作为ContentView的容器
 *
 * @author duanpeikun
 */
public class ContentFrame extends RelativeLayout {

	public ContentFrame(Context context) {
		super(context);
	}

	public ContentFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void addContentView(ContentView view) {
//		getViewTreeObserver().addOnGlobalLayoutListener(mOGLListener);
		addView(view.getView(), 0);
	}

	public void removeAllView() {
		if (getChildCount() > 0) {
			removeViewAt(0);
		}
	}
/*
	private boolean isKeyboardShown;
	private int lastTopMargin;
	
	private OnGlobalLayoutListener mOGLListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			SimpleLog.e("onGlobalLayout", "---------------------onGlobalLayout");
			if (!isShown()) {
				return;
			}
			// 获取root在窗体的可视区域
			Rect rect = new Rect();
			getWindowVisibleDisplayFrame(rect);
			// 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度-->键盘高度)
			int rootInvisibleHeight = getRootView().getHeight() - rect.bottom;
			// 若不可视区域高度大于100
			if (rootInvisibleHeight > 100 && !isKeyboardShown) {
				// fix bug 
				SimpleLog.e("OnGlobalLayoutListener", "==Shown");
				if (getChildCount() > 0) {
					ViewGroup vg = (ViewGroup) getChildAt(0);
					View webView = vg.getChildAt(0);
					LayoutParams params = (LayoutParams) webView.getLayoutParams();
					int h = vg.getMeasuredHeight();
					// 40dp: toobarbottom view height
					params.height = h - rootInvisibleHeight + DensityUtil.dip2px(getContext(), 40);
					if (params.height > 100) {
						webView.setLayoutParams(params);
						isKeyboardShown = true;
					}
				}
			} else if (rootInvisibleHeight < 100 && isKeyboardShown) {
				// 键盘隐藏
				SimpleLog.e("OnGlobalLayoutListener", "==hide");
				if (getChildCount() > 0) {
					ViewGroup vg = (ViewGroup) getChildAt(0);
					View webView = vg.getChildAt(0);
					LayoutParams params = (LayoutParams) webView.getLayoutParams();
					params.height = LayoutParams.MATCH_PARENT;
					webView.setLayoutParams(params);
					isKeyboardShown = false;
				}
			}
			
			LayoutParams rootParam = (LayoutParams) getLayoutParams();
			int top = rootParam.topMargin;
			
			SimpleLog.e("OnGlobalLayoutListener", "--------rootParam.topMargin == " + top);
			
			if (top != lastTopMargin) {
				
				if (rootInvisibleHeight > 100 && isKeyboardShown) {
					// fix bug QuickInput隐藏后，横竖屏切换 闪
					SimpleLog.e("OnGlobalLayoutListener", "==Shown");
					if (getChildCount() > 0) {
						ViewGroup vg = (ViewGroup) getChildAt(0);
						View webView = vg.getChildAt(0);
						LayoutParams params = (LayoutParams) webView.getLayoutParams();
						int h = vg.getMeasuredHeight();
						// 40dp: toobarbottom view height
						params.height = h - rootInvisibleHeight + DensityUtil.dip2px(getContext(), 40);
						if (params.height > 100) {
							webView.setLayoutParams(params);
							isKeyboardShown = true;
						}
					}
				} 
				
			}
			
			lastTopMargin = top;
			
		}
	};
	
	/*
	public void reLayout(){
		Rect rect = new Rect();
		getWindowVisibleDisplayFrame(rect);
		// 获取root在窗体的不可视区域高度(被其他View遮挡的区域高度-->键盘高度)
		int rootInvisibleHeight = getRootView().getHeight() - rect.bottom;
		if (rootInvisibleHeight > 100 && isKeyboardShown) {
			// fix bug QuickInput隐藏后，横竖屏切换 闪
			SimpleLog.e("OnGlobalLayoutListener", "==Shown");
			if (getChildCount() > 0) {
				ViewGroup vg = (ViewGroup) getChildAt(0);
				View webView = vg.getChildAt(0);
				LayoutParams params = (LayoutParams) webView.getLayoutParams();
				int h = vg.getMeasuredHeight();
				// 40dp: toobarbottom view height
				params.height = h - rootInvisibleHeight + DensityUtil.dip2px(getContext(), 40);
				if (params.height > 100) {
					webView.setLayoutParams(params);
					isKeyboardShown = true;
				}
			}
		} 
	}*/
}
