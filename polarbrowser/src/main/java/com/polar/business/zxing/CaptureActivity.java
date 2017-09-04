package com.polar.business.zxing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.common.data.NavigateSource;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.i.IQrResult;
import com.polar.browser.manager.TabViewManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SearchUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.view.LoggingDialog;
import com.polar.browser.view.QrResultView;
import com.polar.business.zxing.camera.CameraManager;
import com.polar.business.zxing.decoding.CaptureActivityHandler;
import com.polar.business.zxing.decoding.InactivityTimer;
import com.polar.business.zxing.decoding.Utils;
import com.polar.business.zxing.util.ZXingUtil;
import com.polar.business.zxing.view.ViewfinderView;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

/**
 * 二维码扫一扫
 * 
 */
public class CaptureActivity extends Activity implements Callback {


	private static final int REQUEST_CODE = 234;
	private static final String TAG = "CaptureActivity";
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
//	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private String photo_path;

	private Bitmap mBitmap;
	
	private SurfaceHolder mSurfaceHolder;
	
	private boolean isAnimating;
	
	private QrResultView mTextResultView;
	private IQrResult mQrTextResultDelegate;
	
	/** 短信Intent前缀 **/
	private static final String SMS_PREFIX = "smsto:";
	private LoggingDialog mProcessDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_qrcode);
		
		openQrAnim();
		
		// 初始化 CameraManager
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.mo_scanner_viewfinder_view);

		initView();

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		
	}
	
	private void initListener() {
		findViewById(R.id.mo_scanner_back).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
		findViewById(R.id.mo_scanner_photo).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(CommonUtils.isFastDoubleClick()){
							return;
						}
						photo();
					}
				});

		findViewById(R.id.mo_scanner_light).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(CommonUtils.isFastDoubleClick()){
							return;
						}
						// 闪光灯
						light();
					}
				});
	}

	private void initView() {
		
		mTextResultView = (QrResultView) findViewById(R.id.text_result_view);
		mQrTextResultDelegate = new IQrResult() {
			@Override
			public void search(String result) {
				TabViewManager.getInstance().loadUrl(buildSearchUrl(result, JuziApp.getAppContext()), NavigateSource.Other);
				Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.SEATHC_TEXT_RESULT);
				finish();
			}
			@Override
			public void copy(String result) {
				// 得到剪贴板管理器
				android.content.ClipboardManager cmb = (android.content.ClipboardManager) JuziApp
						.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
				if (!TextUtils.isEmpty(result)) {
					cmb.setPrimaryClip(ClipData.newPlainText(null, result));
					ThreadManager.postTaskToUIHandler(new Runnable() {
						@Override
						public void run() {
							CustomToastUtils.getInstance().showTextToast(R.string.copy_success);
							Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.CP_TEXT);
						}
					});
				}
				finish();
			}
			@Override
			public void back() {
				onBackPressed();
			}
		};
		mTextResultView.setResultDelegate(mQrTextResultDelegate);
		
	}
	
	/**
	 * QR 开启动画
	 */
	private void openQrAnim(){
		final View topLogo = findViewById(R.id.qr_top_logo);
		final View bottomLogo = findViewById(R.id.qr_bottom_logo);
		Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.qr_top_open);
		topAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				topLogo.setVisibility(View.VISIBLE);
				isAnimating = true;
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				topLogo.setVisibility(View.GONE);
				isAnimating = false;
				// fix bug #2377 动画结束后再设置点按监听
				initListener();
			}
		});
		Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.qr_bottom_open);
		bottomAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				bottomLogo.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				bottomLogo.setVisibility(View.GONE);
			}
		});
		topLogo.startAnimation(topAnim);
		bottomLogo.startAnimation(bottomAnim);
		
	}
	
	/**
	 * QR 关闭动画
	 */
	private void closeQrAnim(){
		if (isAnimating) {
			return;
		}
		if (handler != null) {
			// 暂停扫描
			handler.quitSynchronously();
		}
		final View topLogo = findViewById(R.id.qr_top_logo);
		final View bottomLogo = findViewById(R.id.qr_bottom_logo);
		Animation topAnim = AnimationUtils.loadAnimation(this, R.anim.qr_top_close);
		topAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				topLogo.setVisibility(View.VISIBLE);
				isAnimating = true;
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
//				topLogo.setVisibility(View.GONE);
				isAnimating = false;
			}
		});
		Animation bottomAnim = AnimationUtils.loadAnimation(this, R.anim.qr_bottom_close);
		bottomAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
				bottomLogo.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
//				bottomLogo.setVisibility(View.GONE);
				// 动画结束，关闭activity
				finish();
				overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
			}
		});
		topLogo.startAnimation(topAnim);
		bottomLogo.startAnimation(bottomAnim);
		
	}
	
	@Override
	public void onBackPressed() {
		if (mTextResultView.isShown()) {
			mTextResultView.hide();
			// 重新开启扫描
			handler.sendEmptyMessage(R.id.restart_preview);
			return;
		}
		closeQrAnim();
	}
	
	boolean flag = true;
	protected void light() {
		Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.QR_CODE_LIGHT);
		if (flag == true) {
			flag = false;
			// 开闪光灯
			CameraManager.get().openLight();
		} else {
			flag = true;
			// 关闪光灯
			CameraManager.get().offLight();
		}
	}

	private void photo() {
		Intent innerIntent = new Intent(); // "android.intent.action.GET_CONTENT"
		if (Build.VERSION.SDK_INT < 19) {
			innerIntent.setAction(Intent.ACTION_GET_CONTENT);
		} else {
			innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		}
		// innerIntent.setAction(Intent.ACTION_GET_CONTENT);
		innerIntent.setType("image/*");
		Intent wrapperIntent = Intent.createChooser(innerIntent, getString(R.string.qrcode_choose_pic));
		startActivityForResult(wrapperIntent, REQUEST_CODE);
		Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.QR_CODE_ALBUM);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE:
				showProcessDialog(true);
				String[] proj = { MediaStore.Images.Media.DATA };
				// 获取选中图片的路径
				Cursor cursor = getContentResolver().query(data.getData(),
						proj, null, null, null);
				if (cursor != null && data != null && cursor.moveToFirst()) {
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					photo_path = cursor.getString(column_index);
					if (photo_path == null) {
						photo_path = Utils.getPath(getApplicationContext(), data.getData());
					}
				} else {
					if (data.getData() != null) {
						photo_path = Utils.getPath(getApplicationContext(), data.getData());
					}
				}
				if (cursor != null) {
					cursor.close();
				}
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							final Result result = ZXingUtil.scanningImage(photo_path);
							if (result == null) {
								Looper.prepare();
								handleResult(null);
								Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.SCAN_FAILED);
								Looper.loop();
							} else {
								Looper.prepare();
								ThreadManager.postTaskToUIHandler(new Runnable() {
									@Override
									public void run() {
										handleResult(result.toString());
										//TODO 成功识别相册图片二维码统计
										Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.RECOG_ALBUM_QR);
									}
								});
								Looper.loop();
							}
						} catch (Exception e) {
							SimpleLog.e(e);
							Looper.prepare();
							handleResult(null);
							Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.SCAN_FAILED);
							Looper.loop();
						}
					}
				}).start();
				break;
			}
		}
	}

	
	/** 
     * 把中文转成Unicode码 
     * @param str 
     * @return 
     */  
    public String chinaToUnicode(String str){  
        String result="";  
        for (int i = 0; i < str.length(); i++){  
            int chr1 = (char) str.charAt(i);  
            if(chr1>=19968&&chr1<=171941){//汉字范围 \u4e00-\u9fa5 (中文)  
                result+="\\u" + Integer.toHexString(chr1);  
            }else{  
                result+=str.charAt(i);  
            }  
        }  
        return result;  
    }  
	

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.mo_scanner_preview_view);
		mSurfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(mSurfaceHolder);
		} else {
			mSurfaceHolder.addCallback(this);
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
//		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		// viewfinderView.drawViewfinder();
	}

	public void handleDecode(final Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
//		playBeepSoundAndVibrate();
		String recode = ZXingUtil.recode(result.toString());
		// 数据返回
//		Intent data = new Intent();
//		data.putExtra("result", recode);
//		Toast.makeText(this, recode, Toast.LENGTH_LONG).show();
//		setResult(300, data);
//		finish();
		showProcessDialog(true);
		handleResult(recode);
		if (TextUtils.equals(result.getBarcodeFormat().toString(), "QR_CODE")) {
			SimpleLog.d("qr_code", "this is a qr code");
			Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.SCAN_SUCC_QR);
		}else {
			SimpleLog.d("qr_code", "this is a bar code");
			Statistics.sendOnceStatistics(GoogleConfigDefine.QR_CODE, GoogleConfigDefine.SCAN_SUCC_BAR);
		}
	}

	/**
	 * 处理扫码结果
	 * @param resultStr
	 */
	private void handleResult(String resultStr){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showProcessDialog(false);
			}
		});

		if (TextUtils.isEmpty(resultStr)) {
			// 结果为空
			CommonDialog dialog = new CommonDialog(CaptureActivity.this, R.string.tips, R.string.qrcode_not_find);
			dialog.setBtnCancelText(R.string.ok);
			dialog.setButtonVisibility(CommonDialog.ID_BTN_DEFAULT, false);
			dialog.setButtonVisibility(CommonDialog.ID_BTN_OK, false);
			dialog.show();
		} else {
			SimpleLog.e(TAG, resultStr);
			if (isCanOpenByBrowser(resultStr)) {
				// 浏览器可以打开
				TabViewManager.getInstance().loadUrl(resultStr, NavigateSource.Other);
				finish();
			} else {
				// 浏览器打不开。。
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				if (resultStr.startsWith(SMS_PREFIX)) {
					// 短信
					int last = resultStr.lastIndexOf(":");
					intent.setData(Uri.parse(resultStr.substring(0, last)));
					intent.putExtra("sms_body", resultStr.substring(last + 1));  
				} else {
					// 其他
					intent.setData(Uri.parse(resultStr));
				}
	            try {
					startActivity(intent);
					finish();
				} catch (Exception e) {
					// TODO 打不开
					SimpleLog.e(e);
					handleTextResult(resultStr);
				}
			}
		}
		
	}
	
	private void handleTextResult(String resultStr) {
		mTextResultView.showTextResult(resultStr, mBitmap);
	}

	/**
	 * 根据不同搜索引擎build不同url
	 * @param content
	 * @param context
	 * @return
	 */
	private String buildSearchUrl(String content, Context context) {
		return SearchUtils.buildSearchUrl(content, context);
	}
	
	@SuppressLint("DefaultLocale") 
	private boolean isCanOpenByBrowser(String result){
		String lowerResult = result.toLowerCase();
		if (lowerResult.startsWith("http://") ||
				lowerResult.startsWith("https://") ||
				lowerResult.startsWith("rtsp://")) {
			return true;
		}
		return false;
	}

	private static final long VIBRATE_DURATION = 200L;

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	//显示或隐藏处理中对话框
	private void showProcessDialog(boolean show) {
		if (mProcessDialog == null) {
			mProcessDialog = new LoggingDialog(this, R.string.qrcode_processing, R.drawable.login_loading);
		}

		if (show) {
			mProcessDialog.show();
		} else {
			mProcessDialog.hide();
		}
	}
	
}