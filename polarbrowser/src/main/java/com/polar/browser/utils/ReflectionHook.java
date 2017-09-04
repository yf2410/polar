package com.polar.browser.utils;

import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public final class ReflectionHook {

    public static final String TAG = "ReflectionHook";

    /**
     * @note 4.1.1 ~ 4.1.2 中因为部分机器开启了此特性，导致在WebViewClassic.onPageFInished
     * @note 会进入AccessibilityInjector.onPageFinished逻辑，从而导致后续URLEncodedUtils.parse
     * @note 因为url参数异常而崩溃，故对此版本做此patch
     * @param context
     */

    public static void hookAccessibilityManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN +2) {
            try {
                Class clazz = AccessibilityManager.class;
                Method med=clazz.getMethod("getInstance", Context.class);
                AccessibilityManager am = (AccessibilityManager) med.invoke(null, context);
                setClassField(am, "mIsEnabled", false);
                SimpleLog.d(TAG, "Accessibility enabled:" + am.isEnabled());
            } catch (Throwable e) {
            }
        }
    }

    public static void setClassField(Object object, String fieldName, Object fieldNewValue) {
        Class<? extends Object> type = object.getClass();
        Field field = null;
        try {
            field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, fieldNewValue);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getClassField(Object object, String fieldName, boolean superClass) {
        Class<?> type = superClass ? object.getClass().getSuperclass() : object.getClass();
        Field field = null;
        Object value = null;
        try {
            field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            value = field.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }
}
