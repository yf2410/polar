
package com.polar.browser.download_refactor.util;

import com.polar.browser.utils.SimpleLog;

public class LaunchPerformanceUtil {
    // Debug下开启Trace
    public final static boolean ENABLE_STARTUP_METHODTRACE = false;
    static public long mStarttimeApplicationCreated = 0;
    static public long mStartTimeBrowserActivityCreated = 0;
    static public long mStartTimeBrowserActivityonResumed = 0;
    static public boolean mFetchingData = false;

    private static final String TAG = "PERF";
    // 正常启动
    static public final int NORMAL = 1;
    // 外链调起
    static public final int THIRD_OPEN = NORMAL + 1;
    // Tab恢复
    static public final int RESTORE = NORMAL + 2;
    // 截图显示时间
    static public final int HOME_SCREEN = 1;
    // Home页面完全绘制完成时间
    static public final int POST_INIT = 2;
    // 本次启动类型
    static private int mLaunchType = 0;

    static public final int APPLICATION_CREATED = 1;
    static public final int BROWSER_ACTIVITY_CREATED = 2;

    static public void setApplicationCreated() {
        mStarttimeApplicationCreated = System.currentTimeMillis();
    }

    static public void setBrowserAcitivityCreated() {
        mStartTimeBrowserActivityCreated = System.currentTimeMillis();
    }

    public static void setActivityResumed() {
        mStartTimeBrowserActivityonResumed = System.currentTimeMillis();
    }

    public static void setFetchingData(boolean fetching) {
        mFetchingData = fetching;
    }

    static public void setLaunchType(int type) {
        mLaunchType = type;
    }

    static public void printResult(String functionName, int start_type) {
        String startType = null;
        long startTime = 0;
        if (start_type == APPLICATION_CREATED) {
            startTime = mStarttimeApplicationCreated;
            startType = "ApplicationCreated";
        } else {
            startTime = mStartTimeBrowserActivityCreated;
            startType = "BrowserActivityCreated";
        }
        long time = System.currentTimeMillis();
        SimpleLog.i(TAG, functionName + "," + (time - startTime) + " ms since " + startType);
    }

    static public void reportLaunchTime(int action) {
//        String clickName = null;
//        String timeValue = null;
//        switch (mLaunchType) {
//            case NORMAL:
//                if (action == HOME_SCREEN)
//                    clickName = UserLogConstants.NORMAL_LAUNCH_HOEMSCREEN;
//                else if (action == POST_INIT)
//                    clickName = UserLogConstants.NORMAL_LAUNCH_POSTINIT;
//                break;
//
//            case THIRD_OPEN:
//                break;
//
//            case RESTORE:
//                break;
//        }
//        if (clickName != null) {
//            long timeSolt = System.currentTimeMillis() - mStartTimeBrowserActivityCreated;
//            timeValue = Long.toString(timeSolt);
//            UserBehaviorLogManager.onClick(UserLogConstants.MODULE_LAUNCH, clickName, timeValue);
//            if(KLog.DEBUG){
//			    KLog.i(TAG, LaunchPerformanceUtil.class.getSimpleName()
//                    + ",reportLaunchTime,timeValue:" + timeValue + ",action:" + action);
//            }
//        }
//        if (action == POST_INIT)
//            endStartupTrace();
    }

    @SuppressWarnings("unused")
    public static void beginStartupTrace(String name) {
//        if (AppEnv.DEBUG && ENABLE_STARTUP_METHODTRACE)
//            Debug.startMethodTracing(name);
    }

    @SuppressWarnings("unused")
    public static void endStartupTrace() {
//        if (AppEnv.DEBUG && ENABLE_STARTUP_METHODTRACE)
//            Debug.stopMethodTracing();
    }
}
