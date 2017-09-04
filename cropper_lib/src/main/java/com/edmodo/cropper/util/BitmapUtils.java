package com.edmodo.cropper.util;

import android.graphics.Bitmap;

public final class BitmapUtils {
	
	public static void recycleBitmap(Bitmap bitmap){
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}
	
}
