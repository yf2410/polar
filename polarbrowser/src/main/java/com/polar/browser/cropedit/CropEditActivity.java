package com.polar.browser.cropedit;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.edmodo.cropper.i.ICropEdit;
import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.data.ConfigDefine;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.CommonUtils;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;

import org.sprite2d.apps.pp.BrushPreset;
import org.sprite2d.apps.pp.IPainter;
import org.sprite2d.apps.pp.PainterCanvas;
import org.sprite2d.apps.pp.PainterCanvas.ISaveComplete;

public class CropEditActivity extends LemonBaseActivity implements OnClickListener, IPainter ,ICropEdit{
	private CropView mCropView;
	
	private PainterCanvas mCanvas;
	private View mRlCanvas;
	
	private ColorBoxView mColorBoxView;
	private View mArrow;

	private View mMask;
	private View mCanvasMask;
	
	private ImageView mBtnPaint;
	
	private ImageView mPaint1;
	private ImageView mPaint2;
	private ImageView mPaint3;
	
	private View mRlPaints;
	private View mPaintsArrow;
	
	private View mBtnCrop;
	private View mBtnRevert;
	private BrushPoit mBtnChooseColor;
	private View mBottomToolbar;
	private View mBtnBack;
	private View mBtnShare;
	
	private com.polar.browser.cropedit.MosaicsView mMosaicsView;
	
	private com.polar.browser.cropedit.CropShareView mCropShareView;
	
	public static Bitmap mScreenShot;
	
	/** 来标记用户是否保存/分享过图片，保存过的话，按返回键就直接返回 **/
	public static boolean sSavedPic;
	
	private com.polar.browser.cropedit.IFinishedCrop mFinishedCropListener = new com.polar.browser.cropedit.IFinishedCrop() {
		
		@Override
		public void finishedCrop(Bitmap bitmap) {
			hideCropView(bitmap);
			com.polar.browser.cropedit.CropStorageUtil.setBitmap(bitmap);
		}
		
		@Override
		public void cancelCrop() {
//			hideCropView(CropStorageUtil.getBitmap());
			cropCancel();
		}
	};
	
	private com.polar.browser.cropedit.IFinishedMosaics mFinishedMosaics = new com.polar.browser.cropedit.IFinishedMosaics() {
		
		@Override
		public void finishedMosaics(Bitmap bitmap) {
			com.polar.browser.cropedit.CropStorageUtil.setBitmap(bitmap);
			mosaicsFinish(bitmap);
		}
		
		@Override
		public void cancelMosaics() {
			mosaicsCancel();
		}
	};
	private Bitmap mBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ConfigWrapper.put(ConfigDefine.CROPEDITACTIVITY_IS_STATUS_BAR,false);
		ConfigWrapper.apply();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cropedit);
		// 是否全屏判断
//        SysUtils.setFullScreen(this, ConfigManager.getInstance().isFullScreen());
//        Statistics.sendEventStatis( ConfigDefine.UM_MENU_SCEENSHOT_PAINT_ALL);
		initView();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 设置竖屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		if(ConfigManager.getInstance().isEnableNightMode()){
//			WindowManager.LayoutParams lp  =  getWindow().getAttributes();
//			lp.screenBrightness = SysUtils.getSuitableNightBrightness(this);
//			getWindow().setAttributes(lp);
//		}
	}
	
	private void initView() {
		mCanvas = (PainterCanvas) findViewById(R.id.canvas);
		mCropView = (CropView) findViewById(R.id.crop_view);
		mColorBoxView = (ColorBoxView) findViewById(R.id.color_box);
		mCropShareView = (CropShareView) findViewById(R.id.crop_share_view);
		mMask = findViewById(R.id.color_box_mask);
		mRlCanvas = findViewById(R.id.rl_canvas);
		mCanvasMask = findViewById(R.id.canvas_mask);
		mBtnCrop = findViewById(R.id.btn_crop);
		mBtnRevert = findViewById(R.id.btn_revert);
		mBtnChooseColor = (BrushPoit) findViewById(R.id.btn_choose_color);
		mBottomToolbar = findViewById(R.id.bottom_toolbar);
		mArrow = findViewById(R.id.color_box_arrow);
		mBtnBack = findViewById(R.id.btn_back);
		mBtnShare = findViewById(R.id.btn_share);
		mBtnPaint = (ImageView) findViewById(R.id.btn_paint);
		
		mMosaicsView = (MosaicsView) findViewById(R.id.mosaics_view);
		
		mRlPaints = findViewById(R.id.rl_paints);
		
		mPaint1 = (ImageView) findViewById(R.id.paint_1);
		mPaint2 = (ImageView) findViewById(R.id.paint_2);
		mPaint3 = (ImageView) findViewById(R.id.paint_3);
		mPaintsArrow = findViewById(R.id.paints_arrow);
		
		mBtnCrop.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);
		mBtnChooseColor.setOnClickListener(this);
		mMask.setOnClickListener(this);
		mBtnBack.setOnClickListener(this);
		mBtnShare.setOnClickListener(this);
		mBtnPaint.setOnClickListener(this);
		mPaint1.setOnClickListener(this);
		mPaint2.setOnClickListener(this);
		mPaint3.setOnClickListener(this);
		
		findViewById(R.id.btn_mosaics).setOnClickListener(this);
		
		initBrushTools();
		mBitmap = CropStorageUtil.getBitmap();
		resetCanvasSize(mBitmap.getWidth(), mBitmap.getHeight());
	}

	private void initBrushTools() {
		mColorBoxView.setCanvas(mCanvas);
		mColorBoxView.setIColorBoxChange(new com.polar.browser.cropedit.IColorBoxChange() {
			@Override
			public void changedSize(float size) {
				mBtnChooseColor.setSize(size);
				mBtnChooseColor.postInvalidate();
			}
			
			@Override
			public void changedColor(int color) {
				mBtnChooseColor.setColor(color);
				mBtnChooseColor.postInvalidate();
			}
		});
		
		mCanvas.setPresetColor(getResources().getColor(R.color.color_box_item_7));
		mCanvas.setPresetSize(14.5f);
		LayoutParams params = mBtnChooseColor.getLayoutParams();
		int width = DensityUtil.dip2px(JuziApp.getInstance(), 34);
		params.width = width;
		params.height = width;
		mBtnChooseColor.setLayoutParams(params);
		
		setRevertEnabled(false);
	}

	private void initData() {
		mCropView.registerListener(mFinishedCropListener);
		mMosaicsView.registerListener(mFinishedMosaics);
	}
	
	private void resetCanvasSize(final int width, final int height){
		
		mRlCanvas.post(new Runnable() {
			@Override
			public void run() {
				int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		        mRlCanvas.measure(w, h);
		        // canvas 外部layout宽高
				int rlWidth = mRlCanvas.getWidth();
				int rlHeight = mRlCanvas.getHeight();
				// 假如是全屏截图 ，需要给截图缩放，缩放后的宽高
				int scaleWidth = AppEnv.SCREEN_WIDTH * 4 / 5;
				int scaleHeight = 0;
				if (AppEnv.sIsFullScreen) {
					scaleHeight = AppEnv.SCREEN_HEIGHT * 4 / 5;
				} else {
					scaleHeight = (AppEnv.SCREEN_HEIGHT - getStatusBarHeight()) * 4 / 5;
				}
				if (width == 0) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCanvas.getLayoutParams();
					if (scaleWidth > rlWidth) {
						params.width = rlWidth;
						params.height = scaleHeight * rlWidth / scaleWidth;
					} else if (scaleHeight > rlHeight) {
						params.height = rlHeight;
						params.width = scaleWidth * rlHeight / scaleHeight;
					} else {
						params.width = scaleWidth;
						params.height = scaleHeight;
					}
					int topMargin = (rlHeight - params.height) / 2 + mBtnShare.getHeight();
					params.topMargin = topMargin;
					mCanvas.setLayoutParams(params);
					mCanvasMask.setLayoutParams(params);
					mMosaicsView.resetCanvasSize(params.width, params.height, topMargin);
					scaleBitmapBySize(params.width,params.height);
				} else if (width >= AppEnv.SCREEN_WIDTH) {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCanvas.getLayoutParams();
					float ratio = scaleRatio(width,height,scaleWidth,scaleHeight);
					params.width = (int)(width * ratio);
					params.height = (int)(height * ratio);
					int topMargin = (rlHeight - params.height) / 2 + mBtnShare.getHeight();
					params.topMargin = topMargin;
					mCanvas.setLayoutParams(params);
					mCanvasMask.setLayoutParams(params);
					mMosaicsView.resetCanvasSize(params.width, params.height, topMargin);
					scaleBitmapBySize(params.width,params.height);
				} else {
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCanvas.getLayoutParams();
					params.width = width;
					params.height = height;
					int topMargin = (rlHeight - params.height) / 2 + mBtnShare.getHeight();
					params.topMargin = topMargin;
					mCanvas.setLayoutParams(params);
					mCanvasMask.setLayoutParams(params);
					mMosaicsView.resetCanvasSize(params.width, params.height, topMargin);
					scaleBitmapBySize(params.width,params.height);
				}
			}
		});
		
	}

	/**
	 * According to the size of canvas to scale bitmap
	 * @param width
	 * @param height
     */
	private void scaleBitmapBySize(int width, int height){
		try {
			Bitmap bitmap = Bitmap.createScaledBitmap(CropStorageUtil.getBitmap(),width,height,false);
			if(bitmap != null){
				CropStorageUtil.setBitmap(bitmap);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param width bitmap width
	 * @param height bitmap height
	 * @param maxWidth limit max width
	 * @param maxHeight limit max height
     * @return
     */
	private float scaleRatio(int width, int height, int maxWidth, int maxHeight){
		if (width > maxWidth || height > maxHeight) {
			// 计算出实际宽高和目标宽高的比率
			float widthRatio = maxWidth / (float)width;
			float heightRatio = maxHeight / (float)height;
			return widthRatio > heightRatio ? heightRatio : widthRatio;
		}
		return 1;
	}
	
	private int getStatusBarHeight(){
		if (ConfigManager.getInstance().isFullScreen()) {
			return 0;
		}
		
		if (Build.VERSION.SDK_INT <= 18) {
			// 获取状态栏高度
			Rect frame = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			return frame.top;
		} else {
			return AppEnv.STATUS_BAR_HEIGHT;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_crop:
			if (CommonUtils.isFastDoubleClick()) {
				return;
			}
			handleCrop();
			break;
		case R.id.btn_revert:
			handleRevert();
			break;
		case R.id.btn_choose_color:
			handleChooseColor();
			break;
		case R.id.color_box_mask:
			if (CommonUtils.isFastDoubleClick()) {
				return;
			}
			handleColorBoxMaskClick();
			break;
		case R.id.btn_back:
			if (CommonUtils.isFastDoubleClick()) {
				return;
			}
			if (sSavedPic) {
				sSavedPic = false;
				finish();
				return;
			}
			showExitDialog();
			break;
		case R.id.btn_share:
			if (CommonUtils.isFastDoubleClick()) {
				return;
			}
			handleBtnShareClick();
			break;
			
		case R.id.btn_mosaics:
			if (CommonUtils.isFastDoubleClick()) {
				return;
			}
			handleMosaics();
			break;
			
		case R.id.btn_paint:
			handlePaint();
			break;
			
		case R.id.paint_1:
			handlePaintClick(1);
			break;
			
		case R.id.paint_2:
			handlePaintClick(2);
			break;
			
		case R.id.paint_3:
			handlePaintClick(3);
			break;
			
		default:
			break;
		}
	}

	private void handlePaintClick(int index){
//		String typeValue = null;
		switch (index) {
		case 1:
			mCanvas.setPresetType(BrushPreset.BRUSH);
			mPaint1.setImageResource(R.drawable.icon_brush);
			mPaint2.setImageResource(R.drawable.icon_brush2_gray);
			mPaint3.setImageResource(R.drawable.icon_brush3_gray);
			mBtnPaint.setImageResource(R.drawable.icon_brush_gray);
//			typeValue = ConfigDefine.UM_SCEENSHOT_PAINT_BRUSH_TYPE1;
			break;
		case 2:
			mCanvas.setPresetType(BrushPreset.MARKER);
			mPaint1.setImageResource(R.drawable.icon_brush_gray);
			mPaint2.setImageResource(R.drawable.icon_brush2);
			mPaint3.setImageResource(R.drawable.icon_brush3_gray);
			mBtnPaint.setImageResource(R.drawable.icon_brush2_gray);
//			typeValue = ConfigDefine.UM_SCEENSHOT_PAINT_BRUSH_TYPE2;
			break;
		case 3:
			mCanvas.setPresetType(BrushPreset.PEN);
			mPaint1.setImageResource(R.drawable.icon_brush_gray);
			mPaint2.setImageResource(R.drawable.icon_brush2_gray);
			mPaint3.setImageResource(R.drawable.icon_brush3);
			mBtnPaint.setImageResource(R.drawable.icon_brush3_gray);
//			typeValue = ConfigDefine.UM_SCEENSHOT_PAINT_BRUSH_TYPE3;
			break;

		default:
			break;
		}
//		if (typeValue != null) {
//			Map<String,String> map_value = new HashMap<String, String>();
//			map_value.put(ConfigDefine.UM_SCEENSHOT_PAINT_BRUSH_TYPE_KEY, typeValue);
////			Statistics.sendEventStatis( ConfigDefine.UM_SCEENSHOT_PAINT_BRUSH_TYPE, map_value);
//		}
	}
	
	/**
	 * 选择画笔
	 */
	private void handlePaint() {
		mRlPaints.setVisibility(View.VISIBLE);
		mPaintsArrow.setVisibility(View.VISIBLE);
		mMask.setVisibility(View.VISIBLE);
		mCanvasMask.setVisibility(View.VISIBLE);
//		Statistics.sendEventStatis( ConfigDefine.UM_SCEENSHOT_PAINT_BRUSH_BTN);
	}

	/**
	 * 设置画板撤销是否可用
	 * @param enabled
	 */
	public void setRevertEnabled(boolean enabled){
		mBtnRevert.setEnabled(enabled);
	}
	/**
	 * 设置马赛克页撤销是否可用
	 * @param enabled
	 */
	public void setMosaicsRevertEnabled(boolean enabled){
		mMosaicsView.setRevertEnabled(enabled);
	}
	
	private void showExitDialog(){
		final CommonDialog mCommonDialog;
		mCommonDialog = new CommonDialog(this, getString(R.string.title_exit_edit_crop), getString(R.string.tip_exit_edit_crop));
		mCommonDialog.setBtnCancel(getString(R.string.exit), new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
				finish();
			}
		});
		mCommonDialog.setBtnOk(getString(R.string.save), new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
				final String path = VCStoragerManager.getInstance().getImageDirPath() + System.currentTimeMillis() + ".png";
				saveBitmap(path, new ISaveComplete() {
					@Override
					public void complete() {
						CustomToastUtils.getInstance().showDurationToast(getString(R.string.save_success) + path, 3000);
//						Statistics.sendEventStatis( ConfigDefine.UM_SCEENSHOT_PAINT_SAVE);
						finish();
					}
				});
			}
		});
		mCommonDialog.setBtnOption(getString(R.string.cancel), new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCommonDialog.dismiss();
			}
		});
		mCommonDialog.show();
	}
	
	private void handleBtnShareClick() {
		mCropShareView.show();
	}

	private void handleColorBoxMaskClick() {
		mColorBoxView.setVisibility(View.GONE);
		mMask.setVisibility(View.GONE);
		mCanvasMask.setVisibility(View.GONE);
		mArrow.setVisibility(View.GONE);
		mRlPaints.setVisibility(View.GONE);
		mPaintsArrow.setVisibility(View.GONE);
	}

	private void handleCrop() {
		Bitmap bitmap = mCanvas.getThread().getBitmap();
		mCropView.setVisibility(View.VISIBLE);
		mBottomToolbar.setVisibility(View.GONE);
		mBtnBack.setVisibility(View.GONE);
		mBtnShare.setVisibility(View.GONE);
		showCropView(bitmap);
		SimpleLog.e("handleCrop", "bitmap == " + bitmap);
//		Statistics.sendEventStatis( ConfigDefine.UM_SCEENSHOT_PAINT_CROP);
	}
	
	private void handleMosaics() {
		Bitmap bitmap = mCanvas.getThread().getBitmap();
		mBottomToolbar.setVisibility(View.GONE);
		mBtnBack.setVisibility(View.GONE);
		mBtnShare.setVisibility(View.GONE);
		showMosaicsView(bitmap);
		ThreadManager.postTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				mCanvas.setVisibility(View.GONE);
			}
		});
//		Statistics.sendEventStatis( ConfigDefine.UM_SCEENSHOT_PAINT_MOSAICS_BTN);
	}

	protected void mosaicsFinish(Bitmap bitmap) {
		
		PainterCanvas.clearAllActions();
		setRevertEnabled(false);
		
		mBottomToolbar.setVisibility(View.VISIBLE);
		mBtnBack.setVisibility(View.VISIBLE);
		mBtnShare.setVisibility(View.VISIBLE);
		mCanvas.setVisibility(View.VISIBLE);
		mCanvas.getThread().setBitmap(bitmap, false);
		mCanvas.setBitmap(bitmap);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCanvas.getLayoutParams();
		mCanvas.setLayoutParams(params);
	}

	private void showMosaicsView(Bitmap bitmap) {
		mMosaicsView.showMosaicsView();
		mMosaicsView.setBitmap(bitmap);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mCanvas.getLayoutParams();
		mMosaicsView.resetCanvasSize(params.width, params.height, params.topMargin);
	}

	private void handleRevert() {
		if (PainterCanvas.mCurrentPaintIndex > 0) {
			PainterCanvas.mCurrentPaintIndex--;
			if (PainterCanvas.mCurrentPaintIndex == 0) {
				setRevertEnabled(false);
			}
//			Statistics.sendEventStatis( ConfigDefine.UM_SCEENSHOT_PAINT_REVERT);
			CropEditActivity.sSavedPic = false;
		}
	}
	
	private void handleChooseColor() {
		if (mColorBoxView.isShown()) {
			mColorBoxView.setVisibility(View.GONE);
			mMask.setVisibility(View.GONE);
			mCanvasMask.setVisibility(View.GONE);
			mArrow.setVisibility(View.GONE);
		} else {
			mColorBoxView.setVisibility(View.VISIBLE);
			mMask.setVisibility(View.VISIBLE);
			mCanvasMask.setVisibility(View.VISIBLE);
			mArrow.setVisibility(View.VISIBLE);
//			Statistics.sendEventStatis( ConfigDefine.UM_SCEENSHOT_PAINT_BRUSH_WIDTH);
		}
	}

	@Override
	public Bitmap getLastPicture() {
		return com.polar.browser.cropedit.CropStorageUtil.getBitmap();
	}
	
	@Override
	public Bitmap getCurrentDrawing(){
		return mCanvas.getThread().getBitmap();
	}
	
	private void showCropView(Bitmap bitmap) {
		mCropView.setVisibility(View.VISIBLE);
		mCropView.setImageBitmap(bitmap);
		mBottomToolbar.setVisibility(View.GONE);
		mCanvas.setVisibility(View.GONE);
	}
	
	private void hideCropView(Bitmap bitmap) {
		mCropView.setVisibility(View.GONE);
		mBottomToolbar.setVisibility(View.VISIBLE);
		mCanvas.setVisibility(View.VISIBLE);
		mBtnBack.setVisibility(View.VISIBLE);
		mBtnShare.setVisibility(View.VISIBLE);
		
		PainterCanvas.clearAllActions();
		setRevertEnabled(false);
		
		final int width = bitmap.getWidth(); 
		final int height = bitmap.getHeight(); 
		mCanvas.post(new Runnable() {
			@Override
			public void run() {
				resetCanvasSize(width, height);
			}
		});
		// 要剪裁的图片尺寸过小时，将不允许再剪裁
		if (width <= 48 || height <= 48) {
			mBtnCrop.setEnabled(false);
		}
		mCanvas.getThread().setBitmap(bitmap, false);
		mCanvas.setBitmap(bitmap);
	}

	protected void cropCancel() {
		mCropView.setVisibility(View.GONE);
		mBottomToolbar.setVisibility(View.VISIBLE);
		mBtnBack.setVisibility(View.VISIBLE);
		mBtnShare.setVisibility(View.VISIBLE);
		mCanvas.setVisibility(View.VISIBLE);
	}
	
	private void mosaicsCancel() {
		mMosaicsView.setVisibility(View.GONE);
		mBottomToolbar.setVisibility(View.VISIBLE);
		mBtnBack.setVisibility(View.VISIBLE);
		mBtnShare.setVisibility(View.VISIBLE);
		mCanvas.setVisibility(View.VISIBLE);
	}
	
	public void saveBitmap(String path, final ISaveComplete iComplete){
		
		if (mCanvas != null) {
			if (TextUtils.isEmpty(path)) {
				path = VCStoragerManager.getInstance().getImageDirPath() + ShareUtil.SHARE_SHOT_IMG;
			}
			final String p = path;
			ThreadManager.postTaskToLogicHandler(new Runnable() {
				public void run() {
					try {
						mCanvas.saveBitmap(p, iComplete);
					} catch (Exception e) {
						SimpleLog.e(e);
					}
				}
			});
		}
		
	}
	
	@Override
	public void onBackPressed() {
		if (mMosaicsView.onBackPress()) {
			return;
		}
		if (mCropShareView != null && mCropShareView.isShown()) {
			mCropShareView.hide();
		} else if (mCropView != null && mCropView.isShown()) {
			cropCancel();
		} else if (mColorBoxView != null && mColorBoxView.isShown()) {
			mColorBoxView.setVisibility(View.GONE);
			mMask.setVisibility(View.GONE);
			mCanvasMask.setVisibility(View.GONE);
			mArrow.setVisibility(View.GONE);
		} else if(sSavedPic){
			finish();
		}else {
			showExitDialog();
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
		mCanvas.recycle();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		com.polar.browser.cropedit.CropStorageUtil.recycle();
	}

	@Override
	public void setSaved(boolean saved) {
		sSavedPic = saved;
	}
	
}