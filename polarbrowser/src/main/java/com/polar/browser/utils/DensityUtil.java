package com.polar.browser.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * px dp 转换util
 */
public final class DensityUtil {
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int getPxByAttributeValue(Context context,String value){
		if(value == null) return 0;
		String result = "0";
		try{
			if(value.endsWith("dp")){
				result = value.substring(0,value.length()-2);
				return dip2px(context,(int)Float.parseFloat(result));
			}else if(value.endsWith("dip")){
				result = value.substring(0,value.length()-3);
				return dip2px(context,(int)Float.parseFloat(result));
			}else if(value.endsWith("px")){
				result = value.substring(0,value.length()-2);
				return (int)Float.parseFloat(result);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return  0;
	}

	public static final float getHeightInPx(Context context) {
		final float height = context.getResources().getDisplayMetrics().heightPixels;
		return height;
	}

	public static final float getWidthInPx(Context context) {
		final float width = context.getResources().getDisplayMetrics().widthPixels;
		return width;
	}

	public static final int getHeightInDp(Context context) {
		final float height = context.getResources().getDisplayMetrics().heightPixels;
		int heightInDp = px2dip(context, height);
		return heightInDp;
	}

	public static final int getWidthInDp(Context context) {
		final float height = context.getResources().getDisplayMetrics().heightPixels;
		int widthInDp = px2dip(context, height);
		return widthInDp;
	}

	/**
	 * 获取顶部 status bar 高度
	 *
	 * @param context
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
		return resources.getDimensionPixelSize(resourceId);
	}
	/*
	private static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        	SimpleLog.e(e);
        }
        return hasNavigationBar;
//        return true;
    }
	
	/**
	 *  获取底部 navigation bar 高度
	 * @param context
	 * @return
	 */
	/*
	public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        if (AppEnv.sIsFullScreen) {
			return 0;
		}
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }*/
}
