package com.polar.browser.download_refactor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * Created by misty on 15/2/4.
 */
public class DownloadExecutor implements Executor {

    private ArrayList<FutureTask<?>> mQueuedFutures = new ArrayList<FutureTask<?>>();
    private int mMaxThreadCount = 0;    // 0表示不限制
    private ArrayList<Thread> mThreads = new ArrayList<Thread>();
    private boolean mShutdown = false;
    private static DownloadExecutor sExecutor;
    
    public static DownloadExecutor getInstance(){
        if(sExecutor == null)
            sExecutor = new DownloadExecutor(Constants.MAX_DOWNLOAD_TASK_COUNT);
        return sExecutor;
    }
    public DownloadExecutor(int maxThreadCount) {
        setmMaxThreadCount(maxThreadCount);
    }

    public void setmMaxThreadCount(int maxThreadCount) {
        mMaxThreadCount = Math.max(maxThreadCount, 0);
    }

    public void execute(Runnable command) {
        submit(command);
    }

    public void shutdown() {
        synchronized (this) {
            mShutdown = true;
        }
    }

    public List<Runnable> shutdownNow() {
        synchronized (this) {
            mShutdown = true;
            List<Runnable> list = new ArrayList<Runnable>(mQueuedFutures.size());
            for (FutureTask<?> futureTask : mQueuedFutures) {
                list.add(futureTask);
            }
            mQueuedFutures.clear();
            return list;
        }
    }

    public FutureTask<?> submit(Runnable task) {
        FutureTask<?> future = null;
        synchronized (this) {
            if (mShutdown)
                return null;
            InnerTask innerTask = new InnerTask(task);
            future = new FutureTask<Integer>(innerTask, 0);
            mQueuedFutures.add(future);
        }
        checkStart();
        return future;
    }

    private class InnerTask implements Runnable {

        public Runnable mTask = null;

        InnerTask(Runnable task) {
            mTask = task;
        }

        public void run() {
            try {
                if (mTask != null) {
                    mTask.run();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                exitThread(Thread.currentThread());
            }
        }
    }

    private void checkStart() {
        while (true) {
            synchronized (this) {
                FutureTask<?> future = null;
                if ((mMaxThreadCount > 0 && mThreads.size() >= mMaxThreadCount))
                    break;

                if (mQueuedFutures.size() > 0) {
                    future = mQueuedFutures.get(0);
                    mQueuedFutures.remove(0);
                }

                if (future == null)
                    break;

                Thread thread = new Thread(future);
                mThreads.add(thread);
                thread.start();
            }
        }
    }
    
    public void removeThread(FutureTask<?> future) {
        synchronized (this) {
            if(mQueuedFutures.contains(future))
            mQueuedFutures.remove(future);
        }
    }
    
    private void exitThread(Thread thread) {
        synchronized (this) {
            mThreads.remove(thread);
        }
        checkStart();
    }
}
