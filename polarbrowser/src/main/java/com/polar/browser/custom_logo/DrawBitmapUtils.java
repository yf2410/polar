package com.polar.browser.custom_logo;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.env.AppEnv;
import com.polar.browser.utils.RegularUtils;

/**
 *绘制和回收bitmap的工具类，提供了几种绘制bitmap的方式。
 * @author sw_01
 *
 */
public final class DrawBitmapUtils {
	
	/**
	 * @param bitmap
	 */
	public static void recycleBitmap(Bitmap bitmap){
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}
	
	/**
	 * 绘制圆形图片
	 * @param bm
	 * @param r
	 * @param g
	 * @param b
	 * @param text
	 * @return
	 */
	public static Bitmap drawRoundBitmap(Bitmap bm,int r,int g,int b,String text) {
		Canvas canvas = new Canvas(bm);
		
		Paint paint = new Paint();
		Matrix matrix = new Matrix();
		
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		//画图
		canvas.drawColor(Color.TRANSPARENT);
		
		
		canvas.drawBitmap(bm, matrix, paint);
		canvas.drawCircle(150, 150, 150, paint);
		Typeface font = Typeface.create("宋体", Typeface.BOLD);
		//写字
		paint.setAntiAlias(true);
		paint.setTypeface(font);
		paint.setTextSize(160.0f);
		
		paint.setColor(Color.rgb(r, g, b));
		
		canvas.drawText(text,(bm.getWidth()/2)-80,(bm.getHeight()/2+60), paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		
		canvas.restore();
		
		return bm;
	}
	
	
	/**
	 * 绘制矩形圆角图片
	 * @param s
	 * @param text
	 * @return
	 */
//	public static Bitmap drawRoundRect(Swatch s,String text) {
//		Bitmap bm = Bitmap.createBitmap(300, 300, Config.ARGB_4444);
//		Canvas canvas = new Canvas(bm);
//		
//		Paint paint = new Paint();
//		Matrix matrix = new Matrix();
//		
//		paint.setColor(s.getRgb());
//		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
//				| Paint.FILTER_BITMAP_FLAG));
//		//画图
//		canvas.drawColor(Color.TRANSPARENT);
//		
//		canvas.drawBitmap(bm, matrix, paint);
//		// 设置个新的长方形  
//		RectF rect = new RectF(0, 0, 300, 300);
//		//第二个参数是x半径，第三个参数是y半径  
//        canvas.drawRoundRect(rect, 40, 45, paint);
//		Typeface font = Typeface.create("宋体", Typeface.BOLD);
//		//写字
//		paint.setTypeface(font);
//		paint.setTextSize(160.0f);
//		paint.setColor(s.getTitleTextColor());
//		paint.setAntiAlias(true);
//		if (RegularUtils.isAllLettersAndNum(text)) {
//			canvas.drawText(text,(bm.getWidth()/2-60),(bm.getHeight()/2+60), paint);
//		}else {
//			canvas.drawText(text,(bm.getWidth()/2)-80,(bm.getHeight()/2+60), paint);
//		}
//		canvas.save(Canvas.ALL_SAVE_FLAG);
//		canvas.restore();
//		
//		return bm;
//	}
	
	/**
	 * 根据url对应的icon绘制自定义logo
	 * @param text
	 * @return
	 */
	public static Bitmap drawBitmapDepenRGB(String text){
		return drawPolygon(text);
	}
	
	/**
	 * 画多边形
	 * @param text
	 * @return
	 */
	public static Bitmap drawPolygon(String text) {
		if (TextUtils.equals("I", text)) {
			text = " "+text;
		}
		Bitmap bgbm = BitmapFactory.decodeResource(JuziApp.getInstance().getResources(), getRandomBg());
		Matrix matrix = new Matrix();
		
		Bitmap bm = Bitmap.createBitmap(300, 300, Config.ARGB_4444);
		Canvas canvas = new Canvas(bm);
		
		Paint paint = new Paint();
		
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		//画图
		canvas.drawColor(Color.TRANSPARENT);
		// M' = S(sx, sy) * M
		 float scaleX = (float) ((AppEnv.SCREEN_WIDTH / (float) bgbm.getWidth())/(4.8*AppEnv.SCREEN_WIDTH / 1440));

//		 float scaleY = (float) ((AppEnv.SCREEN_HEIGHT / (float) bgbm.getHeight())/(8.1*AppEnv.SCREEN_HEIGHT / 2368)); 
		 
		matrix.postScale(scaleX, scaleX);
		
		canvas.drawBitmap(bgbm, matrix, paint);
		
		Typeface font = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
		//写字
		paint.setTypeface(font);
		paint.setColor(Color.WHITE);
		paint.setTextSize(160.0f);
		paint.setAntiAlias(true);
		
		if (RegularUtils.isAllLettersAndNum(text)) {
			canvas.drawText(text,(bm.getWidth()/2-60),(bm.getHeight()/2+56), paint);
		}else {
			canvas.drawText(text,(bm.getWidth()/2)-80,(bm.getHeight()/2+60), paint);
		}
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		
		return bm;
	}
	
	public static int getRandomBg() {
		int bgs[] = {
				R.drawable.default_bg_01,
				R.drawable.default_bg_02,
				R.drawable.default_bg_03,
				R.drawable.default_bg_04,
				R.drawable.default_bg_05,
				R.drawable.default_bg_06,
				R.drawable.default_bg_07,
				};
		Random r = new Random();
		return bgs[r.nextInt(6)];
	}
}
