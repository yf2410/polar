package com.polar.browser.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.polar.browser.JuziApp;
import com.polar.browser.bean.ApkInfo;
import com.polar.browser.database.FileScanStore;
import com.polar.browser.download.savedpage.SavedPageUtil;
import com.polar.browser.manager.ConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.polar.browser.download_refactor.Constants.TYPE_APK;
import static com.polar.browser.download_refactor.Constants.TYPE_AUDIO;
import static com.polar.browser.download_refactor.Constants.TYPE_DOC;
import static com.polar.browser.download_refactor.Constants.TYPE_IMAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_OTHER;
import static com.polar.browser.download_refactor.Constants.TYPE_VIDEO;
import static com.polar.browser.download_refactor.Constants.TYPE_WEB_PAGE;
import static com.polar.browser.download_refactor.Constants.TYPE_ZIP;

/**
 * Created by yd_lp on 2016/10/28.
 */

final public class QueryUtils {

    public static final String KEY_VIDEO = "video";
    public static final String KEY_AUDIO = "audio";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_APK = "apk";
    public static final String KEY_OTHER = "other";

    private static final String[] PROJECTION = {
            "COUNT(*) AS SIZE"
    };

    private static final String[] APK_PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
    };

    private static final String APK_WHERE = MediaStore.Files.FileColumns.DATA + " like '%.apk'";
    public static final String WEB_PAGE_WHERE = MediaStore.Files.FileColumns.DATA + " like '%.html'";

    public static final String DOC_WHERE = MediaStore.Files.FileColumns.MIME_TYPE + "==" + "'text/plain' or "  //txt
            + MediaStore.Files.FileColumns.MIME_TYPE + "==" + "'application/pdf' or "  //pdf
            + MediaStore.Files.FileColumns.MIME_TYPE + "==" + "'application/msword' or "  //doc
            + MediaStore.Files.FileColumns.MIME_TYPE + "==" + "'application/vnd.ms-excel' or "  //xls
            + MediaStore.Files.FileColumns.MIME_TYPE + "==" + "'application/vnd.openxmlformats-officedocument.wordprocessingml.document' or "  //docx
            + MediaStore.Files.FileColumns.MIME_TYPE + "==" + "'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' or "  //xlsx
            + MediaStore.Files.FileColumns.MIME_TYPE + "==" + "'application/vnd.ms-powerpoint' "; //ppt

    public static final String ZIP_WHERE = MediaStore.Files.FileColumns.DATA + " like '%.zip' or "
            + MediaStore.Files.FileColumns.DATA + " like '%.rar'";


    public static final String OTHER_WHERE =
            //文档
            MediaStore.Files.FileColumns.MIME_TYPE + "!=" + "'text/plain' and "  //txt
                    + MediaStore.Files.FileColumns.MIME_TYPE + "!=" + "'application/pdf' and "  //pdf
                    + MediaStore.Files.FileColumns.MIME_TYPE + "!=" + "'application/msword' and "  //doc
                    + MediaStore.Files.FileColumns.MIME_TYPE + "!=" + "'application/vnd.ms-excel' and "  //xls
                    + MediaStore.Files.FileColumns.MIME_TYPE + "!=" + "'application/vnd.openxmlformats-officedocument.wordprocessingml.document' and "  //docx
                    + MediaStore.Files.FileColumns.MIME_TYPE + "!=" + "'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' and "  //xlsx
                    + MediaStore.Files.FileColumns.MIME_TYPE + "!=" + "'application/vnd.ms-powerpoint' and "  //ppt
                    //压缩包
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.zip' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.rar' and "
                    //安装包
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.apk' and "
                    //图片
                    + MediaStore.Images.Media.MIME_TYPE + " NOT LIKE 'image/%' and "
                    //视频
                    + MediaStore.Video.Media.MIME_TYPE + " NOT LIKE 'video/%' and "
                    //音频
                    + MediaStore.Audio.Media.MIME_TYPE + " NOT LIKE 'audio/%'  and "

                    //音频（防止损坏的音频格式被筛选）
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.amr' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.mp3' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.wav' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.ogg' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.midi' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.aac' and "
                    //视频（防止损坏的视频格式被筛选）
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.mp4' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.rmvb' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.avi' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.wmv' and "
                    //图片（防止损坏的图片格式被筛选）
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.jpg' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.jpeg' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.png' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.bmp' and "
                    + MediaStore.Files.FileColumns.DATA + " NOT LIKE '%.gif' ";


    private static final String OTHERS_WHERE[] = new String[]{OTHER_WHERE};

    public static final String KEY_DOCUMENT = "doc";
    public static final String KEY_ZIP_FILE = "zip_file";
    public static final String KEY_WEB_PAGE = "web_page";
    public static final Map<String, Integer> queryMap = new HashMap<>();

    private QueryUtils() {

    }

    public static String getVideoThumbnailPath(Context context, long id) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Thumbnails.DATA},
                    MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static void notifyFileCountChanged(String type) {
        ConfigManager.getInstance().notifyFileCountChanged(type);
    }

    public static Map<String, Integer> queryCount(Context context) {
        ContentResolver resolver = context.getContentResolver();
        int videoCount = queryVideoCount(resolver);
        int audioCount = queryAudioCount(resolver);
        int imageCount = queryImageCount(resolver);
        int documentCount = queryDocumentCount(resolver);
        int zipFileCount = queryZipFileCount(resolver);
        int apkCount = queryApkCount(resolver);
//        int offlineWebPageCount = queryWebpageCount(resolver);
        int otherCount = queryOtherCount(resolver);
        //扫描本地APK
//      scanApkInfo(context);

        queryMap.put(KEY_VIDEO, videoCount);
        queryMap.put(KEY_AUDIO, audioCount);
        queryMap.put(KEY_IMAGE, imageCount);
        queryMap.put(KEY_APK, apkCount);
        queryMap.put(KEY_DOCUMENT, documentCount);
        queryMap.put(KEY_ZIP_FILE, zipFileCount);
        queryMap.put(KEY_WEB_PAGE, SavedPageUtil.getSavedPageList(JuziApp.getAppContext()).size());
        queryMap.put(KEY_OTHER, otherCount);

        return queryMap;
    }

    public static void queryCountByFileType(String type) {
        ContentResolver resolver = JuziApp.getAppContext().getContentResolver();
        switch (type) {
            case TYPE_APK:
                queryMap.put(KEY_APK, queryApkCount(resolver));
            case TYPE_AUDIO:
                queryMap.put(KEY_AUDIO, queryAudioCount(resolver));
            case TYPE_DOC:
                queryMap.put(KEY_DOCUMENT, queryDocumentCount(resolver));
            case TYPE_IMAGE:
                queryMap.put(KEY_IMAGE, queryImageCount(resolver));
            case TYPE_OTHER:
                queryMap.put(KEY_OTHER, queryOtherCount(resolver));
            case TYPE_VIDEO:
                queryMap.put(KEY_VIDEO, queryVideoCount(resolver));
            case TYPE_WEB_PAGE:
                queryMap.put(KEY_WEB_PAGE, SavedPageUtil.getSavedPageList(JuziApp.getAppContext()).size());
            case TYPE_ZIP:
                queryMap.put(KEY_ZIP_FILE, queryZipFileCount(resolver));
        }
    }

    private static int queryWebpageCount(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Files.getContentUri("external"),
                    PROJECTION,
                    WEB_PAGE_WHERE,
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static int queryZipFileCount(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Files.getContentUri("external"),
                    PROJECTION,
                    ZIP_WHERE,
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static int queryDocumentCount(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Files.getContentUri("external"),
                    PROJECTION,
                    DOC_WHERE,
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public static int queryVideoCount(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION,
                    null,
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int queryAudioCount(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION,
                    null,
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int queryImageCount(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION,
                    null,
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int queryApkCount(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(MediaStore.Files.getContentUri("external"),
                    PROJECTION,
                    APK_WHERE,
                    null,
                    null);
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                return cursor.getInt(0);
            } else {
                return 0;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 扫描本地所有APK
     *
     * @param context
     */
    public static void scanApkInfo(Context context) {
        Cursor cursor = null;
        List<ApkInfo> list = new ArrayList<ApkInfo>();
        try {
            cursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                    APK_PROJECTION,
                    APK_WHERE,
                    null,
                    null);
            if (cursor != null) {
                saveScanResult(context, processResult(context, cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private static List<ApkInfo> processResult(Context context, Cursor cursor) {
        ArrayList<ApkInfo> list = new ArrayList<ApkInfo>();
        try {
            while (cursor.moveToNext()) {
                try {
                    ApkInfo info = new ApkInfo();
                    info.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)));
                    info.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)));
                    info.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
                    info.setDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
                    info.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                    if (info.getPath() == null) continue;
                    if (info.getName() == null) {
                        info.setName(info.getPath().substring(info.getPath().lastIndexOf("/") + 1));
                    }
                    info.setInstalled(ApkUtils.isApkInstalled(context, info.getPath()));
                    list.add(info);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    private static void saveScanResult(Context context, List<ApkInfo> list) {
        for (ApkInfo info : list) {
            try {
                ContentValues values = new ContentValues(7);
                values.put(FileScanStore.Apk.ApkColumes.COLUME_APK_ID, info.getId());
                values.put(FileScanStore.Apk.ApkColumes.COLUME_ICON, "");
                values.put(FileScanStore.Apk.ApkColumes.COLUME_NAME, info.getName());
                values.put(FileScanStore.Apk.ApkColumes.COLUME_PATH, info.getPath());
                values.put(FileScanStore.Apk.ApkColumes.COLUME_DATE_ADD, info.getDate());
                values.put(FileScanStore.Apk.ApkColumes.COLUME_SIZE, info.getSize());
                values.put(FileScanStore.Apk.ApkColumes.COLUME_INSTALLED, info.isInstalled() == true ? 1 : 0);
                context.getContentResolver().insert(FileScanStore.Apk.CONTENT_URI, values);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public static int queryOtherCount(ContentResolver resolver) {
        Cursor cursor = null;
        int count = 0;
        try {
            for (String where : OTHERS_WHERE) {
                cursor = resolver.query(MediaStore.Files.getContentUri("external"),
                        PROJECTION,
                        where,
                        null,
                        null);
                if (cursor != null && cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    count += cursor.getInt(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    //NOTE: 刷新媒体数据库请使用MediaDBRefreshHelper
    //重命名后进行文件刷新：扫描媒体库，发送广播通知文件个数发生变化。
/*    public static void refreshRenameedFiles(final Context context,String path){
        DownloadUtil.requestMediaScan(path);
        ThreadManager.postDelayedTaskToLogicHandler(new Runnable() {
            @Override
            public void run() {
                QueryUtils.notifyFileCountChanged(context);
            }
        },4000);
    }

    //延时发送文件个数改变广播
    public static void delayNotifyFileCountChanged(final Context context,int delayTime){
        ThreadManager.postDelayedTaskToLogicHandler(new Runnable() {
            @Override
            public void run() {
                QueryUtils.notifyFileCountChanged(context);
            }
        },delayTime);
    }

    //仅仅扫描媒体库
    public static void scanRenameedFiles(String path){
        DownloadUtil.requestMediaScan(path);
    }
*/

    public static void clearQueryMap() {
        queryMap.clear();
    }

}
