/**
 * @brief     Package com.polar.browser.utils
 * @author    zhouchenguang
 * @since     1.0.0.0
 * @version   1.0.0.0
 * @date      2012-12-20
 */

package com.polar.browser.download_refactor.util;

/**
 * @file      KFile.java
 * @brief     This file is part of the Utils module of KBrowser project. \n
 *            This file serves as "java" source file that presents common file 
 *            utilities that would be required by all of the modules. \n
 * 
 * @author zhouchenguang
 * @since 1.0.0.0
 * @version 1.0.0.0
 * @date 2012-12-20
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
 * zhouchenguang[7897]   |  2012-12-20  | <xxxxxxxx> | Initial Created.\n
 *\n
 * \endif
 *
 * <tt>
 *\n
 * Release History:\n
 *\n
 * Author (Name[WorkID]) | ModifyDate | Version | Description \n
 * --------------------- | ---------- | ------- | -----------------------------\n
 * zhouchenguang[7897]   | 2012-12-20 | 1.0.0.0 | Initial created. \n
 *\n
 * </tt>
 */
//=============================================================================
//                                  IMPORT PACKAGES
//=============================================================================

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.polar.browser.utils.SimpleLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

// =============================================================================
//                               CLASS DEFINITIONS
// =============================================================================
/**
 * @class KFile.java
 * @brief tools for common file operation. \n
 * @author zhouchenguang
 * @since 1.0.0.0
 * @version 1.0.0.0
 * @date 2012-12-20
 * @par Applied: External
 */
public class KFile {
    /**
     * @brief For debug.
     */
    private static final String TAG = "KFile";

    /**
     * SD卡上用于存储咱们的数据的文件夹名字
     * 
     * @author caisenchuan
     */
    public static final String SDCARD_KBROWSER_DIR = "/CM Browser";
    /**
     * SD上存储db备份数据的根目录
     * 
     * @author caisenchuan
     */
    public static final String SDCARD_BACKUP_DIR = SDCARD_KBROWSER_DIR + "/backup";

    public static final String SDCARD_DCIM = "/DCIM";

    /*
     * 文件类型 by caisenchuan
     */
    public static final String APK = "apk";
    public static final String DOC = "doc";
    public static final String AUDIO = "audio";
    public static final String PIC = "pic";
    public static final String VIDEO = "video";
    public static final String ZIP = "zip";
    public static final String OTHER = "other";

    private static final int FIX_N = 0;
    private static final int MIME_N = 1;
    private static final int TYPE_N = 2;

    /**
     * MIME list 请将最常用的放在最前面,提高查询的效率
     */
    private static final String[][] MIME_MapTable = {
            // {.name, MIME type, Type}
            {".3gp", "video/3gpp", VIDEO},
            {".aiff", "audio/x-aiff", AUDIO},
            {".apk", "application/vnd.android.package-archive", APK},
            {".asf", "video/x-ms-asf", VIDEO},
            {".au", "audio/basic", AUDIO},
            {".avi", "video/x-msvideo", VIDEO},
            {".bin", "application/octet-stream", OTHER},
            {".bmp", "image/bmp", PIC},
            {".c", "text/plain", DOC},
            {".class", "application/octet-stream", OTHER},
            {".conf", "text/plain", DOC},
            {".cpp", "text/plain", DOC},
            {".doc", "application/msword", DOC},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", DOC},
            {".dot", "application/msword", DOC},
            {".dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template", DOC},
            {".exe", "application/octet-stream", OTHER},
            {".flv", "video/x-flv", VIDEO},
            {".gif", "image/gif", PIC},
            {".gtar", "application/x-gtar", ZIP},
            {".gz", "application/x-gzip", ZIP},
            {".h", "text/plain", DOC},
            {".htm", "text/html", DOC},
            {".html", "text/html", DOC},
            {".jar", "application/java-archive", ZIP},
            {".java", "text/plain", DOC},
            {".jpeg", "image/jpeg", PIC},
            {".jpg", "image/jpeg", PIC},
            {".js", "application/x-javascript", DOC},
            {".log", "text/plain", DOC},
            {".m3u", "audio/x-mpegurl", AUDIO},
            {".m4a", "audio/mp4a-latm", AUDIO},
            {".m4b", "audio/mp4a-latm", AUDIO},
            {".m4p", "audio/mp4a-latm", AUDIO},
            {".m4u", "video/vnd.mpegurl", VIDEO},
            {".m4v", "video/x-m4v", VIDEO},
            {".mid", "audio/midi", AUDIO},
            {".midi", "audio/midi", AUDIO},
            {".mov", "video/quicktime", VIDEO},
            {".mp2", "audio/x-mpeg", AUDIO},
            {".mp3", "audio/x-mpeg", AUDIO},
            {".mp4", "video/mp4", VIDEO},
            {".mpc", "application/vnd.mpohun.certificate", OTHER},
            {".mpe", "video/mpeg", VIDEO},
            {".mpeg", "video/mpeg", VIDEO},
            {".mpg", "video/mpeg", VIDEO},
            {".mpg4", "video/mp4", VIDEO},
            {".mpga", "audio/mpeg", AUDIO},
            {".msg", "application/vnd.ms-outlook", OTHER},
            {".ogg", "audio/ogg", AUDIO},
            {".pdf", "application/pdf", DOC},
            {".png", "image/png", PIC},
            {".pps", "application/vnd.ms-powerpoint", DOC},
            {".ppt", "application/vnd.ms-powerpoint", DOC},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", DOC},
            {".prop", "text/plain", DOC},
            {".rar", "application/rar", ZIP},
            {".rc", "text/plain", DOC},
            {".rmvb", "audio/x-pn-realaudio", AUDIO},
            {".rtf", "application/rtf", DOC},
            {".sh", "text/plain", DOC},
            {".tar", "application/x-tar", ZIP},
            {".tif", "image/tiff", PIC},
            {".tiff", "image/tiff", PIC},
            {".tgz", "application/x-compressed", ZIP},
            {".txt", "text/plain", DOC},
            {".v", "video/*", VIDEO}, // 视频的特殊类型,自定义
            {".wav", "audio/x-wav", AUDIO},
            {".wma", "audio/x-ms-wma", AUDIO},
            {".wmv", "audio/x-ms-wmv", AUDIO},
            {".wps", "application/vnd.ms-works", DOC},
            {".xls", "application/vnd.ms-excel", DOC},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", DOC},
            {".xml", "text/xml", DOC},
            {".xml", "text/plain", DOC},
            {".z", "application/x-compress", ZIP},
            {".zip", "application/zip", ZIP},
            {"", "*/*", OTHER}
    };

    // -------------------------------------------------------------------------
    /**
     * @brief get MIME type by file.\n
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] FILE .\n
     * @return String MIME TYPE.
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static String getMIMEType(File file) {
        String type = "*/*"; // application/unknown

        String fName = file.getName();

        // search for .
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }

        /* get file .name */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end.equals("")) {
            return type;
        }

        // search MIME list for match mime type
        // TODO 使用HashMap等更高效的方式实现, by caisenchuan
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][FIX_N])) {
                type = MIME_MapTable[i][MIME_N];
            }
        }

        // KLog.debug(TAG, "getMIMEType : " + type);

        return type;
    }

    /**
     * @brief zip files to zip format
     * @param fs
     * @param zf
     * @return true if success
     */
    public static boolean zipFiles(File fs[], File zf) {
        boolean result = false;

        ZipOutputStream zo = null;
        FileInputStream fi = null;
        try {
            final int buff_len = 1024;
            byte buff[] = new byte[buff_len];

            zo = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zf), 1024 * 4));
            for (int i = 0; i < fs.length; i++) {
                if (fs[i] == null)
                    continue;
                ZipEntry ze = new ZipEntry(fs[i].getName());
                zo.putNextEntry(ze);
                fi = new FileInputStream(fs[i]);
                while (true) {
                    int count = fi.read(buff, 0, buff_len);
                    if (count <= 0) {
                        break;
                    }
                    zo.write(buff, 0, count);
                }
                zo.closeEntry();
                fi.close();
                fi = null;
            }
            zo.flush();
            result = true;
        } catch (FileNotFoundException e) {
            SimpleLog.e(TAG, "Error when opening file");
            SimpleLog.e(e);
        } catch (IOException e) {
            SimpleLog.e(TAG, "Error when reading/writing file");
            SimpleLog.e(e);
        } finally {
            if (zo != null) {
                try {
                    zo.close();
                } catch (IOException e) {
                    SimpleLog.e(TAG, "Error when closing Zip output stream");
                    SimpleLog.e(e);
                    result = false;
                }
            }
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException e) {
                    SimpleLog.e(TAG, "Error when closing file input stream");
                    SimpleLog.e(e);
                }
            }
        }

        return result;
    }

    /**
     * @brief unzip file to the folder
     * @param zipFilePath path of the file which to be unzip
     * @param folderPath the path which the files unziped to
     * @return
     * @throws ZipException
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    public static boolean unZipFile(String zipFilePath, String folderPath) {
        // public static void upZipFile() throws Exception{
        boolean flag = false;
        try {
            ZipFile zfile = new ZipFile(zipFilePath);
            Enumeration zList = zfile.entries();
            ZipEntry ze = null;
            byte[] buf = new byte[1024];
            while (zList.hasMoreElements()) {
                ze = (ZipEntry) zList.nextElement();
                if (ze.isDirectory()) {
                    String dirstr = folderPath + ze.getName();
                    // dirstr = KSystemUtils.recoverString(dirstr);
                    File f = new File(dirstr);
                    f.mkdir();
                    continue;
                }
                OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(
                        folderPath, ze.getName())));
                InputStream is2 = new BufferedInputStream(zfile.getInputStream(ze));
                int readLen = 0;
                while ((readLen = is2.read(buf, 0, 1024)) != -1) {
                    os.write(buf, 0, readLen);
                }
                is2.close();
                os.close();
            }
            flag = true;
            zfile.close();
        } catch (Exception e) {
        	SimpleLog.e(e);
        }
        return flag;
    }

    private static File getRealFileName(String baseDir, String relativeFileName) {
        String[] dirs = relativeFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length >= 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                try {
                    // substr.trim();
                    substr = new String(substr.getBytes("8859_1"), "GB2312");

                } catch (UnsupportedEncodingException e) {
                	SimpleLog.e(e);
                }
                ret = new File(ret, substr);

            }
            if (!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length - 1];
            try {
                // substr.trim();
                substr = new String(substr.getBytes("8859_1"), "GB2312");
            } catch (UnsupportedEncodingException e) {
            	SimpleLog.e(e);
            }

            ret = new File(ret, substr);
            return ret;
        }
        return ret;
    }

    /**
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir =
     * apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     * 摘自：http://www.cnblogs.com/3dant/archive/2012/04/25/2469913.html
     * 
     * @param context - 上下文
     * @param apkPath - apk的路径
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        if (context == null) {
            SimpleLog.e(TAG, "context == null!");
            return null;
        }

        if (apkPath == null) {
            SimpleLog.e(TAG, "apkPath == null!");
            return null;
        }

        File file = new File(apkPath);
        if (!file.exists()) {
            SimpleLog.e(TAG, "file not exist : " + apkPath);
            return null;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                SimpleLog.e("ApkIconLoader", e.toString());
            }
        }

        return null;
    }

    /**
     * 获取某个文件的类型
     * 
     * @param filePath - 文件路径
     * @author caisenchuan
     */
    public static String getFileType(String filePath)
    {
        File file = new File(filePath);
        String type = OTHER;

        /*
         * 文件不存在也可以getName(), by caisenchuan if (!file.exists()) { return type;
         * }
         */

        String fName = file.getName();

        // search for .
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0)
        {
            return type;
        }

        /* get file .name */
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end.equals(""))
        {
            return type;
        }

        // search type list for match file type
        // TODO 使用HashMap等更高效的方式实现, by caisenchuan
        for (int i = 0; i < MIME_MapTable.length; i++)
        {
            if (end.equals(MIME_MapTable[i][FIX_N]))
            {
                type = MIME_MapTable[i][TYPE_N];
                break; // 适配第一个符合的类型
            }
        }

        // KLog.debug(TAG, "getFileType : " + type);

        return type;
    }

    /**
     * 根据mime读取文件类型
     * 
     * @param mime
     * @return 文件类型
     * @author caisenchuan
     */
    public static String getTypeByMimeType(String mime)
    {
        String type = OTHER;

        if (mime == null)
        {
            return type;
        }

        // TODO 使用HashMap等更高效的方式实现, by caisenchuan
        for (int i = 0; i < MIME_MapTable.length; i++)
        {
            if (mime.equals(MIME_MapTable[i][MIME_N]))
            {
                type = MIME_MapTable[i][TYPE_N];
                break; // 适配第一个符合的类型
            }
        }

        // KLog.debug(TAG, "getTypeByMimeType : " + type);

        return type;
    }

    /**
     * 关闭文件,忽略报错
     * 
     * @param c
     * @author caisenchuan
     */
    private static void close(Closeable c) {
        if (c == null) {
            SimpleLog.e(TAG, "c == null! ");
            return;
        }

        try {
            c.close();
        } catch (Exception e) {
            SimpleLog.e(e);
        }

        return;
    }

    /**
     * 复制文件,若目标文件不存在则创建之,若存在则覆盖之
     * 
     * @param source - 源文件
     * @param target - 目标文件
     * @author caisenchuan
     */
    public static void copyFile(File source, File target) {
        if (source == null || target == null) {
            SimpleLog.e(TAG, "source == null or target == null!");
            return;
        }

        if (!source.exists()) {
            SimpleLog.e(TAG, "source not exist!");
            return;
        }

        if (!target.exists()) {
            // 创建父级目录
            File parent = target.getParentFile();
            if (!parent.exists()) {
                SimpleLog.e(TAG, "create parent file : " + parent.toString());
                parent.mkdirs();
            }
            // 创建文件
            try {
                target.createNewFile();
            } catch (IOException e) {
                SimpleLog.e(e);
                return;
            }
        }

        // KLog.d(TAG, "copy from : " + source.toString() + ", to : " +
        // target.toString());

        InputStream fis = null;
        OutputStream fos = null;
        try {
            fis = new BufferedInputStream(new FileInputStream(source));
            fos = new BufferedOutputStream(new FileOutputStream(target));
            int i;
            while ((i = fis.read()) != -1) {
                fos.write(i);
            }
            fos.flush();
        } catch (Exception e) {
        	SimpleLog.e(e);
        } finally {
            close(fis);
            close(fos);
        }

        return;
    }

    /**
     * 将文件重命名然后删除
     */
    public static boolean renameDeleteFile(File f) {
        File to = renameToOnlyFileName(f);

        if (to == null) {
        	SimpleLog.e(TAG, "to == null !");
            return false;
        }

        SimpleLog.d(TAG, "Delete : " + to.getAbsolutePath());
        
        if (to.delete()) {
            return true;
        } else {
            SimpleLog.e(TAG, "Delete faild!");
            return false;
        }
    }

    /**
     * 将文件或文件夹自动重命名成唯一的文件名
     * 
     * @author caisenchuan
     */
    public static File renameToOnlyFileName(File f) {
        if (f == null) {
            SimpleLog.e(TAG, "f == null!");
            return null;
        }

        if (!f.exists()) {
            SimpleLog.e(TAG, "File not exist : " + f.toString());
            return null;
        }

        boolean ret = false;
        int i = 0;
        File to = null;
        while (true) {
            to = new File(f.getAbsolutePath() + System.currentTimeMillis() + "_" + i);
            if (!to.exists()) {
                ret = f.renameTo(to);
                SimpleLog.d(TAG, "Rename file to : " + to.getAbsolutePath());
                break;
            }

            i++;
        }

        if (ret) {
            return to;
        } else {
            SimpleLog.e(TAG, "Rename faild!");
            return null;
        }
    }
    
    public static String getMajorForMimetype(String mimetype) {
        if(mimetype == null)
            return "";
        int index = mimetype.indexOf("/");
        if (index == -1) 
            return mimetype;
        return mimetype.substring(0, index);
    }

// TODO Remove unused code found by UCDetector
//     /**
//      * 获得/data应用目录中的某个文件的File句柄
//      */
//     public static File getDataDBPath(Context context, String name) {
//         if (context == null) {
//             return null;
//         }
// 
//         return context.getDatabasePath(name);
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 获得SD卡的备份目录中的某个文件的File句柄
//      */
//     public static File getSDBackupPath(Context context, String name) {
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
//             file = new File(path + KFile.SDCARD_BACKUP_DIR + "/" + name);
//         }
// 
//         return file;
//     }
}
