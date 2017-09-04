package com.polar.browser.cropedit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CommonUtils;

import org.sprite2d.apps.pp.PainterCanvas.ISaveComplete;

import java.io.File;

public class CropShareView extends RelativeLayout implements View.OnClickListener {

	private View mBtnSave;
	private TextView mTvSave;
	private TextView mTvSaveDes;

	public CropShareView(Context context) {
		this(context, null);
	}

	public CropShareView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_crop_share, this);
		initView();
	}

	private void initView() {
		mBtnSave = findViewById(R.id.btn_save);
		mTvSave = (TextView) findViewById(R.id.tv_save);
		mTvSaveDes = (TextView) findViewById(R.id.tv_save_des);
		mBtnSave.setOnClickListener(this);
		findViewById(R.id.btn_share_back).setOnClickListener(this);
		findViewById(R.id.btn_share_facebook).setOnClickListener(this);
		findViewById(R.id.btn_share_twitter).setOnClickListener(this);
		findViewById(R.id.btn_share_whatsapp).setOnClickListener(this);
	}

	/**
	 * 显示分享页面
	 */
	public void show() {
		setVisibility(VISIBLE);
		ShareUtil.getInstance().init();
		// 打开分享页，先截图
		if (getContext() instanceof CropEditActivity) {
			((CropEditActivity) getContext()).saveBitmap(null, null);
		}
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		CropEditActivity.sSavedPic = true;
		switch (v.getId()) {
			case R.id.btn_share_back:
				hide();
				break;
			case R.id.btn_save:
				handleBtnSaveClick();
				Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_SAVE);
				break;
			case R.id.btn_share_facebook:
				ShareUtil.getInstance().share2Facebook(getContext());
				Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_SHARE, "facebook");
				break;
			case R.id.btn_share_twitter:
				ShareUtil.getInstance().share2Twitter(getContext());
				Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_SHARE, "twitter");
				break;
			case R.id.btn_share_whatsapp:
				Statistics.sendOnceStatistics(GoogleConfigDefine.SCREENSHOTS_BRUSH, GoogleConfigDefine.SCREENSHOTS_BRUSH_TYPE_SHARE, "whatsapp");
				ShareUtil.getInstance().share2Whatsapp(getContext());
				break;
			default:
				break;
		}
	}

	public void hide() {
		setVisibility(GONE);
		mTvSaveDes.setText("");
		mTvSaveDes.setVisibility(View.INVISIBLE);
		mTvSave.setText(R.string.save_into_album);
		mTvSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_album, 0, 0, 0);
		deleteShareFile();
		mBtnSave.setEnabled(true);
	}

	private void deleteShareFile() {
		ThreadManager.postTaskToLogicHandler(new Runnable() {
			@Override
			public void run() {
				// 删除预备分享的图
				String path = VCStoragerManager.getInstance().getImageDirPath() + ShareUtil.SHARE_SHOT_IMG;
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			}
		});
	}

	private void handleBtnSaveClick() {
		final String path = VCStoragerManager.getInstance().getImageDirPath() + System.currentTimeMillis() + ".png";
		if (getContext() instanceof CropEditActivity) {
			((CropEditActivity) getContext()).saveBitmap(path, new ISaveComplete() {
				@Override
				public void complete() {
					mTvSaveDes.setText(path);
					mTvSaveDes.setVisibility(View.VISIBLE);
					mTvSave.setText(R.string.saved);
					mTvSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_crop_ok, 0, 0, 0);
				}
			});
			mBtnSave.setEnabled(false);
		}
	}
}
