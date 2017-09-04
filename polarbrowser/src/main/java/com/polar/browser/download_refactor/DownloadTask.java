package com.polar.browser.download_refactor;

import android.app.DownloadManager.Request;
import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.polar.browser.JuziApp;
import com.polar.browser.download_refactor.db.DownloadProvider;
import com.polar.browser.download_refactor.dinterface.IContinuingStatusChange;
import com.polar.browser.download_refactor.dinterface.IProgressChange;
import com.polar.browser.download_refactor.util.FileUtil;
import com.polar.browser.download_refactor.util.PathResolver;
import com.polar.browser.download_refactor.util.SmartDecode;
import com.polar.browser.download_refactor.util.URLUtilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static android.text.format.DateUtils.SECOND_IN_MILLIS;
import static com.polar.browser.download_refactor.Constants.TAG;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_BAD_REQUEST;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_CANCELED;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_CANNOT_RESUME;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_FILE_ERROR;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_HTTP_DATA_ERROR;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_PREPARING;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_QUEUED_FOR_WIFI;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_RUNNING;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_SUCCESS;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_TOO_MANY_REDIRECTS;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_UNKNOWN_ERROR;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_WAITING_FOR_NETWORK;
import static com.polar.browser.download_refactor.Downloads.Impl.STATUS_WAITING_TO_RETRY;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

/**
 * Created by misty on 14/12/8.
 */
public class DownloadTask implements Runnable {

    // TODO: bind each download to a specific network interface to avoid state
    // checking races once we have ConnectivityManager API

    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    private static final int HTTP_TEMP_REDIRECT = 307;

    private static final int DEFAULT_TIMEOUT = (int) (20 * SECOND_IN_MILLIS);

    //private final ExecutorService mExecutor;
    private final Context mContext;
    private final DownloadInfo mInfo;
    private final SystemFacade mSystemFacade;
    private final StorageManager mStorageManager;
    private final IProgressChange mProgressChange;
    private final IContinuingStatusChange mContinuingStatusChange;
    private final DownloadProvider mDownloadProvider;
//    private final DownloadNotifier mNotifier;

    private TaskState mState;

    private DownloadingFile mMapFile;

    private int mActiveThreadCount = 0;
    private int mTransferThreadCount = 0;
    private ArrayList<DownloadThread> mThreads = new ArrayList<DownloadThread>();
    private ArrayList<Future<?>> mFutures = new ArrayList<Future<?>>();

    private Handler mHandler;

    private static final int TASK_MSG_INIT_TASK     = 1;   // 添加子下载（下载分片，如果可以的话）
    private static final int TASK_MSG_ADD_THREAD    = 2;   // 添加子下载（下载分片，如果可以的话）
    private static final int TASK_MSG_THREAD_EXIT   = 3;   // 子线程退出

    /**
     * Returns the user agent provided by the initiating app, or use the default one
     */
    private String userAgent() {
        String userAgent = mInfo.mUserAgent;
        if (userAgent == null) {
            userAgent = Constants.DEFAULT_USER_AGENT;
        }
        return userAgent;
    }

    public DownloadTask(Context context, SystemFacade systemFacade, DownloadInfo info,
                        StorageManager storageManager,IProgressChange ob,IContinuingStatusChange ob2) {
        mContext = context;
        mSystemFacade = systemFacade;
        mInfo = info;
        mStorageManager = storageManager;
        mProgressChange = ob;
        mContinuingStatusChange = ob2;
        mDownloadProvider = DownloadProvider.getInstance();
    }

    /**
     * State for the entire run() method.
     */
    static class TaskState {
        public String mFilePath;    // 当前文件路径，一般为未完成的临时文件
        public String mDestFile;    // 目标文件路径
        public String mMimeType;
        public int mRetryAfter = 0;
        public String mRequestUri;
        public long mTotalBytes = -1;
        public long mCurrentBytes = 0;
        public String mHeaderETag;
        public long mBytesNotified = 0;
        public long mTimeLastNotification = 0;
        public long mBytesSynced = 0;
        public long mTimeLastSync = 0;
//        public int mNetworkType = ConnectivityManager.TYPE_NONE;
        public int mNetworkType = 0;
        public int mMaxThreadCount = Constants.MAX_THREAD_COUNT;

        /** Historical bytes/second speed of this download. */
        public long mSpeed;
        /** Time when current sample started. */
        public long mSpeedSampleStart;
        /** Bytes transferred since current sample started. */
        public long mSpeedSampleBytes;

        public String mContentDisposition;
        public String mContentLocation;

        public String mMD5Hash = null;
        // 支持续传的标记
        static public final int CONTINUING_STATE_UNKNOW = 0;
        static public final int CONTINUING_STATE_DETECTING = 1;
        static public final int CONTINUING_STATE_POSITIVE = 2;
        static public final int CONTINUING_STATE_SPLITED = 3;
        static public final int CONTINUING_STATE_NEGATIVE = -1;

        public int mContiningState = CONTINUING_STATE_UNKNOW;

        public int mRedirectionCount;
        public URL mUrl;

        public TaskState(DownloadInfo info) {
            // 不能调用Intent.normalizeMemeType，Adnroid 4.0没有此方法
            mMimeType = FileUtil.normalizeMimeType(info.mMimeType);
            mRequestUri = info.mUri;
            mDestFile = info.mDestFile;
            mFilePath = info.mDownlaodFile;
            mTotalBytes = info.mTotalBytes;
            mCurrentBytes = info.mCurrentBytes;
           // mContiningState = info.mContinuingState;
        }
    }

    public boolean isRunning(){
        return mHandler!=null;
    }
    
    @Override
    public void run() {
        // Skip when download already marked as finished; this download was
        // probably started again while racing with UpdateThread.
        /* FIXME
        if (DownloadInfo.queryDownloadStatus(mContext.getContentResolver(), mInfo.mId)
                == STATUS_SUCCESS) {
            Log.d(TAG, "Download " + mInfo.mId + " already finished; skipping");
            return;
        }
        */
        if (Base64ImageDownloader.isBase64Image(mInfo.mUri)) {
            notifyDownloadCompleted(STATUS_SUCCESS, "", 0);
            return;
        }
        // 保存目标文件路径不能无效
        if (TextUtils.isEmpty(mInfo.mDestFile)) {
            Log.d(TAG, "Download " + mInfo.mId + " Invalid filename");
            notifyDownloadCompleted(STATUS_FILE_ERROR, "", 0);
            return;
        }

        // 这里不能使用ExecutorService等通常的线程池，每个线程的looper一旦quit之后就不能再被复用。
        //if (Looper.myLooper() == null) {
            Looper.prepare();
        //}

        mHandler = new Handler(Looper.myLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case TASK_MSG_INIT_TASK:
                            initTask();
                            break;
                        case TASK_MSG_ADD_THREAD:
                            addSubthreadIfReady(msg.arg1);
                            break;
                        case TASK_MSG_THREAD_EXIT:
                            onSubthreadExit((DownloadThread)msg.obj);
                            break;
                    }
                } catch (Throwable e) {
                    try {
                        finishDownload(STATUS_UNKNOWN_ERROR);
                    } catch (Throwable ignored) {

                    } finally {
                        mHandler.getLooper().quit();
                    }
                }
                return false;
            }
        });

        mHandler.obtainMessage(TASK_MSG_INIT_TASK).sendToTarget();
        Looper.loop();
        mHandler = null;
    }

    private void initTask() {

        mInfo.changeStatusTo(STATUS_PREPARING);

        mState = new TaskState(mInfo);
        String errorMsg = null;

        // Network traffic on this thread should be counted against the
        // requesting UID, and is tagged with well-known value.
        // TODO Fix me 
//        TrafficStats.setThreadStatsTag(TrafficStats.TAG_SYSTEM_DOWNLOAD);
//        TrafficStats.setThreadStatsUid(mContext.getApplicationInfo().uid);
        try {
            // TODO: migrate URL sanity checking into client side of API
            mState.mUrl = new URL(mState.mRequestUri);
        } catch (MalformedURLException e) {
            errorMsg = "Aborting request for download " + mInfo.mId + ": " + "Bad request url: " + mState.mRequestUri;
            Log.d(TAG, errorMsg);
            mInfo.changeStatusTo(STATUS_BAD_REQUEST);
            return;
        }

        String parsedFilename = null;
        if (mState.mContentDisposition != null) {
            parsedFilename = URLUtilities.parseContentDisposition(mState.mContentDisposition);
        }
        // Pass TRUE if it've already got the correct filename.
        prepareDownloadFile((parsedFilename != null) || !mState.mDestFile.contains(URLUtilities.DEFAULT_FILENAME));

        assert (mMapFile.isOpen());

        // skip when already finished; remove after fixing race in 5217390
        if (mState.mCurrentBytes == mState.mTotalBytes) {
            Log.i(Constants.TAG, "Skipping initiating request for download " +
                    mInfo.mId + "; already completed");
            finishDownload(STATUS_SUCCESS);
            return;
        } else if (mState.mTotalBytes > 0 && mState.mCurrentBytes > mState.mTotalBytes) {   // 文件大小异常
            Log.i(Constants.TAG, "Initiating request for download " +
                    mInfo.mId + "failed, conflicting file size: " + mState.mCurrentBytes + "/" + mState.mTotalBytes);
            finishDownload(STATUS_CANNOT_RESUME);
            return;
        } else if (mState.mCurrentBytes > 0 && mState.mContiningState == TaskState.CONTINUING_STATE_NEGATIVE) {
            Log.i(Constants.TAG, "Initiating request for download " +
                    mInfo.mId + "failed, continuing download is not supported: " + mState.mCurrentBytes + "/" + mState.mTotalBytes);
            finishDownload(STATUS_CANNOT_RESUME);
            return;
        }

        addSubthreadIfReady(0);
    }

    private void prepareDownloadFile(boolean actual) {
        try {
            File destFile = new File(mState.mDestFile);
            File dir = destFile.getParentFile();
            if (dir != null) {
                // 检查路径是否可写，若不可写，则用setting里的下载路径替换后再试
                if (!DownloadFileUtils.isDirPathCanWrite(dir.getPath(), null, false)) {
                    if (mInfo.mDownloadDirectory == null || mInfo.mDownloadDirectory.equals("") || mInfo.mDownloadDirectory.equals(dir.getPath())) {
                        finishDownload(STATUS_FILE_ERROR);
                        return;
                    } else {
                        dir = new File(mInfo.mDownloadDirectory);
                        if (!DownloadFileUtils.isDirPathCanWrite(dir.getPath(), null, false)) {
                            finishDownload(STATUS_FILE_ERROR);
                            return;
                        } else {
                            mState.mDestFile = mInfo.mDownloadDirectory + "/" + destFile.getName();
                            destFile = new File(mState.mDestFile);
                            mState.mFilePath = null;
                        }
                    }
                }
            }

            // 对于重新开始的任务，则需要先删除原来的文件
            // TODO: 对于已删除任务的遗留文件（只删除任务），这里最好可以校验遗留文件并续传
            if (mInfo.mCurrentBytes == 0 && mInfo.mTotalBytes == -1) {
                destFile.delete();
                if (!TextUtils.isEmpty(mInfo.mDownlaodFile)) {
                    new File(mInfo.mDownlaodFile).delete();
                    mInfo.mDownlaodFile = null;
                }
            }
            // 为了防止文件名冲突，这里先创建一个临时文件来占用目标文件名称，
            // 等下载完成后，再删除此文件，将下载完成的文件重命名为此名称。
            // Only create temporary file only if here came out the correct filename. 
            if (!destFile.exists() && actual) {
                destFile.createNewFile();
            }
            if (mMapFile == null && !openDestinationFile()) {
                createDestinationFile();
            }
            
            updateDownloadResult(STATUS_RUNNING, "", 0); // 更新文件存储路径
            assert (mMapFile != null && mMapFile.isOpen());
        } catch (StopRequestException e) {
            finishDownload(e.getFinalStatus());
            return;
        } catch (Throwable e) {
            finishDownload(STATUS_FILE_ERROR);
            return;
        }
    }

    private void updateFileInfo() {
        synchronized (mMapFile) {
            assert (mMapFile != null && mMapFile.isOpen());
            mMapFile.setUrl(mState.mRequestUri);
            mMapFile.setTag(mState.mHeaderETag);
            mMapFile.setFileSize(mState.mTotalBytes);
        }
    }

    private void updateFilename() {
        if (!TextUtils.isEmpty(mState.mContentDisposition)) {
            String contentDisposition =
                    SmartDecode.recoverString(mState.mContentDisposition,
                            SmartDecode.isGB2312Url(mState.mUrl.toString()), false);
            String filename = URLUtilities.guessFileName(mState.mUrl.toString(), contentDisposition,
                    mState.mMimeType, true);
            if (!TextUtils.isEmpty(filename)) {
                filename = PathResolver.getUniqueName(filename, PathResolver.getDownloadFileDir(""));
                mState.mDestFile = PathResolver.getDownloadFileDir("")+filename;
                mState.mFilePath = mState.mDestFile + ".vcdownload";
                mMapFile.rename(mState.mFilePath);
                prepareDownloadFile(true);
                mInfo.mDestFile = mState.mDestFile;
                DownloadItemInfo info = DownloadManager.getInstance().getDownloadItem(mInfo.mId);
                if (info != null) {
                    info.mFilePath = mState.mDestFile;
                }
            }
        }
    }

    private void finishDownload(int finalStatus) {
        mHandler.removeMessages(TASK_MSG_ADD_THREAD);

        if (mInfo.mDeleted == 1) {
            new File(mInfo.mDestFile).delete();
            new File(mInfo.mDownlaodFile).delete();
            mHandler.getLooper().quit();
            return;
        }

        String errorMsg = null;

        if (mMapFile != null && mMapFile.isOpen()) {
            try {
                if (finalStatus == STATUS_SUCCESS) {
                    mMapFile.makeCompleted();
                }
            } catch (ParamException e) {
                finalStatus = STATUS_UNKNOWN_ERROR;
                errorMsg = "Close file exception: " + e;
            } catch (IOException e) {
                finalStatus = STATUS_FILE_ERROR;
                errorMsg = "Close file exception: " + e;
            } finally {
                try {
                    mMapFile.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (finalStatus == STATUS_SUCCESS) {
            finalizeDestinationFile();
        }
        notifyDownloadCompleted(finalStatus, errorMsg, mInfo.mNumFailed);
//        mInfo.changeStatusTo(finalStatus);
        Log.d(TAG, "Download thread exiting.");
        
        mStorageManager.incrementNumDownloadsSoFar();

        mHandler.getLooper().quit();
    }

    private void onSubthreadExit(DownloadThread thread) {
        int idx = mThreads.indexOf(thread);
        assert (idx != -1);
        mThreads.remove(idx);
        try { mFutures.get(idx).get();
        } catch (Exception ignored) {}
        mFutures.remove(idx);

        if (thread.mThreadStatus == STATUS_WAITING_TO_RETRY) {
            int delay = thread.mRetryAfter;
            if (delay < Constants.MIN_RETRY_AFTER * 1000) delay = Constants.MIN_RETRY_AFTER * 1000;
            else if (delay > Constants.MAX_RETRY_AFTER * 1000) delay = Constants.MAX_RETRY_AFTER * 1000;
            if (mActiveThreadCount == 0) { // 唯一线程等待时间缩短
                delay = 2 * 1000;
            }
            mHandler.sendMessageDelayed(mHandler.obtainMessage(TASK_MSG_ADD_THREAD, thread.mRetryCount, 0), delay);
            return;
        }
        if (mActiveThreadCount > 0)
            return;

        finishDownload(thread.mThreadStatus);
    }

    /**
     * 添加子线程，请保证只由调度线程调用
     * @param retryCount 所起的线程重试次数的基数。如果某个线程以 STATUS_WAITING_TO_RETRY 状态退出，
     *                   则由其发起的 retry 操作线程需要以之前的 retryCount 为基数继续执行，直到超过下载重试限制为止。
     */
    private void addSubthreadIfReady(int retryCount) {
        assert (mMapFile != null && mMapFile.isOpen());

        synchronized (mState) {
            if (mState.mContiningState == TaskState.CONTINUING_STATE_UNKNOW) {
                DownloadingFile.Assignment assignment = mMapFile.getAvailableAssignment(true);
                assert  (assignment != null);
                DownloadThread thread = new DownloadThread(assignment);
                thread.mRetryCount = retryCount;
                addSubThreadInternal(thread);
            } else if (mState.mContiningState == TaskState.CONTINUING_STATE_DETECTING) {
                // 创建第二个线程，根据请求返回的数据确定支持续传时，才调splitAssignment切分文件，并转换为CONTINUING_STATE_POSITIVE状态
                assert (mMapFile.getAssignmentCount() == 1);
                if (mState.mMaxThreadCount > 1 &&
                        mState.mTotalBytes > Constants.DEFAULT_THREAD_THRESHOLD) {
                    // TODO: 如果第一个线程下载速度很快，或者第二个线程经过重试之后再进行分片，有可能出现第一个分片的下载进度已经覆盖了新的分片的起始位置。这不会导致逻辑上的错误，但会浪费流量，增加下载时间
                    long total = mMapFile.getTotalBytes();// - mMapFile.getCurrentBytes();
                    long partial = (total + mState.mMaxThreadCount - 1) / mState.mMaxThreadCount;
                    long start = partial;
                    long end = Math.min(start + partial, total);
                    DownloadThread thread = new DownloadThread(start, end);
                    thread.mRetryCount = retryCount;
                    addSubThreadInternal(thread);
                }
            } else if (mState.mContiningState == TaskState.CONTINUING_STATE_POSITIVE) {
                // 已确定支持多线程下载，拆分后续部分文件。此时应该已经切分出两段并已开始下载了，这里获取的应为第3段
                assert (mMapFile.getAssignmentCount() >= 2);
                DownloadingFile.Assignment assignment = mMapFile.getAvailableAssignment(false);
                if (assignment != null) {
                    // 取到第3个分片，并将其按指定大小拆分
                    long total = mMapFile.getTotalBytes();// - mMapFile.getCurrentBytes();
                    long partial = (total + mState.mMaxThreadCount - 1) / mState.mMaxThreadCount;
                    while (true) {
                        // 当当前下载位置已经超过要切分位置时，返回false，否则返回true，或抛出其他异常
                        if (assignment.getEndBytes() - assignment.getCurrentBytes() > partial) {
                            mMapFile.splitAssignment(assignment, partial);
                        }
                        mMapFile.setAssignmentInUse(assignment);
                        DownloadThread thread = new DownloadThread(assignment);
                        thread.mRetryCount = retryCount;
                        addSubThreadInternal(thread);
                        assignment = mMapFile.getAvailableAssignment(true);
                        if (assignment == null)
                            break;
                    }
                }
                setContinuingState(TaskState.CONTINUING_STATE_SPLITED);
            } else if (mState.mContiningState == TaskState.CONTINUING_STATE_SPLITED) {
                while (true) {
                    DownloadingFile.Assignment assignment = mMapFile.getAvailableAssignment(true);
                    if (assignment == null)
                        break;
                    DownloadThread thread = new DownloadThread(assignment);
                    thread.mRetryCount = retryCount;
                    addSubThreadInternal(thread);
                }
            } else { //if (mState.mContiningState == TaskState.CONTINUING_STATE_NEGATIVE) {
                return;
            }
        }
    }

    private void addSubThreadInternal(DownloadThread thread) {
        mThreads.add(thread);
        FutureTask<?> future = new FutureTask<Integer>(thread, 0);
        new Thread(future).start();
        mFutures.add(future);
    }

    class DownloadThread implements Runnable {

        public boolean mGotData = false;
        public int mRetryAfter = 0;
        public int mRetryCount = 0;
        public boolean mContinuingDownload = false;
//        public int mNetworkType = ConnectivityManager.TYPE_NONE;
        public int mNetworkType = 0;

        public int mThreadStatus = Downloads.Impl.STATUS_UNKNOWN_ERROR;

        private long mDetectStartBytes = 0;    // 用于续传detecting，仅当 mState.mContiningState != TaskState.CONTINUING_STATE_DETECTING 时有效
        private long mDetectEndBytes = -1;     // -1表示到文件末尾，该值指向数据的最后一个字节的下一个字节，注意于Content-Range的from-to区别开
        DownloadingFile.Assignment mAssignment = null;

        /**
         * 每个下载启动的第一个线程为主下载线程，负责尝试全文下载，并根据Header返回结果确定创建辅助线程，直到辅助线程成功取到数据，则调整主下载线程的下载range
         * @param assignment 指定要下载的分片，若是主下载线程，则assignment为null，主下载线程负责打开文件并确定分片信息
         */
        public DownloadThread(DownloadingFile.Assignment assignment) {
            assert (mState.mContiningState != TaskState.CONTINUING_STATE_DETECTING);

            assert (assignment != null);
            mAssignment = assignment;

            if (mAssignment.getCurrentBytes() > 0 || mAssignment.getEndBytes() < (mMapFile.getTotalBytes())) {
                mContinuingDownload = true;
                assert (mState.mContiningState == TaskState.CONTINUING_STATE_POSITIVE ||
                        mState.mContiningState == TaskState.CONTINUING_STATE_SPLITED);
            } else {
                mContinuingDownload = false;
                assert (mState.mContiningState != TaskState.CONTINUING_STATE_POSITIVE &&
                        mState.mContiningState != TaskState.CONTINUING_STATE_SPLITED);
            }
        }

        public DownloadThread(long startBytes, long endBytes) {
            assert (mState.mContiningState == TaskState.CONTINUING_STATE_DETECTING);
            mDetectStartBytes = startBytes;
            mDetectEndBytes = endBytes;
            mContinuingDownload = true;
        }

        public void run() {
            synchronized (this) {
                mActiveThreadCount++;
            }
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            mThreadStatus = Downloads.Impl.STATUS_UNKNOWN_ERROR;
            String errorMsg = null;

            try {
                Log.i(Constants.TAG, "Download " + mInfo.mId + " starting");

                // Remember which network this download started on; used to
                // determine if errors were due to network changes.
                final NetworkInfo info = mSystemFacade.getActiveNetworkInfo();
                if (info != null) {
                    mNetworkType = info.getType();
                }

                executeDownload();

                mThreadStatus = Downloads.Impl.STATUS_SUCCESS;
            } catch (StopRequestException error) {
                // remove the cause before printing, in case it contains PII
                errorMsg = error.getMessage();
                if (Constants.LOGV) {
                    Log.d(Constants.TAG, "Aborting request for download " + mInfo.mId + ": " + errorMsg);
                }
                mThreadStatus = error.getFinalStatus();

                // Nobody below our level should request retries, since we handle
                // failure counts at this level.
                assert (mThreadStatus != STATUS_WAITING_TO_RETRY);

                // Some errors should be retryable, unless we fail too many times.
                if (isStatusRetryable(mThreadStatus)) {
                    if (mGotData) {
                        mRetryCount = 1;
                    } else {
                        mRetryCount += 1;
                    }

                    if (mRetryCount < Constants.MAX_RETRIES) {
                        final NetworkInfo info = mSystemFacade.getActiveNetworkInfo();
                        if (info != null && info.isConnected()) {
                            if (info.getType() == mNetworkType) {
                                // Underlying network is still intact, use normal backoff
                                mThreadStatus = STATUS_WAITING_TO_RETRY;
                            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                                if ((mInfo.mAllowedNetworkTypes & Request.NETWORK_MOBILE) == 0) {
                                    mThreadStatus = STATUS_QUEUED_FOR_WIFI;
                                } else {
                                    // WiFi -> Mobile
                                    mThreadStatus = STATUS_WAITING_TO_RETRY;
                                }
                            } else {
                                // Mobile -> WiFi
                                mThreadStatus = STATUS_WAITING_TO_RETRY;
                            }
                        } else {
                            // Network changed, retry on any next available
                            mThreadStatus = STATUS_WAITING_FOR_NETWORK;
                        }
                    }
                }
                // fall through to finally block
            } catch (CancellationException e) {
                errorMsg = "Download thread canceled: " + e;
                Log.d(TAG, errorMsg);
                mThreadStatus = STATUS_CANCELED;
            } catch (Throwable ex) {
                errorMsg = ex.getMessage();
                ex.printStackTrace();
                String msg = "Exception for id " + mInfo.mId + ": " + errorMsg;
                Log.w(Constants.TAG, msg, ex);
                mThreadStatus = Downloads.Impl.STATUS_UNKNOWN_ERROR;
                // falls through to the code that reports an error
            } finally {
                if (mThreadStatus == STATUS_SUCCESS) {
                    TrafficStats.incrementOperationCount(1);
                }
                //else if (mThreadStatus == Downloads.Impl.STATUS_WAITING_TO_RETRY) {
                // 由于服务器实现的非标准化，对于下载加速线程（第二个及以上的线程），
                // 只要服务器明确返回了错误码，且有活动线程正在传输数据，就认为该服务器不支持多线程下载
                else if (mThreadStatus != Downloads.Impl.STATUS_HTTP_DATA_ERROR) {
                    synchronized (mState) {
                        if (mTransferThreadCount > 0
                                && mState.mContiningState == TaskState.CONTINUING_STATE_DETECTING
                                && mContinuingDownload) {
                            // 对于不支持下载加速的任务，这个线程的任务就是判断是否支持下载加速。因此返回成功，并重置下载加速状态为不支持
                            mThreadStatus = Downloads.Impl.STATUS_SUCCESS;
                            setContinuingState(TaskState.CONTINUING_STATE_NEGATIVE);
                        }
                    }
                }
             // TODO Fix me 
//                TrafficStats.clearThreadStatsTag();
//                TrafficStats.clearThreadStatsUid();

                if (mAssignment != null) {
                    Log.i(Constants.TAG, "Download " + mInfo.mId + ": bytes" + mAssignment.getStartBytes() + "-" + (mAssignment.getEndBytes() - 1) + " finished with status "
                            + Downloads.Impl.statusToString(mThreadStatus));

                    mMapFile.releaseAssignment(mAssignment);
                    mAssignment = null;
                } else {
                    Log.i(Constants.TAG, "Download " + mInfo.mId + ": null assignment" + " finished with status "
                            + Downloads.Impl.statusToString(mThreadStatus));
                }

                synchronized (DownloadTask.this) {
                    mActiveThreadCount--;
                }
                mHandler.obtainMessage(TASK_MSG_THREAD_EXIT, this).sendToTarget();
            }
        }

        /**
         * Fully execute a single download request. Setup and send the request,
         * handle the response, and transfer the data to the destination file.
         */
        private void executeDownload() throws StopRequestException {
            mState.mRedirectionCount = 0;
            while (mState.mRedirectionCount++ < Constants.MAX_REDIRECTS) {
                // Open connection and follow any redirects until we have a useful
                // response with body.
                HttpURLConnection conn = null;
                try {
                    checkConnectivity();
                    conn = (HttpURLConnection) mState.mUrl.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setConnectTimeout(DEFAULT_TIMEOUT);
                    conn.setReadTimeout(DEFAULT_TIMEOUT);

                    addRequestHeaders(conn);

                    final int responseCode = conn.getResponseCode();
                    switch (responseCode) {
                        case HTTP_OK:
                            if (mContinuingDownload) {
                                throw new StopRequestException(
                                        STATUS_CANNOT_RESUME, "Expected partial, but received OK");
                            }
                            processResponseHeaders(conn);
                            transferData(conn);
                            return;

                        case HTTP_PARTIAL:
                            if (!mContinuingDownload) {
                                throw new StopRequestException(
                                        STATUS_CANNOT_RESUME, "Expected OK, but received partial");
                            }
                            processResponseHeaders(conn);
                            transferData(conn);
                            return;

                        case HTTP_MOVED_PERM:
                        case HTTP_MOVED_TEMP:
                        case HTTP_SEE_OTHER:
                        case HTTP_TEMP_REDIRECT:
                            final String location = conn.getHeaderField("Location");
                            mState.mUrl = new URL(mState.mUrl, location);
                            if (responseCode == HTTP_MOVED_PERM) {
                                // Push updated URL back to database
                                mState.mRequestUri = mState.mUrl.toString();
                            }
                            continue;

                        case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
                            throw new StopRequestException(
                                    STATUS_CANNOT_RESUME, "Requested range not satisfiable");

                        case HTTP_UNAVAILABLE:
                            parseRetryAfterHeaders(conn);
                            throw new StopRequestException(
                                    HTTP_UNAVAILABLE, conn.getResponseMessage());

                        case HTTP_INTERNAL_ERROR:
                            throw new StopRequestException(
                                    HTTP_INTERNAL_ERROR, conn.getResponseMessage());

                        default:
                            StopRequestException.throwUnhandledHttpError(
                                    responseCode, conn.getResponseMessage());
                    }
                } catch (IOException e) {
                    // Trouble with low-level sockets
                    throw new StopRequestException(STATUS_HTTP_DATA_ERROR, e);

                } finally {
                    if (conn != null) {
                        try { conn.disconnect(); }
                        catch (Exception ignored) {}
                    }
                }
            }

            throw new StopRequestException(STATUS_TOO_MANY_REDIRECTS, "Too many redirects");
        }

        /**
         * Transfer data from the given connection to the destination file.
         */
        private void transferData(HttpURLConnection conn) throws StopRequestException {

            assert (mAssignment != null);

            mInfo.changeStatusTo(STATUS_RUNNING);
            mTransferThreadCount++;

            if (!mMapFile.isAllocatioinCompleted()) {
                mMapFile.allocateDiskSpace(new FileAllocateResultHandler() {
                    public void onAllocateResult(int result, String msg) {
                        if (result == 0 && mState.mMaxThreadCount > 1) {
                            setContinuingState(TaskState.CONTINUING_STATE_DETECTING);
                            mHandler.obtainMessage(TASK_MSG_ADD_THREAD).sendToTarget();
                        } else {
                            // 分配磁盘空间错误，不做任何处理，也不再尝试多线程分片下载。
                            // 如果本次任务被暂停，下次继续下载（Resume）时这里还会再次尝试分配空间，如果成功，还可以再切片进行多线程下载。
                        }
                    }
                });
            }

            InputStream in = null;
            try {
                try {
                    in = conn.getInputStream();
                } catch (IOException e) {
                    throw new StopRequestException(STATUS_HTTP_DATA_ERROR, e);
                }

                // Start streaming data, periodically watch for pause/cancel
                // commands and checking disk space as needed.
                int flushBytes = 0;
                final byte data[] = new byte[Constants.BUFFER_SIZE];
                for (; ; ) {
                    checkPausedOrCanceled();

                    int bytesRead = readFromResponse(in, data);
                    if (bytesRead == -1) { // success, end of stream already reached
                        handleEndOfStream();
                        break;
                    }

                    mGotData = true;
                    int bytesWrite = writeToFile(data, bytesRead);
                    
                    // 100KB主动更新一次文件尾元数据
                    flushBytes += bytesWrite;
                    if (flushBytes >= 100 * 1024) {
                        mMapFile.flushMetaData();
                        flushBytes = 0;
                    }
                    reportDownloadData(bytesWrite);

                    if (Constants.LOGVV) {
                        Log.v(Constants.TAG, "download " + mAssignment.getCurrentBytes() + " for "
                                + mInfo.mUri + ", and " + bytesWrite + " bytes has been written to file." );
                    }
                    if (mAssignment.isCompleted()) {
                        handleEndOfStream();
                        break;
                    }
                }
            } catch (StopRequestException e) {
                Log.v(Constants.TAG, "download " + mAssignment.getCurrentBytes() + " for "
                        + mInfo.mUri + ", Stopped: " + e.getMessage());
                throw e;
            } catch (IOException e) {
                Log.v(Constants.TAG, "download " + mAssignment.getCurrentBytes() + " for "
                        + mInfo.mUri + ", IO error: " + e.toString());
                throw new StopRequestException(STATUS_FILE_ERROR, e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ignored) {
                }

                try {
                    mMapFile.flushMetaData();
                } catch (IOException ignored) {
                }
                mTransferThreadCount--;
            }
        }

        /**
         * Called when we've reached the end of the HTTP response stream, to update the database and
         * check for consistency.
         */
        private void handleEndOfStream() throws StopRequestException {
            if (mAssignment.getEndBytes() > 0 && !mAssignment.isCompleted()) {
                throw new StopRequestException(STATUS_HTTP_DATA_ERROR,
                        "closed socket before end of file");
            }
        }

        /**
         * Read some data from the HTTP response stream, handling I/O errors.
         * @param data buffer to use to read data
         * @param entityStream stream for reading the HTTP response entity
         * @return the number of bytes actually read or -1 if the end of the stream has been reached
         */
        private int readFromResponse(InputStream entityStream, byte[] data)
                throws StopRequestException {
            try {
                return entityStream.read(data);
            } catch (IOException ex) {
                // TODO: handle stream errors the same as other retries
                if ("unexpected end of stream".equals(ex.getMessage())) {
                    return -1;
                }
                throw new StopRequestException(STATUS_HTTP_DATA_ERROR,
                            "Failed reading response: " + ex, ex);
            }
        }

        /**
         * 写入文件
         * @param data
         * @param bytesToWrite
         * @return 返回写入的字节数
         */
        private int writeToFile(byte[] data, int bytesToWrite) throws StopRequestException{
            if (mAssignment.getEndBytes() > 0 
                    && mAssignment.getEndBytes() - mAssignment.getCurrentBytes() < bytesToWrite) {
                bytesToWrite = (int)(mAssignment.getEndBytes() - mAssignment.getCurrentBytes());
            }
            try {
                mMapFile.write(mAssignment, data, bytesToWrite);
            }
            catch (IOException e) {
                Log.v(Constants.TAG, "download " + mAssignment.getCurrentBytes() + " for "
                        + mInfo.mUri + ", write file error:" + bytesToWrite + " bytes." );
                throw new StopRequestException(STATUS_FILE_ERROR, e);
            }
            return bytesToWrite;
        }

        /**
         * Prepare target file based on given network response. Derives filename and
         * target size as needed.
         */
        private void processResponseHeaders(HttpURLConnection conn)
                throws StopRequestException {
            boolean available = true;
            String parsedFilename = null;
            if (mState.mContentDisposition != null) {
                parsedFilename = URLUtilities.parseContentDisposition(mState.mContentDisposition);
            }

            if (mContinuingDownload) {
                assert (mMapFile != null);
                checkResponseHeaders(conn);
            } else {
                assert (mMapFile == null);
                readResponseHeaders(conn);

                // TODO Fix me 取一个前台的context
//                Context context = KApplication.getInstance().getForegroundActivity();
                Context context = JuziApp.getInstance();
                available = DownloadManagerCheck.checkStorageSpace(context, PathResolver.getDownloadFileDir(""), mInfo.mTotalBytes);
                // Check storage again now that we know the total size.
                // Skip preallocation for unavailable file and don't worry about continue download
                // because it won't happen in STATUS_FILE_ERROR.
                if (available) {
                    updateFileInfo();
                }
                // Still need to update information for this downloading.
                updateDatabaseFromHeaders();
            }

            boolean needRename = (parsedFilename == null) && mState.mDestFile.contains(URLUtilities.DEFAULT_FILENAME);
            // Only work when there is no correct filename extracted from initialized ContentDisposition.
            // And update it with new ContentDisposition after header checker in redirect response.
            if (!mContinuingDownload) {
                if (needRename) {
                    updateFilename();
                }
                if (!available) {
                    throw new StopRequestException(STATUS_FILE_ERROR, "Not enough storage space");
                }
            }

            // check connectivity again now that we know the total size
            checkConnectivity();
        }

        /**
         * Read headers from the HTTP response and store them into local state.
         */
        private void readResponseHeaders(HttpURLConnection conn)
                throws StopRequestException {
            assert (mAssignment == null);

            mState.mContentDisposition = conn.getHeaderField("Content-Disposition");
            mState.mContentLocation = conn.getHeaderField("Content-Location");

            // 从头开始下载时，不需要保证与新建任务时传入的MIMEType一致，只需要本次下载过程中个线程返回的MIMEType一直即可
            //if (mState.mMimeType == null) {
                mState.mMimeType = FileUtil.normalizeMimeType(conn.getContentType());
            //}

            mState.mHeaderETag = conn.getHeaderField("ETag");

            final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
            if (transferEncoding == null) {
                mState.mTotalBytes = getHeaderFieldLong(conn, "Content-Length", -1);
            } else {
                Log.i(TAG, "Ignoring Content-Length since Transfer-Encoding is also defined");
                mState.mTotalBytes = -1;
            }

            mState.mCurrentBytes = 0;
            mInfo.mTotalBytes = mState.mTotalBytes;
            if (mInfo.mTotalBytes < 0) {
                setContinuingState(TaskState.CONTINUING_STATE_NEGATIVE);
            }
        }

        private void checkResponseHeaders(HttpURLConnection conn)
                throws StopRequestException {
            assert ((mAssignment != null && mState.mContiningState != TaskState.CONTINUING_STATE_DETECTING) ||
                    (mAssignment == null && mState.mContiningState == TaskState.CONTINUING_STATE_DETECTING));

            final String mimeType = FileUtil.normalizeMimeType(conn.getContentType());
            if (!mState.mMimeType.equals(mimeType))
                throw new StopRequestException(STATUS_CANNOT_RESUME, "MIME Type mismatched");

            final String headerETag = conn.getHeaderField("ETag");
            if (TextUtils.isEmpty(headerETag) && TextUtils.isEmpty(mState.mHeaderETag)
                    || mState.mHeaderETag.equals(headerETag)) {
                // ETag check ok
            } else {
                throw new StopRequestException(STATUS_CANNOT_RESUME, "ETag header mismatched");
            }

            long startBytes = 0, endBytes = 0, contentLength = -1;
            while (true) {
                final String contentRange = conn.getHeaderField("Content-Range");
                if (contentRange == null) break;
                int index0 = contentRange.indexOf("bytes ");
                if (index0 < 0) break;
                index0 += "bytes ".length();
                int index1 = contentRange.indexOf('-', index0);
                if (index1 <= 0) break;
                int index2 = contentRange.indexOf('/', index1 + 1);
                if (index2 <= 0) break;
                try { startBytes = Long.parseLong(contentRange.substring(index0, index1));
                } catch (NumberFormatException ignore) {}
                try { endBytes = Long.parseLong(contentRange.substring(index1 + 1, index2));
                } catch (NumberFormatException ignore) {}
                try { contentLength = Long.parseLong(contentRange.substring(index2 + 1));
                } catch (NumberFormatException ignore) {}
                break;
            }

            if (contentLength != mState.mTotalBytes
                    || startBytes != (mAssignment == null ? mDetectStartBytes : mAssignment.getCurrentBytes())
                    || endBytes + 1 < (mAssignment == null ? mDetectEndBytes : mAssignment.getEndBytes()))
                throw new StopRequestException(STATUS_CANNOT_RESUME, "Content range mismatched");

            final String transferEncoding = conn.getHeaderField("Transfer-Encoding");
            if (transferEncoding != null && transferEncoding.equalsIgnoreCase("chunked")) {
                throw new StopRequestException(STATUS_CANNOT_RESUME,
                        "can't know size of download, giving up");
            }

            if (mState.mContiningState == TaskState.CONTINUING_STATE_DETECTING) {
                DownloadingFile.Assignment assignment = mMapFile.getAssignmentAtIndex(0);
                mMapFile.splitAssignment(assignment, mDetectStartBytes - assignment.getStartBytes());
                assignment = mMapFile.getAvailableAssignment(true);
                mMapFile.splitAssignment(assignment, mDetectEndBytes - mDetectStartBytes);
                mAssignment = assignment;
                setContinuingState(TaskState.CONTINUING_STATE_POSITIVE);
                mHandler.obtainMessage(TASK_MSG_ADD_THREAD).sendToTarget();
            }
        }

        private void parseRetryAfterHeaders(HttpURLConnection conn) {
            mRetryAfter = conn.getHeaderFieldInt("Retry-After", -1);
            if (mRetryAfter < 0) {
                mRetryAfter = 0;
            } else {
                if (mRetryAfter < Constants.MIN_RETRY_AFTER) {
                    mRetryAfter = Constants.MIN_RETRY_AFTER;
                } else if (mRetryAfter > Constants.MAX_RETRY_AFTER) {
                    mRetryAfter = Constants.MAX_RETRY_AFTER;
                }
                mRetryAfter += Helpers.sRandom.nextInt(Constants.MIN_RETRY_AFTER + 1);
                mRetryAfter *= 1000;
            }
        }

        /**
         * Add custom headers for this download to the HTTP request.
         */
        private void addRequestHeaders(HttpURLConnection conn) {
            for (Pair<String, String> header : mInfo.getHeaders()) {
                conn.addRequestProperty(header.first, header.second);
            }

            // Only splice in user agent when not already defined
            if (conn.getRequestProperty("User-Agent") == null) {
                conn.addRequestProperty("User-Agent", userAgent());
            }

            // Defeat transparent gzip compression, since it doesn't allow us to
            // easily resume partial downloads.
            conn.setRequestProperty("Accept-Encoding", "identity");

            if (mContinuingDownload) {
                if (mState.mHeaderETag != null) {
                    conn.addRequestProperty("If-Match", mState.mHeaderETag);
                }
                if (mAssignment == null) {
                    conn.addRequestProperty("Range", "bytes=" + mDetectStartBytes + "-" + (mDetectEndBytes - 1));
                } else {
                    conn.addRequestProperty("Range", "bytes=" + mAssignment.getCurrentBytes() + "-" + (mAssignment.getEndBytes() - 1));
                }
            }
        }
    }

    /**
     * Update necessary database fields based on values of HTTP response headers that have been
     * read.
     */
    private void updateDatabaseFromHeaders() {
        synchronized (mState) {
            handerContinuingState(mState.mContiningState);
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_DOWNLOAD_FILE_PATH, mState.mFilePath);
            if (mState.mHeaderETag != null) {
                values.put(Constants.ETAG, mState.mHeaderETag);
            }
            if (mState.mMimeType != null) {
                values.put(Downloads.Impl.COLUMN_MIME_TYPE, mState.mMimeType);
            }            
            values.put(Downloads.Impl.COLUMN_TOTAL_BYTES, mInfo.mTotalBytes);
            values.put(Downloads.Impl.COLUMN_CONTINUING_STATE, mState.mContiningState);
            mDownloadProvider.updateDownload(mInfo.mId, values,null);
        }
    }
    /**
     * 更新断点续传的检查状态
     */
    private void setContinuingState(int continuingState) {
        synchronized (mState) {
            if (mState.mContiningState == continuingState)
                return;

            mState.mContiningState = continuingState;

            if ( mState.mContiningState != TaskState.CONTINUING_STATE_UNKNOW) {

                mMapFile.setPartialSupportStatus(continuingState);
                handerContinuingState(mState.mContiningState);
                ContentValues values = new ContentValues();
                values.put(Downloads.Impl.COLUMN_CONTINUING_STATE, mState.mContiningState); 
                mDownloadProvider.updateDownload(mInfo.mId, values,null);
            }
        }
    }
    
    private void handerContinuingState(int continuingState){
        mContinuingStatusChange.onContinuingStatusChange(mInfo.mId, continuingState);
    }

    /**
     * Called after a successful completion to take any necessary action on the downloaded file.
     */
    private void finalizeDestinationFile() {
        synchronized (mState) {
            assert (!TextUtils.isEmpty(mState.mFilePath)
                    && !TextUtils.isEmpty(mState.mDestFile));

            try {
                // rename destination file
                new File(mState.mDestFile).delete();
                new File(mState.mFilePath).renameTo(new File(mState.mDestFile));
                // TODO make sure the file is readable
//                FileUtils.setPermissions(mInfo.mDestFile, 0644, -1, -1);
                mInfo.mDownlaodFile = mState.mFilePath = null;
            } catch (Throwable e) {
                Log.w(TAG, "finalize destination file error: " + e);
            }
        }
    }

    /**
     * Check if the download has been paused or canceled, stopping the request appropriately if it
     * has been.
     */
    private void checkPausedOrCanceled() throws StopRequestException {
        synchronized (mInfo) {
            if (mInfo.mControl == Downloads.Impl.CONTROL_PAUSED) {
                throw new StopRequestException(
                        mInfo.mStatus == Downloads.Impl.STATUS_INTERRUPTED
                                ? Downloads.Impl.STATUS_INTERRUPTED
                                : Downloads.Impl.STATUS_PAUSED_BY_APP, "download paused by owner");
            }
            if (mInfo.mStatus == Downloads.Impl.STATUS_CANCELED || mInfo.mDeleted != 0) {
                throw new StopRequestException(Downloads.Impl.STATUS_CANCELED, "download canceled");
            }
        }
    }

    /**
     * Check if current connectivity is valid for this request.
     */
    private void checkConnectivity() throws StopRequestException {
        ///> FIXME:
        /*        
        // checking connectivity will apply current policy
        final DownloadInfo.NetworkState networkUsable = mInfo.checkCanUseNetwork();
        if (networkUsable != DownloadInfo.NetworkState.OK) {
            int status = Downloads.Impl.STATUS_WAITING_FOR_NETWORK;
            if (networkUsable == DownloadInfo.NetworkState.UNUSABLE_DUE_TO_SIZE) {
                status = Downloads.Impl.STATUS_QUEUED_FOR_WIFI;
//                mInfo.notifyPauseDueToSize(true);
            } else if (networkUsable == DownloadInfo.NetworkState.RECOMMENDED_UNUSABLE_DUE_TO_SIZE) {
                status = Downloads.Impl.STATUS_QUEUED_FOR_WIFI;
//                mInfo.notifyPauseDueToSize(false);
            }
            throw new StopRequestException(status, networkUsable.name());
        }
        */
    }

    private void reportDownloadData(long bytesWrite) {
        synchronized (mState) {
            mState.mCurrentBytes += bytesWrite;
        }
        reportProgress();
        saveProgress(false);
    }

    /**
     * Report download progress through the database if necessary.
     */
    private void reportProgress() {
        final long now = SystemClock.elapsedRealtime();
        final long sampleDelta = now - mState.mSpeedSampleStart;
        synchronized (mState) {
            if (sampleDelta > 500) {
                final long sampleSpeed = ((mState.mCurrentBytes - mState.mSpeedSampleBytes) * 1000)
                        / sampleDelta;

                if (mState.mSpeed == 0) {
                    mState.mSpeed = sampleSpeed;
                } else {
                    mState.mSpeed = ((mState.mSpeed * 3) + sampleSpeed) / 4;
                }

                // Only notify once we have a full sample window
//                if (mState.mSpeedSampleStart != 0) {
//                    mNotifier.notifyDownloadSpeed(mInfo.mId, mState.mSpeed);
//                }

                mState.mSpeedSampleStart = now;
                mState.mSpeedSampleBytes = mState.mCurrentBytes;
            }

            if (mState.mCurrentBytes - mState.mBytesNotified > Constants.MIN_PROGRESS_STEP &&
                    now - mState.mTimeLastNotification > Constants.MIN_PROGRESS_TIME) {
                mProgressChange.onProgressChange(mInfo.mId, mState.mCurrentBytes, mState.mTotalBytes, mState.mSpeed);
                mState.mBytesNotified = mState.mCurrentBytes;
                mState.mTimeLastNotification = now;
            }
        }
    }

    private void saveProgress(boolean force) {

        final long now = SystemClock.elapsedRealtime();
        synchronized (mState) {
            if (!force) {
                if (mState.mCurrentBytes - mState.mBytesSynced < 1024   // 至少1KB更新一次
                        || now - mState.mTimeLastSync < 2000) {     // 数据库更新频率2秒以上
                    return;
                }
            }
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_CURRENT_BYTES, mState.mCurrentBytes);
            mDownloadProvider.updateDownload(mInfo.mId, values,null);
            mState.mBytesSynced = mState.mCurrentBytes;
            mState.mTimeLastSync = now;
        }
    }

    /**
     * Prepare the destination file to receive data.  If the file already exists, we'll set up
     * appropriately for resumption.
     */
    private boolean openDestinationFile() throws StopRequestException {
        synchronized (mState) {
            if (TextUtils.isEmpty(mState.mFilePath)) {
                // 目标文件为空，文件没有下载过
                return false;
            }

            DownloadingFile mf = new DownloadingFile(mState.mFilePath);
            try {
                if (!mf.open()) {
                    return false;
                }
                // FIXME: 校验mInfo与mMapFile数据一致
//                if (mInfo.mETag != null && mInfo.mETag.equals(mf.getETag())) {
//                    if (Constants.LOGVV) {
//                        Log.d(TAG, "openDestinationFile() unable to resume download, deleting "
//                                + mState.mFilePath);
//                    }
//                    throw new StopRequestException(Downloads.Impl.STATUS_CANNOT_RESUME,
//                            "Trying to resume a download that can't be resumed");
//                }
            } catch (FileFormatException e) {
                // 文件格式错误，删除后重新下载。
                // 因为我们的下载临时文件都会添加".vcdownload"后缀，所以通常可以认为这重名文件就是我们自己生成的缓存文件。
                // 这里如果删除失败，暂时不做处理，后面继续创建该文件，如果出错，再抛出异常。
                new File(mState.mFilePath).delete();
                return false;
            } catch (IOException e) {
                return false;
            }
            if (Constants.LOGV) {
                Log.i(Constants.TAG, "resuming download for id: " + mInfo.mId +
                        ", and state.mFilePath: " + mState.mFilePath);
            }

            mMapFile = mf;
            // All right, we'll be able to resume this download
            if (Constants.LOGV) {
                Log.i(Constants.TAG, "resuming download for id: " + mInfo.mId +
                        ", and starting with file of length: " + mf.getCurrentBytes());
            }

            mState.mCurrentBytes = mMapFile.getCurrentBytes();
            mState.mTotalBytes = mInfo.mTotalBytes;
            mState.mHeaderETag = mInfo.mETag;
            mState.mContiningState = mMapFile.getPartialSupportStatus();
            if (mState.mContiningState == TaskState.CONTINUING_STATE_DETECTING) {
                mState.mContiningState = TaskState.CONTINUING_STATE_UNKNOW;
            }
            // 兼容老版本设计，如果升级后线程数减少，则延续老版本线程数控制
            // TODO: 如果分片数超过了新版线程数，应该按照新的线程数执行，多余的分片排队等候
            mState.mMaxThreadCount = Math.max(mState.mMaxThreadCount, mMapFile.getAssignmentCount());
            if (Constants.LOGV) {
                Log.i(Constants.TAG, "resuming download for id: " + mInfo.mId +
                        ", state.mCurrentBytes: " + mState.mCurrentBytes +
                        ", and mPartialSupported is: " + mState.mContiningState);
            }
            return true;
        }
    }

    private void createDestinationFile() throws StopRequestException {
        synchronized (mState) {
            assert (!TextUtils.isEmpty(mInfo.mDestFile));
            mState.mFilePath = mState.mDestFile + ".vcdownload";

            if (Constants.LOGV) {
                Log.i(Constants.TAG, "create file for id: " + mInfo.mId +
                        ", and state.mFilePath: " + mState.mFilePath);
            }

            DownloadingFile mf = new DownloadingFile(mState.mFilePath);
            if (!mf.create()) {
                throw new StopRequestException(STATUS_FILE_ERROR, "Create file failed.");
            }
            if (Constants.LOGV) {
                Log.i(Constants.TAG, "new download file for id: " + mInfo.mId +
                        ", and state.mFilePath: " + mState.mFilePath);
            }
            mMapFile = mf;
            mInfo.mDownlaodFile = mState.mFilePath;
            if (Constants.LOGV) {
                Log.i(Constants.TAG, "create file for id: " + mInfo.mId +
                        ", state.mCurrentBytes: " + mState.mCurrentBytes +
                        ", and setting mPartialSupported to true: ");
            }
        }
    }

    /**
     * Stores information about the completed download, and notifies the initiating application.
     */
    private void notifyDownloadCompleted(int finalStatus, String errorMsg, int numFailed) {
        updateDownloadResult(finalStatus, errorMsg, numFailed);
        mInfo.notifyDownloadStatus(finalStatus);
    }

    private void updateDownloadResult(int finalStatus, String errorMsg, int numFailed) {
 
        ContentValues values = new ContentValues();
        values.put(Downloads.Impl.COLUMN_STATUS, finalStatus);
        if (mState != null) {
            handerContinuingState(mState.mContiningState);
            values.put(Downloads.Impl.COLUMN_FILE_PATH, mState.mDestFile == null ? "" : mState.mDestFile);
            values.put(Downloads.Impl.COLUMN_DOWNLOAD_FILE_PATH, mState.mFilePath == null ? "" : mState.mFilePath);
            values.put(Downloads.Impl.COLUMN_MIME_TYPE, mState.mMimeType == null ? "" : mState.mMimeType);
            values.put(Downloads.Impl.COLUMN_CONTINUING_STATE, mState.mContiningState);
            values.put(Downloads.Impl.COLUMN_LAST_MODIFICATION, mSystemFacade.currentTimeMillis());
            values.put(Downloads.Impl.COLUMN_FAILED_CONNECTIONS, numFailed);
            values.put(Constants.RETRY_AFTER_X_REDIRECT_COUNT, mState.mRetryAfter);

            if (!TextUtils.equals(mInfo.mUri, mState.mRequestUri)) {
                values.put(Downloads.Impl.COLUMN_URI, mState.mRequestUri);
            }
        }

        // save the error message. could be useful to developers.
        if (!TextUtils.isEmpty(errorMsg)) {
            values.put(Downloads.Impl.COLUMN_ERROR_MSG, errorMsg);
        }
        mDownloadProvider.updateDownload(mInfo.mId, values,null);
    }

    public static long getHeaderFieldLong(URLConnection conn, String field, long defaultValue) {
        try {
            return Long.parseLong(conn.getHeaderField(field));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Return if given status is eligible to be treated as
     * {@link com.polar.browser.download_refactor.Downloads.Impl#STATUS_WAITING_TO_RETRY}.
     */
    public static boolean isStatusRetryable(int status) {
        switch (status) {
            case STATUS_HTTP_DATA_ERROR:
            case HTTP_UNAVAILABLE:
            case HTTP_INTERNAL_ERROR:
                return true;
            default:
                return false;
        }
    }
}
