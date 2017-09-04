package com.polar.browser.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.vclibrary.common.Constants;

/**
 * Created by FKQ on 2016/11/23.
 */

public class ShortCutUtil {

    private static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    private static final String ACTION_REMOVE_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";

    /**
     * 发送快捷方式到手机桌面
     * @param context
     */
    public static void addShortCutToDesktop(Context context) {

//        try {
            Parcelable icon = Intent.ShortcutIconResource.fromContext(context, R.drawable.affiliate_icon);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.polar.browser", "com.polar.browser.activity.BrowserActivty");
            intent.putExtra(CommonData.ACTION_GOTO_URL, "");
            intent.putExtra(CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_SHORTCUT);
//			String packageName = c.getPackageName();
//	        intent.setComponent(new ComponentName("com.polar.browser", "com.polar.browser.JuziApp"));
            Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            // 快捷方式的标题
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.launcher_affiliate_name));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            // 快捷方式的动作
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            // 是否允许重复添加
            addIntent.putExtra("duplicate", false);
            // 发送广播
            context.getApplicationContext().sendBroadcast(addIntent);
//        } catch (Exception e) {
//        }

//        try {
//            Parcelable icon = Intent.ShortcutIconResource.fromContext(context, R.drawable.affiliate_icon);
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            intent.setClassName("com.polar.browser", "com.polar.browser.activity.BrowserActivity");
////			String packageName = c.getPackageName();
////	        intent.setComponent(new ComponentName("com.polar.browser", "com.polar.browser.JuziApp"));
//            Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
//            // 快捷方式的标题
//            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.launcher_affiliate_name));
//
////            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "rtfgalgjfgdsgjfajfg");
//
//            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
//            // 快捷方式的动作
//            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
//            // 是否允许重复添加
//            addIntent.putExtra("duplicate", false);
//            // 发送广播
//            context.getApplicationContext().sendBroadcast(addIntent);
//        } catch (Exception e) {
//        }

//        try {
//            Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
//            // 不允许重复创建
//            addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
//            // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
//            // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
//            // 屏幕上没有空间时会提示
//            // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式
//
//            // 名字
//            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.launcher_affiliate_name));
//
//            // 图标
//            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//                    Intent.ShortcutIconResource.fromContext(context, R.drawable.affiliate_icon));
//
//            // 设置关联程序
//            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
//            launcherIntent.setClass(context, WelcomeActivity2.class);
//            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
//            // 发送广播
//            context.getApplicationContext().sendBroadcast(addShortcutIntent);
//        } catch (Exception e) {
//
//        }
    }

    /**
     * 创建桌面快捷方式
     */
    public static void addShortCutToDesktop(Context c, String title) {
        try {
            Parcelable icon = Intent.ShortcutIconResource.fromContext(c, R.drawable.affiliate_icon);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.polar.browser", "com.polar.browser.activity.BrowserActivity");
            intent.putExtra(CommonData.ACTION_TYPE_FROM, Constants.TYPE_FROM_DESKTOP_LAUNCHER);
//			String packageName = c.getPackageName();
//	        intent.setComponent(new ComponentName("com.polar.browser", "com.polar.browser.JuziApp"));
            Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            // 快捷方式的标题
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);

            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            // 快捷方式的动作
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            // 是否允许重复添加
            addIntent.putExtra("duplicate", false);
            // 发送广播
            c.getApplicationContext().sendBroadcast(addIntent);
        } catch (Exception e) {
        }
    }




    public static void removeShortcut(Context context) {
        // remove shortcut的方法在小米系统上不管用，在三星上可以移除
//        Intent intent = new Intent(ACTION_REMOVE_SHORTCUT);
//
//        // 名字
//        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.launcher_affiliate_name));
//
//        // 设置关联程序
//        Intent launcherIntent = new Intent(context.getApplicationContext(),
//                WelcomeActivity2.class).setAction(Intent.ACTION_MAIN);
//
//        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
//
//        // 发送广播
//        context.getApplicationContext().sendBroadcast(intent);
    }
}
