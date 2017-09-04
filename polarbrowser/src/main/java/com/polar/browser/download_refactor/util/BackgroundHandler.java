/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.polar.browser.download_refactor.util;

import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundHandler {

    static private HandlerThread sLooperThread;
    static private ExecutorService mThreadPool;

    static {
        sLooperThread = new HandlerThread("BackgroundHandler", HandlerThread.MIN_PRIORITY);
        sLooperThread.start();
        mThreadPool = Executors.newCachedThreadPool();
    }

    /**
     * 在没有任务个数限制的Executor中运行runnable
     * 
     * @param runnable
     */
    public static void executeOnFullTaskExecutor(Runnable runnable) {
        mThreadPool.execute(runnable);
    }

    /**
     * 得到没有任务个数限制的Executor
     * 
     * @return
     */
    public static ExecutorService getFullTaskExecutor() {
        return mThreadPool;
    }

    public static Looper getLooper() {
        return sLooperThread.getLooper();
    }

    private BackgroundHandler() {
    }
}
