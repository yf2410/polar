/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.polar.browser.download_refactor;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;

import com.polar.browser.download_refactor.Downloads.Impl;
import com.polar.browser.download_refactor.dinterface.IContinuingStatusChange;
import com.polar.browser.download_refactor.dinterface.IProgressChange;
import com.polar.browser.download_refactor.dinterface.IStatusChange;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Stores information about an individual download.
 */
public class DownloadInfo {
    /**
     * Constants used to indicate network state for a specific download, after
     * applying any requested constraints.
     */
    public enum NetworkState {
        /**
         * The network is usable for the given download.
         */
        OK,

        /**
         * There is no network connectivity.
         */
        NO_CONNECTION,

        /**
         * The download exceeds the maximum size for this network.
         */
        UNUSABLE_DUE_TO_SIZE,

        /**
         * The download exceeds the recommended maximum size for this network,
         * the user must confirm for this download to proceed without WiFi.
         */
        RECOMMENDED_UNUSABLE_DUE_TO_SIZE,

        /**
         * The current connection is roaming, and the download can't proceed
         * over a roaming connection.
         */
        CANNOT_USE_ROAMING,

        /**
         * The app requesting the download specific that it can't use the
         * current network connection.
         */
        TYPE_DISALLOWED_BY_REQUESTOR,

        /**
         * Current network is blocked for requesting application.
         */
        BLOCKED
    }

    /**
     * For intents used to notify the user that a download exceeds a size threshold, if this extra
     * is true, WiFi is required for this download size; otherwise, it is only recommended.
     */
    public static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

    public long mId;
    public String mUri;
    public String mDestFile;        // 新建下载任务时预先确定的本地存储路径
    public String mDownlaodFile;    // 当前实际文件路径，若在下载过程中，此为临时文件路径，此文件路径有可能失效
    public String mMimeType;
    public int mVisibility;
    public int mVirusStatus;
    public int mControl;
    public int mStatus;
    public int mNumFailed;
    public int mRetryAfter;
    public long mLastMod;
    public String mExtras;
    public String mCookies;
    public String mUserAgent;
    public String mReferer;
    public long mTotalBytes;
    public long mCurrentBytes;
    public String mETag;
    public int mMediaScanned;
    public int mDeleted;
    public int mAllowedNetworkTypes;
    public boolean mAllowRoaming;
    public boolean mAllowMetered;
    public boolean mDownloadProtection;
    public String mDownloadDirectory;
    public int mContinuingState;
//    public String mTitle;
    public int mBypassRecommendedSizeLimit;

    public int mFuzz;
    
    public List<Pair<String, String>> mRequestHeaders = new ArrayList<Pair<String, String>>();

    /**
     * Result of last {@link DownloadTask} started by
     * {@link #startDownloadIfReady(java.util.concurrent.ExecutorService)}.
     */
    // @GuardedBy("this")
    private FutureTask<?> mSubmittedTask;

    // @GuardedBy("this")
    private DownloadTask mTask;

    private final Context mContext;
    private final SystemFacade mSystemFacade;
    private final StorageManager mStorageManager;
    ///> XXX 这块要修正状态的调用接口,但是暂时用原有DownloadInfo的流程后续再改吧......
    private final IStatusChange mStatusChange;
    private final IProgressChange mProgressChange;
    private final IContinuingStatusChange mContinuingStatusChange;

    public DownloadInfo(Context context, SystemFacade systemFacade, StorageManager storageManager,IStatusChange ob,IProgressChange ob2,IContinuingStatusChange ob3) {
        mContext = context;
        mSystemFacade = systemFacade;
        mStorageManager = storageManager;
        mFuzz = Helpers.sRandom.nextInt(1001);
        mStatusChange = ob;
        mProgressChange = ob2;
        mContinuingStatusChange = ob3;
    }
    
    public DownloadItemInfo toDownloadItemInfo(){
        DownloadItemInfo info = new DownloadItemInfo();
        info.mId = mId;
        info.mUrl = mUri;
        info.mReferer = mReferer;
        info.mMediaType = mMimeType;
        //info.mDate;
        info.mStatus = UiStatusDefine.translateStatus(mStatus);
        //info.mReason;
        info.mFilePath = mDestFile;
        info.mCurrentBytes = mCurrentBytes;
        info.mTotalBytes = mTotalBytes;
        //info.mVirusStatus;
        return info;
    }
    
    public void waitTask(long timeout){
        if(mSubmittedTask!=null)
            try {
                mSubmittedTask.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
    
    public void stop(){
        //还没进入下载流程的任务直接暂停
        if(!mTask.isRunning()) 
            changeStatusTo(Downloads.Impl.STATUS_PAUSED_BY_APP);
        else
            changeStatusTo(Downloads.Impl.STATUS_PAUSING);
        if(mSubmittedTask!=null){
            mSubmittedTask.cancel(false);
            DownloadExecutor.getInstance().removeThread(mSubmittedTask);
        }
        synchronized (this) {
            mControl = Downloads.Impl.CONTROL_PAUSED;
        }

    }

    public Collection<Pair<String, String>> getHeaders() {
        return Collections.unmodifiableList(mRequestHeaders);
    }

    /**
     * 为安全起见，下载网络设置仅App周期内有效，下次重新启动时此状态应恢复为默认值。
     */
    public void setAllowedNetworkTypes(int networkTypes) {
        mAllowedNetworkTypes = networkTypes != 0 ? networkTypes : Request.NETWORK_NO_MOBILE;
    }
    void setDownloadProtection(boolean protection){
        mDownloadProtection = protection;
    }
    void setDownloadDirectory(String dir) {
        mDownloadDirectory = dir;
    }
    /**
     * Returns the time when a download should be restarted.
     */
    public long restartTime(long now) {
        if (mNumFailed == 0) {
            return now;
        }
        if (mRetryAfter > 0) {
            return mLastMod + mRetryAfter;
        }
        return mLastMod +
                Constants.RETRY_FIRST_DELAY *
                    (1000 + mFuzz) * (1 << (mNumFailed - 1));
    }

    /**
     * Returns whether this download should be enqueued.
     */
    public boolean isReadyToDownload() {
        if (mControl == Downloads.Impl.CONTROL_PAUSED) {
            // the download is paused, so it's not going to start
            return false;
        }
        switch (mStatus) {
            case 0: // status hasn't been initialized yet, this is a new download
            case Downloads.Impl.STATUS_PENDING: // download is explicit marked as ready to start
            case Downloads.Impl.STATUS_QUEUEING:
            case Downloads.Impl.STATUS_PREPARING:
            case Downloads.Impl.STATUS_RUNNING: // download interrupted (process killed etc) while
                                                // running, without a chance to update the database
            case Downloads.Impl.STATUS_PAUSING:
                return true;

            case Downloads.Impl.STATUS_WAITING_FOR_NETWORK:
            case Downloads.Impl.STATUS_QUEUED_FOR_WIFI: {
                NetworkState state = checkCanUseNetwork();
                if (state == NetworkState.OK) {
                    return true;
                } else {
                    if (state == NetworkState.UNUSABLE_DUE_TO_SIZE
                            || state == NetworkState.RECOMMENDED_UNUSABLE_DUE_TO_SIZE) {
                        changeStatusTo(Impl.STATUS_QUEUED_FOR_WIFI);
                    } else {
                        changeStatusTo(Downloads.Impl.STATUS_WAITING_FOR_NETWORK);
                    }
                    return false;
                }
            }

            case Downloads.Impl.STATUS_WAITING_TO_RETRY:
                // download was waiting for a delayed restart
                final long now = mSystemFacade.currentTimeMillis();
                return restartTime(now) <= now;
            case Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR:
                // is the media mounted?
                return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            case Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR:
                // avoids repetition of retrying download
                return false;
        }
        return false;
    }

    public boolean isActiveDownload() {
        return mSubmittedTask != null && !mSubmittedTask.isDone();
    }

    /**
     * Returns whether this download has a visible notification after
     * completion.
     */
    public boolean hasCompletionNotification() {
        if (!Downloads.Impl.isStatusCompleted(mStatus)) {
            return false;
        }
        return mVisibility == Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    }

    /**
     * Returns whether this download is allowed to use the network.
     */
    public NetworkState checkCanUseNetwork() {
        final NetworkInfo info = mSystemFacade.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return NetworkState.NO_CONNECTION;
        }
        if (DetailedState.BLOCKED.equals(info.getDetailedState())) {
            return NetworkState.BLOCKED;
        }
        // 不检测漫游
//        if (mSystemFacade.isNetworkRoaming()) {
//            return NetworkState.CANNOT_USE_ROAMING;
//        }
        // 不检测网络计费
//        if (mSystemFacade.isActiveNetworkMetered() && !mAllowMetered) {
//            return NetworkState.TYPE_DISALLOWED_BY_REQUESTOR;
//        }
        return checkIsNetworkTypeAllowed(info.getType());
    }

    /**
     * Check if this download can proceed over the given network type.
     * @param networkType a constant from ConnectivityManager.TYPE_*.
     * @return one of the NETWORK_* constants
     */
    private NetworkState checkIsNetworkTypeAllowed(int networkType) {
        final int flag = translateNetworkTypeToApiFlag(networkType);
//        final boolean allowAllNetworkTypes = mAllowedNetworkTypes == DownloadManager.Request.NETWORK_MOBILE;
//        if (!allowAllNetworkTypes && (flag & mAllowedNetworkTypes) == 0) {
//            return NetworkState.TYPE_DISALLOWED_BY_REQUESTOR;
//        }
        if ((flag & mAllowedNetworkTypes) == 0) {
            if (flag == Request.NETWORK_MOBILE) {
                return NetworkState.UNUSABLE_DUE_TO_SIZE;
            } else /*if (flag != Request.NETWORK_WIFI)*/ {
                return NetworkState.TYPE_DISALLOWED_BY_REQUESTOR;
            }
        }
        return checkSizeAllowedForNetwork(networkType);
    }

    /**
     * Translate a ConnectivityManager.TYPE_* constant to the corresponding
     * DownloadManager.Request.NETWORK_* bit flag.
     */
    private int translateNetworkTypeToApiFlag(int networkType) {
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return Request.NETWORK_MOBILE;

            case ConnectivityManager.TYPE_WIFI:
                return Request.NETWORK_WIFI;

            case ConnectivityManager.TYPE_BLUETOOTH:
                return Request.NETWORK_BLUETOOTH;

            default:
                return 0;
        }
    }

    /**
     * Check if the download's size prohibits it from running over the current network.
     * @return one of the NETWORK_* constants
     */
    private NetworkState checkSizeAllowedForNetwork(int networkType) {
        if (mTotalBytes <= 0) {
            return NetworkState.OK; // we don't know the size yet
        }
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            return NetworkState.OK; // anything goes over wifi
        }
        Long maxBytesOverMobile = mSystemFacade.getMaxBytesOverMobile();
        if (maxBytesOverMobile != null && mTotalBytes > maxBytesOverMobile) {
            return NetworkState.UNUSABLE_DUE_TO_SIZE;
        }
        if (mBypassRecommendedSizeLimit == 0) {
            Long recommendedMaxBytesOverMobile = mSystemFacade.getRecommendedMaxBytesOverMobile();
            if (recommendedMaxBytesOverMobile != null
                    && mTotalBytes > recommendedMaxBytesOverMobile) {
                return NetworkState.RECOMMENDED_UNUSABLE_DUE_TO_SIZE;
            }
        }
        return NetworkState.OK;
    }

    // 在程序异常等特殊情况下，可能导致下载任务状态错误，导致下次启动时任务无法继续的问题。
    // 这里当一个任务新加载时，通过此方式修正这类问题
    // 因为这里是无条件修复错误，只能在新建任务时调用，否则将出现逻辑错误。
    public void repairStatus() {
        if (mStatus == Impl.STATUS_PAUSING) {
            changeStatusTo(Impl.STATUS_PAUSED_BY_APP);
        } else if (mStatus == Impl.STATUS_QUEUEING || mStatus == Impl.STATUS_PREPARING) {
            changeStatusTo(Impl.STATUS_PENDING);
        }
    }

    public void changeStatusTo(int status) {
        if (mStatus == status) {
            return;
        }
        mStatus = status;
        notifyDownloadStatus(status);
    }

    public void notifyDownloadStatus(int status) {
        mStatusChange.onDownloadStatusChange(mId, status);
    }
       
    /**
     * If download is ready to start, and isn't already pending or executing,
     * create a {@link DownloadTask} and enqueue it into given
     * {@link java.util.concurrent.Executor}.
     *
     * @return If actively downloading.
     */
    public boolean startDownloadIfReady(DownloadExecutor executor) {
        synchronized (this) {
            if (!isReadyToDownload())
                return false;
            if (isActiveDownload())
                return true;

            changeStatusTo(Impl.STATUS_QUEUEING);

            mTask = new DownloadTask(mContext, mSystemFacade,this, mStorageManager,mProgressChange,mContinuingStatusChange);
            mSubmittedTask = executor.submit(mTask);
            return true;
        }
    }

    public Uri getMyDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, mId);
    }

    public Uri getAllDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, mId);
    }

    public void dump(PrintWriter pw) {
        pw.println("DownloadInfo:");
        pw.println("mId=" + mId);
        pw.println("mLastMod=" + mLastMod);
        pw.println();

        pw.println("mUri=" + mUri);
        pw.println();

        pw.println("mMimeType=" + mMimeType);
        pw.println("mCookies=" + ((mCookies != null) ? "yes" : "no"));
        pw.println("mReferer=" + ((mReferer != null) ? "yes" : "no"));
        pw.println("mUserAgent=" +  mUserAgent);
        pw.println();

        pw.println("mDestFile=" +  mDestFile);
        pw.println("mDownloadFile=" +  mDownlaodFile);
        pw.println();

        pw.println("mStatus=" +  Downloads.Impl.statusToString(mStatus));
        pw.println("mCurrentBytes=" +  mCurrentBytes);
        pw.println("mTotalBytes=" +  mTotalBytes);
        pw.println();

        pw.println("mNumFailed=" +  mNumFailed);
        pw.println("mRetryAfter=" +  mRetryAfter);
        pw.println("mETag=" +  mETag);
        pw.println();

        pw.println("mAllowedNetworkTypes=" +  mAllowedNetworkTypes);
        pw.println("mAllowRoaming=" +  mAllowRoaming);
        pw.println("mAllowMetered=" +  mAllowMetered);
    }

    /**
     * Return time when this download will be ready for its next action, in
     * milliseconds after given time.
     *
     * @return If {@code 0}, download is ready to proceed immediately. If
     *         {@link Long#MAX_VALUE}, then download has no future actions.
     */
    public long nextActionMillis(long now) {
        if (Downloads.Impl.isStatusCompleted(mStatus)) {
            return Long.MAX_VALUE;
        }
        if (mStatus != Downloads.Impl.STATUS_WAITING_TO_RETRY) {
            return 0;
        }
        long when = restartTime(now);
        if (when <= now) {
            return 0;
        }
        return when - now;
    }

    /**
     * Returns whether a file should be scanned
     */
    public boolean shouldScanFile() {
        return (mMediaScanned == 0)
                && Downloads.Impl.isStatusSuccess(mStatus);
    }

    /**
     * Query and return status of requested download.
     */
    public static int queryDownloadStatus(ContentResolver resolver, long id) {
        final Cursor cursor = resolver.query(
                ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, id),
                new String[] { Downloads.Impl.COLUMN_STATUS }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                // TODO: increase strictness of value returned for unknown
                // downloads; this is safe default for now.
                return Downloads.Impl.STATUS_PENDING;
            }
        } finally {
            cursor.close();
        }
    }
}
