package com.polar.browser.download_refactor;

import android.content.Context;
import android.os.Environment;

import com.polar.browser.JuziApp;
import com.polar.browser.download_refactor.util.KSystemUtils;

import java.io.File;


/**
 * @author zhangjun
 * donwload功能的环境抽象
 * 暂时记录下载功能的what changes到这里：
 * 2014/07/13 主要修改自定义下载路径后文件目录的权限问题：
 * http://www.doubleencore.com/2014/03/android-external-storage/
 * 1、去掉了DownloadSaveSDCardPath 选项，没有用到相对路径保存文件的逻辑，没有意义
 * 2、去掉了DownloadSaveSDCardPath 选项相关的ui交互代码
 * 3、去掉了自动嗅探可用SDCard的逻辑
 * 4、修正磁盘空间不足检测的bug
 * 5、用户选择目录作为默认下载保存路径时，检查目录是否可以创建可写文件，如果可写则设置成功；否则通知
 *      用户该目录不可写
 * 6、用户选择SDCard时，如果当前系统为4.4及以后，则hardcode唯一可写路径，并嗅探是否真正可写，
 *      如果不可写，则通知用户该SDCard不可写；如果可写则提示用户是否要保存到这里。
 * 7、新加逻辑点的统计、下载过程中不能创建和写文件的路径及系统版本号上传
 * 8、下载任务开始(含base64图片长按保存)时，检查目标路径，如果可写，正常下载；如果不可写：
 *      1) 如果用户设置目录为空，则提示用户需要重新选择目录
 *      2) 如果用户设置目录与目标目录相等，则提示用户需要重新选择目录
 *      3) 如果用户设置目录与目标目录不相等，如果可写，则使用用户设置目录继续下载；
 *          如果也不能写，则提示用户需要重新写目录
 * 9、下载列表视图遗留bug：base64图片下载成功但是查看下载地址失败；base64图片可能获取".null"的
 *      扩展名
 */
public class DownloadEnvironment {
    public static final int ANDROID_4_4_2  = 19; // first 4.4 API

    /**
     * 系统的SDK版本号
     * @return
     */
    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

     /**
     * 获得app的context,其他模块不应使用这里的context。从KBrowserEngine获取，未必生命周期
     * 一致。
     * @hide
     */
    public static Context getContext() {
        return JuziApp.getInstance().getApplicationContext();
    }

    /**
     * 用于展现ui时的context
     * @hide
     */
    public static Context getUiContext() {
    	// TODO Fix me
//        return KApplication.getInstance().getTopActivity();
        return JuziApp.getInstance();
    }

    /**
     * 获取Interal Storage上的默认存储目录，app私有目录，卸载时存在这里的文件会被删除
     * @return
     */
    public static String getInternalStorageSaveDir() {
        File filesDirFile = KSystemUtils.getFilesDir(getContext());
        File downloadDirFile = new File(filesDirFile,
                Environment.DIRECTORY_DOWNLOADS);
        return downloadDirFile.getAbsolutePath();
    }

    /**
     * 获取External Storage上的公开存储目录，卸载时不会删除
     * @return
     */
    public static String getExternalStoragePublicSaveDir() {
        File filesDirFile = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        return filesDirFile.getAbsolutePath();
    }
}
