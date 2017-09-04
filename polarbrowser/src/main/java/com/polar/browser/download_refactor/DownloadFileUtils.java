package com.polar.browser.download_refactor;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.download_refactor.DownloadException.ExceptionCode;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author zhangjun
 *与文件操作相关的帮助函数封装
 */
public class DownloadFileUtils {
    
    private static final String GET_EXTERNAL_FILES_DIRS = "getExternalFilesDirs";

    /**
     * 嗅探文件路径是否可写
     * @param filePath 嗅探目标文件的全路径
     * @param delAfterExit 嗅探目标文件如果是由嗅探程序创建的，是否要在离开时删除
     * @return
     * @throws DownloadException
     *      ExceptionCode.TargetFilePathIsPlacedByDir
     *      ExceptionCode.TargetDirPathIsPlacedByFile
     */
    public static boolean isFilePathCanWrite(String filePath,
            boolean delAfterExit) throws DownloadException {
        if (TextUtils.isEmpty(filePath))
            return false;

        File file = new File(filePath);
        return isDirPathCanWrite(file.getParent(), file.getName(),
                delAfterExit);
    }

    /**
     * 嗅探|dirPath|下文件名是否可写，！！！假定|dirPath|可写
     * @param dirPath 嗅探目标文件所在目录路径
     * @param fileName 嗅探目标文件的文件名
     * @param delAfterExit 嗅探目标文件如果是由嗅探程序创建的，是否要在离开时删除
     * @return
     * @throws DownloadException
     *      ExceptionCode.TargetFilePathIsPlacedByDir
     */
    public static boolean isFileNameCanWrite(String dirPath,
            String fileName, boolean delAfterExit) throws DownloadException {
        if (TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(fileName))
            return false;

        File file = new File(dirPath, fileName);
        final boolean isFileExists = file.exists(); // 文件是否原来就存在的
        if (isFileExists && !file.isFile())
            throw new DownloadException(
                    ExceptionCode.TargetFilePathIsPlacedByDir);
        
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        if (!file.exists())
            return false;

        try {
            // 暂时信任canWrite权限判断，否则自己写1byte进行嗅探
            if (!file.canWrite())
                return false;
            return true;
        } finally {
            // 原本不存在，由我们临时创建出来的根据选项参数决定是否删除
            if (!isFileExists && delAfterExit)
                file.delete();
        }
    }

    /**
     * 嗅探目录是否可写，！！！会创建目录
     * @param dirPath 嗅探目标目录的路径
     * @param testFileName 嗅探用的临时文件名，如果isEmpty()则会使用内部临时文件名
     * @param delAfterExit 嗅探用的文件如果是由嗅探程序创建的，是否要在离开时删除
     * @return
     * @throws DownloadException
     *      ExceptionCode.TargetDirPathIsPlacedByFile
     *      ExceptionCode.TargetFilePathIsPlacedByDir
     */
    public static boolean isDirPathCanWrite(String dirPath,
            String testFileName, boolean delAfterExit)
            throws DownloadException {
        if (TextUtils.isEmpty(dirPath))
            return false;

        File dirFile = new File(dirPath);
        if (dirFile.exists() && !dirFile.isDirectory())
            throw new DownloadException(
                    ExceptionCode.TargetDirPathIsPlacedByFile);

        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                if (!mkdirsAdvanced(dirFile)) {
                    return false;
                }
            }
        }

        if (!dirFile.exists())
            return false;
        if (TextUtils.isEmpty(testFileName)) {
            testFileName = getUniqueName("test_can_write.temp", dirPath);
            delAfterExit = true; // 容错逻辑，不管外部如何设置，内部临时文件都应被删除
        }
        return isFileNameCanWrite(dirPath, testFileName, delAfterExit);
    }
    
    /*
     * 4.4之后的系统中，第二个扩展存储区（sd卡）中仅/Android/data/[packagename]/files 目录可以写入。
     * 而这个目录默认并未创建，mkdirs()函数也无法创建。
     * 该目录会在调用系统API getExternalFilesDirs() 时自动创建.
     * 由于我们获取路径使用了黑科技，并未调用这个函数，因此mkdirs()失败时需要尝试调用一下这个函数。
     * 该函数需要API level 19，因此使用method.invoke的方式调用。
     */
    private static boolean mkdirsAdvanced(File dirFile) {
        
        if (DownloadEnvironment.getSdkVersion() >= DownloadEnvironment.ANDROID_4_4_2 
                && JuziApp.getInstance().getApplicationContext() instanceof ContextWrapper) {
            try {
                ContextWrapper contextWrapper = (ContextWrapper) JuziApp.getInstance().getApplicationContext();
                Method method = contextWrapper.getClass().getMethod(GET_EXTERNAL_FILES_DIRS, String.class);
                String s = null;
                method.invoke(contextWrapper, s);
                
                dirFile.mkdirs();
                return dirFile.exists() && dirFile.isDirectory();
            } catch (Exception e) {
                return false;
            }
         }
        else {
            return false;
        }
    }

    /**
     * 以|fileName|为baseName，在|customDir|中获得唯一名，重名则附加形如(1)的后缀
     * @param fileName
     * @param customDir
     * @return
     */
    public static String getUniqueName(String fileName, String customDir) {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }

        String candidate = fileName;
        int i = 1;
        // 依次尝试各个后缀的文件名，知道找到第一个不重名的文件
        while (true) {
            if (i > 1) {
                // 从第二个文件开始添加数字后缀
                candidate = addSuffixToFileName(fileName, i);
            }

            File file = new File(customDir, candidate);
            if (!file.exists()) {
                // 若文件名不存在，则说明不重名
                return candidate;
            }

            i++;
        }
    }

    public static String getFileName(String filePath) {
        if (filePath == null)
            return "";
        String filename = filePath;
        int index = filename.lastIndexOf('/');
        if (index >= 0) {
            filename = filename.substring(index + 1);
        }
        return filename;
    }

    /**
     * 给文件名添加数组后缀
     * 
     * @param fileName 文件名
     * @param fix 要添加的数字
     * @return 添加后缀后的文件名
     */
    public static String addSuffixToFileName(String fileName, int fix) {
        if (TextUtils.isEmpty(fileName))
            return fileName;

        int sep = fileName.lastIndexOf('.');
        if (sep == -1) {
            // 无扩展名
            return String.format(java.util.Locale.US, "%s(%d)", fileName, fix);
        }

        // 有扩展名，则在文件名后加后缀
        return String.format(java.util.Locale.US, "%s(%d)%s", fileName.substring(0, sep), fix, fileName.subSequence(sep, fileName.length()));
    }

    /**
     * 黑科技兼容接口。具体sdcard的使用权限定义，可能会因不同的硬件厂商而yy出不同的权限定义和用
     * 法。official build和海外主流版本可以参照下面这段解惑。但三星等的无节操可能需要抓到问题时
     * 具体解决。
     * <p>In KitKat, the external storage API was split out to include multiple
     *  volumes; one “primary” and one or more “secondary.”  The primary volume
     *  is, for all intents and purposes, exactly the same as the previous 
     * single volume.  All APIs that existed prior to KitKat reference the 
     * primary external storage.  The secondary volume(s) modify write 
     * permissions a bit; they are globally readable under the same permission 
     * described above.  Directories outside the application’s own managed area
     *  (i.e. /Android/data/[PACKAGE_NAME]) are not writable at all by that 
     *  application.</p>
     *  /Android/data/<package_name>/files/
     * @param sdcardPathString
     * @param context
     * @return
     */
    public static String getSaveDirPathBySDCardPath(String sdcardPathString,
            Context context) {
        String packageDataString = "/Android/data/" + context.getPackageName()
                + "/files/Download";
        File dirFile = new File(sdcardPathString, packageDataString);
        return dirFile.getAbsolutePath();
    }

    public static boolean checkDownloadDirectoryCanWrite(final DownloadItemInfo info) {
        if (TextUtils.isEmpty(info.mFilePath))
            return false;
        String dir = new File(info.mFilePath).getParent();
        try {
            if (DownloadFileUtils.isDirPathCanWrite(dir, null, true))
                return true;
//			dir = SettingsModel.getInstance().getDefaultDownloadPath();
            dir = VCStoragerManager.getInstance().getDownloadDirPath();
            if (dir != null && DownloadFileUtils.isDirPathCanWrite(dir, null, true))
                return true;
        } catch (DownloadException e) {
            // android上应少对抗，暂时只统计
            e.printStackTrace();
        }
//		mManagerView.showCheckDirCanWriteDialog();
        SimpleLog.e("", "checkDownloadDirectoryCanWrite --- false");
        return false;
    }
}
