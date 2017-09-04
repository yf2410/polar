package com.polar.browser.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import com.polar.browser.JuziApp;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.db.DownloadProvider;
import com.polar.browser.env.AppEnv;
import com.polar.browser.library.utils.SecurityUtil;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.upload.UploadHandler;
import com.polar.browser.vclibrary.bean.db.HistoryRecord;
import com.polar.browser.vclibrary.common.Constants;
import com.polar.browser.vclibrary.db.CustomOpenHelper;
import com.polar.browser.vclibrary.db.HistoryRecordApi;
import com.polar.browser.vclibrary.util.ApiUtil;

import org.apache.http.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.polar.browser.download_refactor.Constants.TYPE_APK;
import static com.polar.browser.download_refactor.Constants.TYPE_AUDIO;
import static com.polar.browser.download_refactor.Constants.TYPE_DOC;
import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_OTHER;
import static com.polar.browser.download_refactor.Constants.TYPE_VIDEO;
import static com.polar.browser.download_refactor.Constants.TYPE_WEB_PAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_ZIP;

public class FileUtils {
	public static final int FILE_TYPE_TXT = 0;
	public static final int FILE_TYPE_IMG = 1;
	public static final int FILE_TYPE_VIDEO = 2;
	public static final int FILE_TYPE_ZIP = 3;
	public static final int FILE_TYPE_MUSIC = 4;
	public static final int FILE_TYPE_APK = 5;
	public static final int FILE_TYPE_OTHER = 6;
	private static final String TAG = "FileUtils";
	private static final int BOM_HEADER_SIZE = 2;
	private static final int UNICODE_END_SIZE = 2;
	/**
	 * 下载数据库路径
	 */
	private static final String DB_TABLE_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/com.polar.browser/databases/download_db";
	/**
	 * 下载数据库表名
	 */
	private static final String DB_TABLE_NAME = "downloads";
	private static final int FILE_TYPE_WEB_PAGE = 6;

	private static final String STA_KEY = "lfdyp8kq";

	/**
	 * 复制文件
	 *
	 * @param is
	 * @param destFile
	 * @throws IOException
	 */

	private static UploadHandler mFileUploadHandler = null;

	public static void openFileChooser(Activity activity, ValueCallback<Uri> uploadFile,
									   String acceptType, String capture) {
		if (null == activity) {
			return;
		}
		mFileUploadHandler = new UploadHandler(activity, uploadFile, acceptType, capture);
		mFileUploadHandler.openFileChooser(uploadFile, acceptType, capture);
	}

	public static void openFileChooser(Activity activity, ValueCallback<Uri[]> uploadFile,
									   WebChromeClient.FileChooserParams fileChooserParams) {
		if (null == activity) {
			return;
		}
		mFileUploadHandler = new UploadHandler(activity, uploadFile, fileChooserParams);
		mFileUploadHandler.openFileChooser(uploadFile, fileChooserParams);
	}
	public static UploadHandler getFileUploadHandler() {
		return mFileUploadHandler;
	}

	public static void resetFileUploadHandler() {
		if (mFileUploadHandler != null) {
			mFileUploadHandler = null;
		}
	}

	public static void dismissFileUploadHandler() {
		if (mFileUploadHandler != null) {
			mFileUploadHandler.dimiss();
		}
	}

	public static void copyFile(InputStream is, File destFile)
			throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destFile);
			byte arrayByte[] = new byte[1024];
			int i = 0;
			while ((i = is.read(arrayByte)) != -1) {
				fos.write(arrayByte, 0, i);
			}
			fos.flush();
			fos.getFD().sync();// android.os.FileUtils.sync(fos);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 从assets中拷贝文件到file
	 *
	 * @param context
	 * @param assetsFileName
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyAssetsFile(Context context, String assetsFileName,
									  File destFile) throws IOException {
		copyFile(context.getAssets().open(assetsFileName), destFile);
	}

	public static String getPresetListName(String lan) {
		String presetListName = null;
		if (!TextUtils.isEmpty(lan)){
			presetListName = String.format("TOPWS_%s.txt", lan);
		}
		return presetListName;
	}

	/**
	 * 定义用于检查预置网址支持的语言
	 * @param lan
	 * @param fileEndings
     * @return
     */
	public static String getPresetListLan(String lan, String[] fileEndings) {
		if (TextUtils.isEmpty(lan)) {
			return "en";
		}
		for (String s : fileEndings) {
			if (lan.equalsIgnoreCase(s))
				return lan;
		}
		return "en";
	}

	/**
	 * @desc 检查输入的路径是否存在
	 * @author Hacken
	 * @time 2016/8/2 14:54
	 */
	public static boolean checkPathIsExist(String path){
		if (path != null){
			File file_db = new File(path);
			if (file_db.exists()) {
				return true;
			}
			return false;
		}
		SimpleLog.d(TAG, "debug_checkPathIsExist");
		return false;
	}

	/**
	 * 从data中拷贝文件到file
	 *
	 * @param context
	 * @param dataFilesFileName
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyDataFilesFile(Context context,
										 String dataFilesFileName, File destFile) throws IOException {
		copyFile(context.openFileInput(dataFilesFileName), destFile);
	}

	/**
	 * 关闭cursor
	 *
	 * @param cursor
	 */
	public static void closeCursor(Cursor cursor) {
		try {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		} catch (Exception ex) {
			// java.lang.IllegalStateException: stableCount < 0: -1
			// at android.os.Parcel.readException(Parcel.java:1433)
			// at android.os.Parcel.readException(Parcel.java:1379)
			// at
			// android.app.ActivityManagerProxy.refContentProvider(ActivityManagerNative.java:2405)
			// at
			// android.app.ActivityThread.releaseProvider(ActivityThread.java:4406)
			// at
			// android.app.ContextImpl$ApplicationContentResolver.releaseProvider(ContextImpl.java:1730)
			// at
			// android.content.ContentResolver$CursorWrapperInner.close(ContentResolver.java:1841)
		}
	}

	/**
	 * @param in
	 */
	public static final void closeSilently(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Throwable e) {
				if (AppEnv.DEBUG) {
					SimpleLog.e(TAG, e.getMessage());
				}
			}
		}
	}

	/**
	 * 保存bitmap到文件
	 *
	 * @param bitmap
	 * @param path
	 * @param fileName
	 */
	public static void saveBitmapToFile(Bitmap bitmap, String path,
										String fileName) {
		saveBitmapToFile(bitmap, path, fileName, 100, Bitmap.CompressFormat.PNG);
	}

	/**
	 * 保存bitmap到文件，需要指定保存的文件质量
	 *
	 * @param bitmap
	 * @param path
	 * @param fileName
	 * @param quality
	 */
	public static void saveBitmapToFile(Bitmap bitmap, String path,
										String fileName, int quality, Bitmap.CompressFormat format) {
		if (bitmap == null || bitmap.isRecycled() || path == null || fileName == null) {
			return;
		}
		File dir = new File(path);
		if (!dir.exists())
			dir.mkdirs();
		else if (!dir.isDirectory() && dir.canWrite()) {
			dir.delete();
			dir.mkdirs();
		}
		File f = new File(path, fileName);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			// Fix a crash. Strange!
			if (!bitmap.isRecycled()) {
				bitmap.compress(format, quality, out);
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		SimpleLog.d(TAG, "图片保存完成");
	}

	/**
	 * 从文件中获取Bitmap
	 *
	 * @param filePath
	 * @return
	 */
	public static Bitmap getBitmapFromFile(String filePath) {
		Bitmap bitmap = null;
		File f = new File(filePath);
		if (!f.exists()) {
			return bitmap;
		}
		try {
			FileInputStream fis = new FileInputStream(filePath);
			bitmap = BitmapFactory.decodeStream(fis).copy(Bitmap.Config.ARGB_8888, true);
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		return bitmap;
	}

	/**
	 * 从文件中获取Bitmap
	 *
	 * @param filePath
	 * @return
	 */
	public static Bitmap getBitmapFromFileCp(String filePath) {
		Bitmap bitmap = null;
		File f = new File(filePath);
		if (!f.exists()) {
			return bitmap;
		}
		try {
			FileInputStream fis = new FileInputStream(filePath);
			bitmap = BitmapFactory.decodeStream(fis).copy(Bitmap.Config.ARGB_8888, true);
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		return bitmap;
	}

	/**
	 * 从文件中获取RGB565的bitmap
	 *
	 * @param filePath
	 * @return
	 */
	public static Bitmap getRgb565BitmapFromFile(String filePath) {
		Bitmap bitmap = null;
		File f = new File(filePath);
		if (!f.exists()) {
			return bitmap;
		}
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inPurgeable = true;
			opts.inInputShareable = true;
			bitmap = BitmapFactory.decodeStream(fis, null, opts);
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		return bitmap;
	}

	/**
	 * 格式化文件大小
	 *
	 * @param fileSize_B 多少byte
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static String formatFileSize(long fileSize_B) {
		if (fileSize_B <= 0) {
			return "0B";
		}
		double G = fileSize_B * 1.0 / 1024 / 1024 / 1024;
		if (G > 1) {
			return String.format("%.1fG", G);
		}
		double M = fileSize_B * 1.0 / 1024 / 1024;
		if (M > 1) {
			return String.format("%.1fM", M);
		}
		double KB = fileSize_B * 1.0 / 1024;
		if (KB > 1) {
			return String.format("%.1fKB", KB);
		}
		return fileSize_B + "B";
	}

	/**
	 * byte[]写入到文件中
	 *
	 * @param file
	 * @param bytes
	 * @return
	 */
	public static boolean writeFile(File file, byte[] bytes) {
		boolean ret = true;
		FileOutputStream fos = null;
		try {
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			fos = new FileOutputStream(file);
			fos.write(bytes);
		} catch (Exception e) {
			ret = false; // 这里设计成如果头像或者信息其一保存失败，都算整体失败
		} finally {
			close(fos);
		}
		return ret;
	}

	/**
	 * 从文件中读取出byte[]
	 *
	 * @param file
	 * @return
	 */
	public static byte[] readFile(File file) {
		byte[] bytes = null;
		FileInputStream fis = null;
		if (file != null && file.exists()) {
			try {
				fis = new FileInputStream(file);
				bytes = new byte[fis.available()];
				fis.read(bytes);
			} catch (Exception e) {
				bytes = null;
			} finally {
				close(fis);
			}
		}
		return bytes;
	}

	/**
	 * 读取文本文件内容
	 *
	 * @param filePath 文件所在路径
	 * @return 文本内容
	 * @throws IOException 异常
	 * @author cn.outofmemory
	 * @date 2013-1-7
	 */
	public static String readFile(String filePath) throws IOException {
		StringBuffer sb = new StringBuffer();
		FileUtils.readToBuffer(sb, filePath);
		return sb.toString();
	}

	public static void readToBuffer(StringBuffer buffer, String filePath) throws IOException {
		InputStream is = new FileInputStream(filePath);
		String line; // 用来保存每行读取的内容
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		line = reader.readLine(); // 读取第一行
		while (line != null) { // 如果 line 为空说明读完了
			buffer.append(line); // 将读到的内容添加到 buffer 中
//			buffer.append("\n"); // 添加换行符
			line = reader.readLine(); // 读取下一行
		}
		reader.close();
		is.close();
	}

	//创建目录
	public static void createDir(String str) {
		File filePath = new File(JuziApp.getAppContext().getFilesDir() + File.separator + str);
		if (!filePath.exists()) {
			filePath.mkdir();
		}
	}

	//获取文件大小
	public static long getFileSize(File file) {
		if (file.exists()) {
			return file.length();
		}
		return 0;
	}


	//获取文件夹大小
	public static long getDirSize(File file) {
		//判断文件是否存在
		if (file != null && file.exists()) {
			//如果是目录则递归计算其内容的总大小
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				long size = 0;
				for (File f : children)
					size += getDirSize(f);
				return size;
			} else {//如果是文件则直接返回其大小,以“兆”为单位
				long size = (long) file.length();
				return size;
			}
		} else {
			//System.out.println("文件或者文件夹不存在，请检查路径是否正确！");
			return 0;
		}
	}

	/**
	 * 从Assets中读取文件
	 *
	 * @param c
	 * @param fileName
	 * @return
	 */
	public static byte[] readFileFromAssets(Context c, String fileName) {
		byte[] ret = null;
		InputStream in = null;
		try {
			in = c.getResources().getAssets().open(fileName);
			/* 获取文件的大小(字节数) */
			int length = in.available();

			/* 创建一个byte数组， 用于装载字节信息 */
			ret = new byte[length];

			/* 开始读取文件read(); *//* 解释：将读取到的字节放入到buffer这个数组中 */
			in.read(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		} finally {
			close(in);
		}
		return ret;
	}

	/**
	 * 关闭io
	 *
	 * @param io
	 */
	public static void close(Closeable... io) {
		if (io != null) {
			try {
				for (Closeable element : io) {
					if (element != null) {
						element.close();
					}
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * utf8->unicode
	 *
	 * @param utf8Bytes
	 * @return
	 */
	public static byte[] utf8toUnicode(byte[] utf8Bytes) {
		byte[] buffer = null;
		byte[] unicodeBytes = null;
		String strUnicode = new String(utf8Bytes);
		try {
			buffer = strUnicode.getBytes("unicode");
			unicodeBytes = new byte[buffer.length];
			for (int i = 2; i < buffer.length; ++i) {
				unicodeBytes[i - 2] = buffer[i];
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		}
		return unicodeBytes;
	}

	/**
	 * string->utf8 bytes
	 *
	 * @param data
	 * @return
	 */
	public static byte[] stringToUtf8Bytes(String data) {
		byte[] bytes = null;
		if (TextUtils.isEmpty(data)) {
			return bytes;
		}
		try {
			bytes = data.getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		}
		byte[] utf8TitleBytes = new byte[bytes.length + 2];
		for (int i = 0; i < bytes.length; ++i) {
			utf8TitleBytes[i] = bytes[i];
		}
		return utf8TitleBytes;
	}

	/**
	 * utf8->unicode
	 *
	 * @param utf8str
	 * @return
	 */
	public static String utf8toUnicode(String utf8str) {
		if (TextUtils.isEmpty(utf8str)) {
			return null;
		}
		byte[] unicodeBytes = null;
		byte[] unicodeBytesContainer = null;
		String strUnicode = new String(utf8str);
		try {
			// 由于unicode编码最后结尾需要有2个0， 所以人为增加2个字符长度
			unicodeBytes = strUnicode.getBytes("unicode");
			unicodeBytesContainer = new byte[unicodeBytes.length
					+ UNICODE_END_SIZE];
			for (int i = 0; i < unicodeBytes.length; ++i) {
				unicodeBytesContainer[i] = unicodeBytes[i];
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			SimpleLog.e(e);
		}
		// 由于转化后的unicode带有-2,-1两个字符的BOM头，因此去掉这两个字符
		String result = new String(unicodeBytesContainer);
		if (result != null && result.length() > BOM_HEADER_SIZE) {
			result = result.substring(BOM_HEADER_SIZE);
		}
		return result;
	}

	/**
	 * 只删除文件
	 **/
	public static void deleteOnlyFile(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isFile()) {
			file.delete();
		}
	}

	/**
	 * 删除一个文件 ,或者删除整个文件夹
	 */
	public static void deleteFileOrDirectory(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}
			for (File childFile : childFiles) {
				deleteFileOrDirectory(childFile);
			}
			file.delete();
		}
	}

	/**
	 * 删除目前目录中，带有指定前缀的文件
	 */
	public static void deleteFileWithPrefix(String directory,String prefix) {
		if (directory == null || prefix == null) {
			return;
		}
		File file = new File(directory);
		if (file.exists() && file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if(childFiles != null){
				for (File childFile : childFiles) {
					try{
						if(childFile != null && childFile.isFile() && childFile.getName().startsWith(prefix)){
							childFile.delete();
						}
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 获取文件类型
	 *
	 * @param fileName
	 * @return
	 */
	public static String getFileType(String fileName) {
		fileName = fileName.toLowerCase();
		// 文档 txt,doc,docx,xls,xlsx,ppt,pptx,hlp,wps,rtf,html,htm,pdf,xml
		if (fileName.endsWith(".txt") || fileName.endsWith(".pdf") ||
				fileName.endsWith(".xls") || fileName.endsWith(".xlsx") ||
				fileName.endsWith(".ppt") || fileName.endsWith(".pptx") ||
				fileName.endsWith(".doc") || fileName.endsWith(".docx") ||
				fileName.endsWith(".html") || fileName.endsWith(".htm") ||
				fileName.endsWith(".wps") || fileName.endsWith(".hlp") ||
				fileName.endsWith(".rtfd.zip") || fileName.endsWith(".rtf") ||
				fileName.endsWith(".numbers") || fileName.endsWith(".pages") ||
				fileName.endsWith(".numbers.zip") || fileName.endsWith(".pages.zip") ||
				fileName.endsWith(".xml") || fileName.endsWith(".json")) {
			return TYPE_DOC;
		}
		// 音乐 wav,aif,au,mp3,ram,wma,aac,ogg,ape,ac3,aiff,mid
		if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
				fileName.endsWith(".aif") || fileName.endsWith(".au") ||
				fileName.endsWith(".ram") || fileName.endsWith(".wma") ||
				fileName.endsWith(".aac") || fileName.endsWith(".ogg") ||
				fileName.endsWith(".ape") || fileName.endsWith(".acg") ||
				fileName.endsWith(".aiff") || fileName.endsWith(".mid")) {
			return TYPE_AUDIO;
		}
		// 视频 mp4,avi,3gp,mpg,mov,swf,wmv,flv,mkv,rmvb mpeg,m4v,asf,ac3,rm
		if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") ||
				fileName.endsWith(".3gp") || fileName.endsWith(".mpg") ||
				fileName.endsWith(".mov") || fileName.endsWith(".swf") ||
				fileName.endsWith(".wmv") || fileName.endsWith(".flv") ||
				fileName.endsWith(".mkv") || fileName.endsWith(".rmvb") ||
				fileName.endsWith(".mpeg") || fileName.endsWith(".m4v") ||
				fileName.endsWith(".asf") || fileName.endsWith(".ac3") ||
				fileName.endsWith(".rm")) {
			return TYPE_VIDEO;
		}
		// 图片 图片	bmp,gif,jpg,jpeg,pic,png,tiff,raw,svg,ai,tga,exif,fpx,psd,cdr,pcd,dxf,ufo,eps,hdri
		if (fileName.endsWith(".bmp") || fileName.endsWith(".gif") ||
				fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
				fileName.endsWith(".pic") || fileName.endsWith(".png") ||
				fileName.endsWith(".tiff") || fileName.endsWith(".raw") ||
				fileName.endsWith(".svg") || fileName.endsWith(".ai") ||
				fileName.endsWith(".tga") || fileName.endsWith(".exif") ||
				fileName.endsWith(".fpx") || fileName.endsWith(".psd") ||
				fileName.endsWith(".cdr") || fileName.endsWith(".pcd") ||
				fileName.endsWith(".dxf") || fileName.endsWith(".ufo") ||
				fileName.endsWith(".eps") || fileName.endsWith(".hdri")) {
			return TYPE_IMAGE;
		}
		// 压缩文件 rar,zip,arj,gz,z,cab,7z,iso
		if (fileName.endsWith(".zip") || fileName.endsWith(".rar") ||
				fileName.endsWith(".arj") || fileName.endsWith(".gz") ||
				fileName.endsWith(".z") || fileName.endsWith(".cab") ||
				fileName.endsWith(".7z") || fileName.endsWith(".iso")) {
			return TYPE_ZIP;
		}
		// apk文件
		if (fileName.endsWith(".apk")) {
			return TYPE_APK;
		}

		if(fileName.endsWith(".mht")){
			return TYPE_WEB_PAGE;
		}

		return TYPE_OTHER;
	}

	/**
	 * 当前路径是否为手机存储的路径
	 *
	 * @return
	 */
	public static boolean isCurrentPathMobile() {
		boolean isMobilePath = true;
		String currentFolder = ConfigWrapper.get(CommonData.KEY_DOWN_ROOT, VCStoragerManager.getInstance().getDefaultDownloadDirPath());
		String phoneRoot = VCStoragerManager.getInstance().getPhoneStorage();
		String sdRoot = VCStoragerManager.getInstance().getSDCardStorage();
		if (!TextUtils.isEmpty(phoneRoot) && currentFolder.startsWith(phoneRoot)) {
			isMobilePath = true;
		}
		if (!TextUtils.isEmpty(sdRoot) && currentFolder.startsWith(sdRoot)) {
			isMobilePath = false;
		}
		return isMobilePath;
	}

	/**
	 * 判断用户是否自定义了下载路径
	 *
	 * @return
	 */
	public static boolean isCustomDownloadPath() {
		String currentFolder = ConfigWrapper.get(CommonData.KEY_DOWN_ROOT, VCStoragerManager.getInstance().getDefaultDownloadDirPath());
		if (currentFolder != null && currentFolder.equals(VCStoragerManager.getInstance().getDefaultDownloadDirPath())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 获取目录下子文件夹个数
	 *
	 * @param file
	 * @return
	 */
	public static int getChildFolderCount(File file) {
		File[] files = file.listFiles();
		int folderCount = 0;
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					folderCount++;
				}
			}
		}
		return folderCount;
	}

	public static int getChildFileAndFolderCount(File file) {
		File[] files = file.listFiles();
		int folderCount = 0;
		if (files != null) {
			for (File f : files) {
				if (!f.getName().startsWith(".")) {
					folderCount++;
				}
			}
		}
		return folderCount;
	}

	/**
	 * 遍历该路径下文件
	 *
	 * @return
	 */
	public static void getVideoFileName() {
		SimpleLog.d(TAG, "getVideoFileName()");
		String downloadDataDirPath = VCStoragerManager.getInstance().getDownloadDataDirPath();
		if (downloadDataDirPath == null){

			return;
		}
		SimpleLog.d(TAG, "downloadDataDirPath=="+downloadDataDirPath);
		File file = new File(downloadDataDirPath);
		if (file.exists()) {
			File[] subFile = file.listFiles();
			if (subFile == null || subFile.length == 0) {
				return;
			}
			boolean checkTableIsExist = checkTableIsExist(DB_TABLE_PATH, DB_TABLE_NAME);
			SimpleLog.d(TAG, "checkTableIsExist==新库=="+checkTableIsExist);
			if (checkTableIsExist){
				for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
					// 判断是否为文件夹
					if (!subFile[iFileLength].isDirectory()) {
						String filename = subFile[iFileLength].getName();
						// 判断是否为.obj结尾
						if (filename.trim().toLowerCase().endsWith(".obj")) {
							try {
								ObjectInputStream in = new ObjectInputStream(new FileInputStream(downloadDataDirPath + filename));
								DownloadItemInfo downloadItemInfo = (DownloadItemInfo) in.readObject();
								//2016年8月15日14:04:31 添加判断：文件不存在的话不进行反序列化。
								if(new File(downloadItemInfo.mFilePath).exists()){
									DownloadProvider.getInstance().insertDownloadItemInfo(downloadItemInfo);
									SimpleLog.d(TAG,"反序列化完成");
								}else{
									SimpleLog.d(TAG,"没有进行反序列化删除obj文件。");
									DownloadManager.getInstance().deleteObjFile(downloadItemInfo);
								}
								in.close();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}else {
//
			}
		}
	}

	/**
	 * @desc 检查对应数据库传入的表是否存在
	 * @author Hacken
	 * @time 2016/8/2 17:23
	 */
	public static boolean checkTableIsExist(String tablePath,String tableName) {
		SQLiteDatabase db = null;
		boolean isTempleTableExist = false;
		if (checkTableIsExist(tablePath)){
			String strTableCheckSql = String.format("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='%s' ",
					tableName);
			Cursor tableCursor = null;
			try {
				db = SQLiteDatabase.openDatabase(tablePath,null,SQLiteDatabase.OPEN_READWRITE);
				tableCursor = db.rawQuery(strTableCheckSql, null);
				if(tableCursor.moveToNext()){
					int count = tableCursor.getInt(0);
					if(count > 0){
						isTempleTableExist = true;
					}
				}
			} catch (Exception e) {
				String exceptionStr = e.toString();
				SimpleLog.v("SQL", exceptionStr);
			} finally {
				if (tableCursor != null) {
					tableCursor.close();
				}
				if(db != null)
					db.close();
			}
		}
		return isTempleTableExist;
	}

	// 数据库字段定义
	private static final String COL_ID = "id";
	private static final String COL_URL = "url";
	private static final String COL_TITLE = "title";
	private static final String COL_SRC = "src";

	public static void upgradeHistoryDatabase(Context context) {
		//下载数据库路径
		final String DB_TABLE_PATH = "/data" + Environment.getDataDirectory().getAbsolutePath() + "/com.polar.browser/databases/history.db";
		//下载数据库老版本表名
		final String TABLE_NAME_MAIN = "history_main";
		SQLiteDatabase db = null;
		if (checkTableIsExist(DB_TABLE_PATH)){
			String SQL_QUERY_LIMIT_DISTINCT_URL = String.format("SELECT * FROM '%s' GROUP BY %s ORDER BY ts DESC LIMIT ?", TABLE_NAME_MAIN, "url");
			Cursor c = null;
			try {
				db = SQLiteDatabase.openDatabase(DB_TABLE_PATH,null,SQLiteDatabase.OPEN_READWRITE);
				c = db.rawQuery(SQL_QUERY_LIMIT_DISTINCT_URL, new String[]{String.valueOf(100)});

				if (c != null && !c.isClosed() && c.getCount() > 0) {
					for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
						HistoryRecord historyRecord = new HistoryRecord();
						historyRecord.setId(c.getInt(c.getColumnIndex(COL_ID)));
						historyRecord.setHistoryAddr(c.getString(c.getColumnIndex(COL_URL)));
						historyRecord.setHistoryTitle(c.getString(c.getColumnIndex(COL_TITLE)));
						historyRecord.setSource(c.getInt(c.getColumnIndex(COL_SRC)));
						historyRecord.setCount(0);
						historyRecord.setTs(new Date());
						HistoryRecordApi.getInstance(CustomOpenHelper.getInstance(context)).insert(historyRecord);
					}
				}

				if (c != null && !c.isClosed()) {
					c.close();
				}

				if (DB_TABLE_PATH != null){
					File file_db = new File(DB_TABLE_PATH);
					if (file_db.exists()) {
						file_db.delete();
					}
				}
			} catch (Exception e) {
				String exceptionStr = e.toString();
				SimpleLog.v("SQL", exceptionStr);
			} finally {
				if (c != null) {
					c.close();
				}
				if(db != null)
					db.close();
			}
		}
	}

	/**
	 * @desc 检查路径是否存在
	 * @author Hacken
	 * @time 2016/8/2 17:45
	 */
	public static boolean checkTableIsExist(String path){
		boolean isTableExist = false;
		if (path != null){
			File file_db = new File(path);
			if (file_db.exists()) {
				isTableExist = true;
			}
		}
		return isTableExist;
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */
	@SuppressLint("NewApi")
	public static String getPath(final Context context, final Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				final String selection = "_id=?";
				final String[] selectionArgs = new String[] {
						split[1]
				};
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}


	/**
	 * 获取应用专属缓存目录
	 * android 4.4及以上系统不需要申请SD卡读写权限
	 * 因此也不用考虑6.0系统动态申请SD卡读写权限问题，切随应用被卸载后自动清空 不会污染用户存储空间
	 * @param context 上下文
	 * @param type 文件夹类型 可以为空，为空则返回API得到的一级目录
	 * @return 缓存文件夹 如果没有SD卡或SD卡有问题则返回内存缓存目录，否则优先返回SD卡缓存目录
	 */
	public static File getCacheDirectory(Context context,String type) {
		File appCacheDir = getExternalCacheDirectory(context,type);
		if (appCacheDir == null){
			appCacheDir = getExternalCacheDirectory(context,type);
		}

		if (appCacheDir == null){
			SimpleLog.e("getCacheDirectory","getCacheDirectory fail ,the reason is mobile phone unknown exception !");
		}else {
			if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
				SimpleLog.e("getCacheDirectory","getCacheDirectory fail ,the reason is make directory fail !");
			}
		}
		return appCacheDir;
	}

	/**
	 * 获取SD卡缓存目录
	 * @param context 上下文
	 * @param type 文件夹类型 如果为空则返回 /storage/emulated/0/Android/data/app_package_name/cache
	 *             否则返回对应类型的文件夹如Environment.DIRECTORY_PICTURES 对应的文件夹为 .../data/app_package_name/files/Pictures
	 * {@link android.os.Environment#DIRECTORY_MUSIC},
	 * {@link android.os.Environment#DIRECTORY_PODCASTS},
	 * {@link android.os.Environment#DIRECTORY_RINGTONES},
	 * {@link android.os.Environment#DIRECTORY_ALARMS},
	 * {@link android.os.Environment#DIRECTORY_NOTIFICATIONS},
	 * {@link android.os.Environment#DIRECTORY_PICTURES}, or
	 * {@link android.os.Environment#DIRECTORY_MOVIES}.or 自定义文件夹名称
	 * @return 缓存目录文件夹 或 null（无SD卡或SD卡挂载失败）
	 */
	public static File getExternalCacheDirectory(Context context,String type) {
		File appCacheDir = null;
		if( Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			if (TextUtils.isEmpty(type)){
				appCacheDir = context.getExternalCacheDir();
			}else {
				appCacheDir = context.getExternalFilesDir(type);
			}

			if (appCacheDir == null){// 有些手机需要通过自定义目录
				appCacheDir = new File(Environment.getExternalStorageDirectory(),"Android/data/"+context.getPackageName()+"/cache/"+type);
			}

			if (!appCacheDir.exists()&&!appCacheDir.mkdirs()){
                SimpleLog.e("getExternalDirectory","getExternalDirectory fail ,the reason is make directory fail !");
            }
		}else {
			SimpleLog.e("getExternalDirectory","getExternalDirectory fail ,the reason is sdCard nonexistence or sdCard mount fail !");
		}
		return appCacheDir;
	}
	/**
	 * 获取内存缓存目录
	 * @param type 子目录，可以为空，为空直接返回一级目录
	 * @return 缓存目录文件夹 或 null（创建目录文件失败）
	 * 注：该方法获取的目录是能供当前应用自己使用，外部应用没有读写权限，如 系统相机应用
	 */
	public static File getInternalCacheDirectory(Context context,String type) {
		File appCacheDir = null;
		if (TextUtils.isEmpty(type)) {
			appCacheDir = context.getCacheDir();// /data/data/app_package_name/cache
		} else {
			appCacheDir = new File(context.getFilesDir(), type);// /data/data/app_package_name/files/type
		}

		if (!appCacheDir.exists() && !appCacheDir.mkdirs()) {
			SimpleLog.e("getInternalDirectory", "getInternalDirectory fail ,the reason is make directory fail !");
		}
		return appCacheDir;
	}

	/**
	 * 根据文件路径获得文件名称
	 * @param filePath
	 * @return
	 */
	public static String getFileNameByPath(String filePath){
		String name = null;
		try{
			name = filePath.substring(filePath.lastIndexOf("/") + 1);
			if(!name.contains(".")){
				name = null;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return name;

	}
	public static void saveFile(Bitmap bm, String path) throws IOException {
		File dirFile = new File(path);
		if(!dirFile.exists()){
			dirFile.mkdir();
		}
		File myCaptureFile = new File(path + Constants.JS_IMG_NAME);
		if (myCaptureFile.exists()) {
			deleteOnlyFile(myCaptureFile);
		}
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
		bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		bos.flush();
		bos.close();
	}


	public static void saveToJsonFile(File file, String key, ArrayList<String> urls) {
		for (String url: urls) {
			saveToJsonFile(file, key,url);
		}
	}

	private static void saveToJsonFile(final File file, String key, String url) {
		if (url == null) {
			return;
		}
		final String language = ApiUtil.getLan();
		final String area = ApiUtil.getArea();
		final String fileName = FileUtils.getFileName(file.toString());
		final JSONObject object = StringUtils.generateJson(key, url, language, area);
		if (null == object) {
			return;
		}
		ThreadManager.postTaskToIOHandler(new Runnable() {
			@Override
			public void run() {
				try {
					String keyWordString = SecurityUtil.DES_decrypt(readFile(file.toString()), STA_KEY);
					JSONObject obj;
					JSONArray jsonArray;
					if (TextUtils.isEmpty(keyWordString)) {
						obj = new JSONObject();
						obj.put("file", fileName);
						obj.put("language", language);
						obj.put("area", area);
						jsonArray = new JSONArray();
					} else {
						obj = new JSONObject(keyWordString);
						jsonArray = new JSONArray(obj.get("content").toString());
					}
					jsonArray.put(object);
					obj.put("content", jsonArray);
					keyWordString = SecurityUtil.DES_encrypt(obj.toString(), STA_KEY);
					byte[] data = keyWordString.getBytes();
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(data);
					fos.flush();
					fos.close();
				} catch (JSONException | IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	//从文件路径中获取文件名称，专用方法，慎用。
	private static String getFileName(String filePath) {
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
	}

	public static File getFileWithName(String givenName) {
		File wantedFile = new File(VCStoragerManager.getInstance().getTmpPath() + File.separator + givenName);
		if (!wantedFile.exists()) {
			try {
				wantedFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return wantedFile;
	}

	//上传指定文件到指定url  调用时抛到网络线程执行。
	public static void uploadFile(final File file, final String url, final Context context) {
		if (file.exists()) {
			try {
				byte[] fileBytes = FileUtils.readFile(file);
				final byte[] gzipInfoBytes = ZipUtil.gZip(fileBytes);
				if (gzipInfoBytes != null && gzipInfoBytes.length > 0) {

					ThreadManager.postTaskToNetworkHandler(new Runnable() {
						@Override
						public void run() {
							HttpClient httpClient = NetworkUtils.createHttpClient(NetworkUtils.getCurrentProxy(context));
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							int result = NetworkUtils.PostFormForUpload(httpClient, url, gzipInfoBytes, bos, null);
							if (result >= 0) { // 上传成功，需要把文件删除

								ThreadManager.postTaskToIOHandler(new Runnable() {
									@Override
									public void run() {
										FileUtils.deleteFileOrDirectory(file);
									}
								});
							}
						}
					});
				}
			} catch (Throwable e) {
			}
		}
	}

	public static List<File> getAllFiles(List<File> fileList, String jsDir) {
		File dir = new File(jsDir);
		File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i].getName();
				if (files[i].isDirectory()) { // 判断是文件还是文件夹
					getAllFiles(fileList, files[i].getAbsolutePath()); // 获取文件绝对路径
				} else if (fileName.endsWith("json")) { // 判断文件名是否以.avi结尾
					fileList.add(files[i]);
				} else {
					continue;
				}
			}

		}
		return fileList;
	}

	/**
	 * 写入文件
	 *
	 * @param in
	 * @param file
	 */
	public synchronized static void writeFile(InputStream in, File file) throws IOException {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();

		if (file != null && file.exists())
			file.delete();

		FileOutputStream out = new FileOutputStream(file);
		byte[] buffer = new byte[1024 * 128];
		int len = -1;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		out.flush();
		out.close();
		in.close();
	}

	public static boolean moveFileByRename(File fromFile,String toPath){
		try{
			if(fromFile != null && toPath != null){
				if(fromFile.renameTo(new File(toPath))){
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
}
