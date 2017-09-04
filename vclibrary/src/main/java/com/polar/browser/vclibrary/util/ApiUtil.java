package com.polar.browser.vclibrary.util;

import android.content.Context;
import com.polar.browser.library.utils.SystemUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by James on 2016/9/18.
 */

public class ApiUtil {
    public static final String MOBILE_PLATFORM_ANDROID = "android";
    public static final String APP_NAME = "polar";

    /**
     * 获取应用版本
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        return SystemUtils.getVersionName(context);
    }

    public static String getVersionCode(Context context) {
        return String.valueOf(SystemUtils.getVersionCode(context));
    }

    public static String getLan() {
        return SystemUtils.getLan();
    }

    public static String getMobilePlatForm() {
        return MOBILE_PLATFORM_ANDROID;
    }

    public static String getAppName() {
        return APP_NAME;
    }

    /**
     * @return
     */
    public static String getOS() {
        return SystemUtils.getOSVersion();
    }

    public static String getMCC(Context context) {
        return SystemUtils.getMCC(context);
    }

    public static String getArea() {
        return SystemUtils.getArea();
    }

    public static String getCV() {
        return String.valueOf(new Random().nextInt(50000));
    }

    public static String getMID(Context context) {
        return SystemUtils.getMid(context);
    }

    public static String getMMOD() {
        return SystemUtils.getModel();
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public static String getTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        return  tz.getID();
    }
}
