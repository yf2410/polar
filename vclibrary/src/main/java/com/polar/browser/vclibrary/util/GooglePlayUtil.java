package com.polar.browser.vclibrary.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by FKQ on 2016/11/4.
 */

public class GooglePlayUtil {

    public static final String GOOGLE_PLAY_APP_STORE_URL_PREFIX = "https://play.google.com/store/apps/";

    public static final String GOOGLE_PLAY_APP_STORE_URL_AUTHORITY = "play.google.com";


    public static final String GOOGLE_PLAY_APP_DETAILS_URL_PREFIX = "https://play.google.com/store/apps/details?";

    public static final String GOOGLE_PLAY_APP_PKGNAME = "com.android.vending";


    /** *
     * @param appPkg   应用包名
     * @param marketPkg 应用商店包名
     * @param context
     */
    public static void launchAppDetail(Context context, String appPkg, String marketPkg) throws ActivityNotFoundException {
        if (TextUtils.isEmpty(appPkg)) {
            return;
        }
        if(!appPkg.startsWith("market://")){
            appPkg = buildMarketUri(appPkg);
        }
        Uri uri = Uri.parse(appPkg);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (!TextUtils.isEmpty(marketPkg)) {
            intent.setPackage(marketPkg);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static String buildMarketUri(String appPkg){
        return "market://details?id=" + appPkg;
    }

    /** *
     * 打开谷歌市场
     */
    public static void launchAppDetail(Context context, String url) throws ActivityNotFoundException {
        launchAppDetail(context,url,GOOGLE_PLAY_APP_PKGNAME);
    }



    /** *
     * @param url   APP应用市场链接
     * @param context
     */
    public static void goGooglePlayDetail(Context context, String url) throws ActivityNotFoundException {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(GOOGLE_PLAY_APP_PKGNAME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isGooglePlayUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            URI u = new URI(url);
            if (TextUtils.equals(GOOGLE_PLAY_APP_STORE_URL_AUTHORITY,u.getAuthority())) {
                return true;
            }
        } catch (URISyntaxException e) {
        }
        return false;
    }

    public static boolean isActivityExist(Intent intent,Context context){
        PackageManager packageManager = context.getPackageManager();
        return intent.resolveActivity(packageManager) != null;
    }
}
