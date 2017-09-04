// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.polar.browser.download_refactor.util;

import android.app.Notification;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

//import com.ijinshan.browser.mojo.CustomizeWebViewCore;
//import com.ijinshan.browser.utils.AndroidBuild;
//import com.ijinshan.browser.utils.KLog;

/**
 * Utility class to use new APIs that were added after ICS (API level 14).
 */
public class ApiCompatibilityUtils {

    private static final String LOGTAG = "ApiCompatibilityUtils";

    private ApiCompatibilityUtils() {
    }

// TODO Remove unused code found by UCDetector
//     /**
//      * Returns true if view's layout direction is right-to-left.
//      *
//      * @param view the View whose layout is being considered
//      */
//     public static boolean isLayoutRtl(View view) {
//         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//             return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
//         } else {
//             // All layouts are LTR before JB MR1.
//             return false;
//         }
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * @see android.view.View#setLayoutDirection(int)
//      */
//     public static void setLayoutDirection(View view, int layoutDirection) {
//         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//             view.setLayoutDirection(layoutDirection);
//         } else {
//             // Do nothing. RTL layouts aren't supported before JB MR1.
//         }
//     }

    

// TODO Remove unused code found by UCDetector
//     /**
//      * @see android.view.ViewGroup.MarginLayoutParams#getMarginEnd()
//      */
//     public static int getMarginEnd(MarginLayoutParams layoutParams) {
//         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//             return layoutParams.getMarginEnd();
//         } else {
//             return layoutParams.rightMargin;
//         }
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * @see android.view.ViewGroup.MarginLayoutParams#setMarginStart(int)
//      */
//     public static void setMarginStart(MarginLayoutParams layoutParams, int start) {
// //        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
// //            layoutParams.setMarginStart(start);
// //        } else {
// //            layoutParams.leftMargin = start;
// //        } raulli
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * @see android.view.ViewGroup.MarginLayoutParams#getMarginStart()
//      */
//     public static int getMarginStart(MarginLayoutParams layoutParams) {
//         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//             return layoutParams.getMarginStart();
//         } else {
//             return layoutParams.leftMargin;
//         }
//     }

// TODO Remove unused code found by UCDetector
//     /**
//      * @see android.view.View#postInvalidateOnAnimation()
//      */
//     public static void postInvalidateOnAnimation(View view) {
//         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//             view.postInvalidateOnAnimation();
//         } else {
//             view.postInvalidate();
//         }
//     }

    // These methods have a new name, and the old name is deprecated.

    /**
     * @see View#setBackground(Drawable)
     */
    @SuppressWarnings("deprecation")
    public static void setBackgroundForView(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    /**
     * @see ViewTreeObserver#removeOnGlobalLayoutListener()
     */
    @SuppressWarnings("deprecation")
    public static void removeOnGlobalLayoutListener(
            View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        }
    }

    /*
    public static void setPageCacheCapacity(WebSettings settings, Context context) {
        if (Build.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.ICE_CREAM_SANDWICH
                && Build.VERSION.SDK_INT < AndroidBuild.VERSION_CODES.KITKAT) {
            int size = CustomizeWebViewCore.pageCacheSize(context);
            if (-1 == size)
                return;
            
            Method m = getMethodNoException(settings.getClass(), "setPageCacheCapacity",
                    Integer.TYPE);
            if (m != null) {
                invokeVoidMethod(settings, m, Integer.valueOf(size));
            }
        }
    }

    public static void setProperty(WebSettings settings, String property, String value) {
        if (Build.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && Build.VERSION.SDK_INT < AndroidBuild.VERSION_CODES.KITKAT) {
            Method m = getMethodNoException(settings.getClass(), "setProperty",
                    String.class, String.class);
            if (m != null) {
                invokeVoidMethod(settings, m, property, value);
            }
        }
    }

    private static Method getMethodNoException(Class<?> clazz, String name,
            Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static void invokeVoidMethod(Object object, Method method, Object... params) {
        try {
            method.invoke(object, params);
        } catch (IllegalArgumentException e) {
            if(KLog.DEBUG){
                KLog.e(LOGTAG, "invokeVoidMethod: IllegalArgumentException: " + e.getMessage());
            }
        } catch (IllegalAccessException e) {
            if(KLog.DEBUG){
                KLog.e(LOGTAG, "invokeVoidMethod: IllegalAccessException: " + e.getMessage());
            }
        } catch (InvocationTargetException e) {
            if(KLog.DEBUG){
                KLog.e(LOGTAG, "invokeVoidMethod: InvocationTargetException: " + e.getMessage());
            }
        }
    }*/

    @SuppressWarnings("deprecation")
    public static Notification getNotification(Notification.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Notification result = null;
            try {
                result = builder.build();
            } catch (NoSuchMethodError e) {
                result = builder.getNotification();
            }
            return result;
        }
        else
            return builder.getNotification();
    }
    
    
    /*
    @SuppressWarnings("deprecation")
    public static void setPluginsEnabled(WebSettings webSetting, boolean paramBool) {
        if (Build.VERSION.SDK_INT < AndroidBuild.VERSION_CODES.JELLY_BEAN_MR2)
            webSetting.setPluginsEnabled(paramBool);
    }
    
    @SuppressWarnings("deprecation")
    public static void viewTreeObserverRemoveGlobalOnLayoutListener(ViewTreeObserver viewTreeObserver, 
            ViewTreeObserver.OnGlobalLayoutListener victim) {
        if (Build.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.JELLY_BEAN)
            viewTreeObserver.removeOnGlobalLayoutListener(victim);
        else
            viewTreeObserver.removeGlobalOnLayoutListener(victim);
    }

    public static void viewPostInvalidateOnAnimation(View view) {
        if (Build.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.JELLY_BEAN)
            view.postInvalidateOnAnimation();
        else
            view.invalidate();
    }

    public static boolean viewGetImportantForAccessibilityAuto(View view) {
        if (Build.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.JELLY_BEAN)
            return view.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        else
            return false;
    }

    public static void viewPostOnAnimation(View view, Runnable runnable) {
        if (Build.VERSION.SDK_INT >= AndroidBuild.VERSION_CODES.JELLY_BEAN)
            view.postOnAnimation(runnable);
        else
            view.postDelayed(runnable, 10);
    }
    */
}
