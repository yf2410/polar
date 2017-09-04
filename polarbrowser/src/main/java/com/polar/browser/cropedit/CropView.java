package com.polar.browser.cropedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.edmodo.cropper.CropImageView;
import com.polar.browser.R;
import com.polar.browser.utils.CommonUtils;

/**
 * 用于处理截屏
 *
 * @author dpk
 */
public class CropView extends RelativeLayout implements OnClickListener {

	private CropImageView mCropImageView;

	private View mBtnOk;
	private View mBtnCancel;
	private IFinishedCrop mListener;

	public CropView(Context context) {
		this(context, null);
	}

	public CropView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(getContext()).inflate(R.layout.view_crop, this);
		initView();
	}

	public void setImageBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			mCropImageView.setImageBitmap(bitmap);
		}
	}

	public void registerListener(IFinishedCrop listener) {
		mListener = listener;
	}

	private void initView() {
		mCropImageView = (CropImageView) findViewById(R.id.cropview);
		mBtnOk = findViewById(R.id.iv_ok);
		mBtnCancel = findViewById(R.id.iv_cancel);
		mBtnOk.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (CommonUtils.isFastDoubleClick()) {
			return;
		}
		switch (v.getId()) {
			case R.id.iv_ok:
				if (mListener != null) {
					mListener.finishedCrop(mCropImageView.getCroppedImage());
				}
				CropEditActivity.sSavedPic = false;
				break;
			case R.id.iv_cancel:
				if (mListener != null) {
					mListener.cancelCrop();
				}
				break;
			default:
				break;
		}
	}
}
