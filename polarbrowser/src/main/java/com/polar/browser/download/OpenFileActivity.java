package com.polar.browser.download;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.polar.browser.R;
import com.polar.browser.base.LemonBaseActivity;
import com.polar.browser.common.ui.CommonTitleBar;
import com.polar.browser.common.ui.LoadingDialog;
import com.polar.browser.database.MediaDBRefreshHelper;
import com.polar.browser.download.uncompress.UncompressFolderAdapter;
import com.polar.browser.download.uncompress.UncompressInfo;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.utils.OpenFileUtils;
import com.polar.browser.utils.QueryUtils;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.ToastDialog;
import com.polar.browser.utils.UncompressPrefs;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.Zip4jUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class OpenFileActivity extends LemonBaseActivity implements
		OnClickListener{

	private static final String TAG = "OpenFileActivity";
	private static final int COMPRESSTYPE = 4;
	private TextView mTvTitle;
	private String mFileAbsPath;
	private Intent intent;
	private CommonTitleBar mTitle;
	private TextView mIvMore;
	private ImageView mIvBack;
	private ListView mLvDecompress;
	private UncompressFolderAdapter uncompressFolderAdapter;
	private List<UncompressInfo> mZipFileNameList;
	private String postfix;
	private String mTitleFileName;
	private String mDownloadRootPath;
	private ZoomImageView zoomImageView;
	private int mInSampleSize = 1;
	private String type;
	private String mDestRootPath;
	private LoadingDialog loadingDialog;
	private ToastDialog toastDialog;
	private static final String DEFAULT_SAVE_DIR = "temp/";
	private String tempSavePath;

	//压缩包打开方式，自动 或 手动
	public static final String ACTION_OPEN_FILE = "ACTION_OPEN_FILE";
	public static final String OPEN_AUTO = "OPEN_AUTO";
	public static final String OPEN_MANUAL = "OPEN_MANUAL";
	private String openAction = OPEN_MANUAL;  //默认为手动解压

	private Intent alreadyLoadIntent = null;

	/**
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		intent = getIntent();
		if(intent != null){
			if(alreadyLoadIntent == intent){
				return;
			}
			alreadyLoadIntent = intent;
			String type = intent.getStringExtra(OpenFileUtils.TYPE_KEY);
			if (type != null) {
				getDataByType(type);
			} else {
				onBackPressed();
				return;
			}
		}
	}

	private void getDataByType(String type) {
		if (type.equalsIgnoreCase(OpenFileUtils.TYPE_COMPRESS)) {
			initView();
			initDecompressData(type);
		} else if (type.equalsIgnoreCase(OpenFileUtils.TYPE_PICTURE)) {
			initImageData();
		}
	}

	private void initView() {
		setContentView(R.layout.activity_open_file);
		mTitle = (CommonTitleBar) findViewById(R.id.title_bar);
		mIvMore = (TextView) mTitle.findViewById(R.id.common_tv_setting);
		mIvMore.setText(getResources().getString(R.string.extract));
		mIvMore.setVisibility(View.VISIBLE);
/*		mIvMore.setBackgroundResource(0);
		mIvMore.setImageResource(R.drawable.common_title_bar_more);*/
		mTvTitle = (TextView) mTitle.findViewById(R.id.common_tv_title);
		mTvTitle.setVisibility(View.VISIBLE);
		mIvBack = (ImageView) mTitle.findViewById(R.id.common_img_back);
		mLvDecompress = (ListView) findViewById(R.id.lv_decompress);
		mIvBack.setOnClickListener(this);
		mIvMore.setOnClickListener(this);
		initDecompressView( );
	}

	private void initDecompressView( ) {
		uncompressFolderAdapter = new UncompressFolderAdapter(getApplicationContext(),null,null);
		mLvDecompress.setAdapter(uncompressFolderAdapter);
		mLvDecompress.setVisibility(View.VISIBLE);
		initTempSavePath();  //初始化临时保存路径
		mLvDecompress.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				UncompressInfo info = (UncompressInfo)uncompressFolderAdapter.getItem(position);
				SimpleLog.d(TAG,"onItemClick  info = "+info.toString());
				if(info.getType() == UncompressInfo.TYPE_FILE){  //文件类型

					if(info.getPath() != null){  //文件已解压则直接打开
						if(OpenFileUtils.checkEndsWithInStringArray(info.getPath(), getResources()
								.getStringArray(R.array.fileEndingImage)) ){
							intent = new Intent(OpenFileActivity.this, ImageGalleryActivity.class);
							intent.putStringArrayListExtra("imagePaths", (ArrayList<String>) uncompressFolderAdapter.getAllImagePaths());
							intent.putExtra("position", uncompressFolderAdapter.getAllImagePaths().indexOf(info.getPath()));
							startActivity(intent);
							return;
						}
						OpenFileUtils.openFile(new File(info.getPath()), OpenFileActivity.this.getApplication());
					}
					else if(info.getName() != null){  //文件未解压，则解压单一文件并打开

						final String fileName = info.getName();
						if (postfix != null && postfix.equalsIgnoreCase("zip")) {
							ThreadManager.postTaskToIOHandler(new Runnable() {
								@Override
								public void run() {
									final String filePath = unZipItem(fileName);
									ThreadManager.postTaskToUIHandler(new Runnable() {
										@Override
										public void run() {
											if(filePath != null){
												String newFileName = fileName;
												if(fileName.lastIndexOf('/') != -1){   //文件名可能是一个路径
													newFileName = fileName.substring(fileName.lastIndexOf('/'),fileName.length());
												}
												File file = new File(tempSavePath,newFileName);
												if(file !=null && file.exists() && file.isFile()){
													OpenFileUtils.openFile(file, OpenFileActivity.this);
												}else{
													CustomToastUtils.getInstance().showTextToast(R.string.openfile_no_exist);
												}
											}else{
												CustomToastUtils.getInstance().showTextToast(R.string.openfile_error);  //打开文件失败
											}
										}
									});
								}
							});
						} else if (postfix != null && postfix.equalsIgnoreCase("rar")) {
							ThreadManager.postTaskToIOHandler(new Runnable() {
								@Override
								public void run() {
									final String filePath = unRarSingleFile(mFileAbsPath,fileName,tempSavePath);
									ThreadManager.postTaskToUIHandler(new Runnable() {
										@Override
										public void run() {
											if(filePath != null){
												File file = new File(tempSavePath,fileName);
												if(file !=null && file.exists() && file.isFile()){
													OpenFileUtils.openFile(file, OpenFileActivity.this);
												}else{
													CustomToastUtils.getInstance().showTextToast(R.string.openfile_no_exist);
												}
											}else{
												CustomToastUtils.getInstance().showTextToast(R.string.openfile_error);  //打开文件失败
											}
										}
									});
								}
							});
						}
					}else {
						CustomToastUtils.getInstance().showTextToast(R.string.openfile_no_exist);
					}
				}
			}
		});
	}

	private String unZipItem(String fileName){
		String filePath = null;
		ZipFile zf = null;
		net.lingala.zip4j.model.FileHeader fileHeader = null;

		String tempSaveDir = tempSavePath;
		try {
			zf = new ZipFile(mFileAbsPath);
			zf.setFileNameCharset("GBK");
			fileHeader = zf.getFileHeader(fileName);
			String newFileName = getSimpleFileName(fileName);
			File newDir = new File(tempSaveDir);
			//删除重复文件
			if(newDir!=null && newDir.exists()){
				File newFile = new File(tempSaveDir,newFileName);
				if(newFile!=null && newFile.exists() && newFile.isFile() ) newFile.delete();
			}

			zf.extractFile(fileHeader,tempSaveDir,null,newFileName);  //解压被点击文件
			filePath = new File(newDir,newFileName).getPath();
			SimpleLog.d(TAG,"tempSaveDir = "+tempSaveDir+" fileName = "+fileName+" newFileName = "+newFileName);

		} catch (ZipException e) {
			e.printStackTrace();
			CustomToastUtils.getInstance().showTextToast(R.string.openfile_error);  //打开文件失败
		}
		return filePath;
	}

	/**
	 * 保存临时解压文件，退出后删除
	 */
	private void initTempSavePath(){
		String defaultDownloadPath = VCStoragerManager.getInstance().getPhoneStorage();
		if(defaultDownloadPath == null || defaultDownloadPath.isEmpty()){
			defaultDownloadPath = Environment.getExternalStorageDirectory().toString();
		}
		if(!defaultDownloadPath.endsWith("/")){
			defaultDownloadPath = defaultDownloadPath+"/";
		}
		tempSavePath = defaultDownloadPath + DEFAULT_SAVE_DIR;
	}

	private void removeTempSavePath(){
		FileUtils.deleteFileOrDirectory(new File(tempSavePath));
	}

	private void initDecompressData(String type) {
		mFileAbsPath = intent.getStringExtra(OpenFileUtils.FILE_PATH);
		openAction = intent.getStringExtra(ACTION_OPEN_FILE);  //获取打开方式
		if (mFileAbsPath == null) {
			showMsg(getString(R.string.openfile_no_exist), true);
			return;
		}
		if(openAction == null){
			openAction = OPEN_MANUAL;  //默认手动解压
		}
		SimpleLog.d(TAG, mFileAbsPath);
		UncompressPrefs.put(mFileAbsPath,false);  //记录曾经解压过的文件。
		UncompressPrefs.apply();
		boolean IsError = ConfigWrapper.get(mFileAbsPath, false);
		SimpleLog.d(TAG, "IsError = "+IsError + "");
/*		if (IsError) {    //TODO  有什么作用？
			openFileWithDefault(true);
			return;
		}*/
		int lastIndexOf = mFileAbsPath.lastIndexOf('.');
		if (lastIndexOf > 0) {
			postfix = mFileAbsPath.substring(lastIndexOf + 1);
		}
		SimpleLog.d(TAG, "后缀名" + postfix);
		mDownloadRootPath = getRootPath(mFileAbsPath);
		mTitleFileName = getRealFileName(type, mFileAbsPath, mDownloadRootPath);
		mTvTitle.setText(mTitleFileName);
		mDestRootPath = mDownloadRootPath + File.separator
				+ mTitleFileName;
		Runnable r = new Runnable() {

			@Override
			public void run() {
				if (postfix != null && postfix.equalsIgnoreCase("zip")) {
					getZipFileList(mFileAbsPath);
				} else if (postfix != null && postfix.equalsIgnoreCase("rar")) {
					getRarFileList(mFileAbsPath);
				}
				if(OPEN_AUTO.equals(openAction)){
					doUncompress();
				}
			}
		};
		ThreadManager.postTaskToIOHandler(r);
	}

	private void initImageData() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_open_file);
		zoomImageView = (ZoomImageView) findViewById(R.id.zoom_image_view);

		mFileAbsPath = intent.getStringExtra(OpenFileUtils.FILE_PATH);
		if (mFileAbsPath == null) {
			showMsg(getString(R.string.openfile_no_exist), true);
			return;
		}
		zoomImageView.setVisibility(View.VISIBLE);
		int mScreenWidth = AppEnv.SCREEN_WIDTH;
		int mScreenHeight = AppEnv.SCREEN_HEIGHT;
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mFileAbsPath, options);
		options.inSampleSize = calculateInSampleSize(options, mScreenWidth,
				mScreenHeight);
		int insample = options.inSampleSize;
		SimpleLog.d(TAG, insample + "");
		if (options.mCancel || options.outWidth == -1
				|| options.outHeight == -1) {
			showMsg(getString(R.string.openfile_image_error), false); // 表示图片已损毁
			ConfigWrapper.put(mFileAbsPath, true);
			ConfigWrapper.apply();
			openFileWithDefault(true);
			return;
		}
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(mFileAbsPath, options); // filePath代表图片路径
		if (bitmap == null) {
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		} else {
			zoomImageView.setImageBitmap(bitmap);
		}
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
									  int screenWidth, int screenHeight) {
		int mImageWidth = options.outWidth;
		int mImageHeight = options.outHeight;
		SimpleLog.d(TAG, "mImageWidth" + mImageWidth + "");
		SimpleLog.d(TAG, "mImageHeight" + mImageHeight + "");
		while (mImageWidth / mInSampleSize > screenWidth
				|| mImageHeight / mInSampleSize > screenHeight) {
			mInSampleSize++;
		}
		return mInSampleSize;
	}

	private void updataCompressData(final List<UncompressInfo> infoList) {

		ThreadManager.postTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				uncompressFolderAdapter.changeData(infoList);
			}
		});
	}

	/**
	 *
	 * @param srcFilePath
	 * @param destPath
	 * @return
	 */
	private boolean unzip(String srcFilePath, String destPath) {
		boolean result = false;
		if(srcFilePath == null || destPath == null) return result;
		try {
			FileUtils.deleteFileOrDirectory(new File(destPath));  //删除已有文件
			ZipFile zipFile = new ZipFile(
					srcFilePath);
			zipFile.setFileNameCharset("GBK");
//			zipFile.setFileNameCharset("UTF-8");
			if (!zipFile.isValidZipFile()) {
				SimpleLog.d(TAG,"unzip isInvalid zip file");
				saveFileErrorConfig(mFileAbsPath, true);
				openFileWithDefault(true);
				return result;
			}
			if (zipFile.isEncrypted()) {
				showPasswordDialog(zipFile, destPath, 1);
			} else {
				zipFile.extractAll(destPath);
				SimpleLog.d(TAG, "解压完成");
				MediaDBRefreshHelper.getInstance(getApplicationContext()).insertDir(destPath);
				result = true;
			}
		} catch (ZipException e) {
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
			SimpleLog.e(e);
		}
		return result;
	}

	private void showPasswordDialog(
			final ZipFile zipfile,
			final String destPath, int tag) {
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(R.layout.item_dialog_password, null);
		final EditText et = (EditText) view.findViewById(R.id.dialog_password);
		new AlertDialog.Builder(this).setView(view)
				.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String mPassword = et.getText().toString();
						unZipEncrypted(zipfile, destPath, mPassword);
					}
				})
				.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						onBackPressed();
					}
				}).show();
	}

	private void unZipEncrypted(ZipFile zipfile,
								String destPath, String password) {
		try {
			SimpleLog.d(TAG, password);
			zipfile.setPassword(password);
			zipfile.extractAll(destPath);
//			SimpleLog.d(TAG, "加密解压完成");
		} catch (ZipException e) {
			SimpleLog.e(e);
			// openFileWithDefault(true);
			saveFileErrorConfig(mFileAbsPath, true);
			showMsg(getString(R.string.openfile_decompress_error), true);
			SimpleLog.e(TAG, e.toString());
		} catch (Exception e) {
			SimpleLog.d(TAG, "加密解压失败");
			showMsg(getString(R.string.openfile_decompress_error), true);
			saveFileErrorConfig(mFileAbsPath, true);
			// showPasswordDialog(zipfile, destPath, -1);
		}
	}

	private void getZipFileList(String SrcFileName) {
		if (mZipFileNameList != null && mZipFileNameList.size() != 0) {
			mZipFileNameList.clear();
		}
		mZipFileNameList = new ArrayList<UncompressInfo>();
		try {
			ZipFile zipfile = new ZipFile(
					SrcFileName);
			zipfile.setFileNameCharset("GBK");
			if (!zipfile.isValidZipFile()) {    //TODO  优化，文件为空也会走这里
				SimpleLog.d(TAG,"unzip isInvalid zip file");
				saveFileErrorConfig(mFileAbsPath, true);
				openFileWithDefault(true);
				return;
			}

			if (zipfile.isEncrypted()) {
				showPasswordDialog(zipfile, mDestRootPath, 1);    //TODO 获取加密文件列表
			}else{
				List<?> mFileList = zipfile.getFileHeaders();

				for (int i = 0; i < mFileList.size(); i++) {
					final net.lingala.zip4j.model.FileHeader header = (net.lingala.zip4j.model.FileHeader) mFileList
							.get(i);
					UncompressInfo info = new UncompressInfo();
					if(header.isDirectory()){
						info.setType(UncompressInfo.TYPE_DIR);  //不显示目录，因为目录中的文件已经全部在mFileList中了
						continue;
					}else{
						if(OpenFileUtils.checkEndsWithInStringArray(header.getFileName(),getResources()
								.getStringArray(R.array.fileEndingImage))){  //如果是图片文件,解压单个图片
							String newFileName = getSimpleFileName(header.getFileName());
							File file = new File(tempSavePath,newFileName);
							info.setPath(file.getPath());  //提前将文件路径赋值，解压是异步的
							ThreadManager.postTaskToIOHandler(new Runnable() {
								@Override
								public void run() {
									unZipItem(header.getFileName());  //解压文件  耗时
								}
							});
						}
						info.setType(UncompressInfo.TYPE_FILE);
					}
					info.setName(header.getFileName());
					info.setDate(Zip4jUtil.dosToJavaTme(header.getLastModFileTime()));
					info.setSize(header.getUncompressedSize());  //被解压之后的大小
					mZipFileNameList.add(info);
				}
			}
		}
		catch (ZipException e) {
			// ZipException: Expected central directory entry not found (#1) code=-1
			// IOException: Probably not a zip file or a corrupted zip file  code=4
			if(e.getCode() == 4 || e.getCode() == -1){
				CustomToastUtils.getInstance().showTextToast(R.string.openfile_no_exist);
			}
			SimpleLog.e(e);
		}
		updataCompressData(mZipFileNameList);
	}

	public String getSimpleFileName(String fileName){
		if(fileName == null) return null;
		try{
			if(fileName.lastIndexOf('/') != -1){   //文件名可能是一个路径
				fileName = fileName.substring(fileName.lastIndexOf('/'),fileName.length());
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return fileName;
	}

	private void getRarFileList(String srcFileName){
		File srcFile = new File(srcFileName);
		if (!srcFile.isFile() || !srcFile.exists()) {
			return;
		}
		Archive rarfile = null;
		if (mZipFileNameList != null && mZipFileNameList.size() != 0) {
			mZipFileNameList.clear();
		}
		mZipFileNameList = new ArrayList<UncompressInfo>();
		try {
			rarfile = new Archive(srcFile);
			FileHeader fh = rarfile.nextFileHeader();
			while (fh != null) {
				try{
					String entrypath = "";
					if (fh.isUnicode()) {// 解決中文乱码
						entrypath = fh.getFileNameW().trim();
					} else {
						entrypath = fh.getFileNameString().trim();
					}
					entrypath = entrypath.replaceAll("\\\\", "/");
					UncompressInfo info = new UncompressInfo();
					if(fh.isDirectory()){
						info.setType(UncompressInfo.TYPE_DIR);  //不显示目录，因为目录中的文件已经全部在mFileList中了
						fh = rarfile.nextFileHeader();
						continue;
					}else{
						if(OpenFileUtils.checkEndsWithInStringArray(entrypath,getResources()
								.getStringArray(R.array.fileEndingImage))){  //如果是图片文件
							final File file = new File(tempSavePath,entrypath);
							ThreadManager.postTaskToIOHandler(new Runnable() {
								@Override
								public void run() {
									unRarSingleFile(mFileAbsPath,file.getName(),tempSavePath);  //解压文件  耗时
								}
							});
							info.setPath(file.getPath());
						}
						info.setType(UncompressInfo.TYPE_FILE);
					}
					info.setName(entrypath);
					info.setSize(fh.getUnpSize());
					info.setDate(fh.getMTime()!=null ? fh.getMTime().getTime() : new Date().getTime());
					mZipFileNameList.add(info);
					fh = rarfile.nextFileHeader();
				}catch (Exception e){
					e.printStackTrace();
					fh = rarfile.nextFileHeader();
					continue;
				}
			}
			updataCompressData(mZipFileNameList);
			rarfile.close();
		} catch (RarException e) {
			SimpleLog.e(e);
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		} catch (Exception e) {
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		} catch (Error e) {
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		} finally {
			if (rarfile != null) {
				try {
					rarfile.close();
					rarfile = null;
				} catch (Exception e) {
					SimpleLog.e(e);
				}
			}
		}
	}

	private String getRootPath(String SrcFilePath) {
		String mRootPath = new File(mFileAbsPath).getParentFile()
				.getAbsolutePath();
		SimpleLog.d(TAG, "下载目录" + mRootPath);
		return mRootPath;
	}

	private String getRealFileName(String type, String srcfilepath,
								   String downloadpath) {
		if (type.equalsIgnoreCase(OpenFileUtils.TYPE_COMPRESS)) {
			if (srcfilepath.length() - COMPRESSTYPE > 0
					&& srcfilepath.length() - COMPRESSTYPE > mDownloadRootPath
					.length()) {
				String mFileTitle = srcfilepath.substring(
						mDownloadRootPath.length() + 1, srcfilepath.length()
								- COMPRESSTYPE);
				return mFileTitle;
			}
		}
		return srcfilepath;
	}

	private boolean unrar(String srcPath, String destpath) {
		boolean result = false;
		try {
			File srcFile = new File(srcPath);
			if (!srcFile.isFile() || !srcFile.exists()) {
				return result;
			}
			if (null == destpath || "".equals(destpath)) {
				destpath = srcFile.getParentFile().getPath();
			}
			// 保证文件夹路径最后是"/"或者"\"
			char lastChar = destpath.charAt(destpath.length() - 1);
			if (lastChar != '/' && lastChar != '\\') {
				destpath += File.separator;
			}
			result = unrar(srcFile, destpath);
		} catch (Exception e) {
			SimpleLog.e(e);
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		}
		return result;
	}

	private boolean unrar(File srcFile, String destPath) {
		boolean result = false;
		FileOutputStream fileOut = null;
		Archive rarfile = null;
		try {
			FileUtils.deleteFileOrDirectory(new File(destPath));  //删除已有文件
			rarfile = new Archive(srcFile);
			FileHeader fh = rarfile.nextFileHeader();
			while (fh != null) {
				String entrypath = "";
				if (fh.isUnicode()) {// 解決中文乱码
					entrypath = fh.getFileNameW().trim();
				} else {
					entrypath = fh.getFileNameString().trim();
				}
				entrypath = entrypath.replaceAll("\\\\", "/");
				File file = new File(destPath + entrypath);
				if (fh.isDirectory()) {
					file.mkdirs();
				} else {
					File parent = file.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}
					fileOut = new FileOutputStream(file);
					rarfile.extractFile(fh, fileOut);  //解压
					fileOut.flush();
					fileOut.close();
				}
				fh = rarfile.nextFileHeader();
			}
			SimpleLog.d(TAG,"unrar MediaDBRefreshHelper -- ");
			MediaDBRefreshHelper.getInstance(getApplicationContext()).insertDir(destPath);
			result = true;
			rarfile.close();
		} catch (RarException e) {
			SimpleLog.e(e);
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		} catch (Exception e) {
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		} catch (Error e) {
			saveFileErrorConfig(mFileAbsPath, true);
			openFileWithDefault(true);
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
					fileOut = null;
				} catch (Exception e) {
					SimpleLog.e(e);
				}
			}
			if (rarfile != null) {
				try {
					rarfile.close();
					rarfile = null;
				} catch (Exception e) {
					SimpleLog.e(e);
				}
			}
		}
		return result;
	}

	private String unRarSingleFile(String srcPath,String fileName, String destPath) {
		String filePath = null;
		if(srcPath == null || fileName == null || destPath == null ) return filePath;
		FileOutputStream fileOut = null;
		Archive rarfile = null;
		try {
			rarfile = new Archive(new File(srcPath));
			FileHeader fh = null;
			List<FileHeader> headerList = rarfile.getFileHeaders();
			if(headerList == null) return filePath;
			String entrypath = null;
			for (FileHeader header : headerList){
				if (header.isUnicode()) {// 解決中文乱码
					entrypath = header.getFileNameW().trim();
				} else {
					entrypath = header.getFileNameString().trim();
				}
				entrypath = entrypath.replaceAll("\\\\", "/");
				if(fileName.equals(entrypath)){
					fh = header;
					break;
				}
				entrypath = null;
			}

			if (fh != null) {
				File file = new File(destPath + entrypath);
				if (fh.isDirectory()) {
					return filePath;
				} else {
					File parent = file.getParentFile();
					if (parent != null && !parent.exists()) {
						parent.mkdirs();
					}
					fileOut = new FileOutputStream(file);
					rarfile.extractFile(fh, fileOut);  //解压
					fileOut.flush();
					fileOut.close();
				}
				filePath = file.getPath();
			}
			rarfile.close();
		} catch (RarException e) {
			SimpleLog.e(e);
		} catch (Exception e) {
			SimpleLog.e(e);
		} catch (Error e) {
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
					fileOut = null;
				} catch (Exception e) {
					SimpleLog.e(e);
				}
			}
			if (rarfile != null) {
				try {
					rarfile.close();
					rarfile = null;
				} catch (Exception e) {
					SimpleLog.e(e);
				}
			}
		}
		return filePath;
	}

	private void saveFileErrorConfig(String filePath, boolean isError) {
		ConfigWrapper.put(filePath, isError);
		ConfigWrapper.apply();
	}

	private void showMsg(final String msg, boolean IsBack) {
		ThreadManager.postTaskToUIHandler(new Runnable() {

			@Override
			public void run() {
				CustomToastUtils.getInstance().showTextToast(msg);
			}
		});
		if (IsBack) {
			onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.slide_in_from_left,
				R.anim.slid_out_to_right);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.common_img_back:
				onBackPressed();
				break;
			case R.id.common_tv_setting:
				doUncompress();
				break;
			default:
				break;
		}
	}

	private void doUncompress(){

		ThreadManager.postTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				showLoadingDialog();
			}
		});
		Runnable r = new Runnable() {
			@Override
			public void run() {
				boolean result = false;
				if (postfix != null && postfix.equalsIgnoreCase("zip")) {
					result = unzip(mFileAbsPath, mDestRootPath);
				} else if (postfix != null && postfix.equalsIgnoreCase("rar")) {
					result = unrar(mFileAbsPath, mDestRootPath);
				}
				final boolean uncompressResult = result;
				ThreadManager.postTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						dismissLoadingDialog();
						if(uncompressResult){
							showCustomToast();
							QueryUtils.notifyFileCountChanged(Constants.TYPE_ALL);  //通知数量改变
						}else{
							showMsg(getString(R.string.openfile_decompress_error), true);
							saveFileErrorConfig(mFileAbsPath, true);
						}
					}
				});
			}
		};
		ThreadManager.postTaskToIOHandler(r);
	}

	private void showLoadingDialog(){
		if(loadingDialog == null){
			loadingDialog = new LoadingDialog(this);
		}
		loadingDialog.show();
	}
	private void dismissLoadingDialog(){
		if(loadingDialog != null){
			loadingDialog.dismiss();
		}
	}

	private void showCustomToast(){
		CustomToastUtils.getInstance().showClickToast(this,
				getResources().getString(R.string.openfile_decompress_success),
				getResources().getString(R.string.extract_view_now),
				ToastDialog.DEFAULT_SHOW_TIME_LONG,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(OpenFileActivity.this, DecompresstionFolderActivity.class);
						startActivity(intent);
						finish();
					}
				});
	}

	private void openFileWithDefault(boolean back) {
		OpenFileUtils.openFileWithDefault(mFileAbsPath, this);
		if (back) {
			onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SimpleLog.d(TAG,"onDestroy -- ");
		removeTempSavePath();
	}
}

