package com.polar.browser.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.activity.BrowserActivity;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.api.RequestAPI;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SysUtils;
import com.polar.browser.vclibrary.bean.base.Result;
import com.polar.browser.vclibrary.network.ResultCallback;
import com.polar.browser.vclibrary.network.api.Api;
import com.polar.browser.vclibrary.util.AdapterConvertor;
import com.polar.browser.vclibrary.util.MobilePerformanceUtil;

import retrofit2.Call;
import retrofit2.Response;

public class FeedBackActivity extends LemonBaseActivity implements OnClickListener {

	private EditText mEtFeedBack;
	private EditText mEtContactinfomation;
	private CommonTitleBar mTitleBar;
	private TextView mBtnSend;
	private String mTotalMemory;
	private String mProcessCpuRate;
	private String mScreenSize;
	private String mNetworkType;
	private String mAvailMemory;
	private String mAppUserMemory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		initView();
		initListener();

		ThreadManager.postTaskToIOHandler(new Runnable() {
			@Override
			public void run() {
				//手机性能参数获取
				mTotalMemory = SysUtils.getTotalMemory(JuziApp.getAppContext());
				mProcessCpuRate = MobilePerformanceUtil.getProcessCpuRate();
				mScreenSize = AppEnv.SCREEN_WIDTH + "*" + AppEnv.SCREEN_HEIGHT;
				mNetworkType = AdapterConvertor.getNetworkType(JuziApp.getAppContext());
				mAvailMemory = MobilePerformanceUtil.getAvailMemory(JuziApp.getAppContext());
				mAppUserMemory = MobilePerformanceUtil.appUserMemory();
			}
		});
	}

	private void initView() {
		mEtFeedBack = (EditText) findViewById(R.id.et_feedback);
		mEtContactinfomation = (EditText) findViewById(R.id.et_contactinfomation);
		mTitleBar = (CommonTitleBar) findViewById(R.id.title_bar);
		mTitleBar.setSettingTxt(R.string.feedback_send);
		mTitleBar.setSettingVisible(true);
//		mTitleBar.setOnButtonListener(this);
		mTitleBar.findViewById(R.id.common_tv_setting).setOnClickListener(this);
		mBtnSend = (TextView) mTitleBar.findViewById(R.id.common_tv_setting);
		mBtnSend.setEnabled(false);
		findViewById(R.id.feedback_whatsapp_img).setOnClickListener(this);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	private void initListener() {
		mEtFeedBack.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void afterTextChanged(Editable edit) {
				if (TextUtils.isEmpty(edit.toString())) {
					mBtnSend.setEnabled(false);
				} else {
					mBtnSend.setEnabled(true);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		switch (v.getId()) {
			case R.id.common_tv_setting:
				if (!NetWorkUtils.isNetworkConnected(this)) {
					CustomToastUtils.getInstance().showTextToast(getString(R.string.feed_back_failed));
				} else {
					hanleSubmit();
				}
				break;
			case R.id.common_img_back:
				onBackPressed();
				break;
			case R.id.feedback_whatsapp_img:
				Intent intent = new Intent(this, BrowserActivity.class);
				intent.setAction(CommonData.ACTION_OPEN_PRODUCT_ABOUT);
				intent.putExtra(CommonData.SYSTEM_CONTENT_URL, RequestAPI.LOAD_FEEDBACK_WHATSAPP);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
				break;
			default:
				break;
		}
	}

	private void hanleSubmit() {
		final SubmitFeedBackDialog submitFeedBackDialog = new SubmitFeedBackDialog(this,R.string.feed_back_commit,R.drawable.login_loading);
		submitFeedBackDialog.show();
		String feedback = mEtFeedBack.getText().toString();
		String infomation = mEtContactinfomation.getText().toString();
		// TODO 上传
		Api.getInstance().userFeedBack(feedback, infomation,
				mTotalMemory, mProcessCpuRate, mScreenSize, mNetworkType,
				mAvailMemory, mAppUserMemory).enqueue(new ResultCallback<String>() {
			@Override
			public void success(String data, Call<Result<String>> call, Response<Result<String>> response) throws Exception {
				submitFeedBackDialog.dismiss();
				CustomToastUtils.getInstance().showDurationToast(R.string.feedback_ok, 2000);
				onBackPressed();
			}

			@Override
			public void error(Call<Result<String>> call, Throwable t) {
				submitFeedBackDialog.dismiss();
				CustomToastUtils.getInstance().showTextToast(getString(R.string.feed_back_failed));
			}
		});
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slid_out_to_right);
	}
}
