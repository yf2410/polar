/**
 * @brief     Package com.polar.browser.model
 * @author    zhouchenguang
 * @since     1.0.0.0
 * @version   1.0.0.0
 * @date      2012-12-20
 */

package com.polar.browser.download_refactor;

/** 
 * @file      DownloadUtil.java
 * @brief     This file is part of the Model module of KBrowser project. \n
 *            This file serves as "java" source file that presents global 
 *            download utils that would be required by the download module. \n
 *
 * @author    zhouchenguang
 * @since     1.0.0.0
 * @version   1.0.0.0
 * @date      2012-12-20
 *
 * \if TOSPLATFORM_CONFIDENTIAL_PROPRIETARY
 * ============================================================================\n
 *\n
 *           Copyright (c) 2012 zhouchenguang.  All Rights Reserved.\n
 *\n
 * ============================================================================\n
 *\n
 *                              Update History\n
 *\n
 * Author (Name[WorkID]) | Modification | Tracked Id | Description\n
 * --------------------- | ------------ | ---------- | ------------------------\n
 * zhouchenguang [7897]  |  2012-12-20  | <xxxxxxxx> | Initial Created.\n
 *\n
 * \endif
 *
 * <tt>
 *\n
 * Release History:\n
 *\n
 * Author (Name[WorkID]) | ModifyDate | Version | Description \n
 * --------------------- | ---------- | ------- | -----------------------------\n
 * zhouchenguang [7897]  | 2012-12-20 | 1.0.0.0 | Initial created. \n
 *\n
 * </tt>
 */
//=============================================================================
//                                  IMPORT PACKAGES
//=============================================================================

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.polar.browser.JuziApp;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.download_refactor.util.UserAgent;
import com.polar.browser.utils.SimpleLog;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//=============================================================================
//                                 CLASS DEFINITIONS
//=============================================================================
/**
 * @class DownloadUtil.java
 * @brief download utils. \n
 * @author zhouchenguang
 * @since 1.0.0.0
 * @version 1.0.0.0
 * @date 2012-12-20
 * @par Applied: External
 */
public class DownloadUtil
{
    private static final String TAG = "DownloadUtil";

    /* 常用单位, by caisenchuan */
    public final static long KB = 1024;
    public final static long MB = KB * 1024;
    public final static long GB = MB * 1024;
    /** 剩余空间低于此值时，进行警告, by caisenchuan */
//    public static final long DEFAULT_MIN_SPACE_WARNING = 1024 * MB;
    /** 剩余空间低于此值时，强制终止下载, by caisenchuan */
    public static final long DEFAULT_MIN_SPACE_ERROR = 40 * MB;

    /** 小于此大小时,就算支持分段下载也只启动一个线程, by caisenchuan */
    public static final long DEFAULT_SINGLE_THREAD_THRESHOLD = 500 * KB;
    /** 默认UA, by caisenchuan */
    private static String mDefaultUA = UserAgent.DEFAULT_UA;
    static SSLSocketFactory sSSLSocketFactoryInstance = null;

    /** 请求超时时间，单位：ms */
//    public static final int DEFAULT_CONNECTION_TIMEOUT = 2 * 60 * 1000;
//    /** 响应超时时间，单位：ms */
//    public static final int DEFAULT_SOCKET_TIMEOUT = 2 * 60 * 1000;
    /** 重试时的等待间隔，单位：ms */
    public static final int DEFAULT_RETRY_WATING_TIME = 3 * 1000;
    /** 最大重试次数 */
    public static final int MAX_RETRY_COUNT = 100;

    public static enum SortDateType // 日期排序类型
    {
        CREATE_TIME, // 根据创建时间排序
        FINISH_TIME // 根据完成时间排序
    };

    public static boolean nullOrEmptyString(String string)
    {
        if (string == null || string.trim().length() == 0)
            return true;

        return false;
    }

    public static void requestMediaScan(String filePath)
    {
        if (filePath == null)
            return;

        Uri uri = Uri.parse("file://" + filePath);

        Context context = JuziApp.getInstance().getApplicationContext();
        if (context != null)
        {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }
    }

    public static int requestMediaRemoval(String filePath, String mimeType)
    {
        try
        {
            int deletecount = 0;

            if (nullOrEmptyString(mimeType))
                return deletecount;

            Context context = JuziApp.getInstance().getApplicationContext();

            String[] selectionArgs = new String[] {
                    filePath
            };

            if (mimeType.toLowerCase(java.util.Locale.US).startsWith("audio"))
            {
                String selection = MediaStore.Audio.Media.DATA + " = ?";
                deletecount = context.getContentResolver().delete(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, selection,
                        selectionArgs);
            }
            else if (mimeType.toLowerCase(java.util.Locale.US).startsWith("video"))
            {
                String selection = MediaStore.Video.Media.DATA + " = ?";
                deletecount = context.getContentResolver().delete(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, selection,
                        selectionArgs);
            }
            else if (mimeType.toLowerCase(java.util.Locale.US).startsWith("image"))
            {
                String selection = MediaStore.Images.Media.DATA + " = ?";
                deletecount = context.getContentResolver().delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection,
                        selectionArgs);
            }

            return deletecount;
        } catch (Exception e)
        {
            SimpleLog.e(e);
            return -1;
        }
    }

    public static int requestMediaRemoval(String filePath)
    {
        if (nullOrEmptyString(filePath))
            return 0;

        int start = filePath.lastIndexOf(".");
        if (start != -1 && filePath.length() > start + 1)
        {
            String extension = filePath.substring(start + 1);
            String guessMimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension);
            return requestMediaRemoval(filePath, guessMimeType);
        }
        else
        {
            return 0;
        }
    }

    /**
     * 判断当前设置的SD卡的剩余空间是否足够
     * 
     * @param file_size 要下载的文件大小
     * @return
     */
//    public static boolean availableSizeEnough(long file_size) {
//        String path_s = PathResolver.getDownloadSDcardPath();
//        return availableSizeEnough(path_s, file_size);
//    }

//    /**
//     * 判断SD卡的剩余空间是否足够
//     * 
//     * @param file_size 要下载的文件大小
//     * @return
//     */
//    public static boolean availableSizeEnough(String path_s, long file_size) {
//        boolean ret = true;
//
//        long curr_size = getAvailaleSize(path_s);
//        if (file_size > 0) {
//            // 若文件大小有效，判断剩余空间与文件大小关系
//            if (curr_size < file_size) {
//                ret = false;
//            }
//        } else {
//            // 若文件大小无效，则判断剩余空间是否小于阈值
//            if (curr_size < DEFAULT_MIN_SPACE_ERROR) {
//                ret = false;
//            }
//        }
//
//        return ret;
//    }

// TODO Remove unused code found by UCDetector
//     /**
//      * 获取SD卡剩余空间信息的提示，若剩余空间足够，则返回空字符串
//      * 
//      * @param context
//      * @param path_s
//      * @param file_size
//      * @return
//      */
//     public static String getAvailableSizeHint(Context context, String path_s, long file_size) {
//         String ret = "";
// 
//         long curr_size = getAvailaleSize(path_s);
//         if (file_size > 0) {
//             // 若文件大小有效，判断剩余空间与文件大小关系
//             if (curr_size < file_size) {
//                 ret = String
//                         .format("%s : %s",
//                                 context.getString(R.string.s_download_hint_sdcard_space_less_than_file_size),
//                                 showSize(file_size));
//             }
//         } else {
//             // 若文件大小无效，则判断剩余空间是否小于阈值
//             if (curr_size < DEFAULT_MIN_SPACE_ERROR) {
//                 ret = context.getString(R.string.s_download_hint_sdcard_space_less_than_limit);
//             }
//         }
// 
//         return ret;
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 根据当前手机的状态选择默认SD卡
//      * 
//      * @return
//      * @author caisenchuan
//      */
//     public static String getDefaultSDCardPath(Context context) {
//         String ret = "";
// 
//         List<SDCard> list = KSystemUtils.getAllSdcardState(context);
//         SDCard first = null;
//         SDCard firstOuter = null;
//         SDCard firstInner = null;
//         // 遍历列表
//         for (SDCard sd : list) {
//             if (sd != null) {
//                 if (first == null) {
//                     first = sd;
//                 }
// 
//                 if (sd.isMount()) {
//                     if (sd.isRemoveable()) {
//                         // 找到第一张可用的外置SD卡
//                         if (firstOuter == null) {
//                             firstOuter = sd;
//                         }
//                     } else {
//                         // 找到第一张可用的内置SD卡
//                         if (firstInner == null) {
//                             firstInner = sd;
//                         }
//                     }
//                 }
//             }
//         }
// 
//         // 优先使用外置SD卡，否则使用内置，若都不可用，则使用第一张SD卡
//         if (firstOuter != null) {
//             ret = firstOuter.getPath();
//         } else if (firstInner != null) {
//             ret = firstInner.getPath();
//         } else if (first != null) {
//             ret = first.getPath();
//         } else {
//             // ...
//         }
// 
//         KLog.d(TAG, "getDefaultSDCardPath : %s", ret);
// 
//         return ret;
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 找到一张可用的SD卡
//      * 
//      * @return
//      * @author caisenchuan
//      */
//     public static String getValidSDCardPath(Context context) {
//         String ret = "";
// 
//         List<SDCard> list = KSystemUtils.getAllSdcardState(context);
//         SDCard first = null;
//         // 遍历列表
//         for (SDCard sd : list) {
//             if (sd != null) {
//                 if (sd.isMount()) {
//                     // 找到第一张可用的SD卡
//                     first = sd;
//                     break;
//                 }
//             }
//         }
// 
//         // 优先使用外置SD卡，否则使用内置，若都不可用，则使用第一张SD卡
//         if (first != null) {
//             ret = first.getPath();
//         } else {
//             // ...
//         }
// 
//         KLog.d(TAG, "getValidSDCardPath : %s", ret);
// 
//         return ret;
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 找到剩余空间可用的SD卡
//      * 
//      * @param context
//      * @param file_size 要比较的文件大小
//      * @return
//      */
//     public static String getSpaceValidSDCardPath(Context context, long file_size) {
//         String ret = "";
// 
//         List<SDCard> list = KSystemUtils.getAllSdcardState(context);
//         SDCard first = null;
//         // 遍历列表
//         for (SDCard sd : list) {
//             if (sd != null) {
//                 if (sd.isMount() &&
//                         availableSizeEnough(sd.getPath(), file_size)) {
//                     first = sd;
//                     break;
//                 }
//             }
//         }
// 
//         // 优先使用外置SD卡，否则使用内置，若都不可用，则使用第一张SD卡
//         if (first != null) {
//             ret = first.getPath();
//         } else {
//             // ...
//         }
// 
//         KLog.d(TAG, "getSpaceValidSDCardPath : %s", ret);
// 
//         return ret;
//     }

    /**
     * 读取SD卡可用空间大小
     * 
     * @param path_s 会读取此路径所在的SD卡空间大小
     * @return
     */
    public static long getAvailaleSize(String path_s) {
        long ret = 0;

        if (!TextUtils.isEmpty(path_s)) {
            boolean mount = PathResolver.checkSDCardMountByFile(
            		JuziApp.getInstance().getApplicationContext(), path_s);
            if (mount) {
                try {
                    File path = new File(path_s);
                    StatFs stat = new StatFs(path.getPath());
                    long blockSize = stat.getBlockSize();
                    long availableBlocks = stat.getAvailableBlocks();
                    ret = availableBlocks * blockSize;
                } catch (Exception e) {
                    SimpleLog.e(e);
                }
            }
            // KLog.d(TAG, "getAvailableSize : %s %s", path_s, mount);
        } else {
            SimpleLog.e(TAG, "getAvailableSize, path_s : " + path_s);
        }

        return ret;
    }
    public static long getAvailaleSize(File file) {
        return getAvailaleSize(file.getAbsolutePath());
    }
    /**
     * 读取SD卡总空间大小
     * 
     * @param path_s 会读取此路径所在的SD卡空间大小
     * @return
     */
    public static long getTotalSize(String path_s) {
        long ret = 0;

        if (!TextUtils.isEmpty(path_s)) {
            boolean mount = PathResolver.checkSDCardMountByFile(
            		JuziApp.getInstance().getApplicationContext(), path_s);
            if (mount) {
                try {
                    File path = new File(path_s);
                    StatFs stat = new StatFs(path.getPath());
                    long blockSize = stat.getBlockSize();
                    long totalBlocks = stat.getBlockCount();
                    ret = totalBlocks * blockSize;
                } catch (Exception e) {
                    SimpleLog.e(e);
                }
            }
            // KLog.d(TAG, "getTotalSize : %s %s", path_s, mount);
        } else {
            SimpleLog.e(TAG, "getTotalSize, path_s : " + path_s);
        }

        return ret;
    }

    /**
     * 读取SD卡已用空间百分比
     * 
     * @author caisenchuan
     */
//    public static int getUsedSizePer()
//    {
//        String path_s = PathResolver.getDownloadSDcardPath();
//        return getUsedSizePer(path_s);
//    }

    /**
     * 读取SD卡已用空间百分比
     * 
     * @param path_s 会读取此路径所在的SD卡空间百分比
     * @author caisenchuan
     */
    public static int getUsedSizePer(String path_s)
    {
        int per = 0;

        if (!TextUtils.isEmpty(path_s)) {
            boolean mount = PathResolver.checkSDCardMountByFile(
            		JuziApp.getInstance().getApplicationContext(), path_s);
            if (mount) {
                try {
                    File path = new File(path_s);
                    StatFs stat = new StatFs(path.getPath());
                    long availableBlocks = stat.getAvailableBlocks();
                    long totalBlocks = stat.getBlockCount();
                    long usedBloacks = totalBlocks - availableBlocks;
                    if (usedBloacks > 0 && totalBlocks > 0) {
                        per = (int) ((usedBloacks * 100) / totalBlocks);
                    }
                } catch (Exception e) {
                    SimpleLog.e(e);
                }
            }
            // KLog.d(TAG, "getUsedSizePer : %s %s", path_s, mount);
        } else {
            SimpleLog.e(TAG, "getUsedSizePer, path_s : " + path_s);
        }

        return per;
    }

// TODO Remove unused code found by UCDetector
//     /**
//      * Java文件操作 获取文件扩展名,例如:.apk
//      * 
//      * @author caisenchuan
//      * @param filename - 文件名
//      *            摘自：http://blog.csdn.net/redoffice/article/details/6652731
//      */
//     public static String getExtensionName(String filename)
//     {
//         if ((filename != null) && (filename.length() > 0))
//         {
//             int dot = filename.lastIndexOf('.');
//             if (dot > -1)
//             {
//                 return filename.substring(dot);
//             }
//         }
//         return "";
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * Java文件操作 获取不带扩展名的文件名
//      * 
//      * @author caisenchuan
//      * @param filename - 文件名
//      *            摘自：http://blog.csdn.net/redoffice/article/details/6652731
//      */
//     public static String getFileNameNoEx(String filename)
//     {
//         if ((filename != null) && (filename.length() > 0))
//         {
//             int dot = filename.lastIndexOf('.');
//             if (dot > -1)
//             {
//                 return filename.substring(0, dot);
//             }
//         }
//         return filename;
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 截短字符串,去头尾,中间显示...
//      */
//     public static String getBreifStr(String str)
//     {
//         String pro_fix = "";
//         String post_fix = "";
//         int len = str.length();
//         String ret = "";
// 
//         if (len >= 20)
//         {
//             pro_fix = str.substring(0, 13);
//             post_fix = str.substring(len - 3);
//             ret = String.format("%s...%s", pro_fix, post_fix);
//         }
//         else
//         {
//             ret = str;
//         }
// 
//         return ret;
//     }

    /**
     * 截短文件名,保持后缀名,文件名保持定长,中间多出的字符显示...
     */
//    public static String getBreifFileName(String filename)
//    {
//        String name = getFileNameNoEx(filename);
//        String ext = getExtensionName(filename);
//        name = getBreifStr(name);
//
//        String ret = String.format("%s%s", name, ext);
//
//        return ret;
//    }

    /**
     * url与对应的UserAgent列表
     * 
     * @author caisenchuan
     */
    private static final String[][] UserAgentList = {
            // url, User-Agent
            {
                    ".baidu.com", "AndroidDownloadManager"
        }
    };

    /**
     * 根据url读取特殊的UserAgent
     * 
     * @param url
     * @return 若找到对应的url,返回UserAgent,否则返回null
     * @author caisenchuan
     */
    public static String getUserAgentByUrl(String url) {
        String userAgent = mDefaultUA;

        if (!TextUtils.isEmpty(url)) {
            // TODO 使用HashMap等更高效的方式实现, by caisenchuan
            for (int i = 0; i < UserAgentList.length; i++) {
                if (url.indexOf(UserAgentList[i][0]) > 0) {
                    userAgent = UserAgentList[i][1];
                    break; // 适配第一个符合的url
                }
            }
        }

        // KLog.debug(TAG, "getUserAgentByUrl, url : " + url + ", agent :" +
        // userAgent);

        return userAgent;
    }

    /** Header UA字段, by caisenchuan */
//    public static final String HEADER_UA = "User-Agent";

    /**
     * 添加UA Header
     * 
     * @param get - 要添加Header的HttpGet对象
     * @param userAgent - 手动指定UA
     * @param url - 通过url确定UA,当userAgent为空时有效
     * @return true - 成功; false - 有错误,失败;
     * @author caisenchuan
     */
//    public static boolean addUserAgentHeader(HttpGet get, String userAgent, String url) {
//        if (get == null) {
//            KLog.e(TAG, "get == null!");
//            return false;
//        }
//
//        if (!TextUtils.isEmpty(userAgent)) {
//            get.addHeader(HEADER_UA, userAgent);
//            KLog.d(TAG, "userAgent : " + userAgent);
//        } else {
//            String userAgent_t = DownloadUtil.getUserAgentByUrl(url);
//            if (!TextUtils.isEmpty(userAgent_t)) {
//                get.addHeader(HEADER_UA, userAgent_t);
//            }
//            KLog.d(TAG, "userAgent_t : " + userAgent_t);
//        }
//
//        return true;
//    }

    /**
     * 读取UA
     * 
     * @param userAgent
     * @param url
     * @return
     */
    public static String getUserAgent(String userAgent, String url) {
        String ret = "";
        if (!TextUtils.isEmpty(userAgent)) {
            SimpleLog.e(TAG, "userAgent : " + userAgent);
            ret = userAgent;
        } else {
            ret = DownloadUtil.getUserAgentByUrl(url);
        }
        return ret;
    }

    /**
     * 根据文件大小显示相应的字符串
     */
    public static String showSize(long oriBytes)
    {
        if (oriBytes < KB) // Byte
        {
            return String.format(java.util.Locale.getDefault(), "%dB", oriBytes);
        }
        else if (oriBytes < MB) // Kb
        {
            long kb = oriBytes / KB;
            return String.format(java.util.Locale.getDefault(), "%dKB", kb);
        }
        else if (oriBytes < GB) // Mb
        {
            double mb = oriBytes / (double) MB;
            return String.format(java.util.Locale.getDefault(), "%.1fMB", mb);
        }
        else // Gb
        {
            double gb = oriBytes / (double) GB;
            return String.format(java.util.Locale.getDefault(), "%.1fGB", gb);
        }
    }

// TODO Remove unused code found by UCDetector
//     /**
//      * 获取分享路径
//      */
//     public static File getSharePath(Context context, String name) {
//         if (context == null) {
//             return null;
//         }
// 
//         String path = KSystemUtils.getInternalSdcardPath(context); // 优先使用内部SD卡存储
//         if (TextUtils.isEmpty(path)) {
//             path = KSystemUtils.getExternalSdcardPath(context);
//         }
// 
//         File file = null;
//         if (!TextUtils.isEmpty(path)) {
//             file = new File(path + KFile.SDCARD_CAMERA);
//             if (!file.exists()) {
// 
//                 if (!file.mkdirs()) {
//                     return null;
//                 }
//             }
//             file = new File(path + KFile.SDCARD_CAMERA + name);
//         }
// 
//         return file;
//     }

    // /**
    // * 获得SD卡的备份目录中的某个文件的File句柄
    // * */
    // public static File getSDBackupPath(String name){
    // Context context = KBrowserEngine.getApplicationContext();
    // if(context == null) {
    // return null;
    // }
    //
    // String path = KSystemUtils.getInternalSdcardPath(context); //优先使用内部SD卡存储
    // if(TextUtils.isEmpty(path)) {
    // path = KSystemUtils.getExternalSdcardPath(context);
    // }
    //
    // File file = null;
    // if(!TextUtils.isEmpty(path)) {
    // file = new File(path + KFile.SDCARD_BACKUP_DIR + name);
    // }
    //
    // return file;
    // }

    // /**
    // * 获得/data应用目录中的某个文件的File句柄
    // * */
    // public static File getDataDBPath(String name) {
    // Context context = KBrowserEngine.getApplicationContext();
    // if(context == null) {
    // return null;
    // }
    //
    // return context.getDatabasePath(name);
    // }

    public static void dumpRequest(HttpRequest req) {
        if (req != null) {
            SimpleLog.d(TAG, "--------------------dump request begin------------------");
            if (req.getRequestLine() != null && req.getRequestLine().getUri() != null) {
                String uri = req.getRequestLine().getUri().toString();
                SimpleLog.d(TAG, "uri : %s" + uri);
            }
            dumpHeaders(req);
            SimpleLog.d(TAG, "--------------------dump request end----------------");
        }
    }

    /**
     * 打印返回的http header,原来在DownloadTask里
     * 
     * @author caisenchuan
     */
    public static void dumpHeaders(HttpMessage message)
    {
        if (message == null) {
            return;
        }

        HeaderIterator i = message.headerIterator();
        	SimpleLog.d(TAG, "-----------dump headers begin---------");
        while (i.hasNext()) {
            Header header = i.nextHeader();
            SimpleLog.d(TAG, header.getName() + " : " + header.getValue());
        }
        SimpleLog.d(TAG, "-----------dump headers end-----------");
    }

    /**
     * 设置HttpClient的超时
     * 
     * @param httpClient 要修改的HttpClient
     * @param connectTimeout 请求超时
     * @param soTimeout 响应超时
     * @author caisenchuan
     */
//    public static void setTimeout(HttpClient httpClient, int connectTimeout, int soTimeout) {
//        if (httpClient == null) {
//            return;
//        }
//
//        HttpParams param = httpClient.getParams();
//        param.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, connectTimeout);
//        param.setParameter(HttpConnectionParams.SO_TIMEOUT, soTimeout);
//
//        return;
//    }

    /** Default connection and socket timeout of 60 seconds. Tweak to taste. */
    private static final int SOCKET_OPERATION_TIMEOUT = 60 * 1000;

    /**
     * 创建一个预定义好的HttpClient，不缓存SSL会话，使用默认UA
     * 
     * @return
     * @author caisenchuan
     */
//    public static HttpClient getHttpClientInstance() {
//        return getHttpClientInstance(null, null);
//    }

    /**
     * 创建一个预定义好的HttpClient，不缓存SSL会话
     * 
     * @param userAgent 请求时使用的UA，设为null使用默认UA
     * @return
     * @author caisenchuan
     */
//    public static HttpClient getHttpClientInstance(String userAgent) {
//        return getHttpClientInstance(userAgent, null);
//    }

    /**
     * 创建一个预定义好的HttpClient，使用默认UA
     * 
     * @param context 用于缓存SSL会话，可以设为null表示不缓存
     * @return
     * @author caisenchuan
     */
//    public static HttpClient getHttpClientInstance(Context context) {
//        return getHttpClientInstance(null, context);
//    }

    /**
     * 创建一个预定义好的HttpClient（设定的参数包括超时时间等），参考自AndroidHttpClient
     * 
     * @param userAgent 请求时使用的UA，设为null使用默认UA
     * @param context 用于缓存SSL会话，可以设为null表示不缓存
     * @return 构造好的HttpClient
     * @author caisenchuan
     */
    public static HttpClient getHttpClientInstance(String userAgent, Context context) {
        HttpParams params = new BasicHttpParams();

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        HttpConnectionParams.setConnectionTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SOCKET_OPERATION_TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        // 设置是否处理重定向
        HttpClientParams.setRedirecting(params, true);

        // Use a session cache for SSL sockets
        // SSLSessionCache sessionCache = context == null ? null : new
        // SSLSessionCache(context);

        // Set the specified user agent and register standard protocols.
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = mDefaultUA;
        }
        HttpProtocolParams.setUserAgent(params, userAgent);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http",
                PlainSocketFactory.getSocketFactory(), 80));
        // FIXME 此处只是暂时解决https链接无法下载的问题 但还存在漏洞问题 add by tanglong
        schemeRegistry.register(new Scheme("https",
                getSSLSocketFactory(), 443));
        // schemeRegistry.register(new Scheme("https",
        // SSLCertificateSocketFactory.getHttpSocketFactory(
        // SOCKET_OPERATION_TIMEOUT, sessionCache), 443));

        ClientConnectionManager manager =
                new ThreadSafeClientConnManager(params, schemeRegistry);

        // We use a factory method to modify superclass initialization
        // parameters without the funny call-a-static-method dance.
        return new DefaultHttpClient(manager, params);
    }

    private synchronized static SSLSocketFactory getSSLSocketFactory() {
        if (null != sSSLSocketFactoryInstance)
            return sSSLSocketFactoryInstance;

        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            sSSLSocketFactoryInstance = new MySSLSocketFactory(trustStore);
            sSSLSocketFactoryInstance.setHostnameVerifier(
                    SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }

        return sSSLSocketFactoryInstance;
    }

    private static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] {
                    tm
            }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    /**
     * 从HttpResponse中读取mimetype
     * 
     * @param response
     * @return
     * @author caisenchuan
     */
    public static String getMimeType(HttpResponse response) {
        String ret = "";

        if (response != null) {
            Header mimeType = response.getFirstHeader("Content-Type");
            if (mimeType != null) {
                String entry = mimeType.getValue();
                int i = entry.indexOf(";");
                if (i != -1) {
                    ret = entry.substring(0, i);
                }
                else {
                    ret = entry;
                }
            }
        }

        SimpleLog.d(TAG, "getMimeType : " + ret);

        return ret;
    }

    public static void notifyMediaScannerNewFile(final Context context,
            final String filePath, final String mimeType) {
     // 通知MediaScannerService扫描
        String[] scanPaths = {
                filePath,
        };
        String [] scanMimeTypes = {
                mimeType,
        };
        MediaScannerConnection.scanFile(context, scanPaths, scanMimeTypes,
                new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                    }
            
        });
    }
}
