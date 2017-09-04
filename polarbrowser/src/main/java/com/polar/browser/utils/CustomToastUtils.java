package com.polar.browser.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.polar.browser.JuziApp;
import com.polar.browser.R;

import java.io.Serializable;

/**
 * 该类基于CustomToastUtil作修改
 * 解决【主菜单】无图模式、电脑桌面模式时，取消toust状态屏蔽操作
 */
public class CustomToastUtils {
	private static final int DURATION = 1000;
	private static CustomToastUtils sInstance;
	private static Toast toast;
	private View mView;
	private TextView mTextView, tipTextView;
	private TextView mClickTextView;
	private ImageView mImageView;
	private LinearLayout clickTip;
	private static final String TAG = CustomToastUtils.class.getSimpleName();
	private static final String ARGUMENT_EXCEPTION = TAG + " illeage argument exception.";

	private CustomToastUtils() {
		initLayout();
	}

	public static CustomToastUtils getInstance() {
		if (null == sInstance) {
			synchronized (CustomToastUtils.class) {
				if (null == sInstance) {
					sInstance = new CustomToastUtils();
				}
			}
		}
		return sInstance;
	}

	private void initLayout() {
		mView = View.inflate(JuziApp.getInstance(), R.layout.view_toast, null);
		clickTip = (LinearLayout) mView.findViewById(R.id.ll_tip);
		mTextView = (TextView) mView.findViewById(R.id.text);
		mClickTextView = (TextView) mView.findViewById(R.id.tv_click);
		mImageView = (ImageView) mView.findViewById(R.id.icon);
		tipTextView = (TextView) mView.findViewById(R.id.text_tip);
	}

	private void new_Toast() {
		if(toast == null) {
			toast = new Toast(JuziApp.getInstance());
		}
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(mView);
		toast.show();
	}

	public void showImgToast(int stringResId, int imgResId)
			throws Resources.NotFoundException {
		showImgToast(JuziApp.getInstance().getResources().getText(stringResId), imgResId);
	}

	private void showImgToast(CharSequence text, int imgResId) {
		new_Toast();
		mTextView.setPadding(UIUtils.dip2px(JuziApp.getInstance(), 24), 0, UIUtils.dip2px(JuziApp.getInstance(), 24), 0);
		try {
			clickTip.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
			mImageView.setVisibility(View.VISIBLE);
			mImageView.setImageResource(imgResId);
			mTextView.setText(text);
		} catch (Exception e) {
		}
	}

	public void showTextToast(CharSequence text) {
		try {
			new_Toast();
			mImageView.setVisibility(View.GONE);
			clickTip.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(text);
		} catch (Exception e) {
		}
	}

	public void showTextToast(int resId)
			throws Resources.NotFoundException{
		showTextToast(JuziApp.getInstance().getResources().getText(resId));
	}

	public void showDurationToast(int resId, int duration)
			throws Resources.NotFoundException {
		showDurationToast(JuziApp.getInstance().getResources().getText(resId), duration);
	}

	public void showDurationToast(CharSequence text, int duration) {
		try {
			new_Toast();

			toast.setDuration(duration>Toast.LENGTH_SHORT?Toast.LENGTH_LONG:Toast.LENGTH_SHORT);
			mImageView.setVisibility(View.GONE);
			clickTip.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(text);
		} catch (Exception e) {
		}
	}

	public void showClickToast(Activity  activity , int stringResId, View.OnClickListener listener) {
		showClickToast(activity , JuziApp.getInstance().getResources().getText(stringResId),JuziApp.getAppContext().getString(R.string.click_to_see),DURATION * 3,listener,false);
	}

	public void showArrowClickToast(Activity activity, String str,String clickStr, View.OnClickListener listener) {
		showClickToast(activity , str,clickStr,DURATION * 4,listener,true);
	}

	public void showClickToast(Activity activity,CharSequence text, String clickText, int duration, final View.OnClickListener listener){
		showClickToast(activity,text,clickText,duration,listener,false);
	}

	private void showClickToast(Activity activity , CharSequence text, String clickText, int duration, final View.OnClickListener listener,boolean showArrow) {
		if (text == null || clickText == null ||
				text.length() == 0 || clickText.length() == 0 || listener == null) {
			throw new IllegalArgumentException(ARGUMENT_EXCEPTION);
		}

		if (duration < ToastDialog.DEFAULT_SHOW_TIME_SHORT) {
			duration = ToastDialog.DEFAULT_SHOW_TIME_SHORT;
		}
		ToastDialog toastDialog = new ToastDialog(activity);
		toastDialog.setMessage(text.toString());
		toastDialog.setClickMessage(clickText);
		toastDialog.setShowTime(duration);
		toastDialog.setShowArrow(showArrow);
		toastDialog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onClick(v);
			}
		});
		if(toastDialog.isShowing()) return;
		toastDialog.show();
	}

	public static final String TOAST_MAIN_MESSAGE = "toast_main_message"; //主信息
	public static final String TOAST_CLICK_MESSAGE = "toast_click_message"; //可点击的信息
	public static final String TOAST_CLICK_LISTENER = "toast_click_listener"; //点击监听
	public static final String TOAST_SHOW_TIME = "toast_show_time";
	public static final String TOAST_IS_SHOW_BOTTOM_ARROW = "toast_is_show_bottom_arrow";
	public static final String TOAST_FILE_PATH = "toast_file_path";

	public void showClickActivityToast(Context context, CharSequence text, String clickText, int duration, int eventCode, boolean showArrow) {
		Intent intent = new Intent(context,CustomToastActivity.class);
		intent.putExtra(TOAST_MAIN_MESSAGE,text.toString());
		intent.putExtra(TOAST_CLICK_MESSAGE,clickText);
		intent.putExtra(TOAST_SHOW_TIME,duration);
		intent.putExtra(TOAST_IS_SHOW_BOTTOM_ARROW,showArrow);
		//add click listener
		intent.putExtra(TOAST_CLICK_LISTENER,eventCode);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public void showClickActivityToast(Context context, CharSequence text, String clickText, int duration, int eventCode, String filePath, boolean showArrow) {
		Intent intent = new Intent(context,CustomToastActivity.class);
		intent.putExtra(TOAST_MAIN_MESSAGE,text.toString());
		intent.putExtra(TOAST_CLICK_MESSAGE,clickText);
		intent.putExtra(TOAST_SHOW_TIME,duration);
		intent.putExtra(TOAST_IS_SHOW_BOTTOM_ARROW,showArrow);
		intent.putExtra(TOAST_FILE_PATH,filePath);
		//add click listener
		intent.putExtra(TOAST_CLICK_LISTENER,eventCode);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
