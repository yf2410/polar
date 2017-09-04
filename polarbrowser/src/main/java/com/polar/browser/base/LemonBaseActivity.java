package com.polar.browser.base;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.polar.browser.common.data.CommonData;
import com.polar.browser.env.AppEnv;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;

/**
 * 基于BaseActivity，实现横竖屏锁定等功能，夜间模式功能
 * 目前绝大部分Activity需要继承这个类，如果未来出现某些界面不需要横竖屏锁定，亮度调节的需求，就不能继承这个类了
 *
 * @author dpk
 */
public class LemonBaseActivity extends BaseActivity {

    private static String TAG = "LemonBaseActivity";

    @Override
    protected void onResume() {
        super.onResume();
        if (ConfigManager.getInstance().isEnableNightMode()) {
            setBrightness(CommonData.NIGHT_MODE_BRIGHTNESS);
        } else {
            setBrightness(-1);
        }
        // 横竖屏锁定判断
        boolean configScreenLock = ConfigManager.getInstance().isScreenLock();
        if (configScreenLock) {
            // 设置竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            // 设置跟随系统
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        // 是否全屏判断
        SysUtils.setFullScreen(this, ConfigManager.getInstance().isFullScreen());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (ConfigWrapper.get(ConfigDefine.CROPEDITACTIVITY_IS_STATUS_BAR, true)) {
//            // 初始化沉浸时地址栏
//            //initImmersion();
//        } else {
//            ConfigWrapper.put(ConfigDefine.CROPEDITACTIVITY_IS_STATUS_BAR, true);
//            ConfigWrapper.apply();
//        }
        // MiUI6需要更改状态栏字体颜色
//        SysUtils.setStatusBarTextColor(this, 1);
    }

    /**
     * 初始化沉浸式
     */
    private void initImmersion() {
        if (android.os.Build.VERSION.SDK_INT > 18) {
            // TODO 沉浸式地址栏
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//	        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetScreenSize();
    }

    private void resetScreenSize() {
        // 初始化屏幕尺寸
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        AppEnv.SCREEN_WIDTH = dm.widthPixels;
        AppEnv.SCREEN_HEIGHT = dm.heightPixels;
        SimpleLog.d(TAG, "SCREEN_WIDTH = " + AppEnv.SCREEN_WIDTH);
        SimpleLog.d(TAG, "SCREEN_HEIGHT = " + AppEnv.SCREEN_HEIGHT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideIM();
    }

    protected void setBrightness(float f) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = f;
        getWindow().setAttributes(lp);
    }

    protected void hideIM() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
