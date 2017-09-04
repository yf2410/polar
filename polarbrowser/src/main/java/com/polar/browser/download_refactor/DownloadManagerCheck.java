package com.polar.browser.download_refactor;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.download.DownloadAlertDialog;
import com.polar.browser.download_refactor.dialog.DownloadDialog;
import com.polar.browser.download_refactor.dialog.DownloadNetChangeDialog;
import com.polar.browser.download_refactor.netstatus_manager.MoblieAllowDownloads;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.download_refactor.util.SmartDecode;
import com.polar.browser.download_refactor.util.URLUtilities;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.SDCardUtils;
import com.polar.browser.utils.SimpleLog;

import java.io.File;

public class DownloadManagerCheck {
    private static final String TAG = "DownloadManagerCheck";

    private static final long MIN_STORAGE_SIZE = 60 * 1024 * 1024;
    private Context mContext;
    private Request mRequest;
    private String mUri;
    String mFilename;
    String mContentDisposition;
    String mMimetype;
    long mContentLength;
    String mReferer;

    String mCustomFolder;

    boolean mNeedConfirm = false;
    boolean mPause = false;
    boolean mMobileWork = false;
    String mUserAgent;

    private static DownloadManagerCheck sInstance;

    public DownloadManagerCheck(Context context,
                                Request request, String userAgent,
                                String uri, String mimetype, String contentDisposition, long contentLength, String referer,
                                boolean needConfirm) {
        mContext = context;
        mRequest = request;
        mUserAgent = userAgent;
        mUri = uri;
        mMimetype = mimetype;
        mReferer = referer;
        mContentDisposition = contentDisposition;
        mContentLength = contentLength;

        mFilename = getFileName();
        mCustomFolder = PathResolver.getDownloadFileDir("");

        mNeedConfirm = needConfirm;

        sInstance = this;
    }

    /**
     * TODO Fix me 伪单例，方便弹窗。后期有时间更改弹框的实现逻辑,去除该单例
     * @return
     */
    public static DownloadManagerCheck getInstance() {
        return sInstance;
    }

    /**
     * 释放引用,当确认下载,或者取消下载时
     */
    public void destory() {
        sInstance = null;
    }

    public void check() {
        if (TextUtils.isEmpty(mUri)) {
            SimpleLog.e(TAG, "params == null or url is empty!");
            return;
        }

        // 先进行一系列检查，再创建
//        ThreadUtils.postOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//            	// TODO Fix me
//                if (!checkExists())
//                    return;
////                if (!checkDirCanWrite())
////                    return;
//                if (!checkStorageSpace(mContext, mCustomFolder, mContentLength))
//                    return;
//                if (!checkWifi())
//                    return;
//                confirmDownload();
//            }
//        });

        ThreadManager.postTaskToUIHandler(new Runnable() {
            @Override
            public void run() {
                if (!checkExists())
                    return;
//                if (!checkDirCanWrite())
//                    return;
                if (!checkStorageSpace(mContext, mCustomFolder, mContentLength))
                    return;
                if (!checkWifi())
                    return;
                confirmDownload();
            }
        });
    }

    /**
     * 询问用户是否确认要下载：
     * 确认，则添加下载；否则取消下载
     * 会在{@link DownloadDialog}中重入使用，跳出递归的条件为mTaskParams.needConfirm == false
     */
    public void confirmDownload() {
    	// TODO
        if (mNeedConfirm) {
//            new ConfirmDownloadDialog(this, mFilename).showDialog();

            //防止有相同文件名
            String filename = PathResolver.getUniqueName(mFilename, mCustomFolder);
            mFilename = filename;

            Intent intent = new Intent(mContext, DownloadDialog.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra("filename", mFilename);
            intent.putExtra("customFolder", mCustomFolder);
            intent.putExtra("contentLength", mContentLength);
            intent.putExtra("userAgent", mUserAgent);
            if (mContext instanceof ContextWrapper) {
                ((ContextWrapper) mContext).startActivity(intent);
            }
            return;
        }

        if (mPause) {
            mRequest.setAutoStart(false);
        } else {
//            SmartToast.showShort(mContext, R.string.s_download_pending);
//        	CustomToastUtil.getInstance().showDurationToast("R.string.s_download_pending");
        }

        do_download();
        destory();
    }

    /**
     * 设置是否需要再次弹出下载确认框
     * @param needConfirm
     */
    public void setNeedConfirm(boolean needConfirm) {
        this.mNeedConfirm = needConfirm;
    }

    /**
     * 设置下载文件名
     * @param filename
     */
    public void setFileName(String filename) {
        this.mFilename  = filename;
    }

    /**
     * 设置是否允许在手机网络下下载
     * @param mobileWork
     */
    public void setMobileWork(boolean mobileWork) {
        this.mMobileWork = mobileWork;
    }

    /**
     * 检查网络环境：
     * 如果是wifi，则继续；
     * 如果不是wifi，则询问用户是否仍然要下载，是则标志为立即开始下载，否则标志为不立即开始下载
     * @return
     */
    private boolean checkWifi() {
        // 设置无所谓网络类型可以不用检查网络
//        if (!SettingsModel.getInstance().getWifiDownloadState()) {
//            mMobileWork = true;
//            return true;
//        }

        // 设置无所谓网络类型可以不用检查网络
        if (!DownloadManager.getInstance().isOnlyWifiDownload) {
            return true;
        }

        if (NetWorkUtils.isWifiConnected(JuziApp.getAppContext())) {
            return true;
        }
//        new CheckWifiDialog(this).showDialog();
        Intent intent = new Intent(mContext, DownloadNetChangeDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (mContext instanceof ContextWrapper) {
            ((ContextWrapper) mContext).startActivity(intent);
        }

        return false;
    }

    /**
     * 检查要下载的url是否已存在：
     * 如果未下载，则继续；
     * 如果已下载，则提示用户是否仍然要下载，用户选择继续则继续，否则取消本次下载
     * @return
     */
    private boolean checkExists() {
        DownloadItemInfo downloadItem = DownloadManager.getInstance().getDownloadItem(mUri);
        if (downloadItem == null)
            return true;
        if (downloadItem.mStatus == UiStatusDefine.STATUS_SUCCESSFUL
                || downloadItem.mStatus == UiStatusDefine.STATUS_FAILED) {
            File file = new File(downloadItem.mFilePath);
            if (!file.exists()) {
                DownloadManager.getInstance().deleteDownload(new long[]{downloadItem.mId}, false);
                return true;
            }
        }
        String[] segments = null;
        String filename = null;
        if (downloadItem.mFilePath != null) {
            segments = downloadItem.mFilePath.split("/");
        }
        String parameter = null;
        if (segments.length > 0) {
            filename = segments[segments.length - 1];
            parameter = filename;
        } else {
            parameter = mFilename;
        }
        // 20160818 duanwenqiang 不再弹出文件已存在的弹框,直接用(*)来给重名文件命名
        return true;
        // TODO Fix me
//        new CheckExistsDialog(this, parameter).showDialog();
//        return false;
//        CustomToastUtil.getInstance().showDurationToast(parameter + " 存在，但继续下载，后续弹框提示");

//        Intent intent = new Intent(mContext, DownloadFileExistsDialog.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (mContext instanceof ContextWrapper) {
//            ((ContextWrapper) mContext).startActivity(intent);
//        }
//        return false;
    }

    /**
     * 文件重名检查后的后续检查
     */
    public void checkAfterFileExists() {
        // 继续检查
//        if (!checkDirCanWrite())
//            return;
        if (!checkStorageSpace(mContext, mCustomFolder, mContentLength))
            return;
        if (!checkWifi())
            return;
        confirmDownload();
    }

    /**
     * 检查存储区空间是否足够：
     * 如果充足，则继续；
     * 如果不充足，则提示用户存储空间不足，取消本次下载
     * @return
     */
	public static boolean checkStorageSpace(Context context, String dir, long fileSize) {
		if (fileSize <= 0)
			return true;
//		long savePartitionFreeSpace = DownloadUtil.getAvailaleSize(dir);
        // TODO 2060807 Fix me 暂时判断为SD和手机的剩余空间总和，因为 部分手机的SD和手机存储区分暂时有问题，之后优化
		long savePartitionFreeSpace = SDCardUtils.getSDFreeSize();
        // 20160908 对存储空间进行判断,超过当前最小容量限制MIN_STORAGE_SIZE,才允许下载
        if (savePartitionFreeSpace > MIN_STORAGE_SIZE) {
            return true;
        }
		if (fileSize < savePartitionFreeSpace - MIN_STORAGE_SIZE)
			return true;
		// new CheckStorageSpaceDialog(context).showDialog();
		Intent intent = new Intent(context, DownloadAlertDialog.class);
		intent.putExtra("message", context.getString(R.string.download_no_available_space));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (context instanceof ContextWrapper) {
			((ContextWrapper) context).startActivity(intent);
		}
        Statistics.sendOnceStatistics(
                GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_SD_NOSPACE);
		return false;
	}

    /**
     * 检查下载目录是否可写：
     * 如果可写，则继续向下检查；
     * 如果不可写，则提示用户修改下载目录，用户修改下载目录成功后会重试下载，否则取消本次下载。
     * @return
     */
//    private boolean checkDirCanWrite() {
//        mCustomFolder = PathResolver.getDownloadFileDir(mCustomFolder);
//        String taskSaveDir = mCustomFolder;
//        try {
//            // 目标目录可写
//            if (DownloadFileUtils.isDirPathCanWrite(taskSaveDir, null, true))
//                return true;
//        } catch (DownloadException e) {
//            // android上应少对抗，暂时只统计
//            e.printStackTrace();
//        }
//
//        // 没有可用的了，提示用户修改
//        new CheckDirCanWriteDialog(this).showDialog();
//        return false;
//    }

    /**
     * 创建普通文件的下载任务
     * 
     * @return 创建的DownloadTask对象,若有错误,返回null
     * @author caisenchuan
     * @see android.webkit.DownloadListener#onDownloadStart(String,
     *      String, String, String, long)
     */
    private void do_download() {
//        DownloadTask task = new DownloadTask(mTaskParams, false);
//
//        if (listener != null) {
//            task.addListener(listener);
//        }
        
        if(mMobileWork)
            MoblieAllowDownloads.getInstance().addAllowMoblieNetDownload(mUri);
        
        mRequest.setAllowedNetworkTypes(mMobileWork ? Request.NETWORK_ALL : Request.NETWORK_NO_MOBILE);

        //防止有相同文件名
        String filename = PathResolver.getUniqueName(mFilename, mCustomFolder);
        mFilename = filename;

        try {
            mRequest.setDestinationInDir(mCustomFolder, filename);
        } catch (IllegalStateException ex) {
            // This only happens when directory Downloads can't be created or it isn't a directory
            // this is most commonly due to temporary problems with sdcard so show appropriate string
//            Toast.makeText(mContext, "USB storage unavailable", Toast.LENGTH_SHORT).show();
            CustomToastUtils.getInstance().showTextToast(R.string.download_error);
            Statistics.sendOnceStatistics(
                    GoogleConfigDefine.DOWNLOAD, GoogleConfigDefine.DOWNLOAD_PATH_ERROR);
            return;
        }

        DownloadManager.getInstance().createDownload(mRequest);
//        Toast.makeText(mContext, "Starting download…", Toast.LENGTH_SHORT).show();

        sendDownloadResourceStatistics();
    }

    private void sendDownloadResourceStatistics() {
        ContentValues c = mRequest.toContentValues();
        String header0 = c.getAsString(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX + "0");
        String header1 = c.getAsString(Downloads.Impl.RequestHeaders.INSERT_KEY_PREFIX + "1");
        String cookie = "";
        String ua = "";
        //  "2" 去掉:和其后面的空格 see Request encodeHttpHeaders()方法   eg. cookie: BAIDU...
        if (!TextUtils.isEmpty(header0)) {
            if (header0.startsWith("cookie")) {
                cookie = header0.substring("cookie".length() + 2);
            } else if (header0.startsWith("User-Agent")) {
                ua = header0.substring("User-Agent".length() + 2);
            }
        }
        if (!TextUtils.isEmpty(header1)) {
            if (header1.startsWith("cookie")) {
                cookie = header1.substring("cookie".length() + 2);
            } else if (header1.startsWith("User-Agent")) {
                ua = header1.substring("User-Agent".length() + 2);
            }
        }
        if (TextUtils.isEmpty(ua)) {
            ua = "";
        }
        if (TextUtils.isEmpty(cookie)) {
            cookie = "";
        }
        // 上报下载数据
        Statistics.sendDownloadResourceStatistics(1, mUri, ua, mContentDisposition, mMimetype, mContentLength, mReferer, cookie);
    }

    private String getFileName() {
        String contentDisposition =
                SmartDecode.recoverString(mContentDisposition,
                        SmartDecode.isGB2312Url(mUri), false);
        return URLUtilities.guessFileName(mUri, contentDisposition,
                mMimetype, true);
    }

    
    /*
    class ConfirmDownloadDialog extends SmartDialogHelper {
        public ConfirmDownloadDialog(DownloadManagerCheck check,
                String fileName) {
            super();
            this.mDialogType = SmartDialog.DOUBLE_BUTTON_COMMON_DIALOG;
            this.mMessageText = String.format(
                    getString(R.string.s_download_hint_ask), fileName);
            this.mCheckboxText = null;
            this.mButtonText = new String[] {
                    getString(R.string.dialog_btn_download),
                    getString(R.string.cancel)
            };
        }

        @Override
        public void onDidDialogClose(int whichButton, boolean[] checkState) {
            if (SmartDialog.BUTTON_POSITIVE == whichButton) {
                mNeedConfirm = false;
                confirmDownload();
            } else {
            }
        }

        @Override
        public void onDidDialogCancel(DialogInterface arg0) {
        }

        @Override
        public void onWillDialogShow() {
        }

    }

    class CheckWifiDialog extends SmartDialogHelper {
        DownloadManagerCheck mCheckHolder;

        public CheckWifiDialog(DownloadManagerCheck check) {
            super();
            this.mCheckHolder = check;
            this.mDialogType = SmartDialog.DOUBLE_BUTTON_COMMON_DIALOG;
            this.mMessageText = getString(R.string.s_download_hint_mobile);
            this.mCheckboxText = null;
            this.mButtonText = new String[] {
                    getString(R.string.s_download_lable_continue),
                    getString(R.string.cancel)
            };
        }

        @Override
        public void onDidDialogClose(int whichButton, boolean[] checkState) {
            if (SmartDialog.BUTTON_POSITIVE == whichButton) {
                mPause = false;
                mMobileWork = true;
                doConfirm();
            } else {
            }
        }

        @Override
        public void onDidDialogCancel(DialogInterface arg0) {
        }

        @Override
        public void onWillDialogShow() {
        }

       	}

        private void doConfirm() {
            ThreadUtils.postOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 不需要用户确认是否要下载
                    mNeedConfirm = false;
                    confirmDownload();
                }
            });
        }
    }

    class CheckExistsDialog extends SmartDialogHelper {
        DownloadManagerCheck mCheckHolder;

        public CheckExistsDialog(DownloadManagerCheck check, String fileName) {
            super();
            this.mCheckHolder = check;
            this.mDialogType = SmartDialog.DOUBLE_BUTTON_COMMON_DIALOG;
            this.mMessageText = String.format(
                    getString(R.string.download_alreadyexist_info), fileName);
            this.mCheckboxText = null;
            this.mButtonText = new String[] {
                    getString(R.string.ok),
                    getString(R.string.cancel)
            };
        }

        @Override
        public void onDidDialogClose(int whichButton, boolean[] checkState) {
            if (SmartDialog.BUTTON_POSITIVE == whichButton) {
                // 后面不再询问是否确认要下载
                mNeedConfirm = false;
                ThreadUtils.postOnUiThread(new Runnable() {
                    @SuppressWarnings("unused")
                    private DownloadManagerCheck mCheckHolder =
                            CheckExistsDialog.this.mCheckHolder;

                    @Override
                    public void run() {
                        // 继续检查
                        if (!checkDirCanWrite())
                            return;
                        if (!checkStorageSpace(mContext, mCustomFolder, mContentLength))
                            return;
                        if (!checkWifi())
                            return;
                        confirmDownload();
                    }
                });
            } else {
            }
        }

        @Override
        public void onDidDialogCancel(DialogInterface arg0) {
        }

        @Override
        public void onWillDialogShow() {
        }

    }

    class CheckStorageSpaceDialog extends SmartDialogHelper {
        public CheckStorageSpaceDialog() {
            super();
            init();
        }

        public CheckStorageSpaceDialog(Context context) {
            super(context);
            init();
        }

        public void init() {
            mDialogType = SmartDialog.SINGLE_BUTTON_COMMON_DIALOG;
            mMessageText = getString(R.string.s_download_hint_sd_card_no_space);
            mCheckboxText = null;
            mButtonText = new String[] {
                    getString(R.string.btn_download_return)
            };
        }

        @Override
        public void onDidDialogClose(int whichButton, boolean[] checkState) {
        }

        @Override
        public void onDidDialogCancel(DialogInterface arg0) {
        }

        @Override
        public void onWillDialogShow() {
        }

    }

    class CheckDirCanWriteDialog extends SmartDialogHelper {

        public CheckDirCanWriteDialog(DownloadManagerCheck check) {
            super();
            this.mDialogType = SmartDialog.DOUBLE_BUTTON_COMMON_DIALOG;
            this.mMessageText =
                    getString(R.string.download_save_dir_unavailable);
            this.mCheckboxText = null;
            this.mButtonText = new String[] {
                    getString(R.string.btn_download_change),
                    getString(R.string.btn_download_cancel),
            };
        }

        @Override
        public void onDidDialogClose(int whichButton, boolean[] checkState) {
            if (SmartDialog.BUTTON_POSITIVE == whichButton) {
                ToolkitActivity.startToolKitActivity(mContext, ToolkitActivity.TOOLKIT_LAYOUT_STORAGESETTING);
            }
        }

        @Override
        public void onDidDialogCancel(DialogInterface arg0) {
        }

        @Override
        public void onWillDialogShow() {
        }
    }
    */

    public String getmUri() {
        return mUri;
    }
}
