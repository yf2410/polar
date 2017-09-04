package com.polar.browser.download_refactor;

//import android.annotation.SdkConstant;
//import android.annotation.SdkConstant.SdkConstantType;

public class UiStatusDefine {
    /**
     * An identifier for a particular download, unique across the system.  Clients use this ID to
     * make subsequent calls related to the download.
     */
    public final static String COLUMN_ID = Downloads.Impl._ID;

    /**
     * URI to be downloaded.
     */
    public final static String COLUMN_URI = Downloads.Impl.COLUMN_URI;

    /**
     * Internet Media Type of the downloaded file.  If no value is provided upon creation, this will
     * initially be null and will be filled in based on the server's response once the download has
     * started.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc1590.txt">RFC 1590, defining Media Types</a>
     */
    public final static String COLUMN_MEDIA_TYPE = "media_type";

    /**
     * Total size of the download in bytes.  This will initially be -1 and will be filled in once
     * the download starts.
     */
    public final static String COLUMN_TOTAL_SIZE_BYTES = "total_size";

    /**
     * Uri where downloaded file will be stored.  If a destination is supplied by client, that URI
     * will be used here.  Otherwise, the value will initially be null and will be filled in with a
     * generated URI once the download has started.
     */
    public final static String COLUMN_LOCAL_URI = "local_uri";

    public static final String COLUMN_VIRUSSTATUS = "viruscheck";

    // 数据库存储的是目标文件路径，如果需要URI，需要经过转换
    /**
     * The pathname of the file where the download is stored.
     */
    public final static String COLUMN_LOCAL_FILENAME = "local_filename";

    /**
     * Current status of the download, as one of the STATUS_* constants.
     */
    public final static String COLUMN_STATUS = Downloads.Impl.COLUMN_STATUS;
    
    public final static String COLUMN_CONTINUING_STATE = Downloads.Impl.COLUMN_CONTINUING_STATE;

    /**
     * Provides more detail on the status of the download.  Its meaning depends on the value of
     * {@link #COLUMN_STATUS}.
     *
     * When {@link #COLUMN_STATUS} is {@link #STATUS_FAILED}, this indicates the type of error that
     * occurred.  If an HTTP error occurred, this will hold the HTTP status code as defined in RFC
     * 2616.  Otherwise, it will hold one of the ERROR_* constants.
     *
     * When {@link #COLUMN_STATUS} is {@link #STATUS_PAUSED}, this indicates why the download is
     * paused.  It will hold one of the PAUSED_* constants.
     *
     * If {@link #COLUMN_STATUS} is neither {@link #STATUS_FAILED} nor {@link #STATUS_PAUSED}, this
     * column's value is undefined.
     *
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1.1">RFC 2616
     * status codes</a>
     */
    public final static String COLUMN_REASON = "reason";

    /**
     * Number of bytes download so far.
     */
    public final static String COLUMN_BYTES_DOWNLOADED_SO_FAR = "bytes_so_far";

    /**
     * Timestamp when the download was last modified, in {@link System#currentTimeMillis
     * System.currentTimeMillis()} (wall clock time in UTC).
     */
    public final static String COLUMN_LAST_MODIFIED_TIMESTAMP = "last_modified_timestamp";

    /**
     * @hide
     */
    public final static String COLUMN_ALLOW_WRITE = Downloads.Impl.COLUMN_ALLOW_WRITE;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to start.
     */
    public final static int STATUS_PENDING = 1 << 0;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is currently running.
     */
    public final static int STATUS_RUNNING = 1 << 1;

    /**
     * Value of {@link #COLUMN_STATUS} when the download is waiting to retry or resume.
     */
    public final static int STATUS_PAUSED = 1 << 2;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has successfully completed.
     */
    public final static int STATUS_SUCCESSFUL = 1 << 3;

    /**
     * Value of {@link #COLUMN_STATUS} when the download has failed (and will not be retried).
     */
    public final static int STATUS_FAILED = 1 << 4;

    /**
     * Value of COLUMN_ERROR_CODE when the download has completed with an error that doesn't fit
     * under any other error code.
     */
    public final static int ERROR_UNKNOWN = 1000;

    /**
     * Value of {@link #COLUMN_REASON} when a storage issue arises which doesn't fit under any
     * other error code. Use the more specific {@link #ERROR_INSUFFICIENT_SPACE} and
     * {@link #ERROR_DEVICE_NOT_FOUND} when appropriate.
     */
    public final static int ERROR_FILE_ERROR = 1001;

    /**
     * Value of {@link #COLUMN_REASON} when an HTTP code was received that download manager
     * can't handle.
     */
    public final static int ERROR_UNHANDLED_HTTP_CODE = 1002;

    public final static int ERROR_URL_FAILURE = 1003;

    /**
     * Value of {@link #COLUMN_REASON} when an error receiving or processing data occurred at
     * the HTTP level.
     */
    public final static int ERROR_HTTP_DATA_ERROR = 1004;

    /**
     * Value of {@link #COLUMN_REASON} when there were too many redirects.
     */
    public final static int ERROR_TOO_MANY_REDIRECTS = 1005;

    /**
     * Value of {@link #COLUMN_REASON} when there was insufficient storage space. Typically,
     * this is because the SD card is full.
     */
    public final static int ERROR_INSUFFICIENT_SPACE = 1006;

    /**
     * Value of {@link #COLUMN_REASON} when no external storage device was found. Typically,
     * this is because the SD card is not mounted.
     */
    public final static int ERROR_DEVICE_NOT_FOUND = 1007;

    /**
     * Value of {@link #COLUMN_REASON} when some possibly transient error occurred but we can't
     * resume the download.
     */
    public final static int ERROR_CANNOT_RESUME = 1008;

    /**
     * Value of {@link #COLUMN_REASON} when the requested destination file already exists (the
     * download manager will not overwrite an existing file).
     */
    public final static int ERROR_FILE_ALREADY_EXISTS = 1009;

    /**
     * Value of {@link #COLUMN_REASON} when the download has failed because of
     * {@link android.net.NetworkPolicyManager} controls on the requesting application.
     *
     * @hide
     */
    public final static int ERROR_BLOCKED = 1010;

    public final static int RUN_DETAIL_PREPARING = 1;
    public final static int RUN_DETAIL_RUNNING = 2;
    public final static int RUN_DETAIL_PAUSING = 3;

    /**
     * Value of {@link #COLUMN_REASON} when the download is paused because some network error
     * occurred and the download manager is waiting before retrying the request.
     */
    public final static int PAUSED_WAITING_TO_RETRY = 11;

    /**
     * Value of {@link #COLUMN_REASON} when the download is waiting for network connectivity to
     * proceed.
     */
    public final static int PAUSED_WAITING_FOR_NETWORK = 12;

    /**
     * Value of {@link #COLUMN_REASON} when the download exceeds a size limit for downloads over
     * the mobile network and the download manager is waiting for a Wi-Fi connection to proceed.
     */
    public final static int PAUSED_QUEUED_FOR_WIFI = 13;

    /**
     * Value of {@link #COLUMN_REASON} when the download is paused for some other reason.
     */
    public final static int PAUSED_UNKNOWN = 14;

//    /**
//     * Broadcast intent action sent by the download manager when a download completes.
//     */
////    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
//    public final static String ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";
//
//    /**
//     * Broadcast intent action sent by the download manager when the user clicks on a running
//     * download, either from a system notification or from the downloads UI.
//     */
////    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
//    public final static String ACTION_NOTIFICATION_CLICKED =
//            "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
//
//    /**
//     * Intent action to launch an activity to display all downloads.
//     */
////    @SdkConstant(SdkConstantType.ACTIVITY_INTENT_ACTION)
//    public final static String ACTION_VIEW_DOWNLOADS = "android.intent.action.VIEW_DOWNLOADS";
//
//    /**
//     * Intent extra included with {@link #ACTION_VIEW_DOWNLOADS} to start DownloadApp in
//     * sort-by-size mode.
//     */
//    public final static String INTENT_EXTRAS_SORT_BY_SIZE =
//            "com.ijinshan.browser.downloads.extra_sortBySize";
//
//    /**
//     * Intent extra included with {@link #ACTION_DOWNLOAD_COMPLETE} intents, indicating the ID (as a
//     * long) of the download that just completed.
//     */
//    public static final String EXTRA_DOWNLOAD_ID = "extra_download_id";
//
//    /**
//     * When clicks on multiple notifications are received, the following
//     * provides an array of download ids corresponding to the download notification that was
//     * clicked. It can be retrieved by the receiver of this
//     * Intent using {@link android.content.Intent#getLongArrayExtra(String)}.
//     */
//    public static final String EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS = "extra_click_download_ids";
    
    public static int getReason(int status) {
        switch (translateStatus(status)) {
            case STATUS_RUNNING:
                return getRunningDetail(status);

            case STATUS_FAILED:
                return getErrorCode(status);

            case STATUS_PAUSED:
                return getPausedReason(status);

            default:
                return 0; // arbitrary value when status is not an error
        }
    }

    public static int getRunningDetail(int status) {
        switch (status) {
            case Downloads.Impl.STATUS_PREPARING:
                return RUN_DETAIL_PREPARING;
            case Downloads.Impl.STATUS_PAUSING:
                return RUN_DETAIL_PAUSING;
            case Downloads.Impl.STATUS_RUNNING:
            default:
                return RUN_DETAIL_RUNNING;
        }
    }

    public static int getPausedReason(int status) {
        switch (status) {
            case Downloads.Impl.STATUS_WAITING_TO_RETRY:
                return PAUSED_WAITING_TO_RETRY;

            case Downloads.Impl.STATUS_WAITING_FOR_NETWORK:
                return PAUSED_WAITING_FOR_NETWORK;

            case Downloads.Impl.STATUS_QUEUED_FOR_WIFI:
                return PAUSED_QUEUED_FOR_WIFI;

            default:
                return PAUSED_UNKNOWN;
        }
    }

    public static int getErrorCode(int status) {
        switch (status) {
            case Downloads.Impl.STATUS_FILE_ERROR:
                return ERROR_FILE_ERROR;

            case Downloads.Impl.STATUS_UNHANDLED_HTTP_CODE:
            case Downloads.Impl.STATUS_UNHANDLED_REDIRECT:
                return ERROR_UNHANDLED_HTTP_CODE;

            case Downloads.Impl.STATUS_HTTP_DATA_ERROR:
                return ERROR_HTTP_DATA_ERROR;

            case Downloads.Impl.STATUS_TOO_MANY_REDIRECTS:
                return ERROR_TOO_MANY_REDIRECTS;

            case Downloads.Impl.STATUS_INSUFFICIENT_SPACE_ERROR:
                return ERROR_INSUFFICIENT_SPACE;

            case Downloads.Impl.STATUS_DEVICE_NOT_FOUND_ERROR:
                return ERROR_DEVICE_NOT_FOUND;

            case Downloads.Impl.STATUS_CANNOT_RESUME:
                return ERROR_CANNOT_RESUME;

            case Downloads.Impl.STATUS_FILE_ALREADY_EXISTS_ERROR:
                return ERROR_FILE_ALREADY_EXISTS;

            default:
                if (Downloads.Impl.isStatusError(status)
                        && !Downloads.Impl.isStatusCustomClientError(status))
                    return ERROR_URL_FAILURE;
                return ERROR_UNKNOWN;
        }
    }

    public static int translateStatus(int status) {
        switch (status) {
            case Downloads.Impl.STATUS_PENDING:
            case Downloads.Impl.STATUS_QUEUEING:
                return STATUS_PENDING;

            case Downloads.Impl.STATUS_PREPARING:
            case Downloads.Impl.STATUS_RUNNING:
            case Downloads.Impl.STATUS_PAUSING:
                return STATUS_RUNNING;

            case Downloads.Impl.STATUS_INTERRUPTED: // 程序退出中断，Service正常运行之后，这个状态不应该被外部获取到。
            case Downloads.Impl.STATUS_PAUSED_BY_APP:
            case Downloads.Impl.STATUS_WAITING_TO_RETRY:
            case Downloads.Impl.STATUS_WAITING_FOR_NETWORK:
            case Downloads.Impl.STATUS_QUEUED_FOR_WIFI:
                return STATUS_PAUSED;

            case Downloads.Impl.STATUS_SUCCESS:
                return STATUS_SUCCESSFUL;

            default:
                assert Downloads.Impl.isStatusError(status);
                return STATUS_FAILED;
        }
    }
}
