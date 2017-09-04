package org.sprite2d.apps.pp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;

import java.io.File;

public class Utils {
	/**
	 * 部分机型刷新一下媒体数据库，这样图片才能在图库中显示
	 */
	@SuppressLint("NewApi")
	public static void refreshMediaMounted(Context context, final String filePath) {

		if (Build.VERSION.SDK_INT < 19) {
			try {
//				StringBuilder sb = new StringBuilder();
//				sb.append("file://");
//				sb.append(JuziApp.getInstance().getDownloadDirPath());
//				JuziApp.getInstance().sendBroadcast(
//						new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(sb
//								.toString())));
//				sb = new StringBuilder();
//				sb.append("file://");
//				sb.append(JuziApp.getInstance().getImageDirPath());
//				JuziApp.getInstance().sendBroadcast(
//						new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(sb
//								.toString())));

				context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(filePath)));
				
			} catch (Exception e) {
				// SimpleLog.e(e);
			}
		} else {
			try {

				File f = new File(filePath);
				if (f.exists()) {
					File[] files = f.getParentFile().listFiles();
					String[] nameList = new String[files.length];
					for (int i = 0; i < files.length; i++) {
						nameList[i] = files[i].getAbsolutePath();
					}
					MediaScannerConnection.scanFile(context, nameList, null, null);
				}

			} catch (Exception e) {
				// SimpleLog.e(e);
			}
		}

	}
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
