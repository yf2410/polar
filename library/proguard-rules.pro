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

#-------------------------------------------------------------------------

#---------------------------------2.第三方包-------------------------------

# EventBus start
#EventBus 3 混淆问题
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
# EventBus end

# glide start
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
#  **[] $VALUES;
#  public *;
#}
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
# glide end

# ormlite start
# ormlite-proguard相关的配置-OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

#仅忽略混淆使用了DatabaseField注解的类成员
-keepclassmembers class * {
@com.j256.ormlite.field.DatabaseField *;
}
# ormlite end

# OkHttpGlideModule start
-keep class com.bumptech.glide.integration.okhttp.OkHttpGlideModule
# OkHttpGlideModule end

# Retrofit 2.X start
## https://square.github.io/retrofit/ ##

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

# Retrofit 2.X end

-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

#-------------------------------------------------------------------------

#---------------------------------3.与js互相调用的类------------------------


#-------------------------------------------------------------------------

#---------------------------------4.反射相关的类和方法-----------------------

#----------------------------------------------------------------------------
#---------------------------------------------------------------------------------------------------

