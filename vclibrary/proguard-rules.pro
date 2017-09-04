# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Programs\Android\SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-------------------------------------------定制化区域----------------------------------------------
#---------------------------------1.实体类---------------------------------
-keep class com.polar.browser.vclibrary.bean.** { *; }
-keep class com.polar.browser.vclibrary.bean.base.** { *; }
-keep class com.polar.browser.vclibrary.bean.db.** { *; }
-keep class com.polar.browser.vclibrary.bean.events.** { *; }
-keep class com.polar.browser.vclibrary.bean.login.** { *; }

-keep class com.polar.browser.vclibrary.network.api.ApiInterface{*;}


#-------------------------------------------------------------------------

#---------------------------------2.第三方包-------------------------------

#adjust-sdk-start
-keep public class com.adjust.sdk.** { *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep class dalvik.system.VMRuntime {
    java.lang.String getRuntime();
}
-keep class android.os.Build {
    java.lang.String[] SUPPORTED_ABIS;
    java.lang.String CPU_ABI;
}
-keep class android.content.res.Configuration {
    android.os.LocaledList getLocales();
    java.util.Locale locale;
}
-keep class android.os.LocaledList {
    java.util.Locale get(int);
}

#adjust-sdk-end

#-------------------------------------------------------------------------

#---------------------------------3.与js互相调用的类------------------------



#-------------------------------------------------------------------------

#---------------------------------4.反射相关的类和方法-----------------------



#----------------------------------------------------------------------------
#---------------------------------------------------------------------------------------------------

