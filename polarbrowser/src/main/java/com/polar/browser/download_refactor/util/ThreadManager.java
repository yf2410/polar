package com.polar.browser.download_refactor.util;

import android.os.Handler;
import android.os.HandlerThread;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;

//
// 线程管理器
//
public class ThreadManager {
    private static final String TAG = "ThreadManager";
   
    public static final int THREAD_UI = 0;
    public static final int THREAD_DB = 1;
    public static final int THREAD_DATA = 2;
    public static final int THREAD_DOWNLOAD_CTRL = 3;
    public static final int THREAD_SIZE = 4;

    // 线程信息数组
    private static final ArrayList<Handler> HANDLER_LIST = new ArrayList<Handler>(
            Collections.nCopies(THREAD_SIZE, (Handler)null)); 
    private static final String[] THREAD_NAME_LIST = {
        "thread_ui", "thread_db" ,"thread_data" ,"thread_download_ctrl"
    };
    
    public static void startup() {
        HANDLER_LIST.set(THREAD_UI, new Handler());
    }
    
    public static void shutdown() {
        
    }
    
    //
    // 派发任务
    //
    public static void post(int index, Runnable r) {
        postDelayed(index, r, 0);
    }

    private static class FetchDataRunnable implements Runnable {
        protected Runnable mSubRunnable = null;

        public FetchDataRunnable(Runnable subRunnable) {
            super();
            mSubRunnable = subRunnable;
        }

        @Override
        public void run() {
            LaunchPerformanceUtil.setFetchingData(true);
            try {
                mSubRunnable.run();
            }catch (Exception exception) {

            }
            LaunchPerformanceUtil.setFetchingData(false);
        }
    }

    public static void postDelayed(int index, Runnable r, long delayMillis) {
        if(index < 0 || index >= THREAD_SIZE) {
            throw new InvalidParameterException();
        }
        Handler handler = getHandler(index);
        if (THREAD_DATA == index)
            handler.postDelayed(new FetchDataRunnable(r), delayMillis);
        else
            handler.postDelayed(r, delayMillis);
    }

    //
    // 获取线程Handler
    //
    public static Handler getHandler(int index) {
        if(index < 0 || index >= THREAD_SIZE) {
            throw new InvalidParameterException();
        }

        if(HANDLER_LIST.get(index) == null) {
            synchronized (HANDLER_LIST) {
                if(HANDLER_LIST.get(index) == null) {
                    HandlerThread thread = new HandlerThread(THREAD_NAME_LIST[index]);
                    if (index != 0) // 我们不设置ui线程.
                        thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                    Handler handler = new Handler(thread.getLooper());
                    HANDLER_LIST.set(index, handler);
                }
            }
        }
        
        return HANDLER_LIST.get(index);
    }
    
    public static void assertThread(int thread){
        if(ThreadManager.getHandler(thread).getLooper().getThread().getId() != Thread.currentThread().getId())
            throw new RuntimeException("Dont run in thread!");
    }
    
}
