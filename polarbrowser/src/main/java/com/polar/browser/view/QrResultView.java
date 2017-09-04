package com.polar.browser.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.i.IQrResult;

public class QrResultView extends RelativeLayout implements View.OnClickListener{

	private TextView mTVResult;
	
	private TextView mTVResultInvisible;
	
	
	private String mResult;
	
	private IQrResult mResultDelegate;
	
	public QrResultView(Context context) {
		this(context,null);
	}

	public QrResultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		inflate(getContext(), R.layout.view_qr_result, this);
		mTVResult = (TextView) findViewById(R.id.result_text);
		mTVResultInvisible = (TextView) findViewById(R.id.result_text_invisible);
//		mTVResult.setTextIsSelectable(true);
		findViewById(R.id.btn_search).setOnClickListener(this);
		findViewById(R.id.btn_copy).setOnClickListener(this);
		findViewById(R.id.btn_return).setOnClickListener(this);
	}

	public void setResultDelegate(IQrResult resultDelegate){
		mResultDelegate = resultDelegate;
	}
	
	/**
	 * 显示文本结果
	 * @param result 结果
	 */
	public void showTextResult(String result, Bitmap bitmap){
		this.mResult = result;
		if (mResult != null) {
			mTVResult.setText(mResult);
			mTVResultInvisible.setText(mResult);
			postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mTVResultInvisible.getHeight() > mTVResult.getHeight()) {
						findViewById(R.id.text_more).setVisibility(View.VISIBLE);
					} else {
						findViewById(R.id.text_more).setVisibility(View.GONE);
					}
				}
			}, 200);
		}
//		this.setBackground(new BitmapDrawable(getResources(), bitmap));
		setVisibility(VISIBLE);
	}
	
	/**
	 * 隐藏文本结果页面
	 */
	public void hide(){
		setVisibility(GONE);
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btn_search:
			if (mResultDelegate != null) {
				mResultDelegate.search(mResult);
			}
			break;
			
		case R.id.btn_copy:
			if (mResultDelegate != null) {
				mResultDelegate.copy(mResult);
			}
			break;
			
		case R.id.btn_return:
			if (mResultDelegate != null) {
				mResultDelegate.back();
			}
			break;
			
		default:
			break;
			
		}
		
	}
	
}
