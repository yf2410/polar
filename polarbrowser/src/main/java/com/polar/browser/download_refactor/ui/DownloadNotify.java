package com.polar.browser.download_refactor.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.download.DownloadActivity;
import com.polar.browser.download_refactor.Constants;
import com.polar.browser.download_refactor.DownloadItemInfo;
import com.polar.browser.download_refactor.DownloadManager;
import com.polar.browser.download_refactor.UiStatusDefine;
import com.polar.browser.download_refactor.dinterface.IDownloadObserver;
import com.polar.browser.download_refactor.util.ThreadManager;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.FileUtils;
import com.polar.browser.view.ToastClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DownloadNotify implements IDownloadObserver {
    
	private static final String TAG = "DownloadNotify";
    private NotificationManager mNotificationMgr;
    private static final int SIZE_KB = 1024;
    private static final int SIZE_MB = 1024 * 1024;
    private static final int SIZE_GB = 1024 * 1024 * 1024;
//    public final static String KEY_ID = "com.ijinshan.browser.screen.DownloadTask.id";
//    public final static String KEY_VIRUS = "com.ijinshan.browser.DownloadTask.virus";
    private Context mApplicationContext = JuziApp.getInstance().getApplicationContext();
    private final HashMap<String, Long> mNotifiesStartTimeHashMap = new HashMap<String, Long>();
    private final static boolean STANDARD_ICON = Build.HOST != null
            && (Build.HOST.toLowerCase().contains("cyanogenmod")
            || Build.HOST.toLowerCase().contains("exodus"));

	public DownloadNotify() {
		mNotificationMgr = (NotificationManager) JuziApp.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}

    @Override
    public void handleDownloadLists(ArrayList<DownloadItemInfo> lists){
        
    }

	@Override
	public void handleDownloadVirusStatus(long id, int virusStatus, String md5, long interval) {
	}

	@Override
	public void handleDownloadItemAdded(boolean ret, long id, DownloadItemInfo info) {
	}

	@Override
	public void handleDownloadItemRemoved(boolean ret, long[] id) {
	}

	@Override
	public void handleDownloadProgress(long id, long currentBytes, long totalBytes, long speedBytes) {
		String idString = Long.toString(id);
		DownloadItemInfo info = DownloadManager.getInstance().getDownloadItem(id);
		if (info == null) {
			return;
		}
		Notification notif = createBaseNotify(info);
		info.mCurrentBytes = currentBytes;
		info.mTotalBytes = totalBytes;
		activeNotifyPlus(notif, (int)info.getProgress100(), speedBytes);
		if (mNotifiesStartTimeHashMap.containsKey(idString)) {
			long timestamp =  mNotifiesStartTimeHashMap.get(idString);
			notif.when = timestamp;
		} else {
			long timestamp = notif.when;
			mNotifiesStartTimeHashMap.put(Long.toString(id), timestamp);
		}
		mNotificationMgr.notify(makeNotifyId(info.mId), notif);
	}

	@Override
	public void handleDownloadStatus(long id, int status, int reason) {
		DownloadItemInfo info = DownloadManager.getInstance().getDownloadItem(id);
		if (info == null)
			return;
		Notification notif = createBaseNotify(info);
		switch (status) {
			case UiStatusDefine.STATUS_PENDING:
			{
				notif.contentView.setTextViewText(R.id.tv_status, JuziApp.getInstance().getString(R.string.download_waite));
				break;
			}
			case UiStatusDefine.STATUS_RUNNING:
			{
				activeNotifyPlus(notif, (int)info.getProgress100(), 0);
				long timestamp = notif.when;
				mNotifiesStartTimeHashMap.put(Long.toString(id), timestamp);
				break;
			}
			case UiStatusDefine.STATUS_PAUSED:
			{
//				String resonString = getPauseReasonText(reason);//(DownloadManager.getReason(info.mStatus));
//				pauseNotifyPlus(notif, (int)info.getProgress100(), resonString);
				notif.contentView.setTextViewText(R.id.tv_status, JuziApp.getInstance().getString(R.string.download_status_pause));
				notif.contentView.setProgressBar(R.id.progress, 100, (int)info.getProgress100(), false);
				notif.contentView.setTextViewText(R.id.tv_progress, (int)info.getProgress100() + "%");
				break;
			}
			case UiStatusDefine.STATUS_SUCCESSFUL:
			{
//				completeNotifyPlus(notif, info.mVirusStatus, id, info.mFilePath);

				notif.contentView.setTextViewText(R.id.tv_status, JuziApp.getInstance().getString(R.string.download_finish));
				notif.contentView.setViewVisibility(R.id.progress, View.GONE);
				notif.contentView.setViewVisibility(R.id.tv_progress, View.GONE);

				if (info.mFilePath == null) {
					return;
				}
				File file = new File(info.mFilePath);
				if (!file.exists()) {
					return;
				}
				if (info.mFilePath.toLowerCase().endsWith(".apk")) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					PendingIntent pendingIntent = PendingIntent.getActivity(mApplicationContext,
							(int) id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					notif.contentIntent = pendingIntent;
					// 启动安装apk
					JuziApp.getInstance().startActivity(intent);
				}
				else if (TextUtils.equals(FileUtils.getFileType(info.mFilePath), Constants.TYPE_IMAGE)) {
					CustomToastUtils.getInstance().showClickActivityToast(
							JuziApp.getAppContext(),
							JuziApp.getAppContext().getString(R.string.download_page_save),
							JuziApp.getAppContext().getString(R.string.click_to_see),
							3000,
							ToastClickListener.EVENT_CLICK_OPEN_IMAGE,
							info.mFilePath,
							false
					);
				}

				break;
			}
			case UiStatusDefine.STATUS_FAILED:
			{
//				String failReson = getFailReasonText(reason);
//				failNotifyPlus(notif, (int)info.getProgress100(), failReson);
				notif.contentView.setTextViewText(R.id.tv_status, JuziApp.getInstance().getString(R.string.download_status_failure));
				notif.contentView.setViewVisibility(R.id.progress, View.GONE);
				notif.contentView.setViewVisibility(R.id.tv_progress, View.GONE);
				break;
			}
			default:
				return;
		}
		// Log.d("NTF", "handleDownloadStatus() id=" + makeNotifyId(info.mId) + ", status=" + status);
		mNotificationMgr.notify(makeNotifyId(info.mId), notif);
	}


	private Notification createBaseNotify(DownloadItemInfo info) {
		final Notification.Builder builder = new Notification.Builder(this.mApplicationContext);
		int icon = ((info.mStatus == UiStatusDefine.STATUS_RUNNING) ? android.R.drawable.stat_sys_download
				: android.R.drawable.stat_sys_download_done);
		builder.setSmallIcon(icon);
		Notification notif = com.polar.browser.download_refactor.util.ApiCompatibilityUtils.getNotification(builder);

		notif.contentView = new RemoteViews(mApplicationContext.getPackageName(), R.layout.notif_download);

		// 更改文字
		notif.contentView.setTextViewText(R.id.tv_file_name, info.getFilename());
		notif.contentView.setTextViewText(R.id.tv_status, JuziApp.getInstance().getString(R.string.download_waite));
		// 更改进度条
		notif.contentView.setProgressBar(R.id.progress, 100, 0, false);
		notif.contentView.setTextViewText(R.id.tv_progress, 0 + "%");
		notif.contentView.setImageViewResource(R.id.iv_download, getFileIconByFileName(info.getFilename()));


		Intent intent = new Intent(mApplicationContext, DownloadActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(mApplicationContext, (int) info.mId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notif.contentIntent = pendingIntent;

		notif.when = System.currentTimeMillis();
		return notif;
	}

	private static final int OFFSET_NOTIFY_ID = 0x00001000;

	private static int makeNotifyId(long downloadItemInfoID) {
		return OFFSET_NOTIFY_ID + (int) downloadItemInfoID;
	}

	private void activeNotifyPlus(Notification notif, int progress, long speed) {
		notif.contentView.setTextViewText(R.id.tv_status, JuziApp.getInstance().getString(R.string.download_status_downloading));
		notif.contentView.setProgressBar(R.id.progress, 100, progress, false);
		notif.contentView.setTextViewText(R.id.tv_progress, progress + "%");
//		notif.flags |= Notification.FLAG_NO_CLEAR;
	}

	public static void cancelNotify(final long[] ids) {
		postUITask(new Runnable() {
			@Override
			public void run() {
				NotificationManager notificationManager = (NotificationManager) JuziApp.getInstance().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
				for (int i = 0; i < ids.length; i++) {
					notificationManager.cancel(makeNotifyId(ids[i]));
				}
			}
		});
	}

	private static void postUITask( Runnable r ){
		ThreadManager.post(ThreadManager.THREAD_UI, r);
	}

	/**
	 * 根据文件名获取文件类型图片
	 *
	 * @return file icon's drawable id;
	 */
	private int getFileIconByFileName(String fileName) {
		if (TextUtils.isEmpty(fileName)) {
			return R.drawable.file_icon_default;
		}
		fileName = fileName.toLowerCase();
		// 文档 txt,doc,docx,xls,xlsx,ppt,pptx,hlp,wps,rtf,html,htm,pdf,xml
		if (fileName.endsWith(".txt") || fileName.endsWith(".pdf") ||
				fileName.endsWith(".xls") || fileName.endsWith(".xlsx") ||
				fileName.endsWith(".ppt") || fileName.endsWith(".pptx") ||
				fileName.endsWith(".doc") || fileName.endsWith(".docx") ||
				fileName.endsWith(".wps") || fileName.endsWith(".hlp") ||
				fileName.endsWith(".rtfd.zip") || fileName.endsWith(".rtf") ||
				fileName.endsWith(".numbers") || fileName.endsWith(".pages") ||
				fileName.endsWith(".numbers.zip") || fileName.endsWith(".pages.zip") ||
				fileName.endsWith(".xml") || fileName.endsWith(".json")) {
			return R.drawable.file_icon_doc;
		}
		// 音乐 wav,aif,au,mp3,ram,wma,aac,ogg,ape,acg,aiff,mid,ra
		if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
				fileName.endsWith(".aif") || fileName.endsWith(".au") ||
				fileName.endsWith(".ram") || fileName.endsWith(".wma") ||
				fileName.endsWith(".aac") || fileName.endsWith(".ogg") ||
				fileName.endsWith(".ape") || fileName.endsWith(".acg") ||
				fileName.endsWith(".aiff") || fileName.endsWith(".mid") ||
				fileName.endsWith(".ra")) {
			return R.drawable.file_icon_music;
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
			return R.drawable.file_icon_video;
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
			return R.drawable.file_icon_image;
		}
		// 压缩文件 rar,zip,arj,gz,z,cab,7z,iso
		if (fileName.endsWith(".zip") || fileName.endsWith(".rar") ||
				fileName.endsWith(".arj") || fileName.endsWith(".gz") ||
				fileName.endsWith(".z") || fileName.endsWith(".cab") ||
				fileName.endsWith(".7z") || fileName.endsWith(".iso")) {
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
