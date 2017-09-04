package org.sprite2d.apps.pp;

import org.sprite2d.apps.pp.util.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.os.HandlerThread;
import android.view.SurfaceHolder;

import com.edmodo.cropper.i.IDrawEnd;
import com.edmodo.cropper.util.BitmapUtils;


public class MosaicsThread extends HandlerThread implements IPainterThread{

	/**
	 * Freeze when freeze() called
	 */
	public static final int SLEEP = 0;

	/**
	 * Application ready when activate() called
	 */
	public static final int READY = 1;

	public static final int SETUP = 2;

	/**
	 * Holder
	 */
	private SurfaceHolder mHolder;

	/**
	 * Brush object instance of Paint Class
	 */
	private Paint mBrush;

	/**
	 * Brush size in pixels
	 */
	private float mBrushSize;

	/**
	 * Last brush point detect for anti-alias
	 */
	private float mLastBrushPointX;

	/**
	 * Last brush point detect for anti-alias
	 */
	private float mLastBrushPointY;

	/**
	 * Canvas clear color
	 */
	private int mCanvasBgColor;

	/**
	 * Canvas object for drawing bitmap
	 */
	private Canvas mCanvas;

	/**
	 * Bitmap for drawing
	 */
	private Bitmap mBitmap;

	/**
	 * mosaics layer
	 */
	private Bitmap mMosaicsBmp;
	
	/**
	 * True if application is running
	 */
	private boolean mIsActive;

	/**
	 * Status of the running application
	 */
	private int mStatus;

	/** 璁板綍褰撳墠鐢荤敾杞ㄨ抗 **/
	private Action mAction;
	
	private PainterHandler mHandler;
	
	private int mGridWidth;
	
	/**
	 * 
	 * @param surfaceHolder
	 * @param context
	 * @param handler
	 */
	public MosaicsThread(Context context, SurfaceHolder surfaceHolder) {
		super("MosaicsThread");
		// base data
		mHolder = surfaceHolder;
		
		// defaults brush settings
		mBrushSize = 20;
		mBrush = new Paint();
		mBrush.setAntiAlias(true);
		mBrush.setColor(Color.rgb(0, 0, 0));
		mBrush.setStrokeWidth(mBrushSize);
		mBrush.setStrokeCap(Cap.ROUND);

		// default canvas settings
		mCanvasBgColor = Color.WHITE;

		mGridWidth = Utils.dip2px(context, 6);
		
		// set negative coordinates for reset last point
		mLastBrushPointX = -1;
		mLastBrushPointY = -1;
	}
	
	public void initHandler() {
		mHandler = new PainterHandler(getLooper(), this);
//		mHandler.sendEmptyMessage(PainterHandler.MSG_DRAW);
	}
	
	public void startDrawing(){
		mHandler.sendEmptyMessage(PainterHandler.MSG_START);
	}
	
	public void pauseDrawing(){
		mHandler.sendEmptyMessage(PainterHandler.MSG_PAUSE);
	}
	
	public void exit() {
		
	}

	public void drawBitmap() {
		waitForBitmap();
		Canvas canvas = null;
		try {
			canvas = mHolder.lockCanvas();
			switch (mStatus) {
			case MosaicsThread.READY: {
				if (canvas != null && mBitmap != null && !mBitmap.isRecycled()) {
					// 鐢昏儗鏅┈璧涘厠
					drawMosaics(canvas);
					// 鐢绘秱鎶圭殑杞ㄨ抗
					drawPathAction(canvas);
				}
				break;
			}
			case SETUP: {
				if (canvas != null && mBitmap != null && !mBitmap.isRecycled()) {
					canvas.drawColor(mCanvasBgColor);
					canvas.drawLine(50, (mBitmap.getHeight() / 100) * 35,
							mBitmap.getWidth() - 50,
							(mBitmap.getHeight() / 100) * 35, mBrush);
				}
				break;
			}
			}
		} finally {
			if (canvas != null) {
				mHolder.unlockCanvasAndPost(canvas);
			}
			if (isFreeze()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void drawMosaics(Canvas canvas){
		// 鐢婚┈璧涘厠
		Bitmap mosaicsBmp = mMosaicsBmp;
		if (mosaicsBmp != null) {
			// 鍦ㄤ富鐢绘澘涓婄粯鍒朵复鏃剁敾甯冧笂鐨勫浘鍍�
			canvas.drawBitmap(mosaicsBmp, 0, 0, null);
		}
	}
	
	private void drawPathAction(Canvas canvas){
		canvas.drawARGB(0, 0, 0, 0);
		// 鐢昏建杩�
		Bitmap newbit = Bitmap.createBitmap(mBitmap.getWidth(),
				mBitmap.getHeight(), Config.ARGB_8888);
		Canvas canvasTemp = new Canvas(newbit);
		canvasTemp.drawColor(Color.TRANSPARENT);
		canvasTemp.drawBitmap(mBitmap, 0, 0, null);
		
		final int size1 = MosaicsCanvas.mCurrentPaintIndex;
		for (int i = 0; i < size1; i++) {
			if (i < MosaicsCanvas.getActionPathList().size()) {
				Action action = MosaicsCanvas.getActionPathList().get(i);
				action.draw(canvasTemp);
			}
		}
		// 鐢诲綋鍓嶇敾绗旂棔杩�
		if (mAction != null) {
			mAction.draw(canvasTemp);
		}

		// 鍦ㄤ富鐢绘澘涓婄粯鍒朵复鏃剁敾甯冧笂鐨勫浘鍍�
		canvas.drawBitmap(newbit, 0, 0, null);

		// recycle bitmap
		if (newbit != null && !newbit.isRecycled()) {
			newbit.recycle();
			newbit = null;
		}
		
	}
	
	public void setPreset(BrushPreset preset) {
		mBrush.setColor(preset.color);
		mBrushSize = preset.size;
		mBrush.setStrokeWidth(preset.size);
		if (preset.blurStyle != null && preset.blurRadius > 0) {
			mBrush.setMaskFilter(new BlurMaskFilter(preset.blurRadius,
					preset.blurStyle));
		} else {
			mBrush.setMaskFilter(null);
		}
	}

	public void drawBegin(final float x, final float y) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mLastBrushPointX = x;
				mLastBrushPointY = y;
				mAction = new MyMosaics(x, y, mBrush.getStrokeWidth());
				MosaicsCanvas.clearSpareAction();
			}
		});
	}

	public void drawEnd(final float x, final float y, final IDrawEnd drawEnd) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mLastBrushPointX == x && mLastBrushPointY == y) {
					mAction.move(x + 1, y);
				} else {
					mAction.move(x, y);
				}
				MosaicsCanvas.addActionPath(mAction);
				MosaicsCanvas.mCurrentPaintIndex++;
				mLastBrushPointX = -1;
				mLastBrushPointY = -1;
				mAction = null;
				drawEnd.end();
			}
		});
	}

	public void draw(final float x, final float y) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mLastBrushPointX > 0) {
					if (mLastBrushPointX - x == 0 && mLastBrushPointY - y == 0) {
						return;
					}
					mAction.move(x, y);
				} else {
					mAction.move(x + 1, y);
				}
				mLastBrushPointX = x;
				mLastBrushPointY = y;
			}
		});

	}

	private Bitmap getGridMosaics(Bitmap bmp) {
		if (bmp == null || bmp.isRecycled()) {
			return null;
		}
		int imgWidth = bmp.getWidth();
		int imgHeight = bmp.getHeight();
		
		if (imgWidth <= 0 || imgHeight <= 0) {
			return null;
		}
		
		Bitmap bitmap = Bitmap.createBitmap(imgWidth, imgHeight,
				Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);

		int horCount = (int) Math.ceil(imgWidth / (float) mGridWidth);
		int verCount = (int) Math.ceil(imgHeight / (float) mGridWidth);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		for (int horIndex = 0; horIndex < horCount; ++horIndex) {
			for (int verIndex = 0; verIndex < verCount; ++verIndex) {
				int l = mGridWidth * horIndex;
				if (l > imgWidth) {
					l = imgWidth;
				}
				int t = mGridWidth * verIndex;
				if (t > imgHeight) {
					t = imgHeight;
				}
				int r = l + mGridWidth;
				if (r > imgWidth) {
					r = imgWidth;
				}
				int b = t + mGridWidth;
				if (b > imgHeight) {
					b = imgHeight;
				}
				int color = bmp.getPixel(l, t);
				Rect rect = new Rect(l, t, r, b);
				paint.setColor(color);
				canvas.drawRect(rect, paint);
			}
		}
		canvas.save();
		return bitmap;
	}
	
	public void setBitmap(Bitmap bitmap, boolean clear) {
		mBitmap = bitmap;
		if (clear) {
			mBitmap.eraseColor(mCanvasBgColor);
		}
		mCanvas = new Canvas(mBitmap);
		
//		SimpleLog.e("PainterThread", "setBitmap= " + clear);
		
		mMosaicsBmp = getGridMosaics(mBitmap);
	}

	public void restoreBitmap(Bitmap bitmap, Matrix matrix) {
		if (matrix != null) {
			mCanvas.drawBitmap(bitmap, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));
		} else {
			mCanvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
		}
		
//		SimpleLog.e("MosaicsThread", "restoreBitmap= " + bitmap);
		
		// 鍒濆鍖栭┈璧涘厠灞�
		mMosaicsBmp = getGridMosaics(mBitmap);
		
	}

	public void clearBitmap() {
		mBitmap.eraseColor(mCanvasBgColor);
	}

	public Bitmap getBitmap() {
		if (mBitmap == null) {
			return null;
		}
		
		Bitmap newbit = Bitmap.createBitmap(mBitmap.getWidth(),
				mBitmap.getHeight(), Config.ARGB_4444);
		Canvas canvasTemp = new Canvas(newbit);
		
		// 鐢婚┈璧涘厠
		Bitmap mosaicsBmp = mMosaicsBmp;
		if (mosaicsBmp != null) {
			// 鍦ㄤ富鐢绘澘涓婄粯鍒朵复鏃剁敾甯冧笂鐨勫浘鍍�
			canvasTemp.drawBitmap(mosaicsBmp, 0, 0, null);
		}
		
		// 鐢昏建杩�
		Bitmap coverbit = Bitmap.createBitmap(mBitmap.getWidth(),
				mBitmap.getHeight(), Config.ARGB_4444);
		Canvas coverCanvas = new Canvas(coverbit);
		coverCanvas.drawColor(Color.TRANSPARENT);
		coverCanvas.drawBitmap(mBitmap, 0, 0, null);
		for (int i = 0; i < MosaicsCanvas.mCurrentPaintIndex; i++) {
			MosaicsCanvas.getActionPathList().get(i).draw(coverCanvas);
		}
		
		// 鐢诲綋鍓嶇敾绗旂棔杩�
		if (mAction != null) {
			mAction.draw(coverCanvas);
		}
		canvasTemp.drawBitmap(coverbit, 0, 0, null);
		
		BitmapUtils.recycleBitmap(coverbit);
		
		return newbit;
	}

	/**
	 * 鍒囨崲椹禌鍏嬮绮掑ぇ灏�
	 * @param size
	 */
	public void changeMosaicsSize(int size){
		mGridWidth = size;
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mMosaicsBmp = getGridMosaics(mBitmap);
		}
	}
	
	public void on() {
		mIsActive = true;
	}

	public void off() {
		mIsActive = false;
	}

	public void freeze() {
		mStatus = SLEEP;
	}

	public void activate() {
		mStatus = READY;
	}

	public void setup() {
		mStatus = SETUP;
	}

	public boolean isFreeze() {
		return (mStatus == SLEEP);
	}

	public boolean isSetup() {
		return (mStatus == SETUP);
	}

	public boolean isReady() {
		return (mStatus == READY);
	}

	public boolean isRun() {
		return mIsActive;
	}

	public int getBackgroundColor() {
		return mCanvasBgColor;
	}

	public void waitForBitmap() {
		while (mBitmap == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
