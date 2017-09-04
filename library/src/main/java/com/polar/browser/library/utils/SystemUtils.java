package com.polar.browser.library.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by FKQ on 2016/9/19.
 */

public class SystemUtils {

    // 首次计算出来的IMEI
    private static String sCacheIMEI;
    // 默认IMEI
    private static final String DEFAULT_IMEI = "LEMON_DEFAULT_IMEI";
    private static final String IMEI_SUFFIX = "VC_BROWSER";

    private static String sChangedMCC;
    /**
     * 获取唯一id
     *
     * @param context
     * @return
     */
    public static String getMid(Context context) {
        String imei = getImei(context);
        String AndroidID = android.provider.Settings.System.getString(
                context.getContentResolver(), "android_id");
        String serialNo = getDeviceSerial();
        String mid = SecurityUtil.getMD5("" + imei + AndroidID + serialNo
                + IMEI_SUFFIX);
        return mid;
    }

    /**
     * 获取android系统版本
     *
     * @return
     */
    public static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取当前国家或地区
     * @return area
     */
    public static String getArea() {
        return Locale.getDefault().getCountry();
    }

    /**
     * 获取当前设备语言
     * @return lan
     */
    public static String getLan() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取应用版本号
     *
     * @return
     */
    public static String getVersionName(Context context) {
        String version = "";
        PackageInfo info;
        try {
            info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }

    /**
     * 获取应用版本号
     *
     * @return
     */
    public static int getVersionCode(Context context) {
        int vercode = 0;
        PackageInfo info;
        try {
            info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            vercode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return vercode;
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    public static String getModel() {
        return android.os.Build.BRAND + " " + android.os.Build.MODEL;
    }

    public static void setMCC(String mcc) {
        sChangedMCC = mcc;
    }
    /**
     * 获取MCC
     *
     * 备注：在平板上注意该接口可能报错
     * @param context
     * @return
     */
    public static String getMCC(Context context) {
        String mcc = "";
        try {
            if (!TextUtils.isEmpty(sChangedMCC)) {
                return sChangedMCC;
            }
            TelephonyManager telephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = telephonyManager.getNetworkOperator();
            if (!TextUtils.isEmpty(networkOperator) && networkOperator.length() >= 3) {
                mcc = networkOperator.substring(0, 3);
            }
        } catch (Exception e) {
        }
        return mcc;
    }

    /**
     * 获取IMEI
     *
     * @param context
     * @return
     */
    private static synchronized String getImei(Context context) {
        if (sCacheIMEI != null) {
            return sCacheIMEI;
        }
        if (context != null) {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            try {
                if (tm != null) {
                    sCacheIMEI = tm.getDeviceId();
                    if (sCacheIMEI != null) {
                        return sCacheIMEI;
                    }
                }
            } catch (Exception e) {
            }
        }
        return DEFAULT_IMEI;
    }

    /**
     * 获取serialNo
     *
     * @return
     */
    public static String getDeviceSerial() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    /**
     * 获取IMEI
     * @param context
     * @return
     */
    public static String getImeiId(Context context) {
        String imeiId = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imeiId = tm.getDeviceId();
        } catch (Exception e) {
        }
        return imeiId;
    }

    public static String getSerial(Context context) {
        String serial = "";
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            serial = tm.getSimSerialNumber();
        } catch (Exception e) {
        }
        return serial;
    }

    public static String getAndroidId(Context context) {
        String androidId = "";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        return androidId;
    }

    public static String getDeviceMac(Context context) {
        String deviceMac = "";
        try {
            WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            deviceMac = wm.getConnectionInfo().getMacAddress();
        } catch (Exception e) {
        }
        return deviceMac;
    }

}
