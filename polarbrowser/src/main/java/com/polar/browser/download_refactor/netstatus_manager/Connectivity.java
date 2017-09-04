package com.polar.browser.download_refactor.netstatus_manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * <p>ThreadSafe.</p>
 * <p>网络连接相关的定义和帮助函数。</p>
 */
class Connectivity {
	
	// ConnectivityManager 中 @hide 的常量
	private static final int TYPE_WIFI_P2P = 13;
	private static final int TYPE_MOBILE_FOTA = 10;
	private static final int TYPE_MOBILE_IMS = 11;
	private static final int TYPE_MOBILE_CBS = 12;
	
    // 各种网络非主类型之外的类型 ---------------------------------------------------
    private static final int[] s_WifiSecondaryTypes = {
//        ConnectivityManager.TYPE_WIFI_P2P,
    	TYPE_WIFI_P2P,
    };
    private static final int[] s_MobileSecondaryTypes = {
        ConnectivityManager.TYPE_MOBILE_MMS,
        ConnectivityManager.TYPE_MOBILE_SUPL,
        ConnectivityManager.TYPE_MOBILE_DUN,
        ConnectivityManager.TYPE_MOBILE_HIPRI,
//        ConnectivityManager.TYPE_MOBILE_FOTA,
//        ConnectivityManager.TYPE_MOBILE_IMS,
//        ConnectivityManager.TYPE_MOBILE_CBS
        TYPE_MOBILE_FOTA,
        TYPE_MOBILE_IMS,
        TYPE_MOBILE_CBS
        
        };

    // TelephonyManager.NETWORK_TYPE_XXX 网速定义 -------------------------------
    private static final TelephonyTypeDefine[] s_TelephonyTypeDefines = {
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_UNKNOWN,
                false, "Unknown"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_GPRS,
                false, "0 ~ 100 kbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_EDGE,
                false, "0 ~ 50-100 kbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_UMTS,
                true, "0 ~ 400-7000 kbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_CDMA,
                false, "0 ~ 14-64 kbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_EVDO_0,
                true, "0 ~ 400-1000 kbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_EVDO_A,
                true, "0 ~ 600-1400 kbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_1xRTT,
                false, "0 ~ 50 - 100 kbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_HSDPA,
                true, "0 ~ 2-14 Mbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_HSUPA,
                true, "0 ~ 1-23 Mbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_HSPA,
                true, "0 ~ 700-1700 kbps"),
        // API level 8
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_IDEN,
                false, "0 ~ 25 kbps"),
        // API level 9
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_EVDO_B,
                true, "0 ~ 5 Mbps"),
        // API level 11
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_LTE,
                true, "0 ~ 10+ Mbps"),
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_EHRPD,
                true, "0 ~ 1-2 Mbps"),
        // API level 13
        new TelephonyTypeDefine(TelephonyManager.NETWORK_TYPE_HSPAP,
                true, "0 ~ 10-20 Mbps"),
    };

    /**
     * 获取网络状态信息
     * @param context
     * @param networkType
     * @return
     */
    public static NetworkInfo getNetworkInfos(Context context,
            int networkType) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        try {
            return cm.getNetworkInfo(networkType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取以太网连接状态信息
     * @param context
     * @return
     */
    public static NetworkInfo getEthernetNetworkInfo(Context context) {
        return getNetworkInfos(context, ConnectivityManager.TYPE_ETHERNET);
    }

    /**
     * 获取wifi连接状态信息
     * @param context
     * @return
     */
    public static NetworkInfo getWifiNetworkInfo(Context context) {
        return getNetworkInfoFromList(context, ConnectivityManager.TYPE_WIFI,
                s_WifiSecondaryTypes);
    }

    /**
     * 获取mobile连接状态信息
     * @param context
     * @return
     */
    public static NetworkInfo getMobileNetworkInfo(Context context) {
        return getNetworkInfoFromList(context, ConnectivityManager.TYPE_MOBILE,
                s_MobileSecondaryTypes);
    }

    /**
     * 检查是否有连接
     * @param info
     * @return
     */
    public static boolean isConnected(NetworkInfo info) {
        return null != info && info.isConnected();
    }

// TODO Remove unused code found by UCDetector
//     /**
//      * 检查是否是ethernet连接
//      * @param info
//      * @return
//      */
//     public static boolean isConnectedEthernet(NetworkInfo info) {
//         return null != info
//             && info.isConnected()
//             && ConnectivityManager.TYPE_ETHERNET == info.getType();
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 检查是否是wifi连接
//      * @param info
//      * @return
//      */
//     public static boolean isConnectedWifi(NetworkInfo info) {
//         return null != info
//             && info.isConnected()
//             && isNetworkTypeWifi(info.getType());
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 检查是否是mobile连接
//      * @param info
//      * @return
//      */
//     public static boolean isConnectedMobile(NetworkInfo info) {
//         return null != info
//             && info.isConnected()
//             && isNetworkTypeMobile(info.getType());
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * 检查是否高速网络
//      * @param info
//      * @return
//      */
//     public static boolean isConnectedFast(NetworkInfo info) {
//         return null != info
//             && info.isConnected()
//             && Connectivity.isConnectedFast(info.getType(), info.getSubtype());
//     }

    public static boolean isConnectedFast(int type, int subtype) {
        switch (type) {
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_WIFI:
            case TYPE_WIFI_P2P:
                return true;
        };

        if (ConnectivityManager.TYPE_MOBILE != type)
            return false;

        for (TelephonyTypeDefine typeDef : s_TelephonyTypeDefines) {
            if (typeDef.networkType != subtype)
                continue;
            return typeDef.fast;
        }
        return false;
    }

    /**
     * 获取网络理论速度描述
     * @param type
     * @param subtype
     * @return
     */
    public static String getNetworkSpeed(int type, int subtype) {
        switch (type) {
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_WIFI:
            case TYPE_WIFI_P2P:
                return "Fast";
        };

        if (ConnectivityManager.TYPE_MOBILE != type)
            return "Unknown";

        for (TelephonyTypeDefine typeDef : s_TelephonyTypeDefines) {
            if (typeDef.networkType != subtype)
                continue;
            return typeDef.speedDesc;
        }
        return "Unknown";
    }

    /**
     * Checks if a given type uses the cellular data connection.
     * This should be replaced in the future by a network property.
     * @param networkType the type to check
     * @return a boolean - {@code true} if uses cellular network,
     *  else {@code false}
     */
    private static boolean isNetworkTypeMobile(int networkType) {
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
            case ConnectivityManager.TYPE_MOBILE_MMS:
            case ConnectivityManager.TYPE_MOBILE_SUPL:
            case ConnectivityManager.TYPE_MOBILE_DUN:
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
            case TYPE_MOBILE_FOTA:
            case TYPE_MOBILE_IMS:
            case TYPE_MOBILE_CBS:
            // case ConnectivityManager.TYPE_MOBILE_IA:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks if the given network type is backed by a Wi-Fi radio.
     */
    private static boolean isNetworkTypeWifi(int networkType) {
        switch (networkType) {
            case ConnectivityManager.TYPE_WIFI:
            case TYPE_WIFI_P2P:
                return true;
            default:
                return false;
        }
    }

    private static NetworkInfo getNetworkInfoFromList(Context context,
            int mainType, int[] secondaryTypes) {
        NetworkInfo mainInfo = getNetworkInfos(context, mainType);
        if (isConnected(mainInfo))
            return mainInfo;

        for (int secondaryType : secondaryTypes) {
            NetworkInfo secondaryInfo = getNetworkInfos(context, secondaryType);
            if (isConnected(secondaryInfo))
                return secondaryInfo;
        }

        return mainInfo;
    }

    static class TelephonyTypeDefine {
        public final int        networkType;
        /** 是否高速网 */
        public final boolean    fast;
        /** 理论速度描述信息 */
        public final String     speedDesc;

        public TelephonyTypeDefine(int networkType, boolean fast,
                String speedDesc) {
            super();
            this.networkType = networkType;
            this.fast = fast;
            this.speedDesc = speedDesc;
        }
    }
}
