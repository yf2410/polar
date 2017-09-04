package com.polar.browser.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.polar.browser.bean.ApkInfo;


/**
 * Created by yd_lp on 2016/10/25.
 */

final public class ApkUtils {


    private ApkUtils() {

    }

    public static boolean isApkInstalled(Context context, String apkPath) {
        String packageName = getPackageName(context, apkPath);
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(
                    packageName, 0);
        } catch (Exception e) {
            packageInfo = null;
        }
        return packageInfo != null;
    }

    public static String getPackageName(Context context, String apkPath) {
        String packageName = null;
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            packageName = appInfo.packageName;
        }
        return packageName;
    }

    public static ApkInfo getAppNameAndIsInstall(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        ApkInfo info = new ApkInfo();
        if(apkPath == null) return info;
        try {
            PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
            if (pkgInfo != null) {
                ApplicationInfo appInfo = pkgInfo.applicationInfo;
                //必须加以下两句，否则获取到的是包名
                appInfo.sourceDir = apkPath;
                appInfo.publicSourceDir = apkPath;
                info.setName(pm.getApplicationLabel(appInfo).toString());// 得到应用名
                String packageName = appInfo.packageName;
                if(packageName != null){
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                            packageName, 0);
                    info.setInstalled( packageInfo != null );  //得到安装情况
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 获取apk包的信息：版本号，名称，图标等
     * @param context
     * @param apkPath apk包的绝对路径
     */
    public static Drawable getPackageIcon(Context context,String apkPath) {
        Drawable icon1 = null;
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
        if (pkgInfo != null) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            String appName = pm.getApplicationLabel(appInfo).toString();// 得到应用名
            String packageName = appInfo.packageName; // 得到包名
            String version = pkgInfo.versionName; // 得到版本信息
            /* icon1和icon2其实是一样的 */
            icon1 = pm.getApplicationIcon(appInfo);// 得到图标信息
//            Drawable icon2 = appInfo.loadIcon(pm);
            String pkgInfoStr = String.format("PackageName:%s, Vesion: %s, AppName: %s", packageName, version, appName);
            Log.i("ApkUtils", String.format("PkgInfo: %s", pkgInfoStr));
        }
        return icon1;
    }
}
