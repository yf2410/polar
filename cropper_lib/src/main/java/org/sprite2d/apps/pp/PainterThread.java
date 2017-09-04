package org.sprite2d.apps.pp;

import com.edmodo.cropper.i.IDrawEnd;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.os.HandlerThread;
import android.view.SurfaceHolder;


public class PainterThread extends HandlerThread implements IPainterThread {

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
	 * Brush type
	 */
	private int mBrushType;
	
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
	
	/**
	 * 
	 * @param surfaceHolder
	 * @param context
	 * @param handler
	 */
	public PainterThread(SurfaceHolder surfaceHolder) {
		super("PainterThread");
		// base data
		mHolder = surfaceHolder;

		// defaults brush settings
		mBrushSize = 2;
		mBrush = new Paint();
		mBrush.setAntiAlias(true);
		mBrush.setColor(Color.rgb(0, 0, 0));
		mBrush.setStrokeWidth(mBrushSize);
		mBrush.setStrokeCap(Cap.ROUND);

		// default canvas settings
		mCanvasBgColor = Color.WHITE;

		// set negative coordinates for reset last point
		mLastBrushPointX = -1;
		mLastBrushPointY = -1;
	}
	
	public void initHandler() {
		mHandler = new PainterHandler(getLooper(), this);
		mHandler.sendEmptyMessage(PainterHandler.MSG_DRAW);
	}
	
	public void startDrawing(){
		mHandler.sendEmptyMessage(PainterHandler.MSG_DRAW);
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
			case PainterThread.READY: {
				if (canvas != null && mBitmap != null && !mBitmap.isRecycled()) {
					// 鐢绘澘缁樺浘鍖鸿儗鏅浘鐗�
					canvas.drawBitmap(mBitmap, 0, 0, null);
					Bitmap newbit = Bitmap.createBitmap(mBitmap.getWidth(),
							mBitmap.getHeight(), Config.ARGB_8888);
					Canvas canvasTemp = new Canvas(newbit);
					canvasTemp.drawColor(Color.TRANSPARENT);

					final int size = PainterCanvas.mCurrentPaintIndex;
					for (int i = 0; i < size; i++) {
						if (PainterCanvas.getActionPathList().size() > i) {
							PainterCanvas.getActionPathList().get(i).draw(canvasTemp);
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
	
	public void setPreset(BrushPreset preset) {
		mBrush.setColor(preset.color);
		mBrushSize = preset.size;
		mBrushType = preset.type;
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
				// TODO Auto-generated method stub
				mLastBrushPointX = x;
				mLastBrushPointY = y;
				if (mBrushType == BrushPreset.MOSAICS) {
					mAction = new MyMosaics(x, y, mBrush.getStrokeWidth());
				} else if (mBrushType == BrushPreset.ERASE) {
					mAction = new MyErase(x, y, mBrush.getStrokeWidth());
				} else {
					mAction = new MyPath(x, y, mBrush.getStrokeWidth(), mBrush.getColor(), mBrushType);
				}
				
				PainterCanvas.clearSpareAction();
			}
		});
	}

	public void drawEnd(final float x, final float y, final IDrawEnd drawEnd) {
		
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mLastBrushPointX == x && mLastBrushPointY == y) {
					mAction.move(x + 1, y);
				} else {
					mAction.move(x, y);
				}
				
				PainterCanvas.addActionPath(mAction);
				PainterCanvas.mCurrentPaintIndex++;
				
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
				// TODO Auto-generated method stub
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
	
	public void setBitmap(Bitmap bitmap, boolean clear) {
		mBitmap = bitmap;
		if (clear) {
			mBitmap.eraseColor(mCanvasBgColor);
		}
		mCanvas = new Canvas(mBitmap);
		
	}

	public void restoreBitmap(Bitmap bitmap, Matrix matrix) {
		if (matrix != null) {
			mCanvas.drawBitmap(bitmap, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));
		} else {
			mCanvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));
		}
//		mState.undoBuffer = saveBuffer();
		
		// 鍒濆鍖栭┈璧涘厠灞�
//		mMosaicsBmp = getGridMosaics(getBitmap());
//		mMosaicsBmp = getGridMosaics(mBitmap);
		
	}

	public void clearBitmap() {
		mBitmap.eraseColor(mCanvasBgColor);
	}

	public Bitmap getBitmap() {
		if (mBitmap == null || mBitmap.isRecycled()) {
			return null;
		}
		Bitmap newbit = Bitmap.createBitmap(mBitmap);
		Canvas canvasTemp = new Canvas(newbit);
		canvasTemp.drawColor(Color.TRANSPARENT);
		
		for(int i = 0; i < PainterCanvas.mCurrentPaintIndex; i++) {
			if (PainterCanvas.getActionPathList().size() > i) {
				PainterCanvas.getActionPathList().get(i).draw(canvasTemp);
			}
		}
		// 鐢诲綋鍓嶇敾绗旂棔杩�
		if (mAction != null) {
			mAction.draw(canvasTemp);
		}
		
//		return mBitmap;
		
		return newbit;
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
