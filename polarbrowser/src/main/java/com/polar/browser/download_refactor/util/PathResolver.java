
package com.polar.browser.download_refactor.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.polar.browser.JuziApp;
import com.polar.browser.download_refactor.DownloadEnvironment;
import com.polar.browser.download_refactor.DownloadUtil;
import com.polar.browser.download_refactor.util.KSystemUtils.SDCard;
import com.polar.browser.manager.VCStoragerManager;
import com.polar.browser.utils.SimpleLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 与文件操作相关的工具集合
 * 
 * @author caisenchuan
 */
public class PathResolver
{
    private static final String TAG = PathResolver.class.getSimpleName();
    /** 是否打印调试信息 */
    private static final boolean VERBOSE = false;

    /**
     * 静态的SD卡信息<br />
     * !!!注意：其中的mount只会初始化一次，不能反映当前状态，这个对象的主要作用是防止过于频繁的遍历所有的SD卡信息
     * 
     * @author caisenchuan
     */
    private static List<SDCard> mSDCardInfoList = new ArrayList<SDCard>();

    static {
        // 构建类时就调用，否则如果外面没有调用初始化但直接使用了sDefaultFolder等，会取到null，by caisenchuan
        initialize();
    }

    /**
     * 初始化各个下载目录路径
     */
    public static void initialize() {
        try {
            SimpleLog.i(TAG, "initialize()");

            mSDCardInfoList = KSystemUtils
                    .getAllSdcardState(JuziApp.getInstance().getApplicationContext());
        } catch (Exception e) {
        	SimpleLog.e(e);
        }
    }

    /**
     * 根据文件路径获得对应SD卡路径
     * 
     * @param file_path
     * @return
     */
    public static String getSDCardPathByFile(String file_path) {
        String ret = "";

        // 依次循环各个SD卡路径，判断文件路径是否以其开头
        if (!TextUtils.isEmpty(file_path)) {
            String path = file_path.trim();
            path = path.toLowerCase();

            for (SDCard sd : mSDCardInfoList) {
                if (sd != null &&
                        !TextUtils.isEmpty(sd.getPath())) {

                    String sd_path = sd.getPath();
                    sd_path = sd_path.toLowerCase();

                    path = path + "/";
                    sd_path = sd_path + "/"; // 读出来的路径最后是没有斜杠的，导致 /sdcard 与
                                             // /sdcard-ext 会无法区分，所以手动加斜杠

                    if (path.startsWith(sd_path)) {
                        ret = sd.getPath();
                        break;
                    }
                }
            }
        }

        if (VERBOSE) {
            SimpleLog.d(TAG, String.format("getSDCardPathByFile , file_path : %s , ret : %s", file_path, ret));
        }

        return ret;
    }

    /**
     * 根据文件路径判断对应SD卡状态
     * 
     * @param context
     * @param file_path
     * @return
     */
    public static boolean checkSDCardMountByFile(Context context, String file_path) {
        boolean ret = true;

        String sd_path = getSDCardPathByFile(file_path);
        if (!TextUtils.isEmpty(sd_path)) {
            ret = KSystemUtils.checkSDCardMount(context, sd_path);
        }

        if (VERBOSE) {
            SimpleLog.d(TAG, String.format("checkSDCardMountByFile , file_path : %s , ret : %s", file_path, ret));
        }

        return ret;
    }

    /**
     * 根据SD卡的路径获取对应的名字
     * 
     * @param context
     * @param path
     * @return
     * @author caisenchuan
     */
    public static String getSDCardTextByPath(Context context, String path) {
        String ret = "";
        if (!TextUtils.isEmpty(path) && mSDCardInfoList != null) {
            for (SDCard sd : mSDCardInfoList) {
                if (sd.getPath().equals(path)) {
                    ret = sd.getName(context);
                    break;
                }
            }
        }
        return ret;
    }

// TODO Remove unused code found by UCDetector
//     public static Object invokeMethod(Object owner, String methodName, Object[] args) throws Exception {
//         
//         Class<?> ownerClass = owner.getClass();
//     
//         Class<?>[] argsClass = new Class[args.length];
//     
//         for (int i = 0, j = args.length; i < j; i++) {
//             argsClass[i] = args[i].getClass();
//         }
// 
//          Method method = ownerClass.getMethod(methodName,argsClass);
//     
//         return method.invoke(owner, args);
//    }

    public static String getDefaultFileDir() {
        String state = Environment.getExternalStorageState();
        if (state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return DownloadEnvironment.getExternalStoragePublicSaveDir();

        return DownloadEnvironment.getInternalStorageSaveDir();
    }

    /**
     * 获取下载文件的存储路径
     * 
     * @param fileName
     * @param customFolder
     * @return
     * @author caisenchuan
     */
    public static String getDownloadFileDir(String customFolder) {
        String folder = "";
//        String settingsDownloadPath = SettingsModel.getInstance().getDefaultDownloadPath();
        String settingsDownloadPath = VCStoragerManager.getInstance().getDownloadDirPath();
        if (!DownloadUtil.nullOrEmptyString(customFolder)) {
            // 若使用自定义文件夹
            folder = customFolder;
        } else if (!DownloadUtil.nullOrEmptyString(settingsDownloadPath)) {
            folder = settingsDownloadPath;
        } else {
            folder = getDefaultFileDir();
        }

        return addDirectorySuffix(folder);
    }

    private static String addDirectorySuffix(String path) {
        if (path.endsWith("/"))
            return path;
        return String.format("%s/", path); // 添加/后缀
    }

    /**
     * 获取下载文件的路径
     * 
     * @param fileName
     * @param customFolder
     * @return
     * @author caisenchuan
     */
    public static String getDownloadFilePath(String fileName, String customFolder) {
        String folder = null;

        folder = getDownloadFileDir(customFolder);

        if (folder.endsWith("/"))
            return folder + fileName;
        return folder + "/" + fileName;
    }

    /**
     * 获取分段文件路径
     * 
     * @param fileName
     * @param partIndex
     * @param customFolder
     * @return
     */
    public static String getPartFilePath(String fileName, int partIndex,
            String customFolder) {
        return String.format("%s.tmp.part%d",
                getDownloadFilePath(fileName, customFolder), partIndex);
    }

    /**
     * 获得一个唯一的文件名，判断依据是磁盘上是否有重名的文件或者分段文件
     * 
     * @param fileName 要修改的文件名
     * @param customFolder 自定义路径, 不使用则置为""
     * @return 唯一的文件名
     */
    public static String getUniqueName(String fileName, String customFolder)
    {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }

        String ret = fileName;
        String candidate = fileName;
        int i = 1;
        // 依次尝试各个后缀的文件名，知道找到第一个不重名的文件
        while (true) {
            if (i > 1) {
                // 从第二个文件开始添加数字后缀
                candidate = addPostfixToFilename(ret, i - 1);
            }

            File file = new File(getDownloadFilePath(candidate, customFolder));
//            File partFile = new File(getPartFilePath(candidate, 1, customFolder));
//            if (!file.exists() && !partFile.exists()) {
//                // 若文件名与分段文件都不存在，则说明不重名
//                ret = candidate;
//                break;
//            }
            if (!file.exists()) {
                ret = candidate;
                break;
            }

            i++;
        }

        return ret;
    }

    /**
     * 给文件名添加数组后缀
     * 
     * @param fileName 文件名
     * @param fix 要添加的数字
     * @return 添加后缀后的文件名
     * @author caisenchuan
     */
    public static String addPostfixToFilename(String fileName, int fix) {
        String ret = fileName;

        if (!TextUtils.isEmpty(fileName)) {
            int sep = fileName.lastIndexOf('.');
            if (sep == -1) {
                // 无扩展名
                ret = String.format("%s(%d)", fileName, fix);
            } else {
                // 有扩展名，则在文件名后加后缀
                ret = String.format("%s(%d)%s",
                        fileName.substring(0, sep),
                        fix,
                        fileName.subSequence(sep, fileName.length()));
            }
        }

        return ret;
    }

    /**
     * 从完整路径中解析出文件所在路径，这个方法主要用于普通文件下载时，从文件名路径分析出其所在目录的路径
     * 
     * @param path 要解析的文件/文件夹路径
     * @return 如果path是文件，则返回其父级目录路径，如果path是目录，则直接返回其路径，无论哪种情况，返回的路径都以"/"结尾
     * @author caisenchuan
     */
    public static String getDirFromPath(String path) {
        String ret = path;

        if (!TextUtils.isEmpty(path)) {
            try {
                File f = new File(path);
                if (f.isDirectory()) {
                    SimpleLog.d(TAG, "getDirFromPath , isDirectory");
                    // 如果是目录
                    ret = String.format("%s/", f.getAbsolutePath());
                } else if (path.endsWith("/")) {
                    SimpleLog.d(TAG, "getDirFromPath , end with /");
                    // 这个比较纠结，在文件不存在时只能通过路径最后一个字符是否是/来判断，
                    // 因为对于/sdcard/aaa这样的路径实际是不知到它是文件还是文件夹的
                    ret = String.format("%s/", f.getAbsolutePath());
                } else {
                	SimpleLog.d(TAG, "getDirFromPath , get dir from file path");
                    // 其他都认为是文件，取出其父级目录，并且添加/后缀
                    ret = String.format("%s/", f.getParent());
                }
            } catch (Exception e) {
                SimpleLog.e(e);
                ret = path;
            }
        }
		
        SimpleLog.d(TAG, "getDirFromPath , ret = " + ret);
		
        return ret;
    }

    /**
     * 获取可用SD卡列表的字符串，用","连接
     * 
     * @param context
     * @return
     * @author caisenchuan
     */
    public static String getAvailableSDCardListText(Context context) {
        StringBuffer ret = new StringBuffer();

        if (mSDCardInfoList != null) {
            for (SDCard sd : mSDCardInfoList) {
                if (sd.isMount()) {
                    if (ret.length() > 0) {
                        ret.append(",");
                    }
                    ret.append(sd.getName(context));
                }
            }
        }

        return ret.toString();
    }

    /**
     *  判断依磁盘上是否有重名的文件
     *
     * @param fileName 要修改的文件名
     * @param customFolder 自定义路径, 不使用则置为""
     * @return 唯一的文件名
     */
    public static boolean isDownloadFileExists(String fileName, String customFolder) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        File file = new File(getDownloadFilePath(fileName, customFolder));
        if (file.exists()) {
            return true;
        }
        return false;
    }
}
