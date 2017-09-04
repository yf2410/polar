package com.polar.browser.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.polar.browser.R;
import com.polar.browser.download.ImageGalleryActivity;
import com.polar.browser.download.OpenFileActivity;
import com.polar.browser.video.VideoActivity;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("DefaultLocale")
public final class OpenFileUtils {

	private static final String TAG = "OpenFileUtils";

	public static final String TYPE_KEY = "type";
	public static final String TYPE_COMPRESS = "Compress";  //压缩文件
	public static final String TYPE_PICTURE = "image";     //图片

	public static final String FILE_NAME = "fileName";
	public static final String FILE_PATH = "filePath";

	// android获取一个用于打开HTML文件的intent
	public static Intent getHtmlFileIntent(File file) {
		Uri uri = Uri.parse(file.toString()).buildUpon()
				.encodedAuthority("com.android.htmlfileprovider")
				.scheme("content").encodedPath(file.toString()).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	// android获取一个用于打开图片文件的intent
	public static Intent getImageFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	// android获取一个用于打开PDF文件的intent
	public static Intent getPdfFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	// android获取一个用于打开文本文件的intent
	public static Intent getTextFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "text/plain");
		return intent;
	}

	// android获取一个用于打开音频文件的intent
	public static Intent getAudioFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "audio/*");
		return intent;
	}

	// android获取一个用于打开视频文件的intent
	public static Intent getVideoFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	/**
	 * 点击通知栏
	 * android获取一个用于打开视频文件的intent(跳转自定义播放器)
	 *
	 * @param file
	 * @param context
	 * @return
	 */
	public static Intent getVideoFileIntent(File file, Context context) {
		Intent videoIntent = new Intent();
		String fileName = file.getName();
		String filePath = file.toString();
		videoIntent.putExtra("fileName", fileName);
		videoIntent.putExtra("filePath", filePath);
		videoIntent.setClass(context, VideoActivity.class);
		return videoIntent;
	}

	// android获取一个用于打开CHM文件的intent
	public static Intent getChmFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/x-chm");
		return intent;
	}

	// android获取一个用于打开Word文件的intent
	public static Intent getWordFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	// android获取一个用于打开Excel文件的intent
	public static Intent getExcelFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	// android获取一个用于打开PPT文件的intent
	public static Intent getPPTFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	// android获取一个用于打开apk文件的intent
	public static Intent getApkFileIntent(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		return intent;
	}

	// android获取一个用于打开压缩文件的intent
	public static Intent getCompressedFileIntent(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		String fileName = file.getName();
		if (fileName.toLowerCase().endsWith(".rar")) {
			intent.setDataAndType(Uri.fromFile(file),
					"application/x-rar-compressed");
		} else if (fileName.toLowerCase().endsWith(".zip")) {
			intent.setDataAndType(Uri.fromFile(file), "application/zip");
		}
		return intent;
	}

	// android获取一个用于打开默认文件的intent
	public static Intent getDefaultFileIntent(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setData(Uri.fromFile(file));
		return intent;
	}

	/**
	 * 定义用于检查要打开的文件的后缀是否在遍历后缀数组中
	 *
	 * @param checkItsEnd
	 * @param fileEndings
	 * @return
	 */
	public static boolean checkEndsWithInStringArray(String checkItsEnd,
													 String[] fileEndings) {
		if(checkItsEnd == null || fileEndings == null) return false;
		for (String aEnd : fileEndings) {
			if (checkItsEnd.toLowerCase().endsWith(aEnd))
				return true;
		}
		return false;
	}

	/**
	 * 打开文件
	 *
	 * @param file
	 * @param context
	 */
	public static void openFile(File file, Context context) {
		if (file != null && file.isFile()) {
			String filePath = file.toString();
			SimpleLog.d(TAG, filePath);
			Intent intent;
			if (checkEndsWithInStringArray(filePath, context.getResources()
					.getStringArray(R.array.fileEndingImage))) {
				// intent = getImageFileIntent(file);
				// startActivity(intent, context, file);
				openFileWithVc(file, filePath, context, "image");
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingWebText))) {
				intent = getHtmlFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingPackage))) {
				intent = getApkFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingAudio))) {
				intent = getAudioFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingVideo))) {
				openFileWithVcVideo(file, filePath, context);
//				intent = getVideoFileIntent(file);
//				startActivity(intent, context, file);
//				intent = getVideoFileIntent(file,context);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingVideoflv))) {
				intent = getVideoFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingText))) {
				intent = getTextFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingPdf))) {
				intent = getPdfFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingWord))) {
				intent = getWordFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingExcel))) {
				intent = getExcelFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources().getStringArray(R.array.fileEndingPPT))) {
				intent = getPPTFileIntent(file);
				startActivity(intent, context, file);
			} else if (checkEndsWithInStringArray(filePath, context
					.getResources()
					.getStringArray(R.array.fileEndingCompressed))) {
				// intent = getCompressedFileIntent(file);
				// startActivity(intent, context, file);
				openFileWithVc(file, filePath, context, "Compress");
			} else {
				// showMessage("无法打开，请安装相应的软件！");
				try {
					intent = getDefaultFileIntent(file);
					startActivity(intent, context, file);
				} catch (Exception e) {
					SimpleLog.e(e);
					CustomToastUtils.getInstance().showTextToast(
							R.string.file_can_not_open);
				}
			}
		} else {
			// showMessage("对不起，这不是文件！");
			// Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
			CustomToastUtils.getInstance().showTextToast(context.getString(R.string.openfile_error));
		}
	}

	private static void openFileWithVc(File file, String filePath,
									   Context context, String type) {
		if (type.equalsIgnoreCase(TYPE_COMPRESS)) {
			Intent mCompressIntent = new Intent();
			mCompressIntent.putExtra(FILE_PATH, filePath);
			mCompressIntent.putExtra(TYPE_KEY, type);
			mCompressIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mCompressIntent.setClass(context, OpenFileActivity.class);
			context.startActivity(mCompressIntent);
		} else if (type.equalsIgnoreCase("image")) {
//			Intent mImageIntent = new Intent();
//			mImageIntent.putExtra("fileName", FileName);
//			mImageIntent.putExtra("type", type);
//			mImageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			mImageIntent.setClass(context, OpenFileActivity.class);
//			context.startActivity(mImageIntent);
			ArrayList<String> images = new ArrayList<>();
			images.add(filePath);
			ImageGalleryActivity.start(context,0,images);
		}
	}

	/**
	 * 点击item
	 *
	 * @param file
	 * @param filePath
	 * @param context
	 */
	private static void openFileWithVcVideo(File file, String filePath,
											Context context) {
		Intent videoIntent = new Intent();
		String fileName = file.getName();
		videoIntent.putExtra("fileName", fileName);
		videoIntent.putExtra("filePath", filePath);
		videoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		videoIntent.setClass(context, VideoActivity.class);
		context.startActivity(videoIntent);
	}

	public static void openFileWithDefault(String FilePath, Context context) {
		File mFile = new File(FilePath);
		Intent intent = getRightIntent(mFile, context);
		startActivity(intent, context, mFile);
	}

	public static void startActivity(Intent intent, Context context, File file) {
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			SimpleLog.e(e);
			try {
				intent = getDefaultFileIntent(file);
				context.startActivity(intent);
			} catch (Exception e1) {
				SimpleLog.e(e1);
				CustomToastUtils.getInstance().showTextToast(
						R.string.file_can_not_open);
			}
		}
	}

	/**
	 * 获得一个打开文件的Intent
	 *
	 * @param file
	 * @param context
	 * @return
	 */
	public static Intent getRightIntent(File file, Context context) {
		Intent intent = null;
		if (file != null && file.isFile()) {
			String fileName = file.toString();
			if (checkEndsWithInStringArray(fileName, context.getResources()
					.getStringArray(R.array.fileEndingImage))) {
				intent = getImageFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingWebText))) {
				intent = getHtmlFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingPackage))) {
				intent = getApkFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingAudio))) {
				intent = getAudioFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingVideo))) {
				intent = getVideoFileIntent(file);
				//调用自定义播放器
//				intent = getVideoFileIntent(file,context);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingText))) {
				intent = getTextFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingPdf))) {
				intent = getPdfFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingWord))) {
				intent = getWordFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingExcel))) {
				intent = getExcelFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources().getStringArray(R.array.fileEndingPPT))) {
				intent = getPPTFileIntent(file);
			} else if (checkEndsWithInStringArray(fileName, context
					.getResources()
					.getStringArray(R.array.fileEndingCompressed))) {
				intent = getCompressedFileIntent(file);
			} else {
				intent = getDefaultFileIntent(file);
			}
		}
		return intent;
	}

	/**
	 * 根据文件名获取文件类型图片
	 *
	 * @return file icon's drawable id;
	 */
	public static int getFileIconByFileName(String fileName) {
		fileName = fileName.toLowerCase();
		// 文档 txt,doc,docx,xls,xlsx,ppt,pptx,hlp,wps,rtf,html,htm,pdf,xml
		if (fileName.endsWith(".txt") || fileName.endsWith(".pdf")
				|| fileName.endsWith(".xls") || fileName.endsWith(".xlsx")
				|| fileName.endsWith(".ppt") || fileName.endsWith(".pptx")
				|| fileName.endsWith(".doc") || fileName.endsWith(".docx")
				|| fileName.endsWith(".wps") || fileName.endsWith(".hlp")
				|| fileName.endsWith(".rtfd.zip") || fileName.endsWith(".rtf")
				|| fileName.endsWith(".numbers") || fileName.endsWith(".pages")
				|| fileName.endsWith(".numbers.zip")
				|| fileName.endsWith(".pages.zip") || fileName.endsWith(".xml")
				|| fileName.endsWith(".json")) {
			return R.drawable.file_icon_doc;
		}
		// 音乐 wav,aif,au,mp3,ram,wma,aac,ogg,ape,acg,aiff,mid,ra
		if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")
				|| fileName.endsWith(".aif") || fileName.endsWith(".au")
				|| fileName.endsWith(".ram") || fileName.endsWith(".wma")
				|| fileName.endsWith(".aac") || fileName.endsWith(".ogg")
				|| fileName.endsWith(".ape") || fileName.endsWith(".acg")
				|| fileName.endsWith(".aiff") || fileName.endsWith(".mid")
				|| fileName.endsWith(".ra")) {
			return R.drawable.file_icon_music;
		}
		// 视频 mp4,avi,3gp,mpg,mov,swf,wmv,flv,mkv,rmvb mpeg,m4v,asf,ac3,rm
		if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
				|| fileName.endsWith(".3gp") || fileName.endsWith(".mpg")
				|| fileName.endsWith(".mov") || fileName.endsWith(".swf")
				|| fileName.endsWith(".wmv") || fileName.endsWith(".flv")
				|| fileName.endsWith(".mkv") || fileName.endsWith(".rmvb")
				|| fileName.endsWith(".mpeg") || fileName.endsWith(".m4v")
				|| fileName.endsWith(".asf") || fileName.endsWith(".ac3")
				|| fileName.endsWith(".rm") || fileName.endsWith(".webm")) {
			return R.drawable.file_icon_video;
		}
		// 图片 图片
		// bmp,gif,jpg,jpeg,pic,png,tiff,raw,svg,ai,tga,exif,fpx,psd,cdr,pcd,dxf,ufo,eps,hdri
		if (fileName.endsWith(".bmp") || fileName.endsWith(".gif")
				|| fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
				|| fileName.endsWith(".pic") || fileName.endsWith(".png")
				|| fileName.endsWith(".tiff") || fileName.endsWith(".raw")
				|| fileName.endsWith(".svg") || fileName.endsWith(".ai")
				|| fileName.endsWith(".tga") || fileName.endsWith(".exif")
				|| fileName.endsWith(".fpx") || fileName.endsWith(".psd")
				|| fileName.endsWith(".cdr") || fileName.endsWith(".pcd")
				|| fileName.endsWith(".dxf") || fileName.endsWith(".ufo")
				|| fileName.endsWith(".eps") || fileName.endsWith(".hdri")) {
			return R.drawable.file_icon_image;
		}
		// 压缩文件 rar,zip,arj,gz,z,cab,7z,iso
		if (fileName.endsWith(".zip") || fileName.endsWith(".rar")
				|| fileName.endsWith(".arj") || fileName.endsWith(".gz")
				|| fileName.endsWith(".z") || fileName.endsWith(".cab")
				|| fileName.endsWith(".7z") || fileName.endsWith(".iso")) {
			return R.drawable.file_icon_zip;
		}
		// 网页文件
		if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
			return R.drawable.offline_files;
		}
		// apk文件
		if (fileName.endsWith(".apk")) {
			return R.drawable.file_icon_apk;
		}
		return R.drawable.file_icon_default;
	}
}
