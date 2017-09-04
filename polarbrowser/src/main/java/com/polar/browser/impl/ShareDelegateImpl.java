package com.polar.browser.impl;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.i.IShareDelegate;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.ShareUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.ViewUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ShareDelegateImpl implements IShareDelegate {
	/** 分享结果回调 **/
	/*
	 * private PlatformActionListener paListener = new PlatformActionListener()
	 * {
	 * 
	 * @Override public void onError(Platform arg0, int arg1, Throwable arg2) {
	 * // 失败 if (arg0 != null &&
	 * "WechatClientNotExistException".equals(arg0.getClass().getSimpleName()))
	 * { toastInMainThread(R.string.wechat_client_inavailable); } else if (arg0
	 * != null &&
	 * "WechatTimelineNotSupportedException".equals(arg0.getClass().getSimpleName
	 * ())) { toastInMainThread(R.string.wechat_client_inavailable); } else if
	 * (arg0 != null && arg0.getClass().getSimpleName().startsWith("Wechat")) {
	 * toastInMainThread(R.string.wechat_client_inavailable); } else {
	 * toastInMainThread(R.string.share_fail); }
	 * 
	 * }
	 * 
	 * @Override public void onComplete(Platform arg0, int arg1, HashMap<String,
	 * Object> arg2) { if (arg0 != null &&
	 * "Email".equals(arg0.getClass().getSimpleName())) { return; } if (arg0 !=
	 * null && "ShortMessage".equals(arg0.getClass().getSimpleName())) { return;
	 * } if (arg0 != null &&
	 * "SinaWeibo".equals(arg0.getClass().getSimpleName())) { if
	 * (SysUtils.isPackageInstalled(JuziApp.getInstance(), "com.sina.weibo")) {
	 * return; } } toastInMainThread(R.string.share_success); }
	 * 
	 * @Override public void onCancel(Platform arg0, int arg1) {
	 * toastInMainThread(R.string.share_cancel); } };
	 * 
	 * private void toastInMainThread(final int stringRes){
	 * ThreadManager.postTaskToUIHandler(new Runnable() {
	 * 
	 * @Override public void run() {
	 * CustomToastUtil.getInstance().showDurationToast(stringRes); } }); }
	 */

	/**
	 * 要截屏的View
	 **/
	private View mShotView;
	private String mTitle;
	private String mTitleUrl;
	private String mText;
	private String mImagePath;

	public ShareDelegateImpl(View shotView) {
		this.mShotView = shotView;
		mTitle = shotView.getContext().getString(R.string.share_title);
		mTitleUrl = shotView.getContext().getString(R.string.official_website);
		mText = shotView.getContext().getString(R.string.share_content);
		// TODO 分享图片的路径
		mImagePath = getShareImageDirPath() + "share" + ".png";
	}

	private void setSuitableParam() {
		if (TabViewManager.getInstance().isCurrentHome()) {
			// 主页
			mImagePath = getShareImageDirPath() + "share_home" + ".png";
			mTitle = JuziApp.getInstance().getString(R.string.share_title);
			;
			mTitleUrl = JuziApp.getInstance().getString(
					R.string.official_website);
			mText = JuziApp.getInstance().getText(R.string.share_content)
					.toString()
					+ " --"
					+ JuziApp.getInstance().getText(R.string.share_from)
					.toString() + mTitleUrl;
		} else {
			// 非主页
			mImagePath = getShareImageDirPath() + "share" + ".png";
			mTitle = TabViewManager.getInstance().getCurrentTitle();
			mTitleUrl = TabViewManager.getInstance().getCurrentUrl();
			if (!TextUtils.isEmpty(mTitle) && !TextUtils.isEmpty(mTitleUrl)) {
				mText = mTitle
						+ " --"
						+ JuziApp.getInstance().getText(R.string.share_from)
						.toString() + " " + mTitleUrl;
			} else {
				mText = JuziApp.getInstance().getText(R.string.share_content)
						.toString()
						+ " --"
						+ JuziApp.getInstance().getText(R.string.share_from)
						.toString() + " " + mTitleUrl;
			}
		}
	}

	@Override
	public void share2FaceBook() {
		setSuitableParam();
		screenShot(mShotView);
		ShareUtils.share(JuziApp.getAppContext(), "com.facebook.katana", mText, mImagePath);
	}

	@Override
	public void share2Twitter() {
		setSuitableParam();
		screenShot(mShotView);
		ShareUtils.share(JuziApp.getAppContext(), "com.twitter.android", mText, mImagePath);
	}

	@Override
	public void share2Whatsapp() {
		setSuitableParam();
		screenShot(mShotView);
		ShareUtils.share(JuziApp.getAppContext(), "com.whatsapp", mText, mImagePath);
	}

	@Override
	public void share2ShortMessage() {
		setSuitableParam();
		screenShot(mShotView);
		// ShareUtils.share2ShortMessage(mParams, paListener);
		Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("smsto:"));
		// intent.setType("text/plain"); //纯文本
		// 图片分享
		intent.setType("image/png");
		// 添加图片
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mImagePath))); // 添加图片
//		intent.putExtra(Intent.EXTRA_SUBJECT, mTitle);
//		intent.putExtra(Intent.EXTRA_TEXT, mTitle);
		intent.putExtra("sms_body", mText);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// activityInfo.name = com.android.mms.ui.ComposeMessageMms
		// activityInfo.packageName = com.android.mms
		String packageName = null;
		String activityName = null;
		List<ResolveInfo> list = JuziApp.getInstance().getPackageManager()
				.queryIntentActivities(intent, 0);
		for (int i = 0; i < list.size(); i++) {
			packageName = list.get(i).activityInfo.packageName;
			if (TextUtils.equals(packageName, "com.android.mms")) {
				activityName = list.get(i).activityInfo.name;
				break;
			}
		}
		if (packageName != null && activityName != null) {
			intent.setClassName(packageName, activityName);
		}
		JuziApp.getInstance().startActivity(intent);
	}

	@Override
	public void share2Email() {
		setSuitableParam();
		screenShot(mShotView);
		// ShareUtils.share2Email(mParams, paListener);
		Intent it = new Intent(Intent.ACTION_SEND);
		String theme = mText;
		it.putExtra(Intent.EXTRA_SUBJECT, theme);
		it.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mImagePath)));
		it.setType("plain/text");
		// it.setType("plain/text");邮件
		// it.setType("application/octet-stream");邮件+蓝牙+其它
		// intent.setType("text/html"):邮件+蓝牙
		// setType("text/plain")：邮件+蓝牙+其它
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try { // Fix bug #3484
			JuziApp.getInstance().startActivity(it);
		} catch (Exception e) {
			SimpleLog.e(e);
			CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
		}
	}

	@Override
	public void copyLink() {
		// 得到剪贴板管理器
		android.content.ClipboardManager cmb = (android.content.ClipboardManager) JuziApp
				.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
		// if (mIsAbout) {
		// cmb.setPrimaryClip(ClipData.newPlainText(null, JuziApp
		// .getInstance().getText(R.string.share_introduction_url)
		// .toString()));
		// return;
		// }
		String url = TabViewManager.getInstance().getCurrentUrl();
		if (!TextUtils.isEmpty(url)) {
			cmb.setPrimaryClip(ClipData.newPlainText(null, url));
		} else {
			cmb.setPrimaryClip(ClipData.newPlainText(null, JuziApp
					.getInstance().getString(R.string.official_website)));
		}
	}

	@Override
	public void systemShare() {
		setSuitableParam();
		screenShot(mShotView);
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, mText);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mImagePath)));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setType("image/*");
		try { // Fix bug #3484
			JuziApp.getInstance().startActivity(intent);
		} catch (Exception e) {
			CustomToastUtils.getInstance().showTextToast(R.string.share_fail);
		}
	}

	private boolean screenShot(View view) {
		Bitmap bitmap = ViewUtils.getMagicDrawingCache(view);
		if (bitmap != null) {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(mImagePath);
				bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
				return true;
			} catch (Exception e) {
				SimpleLog.e(e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			// System.out.println("bitmap is NULL!");
		}
		return false;
	}

	private String getShareImageDirPath() {
		return VCStoragerManager.getInstance().getImageDirPath();
	}
}
