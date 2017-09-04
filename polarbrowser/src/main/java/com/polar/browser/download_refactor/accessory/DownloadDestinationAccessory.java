package com.polar.browser.download_refactor.accessory;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.polar.browser.manager.VCStoragerManager;

import java.io.File;

/**
 * 用于处理下载的保存路径相关
 * 相关细节可以参考 <url="http://www.doubleencore.com/2014/03/android-external-storage/"/> 等
 */
public class DownloadDestinationAccessory {

    /**
     * 获取下载保存路径
     *
     * @param context Context
     * @param customDir 自定义保存目录
     * @return 应该保存的路径位置
     */
    public static String getDestinationDir(Context context, String customDir) {
        // 自定义目录优先考虑
        if (!TextUtils.isEmpty(customDir))
            return customDir;
        return getDestinationDir(context);
    }

    /**
     * 获取下载保存路径
     *
     * @param context Context
     * @return 应该保存的路径位置
     */
    public static String getDestinationDir(Context context) {
        // 第二优先用户自定义下载路径
//        String dirInSettings = SettingsModel.getInstance().getDefaultDownloadPath();
    	// TODO 
        String dirInSettings = VCStoragerManager.getInstance().getDownloadDirPath();
        if (!TextUtils.isEmpty(dirInSettings))
            return dirInSettings;
        // 最后只能用我们的默认下载路径了
        return getDefaultDestinationDir(context);
    }

    /**
     * 获取默认下载路径。{@link #getDestinationDirInExternalStorage()}可用时优先用这里，否则使用
     * {@link #getDestinationDirInInternalStorage(Context)}
     *
     * @param context Context
     * @return 默认存储目录路径
     */
    public static String getDefaultDestinationDir(Context context) {
        String state = Environment.getExternalStorageState();
        if (state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return getDestinationDirInExternalStorage();
        return getDestinationDirInInternalStorage(context);
    }

    /**
     * 获取Interal Storage上的默认存储目录，app私有目录，仅我们自己可以随意读写，卸载时存在这里的文件会被删除
     *
     * @return 存储目录路径
     */
    public static String getDestinationDirInInternalStorage(Context context) {
        File filesDirFile = context.getFilesDir();
        File downloadDirFile = new File(filesDirFile, Environment.DIRECTORY_DOWNLOADS);
        return downloadDirFile.getAbsolutePath();
    }

    /**
     * 获取External Storage上的公开下载目录，所有app可以随意读写，卸载时不会删除。
     * Should be used for any file that the user can freely access, copy, delete.
     *
     * @return 存储目录路径
     */
    public static String getDestinationDirInExternalStorage() {
        File filesDirFile = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        return filesDirFile.getAbsolutePath();
    }
}
