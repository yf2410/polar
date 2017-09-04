package com.polar.browser.setting;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;

import java.util.ArrayList;
import java.util.List;

public class SetDefaultBrowserActivity extends LemonBaseActivity implements OnClickListener {

	private static String TAG = "SetDefaultBrowserActivity";

	private View mViewSet;
	private View mViewClear;

	private View mViewClearSelf;
//	private ImageView mIvSet;
//	private ImageView mIvClear;

	private TextView mBtnSet;
	private TextView mBtnClear;

	private TextView mStep1;
	private TextView mStep2;

	/**
	 * 是否是清除默认设置
	 **/
	private boolean isToClear;

	/**
	 * 是否是设置默认设置
	 **/
	private boolean isToSetDefault;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_default_browser);
		initView();
	}

	private void initView() {
//		isMiUI = SysUtils.isMiuiRom(this);
		mViewSet = findViewById(R.id.rl_set_default);
		mViewClear = findViewById(R.id.rl_clear_default);
		mViewClearSelf = findViewById(R.id.rl_clear_default_self);
//		mIvSet = (ImageView) findViewById(R.id.iv_set_default);
//		mIvClear = (ImageView) findViewById(R.id.iv_clear_default);
		mStep1 = (TextView) findViewById(R.id.step1);
		mStep2 = (TextView) findViewById(R.id.step2);
//		if (isMiUI) {
////			mIvSet.setImageResource(R.drawable.set_default_miui);
//			mIvClear.setImageResource(R.drawable.clear_default);
//			
//			mStep1.setText(R.string.set_default_step1);
//			mStep2.setText(R.string.set_default_step2);
//			
//		} else {
//			mIvSet.setImageResource(R.drawable.set_default2);
//			mIvClear.setImageResource(R.drawable.clear_default2);
		mStep1.setText(R.string.set_default_step1);
		mStep2.setText(R.string.set_default_step2);
//		}
		mBtnSet = (TextView) findViewById(R.id.btn_set);
		mBtnClear = (TextView) findViewById(R.id.btn_clear);
		mBtnSet.setOnClickListener(this);
		mBtnClear.setOnClickListener(this);
		findViewById(R.id.btn_clear_self).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshUI();
	}

	private void refreshUI() {
		ResolveInfo info = SysUtils.getDefaultBrowser(this.getApplicationContext());
		if (info != null) {
			String packageName = info.activityInfo.packageName;// com.android.browser
			if (TextUtils.equals("android", packageName)
					|| (TextUtils.equals("com.android.browser", packageName) && !isInDefaultList(packageName))) {
				// 没有设置默认
				mViewSet.setVisibility(View.VISIBLE);
				mViewClear.setVisibility(View.GONE);
				mViewClearSelf.setVisibility(View.GONE);
				if (isToClear) {
					isToClear = false;
					CustomToastUtils.getInstance().showTextToast(getString(R.string.setting_clear_default_success));
				}
				if (isToSetDefault) {
					isToSetDefault = false;
					CustomToastUtils.getInstance().showTextToast(getString(R.string.setting_default_failed));
				}
				SimpleLog.e(TAG, "is not  default !!");
			} else if (TextUtils.equals(getPackageName(), packageName)) {
				mViewSet.setVisibility(View.GONE);
				mViewClear.setVisibility(View.GONE);
				mViewClearSelf.setVisibility(View.VISIBLE);
				SimpleLog.e(TAG, "momeng is default !!");
			} else {
				mViewSet.setVisibility(View.GONE);
				mViewClearSelf.setVisibility(View.GONE);
				mViewClear.setVisibility(View.VISIBLE);
				if (isToClear) {
					isToClear = false;
					CustomToastUtils.getInstance().showTextToast(getString(R.string.setting_clear_default_failed));
				}
				if (isToSetDefault) {
					isToSetDefault = false;
					CustomToastUtils.getInstance().showTextToast(getString(R.string.setting_default_failed));
				}
				SimpleLog.e(TAG, packageName + " is default !!");
			}
		}
	}


	/**
	 * 红米手机，判断是否设置了默认浏览器，没有设置默认浏览器 && 设置了默认浏览器 都会是 com.android.browser。。。
	 * 所以添加一个判断，看com.android.browser是否在系统的 所有默认程序列表中
	 *
	 * @return
	 */
	private boolean isInDefaultList(String packageName) {
		PackageManager pm = getPackageManager();
		// Get list of preferred activities
		List<ComponentName> prefActList = new ArrayList<ComponentName>();
		// Intent list cannot be null. so pass empty list
		List<IntentFilter> intentList = new ArrayList<IntentFilter>();
		pm.getPreferredActivities(intentList, prefActList, packageName);
		for (int i = 0; i < prefActList.size(); i++) {
			ComponentName pi = prefActList.get(i);
			SimpleLog.e(TAG, "default  ComponentName " + i + " == " + pi.toString());
			if (TextUtils.equals("com.android.browser", pi.getPackageName())) {
				// 在默认列表中
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		switch (v.getId()) {
			case R.id.btn_set:
				setDefault();
				break;
			case R.id.btn_clear:
				clearDefault();
				break;
			case R.id.btn_clear_self:
				clearSelfDefault();
				break;
			default:
				break;
		}
	}

	private void clearSelfDefault() {
		final CommonDialog dialog = new CommonDialog(this, R.string.tips,
				R.string.setting_default_self_tip);
		dialog.setBtnCancel(getString(R.string.ok),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						// 清除
						PackageManager pm = getPackageManager();
						pm.clearPackagePreferredActivities(getPackageName());
						refreshUI();
					}
				});
		dialog.setBtnOk(getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private void clearDefault() {
		isToClear = true;
		Intent appItent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
		String pkg = "com.android.settings";
		String cls = "com.android.settings.applications.InstalledAppDetails";
		appItent.setComponent(new ComponentName(pkg, cls));
		String packageName = getPackageName();
		ResolveInfo info = SysUtils.getDefaultBrowser(this.getApplicationContext());
		if (info != null) {
			packageName = info.activityInfo.packageName;
		}
		appItent.setData(Uri.parse("package:" + packageName));
		try {
			startActivity(appItent);
		} catch (Exception e) {
			SimpleLog.e(e);
			CustomToastUtils.getInstance().showTextToast(getString(R.string.setting_clear_default_failed));
		}
		SimpleLog.e(TAG, "clear default !!");
	}

	private void setDefault() {
		isToSetDefault = true;
		// 设置默认
		Intent intent2 = new Intent();
		intent2.setAction(Intent.ACTION_VIEW);
		intent2.addCategory(Intent.CATEGORY_BROWSABLE);
//		intent2.setData(Uri.parse("http://momeng.cool/"));
		intent2.setData(Uri.parse(JuziApp.getInstance().getString(R.string.default_browser_website)));
		ComponentName name = new ComponentName("android", "com.android.internal.app.ResolverActivity");
		intent2.setComponent(name);
		try {
			startActivity(intent2);
		} catch (Exception e) { // 设置默认遇到异常，弹失败toast
			CustomToastUtils.getInstance().showTextToast(getString(R.string.setting_default_failed));
		}
		SimpleLog.e(TAG, "set default ~");
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slid_out_to_right);
	}
}
