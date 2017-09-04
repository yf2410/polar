package com.polar.browser.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.network.NoCookieJar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class BitmapUtils {

	public static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}


	/**
	 *
	 * @param pathName
	 * @param reqWidth
	 * @param reqHeight
	 * @param isMagnify  当图片自身宽高，小于控件的宽高时，是否放大图片
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromFile(String  pathName,
													 int reqWidth, int reqHeight, boolean isMagnify) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName,options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName,options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
											int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 *
	 * @param inputStream
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream,
															int reqWidth, int reqHeight) {
		try{
			// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(inputStream,null,options);
			// 调用上面定义的方法计算inSampleSize值
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			// 使用获取到的inSampleSize值再次解析图片
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream(inputStream,null,options);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 混合压缩方法（先根据宽高进行尺寸压缩，再根据文件大小的要求进行文件大小压缩）
	 * @param bitmap
	 * @param reqWidth
	 * @param reqHeight
	 * @param maxSize
	 * @return
	 */
	public static Bitmap compressBitmap(Bitmap bitmap,int reqWidth,int reqHeight, int maxSize) {
		if(bitmap == null || maxSize <= 0) return null;
		try{
			Bitmap bm = createBitmapThumbnail(bitmap,reqWidth,reqHeight);
			return bm;
			//compressImage未起作用，图片质量降低但占用内存未减少
//			return compressImage(bm,maxSize);
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 针对文件的尺寸进行压缩
	 * @param bitMap
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap createBitmapThumbnail(Bitmap bitMap,int reqWidth,int reqHeight) {
		try{
			int width = bitMap.getWidth();
			int height = bitMap.getHeight();
			// 计算缩放比例
			float scaleWidth =  reqWidth / (float)width;
			float scaleHeight = reqHeight / (float)height;
			// 取得想要缩放的matrix参数
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			// 得到新的图片
			Bitmap newBitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height,
					matrix, true);
			return newBitMap;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 针对文件大小进行压缩
	 * @param image
	 * @param maxSize
	 * @return
	 */
	public static Bitmap compressImage(Bitmap image,int maxSize) {
		if(image == null || maxSize <= 0) return null;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.PNG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 100;
			while ( options>=0 && baos.toByteArray().length / 1024 > maxSize) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();//重置baos即清空baos
				image.compress(Bitmap.CompressFormat.PNG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
				options -= 10;//每次都减少10
			}
			ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
			Bitmap bm = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
			if(bm != null)
			return bm;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 下载Url到Bitmap
	 * @param url
	 * @return
     */
	public static Bitmap getBitmapByLoadUrl(String url){

		int timeout = 5*1000;

		OkHttpClient client;
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		NoCookieJar noCookieJar = new NoCookieJar();
		builder.cookieJar(noCookieJar)
				.connectTimeout(timeout, TimeUnit.MILLISECONDS)
				.readTimeout(timeout, TimeUnit.MILLISECONDS)
				.writeTimeout(timeout, TimeUnit.MILLISECONDS);

		client = builder.build();

		Request request = new Request.Builder()
				.url(url)
				.build();

		try {
			Response response = client.newCall(request).execute();
			if(response != null && response.code() == 200){
				InputStream inStream = response.body().byteStream(); // Read the data from the stream
				return BitmapFactory.decodeStream(inStream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static String saveResourceToFile(Context context, int resID,boolean needReplace){
		String filePath = VCStoragerManager.getInstance().getImageDirPath() + Constants.SHARE_AD_BLOCK_IMG_NAME;
		File file = new File(filePath);
		if(file.exists()&&!needReplace){//资源文件不需要替换
			return filePath;
		}else{
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resID);
			FileUtils.saveBitmapToFile(bitmap,VCStoragerManager.getInstance().getImageDirPath(),Constants.SHARE_AD_BLOCK_IMG_NAME);
			return filePath;
		}
	}

	public static Drawable getRoundDrawableByFile(Context context, String path){
		if(path == null) return null;
		RoundedBitmapDrawable roundPH = RoundedBitmapDrawableFactory.create(context.getResources(),path);
		roundPH.setCircular(true);
		return roundPH;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if(drawable == null) return null;
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap;
		int width = Math.max(drawable.getIntrinsicWidth(), 2);
		int height = Math.max(drawable.getIntrinsicHeight(), 2);
		try {
/*            bitmap = Bitmap
                    .createBitmap(width,height,
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);*/
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			drawable.draw(canvas);
		} catch (Exception e) {
			e.printStackTrace();
			SimpleLog.w("BitmapUtils", "Failed to create bitmap from drawable!");
			bitmap = null;
		}

		return bitmap;
	}

}
