package com.polar.browser.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.polar.browser.R;
import com.polar.browser.library.rx.RxBus;
import com.polar.browser.vclibrary.bean.events.LocationPerEvent;
import com.polar.browser.view.SetPermissionDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by duan on 16/8/31.
 */
public class PermissionsHelper {


    final public static int REQUEST_CODE_ASK_PERMISSION = 100;
    final public static int REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE = 101;
    final public static int REQUEST_CODE_ASK_ACCESS_COARSE_LOCATION = 102;

    /** 标识是否是从设置权限页面回来 **/
    private static boolean sIsBackFromSettingPermission;

    /**
     * 请求存储权限
     * @param activity
     */
    public static void requestPermissionOfWriteExternalStorage(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            SimpleLog.e("PermissionsHelper", "checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED   ?? " + (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED));
            SimpleLog.e("PermissionsHelper", "checkWriteExternalStoragePermission ==   ?? " + checkWriteExternalStoragePermission);
            SimpleLog.e("PermissionsHelper", "shouldShowRequestPermissionRationale == " + activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE));

            if (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {

//                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))

                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE}, REQUEST_CODE_ASK_WRITE_EXTERNAL_STORAGE);
                return;
            }

        }
    }

    /**
     * 请求粗略位置权限
     * @param activity
     */
    public static void requestPermissionOfCoarseLocation(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {

            // 检查Manifest是否申请了位置权限,没有的话,直接返回
            if (!hasPermissionInManifest(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                SimpleLog.e("PermissionsHelper", "权限清单-不包含-->" + Manifest.permission.ACCESS_COARSE_LOCATION);
                return;
            }

            int checkCoarseLocationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

            SimpleLog.e("PermissionsHelper", "checkCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ?? " + (checkCoarseLocationPermission != PackageManager.PERMISSION_GRANTED));
            SimpleLog.e("PermissionsHelper", "checkCoarseLocationPermission ==  ?? " + checkCoarseLocationPermission);
            SimpleLog.e("PermissionsHelper", "shouldShowRequestPermissionRationale == " + activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION));

            if (checkCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

//                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))

                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_ACCESS_COARSE_LOCATION);
                return;
            }

        }
    }

    /**
     * 请求权限,地理位置or存储权限
     * @param activity
     */
    public static void requestPermissions(Activity activity) {

        boolean needRequestWriteExternalStoragePermission = false;
        boolean needRequestCoarseLocationPermission = false;
        String[] permissionArray = null;

        if (Build.VERSION.SDK_INT >= 23) {
            // 存储权限
            int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                needRequestWriteExternalStoragePermission = true;
            }

            // 位置权限
            if (hasPermissionInManifest(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                SimpleLog.e("PermissionsHelper", "权限清单包含-->" + Manifest.permission.ACCESS_COARSE_LOCATION);
                int checkCoarseLocationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
                if (checkCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    needRequestCoarseLocationPermission = true;
                }
            }

            // 返回需要请求的权限
            if (needRequestWriteExternalStoragePermission && needRequestCoarseLocationPermission) {
                permissionArray = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION};
            } else if (needRequestWriteExternalStoragePermission){
                permissionArray = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            } else if (needRequestCoarseLocationPermission){
                permissionArray = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
            }

            // 请求权限弹框
            if (permissionArray != null) {
                ActivityCompat.requestPermissions(activity, permissionArray, REQUEST_CODE_ASK_PERMISSION);
            }
        }
    }

    /**
     * 处理权限回调,弹框
     * @param activity
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> deniedPermissions = new ArrayList<String>();
            if (requestCode == PermissionsHelper.REQUEST_CODE_ASK_PERMISSION) {
                boolean hasLocationPermission = false;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                        if (TextUtils.equals(permissions[i], Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            hasLocationPermission = true;
                        }
                    }
                }

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        if (TextUtils.equals(permissions[i], Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            RxBus.get().post(new LocationPerEvent(true));
                        }
                    }
                }


                if (hasLocationPermission && permissions.length == 1) {
                    // 20160912 如果只有location 权限就不弹设置框
                    return;
                }

                if (deniedPermissions.size() > 0) {
                    // 弹框提示用户设置权限
                    showPermissionsSettingDialog(activity, deniedPermissions);
                }

            }
        }

    }

    /**
     * 显示权限设置的对话框
     * @param activity
     * @param permissions
     */
    private static void showPermissionsSettingDialog(Activity activity, ArrayList<String> permissions) {
        Dialog dialog = new SetPermissionDialog(activity, R.style.dialogvideo, permissions);
        dialog.show();
        sIsBackFromSettingPermission = false;
    }

    /**
     * 检查Manifest是否申请了权限
     * @param activity
     * @param permission
     * @return
     */
    public static boolean hasPermissionInManifest(Activity activity, String permission) {
        try {
            PackageInfo pack = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] permissionStrings = pack.requestedPermissions;
            for (int i = 0; i < permissionStrings.length; i++) {
                if (TextUtils.equals(permission, permissionStrings[i])) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 打开应用详情页
     * @param context
     */
    public static void showAppDetail(Context context) {
        Intent appItent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        String pkg = "com.android.settings";
        String cls = "com.android.settings.applications.InstalledAppDetails";
        appItent.setComponent(new ComponentName(pkg, cls));
        String packageName = context.getPackageName();
        appItent.setData(Uri.parse("package:" + packageName));
        try {
            context.startActivity(appItent);
            sIsBackFromSettingPermission = true;
        } catch (Exception e) {
            SimpleLog.e(e);
        }
    }

    /**
     * 从APP权限设置页返回
     * @param activity
     */
    public static void onResume(Activity activity) {

        if (sIsBackFromSettingPermission) {
            // 重新检查权限,如果存储权限未被允许,继续弹窗

            boolean needRequestWriteExternalStoragePermission = false;
            boolean needRequestCoarseLocationPermission = false;
            String[] permissionArray = null;

            if (Build.VERSION.SDK_INT >= 23) {
                // 存储权限
                int checkWriteExternalStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (checkWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    needRequestWriteExternalStoragePermission = true;
                } else {
                    // 有存储权限的话,其他权限暂时先不需要,可以返回
                    return;
                }

                // 位置权限
                if (hasPermissionInManifest(activity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    SimpleLog.e("PermissionsHelper", "权限清单包含-->" + Manifest.permission.ACCESS_COARSE_LOCATION);
                    int checkCoarseLocationPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (checkCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        needRequestCoarseLocationPermission = true;
                    }
                }

                // 返回需要请求的权限
                if (needRequestWriteExternalStoragePermission && needRequestCoarseLocationPermission) {
                    permissionArray = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION};
                } else if (needRequestWriteExternalStoragePermission){
                    permissionArray = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                } else if (needRequestCoarseLocationPermission){
                    permissionArray = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
                    // 20160912 如果只有location 权限就不弹设置框
                    return;
                }

                // 请求权限弹框
                if (permissionArray != null) {
                    ArrayList<String> deniedPermissions = new ArrayList<String>();
                    for (int i = 0; i < permissionArray.length; i++) {
                        deniedPermissions.add(permissionArray[i]);
                    }
                    showPermissionsSettingDialog(activity, deniedPermissions);
                }

            }

        }

    }

    /**
     * 请求相机权限
     * @param activity
     * @return true:需要请求并且请求了 false:无需请求
     */
    public static boolean requestCameraPermission(Activity activity) {
        if (hasPermission(activity, Manifest.permission.CAMERA)) {
            return false;
        }
        requestPermission(activity, new String[] {Manifest.permission.CAMERA});
        return true;
    }

    public static boolean hasPermission(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission = ContextCompat.checkSelfPermission(activity, permission);
            return checkPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestPermission(Activity activity, String[] permissionArray) {
        // 请求权限
        if (permissionArray != null) {
            ActivityCompat.requestPermissions(activity, permissionArray, REQUEST_CODE_ASK_PERMISSION);
        }
    }

}
